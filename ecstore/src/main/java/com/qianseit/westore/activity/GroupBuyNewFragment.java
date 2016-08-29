package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.TwoGoodsAdapter;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleFlowIndicator;
import com.qianseit.westore.ui.FlowView;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class GroupBuyNewFragment extends BaseDoFragment {

	private final int INTERVAL_AUTO_SNAP_FLOWVIEW = 5000;

	private LayoutInflater mLayoutInflater;
	private long systemTime;
	private long endTime;
	private long beginTime;
	private int currentTimeLine = 0;
	private final int pageSize = 20;
	private int pageNum = 0;
	private boolean isLoading;
	private boolean isLoadEnd;
	private boolean isShowCutDownTime = true;

	private FlowView mTopAdsView;
	private TwoGoodsAdapter mAdapter;

	private VolleyImageLoader mVolleyImageLoader;
	private Point mScreenSize;
	private LinearLayout mTimeLineLayout;
	private String specialId;
	private String typeId;
	private ListView mListView;
	private View mAdsLayout;

	private ArrayList<JSONObject> mTopAdsArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mTimeArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mProductArray = new ArrayList<JSONObject>();

	public GroupBuyNewFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		typeId = getArguments().getString(Run.EXTRA_DATA);
		if (typeId.equals("1")) {
			mActionBar.setTitle(R.string.group_buy);
		} else if(typeId.equals("3")){
			mActionBar.setTitle(R.string.pre_sell);
		} else {
			mActionBar.setTitle(R.string.mei_toon_ka);
		}
		mScreenSize = Run.getScreenSize(mActivity.getWindowManager());
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_group_buy_new, null);
		mTopAdsView = (FlowView) rootView
				.findViewById(R.id.fragment_groupbuy_top_adsview);
		mTimeLineLayout = (LinearLayout) rootView
				.findViewById(R.id.fragment_groupbuy_timeline_container);
		mListView = (ListView) rootView.findViewById(R.id.fragment_groupbuy_listview);
		mAdsLayout = rootView.findViewById(R.id.fragment_groupbuy_ads_layout);
		Run.removeFromSuperView(mAdsLayout);
		mAdsLayout.setLayoutParams(new AbsListView.LayoutParams(
				mAdsLayout.getLayoutParams()));
		mListView.addHeaderView(mAdsLayout);
		mAdapter = new GoodsGridAdapter(mActivity,
				mVolleyImageLoader, mProductArray);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5 || isLoadEnd)
					return;

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(pageNum);
			}
		});
		new JsonTask().execute(new GetTopAds());
		new JsonTask().execute(new GetTimeList());
	}

	@Override
	public void onResume() {
		super.onResume();
		mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeMessages(0);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	private void reloadAdsView() {
		if (mTopAdsArray != null && mTopAdsArray.size() > 0) {
			CircleFlowIndicator mTopAdsIndicator = (CircleFlowIndicator) rootView
					.findViewById(R.id.fragment_groupbuy_top_adsview_indicator);
			mTopAdsView.setAdapter(new FlowAdapter());
			mTopAdsView.setFlowIndicator(mTopAdsIndicator);
			mTopAdsIndicator.setViewFlow(mTopAdsView);
			mTopAdsIndicator.setVisibility(View.VISIBLE);
			try {
				JSONObject topAdsObject = mTopAdsArray.get(0);

				// 根据屏幕和图片大小调整显示尺寸
				int width = topAdsObject.optInt("ad_img_w");
				int height = topAdsObject.optInt("ad_img_h");
				int viewHeight = mScreenSize.x * height / width;
				LayoutParams params = mTopAdsView.getLayoutParams();
				params.height = viewHeight;
				mTopAdsView.setLayoutParams(params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class GetTopAds implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray list = all.getJSONArray("data");
					list = list.getJSONObject(0).getJSONArray("items");
					int count = (list != null) ? list.length() : 0;
					for (int i = 0; i < count; i++) {
						mTopAdsArray.add((JSONObject) list.get(i));
					}
					reloadAdsView();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean(
					"mobileapi.salesgoods.get_sales_ads");
		}

	}

	private class FlowAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mTopAdsArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mTopAdsArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				ImageView view = new ImageView(mActivity);
				view.setLayoutParams(new AbsListView.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				view.setScaleType(ScaleType.CENTER_CROP);
				view.setOnClickListener(mAdViewClickListener);
				convertView = view;
			}

			JSONObject topAdsObject = getItem(position);
			convertView.setTag(R.id.tag_object, topAdsObject);
			mVolleyImageLoader.showImage((ImageView) convertView, topAdsObject.optString("ad_img"));
			return convertView;
		}

	}

	private class GoodsGridAdapter extends TwoGoodsAdapter {

		public GoodsGridAdapter(Activity activity, VolleyImageLoader imageLoader,
				ArrayList<JSONObject> items) {
			super(GroupBuyNewFragment.this.mActivity,
					GroupBuyNewFragment.this.mVolleyImageLoader,
					GroupBuyNewFragment.this.mProductArray);
		}

		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_first) != null) {
				JSONObject all = (JSONObject) v.getTag(R.id.tag_first);
				JSONObject product = all.optJSONObject("products");
				openGoodsDetailPage(product.optString("product_id"));
			}
		}

		public void fillupItemView(View convertView, JSONObject all, String key) {
			GroupBuyNewFragment.this.fillupItemView(convertView, all, key);
		}

	}

	private void openGoodsDetailPage(String goodsIID) {
		Intent intent = AgentActivity.intentForFragment(mActivity,
				AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
				Run.EXTRA_PRODUCT_ID, goodsIID);
		mActivity.startActivity(intent);
	}

	private void fillupItemView(View convertView, JSONObject all, String key) {
		convertView.setBackgroundResource(R.drawable.card_background);
		int paddingSmall = mActivity.getResources().getDimensionPixelSize(
				R.dimen.PaddingSmall);
		TextView titleView = (TextView) convertView
				.findViewById(android.R.id.title);
		titleView.setSingleLine(false);
		titleView.setLines(2);
		titleView.setText(all.optString("name"));
		titleView.setPadding(paddingSmall, 0, paddingSmall, 0);
		convertView.setTag(R.id.tag_first, all);

		View summary = convertView.findViewById(android.R.id.summary);
		summary.setVisibility(View.GONE);

		try {
			JSONObject product = all.optJSONObject("products");
			((TextView) convertView.findViewById(android.R.id.text1))
					.setText(Run.buildString("￥", product.optString("price")));
			TextView tv2 = (TextView) convertView
					.findViewById(android.R.id.text2);
			tv2.setText(Run.buildString("￥", product.optString("mktprice")));
			tv2.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			JSONObject imageObj = all.optJSONObject("image_default_url");
			String imageUrl = imageObj.optString("s");
			mVolleyImageLoader.showImage(iconView, imageUrl);

			((FrameLayout) iconView.getParent()).setForeground(null);
		} catch (Exception e) {
		}
	}

	private void addTimeLineView(JSONObject obj) {
		beginTime = obj.optLong("begin_time");
		long endTime = obj.optLong("end_time");
		View view = mLayoutInflater.inflate(R.layout.item_timeline, null);
		if (beginTime <= systemTime && systemTime <= endTime) {
			this.endTime = endTime;
			currentTimeLine = mTimeLineLayout.getChildCount();
			view.setBackgroundColor(0xff8D0011);
			view.setSelected(true);
		} else {
			view.setSelected(false);
			view.setBackgroundColor(Color.TRANSPARENT);
		}
		view.setTag(obj);
		view.setOnClickListener(timeLineClick);
		TextView tv = (TextView) view.findViewById(R.id.item_timeline_name);
		tv.setText(obj.optString("name"));
		mTimeLineLayout.addView(view);
	}

	private OnClickListener timeLineClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			for (int i = 0; i < mTimeLineLayout.getChildCount(); i++) {
				if (view == mTimeLineLayout.getChildAt(i)) {
					if (!view.isSelected()) {

						mTimeLineLayout.getChildAt(i).setBackgroundColor(
								0xff8D0011);
						mTimeLineLayout.getChildAt(i).setSelected(true);
					}
					JSONObject obj = (JSONObject) view.getTag();
					specialId = obj.optString("special_id");
					isLoadEnd = false;
					loadNextPage(0);
				} else {
					mTimeLineLayout.getChildAt(i).setSelected(false);
					mTimeLineLayout.getChildAt(i)
							.setBackgroundColor(0xffBA0014);
				}
			}
		}
	};

	private Handler countDownHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			systemTime = systemTime + 1;
			long remainTime = 0;
			if (systemTime < beginTime) {
				remainTime = systemTime - beginTime;
			} else if (systemTime < endTime){
				remainTime = endTime - systemTime;
			} else {
				if(currentTimeLine!=0){
				((TextView) mTimeLineLayout.getChildAt(
						currentTimeLine).findViewById(R.id.item_timeline_time)).setText("活动已结束");
				}			
				return;
			}
			if (mTimeLineLayout.getChildAt(currentTimeLine) != null) {
				TextView tv = (TextView) mTimeLineLayout.getChildAt(
						currentTimeLine).findViewById(R.id.item_timeline_time);
				if (remainTime < 0) {
					tv.setText("距开始:" + Util.calculateRemainTime(Math.abs(remainTime)));
				} else if(remainTime > 0) {
					tv.setText("距结束:" + Util.calculateRemainTime(remainTime));
				} else {
					new JsonTask().execute(new GetTimeList());
					removeMessages(0);
					return;
				}
			}
			removeMessages(0);
			sendEmptyMessageDelayed(0, 1000);
		}

	};

	private void loadNextPage(int pageNum) {
		if (!isLoading && !isLoadEnd) {
			this.pageNum = 1 + pageNum;
			new JsonTask().execute(new GetProductList(specialId));
		}
	}

	private OnClickListener mAdViewClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_object) != null) {
				JSONObject data = (JSONObject) v.getTag(R.id.tag_object);
				String urlType = data.optString("url_type");
				if ("goods".equals(urlType)) {
					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
							Run.EXTRA_CLASS_ID, data.optString("ad_url")));
				} else if ("article".equals(urlType)) {
					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_ARTICLE_READER).putExtra(
							Run.EXTRA_ARTICLE_ID, data.optString("ad_url")));
				} else if ("virtual_cat".equals(urlType)) {
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_GOODS_LIST)
							.putExtra(Run.EXTRA_VITUAL_CATE,
									data.optString("ad_url"))
							.putExtra(Run.EXTRA_TITLE,
									data.optString("ad_name")));
				} else if ("cat".equals(urlType)) {
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_GOODS_LIST)
							.putExtra(Run.EXTRA_CLASS_ID,
									data.optString("ad_url"))
							.putExtra(Run.EXTRA_TITLE,
									data.optString("ad_name")));
				}
			}
		}
	};

	private class GetTimeList implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			// hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					systemTime = data.optLong("system_time");
					JSONArray list = data.optJSONArray("list");
					int size = list == null ? 0 : list.length();
					mTimeArray.clear();
					pageNum = 0;
					for (int i = 0; i < size; i++) {
						mTimeArray.add(list.getJSONObject(i));
						addTimeLineView(list.getJSONObject(i));
					}
					if (size > 0) {
						isShowCutDownTime = list.getJSONObject(0).optBoolean("cdown");
						specialId = mTimeArray.get(currentTimeLine).optString(
								"special_id");
						loadNextPage(pageNum);
					} else {
						hideLoadingDialog_mt();
					}
					if (isShowCutDownTime) {
						countDownHandler.sendEmptyMessage(0);
					}
				} else {
					hideLoadingDialog_mt();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getGroup");
			req.addParams("type_id", typeId);// type_id --->> 1 团购 2秒杀 3预售 4美通卡特卖
			return req;
		}

	}

	private class GetProductList implements JsonTaskHandler {

		String specialId;

		public GetProductList(String specialId) {
			this.specialId = specialId;
		}

		@Override
		public void task_response(String json_str) {
			isLoading = false;
			hideLoadingDialog_mt();
			if (pageNum <= 1) {
				mProductArray.clear();
			}

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					JSONArray list = data.optJSONArray("items");
					int size = list == null ? 0 : list.length();
					if (size < pageSize) {
						isLoadEnd = true;
					}
					for (int i = 0; i < size; i++) {
						mProductArray.add(list.optJSONObject(i));
					}
					if (mProductArray != null) {
						mAdapter.notifyDataSetChanged();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			if (pageNum <= 1) {
				showLoadingDialog();
			}
			isLoading = true;
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getList");
			req.addParams("special_id", specialId);
			req.addParams("page_size", "" + pageSize);
			req.addParams("page_no", "" + pageNum);
			return req;
		}

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mTopAdsView.getViewsCount() > 1) {
				int count = mTopAdsView.getViewsCount();
				int curScreen = mTopAdsView.getSelectedItemPosition();
				if (curScreen >= (count - 1))
					mTopAdsView.smoothScrollToScreen(0);
				else
					mTopAdsView.smoothScrollToScreen(curScreen + 1);
			}
			mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
		};
	};

}
