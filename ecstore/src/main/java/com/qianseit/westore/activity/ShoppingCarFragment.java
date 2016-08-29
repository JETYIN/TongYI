package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class ShoppingCarFragment extends BaseDoFragment {
	private PullToRefreshListView mListView;
	private Button mCheckoutButton;
	private Button mSelectAllButton;
	private TextView mTotalPriceText;
	private TextView mSavedPriceText;
	private View mEmptyView2;

	private ArrayList<JSONObject> mGoodsItems = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mSelectGoodsItems = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> unSelectItems;
	private JSONArray mCoupon;

	private GoodsItemAdapter mAdapter;
	// private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;
	private JSONObject mRemovedGoods;
	private double mTotalPrice = 0;
	private double mShowedPrice = 0;
	private RelativeLayout mRelLayotNull;
	private boolean from_login = false;

	private View headerView;// 头部
	private View bottomView;// 底部

	public ShoppingCarFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.shopping_car);
		if (getActivity() instanceof MainTabFragmentActivity) {
			mActionBar.setShowHomeView(false);
		}
		// mImageLoader = Run.getDefaultImageLoader(mActivity,
		// mActivity.getResources());
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_shopping_car, null);
		headerView = inflater.inflate(R.layout.item_shopping_car_title, null);
		bottomView = inflater.inflate(R.layout.item_shoppingcar_bottom_view, null);
		mRelLayotNull = (RelativeLayout) findViewById(R.id.shopping_rel);
		mEmptyView2 = inflater.inflate(R.layout.pull_to_refresh_emptyview, null);
		mListView = (PullToRefreshListView) findViewById(R.id.shopping_car_listview);
		Run.removeFromSuperView(mRelLayotNull);

		// mListView.getRefreshableView().setEmptyView(mRelLayotNull);
		mListView.getRefreshableView().addHeaderView(headerView);
		mListView.getRefreshableView().addFooterView(bottomView);
		mListView.getRefreshableView().addFooterView(mRelLayotNull);
		bottomView.setVisibility(View.GONE);
		headerView.setVisibility(View.GONE);
		mRelLayotNull.setVisibility(View.GONE);
		mCheckoutButton = (Button) bottomView.findViewById(R.id.shopping_car_checkout);
		mSelectAllButton = (Button) bottomView.findViewById(R.id.shopping_car_select_all);
		mTotalPriceText = (TextView) bottomView.findViewById(R.id.shopping_car_total_price);
		mSavedPriceText = (TextView) bottomView.findViewById(R.id.shopping_car_save_price);
		mTotalPriceText.setText(mActivity.getString(R.string.shopping_car_total_price, "0.00"));
		mSavedPriceText.setText(mActivity.getString(R.string.shopping_car_save_price, 0.f));
		mSelectAllButton.setOnClickListener(this);
		mCheckoutButton.setOnClickListener(this);

		mAdapter = new GoodsItemAdapter();
		mListView.getRefreshableView().setAdapter(mAdapter);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				mGoodsItems.clear();
				mAdapter.notifyDataSetChanged();
				Run.excuteJsonTask(new JsonTask(), new GetCarTask());
			}

			@Override
			public void onRefreshMore() {
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		// 获取购物车数据
		if (!from_login)
			if (unSelectItems != null && unSelectItems.size() > 0) {
				new JsonTask().execute(new ReAdd2ShopCars1(0));
				// add2ShopCar(0);
			} else {
				Run.excuteJsonTask(new JsonTask(), new GetCarTask());
			}
		from_login = false;
	}

	@Override
	public void onClick(View v) {
		if (v == mCheckoutButton) {
			if (!mSelectGoodsItems.isEmpty()) { // 不全选则情况的结算
				if (mSelectGoodsItems.size() != mGoodsItems.size()) {
					unSelectItems = new ArrayList<JSONObject>();
					unSelectItems.addAll(mGoodsItems);
					unSelectItems.removeAll(mSelectGoodsItems);
					JSONArray removeList = new JSONArray();
					for (int i = 0; i < unSelectItems.size(); i++) {
						JSONObject obj = new JSONObject();
						try {
							obj.put("obj_type", "goods");
							obj.put("obj_ident",

							unSelectItems.get(i).optString

							("obj_ident"));
							removeList.put(obj);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					showCancelableLoadingDialog();
					Run.excuteJsonTask(new JsonTask(), new

					DeleteGoods(removeList.toString()));
				} else {
					unSelectItems = null;
					showCancelableLoadingDialog();
					Run.excuteJsonTask(new JsonTask(), new

					SubmitCarTask());
				}
			} else {
				Toast.makeText(mActivity, "请选择要结算的商品",

				Toast.LENGTH_SHORT).show();
			}
		} else if (v == mSelectAllButton) {
			boolean isSelected = mSelectAllButton.isSelected();
			mSelectGoodsItems.clear();
			if (!isSelected) { // 全选
				mSelectGoodsItems.addAll(mGoodsItems);
				mShowedPrice = mTotalPrice;
			} else {
				mShowedPrice = 0;
			}
			// mTotalPriceText.setText(mActivity.getString(
			// R.string.shopping_car_total_price,
			// String.format("%.2f", mShowedPrice)));
			mTotalPriceText.setText("￥" + String.format("%.2f", mShowedPrice));
			resetSelectAllButton(!isSelected);
			mAdapter.notifyDataSetChanged();
		} else {
			super.onClick(v);
		}
	}

	/* 重置全选按钮 */
	private void resetSelectAllButton(boolean isSelected) {
		mSelectAllButton.setSelected(isSelected);
		mSelectAllButton.setCompoundDrawablesWithIntrinsicBounds(isSelected ? R.drawable.order_detail_status4_ok
				: R.drawable.shopping_car_unselected, 0,

		0, 0);
	}

	private void fillupItemView(View view, JSONObject all) {
		try {
			((TextView) view.findViewById(R.id.shopping_car_item_quantity)).setText(all.optString("quantity"));
			// 商品信息
			JSONObject product = all.optJSONObject("obj_items").optJSONArray("products").getJSONObject(0);
			JSONObject prices = product.optJSONObject("price");
			((TextView) view.findViewById(R.id.shopping_car_item_price)).setText(mActivity.getString

			(R.string.shopping_car_price, prices.optString("buy_price")));
			// 原价
			TextView oldPriceTV = (TextView) view.findViewById(R.id.shopping_car_item_oldprice);
			oldPriceTV.setVisibility(View.INVISIBLE);
			oldPriceTV.setText(mActivity.getString

			(R.string.shopping_car_price, prices.optString("buy_price")));
			oldPriceTV.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG |

			Paint.ANTI_ALIAS_FLAG);

			((TextView) view.findViewById(R.id.shopping_car_item_title)).setText(product.optString("name"));
			if (!product.isNull("spec_info"))
				((TextView) view.findViewById

				(R.id.shopping_car_item_info1)).setText(product.optString("spec_info"));
			// 缩略图
			// Uri imageUri = Uri.parse(product.optString("thumbnail_url"));
			ImageView thumbView = (ImageView) view.findViewById(R.id.shopping_car_item_thumb);
			// thumbView.setTag(imageUri);
			// mImageLoader.showImage(thumbView, imageUri);
			mVolleyImageLoader.showImage(thumbView, product.optString

			("thumbnail_url"));
		} catch (Exception e) {
		}
	}

	private class GetCarTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean(

			"mobileapi.cart.get_list");
		}

		@Override
		public void task_response(String json_str) {
			Log.i("atg", "----->>>GetCarTask:" + json_str);
			findViewById(android.R.id.progress).setVisibility(View.GONE);
			hideLoadingDialog_mt();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mGoodsItems.clear();
					mSelectGoodsItems.clear();

					// mListView.getRefreshableView().addHeaderView(headerView);
					// mListView.getRefreshableView().addFooterView(bottomView);

					JSONObject data = all.optJSONObject("data");
					mTotalPrice = data.optDouble

					("promotion_subtotal");
					mShowedPrice = mTotalPrice;
					// mTotalPriceText.setText(mActivity.getString(
					// R.string.shopping_car_total_price,
					// String.format("%.2f",
					// mShowedPrice)));
					mTotalPriceText.setText("￥" + String.format("%.2f", mShowedPrice));
					mSavedPriceText.setText(mActivity.getString(R.string.shopping_car_save_price, data.optDouble

					("discount_amount")));
					JSONObject object = data.optJSONObject("object");
					JSONArray goods = object.optJSONArray("goods");
					int count = 0;
					for (int i = 0, c = goods.length(); i < c; i++) {
						mGoodsItems.add(goods.getJSONObject(i));
						mSelectGoodsItems.add(goods.getJSONObject

						(i));
						count += goods.getJSONObject(i).optInt

						("quantity");
						resetSelectAllButton(true);
					}
					mCoupon = object.optJSONArray("coupon");
					removeCoupon(0);
					Run.goodsCounts = count;
					MainTabFragmentActivity.mTabActivity.setShoppingCarCount(count);
					mAdapter.notifyDataSetChanged();
				} else {
					if (TextUtils.equals(all.optString("res"),

					"need_login"))
						from_login = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 使用未搜索到商品的文字提示view
				mListView.onRefreshComplete();
				ListView lv = mListView.getRefreshableView();
				// lv.removeHeaderView(mEmptyView2);
				if (mGoodsItems.isEmpty() || mGoodsItems.size() < 0) {
					// lv.addHeaderView(mEmptyView2);
					headerView.setVisibility(View.GONE);
					bottomView.setVisibility(View.GONE);
					mRelLayotNull.setVisibility(View.VISIBLE);

				} else {
					mRelLayotNull.setVisibility(View.GONE);
					headerView.setVisibility(View.VISIBLE);
					bottomView.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	/**
	 * 移除绑定在购物车的优惠券
	 * 
	 * @author chesonqin 2014-12-9
	 * @param index
	 */
	private void removeCoupon(int index) {
		if (index >= mCoupon.length()) {
			if (mCoupon.length() != 0) {
				Run.excuteJsonTask(new JsonTask(), new GetCarTask());
			}
		} else {
			new JsonTask().execute(new RemoveCoupon

			(index, mCoupon.optJSONObject(index).optString("obj_ident")));
		}
	}

	/**
	 * 仅获取获取购物车总价
	 * 
	 * @author cheson_qin
	 * 
	 */
	private class GetTotalPrice implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					double tempTotal = data.optDouble

					("promotion_subtotal");
					double balance = mTotalPrice - mShowedPrice;
					mShowedPrice = mShowedPrice - mTotalPrice +

					tempTotal + balance;
					mTotalPrice = tempTotal;
					// mTotalPriceText.setText(mActivity.getString(
					// R.string.shopping_car_total_price,
					// String.format("%.2f",
					// mShowedPrice)));
					mTotalPriceText.setText("￥" + String.format("%.2f", mShowedPrice));
					mSavedPriceText.setText(mActivity.getString(R.string.shopping_car_save_price, data.optDouble

					("discount_amount")));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean(

			"mobileapi.cart.get_list");
		}

	}

	/**
	 * 结算时不全选则先删除购物车的不选择的商品，结算之后记得把删除了的商品重新加入购
	 * 
	 * 物车
	 * 
	 * @author cheson_qin
	 * 
	 */
	private class DeleteGoods implements JsonTaskHandler {

		String deleteItems;

		public DeleteGoods(String deleteItems) {
			this.deleteItems = deleteItems;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject resp = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, resp)) {
					Log.i("atg", "----->>>DeleteGoods:" + json_str);
					Run.excuteJsonTask(new JsonTask(), new

					SubmitCarTask());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.batch_remove");
			for (int i = 0; i < mSelectGoodsItems.size(); i++) {
				mGoodsItems.remove(mSelectGoodsItems);
			}
			req.addParams("items", deleteItems);
			return req;
		}

	}

	private void add2ShopCar(int i) {
		if (i < unSelectItems.size()) {
			new JsonTask().execute(new ReAdd2ShopCars(i));
		} else {
			unSelectItems = null;
			Run.excuteJsonTask(new JsonTask(), new GetCarTask());
		}
	}

	private class ReAdd2ShopCars implements JsonTaskHandler {

		private int i;

		public ReAdd2ShopCars(int i) {
			this.i = i;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					add2ShopCar(i + 1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.add");
			try {
				JSONObject tempObj = unSelectItems.get(i);
				JSONObject params = tempObj.getJSONObject("params");
				int product_id = params.getInt("product_id");
				int num = tempObj.optInt("quantity");
				req.addParams("product_id", "" + product_id);
				req.addParams("num", "" + num);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return req;
		}

	}

	private class ReAdd2ShopCars1 implements JsonTaskHandler {

		private int i;

		public ReAdd2ShopCars1(int i) {
			this.i = i;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					add2ShopCar(i);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.add");
			try {
				JSONObject tempObj = unSelectItems.get(i);
				JSONObject params = tempObj.getJSONObject("params");
				int product_id = params.getInt("product_id");
				req.addParams("product_id", "" + product_id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return req;
		}

	}

	private class SubmitCarTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.cart.checkout").addParams("isfastbuy", "false");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all) && !all.isNull

				("data")) {
					JSONObject data = all.optJSONObject("data");
					String coupon_lists = "";
					if (data.optJSONArray("coupon_lists") != null) {
						coupon_lists = data.optJSONArray

						("coupon_lists").toString();
					}
					startActivity(AgentActivity.intentForFragment

					(mActivity,

					AgentActivity.FRAGMENT_SUBMIT_SHOPPING_CAR).putExtra(Run.EXTRA_DATA,

					data.toString()).putExtra(Run.EXTRA_COUPON_DATA,

					coupon_lists));
				}
			} catch (Exception e) {
			}
		}
	}

	private class GoodsItemAdapter extends BaseAdapter implements OnClickListener {
		final int ID_SELECTED = R.id.shopping_car_item_selected;
		final int ID_REMOVE = R.id.shopping_car_item_remove;

		private LayoutInflater inflater;
		private Resources res;

		public GoodsItemAdapter() {
			inflater = mActivity.getLayoutInflater();
			res = mActivity.getResources();
		}

		@Override
		public int getCount() {
			// if (mGoodsItems.size() > 0) {
			// return mGoodsItems.size() + 1;//凑单功能
			// }
			return mGoodsItems.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mGoodsItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView instanceof RelativeLayout)

			{
				int layout = R.layout.fragment_shopping_car_item;
				convertView = inflater.inflate(layout, null);
				convertView.findViewById(ID_SELECTED).setOnClickListener

				(this);
				convertView.findViewById(ID_REMOVE).setOnClickListener

				(this);
				convertView.findViewById(R.id.shopping_car_item_minus).setOnClickListener(this);
				convertView.findViewById(R.id.shopping_car_item_plus).setOnClickListener(this);
				convertView.setOnClickListener(this);
			}

			if (position == mGoodsItems.size()) {
				convertView = inflater.inflate

				(R.layout.item_shopping_coudan, null);
				convertView.findViewById

				(R.id.item_shopping_coudan).setOnClickListener(this);
			} else {
				JSONObject all = getItem(position);
				if (all == null)
					return convertView;

				convertView.setTag(all);
				fillupItemView(convertView, all);
				// 选中与否
				boolean isSelected = mSelectGoodsItems.contains(all);
				convertView.findViewById(ID_SELECTED).setTag(all);
				convertView.findViewById(ID_REMOVE).setTag(all);
				convertView.findViewById

				(R.id.shopping_car_item_plus).setTag(all);
				convertView.findViewById

				(R.id.shopping_car_item_minus).setTag(all);
				((ImageButton) convertView.findViewById(ID_SELECTED)).setImageResource(isSelected ?

				R.drawable.order_detail_status4_ok :

				R.drawable.shopping_car_unselected);
			}

			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.item_shopping_coudan) {
				startActivity(AgentActivity.intentForFragment(mActivity,

				AgentActivity.FRAGMENT_COUDAN));
			}
			if (v.getTag() == null)
				return;

			final JSONObject data = (JSONObject) v.getTag();
			if (v.getId() == R.id.shopping_car_item_itemview) {
				try {
					JSONObject product = data.optJSONObject

					("obj_items").optJSONArray

					("products").getJSONObject(0);
					startActivity(AgentActivity.intentForFragment

					(mActivity,

					AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(Run.EXTRA_CLASS_ID,

					product.optString("goods_id")));
				} catch (Exception e) {
				}
			} else if (v.getId() == R.id.shopping_car_item_plus) {
				Run.excuteJsonTask(new JsonTask(), new UpdateCartTask

				(data, data.optInt("quantity") + 1));
			} else if (v.getId() == R.id.shopping_car_item_minus) {
				int quantity = data.optInt("quantity") - 1;
				if (quantity <= 0) {
					askRemoveGoods(data);
					return;
				}

				Run.excuteJsonTask(new JsonTask(), new UpdateCartTask

				(data, quantity));
			} else if (v.getId() == R.id.shopping_car_item_selected) {
				// 需要改变的价格
				double changedPrice = 0;
				try {
					JSONObject product = data.optJSONObject

					("obj_items").optJSONArray

					("products").getJSONObject(0);
					JSONObject prices = product.optJSONObject

					("price");
					changedPrice = data.optInt("quantity") * prices.optDouble("buy_price");
				} catch (Exception e) {
				}

				if (mSelectGoodsItems.contains(data)) {
					resetSelectAllButton(false);
					mSelectGoodsItems.remove(data);
					((ImageButton) v).setImageResource

					(R.drawable.shopping_car_unselected);
					mShowedPrice -= changedPrice;
				} else {
					mSelectGoodsItems.add(data);
					int numOfSelected = mSelectGoodsItems.size();
					resetSelectAllButton(numOfSelected ==

					mGoodsItems.size());
					((ImageButton) v).setImageResource

					(R.drawable.order_detail_status4_ok);
					mShowedPrice += changedPrice;
				}
				// mTotalPriceText.setText(mActivity.getString(
				// R.string.shopping_car_total_price,
				// String.format("%.2f", mShowedPrice)));
				mTotalPriceText.setText("￥" + String.format("%.2f", mShowedPrice));
			} else if (v.getId() == R.id.shopping_car_item_remove) {
				askRemoveGoods(data);
			}
		}
	}

	// 询问删除商品
	private void askRemoveGoods(final JSONObject data) {
		CustomDialog dialog = new CustomDialog(mActivity).setMessage(R.string.shopping_car_delete);
		dialog.setNegativeButton(R.string.cancel, null);
		dialog.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					// 删除购物车中商品
					mRemovedGoods = data;
					JsonRequestBean bean = new JsonRequestBean

					(

					"mobileapi.cart.remove").addParams("obj_type", data.optString

					("obj_type")).addParams("obj_ident", data.optString("obj_ident"));
					Run.excuteJsonTask(new JsonTask(), new

					RemoveCartTask(bean));
				} catch (Exception e) {
				}
			}
		}).setCancelable(true).show();
	}

	// 更新购物车的商品
	private class UpdateCartTask implements JsonTaskHandler {
		private JSONObject data;
		private int newQuantity;

		public UpdateCartTask(JSONObject data, int newQuantity) {
			this.data = data;
			this.newQuantity = newQuantity;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.cart.update")
					.addParams("obj_type", data.optString("obj_type")).addParams("obj_ident", data.optString

					("obj_ident")).addParams("quantity", String.valueOf

					(newQuantity));
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.excuteJsonTask(new JsonTask(), new GetCarTask

					());
					// data.put("quantity", newQuantity);
					// mAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
			}
		}
	}

	// 删除购物车的商品
	private class RemoveCartTask implements JsonTaskHandler {
		private JsonRequestBean bean;

		public RemoveCartTask(JsonRequestBean bean) {
			this.bean = bean;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mGoodsItems.remove(mRemovedGoods);
					if (mGoodsItems.size() <= 0) {
						// mListView.getRefreshableView().removeHeaderView(headerView);
						// mListView.getRefreshableView().removeFooterView(bottomView);
						// mListView.getRefreshableView().addHeaderView(mEmptyView2);
						headerView.setVisibility(View.GONE);
						bottomView.setVisibility(View.GONE);
						mRelLayotNull.setVisibility(View.VISIBLE);
					}
					int count = 0;
					for (int i = 0; i < mGoodsItems.size(); i++) {
						count += mGoodsItems.get(i).optInt

						("quantity");
					}
					Run.goodsCounts = count;
					MainTabFragmentActivity.mTabActivity.setShoppingCarCount(count);
					mAdapter.notifyDataSetChanged();
					Run.excuteJsonTask(new JsonTask(), new

					GetTotalPrice());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (Exception e) {
			}
		}
	}

	private class RemoveCoupon implements JsonTaskHandler {

		private String obj_ident;
		private int index;

		public RemoveCoupon(int index, String obj_ident) {
			this.obj_ident = obj_ident;
			this.index = index;
		}

		@Override
		public void task_response(String json_str) {
			removeCoupon(index + 1);
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.remove");
			req.addParams("obj_type", "coupon");
			req.addParams("obj_ident", obj_ident);
			return req;
		}

	}
}
