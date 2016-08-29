package com.qianseit.westore.activity.account;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountLoginFragment extends BaseDoFragment {
	
	public final static String LOGIN_SINA = "LOGIN_SINA"; 
	public final static String LOGIN_SINA_DATA = "LOGIN_SINA_DATA"; 
	
	private final String PLATFORM_WEIBO = "sina";
	private final String PLATFORM_WECHAT = "weixin";
	private final String PLATFORM_QQ = "qq";

	private EditText mVerifyCodeText;
	private ImageView mVerifyCodeImageView;
	private EditText mUserNameText, mPasswdText;
	private Button mLoginSubmitButton;
	private ImageView mLoginByQQ, mLoginByWechat;
	private ImageView mLoginByAlipay, mLoginByWeibo;
	private CheckBox mVisiblePasswordBox;
	
	private String sinaOpenId;  
	private String sinaToken;  

//	private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;

	public AccountLoginFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mImageLoader = ImageLoader.getInstance(mActivity);
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_login_submit);

		Resources resources = mActivity.getResources();
		rootView = inflater.inflate(R.layout.fragment_account_login, null);
		mUserNameText = (EditText) findViewById(R.id.account_login_username);
		mPasswdText = (EditText) findViewById(R.id.account_login_password);
		mVerifyCodeText = (EditText) findViewById(R.id.account_login_vcode_text);
		mVerifyCodeImageView = (ImageView) findViewById(R.id.account_login_vcode_image);
		mLoginSubmitButton = (Button) findViewById(R.id.account_login_submit_button);
		findViewById(R.id.account_login_forget_passwd).setOnClickListener(this);
		findViewById(R.id.account_login_fast_regist).setOnClickListener(this);
		mLoginSubmitButton.setOnClickListener(this);
		mVerifyCodeImageView.setOnClickListener(this);
		mVisiblePasswordBox = (CheckBox) findViewById(R.id.account_login_password_visible);
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
		autoFillAccountInfo();

		// 第三方登录按钮
		mLoginByQQ = (ImageView) findViewById(R.id.account_login_user_qq);
		mLoginByWechat = (ImageView) findViewById(R.id.account_login_user_wechat);
		mLoginByWeibo = (ImageView) findViewById(R.id.account_login_user_weibo);
		mLoginByAlipay = (ImageView) findViewById(R.id.account_login_user_weibo);
		mLoginByQQ.setOnClickListener(this);
		mLoginByWechat.setOnClickListener(this);
		mLoginByWeibo.setOnClickListener(this);
		mLoginByAlipay.setOnClickListener(this);
		mLoginByQQ.setImageDrawable(Run.getDrawableList(BitmapFactory
				.decodeResource(resources, R.drawable.account_login_use_qq),
				0.5f));
		mLoginByAlipay.setImageDrawable(Run.getDrawableList(
				BitmapFactory.decodeResource(resources,
						R.drawable.account_login_use_alipay), 0.5f));
		mLoginByWechat.setImageDrawable(Run.getDrawableList(
				BitmapFactory.decodeResource(resources,
						R.drawable.account_login_use_wechat), 0.5f));
		mLoginByWeibo.setImageDrawable(Run.getDrawableList(BitmapFactory
				.decodeResource(resources, R.drawable.account_login_use_weibo),
				0.5f));
	}

	@Override
	public void onClick(View v) {
		if (R.id.account_login_fast_regist == v.getId()) {
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ACCOUNT_REGIST),
					REQUEST_CODE_USER_REGIST);
		} else if (R.id.account_login_forget_passwd == v.getId()) {
			mActivity.startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_FORGET_PASSWORD));
		} else if (mLoginSubmitButton == v) {
			AccountLogin();
		} else if (mLoginByQQ == v) {
			Platform platQQ = ShareSDK.getPlatform(mActivity, QQ.NAME);
			platQQ.setPlatformActionListener(new ThirdLoginListener(PLATFORM_QQ));
			platQQ.SSOSetting(false);//设置为false或者不设置这个值，如果设置为 true 则调用客户端
			platQQ.showUser(null);
		} else if (mLoginByWeibo == v) {
			Platform platWB = ShareSDK.getPlatform(mActivity, SinaWeibo.NAME);
			platWB.setPlatformActionListener(new ThirdLoginListener(
					PLATFORM_WEIBO));
			platWB.SSOSetting(false);//设置为false或者不设置这个值，如果设置为 true 则调用客户端
			platWB.showUser(null);
		} else if (mVerifyCodeImageView == v) {
			reloadVcodeImage();
		} else if (mLoginByWechat == v) {
			Platform platWX = ShareSDK.getPlatform(mActivity, Wechat.NAME);
			platWX.setPlatformActionListener(new ThirdLoginListener(
					PLATFORM_WECHAT));
			platWX.SSOSetting(false);
			platWX.showUser(null);
		} else {
			super.onClick(v);
		}
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_USER_REGIST
				&& resultCode == Activity.RESULT_OK) {
			autoFillAccountInfo();
			onClick(mLoginSubmitButton);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 恢复登录用户名和密码
	private void autoFillAccountInfo() {
		if (!TextUtils.isEmpty(Run.loadOptionString(mActivity,
				Run.pk_logined_username, Run.EMPTY_STR)))
			mUserNameText.setText(Run.loadOptionString(mActivity,
					Run.pk_logined_username, Run.EMPTY_STR));
		if (!TextUtils.isEmpty(Run.loadOptionString(mActivity,
				Run.pk_logined_user_password, Run.EMPTY_STR)))
			mPasswdText.setText(Run.loadOptionString(mActivity,
					Run.pk_logined_user_password, Run.EMPTY_STR));
	}

	
	private void AccountLogin() {
		String username = mUserNameText.getText().toString();
		if (TextUtils.isEmpty(username) || !Run.isChinesePhoneNumber(username)) {
			showAlertDialog(mActivity, "请输入11位手机号码", "", "OK", null, null,false,null);
			mUserNameText.requestFocus();
		} else if (TextUtils.isEmpty(mPasswdText.getText().toString())) {
			showAlertDialog(mActivity, "请输入密码", "", "OK", null, null,false,null);
			mPasswdText.requestFocus();
		} else if (mVerifyCodeText.isShown()
				&& TextUtils.isEmpty(mVerifyCodeText.getText().toString())) {
			mVerifyCodeText.requestFocus();
		} else {
			Run.hideSoftInputMethod(mActivity, mUserNameText);
			Run.hideSoftInputMethod(mActivity, mPasswdText);
			Run.hideSoftInputMethod(mActivity, mVerifyCodeText);
			Run.excuteJsonTask(new JsonTask(), new UserLoginTask(
					(DoActivity) mActivity, mUserNameText.getText().toString(),
					mPasswdText.getText().toString(), mVerifyCodeText.getText()
							.toString(), new JsonRequestCallback() {
						@Override
						public void task_response(String jsonStr) {
							userLoginCallback(jsonStr);
						}
					}));
		}
	}

	/**
	 * 用户登录
	 * 
	 * @param json_str
	 */
	private void userLoginCallback(String json_str) {
		((DoActivity) mActivity).hideLoadingDialog_mt();
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all ,false)) {
				userLoginSuccess(all ,false);
			} else {
				JSONObject data = all.optJSONObject("data");
				if (data.optInt("needVcode") == 1) {
					findViewById(R.id.account_login_vcode).setVisibility(
							View.VISIBLE);
					reloadVcodeImage();
				}
				showAlertDialog(mActivity,data.optString("msg"),"","OK",null,null,false,null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Dialog showAlertDialog(Context c ,String message ,String cancelText,String confirmText,
			View.OnClickListener cancelListener, View.OnClickListener okListener , boolean isShowGender , View.OnClickListener genderListener){
		final Dialog dialog = new Dialog(c,R.style.Theme_dialog);
		View view = LayoutInflater.from(c).inflate(R.layout.cunstom_dialog_view, null);
		((TextView) view.findViewById(R.id.dialog_message)).setText(message);
		TextView cancel = (TextView) view.findViewById(R.id.dialog_cancel_btn);
		if (!TextUtils.isEmpty(cancelText)) {
			cancel.setVisibility(View.VISIBLE);
			view.findViewById(R.id.dialog_line).setVisibility(View.VISIBLE);
			cancel.setText(cancelText);
		}
		if (cancelListener != null) {//自定义点击事件
			cancel.setOnClickListener(cancelListener);
		} else {
			cancel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		TextView okBtn = (TextView) view.findViewById(R.id.dialog_conform_btn);
		if (isShowGender) {
			okBtn.setVisibility(View.GONE);
			if (genderListener != null) {
				view.findViewById(R.id.dialog_cancel_gender).setVisibility(View.VISIBLE);
				view.findViewById(R.id.dialog_gender1).setOnClickListener(genderListener);
				view.findViewById(R.id.dialog_gender2).setOnClickListener(genderListener);
				view.findViewById(R.id.dialog_gender3).setOnClickListener(genderListener);
			}
		} else {
			if (!TextUtils.isEmpty(confirmText)) {
				okBtn.setText(confirmText);
			}
			if (okListener != null) {//自定义点击事件
				okBtn.setOnClickListener(okListener);
			} else {
				okBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		}
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return dialog;
	}
	
	public static Dialog showAlertDialog(Context c ,String message ,int cancelText,int confirmText,
			View.OnClickListener cancelListener, View.OnClickListener okListener , boolean isShowGender , View.OnClickListener genderListener){
		final Dialog dialog = new Dialog(c,R.style.Theme_dialog);
		View view = LayoutInflater.from(c).inflate(R.layout.cunstom_dialog_view, null);
		((TextView) view.findViewById(R.id.dialog_message)).setText(message);
		TextView cancel = (TextView) view.findViewById(R.id.dialog_cancel_btn);
		if (-1 != cancelText) {
			cancel.setVisibility(View.VISIBLE);
			cancel.setText(cancelText);
			view.findViewById(R.id.dialog_line).setVisibility(View.VISIBLE);
		}
		if (cancelListener != null) {//自定义点击事件
			cancel.setOnClickListener(cancelListener);
		} else {
			cancel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
		TextView okBtn = (TextView) view.findViewById(R.id.dialog_conform_btn);
		if (isShowGender) {
			okBtn.setVisibility(View.GONE);
			if (genderListener != null) {
				view.findViewById(R.id.dialog_cancel_gender).setVisibility(View.VISIBLE);
				view.findViewById(R.id.dialog_gender1).setOnClickListener(genderListener);
				view.findViewById(R.id.dialog_gender2).setOnClickListener(genderListener);
				view.findViewById(R.id.dialog_gender3).setOnClickListener(genderListener);
			}
		} else {
			if (-1 != confirmText) {
				okBtn.setText(confirmText);
			}
			if (okListener != null) {//自定义点击事件
				okBtn.setOnClickListener(okListener);
			} else {
				okBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		}
		}
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return dialog;
	}

	/**
	 * 用户登录成功
	 * 
	 * @param all
	 */
	private void userLoginSuccess(JSONObject all, boolean isWeboLogin) throws Exception {
		Run.savePrefs(mActivity, LOGIN_SINA, isWeboLogin);
		if (isWeboLogin) {
			Run.savePrefs(mActivity, LOGIN_SINA_DATA, sinaOpenId+"&"+sinaToken);
		}
		LoginedUser user = AgentApplication.getLoginedUser(mActivity);
		user.setIsLogined(true);
		user.setUserInfo(all.getJSONObject("data"));
		// 保存用户名，密码
		Run.savePrefs(mActivity, Run.pk_logined_username, mUserNameText
				.getText().toString());
		Run.savePrefs(mActivity, Run.pk_logined_user_password, mPasswdText
				.getText().toString());

		// 登录成功
		mActivity.setResult(Activity.RESULT_OK);
		mActivity.finish();
	}

	private void reloadVcodeImage() {
		String vcodeUrl = Run.buildString(Run.VCODE_URL,
				System.currentTimeMillis());
//		Uri uri = Uri.parse(vcodeUrl);
//		mVerifyCodeImageView.setTag(uri);
//		mImageLoader.showImage(mVerifyCodeImageView, uri);
		mVolleyImageLoader.showImage(mVerifyCodeImageView, vcodeUrl);
	}

	public static class UserLoginTask implements JsonTaskHandler {
		private String username, passwd, vcode;
		private DoActivity activity;
		private JsonRequestCallback callback;

		public UserLoginTask(DoActivity activity, String username,
				String passwd, String vCode, JsonRequestCallback callback) {
			this.activity = activity;
			this.username = username;
			this.passwd = passwd;
			this.vcode = vCode;
			this.callback = callback;
		}

		@Override
		public JsonRequestBean task_request() {
			if (activity != null)
				activity.showCancelableLoadingDialog();
			JsonRequestBean rb = new JsonRequestBean(
					"mobileapi.passport.post_login").addParams("uname",
					username).addParams("password", passwd);
			if (!TextUtils.isEmpty(vcode))
				rb.addParams("verifycode", vcode);
			return rb;
		}

		@Override
		public void task_response(String json_str) {
			if (activity != null)
				activity.hideLoadingDialog_mt();
			if (callback != null)
				callback.task_response(json_str);
		}
	}

	private class ThirdUserLoginTask implements JsonTaskHandler {
		private String platformName, openid, realname, nickname;
		private String gender, userIcon;
		private String lactionString;

		public ThirdUserLoginTask(String platName, String openid,
				String realname, String nickname) {
			this.platformName = platName;
			this.openid = openid;
			this.realname = realname;
			this.nickname = nickname;
		}
		
		public ThirdUserLoginTask(String platName , Platform arg0 , HashMap<String, Object> arg2){
			this.platformName = platName;
			this.openid = arg0.getDb().getUserId();
			this.realname = arg0.getDb().getUserName();
			this.nickname = arg0.getDb().getUserName();
			this.gender = arg0.getDb().getUserGender() == "m" ? "0" : "1";
			this.userIcon = arg0.getDb().getUserIcon();
			if (platformName == PLATFORM_WEIBO) {
				sinaOpenId = openid;
				lactionString = (String) arg2.get("location");
				sinaToken = arg0.getDb().getToken();
			} else if(platformName == PLATFORM_QQ ){//QQ 微信返回地址
				this.userIcon = (String) arg2.get("figureurl_qq_2");
				lactionString = (String) arg2.get("province") + (String) arg2.get("city");
			} else if(platformName == PLATFORM_WECHAT){
				lactionString = (String) arg2.get("province") + (String) arg2.get("city");
			}
			System.out.println("----->>-->>"+arg2.toString());
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.passport.trust_login").addParams(
					"provider_code", platformName).addParams("openid", openid);
			if (!TextUtils.isEmpty(nickname))
				bean.addParams("nickname", nickname);
			if (!TextUtils.isEmpty(realname))
				bean.addParams("realname", realname);
			if (!TextUtils.isEmpty(userIcon))
				bean.addParams("headimgurl", userIcon);
			if (!TextUtils.isEmpty(gender))
				bean.addParams("sex", gender);
			if (!TextUtils.isEmpty(lactionString)) {
				bean.addParams("country", lactionString);
			}
			bean.addParams("source_app", mActivity.getString(R.string.app_channel_name));//来源
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					if (platformName == PLATFORM_WEIBO) {
						userLoginSuccess(all, true);
					} else {
						userLoginSuccess(all, false);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class ThirdLoginListener implements PlatformActionListener {
		private String platformName;

		public ThirdLoginListener(String platName) {
			this.platformName = platName;
		}

		@Override
		public void onError(Platform arg0, int arg1, Throwable arg2) {
			System.out.println("-------"+arg2.getMessage()+"--------");
			arg2.printStackTrace();
		}

		@Override
		public void onComplete(Platform arg0, int arg1,
				HashMap<String, Object> arg2) {
//			System.out.println("--->>--"+arg2.toString());
//			String openid = arg0.getDb().getUserId();
//			String nickname = arg0.getDb().getUserName();
//			String token = arg0.getDb().getToken();
//			String userIcon = arg0.getDb().getUserIcon();
//			String gender = arg0.getDb().getUserGender();
//			String gender1 = arg0.getDb().getPlatformNname();
//			Run.excuteJsonTask(new JsonTask(), new GetWeboFriends(openid, token)); 
//			String realname = arg0.getDb().getUserGender();
//			Run.excuteJsonTask(new JsonTask(), new ThirdUserLoginTask(
//					platformName, openid, nickname, ""));
			Run.excuteJsonTask(new JsonTask(), new ThirdUserLoginTask(platformName, arg0, arg2));
//			if (PLATFORM_WEIBO.equals(platformName)) {
//				String openid = arg2.get("idstr").toString();
//				String realname = arg2.get("screen_name").toString();
//				String nickname = arg2.get("name").toString();
//				Run.excuteJsonTask(new JsonTask(), new ThirdUserLoginTask(
//						platformName, openid, realname, nickname));
//			} else if(PLATFORM_WECHAT.equals(platformName)){
//				String openid = arg2.get("openid").toString();
//				String realname = arg2.get("nickname").toString();
//				Run.excuteJsonTask(new JsonTask(), new ThirdUserLoginTask(
//						platformName, openid, realname, realname));
//			} else if(PLATFORM_QQ.equals(platformName)){
				
//			}
		}

		@Override
		public void onCancel(Platform arg0, int arg1) {
			System.out.println("-------MSG_AUTH_CANCEL--------");
			Run.log("onCancel:", arg1);
		}

	}
	
	private class GetWeboFriends implements JsonTaskHandler{

		private String UID;
		private String token;
		
		public GetWeboFriends(String uid, String token){
			UID = uid;
			this.token = token;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean("https://api.weibo.com/2/friendships/friends.json?");
			req.addParams("uid", UID);
			req.addParams("count", "200");
			req.addParams("access_token", token);
			req.method = JsonRequestBean.METHOD_GET;
			return req;
		}
		
	}
}
