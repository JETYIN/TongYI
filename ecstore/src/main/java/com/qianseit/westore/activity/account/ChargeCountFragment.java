package com.qianseit.westore.activity.account;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class ChargeCountFragment extends BaseDoFragment {

	private String mPayId;
	private EditText mEditText;
	private Button mSubmitBtn;
	
	public ChargeCountFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		if (b != null) {
			mPayId = b.getString(Run.EXTRA_DATA);
			mActionBar.setTitle(b.getString(Run.EXTRA_TITLE));
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.view_charge_count, null);
		mEditText = (EditText) rootView.findViewById(R.id.charge_count);
		mSubmitBtn = (Button) rootView.findViewById(R.id.charge_submit);
		mSubmitBtn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (mSubmitBtn == v) {
			if (!TextUtils.isEmpty(mEditText.getText().toString()))
					new JsonTask().execute(new ChargeTask());
		}
	}
	
	private class ChargeTask implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (!Run.checkRequestJson(mActivity, all)) {
					Run.startThirdPartyPayment(mActivity, all);
				} else {
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.paycenter.dopayment");
			req.addParams("pay_object", "recharge");
			req.addParams("payment_pay_app_id", mPayId);
			req.addParams("payment_cur_money", mEditText.getText().toString());
			return req;
		}
		
	}
	
}
