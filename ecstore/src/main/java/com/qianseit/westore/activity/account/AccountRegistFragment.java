package com.qianseit.westore.activity.account;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.text.method.TransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleAnimListener;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class AccountRegistFragment extends BaseDoFragment {
	private Button mGetVerifyCodeButton, mSubmitButton, mNextButton;
	private EditText mPhoneNumberText, mVerifyCodeText, mPasswdText,mInvCodeText;

//	private String mVerifyCode;
//	private SmsReceiver mSmsReceiver;
	private CheckBox mVisiblePasswordBox;
	private TextView mRegisterXieYi;

	public AccountRegistFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_login_regist);

//		mSmsReceiver = new SmsReceiver();
//		IntentFilter filter = new IntentFilter(Run.ACTION_SMS_RECEIVED);
//		filter.addAction(Run.ACTION_SMS_DELIVER);
//		mActivity.registerReceiver(mSmsReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		Run.unregistReceiverSafety(mActivity, mSmsReceiver);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_account_regist, null);
		mInvCodeText = (EditText) findViewById(R.id.account_regist_inv_code);
		mGetVerifyCodeButton = (Button) findViewById(R.id.account_regist_get_verify_code_button);
		mSubmitButton = (Button) findViewById(R.id.account_regist_submit_button);
		mNextButton = (Button) findViewById(R.id.account_regist_next_button);
		mPhoneNumberText = (EditText) findViewById(R.id.account_regist_username);
		mVerifyCodeText = (EditText) findViewById(R.id.account_regist_verify_code);
		mPasswdText = (EditText) findViewById(R.id.account_regist_passwd);
		mGetVerifyCodeButton.setOnClickListener(this);
		mSubmitButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		mRegisterXieYi = (TextView) rootView.findViewById(R.id.account_regist_xieyi);
		mRegisterXieYi.setText(getXieyiString());
		mRegisterXieYi.setMovementMethod(LinkMovementMethod.getInstance());
		mRegisterXieYi.setHighlightColor(getResources().getColor(android.R.color.transparent));//方法重新设置文字背景为透明色。
		mVisiblePasswordBox = (CheckBox) rootView.findViewById(R.id.account_register_password_visible);
		mVisiblePasswordBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				TransformationMethod method = isChecked ? SingleLineTransformationMethod
						.getInstance() : PasswordTransformationMethod
						.getInstance();
						mPasswdText.setTransformationMethod(method);
						mPasswdText.setSelection(mPasswdText.getText().length());
						mPasswdText.postInvalidate();
			}
		});

		enableVreifyCodeButton();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v == mSubmitButton) {
			registAccount();
		} else if (v == mNextButton) {
			registAccount();
			// showSecondaryStepView();
		} else if (v == mGetVerifyCodeButton) {
			String phone = mPhoneNumberText.getText().toString();
			if (TextUtils.isEmpty(phone) || !Run.isChinesePhoneNumber(phone)) {
//				Run.alert(mActivity,
//						R.string.account_regist_phone_number_invalid);
				mPhoneNumberText.requestFocus();
				AccountLoginFragment.showAlertDialog(mActivity,"请输入11位手机号码","","OK",null,null,false,null);
			} else {
				Run.excuteJsonTask(new JsonTask(), new GetVerifyCodeTask());
			}
		} else {
			super.onClick(v);
		}
	}
	
	private SpannableString getXieyiString(){
		final String ss = "商派网络服务协议";
		String xieyi = "*注册表示同意商派网络服务协议";
		SpannableString spannableString = new SpannableString(xieyi);
		ClickableSpan clickableSpan = new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
//				AccountLoginFragment.showAlertDialog(mActivity, ss ,"","OK",null,null,false,null);
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", "商派网络服务协议")
						.putExtra("article_id", "60"));
			}
		};
		spannableString.setSpan(clickableSpan, xieyi.indexOf(ss), xieyi.indexOf(ss) + ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		UnderlineSpan spanline = new UnderlineSpan();  
		spannableString.setSpan(spanline, xieyi.indexOf(ss), xieyi.indexOf(ss) + ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ForegroundColorSpan span = new ForegroundColorSpan(getResources().getColor(R.color.theme_color)); 
		spannableString.setSpan(span, xieyi.indexOf(ss), xieyi.indexOf(ss) + ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}

	private void showSecondaryStepView() {
		findViewById(R.id.account_regist_step2).setVisibility(View.VISIBLE);
		Animation animLeft = AnimationUtils.loadAnimation(mActivity,
				R.anim.push_left_out);
		Animation animRight = AnimationUtils.loadAnimation(mActivity,
				R.anim.push_right_in);
		animLeft.setAnimationListener(new SimpleAnimListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				findViewById(R.id.account_regist_step1).setVisibility(
						View.INVISIBLE);
			}
		});
		findViewById(R.id.account_regist_step1).startAnimation(animLeft);
		findViewById(R.id.account_regist_step2).startAnimation(animRight);
	}

	// 设置验证码按钮状态，倒计时60秒
	private void enableVreifyCodeButton() {
		long remainTime = System.currentTimeMillis() - Run.countdown_time;
		remainTime = 60 - remainTime / 1000;
		if (remainTime <= 0) {
			mGetVerifyCodeButton.setEnabled(true);
			mGetVerifyCodeButton
					.setText(R.string.account_regist_get_verify_code);
			mGetVerifyCodeButton.setBackgroundResource(R.drawable.bg_verify_code_red);
			mGetVerifyCodeButton.setTextColor(Color.WHITE);
			return;
		}else{
			mGetVerifyCodeButton.setBackgroundResource(R.drawable.bg_verify_code);
			mGetVerifyCodeButton.setTextColor(mActivity.getResources().getColor(R.color.default_page_bgcolor_3));
		}

		mGetVerifyCodeButton.setEnabled(false);
		mGetVerifyCodeButton.setText(mActivity.getString(
				R.string.account_regist_verify_code_countdown, remainTime));
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				enableVreifyCodeButton();
			}
		}, 1000);
	}

	// 注册用户
	private void registAccount() {
		String invCode = mInvCodeText.getText().toString();
		String phoneNumber = mPhoneNumberText.getText().toString();
		String password = mPasswdText.getText().toString();
		String verifyCode = mVerifyCodeText.getText().toString();
		if (TextUtils.isEmpty(phoneNumber)
				|| !Run.isChinesePhoneNumber(phoneNumber)) {
			Run.alert(mActivity, R.string.account_regist_phone_number_invalid);
			mPhoneNumberText.requestFocus();
		} else if (TextUtils.isEmpty(verifyCode)
		/* || !TextUtils.equals(verifyCode, mVerifyCode) */) {
			Run.alert(mActivity, R.string.account_regist_verify_code_error);
		} else if (TextUtils.isEmpty(invCode)
		/* || !TextUtils.equals(verifyCode, mVerifyCode) */) {
			Run.alert(mActivity, R.string.account_regist_inv_code_error);
		} else if (TextUtils.isEmpty(password) || password.length() < 6
				|| password.length() > 20) {
			Run.alert(mActivity, R.string.account_regist_password_error);
			mPasswdText.requestFocus();
		} else {
			Run.excuteJsonTask(new JsonTask(), new RegistTask());
		}
	}

	private class RegistTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.passportcp.member_create");
			bean.addParams("uname", mPhoneNumberText.getText().toString());
			bean.addParams("login_name", mPhoneNumberText.getText().toString());
			bean.addParams("vcode", mVerifyCodeText.getText().toString());
			bean.addParams("invitation_code", mInvCodeText.getText().toString());
			bean.addParams("password", mPasswdText.getText().toString());
			bean.addParams("source_app", mActivity.getString(R.string.app_channel_name));//来源
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all ,false)) {
					Run.alert(mActivity, R.string.account_regist_success);
					// 保存用户名，密码
					Run.savePrefs(mActivity, Run.pk_logined_username,
							mPhoneNumberText.getText().toString());
					Run.savePrefs(mActivity, Run.pk_logined_user_password,
							mPasswdText.getText().toString());

					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();
				} else {
					AccountLoginFragment.showAlertDialog(mActivity, all.optString("data"),"","OK",null,null,false,null);
				}
			} catch (Exception e) {
			}
		}
	}

	private class GetVerifyCodeTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.passport.send_vcode_sms");
			bean.addParams("uname", mPhoneNumberText.getText().toString());
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
//					mVerifyCode = all.optJSONObject("data").optString("vcode");
					mPhoneNumberText.setEnabled(false);

					// 倒计时60秒
					Run.countdown_time = System.currentTimeMillis();
					enableVreifyCodeButton();
				}
			} catch (Exception e) {
				Run.alert(mActivity, "验证码下发失败！");
			}
		}
	}

//	public class SmsReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.getAction().equals(Run.ACTION_SMS_RECEIVED)
//					|| intent.getAction().equals(Run.ACTION_SMS_DELIVER)) {
//				String msgText = Run.handleSmsReceived(intent);
//				if (msgText.contains("验证码") && msgText.length() > 13) {
//					mVerifyCodeText.setText(msgText.subSequence(7, 13));
//				}
//			}
//		}
//	}
}
