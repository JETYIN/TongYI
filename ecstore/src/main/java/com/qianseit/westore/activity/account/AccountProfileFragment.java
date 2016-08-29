package com.qianseit.westore.activity.account;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class AccountProfileFragment extends BaseDoFragment {
	private EditText mNickNameText;
	private EditText mIntroduceText;
	private RadioGroup mSexRadios;
	private Button mSubmitButton;
	private RadioButton maleRadio;

	private LoginedUser mLoginedUser;

	public AccountProfileFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_profile_title);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);

		rootView = inflater.inflate(R.layout.fragment_account_profile, null);
		mNickNameText = (EditText) findViewById(R.id.account_profile_nickname);
		mIntroduceText = (EditText) findViewById(R.id.account_profile_intro);
		mSexRadios = (RadioGroup) findViewById(R.id.account_profile_sex_radios);
		maleRadio = (RadioButton) findViewById(R.id.account_profile_male);
		mSubmitButton = (Button) findViewById(R.id.account_profile_submit_button);
		mSubmitButton.setOnClickListener(this);

		// 登录用户
		if (mLoginedUser.getUserInfo() != null) {
			JSONObject info = mLoginedUser.getUserInfo();
			((TextView) findViewById(R.id.account_profile_intro)).setText(info
					.optString("info"));
			String sex = info.optString("sex");
			if (TextUtils.equals(sex, "0"))
				mSexRadios.check(R.id.account_profile_female);
		}

		// 昵称已存在不可修改
		if (!TextUtils.isEmpty(mLoginedUser.getRealNickName(mActivity))) {
			mNickNameText.setText(mLoginedUser.getRealNickName(mActivity));
			mNickNameText.setInputType(InputType.TYPE_NULL);
		} else {
			mNickNameText.setInputType(InputType.TYPE_CLASS_TEXT);
		}
	}

	@Override
	public void onClick(View v) {
		if (mSubmitButton == v) {
			resetAccountProfile();
		} else {
			super.onClick(v);
		}
	}

	private void resetAccountProfile() {
		if (TextUtils.isEmpty(mIntroduceText.getText())) {
			mIntroduceText.requestFocus();
			return;
		}

		Run.excuteJsonTask(new JsonTask(), new ResetProfileTask());
	}

	private class ResetProfileTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			String gender = maleRadio.isChecked() ? "male" : "female";
			return new JsonRequestBean(
					"mobileapi.member.save_setting")
					.addParams("gender", gender)
					.addParams("info", mIntroduceText.getText().toString())
					.addParams("contact[name]",
							mNickNameText.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();

					JSONObject info = mLoginedUser.getUserInfo();
					info.put("info", mIntroduceText.getText().toString());
					info.put("sex", maleRadio.isChecked() ? "1" : "0");
					info.put("name", mNickNameText.getText().toString());
				}
			} catch (Exception e) {
			}
		}
	}
}
