package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleAnimListener;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.NotifyChangedScrollView;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class GoodsDetailMoreFragment extends BaseDoFragment implements
		OnCheckedChangeListener {
	public final int DETAIL_NORMAL = 0;
	public final int DETAIL_GROUP_BUY = 1;
	public final int DETAIL_SEC_KILL = 2;

	private int mDetailType = DETAIL_NORMAL;

	private VolleyImageLoader mVolleyImageLoader;
//	private ImageLoader mImageLoader;
	private LayoutInflater mInflater;
	private Resources mResources;
	private String mGoodsIID;
	private int mQuantity = 1;
	private int mEventButton;
	private JSONObject mSelectedSku;

	private WebView mWebView;
	private ListView mListView;
	private ViewGroup mPointArea;
	private NotifyChangedScrollView mScrollView;
	private RadioGroup mRadioGroup;
	private TextView mQuantityTV;

	private GoodsCommentsAdapter mCommentsAdapter;

	private JSONObject mGoodsDetailJsonObject;
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

	public GoodsDetailMoreFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResources = mActivity.getResources();
//		mImageLoader = Run.getDefaultImageLoader(mActivity, mResources);
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
		mInflater = mActivity.getLayoutInflater();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// 清空图片内存
		mHandler.removeMessages(0);
//		mImageLoader.clearCache();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Intent data = mActivity.getIntent();
		mGoodsIID = data.getStringExtra(Run.EXTRA_CLASS_ID);
		String dataStr = data.getStringExtra(Run.EXTRA_DATA);
		int viewId = data.getIntExtra(Run.EXTRA_VALUE,
				R.id.goods_detail_radio_images);
		try {
			mGoodsDetailJsonObject = new JSONObject(dataStr);
		} catch (Exception e) {
			mActivity.finish();
		}

		rootView = inflater.inflate(R.layout.fragment_goods_detail_more, null);
		mScrollView = (NotifyChangedScrollView) findViewById(android.R.id.content);
		mWebView = (WebView) findViewById(R.id.goods_detail_webview_images);

		mListView = (ListView) findViewById(android.R.id.list);
		mPointArea = (ViewGroup) findViewById(R.id.goods_detail_point_area);
		mPointArea.setLayoutParams(new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		Run.removeFromSuperView(mPointArea);
		mCommentsAdapter = new GoodsCommentsAdapter(mActivity, mPointArea);

		int shoppingCarId = R.id.goods_detail_topbar_shoppingcar;
		findViewById(R.id.goods_detail_addto_shopcar).setOnClickListener(this);
		findViewById(R.id.goods_detail_justbuy).setOnClickListener(this);
		rootView.findViewById(shoppingCarId).setOnClickListener(this);

		// 商品属性选择
		findViewById(R.id.goods_detail_buy_qminus).setOnClickListener(this);
		findViewById(R.id.goods_detail_buy_qplus).setOnClickListener(this);
		findViewById(R.id.goods_detail_buy_confirm).setOnClickListener(this);
		findViewById(R.id.goods_detail_buy_cancel).setOnClickListener(this);
		mQuantityTV = (TextView) findViewById(R.id.goods_detail_buy_quantity);

		// 团购商品不能添加到购物车
		if (mDetailType == DETAIL_GROUP_BUY)
			rootView.findViewById(R.id.goods_detail_addto_shopcar)
					.setVisibility(View.INVISIBLE);

		mRadioGroup = (RadioGroup) findViewById(R.id.goods_detail_action_radios);
		for (int i = 0, c = mRadioGroup.getChildCount(); i < c; i++)
			((RadioButton) mRadioGroup.getChildAt(i))
					.setOnCheckedChangeListener(this);
		Run.removeFromSuperView(mRadioGroup);
		mActionBar.setCustomTitleView(mRadioGroup);

		((ImageView) findViewById(shoppingCarId)).setImageDrawable(Run
				.getDrawableList(mActivity, R.drawable.goods_detail_shopcar,
						0.5f));

		parseGoodsDetail(mGoodsDetailJsonObject);
		mRadioGroup.check(viewId);
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if (!isChecked)
			return;

		mListView.setAdapter(null);
		mListView.removeHeaderView(mPointArea);
		mListView.setVisibility(View.VISIBLE);
		mWebView.setVisibility(View.INVISIBLE);
		if (v.getId() == R.id.goods_detail_radio_comments) {
			mListView.addHeaderView(mPointArea);
			mListView.setAdapter(mCommentsAdapter);
			mPointArea.setVisibility(View.VISIBLE);
			mCommentsAdapter.loadNextPage(mGoodsIID);
			mListView.setDivider(mResources
					.getDrawable(R.drawable.goods_detail_comments_hline));
		} else if (v.getId() == R.id.goods_detail_radio_images) {
			mWebView.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.INVISIBLE);
		} else if (v.getId() == R.id.goods_detail_radio_props) {
			mListView.setAdapter(new PropsValuesAdapter());
			mListView.setDivider(null);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.goods_detail_topbar_shoppingcar) {
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
			// 打开属性选择界面
			View parent = findViewById(R.id.goods_detail_buy_parent);
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
			mQuantity += 1;
			mQuantityTV.setText(String.valueOf(mQuantity));
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
						bn, mQuantity) : new AddCartTask(bn, mQuantity);
				Run.excuteJsonTask(new JsonTask(), newTask);
			}
		} else {
			super.onClick(v);
		}
	}

	/**
	 * 填充列表信息
	 * 
	 * @param convertView
	 * @param all
	 * @param key
	 */
	private void fillupItemView(JSONObject all) {
		mQuantityTV.setText(String.valueOf(mQuantity));

		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);

		// wapintro不为空则显示wapintro，为空则显示description
		String wapintro = all.optString("wapintro");
		if (TextUtils.isEmpty(wapintro))
			wapintro = all.optString("description");
		mWebView.loadDataWithBaseURL(Run.MAIN_URL, wapintro, "text/html",
				"utf8", "");
	}

	/**
	 * 商品属性选择，团购商品无需选择属性
	 * 
	 * @param all
	 */
	private void inflateSpecInfosView(JSONObject all) {
		((TextView) findViewById(R.id.goods_detail_buy_title)).setText(all
				.optString("title"));

		ViewGroup containers = (ViewGroup) findViewById(R.id.goods_detail_buy_specinfos);
		for (JSONObject specs : spec_infos) {
			try {
				View view = mInflater.inflate(
						R.layout.fragment_goods_detail_buy_specs, null);
				((TextView) view.findViewById(R.id.goods_detai_buy_specs_title))
						.setText(specs.optString("spec_name"));
				// 属性列表
				ViewGroup specsContainer = (ViewGroup) view
						.findViewById(R.id.goods_detai_buy_specs_container);
				JSONArray specValues = specs.optJSONArray("spec_values");
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
				}

				containers.addView(view);
			} catch (Exception e) {
			}
		}
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
	 * 解析
	 * 
	 * @param all
	 */
	private void parseGoodsDetail(JSONObject top) {
		mGoodsDetailJsonObject = top;
		spec_infos.clear();
		skus.clear();

		// 添加货品与spec_info的对应关系
		JSONArray array = top.optJSONArray("skus");
		if (array != null && array.length() > 0) {
			for (int i = 0, c = array.length(); i < c; i++) {
				try {
					JSONObject child = array.getJSONObject(i);
					skus.put(child.optString("properties"), child);
					id_key_skus.put(child.optString("sku_id"), child);
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
				}
			}
		}
		// spec_info排序
		Collections.sort(spec_infos, new Comparator<JSONObject>() {
			public int compare(JSONObject a, JSONObject b) {
				return a.optString("spec_id").compareTo(b.optString("spec_id"));
			};
		});

		inflateSpecInfosView(mGoodsDetailJsonObject);
		fillupItemView(mGoodsDetailJsonObject);
	}

	// 响应参数选择点击事件
	private OnClickListener mSpecOnclickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			((RadioButton) v).setChecked(true);
			if (v.getTag(R.id.tag_spec_id) == null
					|| v.getTag(R.id.tag_spec_jsonobject) == null)
				return;

			JSONObject value = (JSONObject) v.getTag(R.id.tag_spec_jsonobject);
			String key = (String) v.getTag(R.id.tag_spec_id);
			mSelectSpecs.put(key, value);

			// 更新小图
			String imageKey = "spec_goods_images";
			if (value.has(imageKey)
					&& !TextUtils.isEmpty(value.optString(imageKey))) {
				ImageView thumbView = ((ImageView) findViewById(R.id.goods_detail_buy_thumb));
//				thumbView.setTag(Uri.parse(value.optString(imageKey)));
//				mImageLoader.showImage(thumbView, thumbView.getTag());
				mVolleyImageLoader.showImage(thumbView, imageKey);
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
			JsonRequestBean bean = new JsonRequestBean(Run.API_URL,
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
	 * 添加购物车
	 * 
	 */
	private class AddCartTask implements JsonTaskHandler {
		private String product_id;
		private int quantity;

		public AddCartTask(String product, int number) {
			this.product_id = product;
			this.quantity = number;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(Run.API_URL,
					"mobileapi.cart.add").addParams("product_id", product_id)
					.addParams("num", String.valueOf(quantity));
			if (mEventButton == R.id.goods_detail_justbuy)
				bean.addParams("btype", "is_fastbuy");
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					onClick(findViewById(R.id.goods_detail_buy_cancel));
					if (mEventButton == R.id.goods_detail_justbuy) {
						Run.excuteJsonTask(new JsonTask(), new SubmitCarTask());
					} else {
						Run.alert(mActivity,
								R.string.add_to_shoping_car_success);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private class AddFavoriteTask implements JsonTaskHandler {
		private String product_id;

		public AddFavoriteTask(String product) {
			this.product_id = product;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(Run.API_URL,
					"mobileapi.member.add_fav").addParams("gid", product_id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Resources res = mActivity.getResources();
				}
			} catch (Exception e) {
			}
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
			showCancelableLoadingDialog();
			return new JsonRequestBean(Run.API_URL, "mobileapi.cart.checkout")
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
							.putExtra(Run.EXTRA_VALUE, isFastBuy));
				}
			} catch (Exception e) {
			}
		}
	}

	// 参数说明
	private class PropsValuesAdapter extends BaseAdapter {
		private final int[] ids = { android.R.id.text1, android.R.id.text2 };

		@Override
		public int getCount() {
			return (int) Math.ceil(props_values.size() / 2);
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
						R.layout.fragment_goods_detail_more_props_item, null);

			for (int i = 0; i < ids.length; i++) {
				JSONObject props = getItem(position * 2 + i);
				((TextView) convertView.findViewById(ids[i])).setText(Run
						.buildString(props.optString("props_name"), ":",
								props.optString("props_value")));
			}
			return convertView;
		}
	}
}
