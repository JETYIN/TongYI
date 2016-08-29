package com.qianseit.westore.activity;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;
import com.android.volley.toolbox.JsonObjectRequest;

public class PaymentPickerFragment extends BaseDoFragment {
	private RadioGroup mRadioGroup;

	private JSONObject mPaymentJson;
	private JSONObject expressInfo;

	// private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;

	public PaymentPickerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		// mImageLoader = Run.getDefaultImageLoader(mActivity,
		// mActivity.getResources());
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.confirm_order_paytype);

		rootView = inflater.inflate(R.layout.fragment_payment_picker, null);
		findViewById(R.id.payment_picker_submit).setOnClickListener(this);

		mRadioGroup = (RadioGroup) findViewById(R.id.payment_picker_radios);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// RadioButton checkedButton = (RadioButton)
				// findViewById(checkedId);
				// try {
				// mPaymentJson.put("detail", checkedButton.getText()
				// .toString());
				// } catch (Exception e) {
				// }
			}
		});

		// 配送信息
		Intent data = mActivity.getIntent();
		try {
			expressInfo = new JSONObject(data.getStringExtra(Run.EXTRA_DATA));
		} catch (Exception e) {
		} finally {
			// if (expressInfo == null) {
			// mActivity.finish();
			// return;
			// }
		}

		mRadioGroup.check(R.id.express_picker_delvery);
		Run.excuteJsonTask(new JsonTask(), new GetPayItemsTask("0.00"));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.payment_picker_submit) {
			Intent data = new Intent();
			data.putExtra(Run.EXTRA_DATA, mPaymentJson.toString());
			mActivity.setResult(Activity.RESULT_OK, data);
			mActivity.finish();
		} else {
			super.onClick(v);
		}
	}

	/**
	 * 更新支付方式到UI
	 * 
	 * @param data
	 * @param inf
	 */
	public static void updatePayView(JSONObject data, LayoutInflater inf,
			View parent, String balance, OnClickListener listener,
			VolleyImageLoader imageLoader) {
		String appname = data.optString("app_display_name");
		String appid = data.optString("app_id");
		String appicon = data.optString("icon_src");
		boolean isDeposit = TextUtils.equals(appid, "deposit");
		View vPay = inf.inflate(R.layout.fragment_confirm_order_pay_item, null);
		((TextView) vPay.findViewById(R.id.confirm_order_pay_name))
				.setText(appname);
		// .setText(appname + (isDeposit ? "(￥" + balance + ")" : ""));
		// 支付方式图标
		if (!TextUtils.isEmpty(appicon)) {
			// Uri iconUri = Uri.parse(appicon);
			ImageView iconView = (ImageView) vPay
					.findViewById(R.id.confirm_order_pay_icon);
			// iconView.setTag(iconUri);
			// imageLoader.showImage(iconView, iconUri);
			imageLoader.showImage(iconView, appicon);
		}
		// 付款方式不能为null
		String payBrief = data.optString("app_pay_brief");
		if (!TextUtils.isEmpty(payBrief) && !TextUtils.equals(payBrief, "null")) {
			View briefView = vPay.findViewById(R.id.confirm_order_pay_content);
			((TextView) briefView).setText(payBrief);
		}
		((ViewGroup) parent.findViewById(R.id.confirm_order_pay_items))
				.addView(vPay);
		vPay.setOnClickListener(listener);
		vPay.setTag(data);
	}

	private OnClickListener mPayListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				JSONObject data = (JSONObject) v.getTag();
				ViewGroup items = ((ViewGroup) findViewById(R.id.confirm_order_pay_items));

				// 选中当前付款方式
				mPaymentJson = data;
				for (int i = 0, c = items.getChildCount(); i < c; i++)
					((CheckBox) items.getChildAt(i).findViewById(
							R.id.confirm_order_pay_checkbox)).setChecked(false);
				((CheckBox) v.findViewById(R.id.confirm_order_pay_checkbox))
						.setChecked(true);
			}
		}
	};

	/* 获取支付方式 */
	private class GetPayItemsTask implements JsonTaskHandler {
		private String balance = "0.00";

		public GetPayItemsTask(String money) {
			this.balance = money;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.cart.payment_change");
			if (expressInfo != null)
				bean.addParams("shipping", expressInfo.optString("shipping"));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						LayoutInflater inf = mActivity.getLayoutInflater();
						for (int i = 0, c = child.length(); i < c; i++) {
							JSONObject item = child.getJSONObject(i);
							if (!"wxpayjsapi".equals(item.opt("app_rpc_id"))) {
								updatePayView(item, inf, rootView, balance,
										mPayListener, mVolleyImageLoader);
							}
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
	}
}
