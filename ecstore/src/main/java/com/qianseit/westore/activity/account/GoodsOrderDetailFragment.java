package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.qianseit.westore.ui.RushBuyCountOrdersDownTimerView;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class GoodsOrderDetailFragment extends AliPayFragment {
	private final int WHAT = 0X100;
	private ListView mListView;
	private LinearLayout mGoodsListLinear;
	private VolleyImageLoader mVolleyImageLoader;
	private RelativeLayout mRelPayStatue;
	private TextView mPriceTitle;
	private TextView mPrice;
	private Button PayBut;
	private Button CalelBut;
	private LinearLayout mRelPayTime;
	private JSONObject dataJson;
	private JSONObject orderDateJson;
	private JSONObject defAddress;
	private JSONObject payInfo;
	private JSONObject expressInfo;
	private View mTopView;
	private View mTotView;
	private long newTime;
	private LayoutInflater mInflater;
	private boolean isRecommend = false;
	private int mSelect = 0;
	private boolean isFrist=true;
	private Dialog dialog;
	private String strId;
	private boolean isToRecommend = false;
	private SimpleDateFormat mdateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private BaseAdapter mPayAdapter;
	private ArrayList<JSONObject> mOrderGoods = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mOrderPay = new ArrayList<JSONObject>();
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == WHAT) {
				analysisData(msg);
			}
			super.handleMessage(msg);
		}

	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.order_detail_title);
		dataJson = AgentApplication.getApp(mActivity).mOrderDetail;
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		Intent intent = mActivity.getIntent();
		isToRecommend = intent.getBooleanExtra(Run.EXTRA_DETAIL_TYPE, false);
	}

	@SuppressLint("NewApi")
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_orders_detail_main, null);
		rootView.setVisibility(View.GONE);
		mTopView = findViewById(R.id.order_detail_top);
		mTotView = findViewById(R.id.order_detail_fot);
		mGoodsListLinear = (LinearLayout) findViewById(R.id.order_detail_goods_list);
		mListView = (ListView) findViewById(android.R.id.list);
		mRelPayStatue = (RelativeLayout) findViewById(R.id.order_detail_pay_statue);
		mRelPayTime = (LinearLayout) findViewById(R.id.order_detail_pay_time);
		mPriceTitle = (TextView) findViewById(R.id.order_detail_pay_price_title);
		mPrice = (TextView) findViewById(R.id.order_detail_pay_price);
		PayBut = (Button) rootView.findViewById(R.id.order_detail_pay_but);
		CalelBut = (Button) rootView.findViewById(R.id.order_detail_cancel);
	    rootView.findViewById(R.id.order_detail_logistics).setOnClickListener(this);
		PayBut.setOnClickListener(this);
		CalelBut.setOnClickListener(this);
		rootView.findViewById(R.id.order_detail_affirm)
				.setOnClickListener(this);
		Run.removeFromSuperView(mTopView);
		Run.removeFromSuperView(mTotView);
		mTopView.setLayoutParams(new AbsListView.LayoutParams(mTopView
				.getLayoutParams()));
		mTotView.setLayoutParams(new AbsListView.LayoutParams(mTotView
				.getLayoutParams()));
		mListView.addHeaderView(mTopView, null, false);
		mListView.addFooterView(mTotView, null, false);
		mPayAdapter = new PayAdapter();
		mListView.setAdapter(mPayAdapter);
		strId = dataJson.optString("order_id");
		Run.excuteJsonTask(new JsonTask(), new GetOrdersDetailTask(strId));
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean pay = Run.loadOptionBoolean(mActivity, "WXPayResult", false);
		if (pay) {
			Run.savePrefs(mActivity, "WXPayResult", false);
			if (Run.loadOptionBoolean(mActivity, "PayResult", true)) {
				dialog = AccountLoginFragment.showAlertDialog(mActivity,
						"支付成功！", "", "OK", null, new OnClickListener() {

							@Override
							public void onClick(View v) {
								mActivity.setResult(Activity.RESULT_OK);
								mActivity.finish();						
						  }
						}, false, null);
			}
		}
	}

	private void analysisData(Message msg) {
		orderDateJson = (JSONObject) msg.obj;
		defAddress = orderDateJson.optJSONObject("consignee");
		expressInfo = orderDateJson.optJSONObject("shipping");
		payInfo = orderDateJson.optJSONObject("payinfo");
		JSONObject reagobj = orderDateJson.optJSONObject("goods_items");
		// 取出 jsonObject 中的字段的值的空格
		Iterator itt = reagobj.keys();
		while (itt.hasNext()) {
			String key = itt.next().toString();
			JSONObject goodsJSON = reagobj.optJSONObject(key);
			if (goodsJSON != null) {
				mOrderGoods.add(goodsJSON);
			} else {
				continue;
			}
		}
		updateOrderDetailView(orderDateJson);

	}

	private void updateOrderDetailView(JSONObject data) {
		this.updateAddressInfo();
		((TextView) findViewById(R.id.order_detail_id)).setText("订单号："
				+ data.optString("order_id"));
		long time=data.optLong("createtime")*1000;
		((TextView) findViewById(R.id.order_detail_time)).setText("下单时间："
				+ mdateFormat.format(new Date(time)));
		((TextView) findViewById(R.id.order_detail_express_type))
				.setText(expressInfo.optString("shipping_name"));
		((TextView) findViewById(R.id.order_detail_price)).setText("￥"
				+ data.optString("cost_item"));
		((TextView) findViewById(R.id.order_detail_express_price)).setText("￥"
				+ expressInfo.optString("cost_shipping"));
		((TextView) findViewById(R.id.order_detail_total_price)).setText("￥"
				+ data.optString("total_amount"));
		((TextView) findViewById(R.id.order_detail_pay_price)).setText("￥"
				+ data.optString("total_amount"));
		((TextView) findViewById(R.id.order_detail_deduction)).setText("-￥"
				+ ("".equals(data.optString("order_chgpointmoney")) ? "0.00"
						: data.optString("order_chgpointmoney")));// 樱磅抵扣
		((TextView) findViewById(R.id.order_detail_coupon)).setText("-￥"
				+ data.optString("pmt_order"));

		TextView orderIDStatue = (TextView) findViewById(R.id.order_detail_id_status);
		TextView orderPayType = (TextView) findViewById(R.id.order_detail_pay_type);
		JSONObject payJSON = data.optJSONObject("payment");
		orderPayType.setText(payJSON.optString("app_display_name"));
		if ("dead".equalsIgnoreCase(data.optString("status"))) {
			orderIDStatue.setText(R.string.orders_orders_cancel);

		} else if ("finish".equalsIgnoreCase(data.optString("status"))) {
			if (isToRecommend) {
				orderIDStatue.setText(R.string.orders_recommend);
			} else {
				orderIDStatue.setText(R.string.orders_complete);
			}
			rootView.findViewById(R.id.order_detail_logistics).setVisibility(
					View.VISIBLE);
		} else if (data.optInt("ship_status") == 1) {
			orderIDStatue.setText(R.string.account_orders_state_receive);
			rootView.findViewById(R.id.order_detail_affirm).setVisibility(
					View.VISIBLE);
			rootView.findViewById(R.id.order_detail_logistics).setVisibility(
					View.VISIBLE);
		} else if (data.optInt("pay_status") == 0) {
			rootView.findViewById(R.id.order_detail_pay_price_title)
					.setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.order_detail_pay_price).setVisibility(
					View.VISIBLE);
			orderPayType.setText(payJSON.optString(" "));
			orderIDStatue.setText(R.string.account_trade_paying);
			mRelPayTime.setVisibility(View.VISIBLE);
			mPrice.setVisibility(View.VISIBLE);
			mPriceTitle.setVisibility(View.VISIBLE);
			CalelBut.setVisibility(View.VISIBLE);
			PayBut.setVisibility(View.VISIBLE);
			RushBuyCountOrdersDownTimerView rushBuy = (RushBuyCountOrdersDownTimerView) rootView
					.findViewById(R.id.order_detail_go_time);
			long creadTime=data.optLong("createtime");
			long timeNum=newTime-creadTime;
			long payTime=(30*60)-timeNum;
			if(payTime>0){
			 int minute=(int)((payTime % 3600) / 60);
			 int second =(int) (payTime % 60);
			 rushBuy.setTime(minute, second);
			 rushBuy.start();
			 MyCount cont=new MyCount(payTime*1000, 1000);
			 cont.start();
			}else{
				new JsonTask().execute(new CancelOrderTask(strId,
						orderDateJson));
			}
			JSONObject payinfo = data.optJSONObject("payinfo");

			if ("offlinecard".equals(payinfo.opt("pay_app_id"))) {
				// buttonPay.setVisibility(View.GONE);
			} else {
				// buttonPay.setVisibility(View.VISIBLE);
			}
			Run.excuteJsonTask(new JsonTask(), new GetPayTypeTask());
		} else if (data.optInt("ship_status") == 0) {
			orderIDStatue.setText(R.string.account_orders_state_shipping);
		}

		updateGoodsInfo();
	}
	 class MyCount extends CountDownTimer {     
	        public MyCount(long millisInFuture, long countDownInterval) {     
	            super(millisInFuture, countDownInterval);     
	        }     
	        @Override     
	        public void onFinish() {
	          new JsonTask().execute(new CancelOrderTask(strId,
						orderDateJson));
	           mActivity.setResult(Activity.RESULT_OK);
	           mActivity.finish();
	        }     
	        @Override     
	        public void onTick(long millisUntilFinished) {        
	        }    
	    }     
	private void updateGoodsInfo() {
		GoodsListAdapter adapter = new GoodsListAdapter(mOrderGoods,
				isRecommend);
		mGoodsListLinear.removeAllViews();
		for (int i = 0; i < mOrderGoods.size(); i++) {
			mGoodsListLinear.addView(adapter.getView(i, null, null));
		}
	}

	public void ui(int what, Message msg) {
		switch (msg.what) {
		case SDK_PAY_FLAG: {// 支付宝支付结果
			PayResult payResult = new PayResult((String) msg.obj);

			// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
			String resultInfo = payResult.getResult();

			String resultStatus = payResult.getResultStatus();

			// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
			if (TextUtils.equals(resultStatus, "9000")) {
				Toast.makeText(mActivity, "支付成功", Toast.LENGTH_SHORT).show();
				mActivity.setResult(Activity.RESULT_OK);
				mActivity.finish();
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

	private void updateAddressInfo() {
		boolean isEmpty = (defAddress == null);
		// 没有收货人隐藏
		mTopView.findViewById(R.id.order_detail_addres_name).setVisibility(
				isEmpty ? View.INVISIBLE : View.VISIBLE);
		mTopView.findViewById(R.id.order_detail_addres_photo).setVisibility(
				isEmpty ? View.INVISIBLE : View.VISIBLE);
		mTopView.findViewById(R.id.order_detail_addres).setVisibility(
				isEmpty ? View.INVISIBLE : View.VISIBLE);

		if (defAddress != null) {
			((TextView) mTopView.findViewById(R.id.order_detail_addres))
					.setText(Run.buildString("收货地址：",
							defAddress.optString("txt_area"),
							defAddress.optString("addr")));
			((TextView) mTopView.findViewById(R.id.order_detail_addres_photo))
					.setText(defAddress.optString("mobile"));
			((TextView) mTopView.findViewById(R.id.order_detail_addres_name))
					.setText("收货人：" + defAddress.optString("name"));
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.order_detail_pay_but) {
			JSONObject payinfo = orderDateJson.optJSONObject("payinfo");
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.paycenter.dopayment")
					.addParams("payment_order_id",
							orderDateJson.optString("order_id"))
					.addParams("payment_cur_money",
							orderDateJson.optString("total_amount"))
					.addParams("payment_pay_app_id",
							payinfo.optString("pay_app_id"));
			Run.excuteJsonTask(new JsonTask(), new BalancePayTask(bean));
		} else if (v.getId() == R.id.order_detail_logistics) {
			// 物流
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_GOODS_LOGISTICS).putExtra("orderId",strId));
		} else if (v.getId() == R.id.order_detail_affirm) {
			Run.excuteJsonTask(new JsonTask(), new CompleteOrdersTask(
					orderDateJson.optString("order_id")));
		} else if (v.getId() == R.id.order_detail_cancel) {
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
							String id = orderDateJson.optString("order_id");
							new JsonTask().execute(new CancelOrderTask(id,
									orderDateJson));
						}
					}, false, null);
		}
	}

	private class GetOrdersDetailTask implements JsonTaskHandler {
		private String orderId;

		public GetOrdersDetailTask(String orderID) {
			orderId = orderID;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childJSON = all.optJSONObject("data");
					newTime=childJSON.optLong("time_now");
					if (childJSON != null) {
						JSONObject orderJSON = childJSON.optJSONObject("order");
						if (orderJSON != null) {
							rootView.setVisibility(View.VISIBLE);
							Message message = new Message();
							message.what = WHAT;
							message.obj = orderJSON;
							handler.sendMessage(message);
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.orderdetail");
			req.addParams("order_id", orderId);
			return req;
		}

	}

	private class GoodsListAdapter extends BaseAdapter implements
			OnClickListener {

		private List<JSONObject> mGoodsArray;
		private boolean isRecommend;

		public GoodsListAdapter(List<JSONObject> goodsArray, boolean isRecommend) {
			mGoodsArray = goodsArray;
			this.isRecommend = isRecommend;
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
					goodsImage.setTag(product);
					convertView.findViewById(
							R.id.account_orders_goods_recommend)
							.setOnClickListener(this);
					convertView.findViewById(R.id.account_orders_goods_ratings)
							.setOnClickListener(this);
					goodsImage.setOnClickListener(this);

					if (isRecommend) {
						view.setVisibility(View.VISIBLE);
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
			switch (v.getId()) {
			case R.id.account_orders_goods_ratings:

				break;
			case R.id.account_orders_goods_recommend:

				break;
			case R.id.account_orders_item_thumb:
				JSONObject jsonObject = (JSONObject) v.getTag();
				String goodsIID = jsonObject.optString("goods_id");
				Intent intent = AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
						Run.EXTRA_CLASS_ID, goodsIID);
				mActivity.startActivity(intent);
				break;
			default:
				break;
			}

		}

	}

	private class PayAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mOrderPay.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mOrderPay.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int mPosition = position;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.fragment_orders_detai_pay_item, null);
			}
			ImageView imageIcon = (ImageView) convertView
					.findViewById(R.id.account_detail_pay_icon);
			ImageView imageRadio = (ImageView) convertView
					.findViewById(R.id.account_detail_pay_radio);
			TextView nameText = (TextView) convertView
					.findViewById(R.id.account_detail_pay_name);
			JSONObject payData = getItem(position);
			nameText.setText(payData.optString("app_display_name"));
			imageRadio.setTag(payData);
			mVolleyImageLoader.showImage(imageIcon,
					payData.optString("icon_src"));
			if (payData.optString("app_rpc_id").equals(payInfo.optString("pay_app_id"))) {
				imageRadio.setImageResource(R.drawable.my_address_book_default);
			} else {
				imageRadio
						.setImageResource(R.drawable.my_address_book_not_default);
			}
			imageRadio.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					JSONObject data = (JSONObject) v.getTag();

					if(!payInfo.isNull("pay_app_id"))
						payInfo.remove("pay_app_id");
					try {
						payInfo.put("pay_app_id",data.optString("app_rpc_id"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mPayAdapter.notifyDataSetChanged();
					Run.excuteJsonTask(new JsonTask(),
							new changetPayType(data.optString("app_rpc_id")));
				}
			});
			return convertView;
		}

	}

	private class GetPayTypeTask implements JsonTaskHandler {

		public GetPayTypeTask() {
			super();
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray childArray = all.optJSONArray("data");
					if (childArray != null && childArray.length() > 0) {
						for (int i = 0; i < childArray.length(); i++)
							mOrderPay.add(childArray.optJSONObject(i));
						mPayAdapter.notifyDataSetChanged();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.order.select_payment");
			return req;
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
					mActivity.setResult(Activity.RESULT_OK);
					getActivity().finish();
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
					JSONObject obj = all.optJSONObject("data");
					if (obj.optString("pay_app_id").contains("wxpay")) {
						callWXPay(obj);
					} else {
						callAliPay(obj);
					}
				} else {
					Run.startThirdPartyPayment(mActivity, all);
				}
			} catch (Exception e) {
			}
		}
	}

	private class changetPayType implements JsonTaskHandler {
		String payType;

		public changetPayType(String payType) {
			this.payType = payType;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.order.payment_change");
			req.addParams("order_id", orderDateJson.optString("order_id"));
			req.addParams("payment", payType);
			return req;
		}

	}

	private class CompleteOrdersTask implements JsonTaskHandler {
		private String OrderId;

		public CompleteOrdersTask(String OrderId) {
			this.OrderId = OrderId;
		}

		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.order.dofinish");
			bean.addParams("order_id", OrderId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					dialog = AccountLoginFragment.showAlertDialog(mActivity,
							"确认收货成功！", "", "OK", null, new OnClickListener() {                         
								@Override
								public void onClick(View v) {
									getActivity().setResult(Activity.RESULT_OK);
									getActivity().finish();
								}
							}, false, null);
				}
			} catch (Exception e) {
			}
		}
	}
}
