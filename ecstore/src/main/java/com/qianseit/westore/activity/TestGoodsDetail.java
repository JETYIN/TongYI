package com.qianseit.westore.activity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleAnimListener;
import com.qianseit.westore.adapter.GoodsDetailRecommendAdapter;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleFlowIndicator;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.FlowScrollView;
import com.qianseit.westore.ui.FlowView;
import com.qianseit.westore.ui.MyGridView;
import com.qianseit.westore.ui.NoScrollListView;
import com.qianseit.westore.ui.NotifyChangedScrollView;
import com.qianseit.westore.ui.NotifyChangedScrollView.onScrollChangedListener;
import com.qianseit.westore.ui.RushBuyCountDownTimerView;
import com.qianseit.westore.ui.ShareView;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.ui.SharedPopupWindow;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;
public class TestGoodsDetail extends BaseDoFragment implements ShareViewDataSource, OnCheckedChangeListener ,onScrollChangedListener {

	public final int DETAIL_NORMAL = 0;
	public final int DETAIL_GROUP_BUY = 1;
	public final int DETAIL_SEC_KILL = 2;

//	private long mRemainTime;
//	private boolean isStarted;
	private boolean isFavorite;

	private int mDetailType = DETAIL_NORMAL;
	private String mScanRezult;

	private VolleyImageLoader mVolleyImageLoader;
	private LayoutInflater mInflater;
	private Resources mResources;
	private String mGoodsIID;
	private String mProductIID; // 团购，秒杀，预售传product_id 不传goods_id
	private String mActID;
	private int mQuantity = 1;
	private int mEventButton;
	private JSONObject mSelectedSku;

//	private NotifyChangedScrollView mUpViewScrollView;
	private TextView mQuantityTV;
	private FlowView mBigImagesFlowView;
	private TextView mGoodsCarCountTV;
//	private TextView mRemainTimeTV;
	private LinearLayout mCountLayout;
	private TextView tvBrief;

	private LayoutInflater mLayoutInflater;

	// BottomView
	private WebView mWebView;
	private ListView mBottomListView;
	private ViewGroup mPointArea;
//	private View koubeiRecommend;
	// private GoodsCommentsAdapter mCommentsAdapter;
	private GoodsDetailRecommendAdapter recommendAapter;
	private boolean isLoadedData = false;
	private int mLimit = -1;
	private boolean isShowCommentData; // 当前显示的是评论
//	private boolean isLoadedOnce; // 避免每次切换时候加载下一页
	private MyGridView gridViewTip;

	private LinearLayout ll_add_count_view;
	private LinearLayout ll_parent_container;
	// 分享
	private ShareView mShareView;

	private ViewGroup mRecomentView1;

	private JSONObject mPromotion;
	private JSONObject mGoodsDetailJsonObject, mGroupBuyGoodsDetail;
	private JSONObject mGoodsDetailInfo;
	private JSONArray mFromExtract;
	// 选中的商品属性
	private HashMap<String, JSONObject> mSelectSpecs = new HashMap<String, JSONObject>();
	// 货品列表
	private HashMap<String, JSONObject> skus = new HashMap<String, JSONObject>();
	private HashMap<String, JSONObject> id_key_skus = new HashMap<String, JSONObject>();
	private ArrayList<JSONObject> skus_list = new ArrayList<JSONObject>();
	// 商品属性列表
	private ArrayList<JSONObject> spec_infos = new ArrayList<JSONObject>();
	// 商品属性说明
	private ArrayList<JSONObject> props_values = new ArrayList<JSONObject>();
	// 商品图片列表
	private ArrayList<JSONObject> item_imgs = new ArrayList<JSONObject>();

	private View mRadioGroup;
	private View mTabBar;

	private NotifyChangedScrollView myScrollView;
	private View mGuessView;
	private boolean isFirstIn = true;
	private View fragment_goods_detail_buy;

	public TestGoodsDetail() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResources = mActivity.getResources();
		mInflater = mActivity.getLayoutInflater();
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();

		Intent data = mActivity.getIntent();
		mDetailType = data.getIntExtra(Run.EXTRA_DETAIL_TYPE, DETAIL_NORMAL);
		Log.e("mDetailType","mDetailType:"+mDetailType);
		mGoodsIID = data.getStringExtra(Run.EXTRA_CLASS_ID);
		// mGoodsIID = "1";
		mProductIID = data.getStringExtra(Run.EXTRA_PRODUCT_ID);
		mActID = data.getStringExtra(Run.EXTRA_VALUE);
		mScanRezult = data.getStringExtra(Run.EXTRA_SCAN_REZULT);

		mActionBar.setTitle(R.string.goods_detail);
		mActionBar.setRightImageButton(R.drawable.icon_share,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (v.getId() == R.id.action_bar_titlebar_right_ib) {
							if (TextUtils.isEmpty(getShareText())
									|| TextUtils.isEmpty(getShareImageUrl())
									|| !new File(getShareImageFile()).exists())
								return;

							// 显示分享view
//							mShareView.showShareView();
							SharedPopupWindow morePopWindow = new SharedPopupWindow(mActivity);
							morePopWindow.setDataSource(TestGoodsDetail.this);
							morePopWindow.showPopupWindow(mActionBar.getRightImageButton());
						}
					}
				});

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		mLayoutInflater = mActivity.getLayoutInflater();
		rootView = inflater.inflate(R.layout.test_detail_goods, null);
		rootView.setVisibility(View.INVISIBLE);
		fragment_goods_detail_buy=inflater.inflate(R.layout.fragment_goods_detail_buy, null);
		myScrollView = (NotifyChangedScrollView) rootView
				.findViewById(R.id.goods_detail_scollview);
		mRadioGroup = findViewById(R.id.test_goods);
		mTabBar = findViewById(R.id.test_goods_bar);
//		myScrollView.setOnScrollListener(this);
		myScrollView.setOnScrollChangedListener(this);
		ll_add_count_view = (LinearLayout) findViewById(R.id.ll_add_count_view);
		ll_parent_container = (LinearLayout) findViewById(R.id.ll_parent_container);
		gridViewTip = (MyGridView) findViewById(R.id.gridview_tip);
		gridViewTip.setNumColumns(2);

		// mPagerUpView
		tvBrief = (TextView) findViewById(R.id.tv_brief);
		// 大图循环播放
		mBigImagesFlowView = (FlowView) findViewById(R.id.goods_detail_images);
		mBigImagesFlowView
				.setParentScrollView((FlowScrollView) myScrollView);
		mBigImagesFlowView.setMaskParentOntouch(false);

		int shoppingCarId = R.id.goods_detail_topbar_shoppingcar;
		findViewById(R.id.goods_detail_addto_shopcar).setOnClickListener(this);
		findViewById(R.id.goods_detail_justbuy).setOnClickListener(this);
		findViewById(R.id.rel_brand_view).setOnClickListener(this);
		rootView.findViewById(shoppingCarId).setOnClickListener(this);

		mCountLayout = (LinearLayout) rootView
				.findViewById(R.id.fragment_goods_detail_select_layout);
		rootView.findViewById(R.id.fragment_goods_detail_comfirm)
				.setOnClickListener(this);
		rootView.findViewById(R.id.fragment_goods_detail_cancel)
				.setOnClickListener(this);
