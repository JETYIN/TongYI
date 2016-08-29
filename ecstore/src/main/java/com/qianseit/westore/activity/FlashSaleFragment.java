package com.qianseit.westore.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.android.volley.toolbox.NetworkImageView;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.RushBuyCountDownTimerView;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;


public class FlashSaleFragment extends BaseDoFragment {
	private PullToRefreshListView mListView;

	private RelativeLayout mFlashSaleNew;
	private RelativeLayout mFlashSaleTomorrow;

	private int mPageNum=0;
	private RelativeLayout mSelectView;
	private boolean isScrolling = false;
	private boolean isNew = true; // true：今天 false:明天

	private String timeStatue = "today";
	private BaseAdapter mGoodsListAdapter;
	private LayoutInflater mLayoutInflater;
	private VolleyImageLoader mVolleyImageLoader;
	private JsonTask mTask;
	private String newSystemTime;
	private float width;

	private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();
	private int hour = 0;
	private int min = 0;
	private int sec = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = Float.valueOf(dm.widthPixels);
		mActionBar.setShowTitleBar(false);
		mLayoutInflater = this.getLayoutInflater();
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_flash_sale_main, null);
		mListView = (PullToRefreshListView) findViewById(R.id.flash_sale_listview);
		findViewById(R.id.flash_sale_back).setOnClickListener(this);

		mGoodsListAdapter = new GoodsListAdapter();
		mListView.getRefreshableView().setAdapter(mGoodsListAdapter);

		mFlashSaleNew = (RelativeLayout) findViewById(R.id.flash_sale_new);
		mFlashSaleNew.setOnClickListener(mSaleClickListener);
		mFlashSaleTomorrow = (RelativeLayout) findViewById(R.id.flash_sale_tomorrow);

		mSelectView = mFlashSaleNew;
		mSelectView.setSelected(true);

		mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
		mFlashSaleTomorrow.setOnClickListener(mSaleClickListener);
		mListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (isNew) {
							JSONObject json = (JSONObject) view
									.getTag(R.id.tag_object);
							String goodsIID = json.optString("goods_id");
							Intent intent = AgentActivity.intentForFragment(
									mActivity,
									AgentActivity.FRAGMENT_GOODS_DETAIL)
									.putExtra(Run.EXTRA_CLASS_ID, goodsIID);
							startActivity(intent);
						}

					}

				});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					isScrolling = false;
					mGoodsListAdapter.notifyDataSetChanged();
				} else {
					isScrolling = true;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum,false);
			}
		});
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0,false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		showCancelableLoadingDialog();
		loadNextPage(mPageNum,true);
	}

	private void loadNextPage(int oldPageNum,boolean isFirst) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mGoodsArray.clear();
			mGoodsListAdapter.notifyDataSetChanged();
			if(!isFirst){
				mListView.setRefreshing();
			}
		}else{
		 if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
			return;
		}
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetGoodsTask(isFirst));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.flash_sale_back:
			getActivity().finish();
			break;
		default:
			break;
		}
	}

	private OnClickListener mSaleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mSelectView.setSelected(false);
			mSelectView.getChildAt(1).setVisibility(View.GONE);
			if (v == mFlashSaleNew) {

				mSelectView = mFlashSaleNew;
				timeStatue = "today";
				isNew = true;
			} else if (v == mFlashSaleTomorrow) {

				mSelectView = mFlashSaleTomorrow;
				timeStatue = "tomorrow";
				isNew = false;
			}
			mSelectView.setSelected(true);
			mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
			showCancelableLoadingDialog();
			loadNextPage(0,true);
		}
	};

	@Override
	public void onResume() {
		super.onResume();

	}
	
	@Override
	public void onPause() {
		super.onPause();

	}

	@SuppressLint("SimpleDateFormat")
	private String getTime(int day) {
		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(time);
		Date newDate = getDateAfter(date, day);
		String tr = format.format(newDate);
		return tr;
	}

	// 得到今天的后几天
	private Date getDateAfter(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime();
	}

	// 得到今天的前几天
	private Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	private class GoodsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGoodsArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mGoodsArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mLayoutInflater
						.inflate(R.layout.goods_item, null);
				viewHolder.imageFragme=(FrameLayout)convertView.findViewById(R.id.fragment_goods_item_image);
				viewHolder.imageFragme.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int)(width/2)));
				viewHolder.iconImage = (NetworkImageView) convertView
						.findViewById(R.id.fragment_goods_item_icon);
				viewHolder.titleTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_title);
				viewHolder.priceTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_price);
				viewHolder.statusTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_status);
				viewHolder.timeTextView = (RushBuyCountDownTimerView) convertView
						.findViewById(R.id.fragment_goods_item_time_buy);
				viewHolder.timeView = convertView
						.findViewById(R.id.fragment_goods_item_time);
				viewHolder.soldImage = (ImageView) convertView
						.findViewById(R.id.fragment_goods_item_sold);
				viewHolder.markPriceTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_mark_price);
				viewHolder.timeTitleTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_time_title);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			JSONObject goodsObject = getItem(position);
			convertView.setTag(R.id.tag_object, goodsObject);
			viewHolder.titleTextView.setText(goodsObject.optString("name"));
			JSONObject product = goodsObject.optJSONObject("products");
			viewHolder.priceTextView.setText("￥" + product.optString("price"));
			viewHolder.markPriceTextView.setText("￥"
					+ product.optString("mktprice"));
			viewHolder.markPriceTextView.getPaint().setFlags(
					Paint.STRIKE_THRU_TEXT_FLAG);
			if (!isNew) {
				viewHolder.iconImage.setColorFilter(Color
						.parseColor("#998b8a86"));
				viewHolder.timeTitleTextView.setText(getResources().getString(
						R.string.goods_item_time_start));
			} else {
				viewHolder.iconImage.setColorFilter(Color
						.parseColor("#008b8a86"));
				viewHolder.timeTitleTextView.setText(getResources().getString(
						R.string.goods_item_time_end));
			}
			mVolleyImageLoader.showImage(viewHolder.iconImage,
					goodsObject.optString("ipad_image_url"));
			viewHolder.statusTextView
					.setText(goodsObject.optString("pmt_text"));
			viewHolder.statusTextView.setVisibility(View.VISIBLE);
			viewHolder.timeView.setVisibility(View.VISIBLE);
			viewHolder.timeTextView.setTime(hour, min, sec);
			// 开始倒计时
			viewHolder.timeTextView.start();
			int store = product.optInt("store");
			if (store <= 0) {
				viewHolder.soldImage.setVisibility(View.VISIBLE);
			} else {
				viewHolder.soldImage.setVisibility(View.GONE);
			}

			return convertView;
		}

	}

	private class ViewHolder {
		private FrameLayout imageFragme;
		private NetworkImageView iconImage;
		private ImageView soldImage;
		private RushBuyCountDownTimerView timeTextView;
		private TextView titleTextView;
		private TextView priceTextView;
		private TextView markPriceTextView;
		private TextView statusTextView;
		private View timeView;
		private TextView timeTitleTextView;

	}

	class GetGoodsTask implements JsonTaskHandler {
		private boolean isfirst;
		
		public GetGoodsTask(boolean isFirst){
		this.isfirst=isFirst;	
		}

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				if(!isfirst){
					mListView.onRefreshComplete();
				}
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childs = all.optJSONObject("data");
					newSystemTime = childs.optString("system_time");
					if (childs != null) {
						JSONArray goodsArray = childs.optJSONArray("goods");
						JSONArray listArray = childs.optJSONArray("list");
						if (listArray != null && listArray.length() > 0) {
							analyticalBuyTime(listArray);
						}
						if (goodsArray != null && goodsArray.length() > 0) {
							JSONObject goodsJson = goodsArray.optJSONObject(0);
							if (goodsJson != null) {
								JSONArray itemArray = goodsJson
										.optJSONArray("items");
								if (itemArray != null && itemArray.length() > 0) {
									 loadLocalGoods(itemArray);
								}
							}

						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getGroup");
			req.addParams("page_no", String.valueOf(mPageNum));
			req.addParams("paget_size", String.valueOf(20));
			req.addParams("son_object", "json");
			req.addParams("type_id", "2");
			req.addParams("day",timeStatue);
			return req;
		}
	}

	private void loadLocalGoods(JSONArray jsonArray) {
	    for (int i = 0; i < jsonArray.length(); i++) {
			mGoodsArray.add(jsonArray.optJSONObject(i));
		}
		mGoodsListAdapter.notifyDataSetChanged();
	}

	private void analyticalBuyTime(JSONArray jsonArray) {
		JSONObject jsonData = jsonArray.optJSONObject(0);
		long SecMillis;
		long endTime = 0;
		long time = Long.parseLong(newSystemTime);// 获取系统当前时间
		if (isNew) {
			String timeStr = jsonData.optString("end_time");
			SecMillis = Long.parseLong(timeStr);
			endTime = SecMillis - time;

		} else {
			String timeStr = jsonData.optString("begin_time");
			SecMillis = Long.parseLong(timeStr);
			endTime = SecMillis - time;
		}
		secToTime(endTime);
	}

	private void secToTime(Long time) {
		sec = time.intValue();
		if (sec > 60) {
			min = sec / 60;
			sec = sec % 60;
		}
		if (time > 60) {
			hour = min / 60;
			min = min % 60;
		}
	}

}
