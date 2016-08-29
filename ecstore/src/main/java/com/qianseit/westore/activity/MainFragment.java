package com.qianseit.westore.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.google.zxing.CaptureActivity;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleFlowIndicator;
import com.qianseit.westore.ui.CountDownView;
import com.qianseit.westore.ui.CountDownView.TimeEndListener;
import com.qianseit.westore.ui.FlowScrollView;
import com.qianseit.westore.ui.FlowView;
import com.qianseit.westore.ui.NewCountDownView;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshScrollView;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class MainFragment extends BaseDoFragment {
	private final int INTERVAL_AUTO_SNAP_FLOWVIEW = 5000;

	private JSONArray mAdsGroupJsonArray;
	private ArrayList<JSONObject> mTopAdsArray = new ArrayList<JSONObject>();

	private LayoutInflater mLayoutInflater;
	// private ImageLoader mImageLoader;
	private Point mScreenSize;
	private VolleyImageLoader mVolleyImageLoader;

	private PullToRefreshScrollView mScrollView;
	private FlowView mTopAdsView;
	private FrameLayout mGroupBuyContainer;
	private RelativeLayout mSecondKillContainer;
	private Gallery mGallery;
	private LinearLayout mGalleryIndicator;
	private FrameLayout mPresellContainer;
	private FrameLayout mMeiToonContainer;
	private TextView mSecondsKillRemainTime;

	private long remainTime;
	private boolean isStarted;
	private String mFromExtract;

	public MainFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mScreenSize = Run.getScreenSize(mActivity.getWindowManager());
		mLayoutInflater = mActivity.getLayoutInflater();

		// 自动创建桌面快捷方式
		if (!Run.loadOptionBoolean(mActivity, Run.pk_shortcut_installed, false)) {
			Run.savePrefs(mActivity, Run.pk_shortcut_installed, true);
			Run.createShortcut(mActivity);
		}

		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		// mImageLoader = Run.getDefaultImageLoader(mActivity, mResources,
		// false);
		// mImageLoader.setDisplayImageCallback(new DisplayImageCallback() {
		// @Override
		// public boolean displayImage(View v, Drawable drawable) {
		// v.setBackgroundDrawable(drawable);
		// return true;
		// }
		// });
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(false);
		rootView = inflater.inflate(R.layout.fragment_main, null);
		mScrollView = (PullToRefreshScrollView) findViewById(R.id.main_scrollview);
		mScrollView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				Run.excuteJsonTask(new JsonTask(), new LoadHomeDetailsTask());
				new JsonTask().execute(new LoadSecondsKillTask());
			}

			@Override
			public void onRefreshMore() {
			}
		});
		mScrollView.getRefreshableView().setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent ev) {
						lastScrollY = mScrollView.getRefreshableView()
								.getScrollY();
						if (lastScrollY > 500) {
							rootView.findViewById(R.id.fragment_main_goto_top)
									.setVisibility(View.VISIBLE);
						} else {
							rootView.findViewById(R.id.fragment_main_goto_top)
									.setVisibility(View.GONE);
						}
						switch (ev.getAction()) {
						case MotionEvent.ACTION_UP:
							handlerScroll.sendMessageDelayed(
									handlerScroll.obtainMessage(), 5);
							break;
						}
						return false;
					}
				});
		rootView.findViewById(R.id.fragment_main_goto_top).setOnClickListener(
				this);
		mTopAdsView = (FlowView) findViewById(R.id.main_top_adsview);
		mTopAdsView.setParentScrollView((FlowScrollView) mScrollView
				.getRefreshableView());
		// 添加到ScrollView中
		View contentContainer = findViewById(R.id.fragment_main_content_container);
		LayoutParams params = contentContainer.getLayoutParams();
		params = new FrameLayout.LayoutParams(params);
		mScrollView.removeView(contentContainer);
		mScrollView.getRefreshableView().addView(contentContainer, params);

		mGroupBuyContainer = (FrameLayout) findViewById(R.id.fragment_main_ads_groupbuy);
		mSecondKillContainer = (RelativeLayout) findViewById(R.id.fragment_main_ads_secondskill);
		mPresellContainer = (FrameLayout) findViewById(R.id.fragment_main_ads_presell);
		mMeiToonContainer = (FrameLayout) findViewById(R.id.fragment_main_ads_meitoon);

		findViewById(R.id.fragment_main_button_scan).setOnClickListener(this);
		findViewById(R.id.fragment_main_search).setOnClickListener(this);

		findViewById(R.id.fragment_main_feed_back).setOnClickListener(this);

		// 分类按钮
		ViewGroup catesView1 = (ViewGroup) findViewById(R.id.fragment_main_ads_cates1);
		for (int i = 0, c = catesView1.getChildCount(); i < c; i++)
			catesView1.getChildAt(i).setOnClickListener(mCatesClickListener);

		// 加载首页推荐列表
		new JsonTask().execute(new LoadHomeDetailsTask());
		new JsonTask().execute(new LoadSecondsKillTask());
		new JsonTask().execute(new AboutFragment.UpdateTask(
				(DoActivity) mActivity));
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
				e.printStackTrace();
			}
		} else {
			mTopAdsView.setVisibility(View.GONE);
			rootView.findViewById(R.id.main_top_adsview_indicator)
					.setVisibility(View.GONE);
		}
		setAdsView();
	}

	private void setAdsView(){
		if (mAdsGroupJsonArray != null && mAdsGroupJsonArray.length() > 1) {
			((ViewGroup) findViewById(R.id.fragment_main_ads_container))
					.removeAllViews();
			for (ImageView img : imageViews) {
				if (img == null) continue;
				img.destroyDrawingCache();
		        Drawable drawable = img.getDrawable();
		        if (drawable != null && drawable instanceof BitmapDrawable) {
		            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
		            Bitmap bitmap = bitmapDrawable.getBitmap();
		            img.setImageDrawable(null);
		            if (bitmap != null && !bitmap.isRecycled()) {
		                bitmap.recycle();
		                bitmap = null;
		            }
		            img.setImageBitmap(null);
		            if(drawable != null){
		            	drawable.setCallback(null);
		            }
		        }
			}
			imageViews.clear();
			for (int i = 1, c = mAdsGroupJsonArray.length(); i < c; i++) {
				try {
					JSONObject all = mAdsGroupJsonArray.getJSONObject(i);
					View adsView = makeAdsGroupView2(all);
					//View adsView = makeAdsGroupView2(all);
					if (adsView == null)
						continue;
					((ViewGroup) findViewById(R.id.fragment_main_ads_container))
							.addView(adsView);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 生成广告view
	 * 
	 * @param child
	 */
//	private View makeAdsGroupView(JSONObject child) throws Exception {
//		View groupView = null;
//		int viewWidth = mScreenSize.x;
//		JSONArray topAds = child.optJSONArray("items");
//		String group_code = child.optString("group_code");
//
//		if (TextUtils.equals(group_code, "group_2")) {
//			mGroupBuyContainer.removeAllViews();
//			mGroupBuyContainer.setVisibility(View.GONE);
//			if (topAds == null || topAds.length() < 1) {
//				return null;
//			}
//			if (child.optLong("system_time") < child.optLong("begin_time")) {
//				return null;
//			}
//			long countDown = child.optLong("end_time")
//					- child.optLong("system_time");
//			if (countDown < 0) {
//				return null;
//			}
//			mGroupBuyContainer.setVisibility(View.VISIBLE);
//			View view = mLayoutInflater.inflate(
//					R.layout.fragment_main_ads_groupbuy, null);
//			JSONObject obj = topAds.getJSONObject(0);
//			CountDownView countDownView = (CountDownView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_countdown);
//			countDownView.setTime(countDown);
//			countDownView.start();
//			ImageView topview = (ImageView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_title);
//			topview.setOnClickListener(this);
//			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topview
//					.getLayoutParams();
//			int img_w = obj.optInt("ad_img_w");
//			int img_h = obj.optInt("ad_img_h");
//			int screenWidth = mScreenSize.x;
//			params.height = screenWidth * img_h / img_w;
//			topview.setLayoutParams(params);
//			topview.setTag(R.id.tag_object, 1);
//			// Uri imageUri = Uri.parse(obj.optString("ad_img"));
//			// topview.setTag(imageUri);
//			// mImageLoader.showImage(topview, imageUri);
//			mVolleyImageLoader.showImage(topview, obj.optString("ad_img"));
//			LinearLayout layout1 = (LinearLayout) view
//					.findViewById(R.id.fragment_main_ads_group_buy_layout1);
//			LinearLayout layout2 = (LinearLayout) view
//					.findViewById(R.id.fragment_main_ads_group_buy_layout2);
//			LinearLayout.LayoutParams parentParams = (LinearLayout.LayoutParams) layout1
//					.getLayoutParams();
//			img_w = topAds.getJSONObject(1).optInt("ad_img_w");
//			img_h = topAds.getJSONObject(1).optInt("ad_img_h");
//			screenWidth = screenWidth / 2;
//			parentParams.height = screenWidth * img_h / img_w;
//			layout1.setLayoutParams(parentParams);
//			layout2.setLayoutParams(parentParams);
//			String str = "人已团购";
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_l1_count))
//					.setText(topAds.getJSONObject(1).optString("buy_count")
//							+ str);
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_r1_count))
//					.setText(topAds.getJSONObject(2).optString("buy_count")
//							+ str);
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_l2_count))
//					.setText(topAds.getJSONObject(3).optString("buy_count")
//							+ str);
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_r2_count))
//					.setText(topAds.getJSONObject(4).optString("buy_count")
//							+ str);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_l1),
//					topAds.getJSONObject(1), true);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_r1),
//					topAds.getJSONObject(2), true);
//
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_l2),
//					topAds.getJSONObject(3), true);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_r2),
//					topAds.getJSONObject(4), true);
//			mGroupBuyContainer.addView(view);
//		} else if (TextUtils.equals(group_code, "group_3")) {
//			mPresellContainer.removeAllViews();
//			mPresellContainer.setVisibility(View.GONE);
//			if (topAds == null || topAds.length() < 1) {
//				return null;
//			}
//			View view = mLayoutInflater.inflate(
//					R.layout.fragment_main_ads_groupbuy, null);
//			JSONObject obj = topAds.getJSONObject(0);
//			long systemTime = child.optLong("system_time");
//			if (systemTime < child.optLong("begin_time")) {
//				return null;
//			}
//			long countDown = child.optLong("end_time") - systemTime;
//			if (countDown < 0) {
//				return null;
//			}
//			CountDownView countDownView = (CountDownView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_countdown);
//			countDownView.setTime(countDown);
//			countDownView.start();
//			ImageView topview = (ImageView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_title);
//			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topview
//					.getLayoutParams();
//			int img_w = obj.optInt("ad_img_w");
//			int img_h = obj.optInt("ad_img_h");
//			int screenWidth = mScreenSize.x;
//			params.height = screenWidth * img_h / img_w;
//			topview.setLayoutParams(params);
//			// Uri imageUri = Uri.parse(obj.optString("ad_img"));
//			// topview.setTag(imageUri);
//			// mImageLoader.showImage(topview, imageUri);
//			mVolleyImageLoader.showImage(topview, obj.optString("ad_img"));
//			topview.setTag(R.id.tag_object, 3);
//			topview.setOnClickListener(this);
//			LinearLayout layout1 = (LinearLayout) view
//					.findViewById(R.id.fragment_main_ads_group_buy_layout1);
//			LinearLayout layout2 = (LinearLayout) view
//					.findViewById(R.id.fragment_main_ads_group_buy_layout2);
//			LinearLayout.LayoutParams parentParams = (LinearLayout.LayoutParams) layout1
//					.getLayoutParams();
//			img_w = topAds.getJSONObject(1).optInt("ad_img_w");
//			img_h = topAds.getJSONObject(1).optInt("ad_img_h");
//			screenWidth = screenWidth / 2;
//			parentParams.height = screenWidth * img_h / img_w;
//			layout1.setLayoutParams(parentParams);
//			layout2.setLayoutParams(parentParams);
//			String str = "人已预订";
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_l1_count))
//					.setText(topAds.getJSONObject(1).optString("buy_count")
//							+ str);
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_r1_count))
//					.setText(topAds.getJSONObject(2).optString("buy_count")
//							+ str);
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_l2_count))
//					.setText(topAds.getJSONObject(3).optString("buy_count")
//							+ str);
//			((TextView) view
//					.findViewById(R.id.fragment_main_ads_group_buy_r2_count))
//					.setText(topAds.getJSONObject(4).optString("buy_count")
//							+ str);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_l1),
//					topAds.getJSONObject(1), true);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_r1),
//					topAds.getJSONObject(2), true);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_l2),
//					topAds.getJSONObject(3), true);
//			setAdsImageForView(
//					(ImageView) view
//							.findViewById(R.id.fragment_main_ads_group_buy_r2),
//					topAds.getJSONObject(4), true);
//			mPresellContainer.setVisibility(View.VISIBLE);
//			mPresellContainer.addView(view);
//		} else if (TextUtils.equals(group_code, "group_4")) {
//			groupView = mLayoutInflater.inflate(
//					R.layout.fragment_main_ads_group_youxuan, null);
//			JSONObject obj = topAds.getJSONObject(0);
//			int img_w = obj.optInt("ad_img_w");
//			int img_h = obj.optInt("ad_img_h");
//			ImageView topview = (ImageView) groupView
//					.findViewById(R.id.fragment_main_ads_youxuan_top);
//			LayoutParams topParams = topview.getLayoutParams();
//			int screenWidth = mScreenSize.x;
//			topParams.height = screenWidth * img_h / img_w;
//			topview.setLayoutParams(topParams);
//			setAdsImageForView(topview, obj);
//			View contentView = groupView
//					.findViewById(R.id.fragment_main_ads_youxuan_content);
//			LayoutParams contentParams = contentView.getLayoutParams();
//			screenWidth = screenWidth / 3;
//			img_w = topAds.getJSONObject(1).optInt("ad_img_w");
//			img_h = topAds.getJSONObject(1).optInt("ad_img_h");
//			contentParams.height = screenWidth * img_h / img_w;
//			contentView.setLayoutParams(contentParams);
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.fragment_main_ads_youxuan_left),
//					topAds.getJSONObject(1));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.fragment_main_ads_youxuan_m_up),
//					topAds.getJSONObject(2));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.fragment_main_ads_youxuan_m_b),
//					topAds.getJSONObject(3));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.fragment_main_ads_youxuan_r_up),
//					topAds.getJSONObject(4));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.fragment_main_ads_youxuan_r_b),
//					topAds.getJSONObject(5));
//		} else if (TextUtils.equals(group_code, "group_5")) {
//			groupView = mLayoutInflater.inflate(
//					R.layout.fragment_main_ads_group3_new, null);
//			JSONObject obj = topAds.getJSONObject(0);
//			int screenWidth = mScreenSize.x;
//			int img_w = obj.optInt("ad_img_w");
//			int img_h = obj.optInt("ad_img_h");
//			ImageView topView = (ImageView) groupView
//					.findViewById(R.id.Fragment_main_ads_group3_new_top);
//			LayoutParams tParams = topView.getLayoutParams();
//			tParams.height = screenWidth * img_h / img_w;
//			topView.setLayoutParams(tParams);
//			// Uri imageUri = Uri.parse(obj.optString("ad_img"));
//			// topView.setTag(imageUri);
//			// mImageLoader.showImage(topView, imageUri);
//			mVolleyImageLoader.showImage(topView, obj.optString("ad_img"));
//			LinearLayout layout1 = (LinearLayout) groupView
//					.findViewById(R.id.Fragment_main_ads_group3_new_l1);
//			LinearLayout layout2 = (LinearLayout) groupView
//					.findViewById(R.id.Fragment_main_ads_group3_new_l2);
//			screenWidth = mScreenSize.x / 2;
//			obj = topAds.getJSONObject(1);
//			img_w = obj.optInt("ad_img_w");
//			img_h = obj.optInt("ad_img_h");
//			int height = img_h * screenWidth / img_w;
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//					LayoutParams.MATCH_PARENT, height);
//			layout1.setLayoutParams(params);
//			layout2.setLayoutParams(params);
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.Fragment_main_ads_group3_new_img1),
//					topAds.getJSONObject(1));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.Fragment_main_ads_group3_new_img2),
//					topAds.getJSONObject(2));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.Fragment_main_ads_group3_new_img3),
//					topAds.getJSONObject(3));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.Fragment_main_ads_group3_new_img4),
//					topAds.getJSONObject(4));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.Fragment_main_ads_group3_new_img5),
//					topAds.getJSONObject(5));
//			setAdsImageForView(
//					(ImageView) groupView
//							.findViewById(R.id.Fragment_main_ads_group3_new_img6),
//					topAds.getJSONObject(6));
//		} else if (TextUtils.equals(group_code, "group_6")) {
//			LinearLayout contrainer = new LinearLayout(mActivity);
//			contrainer.setPadding(0, Util.dip2px(mActivity, 15), 0, 0);
//			ImageView img = new ImageView(mActivity);
//			JSONObject obj = topAds.getJSONObject(0);
//			int screenWidth = mScreenSize.x;
//			int img_w = obj.optInt("ad_img_w");
//			int img_h = obj.optInt("ad_img_h");
//			int viewHeight = screenWidth * img_h / img_w;
//			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
//					viewHeight);
//			img.setLayoutParams(params);
//			setAdsImageForView(img, obj);
//			contrainer.addView(img);
//			groupView = contrainer;
//		} else if (TextUtils.equals(group_code, "group_12")) {
//			int column = 2, row = topAds.length() / column;
//			groupView = mLayoutInflater.inflate(
//					R.layout.fragment_main_ads_group, null);
//
//			// 拆分为多行显示
//			for (int i = 0; i < row; i++) {
//				LinearLayout container = new LinearLayout(mActivity);
//				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//				((ViewGroup) groupView
//						.findViewById(R.id.main_ads_group_container)).addView(
//						container, params);
//
//				// 每行中的一列
//				for (int j = 0; j < column; j++) {
//					try {
//						View adsView = makeAdsImageView(
//								topAds.getJSONObject(i * column + j),
//								viewWidth / 2, 0);
//						container.addView(adsView);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		} else {
//			if (isRightOne) {
//				JSONObject adJson = topAds.getJSONObject(2);
//				// 根据第一个广告的尺寸计算高度
//				int width = adJson.optInt("ad_img_w");
//				int height = adJson.optInt("ad_img_h");
//				groupView = mLayoutInflater.inflate(
//						R.layout.fragment_main_ads_group_4, null);
//				ViewGroup.LayoutParams params = groupView.findViewById(
//						R.id.fragment_main_ads_group_4).getLayoutParams();
//
//				params.height = (viewWidth / 2) * height / width;
//
//				groupView.findViewById(R.id.fragment_main_ads_group_4)
//						.setLayoutParams(params);
//
//				setAdsImageForView(
//						(ImageView) groupView
//								.findViewById(R.id.fragment_main_ads_group_4_right),
//						adJson);
//				setAdsImageForView(
//						(ImageView) groupView
//								.findViewById(R.id.fragment_main_ads_group_4_left1),
//						topAds.getJSONObject(0));
//				setAdsImageForView(
//						(ImageView) groupView
//								.findViewById(R.id.fragment_main_ads_group_4_left3),
//						topAds.getJSONObject(1));
//
//			} else {
//				groupView = mLayoutInflater.inflate(
//						R.layout.fragment_main_ads_group3, null);
//				// JSONObject adJson = topAds.getJSONObject(0);
//				// // 根据第一个广告的尺寸计算高度
//				// int width = adJson.optInt("ad_img_w");
//				// int height = adJson.optInt("ad_img_h");
//				JSONObject adJson = topAds.getJSONObject(0);
//				// 根据第一个广告的尺寸计算高度
//				int width = adJson.optInt("ad_img_w");
//				int height = adJson.optInt("ad_img_h");
//				ViewGroup.LayoutParams params = groupView.findViewById(
//						R.id.fragment_main_ads_group3).getLayoutParams();
//				params.height = (viewWidth / 2) * height / width;
//				groupView.findViewById(R.id.fragment_main_ads_group3)
//						.setLayoutParams(params);
//
//				setAdsImageForView(
//						(ImageView) groupView
//								.findViewById(R.id.fragment_main_ads_group3_left),
//						adJson);
//				setAdsImageForView(
//						(ImageView) groupView
//								.findViewById(R.id.fragment_main_ads_group3_right1),
//						topAds.getJSONObject(1));
//				setAdsImageForView(
//						(ImageView) groupView
//								.findViewById(R.id.fragment_main_ads_group3_right3),
//						topAds.getJSONObject(2));
//
//			}
//			isRightOne = !isRightOne;
//		}
//
//		if (groupView == null)
//			return null;
//
//		View titleTV = groupView.findViewById(android.R.id.title);
//		if (titleTV != null) {
//			((TextView) titleTV).setText(child.optString("group_name"));
//			((TextView) titleTV).setTypeface(Typeface.DEFAULT_BOLD);
//			((TextView) titleTV).setTextColor(Color.BLACK);
//		}
//
//		return groupView;
//	}

	private void fillSecondKillView(JSONObject data) {
		if (data == null) {
			mGallery = null;
			return;
		}
		JSONArray items = data.optJSONArray("items");
		if (items == null && items.length() < 0) {
			return;
		}
		final List<JSONObject> listItems = new ArrayList<JSONObject>();
		mGallery = (Gallery) findViewById(R.id.fragment_main_secondkill);
		mGallery.setUnselectedAlpha(0.6f);
		mGalleryIndicator = (LinearLayout) findViewById(R.id.fragment_main_ads_secondskill_indicator);
		mGalleryIndicator.removeAllViews();
		for (int i = 0; i < items.length(); i++) {
			ImageView imageView = new ImageView(mActivity);
			imageView.setImageResource(R.drawable.rodot_normal);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = Util.dip2px(mActivity, 10);
			imageView.setLayoutParams(params);
			mGalleryIndicator.addView(imageView);
			listItems.add(items.optJSONObject(i));
		}
		if (listItems.size() < 1) {
			return;
		}
		mGallery.setAdapter(new GalleyAdapter(listItems));
		mSecondKillContainer.setVisibility(View.VISIBLE);
		final int selected = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2)
				% listItems.size();
		mGallery.setSelection(selected);
		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long arg3) {
				position = position % listItems.size();
				for (int i = 0; i < mGalleryIndicator.getChildCount(); i++) {
					if (i == position) {
						((ImageView) mGalleryIndicator.getChildAt(i))
								.setImageResource(R.drawable.rodot_selected);
					} else {
						((ImageView) mGalleryIndicator.getChildAt(i))
								.setImageResource(R.drawable.rodot_normal);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				Object obj = view.getTag();
				if (obj == null) {
					return;
				}
				JSONObject goods = (JSONObject) obj;
				String productId = goods.optJSONObject("products").optString(
						"product_id");
				openGoodsDetailPageByProductID(productId);
			}
		});
	}

	private void openGoodsDetailPageByProductID(String goodsIID) {
		Intent intent = AgentActivity.intentForFragment(mActivity,
				AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
				Run.EXTRA_PRODUCT_ID, goodsIID);
		mActivity.startActivity(intent);
	}

	private View makeAdsGroupView2(JSONObject child) throws Exception{
		View groupView = null;
		LinearLayout parent = null;
		LinearLayout goodsLayout = null;
		View titleView = null;
		JSONArray adsArrayList = child.optJSONArray("items");
		String group_code = child.optString("group_code");
		int columnSize = child.optInt("column_size");
		long systemTime = child.optLong("system_time");
		long beginTime = child.optLong("begin_time");
		long endTime = child.optLong("end_time");
		boolean isPromo = true; //活动商品
		boolean isStart = false; //是否开始
		long countDown = 0;
		boolean isGroupBuy = false; //团购或是预售
		boolean isPreSale = false;
		String str = "";
		if((isGroupBuy = TextUtils.equals(group_code, "group_2")) || (isPreSale = TextUtils.equals(group_code, "group_3"))){
			if (systemTime >= endTime){
				if(isGroupBuy){
					mGroupBuyContainer.removeAllViews();
					mGroupBuyContainer.setVisibility(View.GONE);
				} else if(isPreSale) {
					mPresellContainer.removeAllViews();
					mPresellContainer.setVisibility(View.GONE);
				} else {
					mMeiToonContainer.removeAllViews();
					mMeiToonContainer.setVisibility(View.GONE);
				}
				return null;
			}
			if (systemTime < beginTime) {
				countDown = beginTime - systemTime;
			} else {
				countDown = endTime - systemTime;
				isStart = true;
			}
			View view = mLayoutInflater.inflate(
					R.layout.fragment_main_ads_group_buy, null);
			titleView = view
					.findViewById(R.id.fragment_main_ads_group_buy_title_bg1);
			titleView.setOnClickListener(this);
			goodsLayout = (LinearLayout) view
					.findViewById(R.id.fragment_main_ads_group_buy_goods_container);
			if (isPreSale || isGroupBuy){
				NewCountDownView countDownView = (NewCountDownView) view
						.findViewById(R.id.fragment_main_ads_group_buy_countdown);
				countDownView.setTimeNew(countDown);
				countDownView.start();
//				countDownView.setTimeEndListener(timeOutListener);
				countDownView.setVisibility(View.VISIBLE);
			}
			if(isGroupBuy){
				mGroupBuyContainer.removeAllViews();
				titleView.setTag(R.id.tag_object, 1);
				str = "人已团购";
				mGroupBuyContainer.addView(view);
				mGroupBuyContainer.setVisibility(View.VISIBLE);
			} else if(isPreSale) {
				mPresellContainer.removeAllViews();
				titleView.setTag(R.id.tag_object, 3);
				mPresellContainer.addView(view);
				mPresellContainer.setVisibility(View.VISIBLE);
				str = "人已预订";
			} else {
				mMeiToonContainer.removeAllViews();
				titleView.setTag(R.id.tag_object, 4);
				mMeiToonContainer.addView(view);
				mMeiToonContainer.setVisibility(View.VISIBLE);
			}
		} else {
			if (systemTime < beginTime || systemTime >= endTime) //未开始的不显示
				return null;
			isPromo = false;
			groupView = mLayoutInflater.inflate(R.layout.fragment_main_ads_container, null);
			parent = (LinearLayout) groupView.findViewById(R.id.fragment_main_ads_parents);
//			View v = mLayoutInflater.inflate(R.layout.ads_view_title, null);//添加
//			NewCountDownView newCountDownView = (NewCountDownView) v.findViewById(R.id.ads_view_title_countdown_view);
//			newCountDownView.setTimeNew(countDown);
//			newCountDownView.start();
//			parent.addView(v);
		}
		int adsCount = adsArrayList == null ? 0 : adsArrayList.length();
		int imageHeight = 0;
		int screenWidth = 0;
		if (columnSize == 3) {
			int rows = adsCount / 3;
			screenWidth = mScreenSize.x / 3;
			JSONObject rowAds1 = null;
			JSONObject rowAds2 = null;
			JSONObject rowAds3 = null;
			for (int i = 0; i < rows; i++) {
				View childView = mLayoutInflater.inflate(
						R.layout.fragment_main_ads_normal3, null);
				rowAds1 = adsArrayList.optJSONObject(i * 3);
				rowAds2 = adsArrayList.optJSONObject(i * 3 + 1);
				rowAds3 = adsArrayList.optJSONObject(i * 3 + 2);
				imageHeight = screenWidth * rowAds1.optInt("ad_img_h")
						/ rowAds1.optInt("ad_img_w");
				LayoutParams params = new LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
				params.height = imageHeight;
				childView.setLayoutParams(params);
				if (isPromo) {
//					if (i == 0) {
//						setAdsImageOnlyForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal3_l),
//								rowAds1 , screenWidth , imageHeight);
//						setAdsImageOnlyForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal3_m),
//								rowAds2, screenWidth , imageHeight);
//						setAdsImageOnlyForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal3_r),
//								rowAds3, screenWidth , imageHeight);
//						titleView.addView(childView);
//					} else {
//						setAdsImageForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal3_l),
//								rowAds1, true, screenWidth , imageHeight);
//						setAdsImageForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal3_m),
//								rowAds2, true, screenWidth , imageHeight);
//						setAdsImageForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal3_r),
//								rowAds3, true, screenWidth , imageHeight);
//						if (isPreSale || isGroupBuy) {
//							((TextView) childView
//									.findViewById(R.id.fragment_main_ads_normal3_l_count))
//									.setText(rowAds1.optString("buy_count") + str);
//							((TextView) childView
//									.findViewById(R.id.fragment_main_ads_normal3_m_count))
//									.setText(rowAds2.optString("buy_count") + str);
//							((TextView) childView
//									.findViewById(R.id.fragment_main_ads_normal3_r_count))
//									.setText(rowAds3.optString("buy_count") + str);
//						}
//						goodsLayout.addView(childView);
//					}
					
					//使用新的倒计时格式
					setAdsImageForView(
							(ImageView) childView
									.findViewById(R.id.fragment_main_ads_normal3_l),
							rowAds1, true, screenWidth , imageHeight);
					setAdsImageForView(
							(ImageView) childView
									.findViewById(R.id.fragment_main_ads_normal3_m),
							rowAds2, true, screenWidth , imageHeight);
					setAdsImageForView(
							(ImageView) childView
									.findViewById(R.id.fragment_main_ads_normal3_r),
							rowAds3, true, screenWidth , imageHeight);
					if (isPreSale || isGroupBuy) {
						if (rowAds1.optInt("buy_count") > 0) {
							((TextView) childView
									.findViewById(R.id.fragment_main_ads_normal3_l_count))
									.setText(rowAds1.optString("buy_count") + str);
						}
						if (rowAds2.optInt("buy_count") > 0) {
							((TextView) childView
									.findViewById(R.id.fragment_main_ads_normal3_m_count))
									.setText(rowAds2.optString("buy_count") + str);
						}
						if (rowAds3.optInt("buy_count") > 0) {
							((TextView) childView
									.findViewById(R.id.fragment_main_ads_normal3_r_count))
									.setText(rowAds3.optString("buy_count") + str);
						}
					}
					goodsLayout.addView(childView);
				
				} else {
					setAdsImageForView(
							(ImageView) childView
							.findViewById(R.id.fragment_main_ads_normal3_l),
							rowAds1, screenWidth , imageHeight);
					setAdsImageForView(
							(ImageView) childView
							.findViewById(R.id.fragment_main_ads_normal3_m),
							rowAds2, screenWidth , imageHeight);
					setAdsImageForView(
							(ImageView) childView
							.findViewById(R.id.fragment_main_ads_normal3_r),
							rowAds3, screenWidth , imageHeight);
					parent.addView(childView);
				}
			}
		} else if (columnSize == 2) {
			int rows = adsCount / 2;
			screenWidth = mScreenSize.x / 2;
			JSONObject rowAds1 = null;
			JSONObject rowAds2 = null;
			for (int i = 0; i < rows; i++) {
				View childView = mLayoutInflater.inflate(
						R.layout.fragment_main_ads_normal2, null);
				rowAds1 = adsArrayList.optJSONObject(i * 2);
				rowAds2 = adsArrayList.optJSONObject(i * 2 + 1);
				LayoutParams params = new LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
				imageHeight = screenWidth * rowAds1.optInt("ad_img_h")
						/ rowAds1.optInt("ad_img_w");
				params.height = imageHeight;
				childView.setLayoutParams(params);
				if (isPromo){
//					if (i == 0) {
//						setAdsImageOnlyForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal2_l),
//								rowAds1, screenWidth , imageHeight);
//						setAdsImageOnlyForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal2_r),
//								rowAds2, screenWidth , imageHeight);
//						titleView.addView(childView);
//					} else {
//						setAdsImageForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal2_l),
//								rowAds1, true, screenWidth , imageHeight);
//						setAdsImageForView(
//								(ImageView) childView
//										.findViewById(R.id.fragment_main_ads_normal2_r),
//								rowAds2, true, screenWidth , imageHeight);
//						if (isPreSale || isGroupBuy){
//							((TextView) childView
//									.findViewById(R.id.fragment_main_ads_normal2_l_count))
//									.setText(rowAds1.optString("buy_count") + str);
//							((TextView) childView
//									.findViewById(R.id.fragment_main_ads_normal2_r_count))
//									.setText(rowAds2.optString("buy_count") + str);
//						}
//						goodsLayout.addView(childView);
//					}
					
					setAdsImageForView(
							(ImageView) childView
									.findViewById(R.id.fragment_main_ads_normal2_l),
							rowAds1, true, screenWidth , imageHeight);
					setAdsImageForView(
							(ImageView) childView
									.findViewById(R.id.fragment_main_ads_normal2_r),
							rowAds2, true, screenWidth , imageHeight);
					if (isPreSale || isGroupBuy) {
						if (rowAds1.optInt("buy_count") > 0) {
							((TextView) childView
									.findViewById(R.id.fragment_main_ads_normal2_l_count))
									.setText(rowAds1.optString("buy_count") + str);
						}
						if (rowAds2.optInt("buy_count") > 0) {
							((TextView) childView
									.findViewById(R.id.fragment_main_ads_normal2_r_count))
									.setText(rowAds2.optString("buy_count") + str);
						}
					}
					goodsLayout.addView(childView);
					
				} else {
					setAdsImageForView(
							(ImageView) childView
							.findViewById(R.id.fragment_main_ads_normal2_l),
							rowAds1, screenWidth , imageHeight);
					setAdsImageForView(
							(ImageView) childView
							.findViewById(R.id.fragment_main_ads_normal2_r),
							rowAds2, screenWidth , imageHeight);
					parent.addView(childView);
				}
			}
		} else {
			for (int i = 0; i < adsCount; i++) {
				ImageView adsView = new ImageView(mActivity);
				JSONObject obj = adsArrayList.optJSONObject(i);
				screenWidth = mScreenSize.x;
				int img_w = obj.optInt("ad_img_w");
				int img_h = obj.optInt("ad_img_h");
				imageHeight = screenWidth * img_h / img_w;
				LayoutParams params = new LayoutParams(
						LayoutParams.MATCH_PARENT, imageHeight);
				adsView.setLayoutParams(params);
				if (isPromo) {
//					if (1 == i) {
//						setAdsImageOnlyForView(adsView, adsArrayList.optJSONObject(i), screenWidth , imageHeight);
//						titleView.addView(adsView);
//					} else {
						setAdsImageForView(adsView, adsArrayList.optJSONObject(i), screenWidth , imageHeight);
						goodsLayout.addView(adsView);
//					}
				} else {
					setAdsImageForView(adsView, adsArrayList.optJSONObject(i), screenWidth , imageHeight);
					parent.addView(adsView);
				}
			}
		}
		return groupView ;
	}

	/**
	 * 为view设置广告图片
	 * 
	 * @param adsView
	 * @param adJson
	 */
	private void setAdsImageForView(ImageView adsView, JSONObject adJson,int width , int height) {
		Uri imageUri = Uri.parse(adJson.optString("ad_img"));
		adsView.setTag(imageUri);
		adsView.setTag(R.id.tag_object, adJson);
		adsView.setOnClickListener(mAdViewClickListener);
		// mImageLoader.showImage(adsView, imageUri);
//		mVolleyImageLoader.showImage(adsView, adJson.optString("ad_img"), (int)(width * 0.7) , (int)(height * 0.7) );
//		mVolleyImageLoader.showImage(adsView, adJson.optString("ad_img"), width , height );
		mVolleyImageLoader.showImage(adsView, adJson.optString("ad_img"));
		imageViews.add(adsView);
	}
	private ArrayList<ImageView> imageViews = new ArrayList<ImageView>();

	/**
	 * 只显示图片，不设置点击事件
	 * 
	 * @param adsView
	 * @param adJson
	 */
	private void setAdsImageOnlyForView(ImageView adsView, JSONObject adJson , int width , int height) {
		Uri imageUri = Uri.parse(adJson.optString("ad_img"));
		adsView.setTag(imageUri);
		adsView.setTag(R.id.tag_object, adJson);
		// mImageLoader.showImage(adsView, imageUri);
//		mVolleyImageLoader.showImage(adsView, adJson.optString("ad_img"), (int)(width * 0.7) , (int)(height * 0.7) );
		mVolleyImageLoader.showImage(adsView, adJson.optString("ad_img"));
		imageViews.add(adsView);
	}

	/**
	 * 
	 * @param adsView
	 * @param adJson
	 * @param isGroupBuy
	 *            判断这个广告位是不是团购或是预售
	 */
	private void setAdsImageForView(ImageView adsView, JSONObject adJson,
			boolean isGroupBuy ,int width ,int height) {
		adsView.setTag(R.id.tag_first, isGroupBuy);
		setAdsImageForView(adsView, adJson, width , height);
	}

	private View makeAdsImageView(JSONObject topAdsObject, int viewWidth,
			int margin) {
		ImageView adsView = (ImageView) mLayoutInflater.inflate(
				R.layout.fragment_main_ads_item, null);

		// 根据屏幕和图片大小调整显示尺寸
		int width = topAdsObject.optInt("ad_img_w");
		int height = topAdsObject.optInt("ad_img_h");
		int viewHeight = viewWidth * height / width;
		LinearLayout.LayoutParams params;
		params = new LinearLayout.LayoutParams(viewWidth, viewHeight);
		params.leftMargin = margin;
		adsView.setLayoutParams(params);

		Uri imageUri = Uri.parse(topAdsObject.optString("ad_img"));
		adsView.setTag(imageUri);
		adsView.setTag(R.id.tag_object, topAdsObject);
		adsView.setOnClickListener(mAdViewClickListener);
		// mImageLoader.showImage(adsView, imageUri);
		mVolleyImageLoader.showImage(adsView, topAdsObject.optString("ad_img"));
		return adsView;
	}

	// 读取缓存的json
	private void loadLocalAdJson() {
		try {
			File file = new File(mActivity.getFilesDir(),
					Run.FILE_HOME_ADS_JSON);
			String jsonStr = FileUtils.readFileToString(file);
			parseHomeAdsJson(jsonStr, false);
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_main_button_scan) {
//			IntentIntegrator integrator = new IntentIntegrator(mActivity);
//			integrator.initiateScan();
			startActivity(new Intent(mActivity, CaptureActivity.class));
		} else if (v.getId() == R.id.fragment_main_search) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_GOODS_SEARCH));
		} else if (v.getId() == R.id.fragment_main_ads_group_buy_title
				|| v.getId() == R.id.fragment_main_ads_group_buy_title_bg1) {
			int typeId = (Integer) v.getTag(R.id.tag_object);
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_GROUP_BUY).putExtra(Run.EXTRA_DATA,
					String.valueOf(typeId)));
		} else if (v.getId() == R.id.fragment_main_feed_back) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_FEEDBACK));
		} else if (v.getId() == R.id.fragment_main_goto_top) {
			mScrollView.getRefreshableView().scrollTo(0, 0);
			handlerScroll.sendMessageDelayed(handlerScroll.obtainMessage(), 5);
		} else {
			super.onClick(v);
		}
	}

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
			return new JsonRequestBean(
					"mobileapi.indexad.get_all_list");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			mScrollView.onRefreshComplete();
			parseHomeAdsJson(json_str, true);
		}
	}

	private class LoadSecondsKillTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					long systemTime = data.optLong("system_time");
					long beginTime = 0;
					long endTime = 0;
					JSONArray list = data.optJSONArray("list");
					String specialId = "";
					if (list != null && list.length() > 0) {
						JSONObject object = list.getJSONObject(0);
						beginTime = object.optLong("begin_time");
						endTime = object.optLong("end_time");
						specialId = object.optString("special_id");
					}
					if (systemTime >= beginTime && systemTime < endTime) {
						isStarted = true;
						remainTime = endTime - systemTime;
					} else if (systemTime < beginTime) {
						remainTime = beginTime - systemTime;
					} else {
						remainTime = 0;
					}
					if (remainTime <= 0) {
						mSecondKillContainer.setVisibility(View.GONE);
						return;
					}
					if (!TextUtils.isEmpty(specialId)) {
						new JsonTask().execute(new LoadSecondKillDetail(
								specialId));
					}
					countDownHandler.sendEmptyMessage(0);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getGroup");
			req.addParams("type_id", "2"); // type_id = 2 (秒杀)
			return req;
		}

	}

	private class LoadSecondKillDetail implements JsonTaskHandler {

		private String specialID;

		public LoadSecondKillDetail(String specialId) {
			specialID = specialId;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					fillSecondKillView(all.optJSONObject("data"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getList");
			req.addParams("special_id", specialID);
			return req;
		}

	}

	private class Add2ShoppingCar implements JsonTaskHandler {

		private String productId;

		public Add2ShoppingCar(String productId) {
			this.productId = productId;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.excuteJsonTask(new JsonTask(), new SubmitCarTask());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (Exception e) {

			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.cart.add").addParams("product_id", productId)
					.addParams("num", String.valueOf(1))
					.addParams("btype", "is_fastbuy");
			return bean;
		}

	}

	private class SubmitCarTask implements JsonTaskHandler {
		private String isFastBuy = "true";

		public SubmitCarTask() {
		}

		public SubmitCarTask(String isFastBuy) {
			this.isFastBuy = isFastBuy;
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.cart.checkout")
					.addParams("isfastbuy", isFastBuy);
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all) && !all.isNull("data")) {
					JSONObject data = all.optJSONObject("data");
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_SUBMIT_SHOPPING_CAR)
							.putExtra(Run.EXTRA_DATA, data.toString())
							.putExtra(Run.EXTRA_VALUE, isFastBuy)
							.putExtra(Run.EXTRA_FROM_EXTRACT, mFromExtract));
				}
			} catch (Exception e) {
			}
		}
	}

	private class GetExtractTime implements JsonTaskHandler {

		private String productID;

		public GetExtractTime(String productId) {
			productID = productId;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						if (!data.isNull("from_extract")) {
							JSONArray list = data.optJSONArray("from_extract");
							if (list != null) {
								mFromExtract = list.toString();
							}
						}
						new JsonTask().execute(new Add2ShoppingCar(productID));
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
					"starbuy.index.getDetail");
			req.addParams("product_id", productID);
			req.addParams("son_object", "json");
			return req;
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
					JSONObject topJson = child.getJSONObject(0);
					JSONArray array = topJson.optJSONArray("items");
					int count = (array != null) ? array.length() : 0;
					for (int i = 0; i < count; i++)
						mTopAdsArray.add(array.getJSONObject(i));

					mAdsGroupJsonArray = child;
//					long systemTime = topJson.optLong("system_time");
//					long beginTime = topJson.optLong("begin_time");
//					long endTime = topJson.optLong("end_time");
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
			if (needSave) // 无需缓存则不重复加载
				loadLocalAdJson();
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

	private OnClickListener mCatesClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.main_ads_cates_fav_goods) {
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_FAVORITE_GOODS));
			} else if (v.getId() == R.id.main_ads_cates_orders) {
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_ACCOUNT_ORDERS));
			} else if (v.getId() == R.id.main_ads_cates_payments) {
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_FEEDBACK));
			} else if(v.getId() == R.id.main_ads_cates_shops){
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_HELP_CENTRE));
			}
		};
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
			// mImageLoader.showImage((ImageView) convertView, imageUri);
			mVolleyImageLoader.showImage((ImageView) convertView,
					topAdsObject.optString("ad_img"));

			return convertView;
		}
	}

	private class GalleyAdapter extends BaseAdapter implements OnClickListener {

		List<JSONObject> list;

		public GalleyAdapter(List<JSONObject> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View container, ViewGroup parent) {
			if (container == null) {
				container = mLayoutInflater.inflate(
						R.layout.item_limit_timebuy, null);
				View view = container
						.findViewById(R.id.item_limit_timebuy_image);
				LayoutParams params = view.getLayoutParams();
				params.width = mScreenSize.x / 2;
				params.height = mScreenSize.x / 2;
				view.setLayoutParams(params);
			}
			position = position % list.size();
			JSONObject obj = list.get(position);
			container.setTag(obj);
			ImageView img = (ImageView) container
					.findViewById(R.id.item_limit_timebuy_image);
			JSONObject imgObj = obj.optJSONObject("image_default_url");
			Uri imageUri = Uri.parse(imgObj.optString("m"));
			img.setTag(imageUri);
			// mImageLoader.showImage(img, imageUri);
			mVolleyImageLoader.showImage(img, imgObj.optString("m"));
			((TextView) container.findViewById(R.id.item_limit_timebuy_title))
					.setText(obj.optString("name"));
			JSONObject goods = obj.optJSONObject("products");
			TextView tv1 = (TextView) container
					.findViewById(R.id.item_limit_timebuy_yuanjia);
			tv1.setText(Run.buildString("￥", goods.optString("mktprice")));
			tv1.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			TextView tv2 = (TextView) container
					.findViewById(R.id.item_limit_timebuy_nowprice);
			tv2.setText(Run.buildString("￥", goods.optString("price")));
			if (isStarted) {
				View view = container.findViewById(R.id.item_limit_timebuy_buy);
				view.setTag(obj);
				view.setOnClickListener(this);
			} else {
				container.findViewById(R.id.item_limit_timebuy_buy).setVisibility(View.INVISIBLE);
			}
			return container;
		}

		@Override
		public void onClick(View view) {
			Object obj = view.getTag();
			if (obj == null) {
				return;
			}
			JSONObject goods = (JSONObject) obj;
			new JsonTask().execute(new GetExtractTime(goods.optJSONObject(
					"products").optString("product_id")));
		}

	}

	private TimeEndListener timeOutListener = new TimeEndListener() {

		@Override
		public void isTimeEnd() {
			new JsonTask().execute(new LoadHomeDetailsTask(true));
		}

	};

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

	private Handler countDownHandler = new Handler() {
		public void handleMessage(Message msg) {
			remainTime -= 1;
			if (remainTime < 0) {
				new JsonTask().execute(new LoadHomeDetailsTask(true));
				return;
			} else {
				if (mSecondsKillRemainTime == null) {
					mSecondsKillRemainTime = (TextView) findViewById(R.id.fragment_main_ads_secondskill_remaintime);
				}
//				if (isStarted) { // 已开始 距离结束时间
//					mSecondsKillRemainTime.setText("距结束:"
//							+ Util.calculateRemainTime(remainTime));
//				} else { // 未开始 距离开始时间
//					mSecondsKillRemainTime.setText("距开始:"
//							+ Util.calculateRemainTime(remainTime));
//				}
				long seconds = remainTime % 60;
				long minutes = remainTime / 60 % 60;
				long hours = remainTime / (60 * 60) % 24;
				long day = remainTime / (60 * 60 * 24);
//				long hour_decade = hours / 10;
//				long hour_unit = hours % 10;

				long min_decade = minutes / 10;
				long min_unit = minutes % 10;

				long sec_decade = seconds / 10;
				long sec_unit = seconds % 10;
				if (day > 0) {
					if (isStarted)
						mSecondsKillRemainTime.setText(day + "天" + hours +"时" +"后结束");
					else
						mSecondsKillRemainTime.setText(day + "天" + hours +"时" +"后开始");
				} else if (hours > 0){
					if (isStarted)
						mSecondsKillRemainTime.setText(hours+":"+minutes+"分"+"后结束");
					else
						mSecondsKillRemainTime.setText(hours+":"+minutes+"分"+"后开始");
				} else {
					if (isStarted)
						mSecondsKillRemainTime.setText(""+min_decade+min_unit+":"+sec_decade+sec_unit+"后结束");
					else
						mSecondsKillRemainTime.setText(""+min_decade+min_unit+":"+sec_decade+sec_unit+"后开始");
				}
			}
			removeMessages(0);
			sendEmptyMessageDelayed(0, 1000);
		};
	};

	int lastScrollY;
	private Handler handlerScroll = new Handler() {
		public void handleMessage(Message msg) {
			int scrollY = mScrollView.getRefreshableView().getScrollY();
			if (lastScrollY != scrollY) {
				lastScrollY = scrollY;
				if (lastScrollY > 500) {
					rootView.findViewById(R.id.fragment_main_goto_top)
							.setVisibility(View.VISIBLE);
				} else {
					rootView.findViewById(R.id.fragment_main_goto_top)
							.setVisibility(View.GONE);
				}
				handlerScroll.sendMessageDelayed(handlerScroll.obtainMessage(),
						5);
			}
		};
	};
}
