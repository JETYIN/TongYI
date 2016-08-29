package com.qianseit.westore.activity;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.ysshopex.wxapi.WXPayEntryActivity;
import com.alipay.client.AliPayFragment;
import com.alipay.client.PayResult;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;


/**
 * 
 * 确认订单
 * 
 */
public class ConfirmOrderFragment extends AliPayFragment {
	private final int REQUEST_ADDRESS = 0x1000;
	private final int REQUEST_EXPRESS = 0x1001;
	private final int REQUEST_INVOICE = 0x1002;
	private final int REQUEST_TICKET = 0x1003;
	private final int REQUEST_PAYMENT = 0x1004;
	private final int REQUEST_IDCARD = 0x1005;

	private JSONArray mCouponLists, couponObj;
	private ArrayList<JSONObject> mOrderGoods = new ArrayList<JSONObject>();
	private JSONObject aCart = null, defAddress = new JSONObject();
	private JSONObject payInfo = null, expressInfo = null;
	private JSONObject priceInfo = null, dataJson = null;
	private JSONObject payJifen = null;
	private JSONObject mInvoiceInfo = null;
	private String isFastBuy = "false";
	private String mNewOrderId = "";
	private String mFromExtract;

	private ListView mListView;
	private View mAddressView;
	private View mExpressView;
	private View mInvoiceView;
	private View mTicketView;
	private View mPayView;
	private View mMemoView;
	private EditText mMemoText;
	private View mIDCardView;
	private View mBView;

	private boolean mTriggerTax;// 是否显示开发票功能

	private VolleyImageLoader mVolleyImageLoader;
	private boolean mPaymentStatus = false;
	private boolean isUseYingBang;
	private int useYingbang;

	private String oldCoupunNum;
	private boolean isSelectedIDCard = true;

	private DecimalFormat df = new DecimalFormat("0.##");