//		koubeiRecommend = mLayoutInflater.inflate(
//				R.layout.item_koubei_recomment_head, null);

		fragment_goods_detail_buy.findViewById(R.id.goods_detail_buy_qminus).setOnClickListener(this);
		fragment_goods_detail_buy.findViewById(R.id.goods_detail_buy_qplus).setOnClickListener(
				this);
		findViewById(R.id.goods_detail_search).setOnClickListener(this);
		findViewById(R.id.goods_detail_button_category)
				.setOnClickListener(this);
		findViewById(R.id.goods_detail_buy_confirm).setOnClickListener(this);
		findViewById(R.id.goods_detail_buy_cancel).setOnClickListener(this);
		findViewById(R.id.goods_detail_like).setOnClickListener(this);
		mQuantityTV = (TextView) findViewById(R.id.goods_detail_buy_quantity);
		mQuantityTV.setOnClickListener(this);
		mGoodsCarCountTV = (TextView) findViewById(R.id.goods_detail_topbar_shoppingcar_count);
		findViewById(R.id.goods_detail_radio_onsale).setOnClickListener(this);
		findViewById(R.id.goods_detail_order_onsale).setOnClickListener(this);
		mGuessView = findViewById(R.id.goods_detai_guess_yourfav);

		mWebView = new WebView(mActivity);
//		mWebView.getSettings().setUseWideViewPort(true);
//		mWebView.getSettings().setLoadWithOverviewMode(true);
		mBottomListView = new NoScrollListView(mActivity, null,
				R.style.listview);
