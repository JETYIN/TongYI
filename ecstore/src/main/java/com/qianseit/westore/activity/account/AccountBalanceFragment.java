package com.qianseit.westore.activity.account;

import java.text.DecimalFormat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonTask;
import cn.shopex.ecstore.R;

public class AccountBalanceFragment extends BaseDoFragment {

	public final int CHARGECARD_REQUEST_CODE = 0x1000;
	
	public AccountBalanceFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.balance);
		rootView = inflater.inflate(R.layout.fragment_account_balance, null);
		findViewById(R.id.account_balance_charge).setOnClickListener(this);
		findViewById(R.id.account_balance_online_charge).setOnClickListener(this);
		findViewById(R.id.account_balance_withdraw).setOnClickListener(this);

		Run.excuteJsonTask(new JsonTask(), new LoadCheckoutHistoryTask(0));
	}

	@Override
	public void onCheckoutHistoryLoaded(String json_str) {
		hideLoadingDialog_mt();
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all)) {
				JSONObject data = all.optJSONObject("data");
				String balance = data.optString("total");
				Double d = Double.parseDouble(balance);
				DecimalFormat df = new DecimalFormat("0.00");
				((TextView) findViewById(R.id.account_balance_money))
						.setText(df.format(d));
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.account_balance_charge) {
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_BALANCE_CHARGE),CHARGECARD_REQUEST_CODE);
		} else if(id == R.id.account_balance_online_charge ){
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_CAHARGE_METHODS));
		} else if (id == R.id.account_balance_withdraw){
			
		} else {
			super.onClick(v);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHARGECARD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Run.excuteJsonTask(new JsonTask(), new LoadCheckoutHistoryTask(0));
		}
	}
}
