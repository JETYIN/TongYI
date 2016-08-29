package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.alipay.client.AliPayFragment;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.activity.MainTabFragmentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class OrderDetailFragment extends AliPayFragment {
	// 订单状态
	public final int ORDER_STATUS_PAYING = 1;
	public final int ORDER_STATUS_SHIPPING = 2;
	public final int ORDER_STATUS_RECEIVING = 3;
	public final int ORDER_STATUS_COMPLETE = 4;
	public final int ORDER_STATUS_DEAD = 5;

	private ArrayList<JSONObject> mOrderGoods = new ArrayList<JSONObject>();
	private JSONObject dataJson = null, defAddress = null;
	private JSONObject payInfo = null, expressInfo = null;
	// private JSONArray orderLogs;
	private int mOrderStatus;

	private ListView mListView;
	private View mAddressView;
	private View mOrderStateView;
	private View mOrderPriceView;
	private View mOrderModeView;
	private View mOrderState;
	private View mQrcodeLayout;
	private View mShippingLayout;
	private CustomDialog mPayItemsDialog;
	private AlertDialog mOrderpmtDialog;

	private VolleyImageLoader mVolleyImageLoader;
//	private ImageLoader mImageLoader;
//	private Resources mRes;

	private int addSucceedCount;
	private int addFailCount;

	public OrderDetailFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.order_detail_title);
