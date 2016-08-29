package com.qianseit.westore.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleFlowIndicator;
import com.qianseit.westore.ui.CommonButton;
import com.qianseit.westore.ui.FlowView;
import com.qianseit.westore.ui.RushBuyCountDownTimerView;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cn.shopex.ecstore.R;


public class MainShoppingFragment extends BaseDoFragment {
	private final int INTERVAL_AUTO_SNAP_FLOWVIEW = 5000;
	private final int TIME_AUTO_INCREASE = 1000;

	private ArrayList<JSONObject> mTopAdsArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();

	private LayoutInflater mLayoutInflater;
	private Point mScreenSize;
	private VolleyImageLoader mVolleyImageLoader;

	private PullToRefreshListView mListView;
	private FlowView mTopAdsView;

	private int mPageNum = 0;
//	private boolean isScrolling = false;
	// private int lastScrollY;

	private boolean isCalcel = false;

	private BaseAdapter mGoodsAdapter;
	private View mAdsLayoutView;
	// private CircleFlowIndicator mTopAdsIndicator;
	private View mAdvertisementView;
	private TextView mAdvertisementViewText;
	private ImageView mAdvertisementViewDelect;
	private float width;
//	private JsonTask mTask;
	private long mNewSysteTime;
	private boolean isLoadEnd;
	private boolean isLoadingData;
	private int totalCount;

	public MainShoppingFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mScreenSize = Run.getScreenSize(mActivity.getWindowManager());
		mLayoutInflater = mActivity.getLayoutInflater();
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = Float.valueOf(dm.widthPixels);
		// 自动创建桌面快捷方式
		if (!Run.loadOptionBoolean(mActivity, Run.pk_shortcut_installed, false)) {
			Run.savePrefs(mActivity, Run.pk_shortcut_installed, true);
			Run.createShortcut(mActivity);
		}

		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(false);
		rootView = inflater.inflate(R.layout.fragment_shangchegn_main, null);
		rootView.findViewById(R.id.fragment_main_category).setOnClickListener(this);
		rootView.findViewById(R.id.fragment_main_goto_top).setOnClickListener(this);
		mListView = (PullToRefreshListView) findViewById(R.id.goods_main_listview);
		mAdsLayoutView = findViewById(R.id.fragment_main_content_container);
		mAdvertisementView = findViewById(R.id.fragment_main_advertisement);
		mAdvertisementViewText = (TextView) findViewById(R.id.fragment_main_advertisement_content);
		mAdvertisementViewDelect = (ImageView) findViewById(R.id.fragment_main_advertisement_delect);
		mAdvertisementViewDelect.setOnClickListener(this);
		mAdvertisementViewText.setFocusable(true);
		mAdvertisementViewText.requestFocus();
		findViewById(R.id.main_top_adsview_foot_flash_Sale).setOnClickListener(
				this);
		findViewById(R.id.main_top_adsview_foot_season)
				.setOnClickListener(this);
		findViewById(R.id.main_top_adsview_foot_new_product)
				.setOnClickListener(this);

		Run.removeFromSuperView(mAdsLayoutView);
		mListView.getRefreshableView().addHeaderView(mAdsLayoutView);
		mTopAdsView = (FlowView) findViewById(R.id.main_top_adsview);
		// mTopAdsIndicator = (CircleFlowIndicator) rootView
		// .findViewById(R.id.main_top_adsview_indicator);
		mGoodsAdapter = new GoodtAdapter();
		mAdsLayoutView.setLayoutParams(new AbsListView.LayoutParams(
				mAdsLayoutView.getLayoutParams()));
		mListView.getRefreshableView().setAdapter(mGoodsAdapter);
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
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
//					isScrolling = false;
//					mGoodsAdapter.notifyDataSetChanged();
				} else {
//					isScrolling = true;
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem >= 2) {
					if (!isCalcel) {
						mAdvertisementView.setVisibility(View.VISIBLE);
					}
					rootView.findViewById(R.id.fragment_main_goto_top)
							.setVisibility(View.VISIBLE);
				} else {
					mAdvertisementView.setVisibility(View.GONE);
					rootView.findViewById(R.id.fragment_main_goto_top)
							.setVisibility(View.GONE);
				}
