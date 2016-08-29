package com.qianseit.westore.activity.account;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class FragmentChargeCard extends BaseDoFragment {

	private EditText mCardNumberEdt;
	private boolean isChargeSucc;
	
	public FragmentChargeCard() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.meitong_card_charge_title);
		rootView = inflater.inflate(R.layout.fragment_charge_card, null);
		findViewById(R.id.charge_card_submit).setOnClickListener(this);
		mCardNumberEdt = (EditText) rootView.findViewById(R.id.charge_card_no);
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.charge_card_submit) {
			if (TextUtils.isEmpty(mCardNumberEdt.getText().toString())) {
				Run.alert(mActivity, "请输入你的美通券号");
				return;
			}
			new JsonTask().execute(new ChargeCardTask(mCardNumberEdt.getText().toString()));
		} else {
			super.onClick(v);
		}
	}
	
	private class ChargeCardTask implements JsonTaskHandler{

		private String CardNum;
		
		public ChargeCardTask(String cardNum){
			this.CardNum = cardNum;
		}
		
		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					isChargeSucc = true;
					Run.alert(mActivity, "充值成功");
				} else {
					Run.alert(mActivity, "充值失败");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean("mobileapi.member.mtk_recharge");
			req.addParams("card_no", CardNum);
			return req;
		}
		
	}
	
	@Override
	public void onDestroyView() {
		if (isChargeSucc) {
			mActivity.setResult(Activity.RESULT_OK);
		}
		super.onDestroyView();
	}
	
}
