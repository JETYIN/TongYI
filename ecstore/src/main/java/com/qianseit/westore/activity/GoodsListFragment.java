package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
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
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GoodsListFragment extends BaseDoFragment {
	private String mKeywords;
	private String mCategoryId;
	private String mVitualCategoryId;
	private String mGoodsListTitle;
	private String mSortKey;

	private int mPageNum;
	private View mSelectView;

	private RelativeLayout mSortDefaultView;
	private RelativeLayout mSortHotView;
	private RelativeLayout mSortBuyCountView;
	private RelativeLayout mSortPriceView;

	private LayoutInflater mLayoutInflater;

	private PullToRefreshListView mListView;
	private BaseAdapter mGoodsListAdapter;
	private VolleyImageLoader mVolleyImageLoader;
	private TextView mTiteTextView;

	private boolean isFine = true; // true:精选 false:其他
	private String newTime;
	private JsonTask mTask;

	private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(true);
		Intent data = mActivity.getIntent();
		mKeywords = data.getStringExtra(Run.EXTRA_KEYWORDS);
		mCategoryId = data.getStringExtra(Run.EXTRA_CLASS_ID);
		mVitualCategoryId = data.getStringExtra(Run.EXTRA_VITUAL_CATE);
		mGoodsListTitle = data.getStringExtra(Run.EXTRA_TITLE);
		mLayoutInflater = mActivity.getLayoutInflater();
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_main_goods_list, null);
		mListView = (PullToRefreshListView) findViewById(R.id.main_goods_list_listview);
		findViewById(R.id.fragment_goods_list_back).setOnClickListener(this);
		mTiteTextView = (TextView) findViewById(R.id.fragment_goods_list_title);

		mSortDefaultView = (RelativeLayout) findViewById(R.id.main_goods_list_topbar_sort_default);
		mSortDefaultView.setOnClickListener(mSortClickListener);
		mSortPriceView = (RelativeLayout) findViewById(R.id.main_goods_list_topbar_sort_price);
		mSortPriceView.setOnClickListener(mSortClickListener);
		mSortBuyCountView = (RelativeLayout) findViewById(R.id.main_goods_list_topbar_sort_sales);
		mSortBuyCountView.setOnClickListener(mSortClickListener);
		mSortHotView = (RelativeLayout) findViewById(R.id.main_goods_list_topbar_sort_hot);
		mSortHotView.setOnClickListener(mSortClickListener);

		mGoodsListAdapter = new GoodsListAdapter();
		mListView.getRefreshableView().setAdapter(mGoodsListAdapter);
		mListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						JSONObject json = (JSONObject) view
								.getTag(R.id.tag_object);
						String goodsIID = json.optString("iid");
						Intent intent = AgentActivity.intentForFragment(
								mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
								.putExtra(Run.EXTRA_CLASS_ID, goodsIID);
						startActivity(intent);

					}

				});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
//					isScrolling = false;
//					mGoodsListAdapter.notifyDataSetChanged();
//				} else {
//					isScrolling = true;
//				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 3)
					loadNextPage(mPageNum);
			}
		});
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0);
			}

			@Override
			public void onRefreshMore() {
			}
		});

//		mSelectView = mSortDefaultView;
//		mSelectView.setSelected(true);
		mSortClickListener.onClick(mSortDefaultView);
