package com.qianseit.westore.activity.account;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import cn.shopex.ecstore.R;

public class AccountInfoFragment extends BaseDoFragment {
	private EditText mPhoneText;
	private EditText mEmailText;

	private LoginedUser mLoginedUser;

	public AccountInfoFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_info_title);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);

		rootView = inflater.inflate(R.layout.fragment_account_info, null);
		mPhoneText = (EditText) findViewById(R.id.account_info_phone);
		mEmailText = (EditText) findViewById(R.id.account_info_email);
		findViewById(R.id.account_info_email_item).setOnClickListener(this);
		findViewById(R.id.account_info_phone_item).setOnClickListener(this);
		findViewById(R.id.account_info_passwd_item).setOnClickListener(this);

		// 登录用户
		if (mLoginedUser.getUserInfo() != null) {
			String phone = mLoginedUser.getPhone();
			if (!TextUtils.isEmpty(phone)) {
				String replaceStr = phone.substring(3, 7);
				mPhoneText.setText(phone.replace(replaceStr, "****"));
			}

			String email = mLoginedUser.getEmail();
			if (!TextUtils.isEmpty(email)) {
				int subStart = email.indexOf("@");
				String replaceStr = email.substring(0, subStart);
				replaceStr = Run.makeSecretString(replaceStr, 4);
				mEmailText.setText(Run.buildString(replaceStr,
						email.substring(subStart)));
			}
		} else {
			mActivity.finish();
		}
	}

	@Override
	public void onClick(View v) {
		if (R.id.account_info_email_item == v.getId()) {
		} else if (R.id.account_info_phone_item == v.getId()) {
		} else if (R.id.account_info_passwd_item == v.getId()) {
		} else {
			super.onClick(v);
		}
	}

}
