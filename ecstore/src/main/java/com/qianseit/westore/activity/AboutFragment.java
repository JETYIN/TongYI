package com.qianseit.westore.activity;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.ShareView;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.MyAutoUpdate;

/**
 * 关于、帮助
 * 
 * 
 */
public class AboutFragment extends BaseDoFragment implements OnClickListener,
		ShareViewDataSource {

	private ShareView mSharedView;
	private String shareUrl;
	private String shareImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.about_us);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_about, null);
		mSharedView = (ShareView) findViewById(R.id.share_view);
		mSharedView.setDataSource(this); 
//		mActionBar.setRightImageButton(R.drawable.icon_share,
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						if (v.getId() == R.id.action_bar_titlebar_right_ib) {
//							// 显示分享view
//							mSharedView.showShareView();
//						}
//					}
//				});

		// long newestVCode = Run.loadOptionLong(mActivity,
		// Run.pk_newest_version_code, 0);
		// if (newestVCode > Run.getVersionCode(mActivity)) {
		// mActionBar.setRightTitleButton(R.string.update,
		// new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Run.excuteJsonTask(new JsonTask(), new UpdateTask(
		// (DoActivity) mActivity));
		// }
		// });
		// }
//		rootView.setVisibility(View.INVISIBLE);

//		new JsonTask().execute(new GetAboutData());

		// 应用名与版本号
		PackageManager mPm = mActivity.getPackageManager();
		try {
			PackageInfo pi = mPm.getPackageInfo(mActivity.getPackageName(), 0);
			((TextView) findViewById(R.id.about_version_info))
					.setText(getString(R.string.about_version, pi.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		findViewById(R.id.about_tel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//用intent启动拨打电话
				Dialog dialog = AccountLoginFragment.showAlertDialog(mActivity, "是否拨打电话！",
						"取消", "确定", null, new OnClickListener() {

							@Override
							public void onClick(View v) {
								Intent intent = new Intent(Intent.ACTION_CALL, Uri
										.parse("tel:" + "4008889739"));
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}, false, null);
			}
		});
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
		// if (v.getId() == R.id.about_update) {
		// ((DoActivity) mActivity).showCancelableLoadingDialog();
		// new JsonTask().execute(new UpdateTask((DoActivity) mActivity));
		// } else if (v.getId() == R.id.about_license) {
		// }
	}

	private class GetAboutData implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data == null) {
						return;
					}
					shareUrl = data.optString("link");
					shareImage = data.optJSONObject("android").optString("qrcode");
					AgentApplication
							.getApp(mActivity)
							.getImageLoader()
							.showImage(
									(ImageView) findViewById(R.id.about_version_qrcode),
									data.optJSONObject("android").optString(
											"qrcode"));
					((TextView) findViewById(R.id.about_tel))
							.setText(Run.buildString("客服电话：",
									data.optString("service_tel")));
					((TextView) findViewById(R.id.about_wechat)).setText(Run
							.buildString("微信公众号：",
									data.optString("wechat_account")));
					((TextView) findViewById(R.id.about_webo))
							.setText(Run.buildString("微博：",
									data.optString("weibo_account")));
					String str = "Copyright©2015\nAll Rights Reserved";
					SpannableString span = new SpannableString(str);
					ForegroundColorSpan spancolor = new ForegroundColorSpan(
							Color.parseColor(getString(R.color.theme_color)));
					span.setSpan(spancolor, 9, 10,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					((TextView) findViewById(R.id.about_copyright))
							.setText(span);
				}
				rootView.setVisibility(View.VISIBLE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.info.get_about");
		}

	}

	public static class UpdateTask implements JsonTaskHandler {
		private DoActivity activity;
		private boolean isShowDialog;

		public UpdateTask(DoActivity activity) {
			this.activity = activity;
		}
		
		public UpdateTask(DoActivity activity ,boolean isShowDialog) {
			this.activity = activity;
			this.isShowDialog = isShowDialog;
		}

		@Override
		public JsonRequestBean task_request() {
			if (isShowDialog) {
				activity.showCancelableLoadingDialog();
			}
			JsonRequestBean jrb = new JsonRequestBean(
					"mobileapi.info.get_version");
			return jrb;
		}

		@Override
		public void task_response(String json_str) {
			if (activity != null && isShowDialog) {
				activity.hideLoadingDialog_mt();
			}
			try {
				JSONObject all = new JSONObject(json_str);
				JSONObject data = all.optJSONObject("data");
				if (data == null) {
					return;
				}
				JSONObject androidVer = data.optJSONObject("android");
				if (androidVer == null) {
					return;
				}
				if (!TextUtils.equals(androidVer.optString("ver"), activity.getString(R.string.app_version_name))) {
//					MyAutoUpdate autoUpdate = new MyAutoUpdate(activity);
//					autoUpdate.checkUpdateInfo(androidVer.optString("down"));
				}
//				final long version = data.optLong("version_num");
//				final String downloadUrl = data.optString("down_url");
//				int d = Run.getVersionCode(activity);
//				if (version <= Run.getVersionCode(activity)
//						|| TextUtils.isEmpty(downloadUrl))
//					return;
//
//				// app内下载，下载完提示安装
//				Run.savePrefs(activity, Run.pk_newest_version_code, version);
//				MyAutoUpdate autoUpdate = new MyAutoUpdate(activity);
//				autoUpdate.checkUpdateInfo(downloadUrl);

				// 版本提示框
				// final CustomDialog dialog = new CustomDialog(activity);
				// dialog.setTitle(R.string.update_newest_app);
				// dialog.setMessage(all.optString("version_msg"));
				// dialog.setNegativeButton(R.string.cancel, null);
				// dialog.setPositiveButton(R.string.ok, new OnClickListener() {
				// @Override
				// public void onClick(View v) {
				// Run.openBrowser(activity, downloadUrl);
				// }
				// }).setCancelable(true).show();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public String getShareText() {
		return "关于我们";
	}

	@Override
	public String getShareImageFile() {
		return CacheUtils.getImageCacheFile(shareImage);
	}

	@Override
	public String getShareImageUrl() {
		return shareImage;
	}

	@Override
	public String getShareUrl() {
		return shareUrl;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(mSharedView.getVisibility()==View.VISIBLE){
				mSharedView.dismissShareView();
				return true;
			}else
				return super.onKeyDown(keyCode, event);
		}else
		return super.onKeyDown(keyCode, event);
	}
}
