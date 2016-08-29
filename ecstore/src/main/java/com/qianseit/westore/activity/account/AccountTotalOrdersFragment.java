package com.qianseit.westore.activity.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.alipay.client.AliPayFragment;
import com.alipay.client.PayResult;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.ChooseUtils;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountTotalOrdersFragment extends AliPayFragment {
	private final int REQUEST_CODE_ORDER_DETAIL = 0x1001;
	private final int REQUEST_CODE_ORDER_RATING = 0x1002;
	private final int REQUEST_CODE_ORDER_RECOMMEND = 0x1003;
	private PullToRefreshListView mOrdersListView;

	private RelativeLayout mTotalOrderAll;
	private RelativeLayout mTotalOrderPaying;
	private RelativeLayout mTotalOrderShipping;
	private RelativeLayout mTotalOrderReceiving;
	private RelativeLayout mTotalOrderRecommend;
	private RelativeLayout mSelectView;
	private BaseAdapter mOrdersListAdapter;
	private boolean isRecommend = false;
	private int pageNum;
	private JsonTask mTask;
	private LayoutInflater mInflater;
	private TextView mHintText;
	private JSONObject mPayingOrder;

	private VolleyImageLoader mVolleyImageLoader;

	private boolean mShowAllOrders = false; // 全部订单

	private String mPayStatus = "0";
	private String mPayStatusKey = "pay_status";
	private int mOrderState = 0;
	private Dialog dialog;

	private ArrayList<JSONObject> mOrderArray = new ArrayList<JSONObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_orders_title);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		Intent data = mActivity.getIntent();
		mOrderState = data.getIntExtra(Run.EXTRA_VALUE, 0);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_total_orders_main, null);
		mTotalOrderAll = (RelativeLayout) findViewById(R.id.taotal_orders_all);
		mTotalOrderPaying = (RelativeLayout) findViewById(R.id.taotal_orders_paying);
		mTotalOrderShipping = (RelativeLayout) findViewById(R.id.taotal_orders_shipping);
		mTotalOrderReceiving = (RelativeLayout) findViewById(R.id.taotal_orders_receiving);
		mTotalOrderRecommend = (RelativeLayout) findViewById(R.id.taotal_orders_recommend);
		mOrdersListView = (PullToRefreshListView) findViewById(R.id.taotal_orders_listview);
		mHintText = (TextView) findViewById(R.id.total_orders_hint);
		mTotalOrderAll.setOnClickListener(mSaleClickListener);
		mTotalOrderPaying.setOnClickListener(mSaleClickListener);
		mTotalOrderShipping.setOnClickListener(mSaleClickListener);
		mTotalOrderReceiving.setOnClickListener(mSaleClickListener);
		mTotalOrderRecommend.setOnClickListener(mSaleClickListener);
		Run.removeFromSuperView(mHintText);
		mHintText.setLayoutParams(new AbsListView.LayoutParams(mHintText
				.getLayoutParams()));
		mOrdersListView.getRefreshableView().addFooterView(mHintText);
		mOrdersListAdapter = new OrderAdapter();
		mOrdersListView.getRefreshableView().setAdapter(mOrdersListAdapter);
		mOrdersListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

					}

				});
		mOrdersListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					mOrdersListAdapter.notifyDataSetChanged();
				} else {
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(pageNum,false);
			}
		});
		mOrdersListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0,false);
			}

			@Override
			public void onRefreshMore() {
			}
		});

		if (mOrderState == R.id.account_orders_paying) {
			mShowAllOrders = false;
			mPayStatus = "0";
			mPayStatusKey = "pay_status";
			mSelectView = mTotalOrderPaying;
		} else if (mOrderState == R.id.account_orders_shipping) {
			mShowAllOrders = false;
			mPayStatus = "0";
			mPayStatusKey = "ship_status";
			mSelectView = mTotalOrderShipping;
		} else if (mOrderState == R.id.account_orders_receiving) {
			mShowAllOrders = false;
			mPayStatus = "1";
			mPayStatusKey = "ship_status";
			mSelectView = mTotalOrderReceiving;
		} else if (mOrderState == R.id.account_orders_recommend) {
			mSelectView = mTotalOrderRecommend;
			isRecommend = true;
		} else {
			mSelectView = mTotalOrderAll;
			mShowAllOrders = true;
		}
		mSelectView.setSelected(true);
		mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
		loadNextPage(pageNum,true);

	}

	private OnClickListener mSaleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mHintText.setVisibility(View.GONE);
			mSelectView.setSelected(false);
			mSelectView.getChildAt(1).setVisibility(View.GONE);
			if (v == mTotalOrderAll) {
				mTask.isCancelled();
				isRecommend = false;
				mShowAllOrders = true;
				mSelectView = mTotalOrderAll;
			} else if (v == mTotalOrderPaying) {
				mTask.isCancelled();
				isRecommend = false;
				mShowAllOrders = false;
				mPayStatus = "0";
				mPayStatusKey = "pay_status";
				mSelectView = mTotalOrderPaying;
			} else if (v == mTotalOrderShipping) {
				mTask.isCancelled();
				isRecommend = false;
				mShowAllOrders = false;
				mPayStatus = "0";
				mPayStatusKey = "ship_status";
				mSelectView = mTotalOrderShipping;
			} else if (v == mTotalOrderReceiving) {
				mTask.isCancelled();
				isRecommend = false;
				mShowAllOrders = false;
				mPayStatus = "1";
				mPayStatusKey = "ship_status";
				mSelectView = mTotalOrderReceiving;
			} else if (v == mTotalOrderRecommend) {
				mTask.isCancelled();
				isRecommend = true;
				mShowAllOrders = false;
				mSelectView = mTotalOrderRecommend;
			}
			mSelectView.setSelected(true);
			mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
			loadNextPage(0,true);
		}
	};

	private void loadNextPage(int oldPageNum , boolean isShowdialog) {
		this.pageNum = oldPageNum + 1;
		if (this.pageNum == 1) {
			mOrderArray.clear();
			mHintText.setVisibility(View.GONE);
			mOrdersListAdapter.notifyDataSetChanged();
			if (!isShowdialog)
				mOrdersListView.setRefreshing();
		} else {
			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;
		}
		mTask = new JsonTask();
		if (isRecommend) {
			Run.excuteJsonTask(mTask, new GetRecommendOrdersTask(isShowdialog));
		} else {
			Run.excuteJsonTask(mTask, new GetOrdersTask(isShowdialog));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean pay = Run.loadOptionBoolean(mActivity, "WXPayResult", false);
		if (pay) {
			Run.savePrefs(mActivity, "WXPayResult", false);
			if (Run.loadOptionBoolean(mActivity, "PayResult", true)) {
				loadNextPage(0,true);
			}
		}
	}

	@Override
	public void ui(int what, Message msg) {
		switch (msg.what) {
		case SDK_PAY_FLAG: {// 支付宝支付结果
			PayResult payResult = new PayResult((String) msg.obj);

			// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
			String resultInfo = payResult.getResult();

			String resultStatus = payResult.getResultStatus();

			// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
			if (TextUtils.equals(resultStatus, "9000")) {
				loadNextPage(0,true);
			} else {
				// 判断resultStatus 为非“9000”则代表可能支付失败
				// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
				if (TextUtils.equals(resultStatus, "8000")) {
					Toast.makeText(mActivity, "支付结果确认中", Toast.LENGTH_SHORT)
							.show();

				} else {
					// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
					Toast.makeText(mActivity, "支付失败", Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;

		}
		}
	}

	private class OrderAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return mOrderArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mOrderArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			boolean isStatus = false;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.fragment_orders_item,
						null);
			}
			JSONObject all = getItem(position);
			String ordersNum = all.optString("order_id");
			convertView.setTag(all);
			List<JSONObject> mGoodsArrayList = new ArrayList<JSONObject>();
			// 订单号
			TextView textNumber = (TextView) convertView
					.findViewById(R.id.account_orders_item_number);
			View goDetailView = convertView
					.findViewById(R.id.account_orders_item_go_detail);
			goDetailView.setTag(all);
			goDetailView.setOnClickListener(this);
			textNumber.setText(ordersNum);
			LinearLayout content = (LinearLayout) convertView
					.findViewById(R.id.account_orders_item_goods);
			((TextView) convertView
					.findViewById(R.id.account_orders_item_goods_num))
					.setText(all.optString("goods_num"));
			((TextView) convertView
					.findViewById(R.id.account_orders_item_price)).setText(Run
					.buildString("￥", all.optString("total_amount")));
			textNumber.setText(all.optString("order_id"));
			JSONObject expressInfo = all.optJSONObject("shipping");
			((TextView) convertView
					.findViewById(R.id.account_orders_item_freight))
					.setText(expressInfo.optString("cost_shipping") + "元");
			TextView statusTextView = ((TextView) convertView
					.findViewById(R.id.account_orders_item_status));
			Button buttonPay = (Button) convertView
					.findViewById(R.id.account_orders_item_pay);
			Button buttonAffirm = (Button) convertView
					.findViewById(R.id.account_orders_item_affirm);
			Button buttonLogistics = (Button) convertView
					.findViewById(R.id.account_orders_item_logistics);
			Button buttonCancel = (Button) convertView
					.findViewById(R.id.account_orders_item_cancel);
			Button buttonCancelComplete = (Button) convertView
					.findViewById(R.id.account_orders_item_cancel_complete);
			buttonPay.setOnClickListener(this);
			buttonAffirm.setOnClickListener(this);
			buttonCancel.setOnClickListener(this);
			buttonCancelComplete.setOnClickListener(this);
			buttonLogistics.setOnClickListener(this);
			buttonPay.setTag(all);
			buttonCancel.setTag(all);
			buttonAffirm.setTag(all);
			buttonLogistics.setTag(all);
			buttonCancelComplete.setTag(all);
			if ("dead".equalsIgnoreCase(all.optString("status"))) {
				statusTextView.setText(R.string.orders_orders_cancel);
				buttonCancelComplete.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
				buttonAffirm.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonLogistics.setVisibility(View.GONE);
			} else if ("finish".equalsIgnoreCase(all.optString("status"))) {
				// statusTextView.setText(R.string.orders_complete);
				// isRecommend = true;
				if (isRecommend) {
					isStatus = true;
					statusTextView.setText(R.string.orders_recommend);
				} else {
					if ("0".equals(all.optString("is_opinions"))) {
						isStatus = true;
						statusTextView.setText(R.string.orders_recommend);
					} else {
						statusTextView.setText(R.string.orders_complete);
					}
				}
				buttonLogistics.setVisibility(View.VISIBLE);
				buttonPay.setVisibility(View.GONE);
				buttonAffirm.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonCancelComplete.setVisibility(View.GONE);
			} else if (all.optInt("ship_status") == 1) {
				statusTextView.setText(R.string.account_orders_state_receive);
				buttonLogistics.setVisibility(View.VISIBLE);
				buttonPay.setVisibility(View.GONE);
				buttonAffirm.setVisibility(View.VISIBLE);
				buttonCancel.setVisibility(View.GONE);
				buttonCancelComplete.setVisibility(View.GONE);
			} else if (all.optInt("pay_status") == 0) {
				statusTextView.setText(R.string.account_trade_paying);
				buttonLogistics.setVisibility(View.GONE);
				buttonAffirm.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.VISIBLE);
				buttonCancelComplete.setVisibility(View.GONE);

				JSONObject payinfo = all.optJSONObject("payinfo");
				if ("offlinecard".equals(payinfo.opt("pay_app_id"))) {
					buttonPay.setVisibility(View.GONE);
				} else {
					buttonPay.setVisibility(View.VISIBLE);
				}
			} else if (all.optInt("ship_status") == 0) {
				statusTextView.setText(R.string.account_orders_state_shipping);
				buttonLogistics.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
				buttonAffirm.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonCancelComplete.setVisibility(View.GONE);
			} else {
				buttonLogistics.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
				buttonAffirm.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonCancelComplete.setVisibility(View.VISIBLE);
			}

			JSONArray goodsArray = all.optJSONArray("goods_items");

			if (goodsArray != null && goodsArray.length() > 0) {
				for (int i = 0; i < goodsArray.length(); i++)
					mGoodsArrayList.add(goodsArray.optJSONObject(i));
				GoodsListAdapter adapter = new GoodsListAdapter(
						mGoodsArrayList, isStatus, ordersNum);
				content.removeAllViews();
				for (int i = 0; i < mGoodsArrayList.size(); i++) {
					content.addView(adapter.getView(i, null, null));
				}
			}

			return convertView;
		}

		@Override
		public void onClick(View v) {
			final JSONObject order = (JSONObject) v.getTag();
			if (v.getId() == R.id.account_orders_item_pay) {
				JSONObject payinfo = order.optJSONObject("payinfo");
				JsonRequestBean bean = new JsonRequestBean(
						"mobileapi.paycenter.dopayment")
						.addParams("payment_order_id",
								order.optString("order_id"))
						.addParams("payment_cur_money",
								order.optString("total_amount"))
						.addParams("payment_pay_app_id",
								payinfo.optString("pay_app_id"));
				Run.excuteJsonTask(new JsonTask(), new BalancePayTask(bean));
			} else if (v.getId() == R.id.account_orders_item_logistics) {
				// 物流
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_LOGISTICS).putExtra("orderId",order.optString("order_id")));

			} else if (v.getId() == R.id.account_orders_item_affirm) {
				Run.excuteJsonTask(new JsonTask(),
						new CompleteOrdersTask(order.optString("order_id")));

			} else if (v.getId() == R.id.account_orders_item_cancel) {
				// 取消
				dialog = AccountLoginFragment.showAlertDialog(mActivity,
						"是否确定取消该订单？", "取消", "确定", new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}, new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
								String id = order.optString("order_id");
								new JsonTask().execute(new CancelOrderTask(id,
										order));
							}
						}, false, null);
			} else {

				AgentApplication.getApp(mActivity).setOrderDetail(order);
				startActivityForResult(
						AgentActivity.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_GOODS_ORDERS_DETAIL)
								.putExtra(Run.EXTRA_DETAIL_TYPE, isRecommend),
						REQUEST_CODE_ORDER_DETAIL);
			}

		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == REQUEST_CODE_ORDER_DETAIL) {
			loadNextPage(0,true);
		} else if (requestCode == REQUEST_CODE_ORDER_RATING) {
			loadNextPage(0,true);
		} else if (requestCode == REQUEST_CODE_ORDER_RECOMMEND) {
			loadNextPage(0,true);
		}
	}

	private class GetOrdersTask implements JsonTaskHandler {
		boolean isTrue;
		public GetOrdersTask(boolean isTrue){
			this.isTrue=isTrue;
		}
		
		@Override
		public JsonRequestBean task_request() {
			if (isTrue)
				showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.orders");
			if (!mShowAllOrders && !TextUtils.isEmpty(mPayStatus)
					&& !TextUtils.isEmpty(mPayStatusKey)) {
				bean.addParams(mPayStatusKey, mPayStatus);
			}
			bean.addParams("n_page", String.valueOf(pageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			mOrdersListView.onRefreshComplete();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray array = all.optJSONArray("data");
					for (int i = 0, count = array.length(); i < count; i++)
						mOrderArray.add(array.getJSONObject(i));
					if (mOrderArray.size() <= 0) {
						mHintText.setVisibility(View.VISIBLE);
					} else {
						mHintText.setVisibility(View.GONE);
					}
					mOrdersListAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
			}
		}
	}

	private class GoodsListAdapter extends BaseAdapter implements
			OnClickListener {
		private String orderId;
		private List<JSONObject> mGoodsArray;
		private boolean isRecommend;

		public GoodsListAdapter(List<JSONObject> goodsArray,
				boolean isRecommend, String orderId) {
			mGoodsArray = goodsArray;
			this.isRecommend = isRecommend;
			this.orderId = orderId;
		}

		@Override
		public int getCount() {
			if (mGoodsArray != null) {
				return mGoodsArray.size();
			}
			return 0;
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
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.fragment_orders_goods_item, null);
			}
			JSONObject goods = getItem(position);
			if (goods != null) {
				JSONObject product = goods.optJSONObject("product");
				if (product != null) {
					View view = convertView
							.findViewById(R.id.account_orders_item_recommend);
					ImageView goodsImage = (ImageView) convertView
							.findViewById(R.id.account_orders_item_thumb);
					TextView goodsTiTextView = (TextView) convertView
							.findViewById(R.id.account_orders_item_title);
					TextView goodsQuantityextView = (TextView) convertView
							.findViewById(R.id.account_orders_item_quantity);
					TextView goodsPricetView = (TextView) convertView
							.findViewById(R.id.account_orders_item_price);

					// LinearLayout goodsRationgLinear = (LinearLayout)
					// convertView
					// .findViewById(R.id.account_orders_goods_rating_comple);
					LinearLayout goodsRecommend = (LinearLayout) convertView
							.findViewById(R.id.account_orders_goods_recommend);

					View recommendView = convertView
							.findViewById(R.id.account_orders_goods_recommend);
					View ratingView = convertView
							.findViewById(R.id.account_orders_goods_ratings);
					recommendView.setTag(product);
					recommendView.setTag(R.id.about_tel, orderId);
					ratingView.setTag(product);
					ratingView.setTag(R.id.about_tel, orderId);
					goodsImage.setTag(product);
					recommendView.setOnClickListener(this);
					ratingView.setOnClickListener(this);
					goodsImage.setOnClickListener(this);
					if (isRecommend) {
						view.setVisibility(View.VISIBLE);
						if ("0".equals(product.optString("is_comment"))
								&& ("0".equals(product.optString("is_opinions")))) {
							ratingView.setVisibility(View.VISIBLE);
							goodsRecommend.setVisibility(View.VISIBLE);
						} else {
							ratingView.setVisibility(View.GONE);
							goodsRecommend.setVisibility(View.GONE);
						}
					} else {
						view.setVisibility(View.GONE);
					}
					mVolleyImageLoader.showImage(goodsImage,
							product.optString("thumbnail_pic_src"));
					goodsTiTextView.setText(product.optString("name"));
					goodsQuantityextView.setText(Run.buildString("x",
							product.optString("quantity")));
					goodsPricetView.setText(product.optString("price"));

				}
			}
			return convertView;
		}

		@Override
		public void onClick(View v) {
			String mOrderId = (String) v.getTag(R.id.about_tel);
			JSONObject googds = (JSONObject) v.getTag();
			if (v.getId() == R.id.account_orders_goods_ratings) {
				startActivityForResult(
						AgentActivity
								.intentForFragment(mActivity,
										AgentActivity.FRAGMENT_ORDERS_RATING)
								.putExtra(Run.EXTRA_DATA, googds.toString())
								.putExtra(Run.EXTRA_ADDR, mOrderId),
						REQUEST_CODE_ORDER_RATING);
			} else if (v.getId() == R.id.account_orders_goods_recommend) {
				ChooseUtils chooseUtils = new ChooseUtils();
				chooseUtils.setOrder_id(mOrderId);
				String goods_id = googds.optString("goods_id");
				chooseUtils.setGoods_id(goods_id);
				String goods_name = googds.optString("goods_name");
				chooseUtils.setGoods_name(goods_name);
				String brand_name = googds.optString("brand_name");
				chooseUtils.setBrand_name(brand_name);
				String image = googds.optString("image");
				chooseUtils.setImagePath(image);
				Bundle bundle = new Bundle();
				bundle.putSerializable(
						mActivity.getString(R.string.intent_key_serializable),
						chooseUtils);
				Intent intent = AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_SHOOSEG);
				intent.putExtras(bundle);
				startActivity(intent);
			} else if (v.getId() == R.id.account_orders_item_thumb) {
				String goodsIID = googds.optString("goods_id");
				Intent intent = AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
						Run.EXTRA_CLASS_ID, goodsIID);
				mActivity.startActivity(intent);
			}

		}
	}

	private class CancelOrderTask implements JsonTaskHandler {
		private String orderId;
		private JSONObject data;

		public CancelOrderTask(String orderID, JSONObject order) {
			orderId = orderID;
			this.data = order;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alertL(mActivity,
							R.string.account_orders_canceled_order_ok);
					this.data.put("status", "dead");
					loadNextPage(0,true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.cancel");
			req.addParams("order_id", orderId);
			return req;
		}

	}

	private class BalancePayTask implements JsonTaskHandler {
		private JsonRequestBean bean;

		public BalancePayTask(JsonRequestBean bean) {
			this.bean = bean;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mPayingOrder = all;
					// if (Run.checkPaymentStatus(mActivity, all)) {
					// mOrderArray.remove(mPayingOrder);
					// mOrdersListAdapter.notifyDataSetChanged();
					// }
					JSONObject obj = all.optJSONObject("data");
					if (obj.optString("pay_app_id").contains("wxpay")) {
						callWXPay(all.optJSONObject("data"));
					} else {
						callAliPay(all.optJSONObject("data"));
					}
				} else {
					Run.startThirdPartyPayment(mActivity, all);
				}
			} catch (Exception e) {
			}
		}
	}

	private class GetRecommendOrdersTask implements JsonTaskHandler {
		boolean isTrue;
		public GetRecommendOrdersTask(boolean isTrue){
			this.isTrue=isTrue;
		}
		
		@Override
		public JsonRequestBean task_request() {
			if (isTrue) {
				showCancelableLoadingDialog();
			}
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.get_order_opinions");
			bean.addParams("n_page", String.valueOf(pageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			mOrdersListView.onRefreshComplete();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray array = all.optJSONArray("data");
					for (int i = 0, count = array.length(); i < count; i++)
						mOrderArray.add(array.getJSONObject(i));
					if (mOrderArray.size() <= 0) {
						mHintText.setVisibility(View.VISIBLE);
					} else {
						mHintText.setVisibility(View.GONE);
					}

					mOrdersListAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
			}
		}
	}

	private class CompleteOrdersTask implements JsonTaskHandler {
		private String OrderId;

		public CompleteOrdersTask(String OrderId) {
			this.OrderId = OrderId;
		}

		public JsonRequestBean task_request() {
			if (pageNum == 1)
				showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.order.dofinish");
			bean.addParams("order_id", OrderId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			mOrdersListView.onRefreshComplete();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					dialog = AccountLoginFragment.showAlertDialog(mActivity,
							"确认收货成功！", "", "OK", null, new OnClickListener() {

								@Override
								public void onClick(View v) {
									dialog.dismiss();
									loadNextPage(0,true);
								}
							}, false, null);
				}
			} catch (Exception e) {
			}
		}
	}
}