//				if (totalItemCount < 3 || (mTask != null && mTask.isExcuting))
				if (totalItemCount < 3 || isLoadingData || isLoadEnd)
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

				Run.excuteJsonTask(new JsonTask(),
						new LoadHomeDetailsTask(true));
			}

			@Override
			public void onRefreshMore() {
			}
		});
		
//		showCancelableLoadingDialog();
		rootView.findViewById(R.id.main_top_adsview_foot).setVisibility(View.GONE);
		Run.excuteJsonTask(new JsonTask(), new LoadHomeDetailsTask(true));
		Run.excuteJsonTask(new JsonTask(), new getAdvTextHandler());
//		Run.excuteJsonTask(new JsonTask(), new GetGoodsTask());
		loadNextPage(0);

	}

	private void loadNextPage(int oldPageNum) {
		if (isLoadingData || isLoadEnd) {
			 return;
		}
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			isLoadEnd = false;
			mGoodsArray.clear();
			mGoodsAdapter.notifyDataSetChanged();
			mListView.setRefreshing();
		}
//		if (mTask != null && mTask.isExcuting)
//				return;
//		mTask = new JsonTask();

//		Run.excuteJsonTask(mTask, new GetGoodsTask());
		new JsonTask().execute(new GetGoodsTask());
	}

	/**
	 * 添加购物车
	 *
	 */
	public static class AddCartTask implements JsonTaskHandler {
		private JsonRequestBean.JsonRequestCallback mCallback;
		private DoActivity mActivity;
		private String product_id;
		private int quantity;
		private String bType;

		public AddCartTask(DoActivity activity, JsonRequestBean.JsonRequestCallback callback,
						   String product, int number, String btype) {
			this.mCallback = callback;
			this.mActivity = activity;
			this.product_id = product;
			this.quantity = number;
			this.bType = btype;
		}

		@Override
		public JsonRequestBean task_request() {
			mActivity.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.cart.add").addParams("product_id", product_id)
					.addParams("num", String.valueOf(quantity));
			if (!TextUtils.isEmpty(bType))
				bean.addParams("btype", this.bType);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			mActivity.hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					if (mCallback != null)
						mCallback.task_response(json_str);
				}
			} catch (Exception e) {
			}
		}
	}

	/* 加入购物车成功 */
	private JsonRequestBean.JsonRequestCallback mAddCarCallback = new JsonRequestBean.JsonRequestCallback() {
		@Override
		public void task_response(String jsonStr) {
			//onClick(findViewById(R.id.goods_detail_buy_cancel));
			Run.alert(mActivity, R.string.add_to_shoping_car_success);
			Run.excuteJsonTask(new JsonTask(), new GetCarCountTask());
		}
	};

	/**
	 * 获取购物车数量
	 */
	private class GetCarCountTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					JSONObject object = data.optJSONObject("object");
					JSONArray goods = object.optJSONArray("goods");
					if (goods != null && goods.length() > 0) {
						int count = 0;
						for (int i = 0; i < goods.length(); i++) {
							count += goods.getJSONObject(i).optInt("quantity");
						}
						Run.goodsCounts = count;
						if(MainTabFragmentActivity.mTabActivity!=null){
							MainTabFragmentActivity.mTabActivity.setShoppingCarCount(count);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.cart.get_list");
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
		mGoodsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeMessages(0);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mHandler.removeMessages(1);
	}

	private void reloadMainView(boolean isOutDate) {
		if (mTopAdsArray != null && mTopAdsArray.size() > 0 && !isOutDate) {
			CircleFlowIndicator mTopAdsIndicator = (CircleFlowIndicator) rootView
					.findViewById(R.id.main_top_adsview_indicator);
			mTopAdsView.setAdapter(new FlowAdapter());
			mTopAdsView.setFlowIndicator(mTopAdsIndicator);
			mTopAdsIndicator.setViewFlow(mTopAdsView);

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
				System.out.println("---->>---e ban");
				e.printStackTrace();
			}
		} else {
			mTopAdsView.setVisibility(View.GONE);
			rootView.findViewById(R.id.main_top_adsview_indicator)
					.setVisibility(View.GONE);
		}
	}

	/**
	 * 退换货
	 */
	class BackClas implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