//		mBottomListView = new GoodsDetailListView(mActivity, null,
//				R.style.listview);
		int padding = Util.dip2px(mActivity, 5);
		mBottomListView.setPadding(padding, padding, padding, padding);
		mBottomListView.setDividerHeight(padding);
		mBottomListView
				.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view,
							int scrollState) {

					}

					@Override
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						// if (totalItemCount == (firstVisibleItem +
						// visibleItemCount)) {
						// // myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
						// myScrollView.scrollTo(0, mRadioGroup.getTop());
						// }
						if (totalItemCount < 3 || !isShowCommentData) {
							return;
						}
						// 滚动到倒数第三个时，自动加载下一页
						// if (totalItemCount
						// - (firstVisibleItem + visibleItemCount) <= 3)
						// mCommentsAdapter.loadNextPage(mGoodsIID);
					}
				});

		mRecomentView1 = (ViewGroup) findViewById(R.id.good_detail_recommend_comtain);

		mPointArea = (ViewGroup) findViewById(R.id.goods_detail_point_area);
		mPointArea.setLayoutParams(new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		((RadioButton) mTabBar.findViewById(R.id.goods_detail_radio_images))
				.setOnCheckedChangeListener(this);
		((RadioButton) mTabBar.findViewById(R.id.goods_detail_radio_props))
				.setOnCheckedChangeListener(this);
		((RadioButton) mTabBar.findViewById(R.id.goods_detail_radio_comments))
				.setOnCheckedChangeListener(this);
		Run.removeFromSuperView(mPointArea);
		// mCommentsAdapter = new GoodsCommentsAdapter(mActivity, mPointArea);
		recommendAapter = new GoodsDetailRecommendAdapter(this,
				mVolleyImageLoader);
		recommendAapter.loadData(mGoodsIID);

		// 团购商品不能添加到购物车
		if (mDetailType == DETAIL_GROUP_BUY)
			rootView.findViewById(R.id.goods_detail_addto_shopcar)
					.setVisibility(View.INVISIBLE);
		// 以producti_id过来的都是团购、秒杀或是预售 只能直接购买
		if (!TextUtils.isEmpty(mProductIID)) {
			rootView.findViewById(R.id.goods_detail_addto_shopcar)
					.setVisibility(View.INVISIBLE);
		}

		mShareView = (ShareView) findViewById(R.id.share_view);
		mShareView.setDataSource(this);
//		mRemainTimeTV = (TextView) rootView
//				.findViewById(R.id.goods_detail_remain_time);

		rootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
//						onScroll(myScrollView.getScrollY());
						onScrollChanged(0, myScrollView.getScrollY(), 0, 0);
					}
				});

		if (!TextUtils.isEmpty(mScanRezult)) {
			Run.excuteJsonTask(new JsonTask(), new GetGoodsID());
			return;
		}
		// 加载商品详情
		if (TextUtils.isEmpty(mGoodsIID)) {// 从活动商品过来就没有mGoodsIID
			Run.excuteJsonTask(new JsonTask(), new GetGroupBuyDetail());
		} else {
			if (TextUtils.isEmpty(mProductIID)) {// 首页过来的只有GoodsIID其他地方过来的都带GoodsIID和mProductIID
				Run.excuteJsonTask(new JsonTask(), new GoodsDetailTask(
						mDetailType));
				findViewById(R.id.goods_detail_shoppingcar).setVisibility(
						View.VISIBLE);
				findViewById(R.id.goods_detail_addto_shopcar).setVisibility(
						View.VISIBLE);
			} else {
				Run.excuteJsonTask(new JsonTask(), new GetGroupBuyDetail());
			}
		}

	}
	
	@Override
	public void onResume() {
		super.onResume();
		// 已登录用户显示购物车数量
		if (AgentApplication.getLoginedUser(mActivity).isLogined()) {
			// Run.excuteJsonTask(new JsonTask(), new GetCarCountTask());
			mGoodsCarCountTV.setVisibility(Run.goodsCounts != 0 ? View.VISIBLE
					: View.INVISIBLE);
			mGoodsCarCountTV.setText(String.valueOf(Run.goodsCounts));
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.rel_brand_view) {
			if (v.getTag() == null) {
				return;
			}
			JSONObject obj = (JSONObject) v.getTag();
			mActivity.startActivity(AgentActivity
					.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_GOODS_DETAIL_BRAND)
					.putExtra(Run.EXTRA_TITLE, obj.optString("brand_name"))
					.putExtra(Run.EXTRA_GOODS_DETAIL_BRAND,
							obj.optString("brand_id")));
		} else if (v.getId() == R.id.goods_detail_like) {
			if (TextUtils.isEmpty(mGoodsIID)) {
				if (mGoodsDetailJsonObject == null)
					return;
				mGoodsIID = mGoodsDetailJsonObject.optString("iid");
			}
			if (isFavorite) {
				Run.excuteJsonTask(new JsonTask(), new RemoveFavoriteTask(
						mGoodsIID));
			} else {
				Run.excuteJsonTask(new JsonTask(), new AddFavoriteTask(
						mGoodsIID));
			}
		} else if (v.getId() == R.id.goods_detail_radio_images
				|| v.getId() == R.id.goods_detail_radio_props
				|| v.getId() == R.id.goods_detail_radio_comments) {
			if (mGoodsDetailJsonObject == null)
				return;

			startActivity(AgentActivity
					.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_GOODS_DETAIL_MORE)
					.putExtra(Run.EXTRA_VALUE, v.getId())
					.putExtra(Run.EXTRA_CLASS_ID, mGoodsIID)
					.putExtra(Run.EXTRA_DATA, mGoodsDetailJsonObject.toString()));
		} else if (v.getId() == R.id.goods_detail_topbar_shoppingcar) {
			mActivity.startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_SHOPPING_CAR));
		} else if (v.getId() == R.id.goods_detail_buy_cancel) {
			// 隐藏属性选择界面
			final View parent = findViewById(R.id.goods_detail_buy_parent);
			Animation animOut = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_down_out);
			animOut.setAnimationListener(new SimpleAnimListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					findViewById(R.id.translucent).setVisibility(View.GONE);
					parent.setVisibility(View.GONE);
				}
			});
			parent.startAnimation(animOut);
		} else if (v.getId() == R.id.goods_detail_addto_shopcar
				|| v.getId() == R.id.goods_detail_justbuy) {
			mEventButton = v.getId();
			// 直接购买第一个商品
			if (mSelectedSku == null && !skus_list.isEmpty())
				mSelectedSku = skus_list.get(0);
			// onClick(findViewById(R.id.goods_detail_buy_confirm));

			// 打开属性选择界面
			View parent = findViewById(R.id.goods_detail_buy_parent);
			parent.findViewById(R.id.goods_detail_buy_qminus).setOnClickListener(this);
			parent.findViewById(R.id.goods_detail_buy_qplus).setOnClickListener(this);
			mQuantityTV = (TextView)parent.findViewById(R.id.goods_detail_buy_quantity);
			mQuantityTV.setOnClickListener(this);
			parent.setVisibility(View.VISIBLE);
			findViewById(R.id.translucent).setVisibility(View.VISIBLE);
			parent.startAnimation(AnimationUtils.loadAnimation(mActivity,
					R.anim.push_up_in));
		} else if (v.getId() == R.id.goods_detail_buy_qminus) {
			if (mQuantity <= 1)
				return;
			mQuantity -= 1;
			mQuantityTV.setText(String.valueOf(mQuantity));
		} else if (v.getId() == R.id.goods_detail_buy_qplus) {
			if (mLimit > 0) {
				if (mQuantity < mLimit) {
					mQuantity += 1;
					mQuantityTV.setText(String.valueOf(mQuantity));
				}
			} else {
				mQuantity += 1;
				mQuantityTV.setText(String.valueOf(mQuantity));
			}
		} else if (v.getId() == R.id.goods_detail_buy_confirm) {
			if (mSelectedSku == null && skus_list.size() == 1)
				mSelectedSku = skus_list.get(0);

			// 不能为空
			if (mSelectedSku == null) {
				Run.alert(mActivity, R.string.please_choose_goods_properties);
				return;
			}

			// 提示库存不足
			if (mQuantity > mSelectedSku.optInt("quantity")) {
				Run.alert(mActivity, mActivity.getString(
						R.string.goods_detail_stock_not_enough,
						mSelectedSku.optString("quantity")));
				return;
			}

			// 添加到购物车
			if (mSelectedSku != null) {
				String bn = mSelectedSku.optString("sku_id");
				JsonTaskHandler newTask = (mDetailType == DETAIL_GROUP_BUY) ? new GroupBuySubmitTask(
						bn, mQuantity)
						: new AddCartTask(
								(DoActivity) mActivity,
								mAddCarCallback,
								bn,
								mQuantity,
								(mEventButton == R.id.goods_detail_justbuy) ? "is_fastbuy"
										: Run.EMPTY_STR);
				Log.e("ldy","bn:"+bn+"--mQuantity"+mQuantity);
				Run.excuteJsonTask(new JsonTask(), newTask);
			}
		} else if (v.getId() == R.id.goods_detail_radio_onsale) {
			if (mPromotion != null) {
				ViewGroup viewGroup = (ViewGroup) findViewById(R.id.goods_detail_promotion);
				if (viewGroup.getVisibility() == View.VISIBLE) {
					viewGroup.setVisibility(View.GONE);
				} else {
					if (viewGroup.getChildCount() < 1) {
						JSONArray goods = mPromotion.optJSONArray("goods");
						int count = (goods == null) ? 0 : goods.length();
						for (int i = 0; i < count; i++) {
							View item = mLayoutInflater.inflate(
									R.layout.item_detail_promotion_view, null);
							((TextView) item
									.findViewById(R.id.item_detail_promotion_tag))
									.setText(goods.optJSONObject(i).optString(
											"tag"));
							((TextView) item
									.findViewById(R.id.item_detail_promotion_name))
									.setText(goods.optJSONObject(i).optString(
											"name"));
							viewGroup.addView(item);
						}
						viewGroup.setVisibility(count > 0 ? View.VISIBLE
								: View.GONE);
					} else {
						viewGroup.setVisibility(View.VISIBLE);
					}
				}
			}
		} else if (v.getId() == R.id.goods_detail_search) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_GOODS_SEARCH));
		} else if (v.getId() == R.id.goods_detail_button_category) {
			Intent intent = new Intent(mActivity, MainTabFragmentActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			MainTabFragmentActivity.mTabActivity.setCurrentTabByIndex(1);
			startActivity(intent);
		} else if (v == mQuantityTV) {
			// mCountLayout.setVisibility(View.VISIBLE);
			// if (mQuantity > 0) {
			// mWheelView.setCurrentItem(mQuantity - 1);
			// }
			// mCountLayout.startAnimation(AnimationUtils.loadAnimation(mActivity,
			// R.anim.push_up_in));
		} else if (v.getId() == R.id.fragment_goods_detail_cancel) {
			Animation animOut = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_down_out);
			animOut.setAnimationListener(new SimpleAnimListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					mCountLayout.setVisibility(View.GONE);
				}
			});
			mCountLayout.startAnimation(animOut);
		} else if (v.getId() == R.id.fragment_goods_detail_comfirm) {
			// mQuantity = mWheelView.getCurrentItem() + 1;
			mQuantityTV.setText(String.valueOf(mQuantity));
			Animation animOut = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_down_out);
			animOut.setAnimationListener(new SimpleAnimListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					mCountLayout.setVisibility(View.GONE);
				}
			});
			mCountLayout.startAnimation(animOut);
		} else if (v.getId() == R.id.goods_detail_order_onsale) {
			if (mPromotion != null) {
				ViewGroup viewGroup = (ViewGroup) findViewById(R.id.goods_detail_order_promotion);
				if (viewGroup.getVisibility() == View.VISIBLE) {
					viewGroup.setVisibility(View.GONE);
				} else {
					if (viewGroup.getChildCount() < 1) {
						JSONArray order = mPromotion.optJSONArray("order");
						int count = (order == null) ? 0 : order.length();
						for (int i = 0; i < count; i++) {
							View item = mLayoutInflater.inflate(
									R.layout.item_detail_promotion_view, null);
							((TextView) item
									.findViewById(R.id.item_detail_promotion_tag))
									.setText(order.optJSONObject(i).optString(
											"tag"));
							((TextView) item
									.findViewById(R.id.item_detail_promotion_name))
									.setText(order.optJSONObject(i).optString(
											"name"));
							viewGroup.addView(item);
						}
						viewGroup.setVisibility(count > 0 ? View.VISIBLE
								: View.GONE);
					} else {
						viewGroup.setVisibility(View.VISIBLE);
					}
				}
			}

		} else {
			super.onClick(v);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		FragmentTransaction transaction = mActivity.getSupportFragmentManager()
				.beginTransaction();
		if (!isChecked)
			return;
		mBottomListView.setAdapter(null);
		// mBottomListView.removeHeaderView(koubeiRecommend);
		mBottomListView.setVisibility(View.VISIBLE);
		if (!isFirstIn) {
			myScrollView.scrollTo(0, mRadioGroup.getTop());
		} else {
			isFirstIn = false;
		}
		if (mBottomListView.getParent() != null) {
			Run.removeFromSuperView(mBottomListView);
		}
		if (mWebView.getParent() != null) {
			Run.removeFromSuperView(mWebView);
		}
		TextView emptyView = (TextView) findViewById(R.id.goods_detail_entyview);
		mWebView.setVisibility(View.GONE);
		View vp = rootView.findViewById(R.id.goods_detail_showarea);
		LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) vp
				.getLayoutParams();
		lp1.height = 0;
		lp1.weight = 1;
		vp.setLayoutParams(lp1);
		vp.setMinimumHeight(minHeight);
		if (v.getId() == R.id.goods_detail_radio_comments) {
			mGuessView.setVisibility(View.VISIBLE);
			emptyView.setText("暂无商品推荐信息");
			mBottomListView.setEmptyView(emptyView);
			vp.setMinimumHeight(minHeight);
			mGuessView.setVisibility(View.GONE);
			isShowCommentData = true;
			mBottomListView.setAdapter(recommendAapter);
			mPointArea.setVisibility(View.VISIBLE);
			mBottomListView.setDivider(null);
			transaction.replace(R.id.goods_detail_showarea,new BottomViewCommentFragment());
		} else if (v.getId() == R.id.goods_detail_radio_images) {
			mGuessView.setVisibility(View.VISIBLE);
			vp.setMinimumHeight(minHeight);
			mWebView.setWebViewClient(new WebViewClient(){
				@Override
				public void onPageFinished(WebView view, String url) {//加载完成后重新刷新高度，不然webview显示不全
					super.onPageFinished(view, url);
					View vp = rootView.findViewById(R.id.goods_detail_showarea);
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vp
							.getLayoutParams();
					lp.height = 0;
					lp.weight = 1;
					vp.setLayoutParams(lp);
					vp.setMinimumHeight(minHeight);
				}
			});
			if (!isLoadedData && mGoodsDetailJsonObject != null) {
				String wapintro = mGoodsDetailJsonObject.optString("wapintro");
				if (TextUtils.isEmpty(wapintro)) {
					wapintro = mGoodsDetailJsonObject.optString("description");
				}
				wapintro = wapintro.replaceAll("<img", "<img width=\"100%\"");
				mWebView.loadDataWithBaseURL(Run.MAIN_URL, wapintro,
						"text/html", "utf8", "");
				isLoadedData = true;
			}
			isShowCommentData = false;
			mWebView.setVisibility(View.VISIBLE);
			mBottomListView.setVisibility(View.INVISIBLE);
			transaction.replace(R.id.goods_detail_showarea, new WebFragment());
		} else if (v.getId() == R.id.goods_detail_radio_props) {
			isShowCommentData = false;
			mGuessView.setVisibility(View.GONE);
			emptyView.setText("暂无商品属性信息");
			mBottomListView.setEmptyView(emptyView);
			mBottomListView.setAdapter(new PropsValuesAdapter());
			mBottomListView.setDivider(null);
			transaction.replace(R.id.goods_detail_showarea,
					new BottomViewCommentFragment());
		}
		transaction.commitAllowingStateLoss();
	}

	/**
	 * 解析
	 * 
	 * @param all
	 */
	private void parseGoodsDetail(JSONObject top) {
		mGoodsDetailJsonObject = top;
		mGoodsIID = mGoodsDetailJsonObject.optString("iid");
		Run.excuteJsonTask(new JsonTask(), new GetRecomendGoods(mGoodsIID));
		spec_infos.clear();
		skus.clear();

		isFavorite = mGoodsDetailJsonObject.optBoolean("is_faved", false);
		if (isFavorite) {
			((ImageButton) findViewById(R.id.goods_detail_like))
					.setImageResource(R.drawable.icon_collectioned);
		} else {
			((ImageButton) findViewById(R.id.goods_detail_like))
					.setImageResource(R.drawable.icon_collection);
		}
		mLimit = top.optInt("buy_limit");
		mPromotion = top.optJSONObject("promotion");
		ViewGroup viewGroup = (ViewGroup) findViewById(R.id.goods_detail_promotion);
		viewGroup.removeAllViews();
		JSONArray goods = mPromotion.optJSONArray("goods");
		int count = (goods == null) ? 0 : goods.length();
		for (int i = 0; i < count; i++) {
			View item = mLayoutInflater.inflate(
					R.layout.item_detail_promotion_view, null);
			((TextView) item.findViewById(R.id.item_detail_promotion_tag))
					.setText(goods.optJSONObject(i).optString("tag"));
			((TextView) item.findViewById(R.id.item_detail_promotion_name))
					.setText(goods.optJSONObject(i).optString("name"));
			viewGroup.addView(item);
		}
		viewGroup.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

		ViewGroup viewGroupOrderP = (ViewGroup) findViewById(R.id.goods_detail_order_promotion);
		viewGroupOrderP.removeAllViews();
		JSONArray orderPromot = mPromotion.optJSONArray("order");
		count = (orderPromot == null) ? 0 : orderPromot.length();
		for (int i = 0; i < count; i++) {
			View item = mLayoutInflater.inflate(
					R.layout.item_detail_promotion_view, null);
			((TextView) item.findViewById(R.id.item_detail_promotion_tag))
					.setText(orderPromot.optJSONObject(i).optString("tag"));
			((TextView) item.findViewById(R.id.item_detail_promotion_name))
					.setText(orderPromot.optJSONObject(i).optString("name"));
			viewGroupOrderP.addView(item);
		}

		viewGroupOrderP.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

		// 添加货品与spec_info的对应关系
		JSONArray array = top.optJSONArray("skus");
		if (array != null && array.length() > 0) {
			for (int i = 0, c = array.length(); i < c; i++) {
				try {
					JSONObject child = array.getJSONObject(i);
					skus.put(child.optString("properties"), child);
					id_key_skus.put(child.optString("sku_id"), child);
					// mSkuQuantity += child.optInt("quantity");
					skus_list.add(child);
				} catch (Exception e) {
				}
			}
		}

		// props_values放入ArrayList中
		array = top.optJSONArray("props_values");
		for (int i = 0, c = (array == null ? 0 : array.length()); i < c; i++) {
			try {
				JSONObject child = array.getJSONObject(i);
				props_values.add(child);
			} catch (Exception e) {
			}
		}
		// spec_info放入ArrayList中
		array = top.optJSONArray("spec_info");
		if (array != null && array.length() > 0) {
			for (int i = 0, c = array.length(); i < c; i++) {
				try {
					JSONObject child = array.getJSONObject(i);
					spec_infos.add(child);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// spec_info排序
		Collections.sort(spec_infos, new Comparator<JSONObject>() {
			public int compare(JSONObject a, JSONObject b) {
				return a.optString("spec_id").compareTo(b.optString("spec_id"));
			};
		});

		fillupItemView(mGoodsDetailJsonObject);
		inflateSpecInfosView(mGoodsDetailJsonObject);
//		if (!isLoadedData && mGoodsDetailJsonObject != null) {
//			String wapintro = mGoodsDetailJsonObject.optString("wapintro");
//			if (TextUtils.isEmpty(wapintro)) {
//				wapintro = mGoodsDetailJsonObject.optString("description");
//			}
//			mWebView.loadDataWithBaseURL(Run.MAIN_URL, wapintro,
//					"text/html", "utf8", "");
//			isLoadedData = true;
//		}
		mVolleyImageLoader.showImage(((ImageView) findViewById(R.id.goods_detail_buy_thumb)), item_imgs.get(0).optString("small_url"));
		((RadioButton) mTabBar.findViewById(R.id.goods_detail_radio_images))
				.setChecked(true);
	}

	/**
	 * 商品属性选择，团购商品无需选择属性
	 * 
	 * @param all
	 */
	private void inflateSpecInfosView(JSONObject all) {
		ll_parent_container.removeView(ll_add_count_view);
		((TextView) findViewById(R.id.goods_detail_buy_title)).setText(all
				.optString("title"));
		if (mDetailType == DETAIL_GROUP_BUY) {
			// 选中的货品
			JSONObject product = mGroupBuyGoodsDetail.optJSONObject("product");
			JSONObject newSku = id_key_skus
					.get(product.optString("product_id"));
			onSkuSelectedSuccess(newSku);
			return;
		}

		ViewGroup containers = (ViewGroup) findViewById(R.id.goods_detail_buy_specinfos);
		for (JSONObject specs : spec_infos) {
			try {
				View view = mInflater.inflate(
						R.layout.fragment_goods_detail_buy_specs, null);
				((TextView) view.findViewById(R.id.goods_detai_buy_specs_title))
						.setText(specs.optString("spec_name"));
				ViewGroup specsContainer = (ViewGroup) view
						.findViewById(R.id.goods_detai_buy_specs_container);
				JSONArray specValues = specs.optJSONArray("spec_values");
				if (TextUtils.equals(specs.optString("spec_type"), "image")) {
					if (specValues != null && specValues.length() > 0) {
						for (int i = 0, c = specValues.length(); i < c; i++) {
							JSONObject child = specValues.getJSONObject(i);
							View childView = mLayoutInflater.inflate(
									R.layout.item_specs_group, null);
							LinearLayout ll = (LinearLayout) childView
									.findViewById(R.id.ll_container);
							ll.setPadding(0, 0, 10, 0);
							CircleImageView img = (CircleImageView) childView
									.findViewById(R.id.goods_detail_buy_specs);
							img.setBorderColor(mActivity.getResources()
									.getColor(R.color.red));
							mVolleyImageLoader.showImage(img,
									child.optString("spec_goods_images"));
							TextView tvName = (TextView) childView
									.findViewById(R.id.tv_color_name);
							tvName.setText(child.optString("spec_value"));
							ll.setOnClickListener(mSpecOnclickListener);
							ll.setTag(R.id.tag_spec_jsonobject, child);
							ll.setId(i);
							ll.setTag(R.id.tag_spec_id,
									specs.optString("spec_id"));
							specsContainer.addView(childView);
							if (i == 0) {// 默认选中第一个
								mSpecOnclickListener.onClick(ll);
							}
						}
						containers.addView(view);
						View viewDiver = new View(mActivity);
						viewDiver
								.setBackgroundResource(R.color.goods_detail_secondary_textcolor);
						viewDiver.setLayoutParams(new LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT, 1));
						containers.addView(viewDiver);
					}
				} else {
					if (specValues != null && specValues.length() > 0) {
						for (int i = 0, c = specValues.length(); i < c; i++) {
							JSONObject child = specValues.getJSONObject(i);
							RadioButton specNameTV = new RadioButton(mActivity,
									null, R.attr.goodsDetailBuySpecTextStyle);
							specNameTV.setText(child.optString("spec_value"));
							specNameTV.setOnClickListener(mSpecOnclickListener);
							specNameTV.setTag(R.id.tag_spec_jsonobject, child);
							specNameTV.setTag(R.id.tag_spec_id,
									specs.optString("spec_id"));
							specsContainer.addView(specNameTV);
						}
						// 默认选中第一个
						mSpecOnclickListener.onClick(specsContainer
								.getChildAt(0));
						containers.addView(view);
						View viewDiver = new View(mActivity);
						viewDiver
								.setBackgroundResource(R.color.goods_detail_secondary_textcolor);
						viewDiver.setLayoutParams(new LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT, 1));
						containers.addView(viewDiver);

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		containers.addView(ll_add_count_view);
		View viewDiver = new View(mActivity);
		viewDiver
				.setBackgroundResource(R.color.goods_detail_secondary_textcolor);
		viewDiver.setLayoutParams(new LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 1));
		containers.addView(viewDiver);
	}

	/**
	 * 商品属性选择成功
	 * 
	 * @param newSku
	 */
	private void onSkuSelectedSuccess(JSONObject newSku) {
		((TextView) findViewById(R.id.goods_detail_buy_price)).setText(Run
				.buildString("￥", newSku.optString("price")));
		((TextView) findViewById(R.id.goods_detail_buy_stock))
				.setText(mActivity.getString(R.string.goods_detail_buy_stock,
						newSku.optString("quantity")));
		this.mSelectedSku = newSku;
	}

	/**
	 * 填充列表信息
	 * 
	 * @param convertView
	 * @param all
	 * @param key
	 */
	private void fillupItemView(JSONObject all) {

		// 显示简介内容
		tvBrief.setText(all.optString("brief"));
		// 显示购物提示
		gridViewTip.setAdapter(new ShoppingTipAdapter(all.optJSONObject("tip")
				.optJSONArray("tip_text")));
		if (all.optJSONObject("brand") != null) {
			((TextView) findViewById(R.id.detail_brand_name)).setText("品牌："
					+ all.optJSONObject("brand").optString("brand_name"));
			findViewById(R.id.rel_brand_view)
					.setTag(all.optJSONObject("brand"));
			mVolleyImageLoader.showImage(
					(ImageView) findViewById(R.id.img_brand_logo), all
							.optJSONObject("brand").optString("logo_src"));
		} else {
			((TextView) findViewById(R.id.detail_brand_name)).setText("品牌：未知");
		}

		// 团购详情
		if (mDetailType == DETAIL_GROUP_BUY) {
			mHandler.sendEmptyMessage(0);
			JSONObject data = mGroupBuyGoodsDetail;
			// 标题在加个上面
			View titleView = findViewById(R.id.goods_detail_title);
			LinearLayout parent = ((LinearLayout) titleView.getParent());
			parent.removeView(titleView);
			parent.addView(titleView, 1);
			((TextView) findViewById(R.id.goods_detail_oldprice)).setText(Run
					.buildString("￥", data.optString("old_price")));
			((TextView) findViewById(R.id.goods_detail_price)).setText(Run
					.buildString("￥", data.optString("price")));
		} else {
			String price = "";
			if (mGoodsDetailInfo != null) {
				mLimit = mGoodsDetailInfo.optInt("buy_limit");
				if (mLimit > 0) {
					((TextView) rootView
							.findViewById(R.id.goods_detail_limit_quantity))
							.setText(mActivity.getString(
									R.string.activity_limit_quantity, 1));
				}
				price = mGoodsDetailInfo.optString("price");
			}
			JSONArray skusList = all.optJSONArray("skus");
			if (skusList != null && skusList.length() > 0) {
				JSONObject sku = skusList.optJSONObject(0);
				boolean is_startby = sku.optBoolean("is_starbuy", false);
				if (is_startby) {
					JSONObject info = sku.optJSONObject("starbuy_info");
					findViewById(R.id.goods_detail_layout).setVisibility(
							View.VISIBLE);
					if (info != null) {
						price = info.optString("promotion_price");
					}
					long remainTime = info.optLong("end_time")
							- all.optLong("system_time");
					int seconds = (int) (remainTime % 60);
					int minutes = (int) (remainTime / 60 % 60);
					int hours = (int) (remainTime / (60 * 60) % 24);
					if (remainTime < 0) {
						seconds = 0;
						minutes = 0;
						hours = 0;
					}
					((RushBuyCountDownTimerView) findViewById(R.id.goods_detail_time))
							.setTime(hours, minutes, seconds);
					((RushBuyCountDownTimerView) findViewById(R.id.goods_detail_time))
							.start();
					((TextView) findViewById(R.id.goods_detail_oldprice))
							.setText(Run.buildString("￥",
									all.optString("market_price")));
				}
			}
			if (TextUtils.isEmpty(price)) {
				price = all.optString("price");
			}
			((TextView) findViewById(R.id.goods_detail_price)).setText(Run
					.buildString("￥", price));
			DecimalFormat formatter = new DecimalFormat();
			formatter.setMaximumFractionDigits(1);
			// double discount = Double.parseDouble(price)
			// / all.optDouble("market_price") * 10;
		}

		((TextView) findViewById(R.id.goods_detail_title)).setText(all
				.optString("title"));
		int darkColor = mResources.getColor(R.color.westore_dark_textcolor);
		// 总销量
		String sales = all.optString("buy_count");
		SpannableStringBuilder builder = new SpannableStringBuilder(sales);
		builder.setSpan(new ForegroundColorSpan(darkColor), 0, sales.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.insert(0, mActivity.getString(R.string.total_sales));
		builder.append(mActivity.getString(R.string.total_sales_unit));
		((TextView) findViewById(R.id.goods_detail_total_sales))
				.setText(builder);
		// 总评价
		String rates = all.optString("comments_count");
		builder = new SpannableStringBuilder(rates);
		builder.setSpan(new ForegroundColorSpan(darkColor), 0, rates.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		builder.insert(0, mActivity.getString(R.string.total_rates));
		builder.append(mActivity.getString(R.string.total_rates_unit));
		((TextView) findViewById(R.id.goods_detail_total_rate))
				.setText(builder);

		((TextView) findViewById(R.id.goods_detail_oldprice)).getPaint()
				.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		mQuantityTV.setText(String.valueOf(mQuantity));
		try {
			JSONArray imgJson = new JSONArray(all.optString("item_imgs"));
			int count = (imgJson == null) ? 0 : imgJson.length();
			for (int i = 0; i < count; i++)
				item_imgs.add(imgJson.getJSONObject(i));

			CircleFlowIndicator mTopAdsIndicator = (CircleFlowIndicator) rootView
					.findViewById(R.id.goods_detail_images_indicator);
			mTopAdsIndicator.setViewFlow(mBigImagesFlowView);
			mBigImagesFlowView.setAdapter(new FlowAdapter());
			mBigImagesFlowView.setFlowIndicator(mTopAdsIndicator);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillRecomendGoods(JSONObject all) {
		JSONArray list = null;
		try {
			list = all.getJSONArray("data");
			if (list != null && list.length() > 0) {
				GelleryAdapter adapter = new GelleryAdapter(list);
				for (int i = 0; i < list.length(); i++) {
					mRecomentView1.addView(adapter.getView(i, null, null));
				}
			} else {
				findViewById(R.id.good_detail_recommend_title).setVisibility(
						View.INVISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			findViewById(R.id.good_detail_recommend_title).setVisibility(
					View.INVISIBLE);
		}
	}

	// 响应参数选择点击事件
	private OnClickListener mSpecOnclickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof RadioButton) {
				((RadioButton) v).setChecked(true);
			} else if (v instanceof LinearLayout) {
				RadioGroup group = (RadioGroup) v.getParent();
				for (int i = 0, count = group.getChildCount(); i < count; i++) {
					LinearLayout childLL = (LinearLayout) group.getChildAt(i);
					TextView tvName = (TextView) childLL.getChildAt(1);
					CircleImageView img = (CircleImageView) childLL
							.getChildAt(0);
					if (v.getId() == childLL.getId()) {
						tvName.setTextColor(getResources().getColor(
								R.color.goods_detail_pink_textcolor));
						// img.setBackgroundResource(R.drawable.icon_circle_bg_red_small);
						img.setBorderWidth(1);
					} else {
						tvName.setTextColor(getResources().getColor(
								R.color.goods_detail_primary_textcolor));
						// img.setBackgroundResource(R.drawable.icon_circle_bg_gray_small);
						img.setBorderWidth(0);
					}
				}
			}
			if (v.getTag(R.id.tag_spec_id) == null
					|| v.getTag(R.id.tag_spec_jsonobject) == null)
				return;

			JSONObject value = (JSONObject) v.getTag(R.id.tag_spec_jsonobject);
			String key = (String) v.getTag(R.id.tag_spec_id);
			mSelectSpecs.put(key, value);

			// 更新小图
			String imageKey = "spec_goods_images";
			if (value.has(imageKey)
					&& !TextUtils.isEmpty(value.optString(imageKey))
					&& v instanceof LinearLayout) {
				CircleImageView thumbView = ((CircleImageView) findViewById(R.id.goods_detail_buy_thumb));
				thumbView.setBorderColor(getActivity().getResources().getColor(
						R.color.red));
				thumbView.setBorderWidth(1);
				mVolleyImageLoader.showImage(thumbView,
						value.optString(imageKey));
			}

			// 属性选择完毕
			if (mSelectSpecs.size() >= spec_infos.size()) {
				Object[] keys = mSelectSpecs.keySet().toArray();
				Arrays.sort(keys, new Comparator<Object>() {
					@Override
					public int compare(Object a, Object b) {
						return Integer.parseInt(a.toString())
								- Integer.parseInt(b.toString());
					}
				});

				// 根据所选参数查询货品
				String properties = "";
				for (Object newkey : keys) {
					JSONObject child = mSelectSpecs.get(newkey);
					properties = Run.buildString(properties,
							properties.isEmpty() ? "" : ";",
							child.optString("properties"));
				}
				// 选中的货品
				JSONObject newSku = skus.get(properties);
				if (newSku != null)
					onSkuSelectedSuccess(newSku);
			}
		}
	};

	private class GelleryAdapter extends BaseAdapter implements OnClickListener {

		private JSONArray list;

		public GelleryAdapter(JSONArray list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.length();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.fragment_detail_recomend_item, null);
			}
			JSONObject object = list.optJSONObject(position);
			mVolleyImageLoader.showImage((ImageView) convertView
					.findViewById(R.id.item_recomend_img1), object
					.optString("image_default"));
			((TextView) convertView.findViewById(R.id.item_recomend_title))
					.setText(object.optString("name"));
			((TextView) convertView.findViewById(R.id.item_recomend_price1))
					.setText("￥" + object.optString("price"));
			convertView.setTag(object);
			convertView.setOnClickListener(this);
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
						Run.EXTRA_CLASS_ID,
						((JSONObject) v.getTag()).optString("goods_id")));
			}
		}

	}

	// 商品大图浏览
	private class FlowAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return item_imgs.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return item_imgs.get(position);
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
				view.setScaleType(ScaleType.FIT_CENTER);
				convertView = view;
			}

			JSONObject topAdsObject = getItem(position);
			mVolleyImageLoader.showImage((ImageView) convertView,
					topAdsObject.optString("big_url"));

			return convertView;
		}
	}

	public class ShoppingTipAdapter extends BaseAdapter {
		private JSONArray jsonArray;

		public ShoppingTipAdapter(JSONArray jsonArray) {
			this.jsonArray = jsonArray;
		}

		@Override
		public int getCount() {
			return jsonArray == null ? 0 : jsonArray.length();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = mLayoutInflater.inflate(R.layout.item_shopping_tip,
					null);
			TextView tvTipName = (TextView) convertView
					.findViewById(R.id.tv_tip_name);
			try {
				String name = jsonArray.getString(position);
				tvTipName.setText(name);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}

	}

	// 参数说明
	private class PropsValuesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return props_values.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return props_values.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.simple_list_item1, null);

			JSONObject props = getItem(position);
			((TextView) convertView.findViewById(R.id.text2)).setText(Run
					.buildString(props.optString("props_name"), ":"));
			((TextView) convertView.findViewById(R.id.text1)).setText(props
					.optString("props_value"));
			return convertView;
		}
	}

	/* 加入购物车成功 */
	private JsonRequestCallback mAddCarCallback = new JsonRequestCallback() {
		@Override
		public void task_response(String jsonStr) {
			onClick(findViewById(R.id.goods_detail_buy_cancel));
			if (mEventButton == R.id.goods_detail_justbuy) {
				Run.excuteJsonTask(new JsonTask(), new SubmitCarTask());
			} else {
				Run.alert(mActivity, R.string.add_to_shoping_car_success);
				Run.excuteJsonTask(new JsonTask(), new GetCarCountTask());
			}
		}
	};

	/**
	 * 团购购买
	 * 
	 */
	private class GroupBuySubmitTask implements JsonTaskHandler {
		private String product_id;
		private int quantity;

		public GroupBuySubmitTask(String product, int number) {
			this.product_id = product;
			this.quantity = number;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"groupactivity.index.buy").addParams("product_id",
					product_id).addParams("num", String.valueOf(quantity));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					onClick(findViewById(R.id.goods_detail_buy_cancel));
					JsonTaskHandler handler = new SubmitCarTask("group");
					Run.excuteJsonTask(new JsonTask(), handler);
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 取消收藏
	 */
	private class RemoveFavoriteTask implements JsonTaskHandler {

		private String iid;

		public RemoveFavoriteTask(String iid) {
			this.iid = iid;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity, all.optString("data"));
					isFavorite = false;
					((ImageButton) findViewById(R.id.goods_detail_like))
							.setImageResource(R.drawable.icon_collection);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.del_fav");
			req.addParams("gid", iid);
			return req;
		}

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

		public AddCartTask(DoActivity activity, JsonRequestCallback callback,
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

	/**
	 * 添加收藏
	 */
	private class AddFavoriteTask implements JsonTaskHandler {
		private String product_id;

		public AddFavoriteTask(String product) {
			this.product_id = product;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.add_fav").addParams("gid", product_id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity, all.optString("data"));
					isFavorite = true;
					((ImageButton) findViewById(R.id.goods_detail_like))
							.setImageResource(R.drawable.icon_collectioned);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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
						mGoodsCarCountTV.setVisibility(View.VISIBLE);
						int count = 0;
						for (int i = 0; i < goods.length(); i++) {
							count += goods.getJSONObject(i).optInt("quantity");
						}
						Run.goodsCounts = count;
						mGoodsCarCountTV.setText(String
								.valueOf(Run.goodsCounts));
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

	/**
	 * 立即购买
	 */
	private class SubmitCarTask implements JsonTaskHandler {
		private String isFastBuy = "true";

		public SubmitCarTask() {
		}

		public SubmitCarTask(String isFastBuy) {
			this.isFastBuy = isFastBuy;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
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
					String coupon_lists = "";
					if (data.optJSONArray("coupon_lists") != null) {
						coupon_lists = data.optJSONArray("coupon_lists")
								.toString();
					}
					if (mFromExtract != null) {
						startActivity(AgentActivity
								.intentForFragment(
										mActivity,
										AgentActivity.FRAGMENT_SUBMIT_SHOPPING_CAR)
								.putExtra(Run.EXTRA_DATA, data.toString())
								.putExtra(Run.EXTRA_VALUE, isFastBuy)
								.putExtra(Run.EXTRA_FROM_EXTRACT,
										mFromExtract.toString())
								.putExtra(Run.EXTRA_COUPON_DATA, coupon_lists));
					} else {
						startActivity(AgentActivity
								.intentForFragment(
										mActivity,
										AgentActivity.FRAGMENT_SUBMIT_SHOPPING_CAR)
								.putExtra(Run.EXTRA_DATA, data.toString())
								.putExtra(Run.EXTRA_VALUE, isFastBuy)
								.putExtra(Run.EXTRA_COUPON_DATA, coupon_lists));
					}
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取推荐商品
	 */
	private class GetRecomendGoods implements JsonTaskHandler {

		private String goodsId;

		public GetRecomendGoods(String goodsId) {
			this.goodsId = goodsId;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					fillRecomendGoods(all);
				} else {
					findViewById(R.id.good_detail_recommend_title)
							.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.goodslink");
			req.addParams("iid", goodsId);
			return req;
		}

	}

	/**
	 * 获取商品详情
	 */
	private class GoodsDetailTask implements JsonTaskHandler {
		private int detailType = DETAIL_NORMAL;
		private boolean isShowDialog = true;

		public GoodsDetailTask(int detailType) {
			this.detailType = detailType;
		}

		public GoodsDetailTask(int detailType, boolean isShowDialog) {
			this.detailType = detailType;
			this.isShowDialog = isShowDialog;
		}

		@Override
		public JsonRequestBean task_request() {
			if (isShowDialog) {
				showCancelableLoadingDialog();
			}
			if (this.detailType == DETAIL_GROUP_BUY) {
				return new JsonRequestBean(
						"groupactivity.index.get_detail").addParams("act_id",
						mActID);
			}
			return new JsonRequestBean( "mobileapi.goods.get_item")
					.addParams("iid", mGoodsIID)
					.addParams("son_object", "json")
					.addParams("brand_detail", "1");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			rootView.setVisibility(View.VISIBLE);
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject child = all.optJSONObject("data");
					if (this.detailType == DETAIL_GROUP_BUY) {
						if (child != null && !child.isNull("data")) {
							mGroupBuyGoodsDetail = child.optJSONObject("data");
							Run.excuteJsonTask(new JsonTask(),
									new GoodsDetailTask(DETAIL_NORMAL));
						}
					} else {
						if (child != null && !child.isNull("item"))
							parseGoodsDetail(child.optJSONObject("item"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取促销商品详情
	 */
	private class GetGroupBuyDetail implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					hideLoadingDialog_mt();
					rootView.setVisibility(View.VISIBLE);
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						if (!data.isNull("from_extract")) {
							mFromExtract = data.optJSONArray("from_extract");
						}
						if (!data.isNull("info")) {
							mGoodsDetailInfo = data.optJSONObject("info");
						}
						if (!data.isNull("goods")) {
							parseGoodsDetail(data.optJSONObject("goods"));
						}
					}
				} else {// 不成功说明不是促销产品
					findViewById(R.id.goods_detail_shoppingcar).setVisibility(
							View.VISIBLE);
					findViewById(R.id.goods_detail_addto_shopcar)
							.setVisibility(View.VISIBLE);
					Run.excuteJsonTask(new JsonTask(), new GoodsDetailTask(
							mDetailType, false));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getDetail");
			req.addParams("product_id", mProductIID);
			req.addParams("son_object", "json");
			return req;
		}

	}

	/**
	 * 根据product_id 获取 goods_id
	 */
	private class GetGoodsID implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mGoodsIID = all.optString("data");
					Run.excuteJsonTask(new JsonTask(), new GoodsDetailTask(
							mDetailType));
					findViewById(R.id.goods_detail_shoppingcar).setVisibility(
							View.VISIBLE);
					findViewById(R.id.goods_detail_addto_shopcar)
							.setVisibility(View.VISIBLE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.get_gid");
			req.addParams("pid", mScanRezult);
			return req;
		}

	}

	private boolean isFirst = true;
	private int minHeight;
	
	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt){
		if (isFirst) {
			minHeight = rootView.getHeight() - mRadioGroup.getHeight()
					- findViewById(R.id.goods_detail_toolbar).getHeight();
			isFirst = false;
			// FrameLayout.LayoutParams lp = new
			// FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,minHeight);
			// lp.height = minHeight;
			// mBottomListView.setLayoutParams(lp);
			mBottomListView.setMinimumHeight(minHeight);
		}
//		if (t == mRadioGroup.getTop()) {
//			((RadioButton) mTabBar.findViewById(R.id.goods_detail_radio_images))
//			.setChecked(true);
//		}
		int mBuyLayout2ParentTop = Math.max(t, mRadioGroup.getTop());
		if (mBuyLayout2ParentTop < 100) {
			mTabBar.setVisibility(View.INVISIBLE);
		} else {
			mTabBar.setVisibility(View.VISIBLE);
		}
		mTabBar.layout(0, mBuyLayout2ParentTop, mTabBar.getWidth(),
				mBuyLayout2ParentTop + mTabBar.getHeight());
	};

	@Override
	public String getShareImageFile() {
		String imageUrl = getShareImageUrl();
		if (!TextUtils.isEmpty(imageUrl))
			// return CacheUtils.getCacheFile(imageUrl);
			return CacheUtils.getImageCacheFile(imageUrl);
		return null;
	}

	@Override
	public String getShareImageUrl() {
		if (!item_imgs.isEmpty())
			return item_imgs.get(0).optString("big_url");
		return null;
	}

	@Override
	public String getShareText() {
		if (mGoodsDetailJsonObject != null)
			return mGoodsDetailJsonObject.optString("title")+"-"+
					mGoodsDetailJsonObject.optString("brief");
		return null;
	}

	@Override
	public String getShareUrl() {
		return String.format(Run.GOODS_URL , mGoodsIID);
	}

	@SuppressLint("ValidFragment")
	public class BottomViewCommentFragment extends BaseDoFragment {
		public BottomViewCommentFragment() {
			super();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return mBottomListView;
		}
	}

	@SuppressLint("ValidFragment")
	public class WebFragment extends BaseDoFragment {
		public WebFragment() {
			super();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return mWebView;
		}
	}

}