//		mSelectView.getChildAt(1).setVisibility(View.VISIBLE);

	}

	private OnClickListener mSortClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mTask != null && mTask.isExcuting){
				//正在加载数据时候取消之前的线程
				mTask.onCancelled();
				return;
			}
			if (mSelectView != null && mSelectView == v) {
				return;
			}
			if (mSelectView != null) {
				mSelectView.setSelected(false);
				((RelativeLayout)mSelectView).getChildAt(1).setVisibility(View.GONE);
			}
			mSelectView = v;
			if (v == mSortDefaultView) {
				isFine = true;
//				mSelectView = mSortDefaultView;
				mSortKey = null;
			} else if (v == mSortHotView) {
				isFine = false;
//				mSelectView = mSortHotView;
				mSortKey = "uptime desc";
			} else if (v == mSortBuyCountView) {
				isFine = false;
//				mSelectView = mSortBuyCountView;
				if (TextUtils.equals("buy_count desc", mSortKey)) {
					mSortKey = "buy_count asc";
				} else {
					mSortKey = "buy_count desc";
				}
			} else if (v == mSortPriceView) {
				isFine = false;
//				mSelectView = mSortPriceView;
				if (TextUtils.equals(mSortKey, "price asc")) {
					mSortKey = "price desc";
				} else {
					mSortKey = "price asc";
				}
			}
			v.setSelected(true);
			((RelativeLayout)v).getChildAt(1).setVisibility(View.VISIBLE);
			loadNextPage(0);
		}
	};

	private void loadNextPage(int oldPageNum) {
		if (mTask != null && mTask.isExcuting)
			return;
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mGoodsArray.clear();
			mGoodsListAdapter.notifyDataSetChanged();
			mListView.setRefreshing();
		}
		mTask = new JsonTask();
		if (isFine) {
			Run.excuteJsonTask(mTask, new GetFineGoodsTask());
		} else {
			Run.excuteJsonTask(mTask, new GetGoodsTask());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fragment_goods_list_back:
			getActivity().finish();
			break;

		default:
			break;
		}

		super.onClick(v);
	}

	@Override
	public void onResume() {
		super.onResume();
		mTiteTextView.setText(mGoodsListTitle);
		if (mGoodsListTitle.contains("母婴")) {

		} else if (mGoodsListTitle.contains("美妆")) {

		} else if (mGoodsListTitle.contains("家居")) {

		} else if (mGoodsListTitle.contains("保健")) {

		} else if (mGoodsListTitle.contains("零食")) {

		} else if (mGoodsListTitle.contains("厨房用品")){

		} else if (mGoodsListTitle.contains("锅具")) {

		} else if (mGoodsListTitle.contains("刀具")) {

		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mGoodsListTitle.contains("母婴")) {

		} else if (mGoodsListTitle.contains("美妆")) {

		} else if (mGoodsListTitle.contains("家居")) {

		} else if (mGoodsListTitle.contains("保健")) {

		} else if (mGoodsListTitle.contains("零食")) {

		} else if (mGoodsListTitle.contains("厨房用品")){

		} else if (mGoodsListTitle.contains("锅具")) {

		} else if (mGoodsListTitle.contains("刀具")) {

		}
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
				viewHolder.titleTextView
						.setText(goodsObject.optString("title"));
				viewHolder.priceTextView.setText("￥"
						+ goodsObject.optString("price"));
				mVolleyImageLoader.showImage(viewHolder.iconImage,
						goodsObject.optString("ipad_image_url"));
				String strPmt = goodsObject.optString("pmt_text");
				if (!TextUtils.isEmpty(strPmt)&&!"null".equals(strPmt)) {
					viewHolder.statusTextView.setText(strPmt);
					viewHolder.statusTextView.setVisibility(View.VISIBLE);
				}else{
					viewHolder.statusTextView.setVisibility(View.GONE);
				}
				int store=goodsObject.optInt("store");
				if(store<=0){
					viewHolder.soldImage.setVisibility(View.VISIBLE);
				}else{
					viewHolder.soldImage.setVisibility(View.GONE);
				}
				JSONArray SkusStatue = goodsObject.optJSONArray("skus");
				if (SkusStatue != null && SkusStatue.length() > 0) {
					JSONObject statueJSON = SkusStatue.optJSONObject(0);
					if (statueJSON != null) {
						JSONObject infoJSON = statueJSON
								.optJSONObject("starbuy_info");
						if (infoJSON != null) {
							if (infoJSON.optInt("type_id") == 2
									&& statueJSON.optBoolean("is_starbuy")) {
								String endTime = infoJSON
										.optString("end_time");
								int min=0;
								int hour=0;
								int sec=0;
								int time=Integer.parseInt(endTime)-Integer.parseInt(newTime);
								sec=time;
								if (sec > 60){
									min = sec / 60;
									sec = sec % 60;
								}
								if (time > 60) {
									hour = min / 60;
									min = min % 60;
								}
								
								if(viewHolder.timeTextView.setTime(hour, min, sec)){
									viewHolder.timeView.setVisibility(View.VISIBLE);
									viewHolder.timeTitleTextView.setText(getResources().getString(R.string.goods_item_time_end));
									viewHolder.timeTextView.start();
									viewHolder.markPriceTextView.setVisibility(View.VISIBLE);
									viewHolder.markPriceTextView.setText("￥"
											+ goodsObject.optString("market_price"));
									viewHolder.markPriceTextView.getPaint().setFlags(
											Paint.STRIKE_THRU_TEXT_FLAG);
								}else{
									viewHolder.timeView.setVisibility(View.GONE);
									viewHolder.markPriceTextView.setVisibility(View.GONE);
								}
							}else{
								viewHolder.timeView.setVisibility(View.GONE);
								viewHolder.markPriceTextView.setVisibility(View.GONE);
							}
						}else{
							viewHolder.timeView.setVisibility(View.GONE);
							viewHolder.markPriceTextView.setVisibility(View.GONE);
						}
					}else{
						viewHolder.timeView.setVisibility(View.GONE);
						viewHolder.markPriceTextView.setVisibility(View.GONE);
					}
				}else{
					viewHolder.timeView.setVisibility(View.GONE);
					viewHolder.markPriceTextView.setVisibility(View.GONE);
				}
	
			return convertView;
		}
	}

	private class ViewHolder {
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

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				mListView.onRefreshComplete();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childs = all.optJSONObject("data");
			       newTime = childs.optString("system_time");
					if (childs != null) {
						JSONObject items = childs.optJSONObject("items");
						if (items != null) {
							loadLocalGoods(items);
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
					"mobileapi.goods.get_all_list");
			req.addParams("page_no", String.valueOf(mPageNum));
			if (!TextUtils.isEmpty(mCategoryId))
				req.addParams("cat_id", mCategoryId);
			if (!TextUtils.isEmpty(mKeywords))
				req.addParams("search_keyword", mKeywords);
			if (!TextUtils.isEmpty(mSortKey))
				req.addParams("orderby", mSortKey);
			if (!TextUtils.isEmpty(mVitualCategoryId))
				req.addParams("virtual_cat_id", mVitualCategoryId);
			req.addParams("son_object", "json");
			return req;
		}

	}

	class GetFineGoodsTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				mListView.onRefreshComplete();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childsJSONObject = all.optJSONObject("data");
					 newTime = childsJSONObject.optString("system_time");
					if (childsJSONObject != null) {
						JSONObject goodsJSONObject = childsJSONObject
								.optJSONObject("0");
						if (goodsJSONObject != null) {							
							JSONArray goodsArray = goodsJSONObject
									.optJSONArray("goods");
							if (goodsArray != null && goodsArray.length() > 0) {
								for (int i = 0; i < goodsArray.length(); i++) {
									mGoodsArray
											.add(goodsArray.optJSONObject(i));
								}
								mGoodsListAdapter.notifyDataSetChanged();
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
					"mobileapi.salesgoods.sales_list");
			req.addParams("page_no", String.valueOf(mPageNum));
			req.addParams("rule_id", String.valueOf("1"));
			req.addParams("son_object", "json");
			if (!TextUtils.isEmpty(mCategoryId))
				req.addParams("cat_id", mCategoryId);
			if (!TextUtils.isEmpty(mKeywords))
				req.addParams("search_keyword", mKeywords);
			if (!TextUtils.isEmpty(mSortKey))
				req.addParams("orderby", mSortKey);
	        if (!TextUtils.isEmpty(mVitualCategoryId))
			 req.addParams("virtual_cat_id", mVitualCategoryId);
			return req;
		}

	}

	private void loadLocalGoods(JSONObject json) {
		JSONArray item = json.optJSONArray("item");
		if (item != null && item.length() > 0) {
//			mGoodsArray.clear();
			for (int i = 0; i < item.length(); i++) {
				mGoodsArray.add(item.optJSONObject(i));
			}
			mGoodsListAdapter.notifyDataSetChanged();
		}

	}

	
}