//		mImageLoader = Run.getDefaultImageLoader(mActivity,
//				mActivity.getResources());
//		mRes = mActivity.getResources();
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();

		try {
			dataJson = AgentApplication.getApp(mActivity).mOrderDetail;
			defAddress = dataJson.optJSONObject("consignee");
			expressInfo = dataJson.optJSONObject("shipping");
			payInfo = dataJson.optJSONObject("payinfo");
//			String orderID = dataJson.optString("order_id");
//			if (!TextUtils.isEmpty(orderID))
//				new JsonTask().execute(new OrderDetail(orderID));

			JSONArray goods = dataJson.optJSONArray("goods_items");
			for (int i = 0, c = goods.length(); i < c; i++) {
				JSONObject item = goods.getJSONObject(i);
				mOrderGoods.add(item.optJSONObject("product"));
			}
		} catch (Exception e) {
			mActivity.finish();
		}
	}

	@Override
	public void init(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_order_detail, null);
		mListView = (ListView) findViewById(android.R.id.list);
		findViewById(R.id.order_detail_buy_again).setOnClickListener(this);
		findViewById(R.id.order_detail_apply_retuen).setOnClickListener(this);
		findViewById(R.id.order_detail_pay).setOnClickListener(this);
		mActionBar.getBackButton().setOnClickListener(this);

		mOrderStateView = findViewById(R.id.order_detail_paystate);
		mAddressView = findViewById(R.id.confirm_order_address);
		mOrderPriceView = findViewById(R.id.order_detail_price_info);
		mOrderModeView = findViewById(R.id.order_detail_paysMode_parent);
		mOrderState = findViewById(R.id.order_detail_status_parent);
		mQrcodeLayout = findViewById(R.id.order_detail_big_qrcode_layout);
		mQrcodeLayout.setOnClickListener(this);
		mShippingLayout = findViewById(R.id.order_detail_shipping_layout);
		updateOrderDetailView(dataJson);
		mListView.setAdapter(new OrdersAdapter());
		String shippingCompany = dataJson.optString("logi_name");
		if (!shippingCompany.equals("null") && TextUtils.isEmpty(shippingCompany)) {
			new JsonTask().execute(new ShippingTask(shippingCompany,dataJson.optString("logi_no")));
		} else {
			mShippingLayout.setVisibility(View.GONE);
		}
		IntentFilter intentFilter = new IntentFilter(AccountOrdersFragment.PAY_SUCCEE);
		intentFilter.setPriority(3);
		getActivity().registerReceiver(paySucceedDetail, intentFilter);
	}

	// 更新收货人信息
	private void updateAddressInfo() {
		boolean isEmpty = (defAddress == null);
		// 没有收货人隐藏
		mAddressView.findViewById(R.id.my_address_book_item_name)
				.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
		mAddressView.findViewById(R.id.my_address_book_item_phone)
				.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
		mAddressView.findViewById(R.id.my_address_book_item_address)
				.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);

		if (defAddress != null) {
			((TextView) mAddressView
					.findViewById(R.id.my_address_book_item_address))
					.setText(Run.buildString(defAddress.optString("txt_area"),
							"\n", defAddress.optString("addr")));
			((TextView) mAddressView
					.findViewById(R.id.my_address_book_item_phone))
					.setText(defAddress.optString("mobile"));
			((TextView) mAddressView
					.findViewById(R.id.my_address_book_item_name))
					.setText(defAddress.optString("name"));
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
			getActivity().unregisterReceiver(paySucceedDetail);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// } else if (v.getId() == R.id.order_detail_change_paytype) {
		// Run.excuteJsonTask(new JsonTask(), new LoadCheckoutHistoryTask(0));
		// } else {
		// super.onClick(v);
		// }

		int ViewID = v.getId();
		if (ViewID == R.id.order_detail_buy_again) {
			addRebuy2ShoppingCar(0);
			showLoadingDialog();
		} else if (ViewID == R.id.order_detail_apply_retuen) {
			showReturnDialog();
		} else if (v.getId() == R.id.order_detail_pay) {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.paycenter.dopayment")
					.addParams("payment_order_id",
							dataJson.optString("order_id"))
					.addParams("payment_cur_money",
							dataJson.optString("total_amount"))
					.addParams("payment_pay_app_id",
							payInfo.optString("pay_app_id"));
			Run.excuteJsonTask(new JsonTask(), new BalancePayTask(bean));
		} else if(v == mActionBar.getBackButton()){
			if (findViewById(R.id.exchagne_tip_layout).getVisibility() == View.VISIBLE) {
				findViewById(R.id.exchagne_tip_layout).setVisibility(View.GONE);
				mActionBar.setTitle(R.string.order_detail_title);
			} else {
				getActivity().finish();
			}
		} else if(v.getId() == R.id.order_detail_promotion_img){
			JSONArray pmt = dataJson.optJSONArray("order_pmt");
			if (pmt != null && pmt.length() > 0) {
				StringBuffer sb = new StringBuffer();
				int i = 0;
				JSONObject obj = null;
				for ( ; i < pmt.length() - 1; i++) {
					obj = pmt.optJSONObject(i);
					if (obj != null) {
						sb.append(obj.optString("pmt_memo")+" ( -"+obj.optString("pmt_amount")+" )\n");
					}
				}
				obj = pmt.optJSONObject(pmt.length() - 1);
				if (obj != null){
					sb.append(obj.optString("pmt_memo")+" ( -"+obj.optString("pmt_amount")+" )");
				}
				final CustomDialog dialog = new CustomDialog(mActivity);
				dialog.setTitle("优惠信息");
				dialog.setMessage(sb);
				dialog.setCenterButton(getString(R.string.ok),
						new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.setCancelable(true).setCanceledOnTouchOutside(true).show();
			}
		} else if(v.getId() == R.id.order_detail_order_barcode){
			mQrcodeLayout.setVisibility(View.VISIBLE);
			((ImageView)findViewById(R.id.order_detail_big_qrcode)).setImageBitmap(Util.CreateTwoDCode(mActivity,(String)v.getTag(),250,250));
		} else if(v.getId() == R.id.order_detail_big_qrcode_layout){
			mQrcodeLayout.setVisibility(View.GONE);
		} else {
			super.onClick(v);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (findViewById(R.id.exchagne_tip_layout).getVisibility() == View.VISIBLE) {
				findViewById(R.id.exchagne_tip_layout).setVisibility(View.GONE);
				mActionBar.setTitle(R.string.order_detail_title);
				return true;
			} else {
				getActivity().finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showReturnDialog() {
		if (mOrderpmtDialog == null) {
			mOrderpmtDialog = new AlertDialog.Builder(mActivity).create();
			Window window = mOrderpmtDialog.getWindow();
			window.setGravity(Gravity.BOTTOM); // 此处可以设置dialog显示的位置
			window.setWindowAnimations(R.style.my_dialog_style);
			mOrderpmtDialog.show();
			mOrderpmtDialog.setContentView(R.layout.exchange_goods_view);
			mOrderpmtDialog.findViewById(R.id.exchange_goods_1).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							mOrderpmtDialog.dismiss();
							startActivity(AgentActivity
									.intentForFragment(
											mActivity,
											AgentActivity.FRAGMENT_EXCHAGNE_LIST)
									.putExtra(Run.EXTRA_DATA,
											dataJson.optString("order_id"))
									.putExtra(Run.EXTRA_VITUAL_CATE, "1"));
						}
					});
			mOrderpmtDialog.findViewById(R.id.exchange_goods_2).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							mOrderpmtDialog.dismiss();
//							startActivity(AgentActivity
//									.intentForFragment(
//											mActivity,
//											AgentActivity.FRAGMENT_EXCHAGNE_LIST)
//									.putExtra(Run.EXTRA_DATA,
//											dataJson.optString("order_id"))
//									.putExtra(Run.EXTRA_VITUAL_CATE, "2"));
							findViewById(R.id.exchagne_tip_layout).setVisibility(View.VISIBLE);
							mActionBar.setTitle("换货须知");
						}
					});
			WindowManager windowManager = mActivity.getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			WindowManager.LayoutParams lp = mOrderpmtDialog.getWindow().getAttributes();
			lp.width = (int) (display.getWidth()); // 设置宽度
			mOrderpmtDialog.getWindow().setAttributes(lp);
		} else {
			mOrderpmtDialog.show();
		}
	}

	private void addRebuy2ShoppingCar(int index) {
		if (index > mOrderGoods.size() - 1) {
			hideLoadingDialog();
			if (addFailCount == 0) {
				Toast.makeText(mActivity,
						getString(R.string.account_orders_rebuy_succeed),
						Toast.LENGTH_SHORT).show();
			} else if (addSucceedCount == 0) {
				Toast.makeText(mActivity,
						getString(R.string.account_orders_rebuy_fail),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						mActivity,
						getString(R.string.account_orders_rebuy_part,
								addSucceedCount), Toast.LENGTH_SHORT).show();
			}
			Intent intent = new Intent(getActivity(),
					MainTabFragmentActivity.class);
			MainTabFragmentActivity.mTabActivity.mSelectIndex = 3;
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Run.EXTRA_TAB_POSITION, 3);
			startActivity(intent);
		} else {
			new JsonTask().execute(new RebuyTask(index));
		}
	}

	private class RebuyTask implements JsonTaskHandler {

		private int index;

		public RebuyTask(int index) {
			this.index = index;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject resp = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, resp)) {
					addSucceedCount += 1;
				} else {
					addFailCount += 1;
				}
				addRebuy2ShoppingCar(index + 1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.cart.add");
			String product_id = mOrderGoods.get(index).optString("goods_id");
			req.addParams("product_id", product_id);
			return req;
		}
	}

	// 更新订单详情
	private void updateOrderDetailView(JSONObject data) {
		this.updateAddressInfo();

		((TextView) findViewById(R.id.order_detail_order_no)).setText(mActivity
				.getString(R.string.order_detail_order_no)
				+ data.optString("order_id"));
//		((ImageView) findViewById(R.id.order_detail_order_barcode))
//				.setImageBitmap(Util.CreateOneDCode(mActivity,
//						data.optString("order_id")));
		long timemills = data.optLong("createtime") * 1000L;
//		ImageView qrcodeImg = (ImageView) findViewById(R.id.order_detail_order_barcode);
//		StringBuffer sb = new StringBuffer();
//		sb.append(data.optString("order_id"));
//		sb.append(";");
//		sb.append(defAddress.optString("mobile"));
//		sb.append(";");
//		sb.append("¥"+data.optString("total_amount"));
//		sb.append(";");
//		sb.append(new SimpleDateFormat("yyyy-MM-dd").format(timemills));
//		sb.append(";");
//		sb.append(defAddress.optString("r_time"));
//		qrcodeImg.setTag(sb.toString());
//		qrcodeImg.setImageBitmap(Util.CreateTwoDCode(mActivity,sb.toString(),50,50));
//		qrcodeImg.setOnClickListener(this);
		
		((TextView) findViewById(R.id.order_detail_paytype)).setText(mActivity
				.getString(R.string.order_detail_payinfo)
				+ payInfo.optString("display_name"));
		((TextView) findViewById(R.id.order_detail_create_time))
				.setText(mActivity.getString(R.string.order_detail_create_time)
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(timemills));
		((TextView) findViewById(R.id.order_detail_goods_total_price))
				.setText("￥" + data.optString("cost_item"));
		((TextView) findViewById(R.id.order_detail_sum_quantity))
				.setText(mActivity.getString(
						R.string.account_orders_order_sum_quantity,
						mOrderGoods.size()));
		JSONObject shipping = data.optJSONObject("shipping");
		((TextView) findViewById(R.id.order_detail_express_fee)).setText("￥"
				+ shipping.optString("cost_shipping"));
		((TextView) findViewById(R.id.order_detail_payment_money)).setText("￥"
				+ data.optString("total_amount"));
		((TextView) findViewById(R.id.order_detail_pmt_order)).setText("￥"
				+ data.optString("pmt_order"));
//		if (data.optDouble("pmt_order") > 0) {
//			findViewById(R.id.order_detail_promotion_img).setOnClickListener(this);
//		}

		if (shipping.optString("shipping_id").equals("2")) {// 自提
			((TextView) findViewById(R.id.order_detail_paysMode))
					.setText(getString(R.string.confirm_order_express) + ":");
			((TextView) findViewById(R.id.order_detail_paysMode_title))
					.setText(shipping.optString("shipping_name"));
			((TextView) findViewById(R.id.order_detail_paysMode_shop))
					.setText(getString(R.string.confirm_order_express_delivery_shop)
							+ ":");
			((TextView) findViewById(R.id.order_detail_paysMode_title_shopname))
					.setText(dataJson.optString("branch_name_user"));
			((TextView) findViewById(R.id.order_detail_paysMode_time))
					.setText(getString(R.string.confirm_order_express_self_time)
							+ ":");
			((TextView) findViewById(R.id.order_detail_paysMode_title_timen))
					.setText(defAddress.optString("r_time"));
		} else {// 送货上门
			((TextView) findViewById(R.id.order_detail_paysMode))
					.setText(getString(R.string.confirm_order_express) + ":");
			((TextView) findViewById(R.id.order_detail_paysMode_title))
					.setText(shipping.optString("shipping_name"));
			((TextView) findViewById(R.id.order_detail_paysMode_shop))
					.setText(getString(R.string.confirm_order_express_delivery_time)
							+ ":");
			((TextView) findViewById(R.id.order_detail_paysMode_title_shopname))
					.setText(defAddress.optString("r_time"));
			// ((TextView)
			// findViewById(R.id.order_detail_paysMode_time)).setText("");
			// ((TextView)
			// findViewById(R.id.order_detail_paysMode_title_timen)).setText("");
			findViewById(R.id.order_detail_paysMode_time).setVisibility(
					View.GONE);
			findViewById(R.id.order_detail_paysMode_title_timen).setVisibility(
					View.GONE);
		}

		// 是否可退换货
		TextView textState = (TextView) findViewById(R.id.order_detail_state);
		if ("active".equalsIgnoreCase(dataJson.optString("status"))) {
			if ("1".equalsIgnoreCase(dataJson.optString("pay_status"))
					&& "1".equalsIgnoreCase(dataJson.optString("ship_status"))) {
				findViewById(R.id.order_detail_apply_retuen).setVisibility(
						View.VISIBLE);
			} else {
				findViewById(R.id.order_detail_apply_retuen).setVisibility(
						View.GONE);
			}
		} else {
			findViewById(R.id.order_detail_apply_retuen).setVisibility(
					View.GONE);
		}

		// 订单状态
		if ("dead".equalsIgnoreCase(dataJson.optString("status"))) {
			mOrderStatus = ORDER_STATUS_DEAD;
			textState.setText(R.string.account_orders_state_cancel);
		} else if ("finish".equalsIgnoreCase(dataJson.optString("status"))) {
			mOrderStatus = ORDER_STATUS_COMPLETE;
			textState.setText(R.string.account_orders_state_complete);
		} else if ("1".equalsIgnoreCase(dataJson.optString("delivery_sign_status"))) {
			mOrderStatus = ORDER_STATUS_COMPLETE;
			textState.setText(R.string.account_orders_state_tuotou);
		} else if (dataJson.optInt("ship_status") == 1) {
			textState.setText(R.string.account_orders_state_receive);
			mOrderStatus = ORDER_STATUS_RECEIVING;
		} else if (dataJson.optInt("pay_status") == 0) {
			mOrderStatus = ORDER_STATUS_PAYING;
			// 显示付款按钮
			if (!Run.isOfflinePayType(payInfo))
				findViewById(R.id.order_detail_pay).setVisibility(View.VISIBLE);

			textState.setText(R.string.account_orders_state_paying);
		} else if (dataJson.optInt("ship_status") == 0) {
			mOrderStatus = ORDER_STATUS_SHIPPING;
			textState.setText(R.string.account_orders_state_shipping);
		} else if (dataJson.optInt("ship_status") == 2) {
			textState.setText(R.string.account_orders_state_part_shipping);
		} else if (dataJson.optInt("ship_status") == 3) {
			textState.setText(R.string.account_orders_state_part_refund);
		} else if (dataJson.optInt("ship_status") == 4) {
			textState.setText(R.string.account_orders_state_refund);
		} else {
			mOrderStatus = ORDER_STATUS_COMPLETE;
			textState.setText(R.string.account_orders_state_cancel);
		}

		// 订单追踪

		Run.removeFromSuperView(mOrderStateView);
		Run.removeFromSuperView(mAddressView);
		Run.removeFromSuperView(mOrderPriceView);
		Run.removeFromSuperView(mOrderModeView);
		Run.removeFromSuperView(mOrderState);
		Run.removeFromSuperView(mShippingLayout);
		mOrderStateView.setLayoutParams(new AbsListView.LayoutParams(
				mOrderStateView.getLayoutParams()));
		mAddressView.setLayoutParams(new AbsListView.LayoutParams(mAddressView
				.getLayoutParams()));
		mOrderPriceView.setLayoutParams(new AbsListView.LayoutParams(
				mOrderPriceView.getLayoutParams()));
		mOrderModeView.setLayoutParams(new AbsListView.LayoutParams(
				mOrderModeView.getLayoutParams()));
		mOrderState.setLayoutParams(new AbsListView.LayoutParams(mOrderState
				.getLayoutParams()));
		mShippingLayout.setLayoutParams(new AbsListView.LayoutParams(mShippingLayout
				.getLayoutParams()));
		mListView.addHeaderView(mOrderStateView);
		mListView.addFooterView(mOrderPriceView);
		mListView.addFooterView(mAddressView);
		mListView.addFooterView(mOrderModeView);
		mListView.addFooterView(mShippingLayout);
	}

	private void updateOrderLog() {
		ImageView image = null;
		JSONObject payment = dataJson.optJSONObject("payment");
		String status = dataJson.optString("status");
		if (status.equals("dead")) {
			image = (ImageView) mOrderState
					.findViewById(R.id.order_detail_status_1);
			image.setImageResource(R.drawable.order_detail_status1_ok);
			((TextView) mOrderState
					.findViewById(R.id.order_detail_status_1_title))
					.setText("订单已取消");
			mListView.addFooterView(mOrderState);
			return;
		}
		if (payment.optBoolean("is_cod")) {// 货到付款
			image = (ImageView) mOrderState
					.findViewById(R.id.order_detail_status_1);
			image.setImageResource(R.drawable.order_detail_status1_ok);
			image = (ImageView) mOrderState
						.findViewById(R.id.order_detail_status_2);
			image.setImageResource(R.drawable.order_detail_status2_ok);
		} else {// 先款后货
			if (dataJson.optString("pay_status").equals("1")
					|| dataJson.optString("pay_status").equals("2")) {
				image = (ImageView) mOrderState
						.findViewById(R.id.order_detail_status_1);
				image.setImageResource(R.drawable.order_detail_status1_ok);
				image = (ImageView) mOrderState
						.findViewById(R.id.order_detail_status_2);
				image.setImageResource(R.drawable.order_detail_status2_ok);
			} else if(dataJson.optString("pay_status").equals("0")){
				image = (ImageView) mOrderState
						.findViewById(R.id.order_detail_status_1);
				image.setImageResource(R.drawable.order_detail_status1_ok);
			}
		}
		int shipStatus = dataJson.optInt("ship_status");
		if (shipStatus != 0) {
			image = (ImageView) mOrderState
					.findViewById(R.id.order_detail_status_3);
			image.setImageResource(R.drawable.order_detail_status3_ok);
		}
		if (!dataJson.optString("delivery_sign_status").equalsIgnoreCase("0")) {
			image = (ImageView) mOrderState
					.findViewById(R.id.order_detail_status_4);
			image.setImageResource(R.drawable.order_detail_status4_ok);
		}
		mListView.addFooterView(mOrderState);
	}

	/**
	 * 显示切换支付方式对话框
	 * 
	 * @param child
	 * @throws Exception
	 */
	private void showChangePaymentDialog(JSONArray child, String balance)
			throws Exception {
		int layout = R.layout.fragment_order_detail_payments;
		LayoutInflater inf = mActivity.getLayoutInflater();
		View payView = inf.inflate(layout, null);
		// for (int i = 0, c = child.length(); i < c; i++)
		// ConfirmOrderFragment.updatePayView(child.getJSONObject(i), inf,
		// payView, balance, mPayListener, mImageLoader);
		mPayItemsDialog = new CustomDialog(mActivity);
		mPayItemsDialog.setTitle(R.string.confirm_order_paytype);
		mPayItemsDialog.setCustomView(payView);
		mPayItemsDialog.setCancelable(true);
		mPayItemsDialog.show();
	}

	@Override
	public void onCheckoutHistoryLoaded(String json_str) {
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all)) {
				JSONObject data = all.optJSONObject("data");
				String balance = data.optString("total");
				Run.excuteJsonTask(new JsonTask(), new GetPayItemsTask(balance));
			} else {
				hideLoadingDialog_mt();
			}
		} catch (Exception e) {
			hideLoadingDialog_mt();
		}
	}

	private OnClickListener mPayListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 隐藏对话框
			if (mPayItemsDialog != null)
				mPayItemsDialog.dismiss();
			if (v.getTag() != null) {
				try {
					JSONObject payment = (JSONObject) v.getTag();
					JSONObject data = new JSONObject();
					data.put("pay_app_id", payment.optString("app_id"));
					data.put("payment_name",
							payment.optString("app_display_name"));

					// 更新当前付款方式
					Run.excuteJsonTask(new JsonTask(),
							new ChangeOrderPaymentTask(data.toString()));
				} catch (Exception e) {
				}
			}
		}
	};

	private class OrdersAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return mOrderGoods.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mOrderGoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_confirm_order_item, null);
				((TextView) convertView
						.findViewById(R.id.account_orders_item_oldprice))
						.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
				convertView.setOnClickListener(this);
			}

			JSONObject product = getItem(position);
			try {
				convertView.setTag(product);
				((TextView) convertView
						.findViewById(R.id.account_orders_item_title))
						.setText(product.optString("name"));
				// ((TextView) convertView
				// .findViewById(R.id.account_orders_item_summary))
				// .setText(product.optString("attr"));
				((TextView) convertView
						.findViewById(R.id.account_orders_item_price))
						.setText("￥" + product.optString("price"));
				((TextView) convertView
						.findViewById(R.id.account_orders_item_oldprice))
						.setText(product.optString("price"));
				((TextView) convertView
						.findViewById(R.id.account_orders_item_quantity))
						.setText(Run.buildString("x",
								product.optString("quantity")));
				// 缩略图
//				Uri imageUri = Uri
//						.parse(product.optString("thumbnail_pic_src"));
				ImageView thumbView = (ImageView) convertView
						.findViewById(R.id.account_orders_item_thumb);
//				thumbView.setTag(imageUri);
//				mImageLoader.showImage(thumbView, imageUri);
				mVolleyImageLoader.showImage(thumbView, product.optString("thumbnail_pic_src"));
			} catch (Exception e) {
			}

			return convertView;
		}

		@Override
		public void onClick(View v) {
			try {
				JSONObject product = (JSONObject) v.getTag();
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
						Run.EXTRA_CLASS_ID, product.optString("goods_id")));
			} catch (Exception e) {
			}
		}
	}

	/* 获取支付方式 */
	private class GetPayItemsTask implements JsonTaskHandler {
		private String balance = "0.00";

		public GetPayItemsTask(String money) {
			this.balance = money;
		}

		@Override
		public JsonRequestBean task_request() {
			payInfo = null;
			return new JsonRequestBean(
					"mobileapi.cart.payment_change").addParams("shipping",
					expressInfo.optString("shipping"));
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0)
						showChangePaymentDialog(child, balance);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class ChangeOrderPaymentTask implements JsonTaskHandler {
		private String paymentJson;

		public ChangeOrderPaymentTask(String payment) {
			this.paymentJson = payment;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.order.payment_change").addParams(
					"payment[pay_app_id]", paymentJson).addParams("order_id",
					dataJson.optString("order_id"));
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					payInfo = data.optJSONObject("payinfo");
					dataJson.put("payinfo", payInfo);
				}
			} catch (Exception e) {
			}
		}
	}

	// 余额支付
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
					if (Run.checkPaymentStatus(mActivity, all)) {
						mActivity.setResult(Activity.RESULT_OK);
						mActivity.finish();
					} else {
						callWXPay(all.optJSONObject("data"));
					}
				} else {
					Run.startThirdPartyPayment(mActivity, all);
				}
			} catch (Exception e) {
				Run.alertL(mActivity, R.string.confirm_order_pay_failed);
			}
		}
	}

	private class OrderDetail implements JsonTaskHandler {

		private String orderID;

		public OrderDetail(String orderID) {
			this.orderID = orderID;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					dataJson = all.optJSONObject("data").optJSONObject("order");
					if (dataJson.optJSONArray("order_pmt") != null && dataJson.optJSONArray("order_pmt").length() > 0) {
						View view = findViewById(R.id.order_detail_promotion_img);
						view.setVisibility(View.VISIBLE);
						view.setOnClickListener(OrderDetailFragment.this);
					}
					updateOrderLog();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.orderdetail");
			req.addParams("order_id", orderID);
			return req;
		}

	}
	
	private class ShippingTask implements JsonTaskHandler{

		private String shipCompany ;
		private String shipid ;
		
		public  ShippingTask(String shipCompany,String shipid){
			this.shipCompany = shipCompany;
			this.shipid = shipid;
		}
		
		@Override
		public void task_response(String json_str) {
			String beginStr = "查询结果如下所示";
			if (!TextUtils.isEmpty(json_str) && json_str.contains(beginStr)) {
				String endStr = "</form>";
				if (!TextUtils.isEmpty(json_str) && json_str.contains(endStr)) {
					json_str = json_str.substring(json_str.indexOf(beginStr)+22, json_str.indexOf(endStr));
					String[] d = json_str.split("</p>");
					if (d.length < 0) {
						return ;
					}
					LinearLayout logs = (LinearLayout) mShippingLayout.findViewById(R.id.order_detail_shipping_log);
					LayoutInflater inflater = LayoutInflater.from(mActivity);
					for (int i = 0; i < d.length; i++) {
						if (TextUtils.isEmpty(d[i].trim())) {
							continue;
						}
						String[] ss = d[i].split("<br />");
						View itemV = inflater.inflate(R.layout.item_shipping_log, null); 
						itemV.setBackgroundColor((i % 2 == 0) ? 0xffeeeeee : 0xffeaeaea);
						((TextView)itemV.findViewById(R.id.shipping_time)).setText(ss[0].substring(12));
						((TextView)itemV.findViewById(R.id.shipping_detail)).setText(ss[1]);
						logs.addView(itemV);
					}
				}
			} else {
				mShippingLayout.setVisibility(View.GONE);
			}
		}

		@Override
		public JsonRequestBean task_request() {
			String url = "http://wap.kuaidi100.com/wap_result.jsp?rand="+System.currentTimeMillis()+"&id="+shipCompany+"&fromWeb=&postid="+shipid;
			JsonRequestBean req = new JsonRequestBean(url, "");
			return req;
		}
		
	}
	
	private BroadcastReceiver paySucceedDetail = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AccountOrdersFragment.PAY_SUCCEE)) {
				mActivity.finish();
			}
		}
	};
}