	public ConfirmOrderFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.confirm_order_title);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication()).getImageLoader();

		try {
			Intent intent = mActivity.getIntent();
			isFastBuy = intent.getStringExtra(Run.EXTRA_VALUE);
			if (TextUtils.isEmpty(isFastBuy))
				isFastBuy = "false";
			mFromExtract = intent.getStringExtra(Run.EXTRA_FROM_EXTRACT);
			String dataStr = intent.getStringExtra(Run.EXTRA_DATA);
			JSONObject data = new JSONObject(dataStr);
			mTriggerTax = data.optBoolean("trigger_tax");
			defAddress = data.optJSONObject("def_addr");
			aCart = data.optJSONObject("aCart");
			payJifen = data.optJSONObject("point_dis_html");
			JSONObject object = aCart.optJSONObject("object");
			couponObj = object.optJSONArray("coupon");
			JSONArray goods = object.optJSONArray("goods");
			for (int i = 0, c = goods.length(); i < c; i++)
				mOrderGoods.add(goods.getJSONObject(i));
			this.dataJson = data;
			String coupon_lists = intent.getStringExtra(Run.EXTRA_COUPON_DATA);
			if (!TextUtils.isEmpty(coupon_lists)) {
				mCouponLists = new JSONArray(coupon_lists);
			}
		} catch (Exception e) {
			mActivity.finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// 查询订单支付状态
		if (!TextUtils.isEmpty(mNewOrderId))
			Run.excuteJsonTask(new JsonTask(), new OrderDetailTask());
	}

	@Override
	public void onResume() {
		super.onResume();

		boolean pay = Run.loadOptionBoolean(mActivity, "WXPayResult", false);
		if (pay) {
			Run.savePrefs(mActivity, "WXPayResult", false);
			if (Run.loadOptionBoolean(mActivity, "PayResult", false)) {
				startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ACCOUNT_ORDERS)
						.putExtra(Run.EXTRA_VALUE, R.id.account_orders_shipping));
				getActivity().finish();
				// mPaymentStatus = true;
			} else {
				// startActivity(AgentActivity.intentForFragment(mActivity,
				// AgentActivity.FRAGMENT_ACCOUNT_ORDERS).putExtra(
				// Run.EXTRA_VALUE, R.id.account_orders_paying));
				mPaymentStatus = false;
			}
			resetPaymentStatusView();
		}

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void init(android.view.LayoutInflater inflater, android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_confirm_order, null);
		findViewById(R.id.confirm_order_checkout).setOnClickListener(this);
		findViewById(R.id.confirm_order_promotion_img).setOnClickListener(this);
		findViewById(R.id.confirm_order_pay_state_ok).setOnClickListener(this);
		mListView = (ListView) findViewById(android.R.id.list);

		mAddressView = findViewById(R.id.confirm_order_address);
		mIDCardView = mAddressView.findViewById(R.id.confirm_order_idcard);
		mIDCardView.setOnClickListener(this);
		mAddressView.findViewById(R.id.confirm_order_idcard_tip_name).setOnClickListener(this);
		mExpressView = inflater.inflate(R.layout.fragment_confirm_order_express_item, null);
		mInvoiceView = inflater.inflate(R.layout.fragment_confirm_order_express_item, null);
		((TextView) mInvoiceView.findViewById(R.id.confirm_order_express_title)).setText(R.string.invoice_info);
		((TextView) mInvoiceView.findViewById(R.id.confirm_order_express_message)).setText(R.string.invoice_type_null);
		mPayView = inflater.inflate(R.layout.fragment_confirm_order_express_item, null);
		mBView = inflater.inflate(R.layout.confirm_order_bottomview, null);
		// ((TextView) mPayView.findViewById(R.id.confirm_order_express_title))
		// .setText(R.string.confirm_order_paytype);
		mTicketView = findViewById(R.id.confirm_order_ticket);
		mMemoView = findViewById(R.id.confirm_order_memo_layout);
		mMemoText = (EditText) findViewById(R.id.confirm_order_memo);
		Run.removeFromSuperView(mAddressView);
		Run.removeFromSuperView(mTicketView);
		Run.removeFromSuperView(mPayView);
		Run.removeFromSuperView(mMemoView);

		Run.removeFromSuperView(mBView);

		mAddressView.setLayoutParams(new AbsListView.LayoutParams(mAddressView.getLayoutParams()));
		mTicketView.setLayoutParams(new AbsListView.LayoutParams(mTicketView.getLayoutParams()));
		// View firstFooterView = new View(mActivity);
		// firstFooterView.setLayoutParams(new AbsListView.LayoutParams(
		// LayoutParams.MATCH_PARENT, mActivity.getResources()
		// .getDimensionPixelSize(R.dimen.PaddingLarge)));
		mMemoView.setLayoutParams(new AbsListView.LayoutParams(mMemoView.getLayoutParams()));
		mListView.addHeaderView(mAddressView);
		// mListView.addFooterView(mAddressView);
		// mListView.addFooterView(mExpressView);
		// mListView.addFooterView(mPayView);
		if (mTriggerTax) {// 后台控制是否显示开发票功能
			mListView.addFooterView(mInvoiceView);
		}
		// mListView.addFooterView(mTicketView);
		// mListView.addFooterView(mMemoView);
		mListView.addFooterView(mBView);
		mAddressView.setOnClickListener(this);
		mExpressView.setOnClickListener(this);
		mInvoiceView.setOnClickListener(this);
		mTicketView.setOnClickListener(this);
		mPayView.setOnClickListener(this);
		mBView.findViewById(R.id.confirm_order_ticket).setOnClickListener(this);
		mBView.findViewById(R.id.confirm_order_submit).setOnClickListener(this);
		// 满多少优惠多少
		((CheckBox) mBView.findViewById(R.id.confirm_order_payyingbang_checkbox))
				.setOnCheckedChangeListener(changeListener);

		updateAddressInfo();
		// 自动获取默认快递
		// if (defAddress != null) {
		// String area = Run.parseAddressId(defAddress);
		// if (!TextUtils.isEmpty(area)) {
		// JsonTaskHandler han = new GetExpressTask(area, isFastBuy, true);
		// Run.excuteJsonTask(new JsonTask(), han);
		// }
		// }

		if (couponObj != null && couponObj.length() > 0) {
			((TextView) mBView.findViewById(R.id.confirm_order_status)).setText(couponObj.optJSONObject(0).optString(
					"name"));
			oldCoupunNum = couponObj.optJSONObject(0).optString("coupon");
		}

		JSONArray tips = dataJson.optJSONArray("tip");
		if (tips != null) {
			if (tips.optJSONObject(0) != null) {
				if (tips.optJSONObject(0).optBoolean("need_idcard")) {
					mIDCardView.setVisibility(View.VISIBLE);
					isSelectedIDCard = false;
				}
				((TextView) mAddressView.findViewById(R.id.confirm_order_idcard_tip_name)).setText(tips
						.optJSONObject(0).optString("tip_name"));
				try {
					expressInfo = new JSONObject();
					String ss = "{\"id\":\"" + tips.optJSONObject(0).optString("dt_ids")
							+ "\",\"has_cod\":\"false\",\"dt_name\":\"快递\",\"money\":\"10\"}";
					expressInfo.put("shipping", ss);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		// if (mCouponLists != null) {
		// TextView tv = (TextView)
		// findViewById(R.id.confirm_order_ticket_name);
		// if (mCouponLists.length() > 0)
		// tv.setHint(R.string.confirm_order_ticket_available);
		// }
		mListView.setAdapter(new OrdersAdapter());
		((TextView) mInvoiceView.findViewById(R.id.confirm_order_express_title))
				.setText(R.string.confirm_order_invoice);
		new JsonTask().execute(new GetTotalPriceTask());
		new JsonTask().execute(new GetPayItemsTask());
		countDownHandler.sendEmptyMessage(0);

		if (dataJson.optJSONObject("promotion_new") != null) {
			if (dataJson.optJSONObject("promotion_new").optJSONArray("order") != null) {
				if (dataJson.optJSONObject("promotion_new").optJSONArray("order").optJSONObject(0) != null) {
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_fee_tips)).setText("（"
							+ dataJson.optJSONObject("promotion_new").optJSONArray("order").optJSONObject(0)
									.optString("name") + "）");
					mBView.findViewById(R.id.confirm_order_bottom_fee_tips).setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			int max = payJifen == null?1:payJifen.optInt("max_discount_value");
			double decount = max * (null==payJifen?1:payJifen.optDouble("discount_rate"));
			if (AgentApplication.getLoginedUser(mActivity).getPoint() >= max) {
				if (isChecked) {
					isUseYingBang = true;
					if (priceInfo.optDouble("total_amount") >= decount) {
						useYingbang = (int) max;
					} else {
						useYingbang = (int) (priceInfo.optDouble("total_amount") / payJifen.optDouble("discount_rate"));
					}
					((TextView) mBView.findViewById(R.id.confirm_order_dikou_tip)).setText(Run.buildString("该笔订单可用",
							max, "积分，抵扣", df.format(decount), "元"));
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_tprice)).setText(Run.buildString(
							"￥",
							df.format(priceInfo.optDouble("total_amount") - useYingbang
									* payJifen.optDouble("discount_rate"))));
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_nprice)).setText(Run.buildString(
							"￥",
							df.format(priceInfo.optDouble("total_amount") - useYingbang
									* payJifen.optDouble("discount_rate"))));
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_dikou)).setText(Run.buildString("-￥",
							df.format(useYingbang * payJifen.optDouble("discount_rate"))));
				} else {
					isUseYingBang = false;
					useYingbang = 0;
					((TextView) mBView.findViewById(R.id.confirm_order_dikou_tip)).setText(Run.buildString("该笔订单可用",
							max, "积分，抵扣", df.format(decount), "元"));
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_tprice)).setText(Run.buildString("￥",
							priceInfo.optDouble("total_amount")));
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_nprice)).setText(Run.buildString("￥",
							priceInfo.optDouble("total_amount")));
					((TextView) mBView.findViewById(R.id.confirm_order_bottom_dikou)).setText("￥0");
				}
			} else {
				if (isChecked) {
					AccountLoginFragment.showAlertDialog(mActivity, "当前积分值"
							+ AgentApplication.getLoginedUser(mActivity).getPoint() + ",不足抵单", "", "确定", null, null,
							false, null);
					((CheckBox) mBView.findViewById(R.id.confirm_order_payyingbang_checkbox)).setChecked(false);
				}
			}
		}

	};
	private String idcardId = null;

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
				Toast.makeText(mActivity, "支付成功", Toast.LENGTH_SHORT).show();
				startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ACCOUNT_ORDERS)
						.putExtra(Run.EXTRA_VALUE, R.id.account_orders_shipping));
				getActivity().finish();
				// mPaymentStatus = true;
			} else {
				// 判断resultStatus 为非“9000”则代表可能支付失败
				// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
				if (TextUtils.equals(resultStatus, "8000")) {
					Toast.makeText(mActivity, "支付结果确认中", Toast.LENGTH_SHORT).show();

				} else {
					// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
					// Toast.makeText(mActivity, "支付失败", Toast.LENGTH_SHORT)
					// .show();
					// startActivity(AgentActivity.intentForFragment(mActivity,
					// AgentActivity.FRAGMENT_ACCOUNT_ORDERS).putExtra(
					// Run.EXTRA_VALUE, 0));
					mPaymentStatus = false;
				}
			}
			resetPaymentStatusView();
			break;

		}
		}
	}

	// 更新收货人信息
	private void updateAddressInfo() {
		boolean isEmpty = (defAddress == null);
		// 没有收货人隐藏
		mAddressView.findViewById(R.id.my_address_book_item_name)
				.setVisibility(isEmpty ? View.INVISIBLE : View.VISIBLE);
		mAddressView.findViewById(R.id.my_address_book_item_phone).setVisibility(
				isEmpty ? View.INVISIBLE : View.VISIBLE);
		mAddressView.findViewById(R.id.my_address_book_item_address).setVisibility(
				isEmpty ? View.INVISIBLE : View.VISIBLE);
		// 没有收货人显示
		mAddressView.findViewById(R.id.my_address_book_item_emptyview).setVisibility(
				isEmpty ? View.VISIBLE : View.INVISIBLE);
		mAddressView.findViewById(R.id.my_address_book_item_arrow).setVisibility(View.VISIBLE);

		if (defAddress != null) {
			((TextView) mAddressView.findViewById(R.id.my_address_book_item_address)).setText(Run.buildString(
					defAddress.optString("txt_area"), defAddress.optString("addr")));
			((TextView) mAddressView.findViewById(R.id.my_address_book_item_phone)).setText(defAddress
					.optString("mobile"));
			((TextView) mAddressView.findViewById(R.id.my_address_book_item_name)).setText("收货人："
					+ defAddress.optString("name"));
		}
	}

	// 更新快递信息
	private void updateExpressInfo() {
		if (expressInfo != null) {
			((TextView) mExpressView.findViewById(R.id.confirm_order_express_message)).setText(Html
					.fromHtml(expressInfo.optString("dt_name") + "\n" + expressInfo.optString("detail")));
		} else {
			((TextView) mExpressView.findViewById(R.id.confirm_order_express_message)).setText(Run.EMPTY_STR);
		}

	}

	@Override
	public void onClick(View v) {
		if (v == mAddressView) {
			String def = "";
			if (defAddress != null) {
				def = defAddress.toString();
			}
			startActivityForResult(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ADDRESS_BOOK)
					.putExtra(Run.EXTRA_VALUE, true).putExtra("old_address", def), REQUEST_ADDRESS);
		} else if (v == mInvoiceView) {
			final String invoiceStr = (mInvoiceInfo != null) ? mInvoiceInfo.toString() : null;
			startActivityForResult(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_INVOICE_EDITOR)
					.putExtra(Run.EXTRA_DATA, invoiceStr), REQUEST_INVOICE);
		} else if (v == mPayView) {
			String data = (expressInfo != null) ? expressInfo.toString() : null;
			startActivityForResult(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_PAYMENT_PICKER)
					.putExtra(Run.EXTRA_DATA, data), REQUEST_PAYMENT);
		} else if (v == mExpressView) {
			if (defAddress == null) {
				checkCanPayment();
				return;
			}

			String area = Run.parseAddressId(defAddress);
			if (TextUtils.isEmpty(area))
				return;

			// 用户自选快递
			Run.excuteJsonTask(new JsonTask(), new GetExpressTask(area, isFastBuy, false));
			// startActivityForResult(AgentActivity.intentForFragment(mActivity,
			// AgentActivity.FRAGMENT_PICK_EXPRESS), REQUEST_EXPRESS);
		} else if (v == mTicketView || v.getId() == R.id.confirm_order_ticket) {
			// String mCouponStr = "";
			// if (mCouponLists != null) {
			// mCouponStr = mCouponLists.toString();
			// }
			// String carInfo = ((TextView) mTicketView
			// .findViewById(R.id.confirm_order_ticket_name)).getText()
			// .toString();

			Intent intent = AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_TICKET_LIST)
					.putExtra(Run.EXTRA_COUPON_DATA, oldCoupunNum + "&" + isFastBuy).putExtra(Run.EXTRA_DATA, true);
			// if (!TextUtils.isEmpty(carInfo)) {
			// intent.putExtra(Run.EXTRA_VALUE, carInfo);
			// }
			startActivityForResult(intent, REQUEST_TICKET);
		} else if (v.getId() == R.id.confirm_order_checkout || v.getId() == R.id.confirm_order_submit) {
			if (!isSelectedIDCard) {
				AccountLoginFragment.showAlertDialog(mActivity, "该笔订单是海外直邮订单，需要上传身份证照片用以清关，请前往页面顶部上传哦。", "", "OK",
						null, null, false, null);
				return;
			}
			JsonRequestBean bean = null;
			if ((bean = buildCreateOrderRequestBean()) != null) {
				Run.excuteJsonTask(new JsonTask(), new CreateOrderTask(bean));

			}
		} else if (v.getId() == R.id.confirm_order_pay_state_ok) {
			if (!mPaymentStatus) {
				startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ACCOUNT_ORDERS)
						.putExtra(Run.EXTRA_VALUE, R.id.account_orders_paying));
			} else {
				startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ACCOUNT_ORDERS)
						.putExtra(Run.EXTRA_VALUE, 0));
			}
			mActivity.finish();
		} else if (v.getId() == R.id.confirm_order_promotion_img || v.getId() == R.id.confirm_order_coupon
				|| v.getId() == R.id.confirm_order_express_fees) {
			if (priceInfo.isNull("pmt_order_info"))
				return;

			final CustomDialog dialog = new CustomDialog(mActivity);
			dialog.setTitle("优惠信息");
			dialog.setMessage(Html.fromHtml(priceInfo.optString("pmt_order_info")));
			dialog.setCenterButton(getString(R.string.ok), new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.setCancelable(true).setCanceledOnTouchOutside(true).show();
		} else if (v == mIDCardView) {
			if (defAddress == null) {
				AccountLoginFragment.showAlertDialog(mActivity, "尚未填写收货信息哦，请前往页面顶部填写", "", "OK", null, null, false,
						null);
				return;
			}
			startActivityForResult(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_SELECT_ID)
					.putExtra(Run.EXTRA_DATA, defAddress.optString("addr_id")).putExtra("idcardId", idcardId),
					REQUEST_IDCARD);
		} else {
			super.onClick(v);
		}
	}

	/**
	 * 检查选择时间是否在提交订单时的时间之后
	 * 
	 * @param selectdTime
	 * @return
	 */
	private boolean isOutSelectedDate(String selectdTime) {
		if (!TextUtils.isEmpty(selectdTime) && selectdTime.contains("立即送")) {
			return false;
		}
		SimpleDateFormat sdf = null;
		if (selectdTime.length() > 15) {
			int index = selectdTime.indexOf(" ");
			int indexEnd = selectdTime.lastIndexOf("-");
			String temp = selectdTime.substring(index, indexEnd + 1);
			selectdTime = selectdTime.replaceAll(temp, " ");
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		} else if (selectdTime.length() > 9) {
			sdf = new SimpleDateFormat("yyyy-MM-dd");
		}
		Date date = null;
		try {
			date = sdf.parse(selectdTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date != null && Calendar.getInstance().getTime().after(date)) {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK || data == null)
			return;

		try {
			if (requestCode == REQUEST_ADDRESS) {
				String jsonstr = data.getStringExtra(Run.EXTRA_DATA);
				defAddress = new JSONObject(jsonstr);
				updateAddressInfo();
				if (defAddress != null) {
					Run.excuteJsonTask(new JsonTask(), new GetTotalPriceTask());
				}
			} else if (requestCode == REQUEST_EXPRESS) {
				String jsonstr = data.getStringExtra(Run.EXTRA_DATA);
				boolean isChangedAddr = data.getBooleanExtra(Run.EXTRA_VALUE, false);
				if (isChangedAddr) {
					String jsonstrAddr = data.getStringExtra(Run.EXTRA_ADDR);
					defAddress = new JSONObject(jsonstrAddr);
					updateAddressInfo();
				}
				expressInfo = new JSONObject(jsonstr);
				updateExpressInfo();
				if (defAddress == null || expressInfo == null) {
				} else {
					Run.excuteJsonTask(new JsonTask(), new GetTotalPriceTask());
				}
			} else if (requestCode == REQUEST_INVOICE) {
				String jsonstr = data.getStringExtra(Run.EXTRA_DATA);
				try {
					mInvoiceInfo = new JSONObject(jsonstr);
					((TextView) mInvoiceView.findViewById(R.id.confirm_order_express_message)).setText(mInvoiceInfo
							.optString("dt_name") + "\n" + mInvoiceInfo.optString("detail"));
				} catch (Exception e) {
					mInvoiceInfo = null;
					((TextView) mInvoiceView.findViewById(R.id.confirm_order_express_message))
							.setText(R.string.invoice_type_null);
				}
			} else if (requestCode == REQUEST_PAYMENT) {
				String jsonstr = data.getStringExtra(Run.EXTRA_DATA);
				payInfo = new JSONObject(jsonstr);
				((TextView) mPayView.findViewById(R.id.confirm_order_express_message)).setText(payInfo
						.optString("app_display_name"));
				if (payInfo == null) {
				} else {
					Run.excuteJsonTask(new JsonTask(), new GetTotalPriceTask());
				}
			} else if (requestCode == REQUEST_TICKET) {
				// if (data.getBooleanExtra(Run.EXTRA_DATA, false)) {
				// String carInfo = ((TextView) mTicketView
				// .findViewById(R.id.confirm_order_ticket_name))
				// .getText().toString();
				// carInfo = carInfo.substring(carInfo.indexOf("(") + 1,
				// carInfo.indexOf(")"));
				// new JsonTask()
				// .execute(new RemoveCoupon("coupon_" + carInfo));
				// } else {
				// String ticket = data.getStringExtra(Run.EXTRA_DATA);
				// Run.excuteJsonTask(new JsonTask(), new AddCounponTask(
				// ticket, isFastBuy));
				// }
				JSONObject obj = new JSONObject(data.getStringExtra(Run.EXTRA_DATA));
				dataJson.put("md5_cart_info", obj.optString("md5_cart_info"));

				Run.excuteJsonTask(new JsonTask(), new GetTotalPriceTask());
				if (data.getBooleanExtra(Run.EXTRA_VALUE, false)) {
					((TextView) mBView.findViewById(R.id.confirm_order_status)).setText("兑换");
					oldCoupunNum = "";
				} else {
					JSONObject couponInfo = obj.optJSONArray("coupon_info").getJSONObject(0);

					((TextView) mBView.findViewById(R.id.confirm_order_status)).setText(couponInfo.optString("name"));
					oldCoupunNum = couponInfo.optString("coupon");
				}

			} else if (requestCode == REQUEST_IDCARD) {
				isSelectedIDCard = true;
				idcardId = data.getStringExtra("idcardId");
				((TextView) mAddressView.findViewById(R.id.confirm_order_idcard_tip)).setText("已上传");
			}
		} catch (Exception e) {
		}
	}

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
				convertView = mActivity.getLayoutInflater().inflate(R.layout.fragment_confirm_order_item, null);
				((TextView) convertView.findViewById(R.id.account_orders_item_oldprice)).getPaint().setFlags(
						Paint.STRIKE_THRU_TEXT_FLAG);
				convertView.setOnClickListener(this);
			}
			JSONObject all = getItem(position);
			try {
				convertView.setTag(all);
				JSONObject product = all.optJSONObject("obj_items").optJSONArray("products").getJSONObject(0);
				JSONObject prices = product.optJSONObject("price");
				((TextView) convertView.findViewById(R.id.account_orders_item_title))
						.setText(product.optString("name"));
				// if (!product.isNull("spec_info")) //樱桃社不用现实属性
				// ((TextView) convertView
				// .findViewById(R.id.account_orders_item_summary))
				// .setText(product.optString("spec_info"));
				((TextView) convertView.findViewById(R.id.account_orders_item_price)).setText("￥"
						+ prices.optString("buy_price"));
				((TextView) convertView.findViewById(R.id.account_orders_item_oldprice)).setText(prices
						.optString("price"));
				((TextView) convertView.findViewById(R.id.account_orders_item_quantity)).setText(Run.buildString("x",
						all.optString("quantity")));
				// 缩略图
				// Uri imageUri = Uri.parse(product.optString("thumbnail_url"));
				ImageView thumbView = (ImageView) convertView.findViewById(R.id.account_orders_item_thumb);
				// thumbView.setTag(imageUri);
				// mImageLoader.showImage(thumbView, imageUri);
				mVolleyImageLoader.showImage(thumbView, product.optString("thumbnail_url"));
			} catch (Exception e) {
			}

			return convertView;
		}

		@Override
		public void onClick(View v) {
		}
	}

	// 付款失败，统一处理
	private void resetPaymentStatusView() {
		mListView.setVisibility(View.GONE);
		findViewById(R.id.confirm_order_paystate).setVisibility(View.VISIBLE);
		if (!mPaymentStatus) {
			((Button) findViewById(R.id.confirm_order_pay_state_ok)).setText(R.string.confirm_order_goto_order_detail);
			((TextView) findViewById(R.id.confirm_order_pay_state_text)).setText(R.string.confirm_order_pay_failed);
			((ImageView) findViewById(R.id.confirm_order_pay_state_icon)).setImageResource(R.drawable.pay_failed_face);
		} else {
			if (Run.isOfflinePayType(payInfo))
				((TextView) findViewById(R.id.confirm_order_pay_state_text))
						.setText(R.string.confirm_order_pay_offline);
		}
	}

	/**
	 * 更新价格信息
	 * 
	 * @param all
	 */
	private void fillupPriceLayout(JSONObject all) throws Exception {
		if (Run.checkRequestJson(mActivity, all)) {
			priceInfo = all.optJSONObject("data");
			// ((TextView) findViewById(R.id.confirm_order_totals_price))
			// .setText(Run.buildString("￥",
			// priceInfo.optString("cost_item")));
			// ((TextView) findViewById(R.id.confirm_order_total_price))
			// .setText(Run.buildString("￥",
			// priceInfo.optString("total_amount")));
			// ((TextView) findViewById(R.id.confirm_order_express_fee))
			// .setText(mActivity.getString(
			// R.string.confirm_order_express_fee,
			// expressInfo.optString("money")));
			// if (priceInfo.has("pmt_order_info")
			// && !"null".equals(priceInfo.optString("pmt_order_info"))) {
			// findViewById(R.id.confirm_order_promotion_img).setVisibility(
			// View.VISIBLE);
			// findViewById(R.id.confirm_order_coupon).setOnClickListener(
			// ConfirmOrderFragment.this);
			// findViewById(R.id.confirm_order_express_fee)
			// .setOnClickListener(ConfirmOrderFragment.this);
			// ((TextView)findViewById(R.id.confirm_order_coupon)).setText(
			// String.format("￥%.2f",priceInfo.optDouble("pmt_amount")));
			// }
			// listview
			if (isUseYingBang) {
				int max = payJifen.optInt("max_discount_value");
				double decount = max * payJifen.optDouble("discount_rate");
				if (priceInfo.optDouble("total_amount") >= decount) {
					useYingbang = (int) max;
				} else {
					useYingbang = (int) (priceInfo.optDouble("total_amount") / payJifen.optDouble("discount_rate"));
				}
			}
			((TextView) mBView.findViewById(R.id.confirm_order_bottom_price)).setText(Run.buildString("￥",
					priceInfo.optString("cost_item")));
			((TextView) mBView.findViewById(R.id.confirm_order_bottom_fee)).setText(Run.buildString("￥",
					priceInfo.optString("cost_freight")));
			((TextView) mBView.findViewById(R.id.confirm_order_bottom_youhui)).setText(Run.buildString("-￥",
					priceInfo.optString("pmt_amount")));
			// ((TextView) mBView.findViewById(R.id.confirm_order_bottom_dikou))
			// .setText(Run.buildString("-￥", "0"));
			// ((TextView)
			// mBView.findViewById(R.id.confirm_order_bottom_tprice))
			// .setText(Run.buildString("￥",
			// priceInfo.optString("total_amount")));
			// ((TextView)
			// mBView.findViewById(R.id.confirm_order_bottom_nprice))
			// .setText(Run.buildString("￥",
			// priceInfo.optString("total_amount")));

			((TextView) mBView.findViewById(R.id.confirm_order_bottom_tprice))
					.setText(Run.buildString(
							"￥",
							df.format(priceInfo.optDouble("total_amount") - useYingbang
									* payJifen.optDouble("discount_rate"))));
			((TextView) mBView.findViewById(R.id.confirm_order_bottom_nprice))
					.setText(Run.buildString(
							"￥",
							df.format(priceInfo.optDouble("total_amount") - useYingbang
									* payJifen.optDouble("discount_rate"))));

			((TextView) mBView.findViewById(R.id.confirm_order_bottom_dikou)).setText(Run.buildString("-￥",
					df.format(useYingbang * payJifen.optDouble("discount_rate"))));

			int max = payJifen.optInt("max_discount_value");
			((TextView) mBView.findViewById(R.id.confirm_order_dikou_tip)).setText(Run.buildString("该笔订单可用", max,
					"积分，抵扣", df.format(max * payJifen.optDouble("discount_rate")), "元"));
			// ((TextView) findViewById(R.id.confirm_order_express_fees))
			// .setText(String.format("￥%.2f",
			// expressInfo.optDouble("money")));

		}
	}

	/* 获取最终价格 */
	private class GetTotalPriceTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			if (getProgressDialog() == null || !getProgressDialog().isShowing())
				showCancelableLoadingDialog();

			try {
				JSONObject payment = new JSONObject();
				if (payInfo != null) {
					payment.put("pay_app_id", payInfo.optString("app_id"));
					payment.put("payment_name", payInfo.optString("app_display_name"));
				}
				JSONObject address = new JSONObject();
				if (defAddress != null) {
					address.put("addr_id", defAddress.optString("addr_id"));
					address.put("area", Run.parseAddressId(defAddress));
				}

				JsonRequestBean bean = new JsonRequestBean( "mobileapi.cart.total");
				bean.addParams("isfastbuy", isFastBuy);

				if (!TextUtils.isEmpty(payment.toString()))
					bean.addParams("payment[pay_app_id]", payment.toString());
				if (!TextUtils.isEmpty(address.toString()))
					bean.addParams("address", address.toString());
				if (expressInfo != null)
					bean.addParams("shipping", expressInfo.optString("shipping"));
				return bean;
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				fillupPriceLayout(all);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 构造生成订单的请求
	 * 
	 * @return
	 */
	private JsonRequestBean buildCreateOrderRequestBean() {
		try {
			JSONObject payment = new JSONObject();
			payment.put("pay_app_id", payInfo.optString("app_id"));
			payment.put("app_pay_type", payInfo.optString("app_pay_type"));
			payment.put("payment_name", payInfo.optString("app_display_name"));
			JSONObject address = new JSONObject();
			address.put("addr_id", defAddress.optString("addr_id"));
			address.put("area", Run.parseAddressId(defAddress));
			String branch_id = expressInfo.optString("branch_id");

			JsonRequestBean bean = new JsonRequestBean( "mobileapi.order.create")
					.addParams("isfastbuy", isFastBuy).addParams("md5_cart_info", dataJson.optString("md5_cart_info"))
					.addParams("payment[pay_app_id]", payment.toString()).addParams("address", address.toString())
					.addParams("shipping", expressInfo.optString("shipping"));
			if (!TextUtils.isEmpty(branch_id)) { // 有自提门店就加入门店参数
				bean.addParams("branch_id", branch_id);
			}
			String time = expressInfo.optString("time");
			if (!TextUtils.isEmpty(time)) { // 送货时间
				bean.addParams("r_time", time);
				if (isOutSelectedDate(time)) {
					Run.alertL(mActivity, "请重新选择时间");
					return null;
				}
			}
			if (mInvoiceInfo != null) {
				bean.addParams("payment[is_tax]", "true");
				bean.addParams("payment[tax_type]", mInvoiceInfo.optString("type"));
				bean.addParams("payment[tax_company]", mInvoiceInfo.optString("dt_name"));
				bean.addParams("payment[tax_content]", mInvoiceInfo.optString("content"));
			}
			if (!TextUtils.isEmpty(mMemoText.getText().toString())) {
				bean.addParams("memo", mMemoText.getText().toString());
			}
			if (isUseYingBang) {
				bean.addParams("point", String.valueOf(useYingbang));
			}
			return bean;
		} catch (Exception e) {
			checkCanPayment();
		}
		return null;
	}

	// 检查是否可以支付
	private boolean checkCanPayment() {
		if (defAddress == null)
			Run.alertL(mActivity, R.string.confirm_order_address_empty);
		else if (expressInfo == null)
			Run.alertL(mActivity, R.string.confirm_order_express_empty);
		else if (payInfo == null)
			Run.alertL(mActivity, R.string.confirm_order_payment_empty);
		else
			return true;
		return false;
	}

	// 生成订单接口
	private class CreateOrderTask implements JsonTaskHandler {
		private JsonRequestBean requestBean;

		public CreateOrderTask(JsonRequestBean bean) {
			this.requestBean = bean;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			return requestBean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					String t = data.optString("total_amount");
					if (TextUtils.equals(t, "0.00")) {
						mPaymentStatus = true;
						resetPaymentStatusView();
						return;
					}
					try {
						JSONObject order = data.optJSONArray("order_objects").getJSONObject(0);
						JSONObject payObj = data.optJSONObject("payinfo");
						mNewOrderId = order.optString("order_id");
						JsonRequestBean bean = new JsonRequestBean( "mobileapi.paycenter.dopayment")
								.addParams("payment_order_id", order.optString("order_id"))
								.addParams("payment_cur_money", data.optString("total_amount"))
								.addParams("payment_pay_app_id", payObj.optString("pay_app_id"));
						Run.excuteJsonTask(new JsonTask(), new BalancePayTask(bean));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 订单详情
	private class OrderDetailTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "b2c.order.detail").addParams("tid", mNewOrderId);
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					mPaymentStatus = !TextUtils.equals(data.optString("pay_status"), "PAY_NO");
				}
			} catch (Exception e) {
			} finally {
				resetPaymentStatusView();
			}
		}

	}

	// 支付订单接口
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
				if (!Run.checkRequestJson(mActivity, all)) {
					Run.startThirdPartyPayment(mActivity, all);
				} else {
					JSONObject data = all.optJSONObject("data");
					String payAppId = all.optJSONObject("data").optString("pay_app_id");
					if ("malipay".equals(payAppId)) {
						// 支付宝
						callAliPay(data);
						return;
					} else if ("wxpayjsapi".equals(payAppId)) {
						// 微信
						callWXPay(data);
						if (Run.loadOptionBoolean(mActivity, "WXPayResult", false)) {
							mPaymentStatus = true;
						}
					} else {
						if (all.optJSONObject("data").optString("msg").contains("成功")) {
							mPaymentStatus = true;
						} else {
							mPaymentStatus = false;
						}
						resetPaymentStatusView();
					}
					// resetPaymentStatusView();
				}
			} catch (Exception e) {
				resetPaymentStatusView();
			}
		}
	}

	private class AddCounponTask implements JsonTaskHandler {
		private String counpon, isFastBuy;

		public AddCounponTask(String counpon, String isFastBuy) {
			this.isFastBuy = isFastBuy;
			this.counpon = counpon;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.add_coupon");
			if (!TextUtils.isEmpty(oldCoupunNum)) {
				req.addParams("old_coupon", oldCoupunNum);
			}
			return req.addParams("coupon", this.counpon).addParams("isfastbuy", String.valueOf(this.isFastBuy));
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					oldCoupunNum = counpon;
					JSONObject data = all.optJSONObject("data");
					dataJson.put("md5_cart_info", data.optString("md5_cart_info"));

					Run.excuteJsonTask(new JsonTask(), new GetTotalPriceTask());

					JSONObject couponInfo = data.optJSONArray("coupon_info").getJSONObject(0);
					// View counponTV =
					// findViewById(R.id.confirm_order_ticket_name);
					// ((TextView)
					// counponTV).setText(couponInfo.optString("name")
					// + "\n(" + couponInfo.optString("coupon") + ")");
					((TextView) mBView.findViewById(R.id.confirm_order_status)).setText("已选择");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 隐藏等待框
			hideLoadingDialog_mt();
		}
	}

	// 获取配送方式接口
	private class GetExpressTask implements JsonTaskHandler {
		private String areaId, isFastBuy;
		private boolean isPickDefault;

		public GetExpressTask(String areaId, String isFastBuy, boolean isPickDefault) {
			this.areaId = areaId;
			this.isFastBuy = isFastBuy;
			this.isPickDefault = isPickDefault;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.cart.delivery_change").addParams("area", this.areaId)
					.addParams("isfastbuy", String.valueOf(this.isFastBuy));
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray data = all.optJSONArray("data");

					if (isPickDefault) {
						// 直接获取默认的快递
						expressInfo = data.getJSONObject(0);
						updateExpressInfo();
						return;
					} else {
						// 用户自选快递
						String expressDataStr = (expressInfo != null) ? expressInfo.toString() : Run.EMPTY_STR;
						startActivityForResult(
								AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_PICK_EXPRESS)
										.putExtra(Run.EXTRA_VALUE, data.toString())
										.putExtra(Run.EXTRA_DATA, expressDataStr)
										.putExtra("com.qianseit.westore.AREA_ID", defAddress.optString("addr_id"))
										.putExtra(Run.EXTRA_ADDR, defAddress.toString())
										.putExtra(Run.EXTRA_FROM_EXTRACT, mFromExtract), REQUEST_EXPRESS);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 隐藏等待框
			hideLoadingDialog_mt();
		}
	}

	private class RemoveCoupon implements JsonTaskHandler {

		private String obj_ident;

		public RemoveCoupon(String obj_ident) {
			this.obj_ident = obj_ident;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.excuteJsonTask(new JsonTask(), new SubmitCarTask());
					View counponTV = findViewById(R.id.confirm_order_ticket_name);
					((TextView) counponTV).setText("");
					if (mCouponLists.length() > 0) {
						((TextView) counponTV).setHint(R.string.confirm_order_ticket_available);
					} else {
						((TextView) counponTV).setHint(R.string.confirm_order_ticket_unavailable);
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
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.remove");
			req.addParams("obj_type", "coupon");
			req.addParams("obj_ident", obj_ident);
			return req;
		}
	}

	// 提交订单
	private class SubmitCarTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.cart.checkout").addParams("isfastbuy", isFastBuy);
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all) && !all.isNull("data")) {
					JSONObject data = all.optJSONObject("data");
					String md5 = data.optString("md5_cart_info");
					dataJson.put("md5_cart_info", md5);
					Run.excuteJsonTask(new JsonTask(), new GetTotalPriceTask());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (Exception e) {
			}
		}
	}

	// 获取支付方式接口
	private class GetPayItemsTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						LayoutInflater inf = mActivity.getLayoutInflater();
						for (int i = 0, c = child.length(); i < c; i++) {
							JSONObject item = child.getJSONObject(i);
//							if (item.optString("app_display_name").contains("预存款")) {
//								continue;
//							}
							updatePayView(item, mBView, inf, mPayListener);
						}

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ViewGroup vg = (ViewGroup) findViewById(R.id.confirm_order_pay_items);
				if (vg.getChildCount() > 0)
					vg.getChildAt(0).performClick();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.order.select_payment");
			return req;
		}

	}

	private void updatePayView(JSONObject data, View parent, LayoutInflater inf, OnClickListener listener) {
		String appname = data.optString("app_display_name");
		String appicon = data.optString("icon_src");
		View vPay = inf.inflate(R.layout.fragment_confirm_order_pay_item, null);
		((TextView) vPay.findViewById(R.id.confirm_order_pay_name)).setText(appname);
		// 支付方式图标
		if (!TextUtils.isEmpty(appicon)) {
			ImageView iconView = (ImageView) vPay.findViewById(R.id.confirm_order_pay_icon);
			mVolleyImageLoader.showImage(iconView, appicon, ScaleType.FIT_CENTER);
		}
		// 付款方式不能为null
		String payBrief = data.optString("app_pay_brief");
		if (!TextUtils.isEmpty(payBrief) && !TextUtils.equals(payBrief, "null")) {
			View briefView = vPay.findViewById(R.id.confirm_order_pay_content);
			((TextView) briefView).setText(payBrief);
		}
		((ViewGroup) parent.findViewById(R.id.confirm_order_pay_items)).addView(vPay);
		vPay.setOnClickListener(listener);
		vPay.setTag(data);
	}

	private OnClickListener mPayListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				JSONObject data = (JSONObject) v.getTag();
				ViewGroup items = ((ViewGroup) mBView.findViewById(R.id.confirm_order_pay_items));

				// 选中当前付款方式
				payInfo = data;
				for (int i = 0, c = items.getChildCount(); i < c; i++)
					((CheckBox) items.getChildAt(i).findViewById(R.id.confirm_order_pay_checkbox)).setChecked(false);
				((CheckBox) v.findViewById(R.id.confirm_order_pay_checkbox)).setChecked(true);
			}
		}
	};

	private Handler countDownHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (mRemainTime <= 0) {
				removeMessages(0);
				return;
			} else {

				mRemainTime -= 1;
				((TextView) mBView.findViewById(R.id.confirm_order_bottom_countdown)).setText(Util
						.calculateRemainTime(mRemainTime));
			}
			removeMessages(0);
			sendEmptyMessageDelayed(0, 1000);
		};
	};

	private long mRemainTime = 30 * 60;

}