//				hideLoadingDialog_mt();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childs = all.optJSONObject("data");
				}
			} catch (Exception e) {
				System.out.println("---->>---e");
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			isLoadingData = true;
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.afterrec");
			req.addParams("member_id", "112");
			req.addParams("nPage", "1");
			return req;
		}

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_main_feed_back) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_FEEDBACK));
		} else if (v.getId() == R.id.fragment_main_goto_top) {
			mListView.getRefreshableView().setSelection(0);
		} else if (v.getId() == R.id.fragment_main_category) {
			//测试退换货接口
			new JsonTask().execute(new BackClas());
//			startActivity(AgentActivity.intentForFragment(mActivity,
//					AgentActivity.FRAGMENT_CATEGORY_YING));
		} else if (v.getId() == R.id.main_top_adsview_foot_season) {

			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_SEASON_SPECIAL));
		} else if (v.getId() == R.id.main_top_adsview_foot_flash_Sale) {

			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_FLASH_SALE));
		} else if (v.getId() == R.id.main_top_adsview_foot_new_product) {

			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_NEW_PRODUCT));

		} else if (v.getId() == R.id.fragment_main_advertisement_delect) {
			mAdvertisementView.setVisibility(View.GONE);
			isCalcel = true;
		} else {
			super.onClick(v);
		}
	}

	/**
	 * 获取banner
	 */
	public class LoadHomeDetailsTask implements JsonTaskHandler {

		private boolean noShowLoading = false;

		private LoadHomeDetailsTask() {
		}

		private LoadHomeDetailsTask(boolean noShowLoading) {
			this.noShowLoading = noShowLoading;
		}

		@Override
		public JsonRequestBean task_request() {
			if (!noShowLoading) {
				showCancelableLoadingDialog();
			}
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.indexad.get_ad");
			req.addParams("app_ad_key", "1");
			return req;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			mListView.onRefreshComplete();
			findViewById(R.id.main_top_adsview_foot).setVisibility(View.GONE);
			parseHomeAdsJson(json_str, true);
		}
	}

	/**
	 * 解析主屏幕广告json
	 * 
	 * @param json_str
	 * @param needSave
	 * @throws Exception
	 */
	private void parseHomeAdsJson(String json_str, boolean needSave) {
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all)) {
				JSONArray child = all.optJSONArray("data");
				if (child != null && child.length() > 0) {
					// 顶部广告
					mTopAdsArray.clear();
					for (int i = 0; i < child.length(); i++) {
						JSONObject topJson = child.getJSONObject(i);
						mTopAdsArray.add(topJson);
					}
					if (mTopAdsArray.size() > 0) {
						reloadMainView(false);
					} else {
						reloadMainView(true);
					}
					if (needSave) {
						File cacheFile = new File(mActivity.getFilesDir(),
								Run.FILE_HOME_ADS_JSON);
						Run.copyString2File(json_str,
								cacheFile.getAbsolutePath());
					}

				} else {
					if (needSave) // 无需缓存则不重复加载
						loadLocalAdJson();
				}

			} else {
				if (needSave) // 无需缓存则不重复加载
					loadLocalAdJson();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (needSave) // 无需缓存则不重复加载
				loadLocalAdJson();
		}
	}

	// 读取缓存的json
	private void loadLocalAdJson() {
		try {
			File file = new File(mActivity.getFilesDir(),
					Run.FILE_HOME_ADS_JSON);
			String jsonStr = FileUtils.readFileToString(file);
			parseHomeAdsJson(jsonStr, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private OnClickListener mAdViewClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_object) != null) {
				JSONObject data = (JSONObject) v.getTag(R.id.tag_object);
				String urlType = data.optString("url_type");

				if ("goods".equals(urlType)) {
					if (v.getTag(R.id.tag_first) != null) {
						startActivity(AgentActivity.intentForFragment(
								mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
								.putExtra(Run.EXTRA_PRODUCT_ID,
										data.optString("ad_url")));
					} else {
						startActivity(AgentActivity.intentForFragment(
								mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
								.putExtra(Run.EXTRA_CLASS_ID,
										data.optString("ad_url")));
					}
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
			Uri imageUri = Uri.parse(topAdsObject.optString("ad_img"));
			convertView.setTag(R.id.tag_object, topAdsObject);
			convertView.setTag(imageUri);
			mVolleyImageLoader.showImage((ImageView) convertView,
					topAdsObject.optString("ad_img"));

			return convertView;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				if (mTopAdsView.getViewsCount() > 1) {
					int count = mTopAdsView.getViewsCount();
					int curScreen = mTopAdsView.getSelectedItemPosition();
					if (curScreen >= (count - 1))
						mTopAdsView.smoothScrollToScreen(0);
					else
						mTopAdsView.smoothScrollToScreen(curScreen + 1);
				}
				mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
			} else if(msg.what == 1){
				mNewSysteTime += 1;
				mHandler.sendEmptyMessageDelayed(1, TIME_AUTO_INCREASE);
			}
		};
	};

	private class GoodtAdapter extends BaseAdapter {

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

		public View getView(int position, View convertView, ViewGroup parent) {
			final int position2 = position;
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mLayoutInflater
						.inflate(R.layout.goods_item, null);
				viewHolder.imageFragme = (FrameLayout) convertView
						.findViewById(R.id.fragment_goods_item_image);
				viewHolder.imageFragme
						.setLayoutParams(new RelativeLayout.LayoutParams(
								LayoutParams.MATCH_PARENT, (int) (width / 2)));
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
				viewHolder.status2TextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_status2);

				viewHolder.addCarBtn = (CommonButton)convertView
						.findViewById(R.id.add_car);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			JSONObject goodsObject = getItem(position);
			convertView.setTag(R.id.tag_object, goodsObject);
			viewHolder.titleTextView.setText(goodsObject.optString("title"));
			viewHolder.priceTextView.setVisibility(View.VISIBLE);
			viewHolder.priceTextView.setText("￥"
					+ goodsObject.optString("price"));

			viewHolder.addCarBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					JSONObject skus = mGoodsArray.get(position2).optJSONArray("skus").optJSONObject(0);
					int quantity = skus.optInt("quantity");
					if (quantity < 1) {
						Toast.makeText(getActivity(), "库存不足", Toast.LENGTH_SHORT).show();
						return;
					}
					String productID = skus.optString("sku_id");
					Log.e("ldy", "quantity:" + quantity + "--productID:" + productID);
					Run.excuteJsonTask(new JsonTask(), new AddCartTask((DoActivity) mActivity,
							mAddCarCallback,
							productID,
							1, ""));
				}
			});

