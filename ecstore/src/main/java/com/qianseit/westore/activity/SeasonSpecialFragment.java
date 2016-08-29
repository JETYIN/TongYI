package com.qianseit.westore.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleView;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;


public class SeasonSpecialFragment extends BaseDoFragment {
	private final int DATA_STUTE = 0 * 100;
	private PullToRefreshListView mListView;
	private TextView mTopFristText;
	private WebView mWebView;
	private TextView mTopTimeText;
	private JsonTask mTask;
	private ImageView mTopImageView;
	private BaseAdapter mGoodsListAdapter;

	private LayoutInflater mLayoutInflater;
	private VolleyImageLoader mVolleyImageLoader;
	private int mPageNum;
	private String StatueImage="";
	private boolean isScrolling = false;
	private View topView;

	private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();
	private SimpleDateFormat mTimeFormat = new SimpleDateFormat("yyyy.MM.dd");
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DATA_STUTE:
				JSONObject data = (JSONObject) msg.obj;
				mTopFristText.setText(data.optString("name"));
				String htemStr = data.optString("description");
				mWebView.loadDataWithBaseURL(null, htemStr, "text/html",
						"utf-8", null);
				String beginTim = data.optString("begin_time");
				Date beginDate = new Date(Long.valueOf(beginTim) * 1000);
				String endTim = data.optString("end_time");
				Date endDate = new Date(Long.valueOf(endTim) * 1000);
				mTopTimeText.setText(mTimeFormat.format(beginDate) + "-"
						+ mTimeFormat.format(endDate));
				JSONArray imgesArray = data.optJSONArray("images");
				if (imgesArray != null && imgesArray.length() > 0) {
					String iconUrl = imgesArray.optString(0);
					mVolleyImageLoader.showImage(mTopImageView, iconUrl);
				}
				break;
			default:
				break;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setShowTitleBar(false);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_season_special, null);
		findViewById(R.id.season_special_back).setOnClickListener(this);
		findViewById(R.id.fragment_main_goto_top).setOnClickListener(this);
		mListView = (PullToRefreshListView) findViewById(R.id.season_special_listview);
		topView = inflater.inflate(R.layout.fragment_seecial_top, null);
		mTopFristText = (TextView) topView
				.findViewById(R.id.season_special_time_top_frist);
		mWebView = (WebView) topView
				.findViewById(R.id.season_special_time_top_Two);
		mTopImageView = (ImageView) topView
				.findViewById(R.id.season_special_time_top_icon);
		mTopTimeText = (TextView) topView
				.findViewById(R.id.season_special_time);
		Run.removeFromSuperView(topView);
		mListView.getRefreshableView().addHeaderView(topView);

		mGoodsListAdapter = new GoodsListAdapter();
		mListView.getRefreshableView().setAdapter(mGoodsListAdapter);
		mListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						JSONObject json = (JSONObject) view
								.getTag(R.id.tag_object);
						String goodsIID = json.optString("goods_id");
						Intent intent = AgentActivity.intentForFragment(
								mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
								.putExtra(Run.EXTRA_CLASS_ID, goodsIID);
						startActivity(intent);

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
				if (firstVisibleItem > visibleItemCount) {
					rootView.findViewById(R.id.fragment_main_goto_top)
							.setVisibility(View.VISIBLE);
				} else {
					rootView.findViewById(R.id.fragment_main_goto_top)
							.setVisibility(View.GONE);
				}
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
		topView.setVisibility(View.INVISIBLE);
		loadNextPage(mPageNum,true);
//		Run.excuteJsonTask(new JsonTask(), new GetGoodsTask());
	}
	
	@Override
	public void onResume() {
		super.onResume();

	}
	
	@Override
	public void onPause() {
		super.onPause();

	}

	private void loadNextPage(int oldPageNum,boolean isFirst) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mGoodsArray.clear();
			mGoodsListAdapter.notifyDataSetChanged();
			if(!isFirst){
				mListView.setRefreshing();
			}
		} else {
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
		case R.id.season_special_back:
			getActivity().finish();
			break;
		case R.id.fragment_main_goto_top:
			mListView.getRefreshableView().setSelection(0);
			break;
		default:
			break;
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
				convertView = mLayoutInflater.inflate(
						R.layout.goods_item_column, null);
				viewHolder.iconImage = (ImageView) convertView
						.findViewById(R.id.goods_item_column_icon);
				viewHolder.titleTextView = (TextView) convertView
						.findViewById(R.id.goods_item_column_title);
				viewHolder.priceTextView = (TextView) convertView
						.findViewById(R.id.goods_item_column_price);
				viewHolder.explainTextView = (TextView) convertView
						.findViewById(R.id.goods_item_column_explain);
				viewHolder.statusImageView = (ImageView) convertView
						.findViewById(R.id.goods_item_column_status);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			JSONObject goodsObject = getItem(position);
			if (goodsObject != null) {
				convertView.setTag(R.id.tag_object, goodsObject);
				viewHolder.titleTextView.setText(goodsObject.optString("name"));
				viewHolder.explainTextView.setText(goodsObject
						.optString("brief"));
				JSONObject productJson = goodsObject.optJSONObject("products");
				if (productJson != null) {
					viewHolder.priceTextView.setText("￥"
							+ productJson.optString("price"));
				}
				mVolleyImageLoader.showImage(viewHolder.iconImage,
						goodsObject.optString("ipad_image_url"));
				if(!TextUtils.isEmpty(StatueImage)){
				  mVolleyImageLoader.showImage(viewHolder.statusImageView,StatueImage);
				}
			}

			return convertView;
		}

	}

	private class ViewHolder {
		private ImageView iconImage;
		private TextView titleTextView;
		private TextView priceTextView;
		private ImageView statusImageView;
		private TextView explainTextView;

	}

	class GetGoodsTask implements JsonTaskHandler {
		
		private boolean isFirst;
		public GetGoodsTask(boolean isFirst){
			this.isFirst=isFirst;
		}
		@Override
		public void task_response(String json_str) {
			try {
				topView.setVisibility(View.VISIBLE);
				hideLoadingDialog_mt();
				if(!isFirst){
					mListView.onRefreshComplete();
				}
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject child = all.optJSONObject("data");
					StatueImage=child.optString("season_image");
					if (child != null) {
						JSONArray listArray = child.optJSONArray("list");
						if (listArray != null && listArray.length() > 0) {
							JSONObject dataTopJson = listArray.optJSONObject(0);
							Message message = new Message();
							message.obj = dataTopJson;
							message.what = DATA_STUTE;
							handler.sendMessage(message);
						}
						JSONArray goodsArray = child.optJSONArray("goods");
						if (goodsArray != null && goodsArray.length() > 0) {
							JSONObject goodsJson = goodsArray.optJSONObject(0);
							if (goodsJson != null) {
								JSONArray itemArray = goodsJson
										.optJSONArray("items");
								if (itemArray != null && itemArray.length() > 0) {
									for (int i = 0; i < itemArray.length(); i++) {
										mGoodsArray.add(itemArray
												.optJSONObject(i));
									}
									mGoodsListAdapter.notifyDataSetChanged();
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
			req.addParams("son_object", "json");
			req.addParams("type_id", "1");
			return req;
		}
	}

}
