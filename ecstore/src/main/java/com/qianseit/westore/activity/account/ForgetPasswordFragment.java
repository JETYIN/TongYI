package com.qianseit.westore.activity.account;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class ForgetPasswordFragment extends BaseDoFragment {
	private long countdown_time = 120;

	private EditText mUserNameTV;
	private EditText mVerifyCodeTV;
	private EditText mNewPasswdTV;
	private Button mSubmitButton;
	private Button mGetVeryfyBtn;
	private CheckBox mVisiblePasswordBox;

	private String send_type = "", mobile = "";

	public ForgetPasswordFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_reset_password);
	}

	@Override
	public void init(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_account_forget_password,
				null);

		mUserNameTV = (EditText) findViewById(R.id.account_forget_password_phone);
		mVerifyCodeTV = (EditText) findViewById(R.id.account_forget_password_verify_code);
		mNewPasswdTV = (EditText) findViewById(R.id.account_forget_password_new);
		mSubmitButton = (Button) findViewById(R.id.account_reset_submit);
		mGetVeryfyBtn = (Button) findViewById(R.id.account_reset_get_verify_code_button);
		mSubmitButton.setOnClickListener(this);
		mGetVeryfyBtn.setOnClickListener(this);
		mGetVeryfyBtn.setBackgroundResource(R.drawable.bg_verify_code_red);
		mGetVeryfyBtn.setTextColor(Color.WHITE);
		mVisiblePasswordBox = (CheckBox) findViewById(R.id.account_reset_password_visible);
		mVisiblePasswordBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						TransformationMethod method = isChecked ? SingleLineTransformationMethod
								.getInstance() : PasswordTransformationMethod
								.getInstance();
						mNewPasswdTV.setTransformationMethod(method);
						mNewPasswdTV.setSelection(mNewPasswdTV.getText()
								.length());
						mNewPasswdTV.postInvalidate();
					}
				});
	};

	@Override
	public void onClick(View v) {
		if (v == mGetVeryfyBtn) {
			String phone = mUserNameTV.getText().toString();
			if (TextUtils.isEmpty(phone) || !Run.isChinesePhoneNumber(phone)) {
				AccountLoginFragment.showAlertDialog(mActivity, "请输入11位手机号码","","OK",null,null,false,null);
				mUserNameTV.requestFocus();
			} else {
				Run.excuteJsonTask(new JsonTask(), new GetVerifyCodeTask());
			}
		} else if (v == mSubmitButton) {
			if (TextUtils.isEmpty(mUserNameTV.getText())) {
				mUserNameTV.requestFocus();
				AccountLoginFragment.showAlertDialog(mActivity, "请输入11位手机号码","","OK",null,null,false,null);
				return;
			}
			if (TextUtils.isEmpty(mVerifyCodeTV.getText())) {
				mVerifyCodeTV.requestFocus();
				AccountLoginFragment.showAlertDialog(mActivity, "请输入验证码","","OK",null,null,false,null);
				return;
			}
			if (TextUtils.isEmpty(mNewPasswdTV.getText())) {
				mNewPasswdTV.requestFocus();
				AccountLoginFragment.showAlertDialog(mActivity, "请输入重置密码","","OK",null,null,false,null);
				return;
			}
			Run.excuteJsonTask(new JsonTask(), new CheckVerifyCodeTask());
		} else {
			super.onClick(v);
		}
	}

	// 注册用户
	private void AccountResetPasswd() {
		Run.excuteJsonTask(new JsonTask(), new ResetPasswdTask());
	}

	// 获取验证码
	private class GetVerifyCodeTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.passport.sendPSW").addParams("username",
					mUserNameTV.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					send_type = data.optString("send_type");
					mobile = data.optString(send_type);
					Run.excuteJsonTask(new JsonTask(), new SendVerifyCodeTask());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (Exception e) {
				hideLoadingDialog_mt();
			}
		}
	}

	// 检查验证码
	private class CheckVerifyCodeTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.passport.resetpwd_code")
					.addParams("send_type", send_type)
					.addParams("username", mobile)
					.addParams("vcode", mVerifyCodeTV.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all, false)) {
//					mHandler.removeMessages(0);
//					mSubmitButton.setEnabled(true);
//					mSubmitButton.setText(R.string.submit);
//					mActionBar.setShowRightButton(false);
					AccountResetPasswd();
				} else {
					AccountLoginFragment.showAlertDialog(mActivity, all.optString("data"),"","OK",null,null,false,null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class ResetPasswdTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.passport.resetpassword").addParams("account",
					mUserNameTV.getText().toString()).addParams(
					"login_password", mNewPasswdTV.getText().toString()).addParams(
					"psw_confirm", mNewPasswdTV.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all , false)) {
					
					AccountLoginFragment.showAlertDialog(mActivity, "重置密码成功","","OK",null,new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mActivity.setResult(Activity.RESULT_OK);
							mActivity.finish();
						}
					},false,null);
				} else {
					AccountLoginFragment.showAlertDialog(mActivity, all.optString("data"),"","OK",null,null,false,null);
				}
			} catch (Exception e) {
			}
		}
	}

	private class SendVerifyCodeTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			boolean isMobileType = TextUtils.equals("mobile", send_type);
			JsonRequestBean bean = new JsonRequestBean(
					isMobileType ? "mobileapi.passport.send_vcode_sms"
							: "mobileapi.passport.send_vcode_email");
			bean.addParams("uname", mobile);
			bean.addParams("type", "forgot");
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					// 用户提示语
//					int messageId = R.string.account_forget_password_step2_message;
					// 倒计时
					countdown_time = 60;
					mHandler.sendEmptyMessage(0);
//					mActionBar.setRightTitleButton(R.string.next_step,
//							new OnClickListener() {
//								@Override
//								public void onClick(View v) {
//									if (!TextUtils.isEmpty(mVerifyCodeTV
//											.getText().toString()))
//										Run.excuteJsonTask(new JsonTask(),
//												new CheckVerifyCodeTask());
//								}
//							});

					mGetVeryfyBtn.setEnabled(false);
				}
			} catch (Exception e) {
//				Run.alert(mActivity, "验证码下发失败！");
				AccountLoginFragment.showAlertDialog(mActivity, "验证码下发失败！","","OK",null,null,false,null);
			}
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 倒计时结束
			if (--countdown_time <= 0) {
				mGetVeryfyBtn.setEnabled(true);
				mGetVeryfyBtn.setText(R.string.account_regist_get_verify_code);
				mGetVeryfyBtn.setBackgroundResource(R.drawable.bg_verify_code_red);
				mGetVeryfyBtn.setTextColor(Color.WHITE);
				return;
			} else {
				mGetVeryfyBtn.setBackgroundResource(R.drawable.bg_verify_code);
				mGetVeryfyBtn.setTextColor(mActivity.getResources().getColor(R.color.default_page_bgcolor_3));
			}

			mGetVeryfyBtn.setText(mActivity.getString(
					R.string.account_forget_password_step2_countdown,
					countdown_time));
			sendEmptyMessageDelayed(0, 1000);
		};
	};
}