//			viewHolder.iconImage.setDefaultImageResId(R.drawable.default_img_rect);  
//			viewHolder.iconImage.setErrorImageResId(R.drawable.default_img_rect);  
//			viewHolder.iconImage.setImageUrl(goodsObject.optString("ipad_image_url") , mVolleyImageLoader.getVImageLoader());  
			
			mVolleyImageLoader.showImage(viewHolder.iconImage,
					goodsObject.optString("ipad_image_url"));
			JSONArray SkusStatue = goodsObject.optJSONArray("skus");
			String strPmt = goodsObject.optString("pmt_text");
			viewHolder.markPriceTextView.setText("￥"
					+ goodsObject.optString("market_price"));
			viewHolder.markPriceTextView.getPaint().setFlags(
					Paint.STRIKE_THRU_TEXT_FLAG);
			if (!TextUtils.isEmpty(strPmt) && !"null".equals(strPmt)) {
				String strStatue = goodsObject.optString("pmt_text").trim();
				if ("NEW".equals(strStatue)) {
					viewHolder.status2TextView.setVisibility(View.VISIBLE);
					viewHolder.statusTextView.setVisibility(View.GONE);
					viewHolder.status2TextView.setText(strStatue);
				} else {
					viewHolder.status2TextView.setVisibility(View.GONE);
					viewHolder.statusTextView.setVisibility(View.VISIBLE);
					viewHolder.statusTextView.setText(strStatue);
				}
			} else {
				viewHolder.statusTextView.setVisibility(View.GONE);
			}
			int store = goodsObject.optInt("store");
			if (store <= 0) {
				viewHolder.soldImage.setVisibility(View.VISIBLE);
			} else {
				viewHolder.soldImage.setVisibility(View.GONE);
			}
			if (SkusStatue != null && SkusStatue.length() > 0) {
				JSONObject statueJSON = SkusStatue.optJSONObject(0);
				if (statueJSON != null) {
					JSONObject infoJSON = statueJSON
							.optJSONObject("starbuy_info");
					if (infoJSON != null) {
						if (infoJSON.optInt("type_id") == 2
								&& statueJSON.optBoolean("is_starbuy")) {
							String endTime = infoJSON.optString("end_time");
							int min = 0;
							int hour = 0;
							int sec = 0;
							long time = Long.parseLong(endTime) - mNewSysteTime;
							sec = (int) time;
							if (sec > 60) {
								min = sec / 60;
								sec = sec % 60;
							}
							if (time > 60) {
								hour = min / 60;
								min = min % 60;
							}
							if (viewHolder.timeTextView.setTime(hour, min, sec)) {
								viewHolder.timeView.setVisibility(View.VISIBLE);
								viewHolder.markPriceTextView
										.setVisibility(View.VISIBLE);
								viewHolder.timeTitleTextView
										.setText(getResources().getString(
												R.string.goods_item_time_end));
								viewHolder.timeTextView.start();
							} else {
								viewHolder.timeView.setVisibility(View.GONE);
								viewHolder.markPriceTextView
										.setVisibility(View.GONE);
							}
						} else {
							viewHolder.timeView.setVisibility(View.GONE);
							viewHolder.markPriceTextView
									.setVisibility(View.GONE);
						}
					} else {
						viewHolder.timeView.setVisibility(View.GONE);
						viewHolder.markPriceTextView.setVisibility(View.GONE);
					}
				} else {
					viewHolder.timeView.setVisibility(View.GONE);
					viewHolder.markPriceTextView.setVisibility(View.GONE);
				}
			} else {
				viewHolder.timeView.setVisibility(View.GONE);
				viewHolder.markPriceTextView.setVisibility(View.GONE);
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
		private TextView status2TextView;
		private View timeView;
		private TextView timeTitleTextView;

		private CommonButton addCarBtn;

	}

	/**
	 * 获取商品列表
	 */
	class GetGoodsTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			isLoadingData = false;
			try {
//				hideLoadingDialog_mt();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childs = all.optJSONObject("data");
					totalCount = childs.optInt("total_results");
					if (mGoodsArray.size() >= totalCount) {
						isLoadEnd = true;
					}
					if (mPageNum == 1) {
						mNewSysteTime = childs.optLong("system_time");
						mHandler.sendEmptyMessageDelayed(1, TIME_AUTO_INCREASE);
					}
					if (childs != null) {
						JSONObject items = childs.optJSONObject("items");
						if (items != null) {
//							loadLocalGoods(items);
							JSONArray item = items.optJSONArray("item");
							if (item != null && item.length() > 0) {
								for (int i = 0; i < item.length(); i++) {
									mGoodsArray.add(item.optJSONObject(i));
								}
								mGoodsAdapter.notifyDataSetChanged();
							}
						}
					}

				}
			} catch (Exception e) {
				System.out.println("---->>---e");
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			isLoadingData = true;
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.get_all_list");
			req.addParams("son_object", "json");
			req.addParams("page_no", String.valueOf(mPageNum));
			req.addParams("page_size", "20");
			return req;
		}

	}

	private void loadLocalGoods(JSONObject json) {
		JSONArray item = json.optJSONArray("item");
		if (item != null && item.length() > 0) {
			for (int i = 0; i < item.length(); i++) {
				mGoodsArray.add(item.optJSONObject(i));
			}
			mGoodsAdapter.notifyDataSetChanged();
		}

	}

	class getAdvTextHandler implements JsonTaskHandler {
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray childs = all.optJSONArray("data");
					if (childs != null && childs.length() > 0) {
						JSONObject items = childs.optJSONObject(0);
						if (items != null) {
							String str = "<u>" + items.optString("ad_name")
									+ "</u>";
							mAdvertisementViewText.setText(Html.fromHtml(str));
						}
					}

				}
			} catch (Exception e) {
				System.out.println("---->>---e ads");
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.indexad.get_ad");
			req.addParams("app_ad_key", "2");
			return req;
		}

	}

}
