package com.qianseit.westore.activity.account;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class AccountResetPasswdFragment extends BaseDoFragment {
	private EditText mOldPasswdText;
	private EditText mNewPasswdText;
	// private EditText mRenewPasswdText;
	private CheckBox mVisibilityBox;
	private Button mSubmitButton;

	public AccountResetPasswdFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_reset_passwd);

		rootView = inflater.inflate(R.layout.fragment_account_reset_passwd,
				null);
		mOldPasswdText = (EditText) findViewById(R.id.account_reset_passwd_oldpass);
		mNewPasswdText = (EditText) findViewById(R.id.account_reset_passwd_newpass);
		mVisibilityBox = (CheckBox) findViewById(R.id.account_reset_passwd_visibility);
		mVisibilityBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						TransformationMethod method = isChecked ? SingleLineTransformationMethod
								.getInstance() : PasswordTransformationMethod
								.getInstance();
						mOldPasswdText.setTransformationMethod(method);
						mNewPasswdText.setTransformationMethod(method);
						mOldPasswdText.postInvalidate();
						mNewPasswdText.postInvalidate();
					}
				});
		// mRenewPasswdText = (EditText)
		// findViewById(R.id.account_reset_passwd_renewpass);
		mSubmitButton = (Button) findViewById(R.id.account_reset_passwd_submit_button);
		mSubmitButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mSubmitButton == v) {
			AccountResetPasswd();
		} else {
			super.onClick(v);
		}
	}

	// 注册用户
	private void AccountResetPasswd() {
		if (TextUtils.isEmpty(mOldPasswdText.getText())) {
			mOldPasswdText.requestFocus();
			return;
		}

		if (TextUtils.isEmpty(mNewPasswdText.getText())) {
			mNewPasswdText.requestFocus();
			return;
		}

		// if (TextUtils.isEmpty(mRenewPasswdText.getText())) {
		// mRenewPasswdText.requestFocus();
		// return;
		// }

		// if (!TextUtils.equals(mNewPasswdText.getText(),
		// mRenewPasswdText.getText())) {
		// Run.alert(mActivity, R.string.account_reset_passwd_confirm_failed);
		// return;
		// }

		Run.excuteJsonTask(new JsonTask(), new ResetPasswdTask());
	}

	private class ResetPasswdTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.save_security")
					.addParams("old_passwd",
							mOldPasswdText.getText().toString())
					.addParams("passwd", mNewPasswdText.getText().toString())
					.addParams("passwd_re", mNewPasswdText.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();
				}
			} catch (Exception e) {
			}
		}
	}
}
