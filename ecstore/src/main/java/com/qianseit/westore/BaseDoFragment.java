package com.qianseit.westore;

import java.io.File;

import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class BaseDoFragment extends DoFragment {
	public final int REQUEST_CODE_USER_LOGIN = 0x11;
	public final int REQUEST_CODE_USER_REGIST = 0x12;

	public BaseDoFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	}

	@Override
	public void ui(int what, Message msg) {
	}

	@Override
	public void onClick(View v) {
		// 点击返回按钮，回到上一个界面，不退出应用
		if (mActionBar.isBackButton(v)) {
			mActivity.finish();
		} else {
			super.onClick(v);
		}
	}

	// ListView的底部View
	public static View makeListFooterView(Context context, int height) {
		View footerView = new View(context);
		footerView.setLayoutParams(new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, Run.dip2px(context, height)));
		return footerView;
	}

	// 检测用户登录状态
	public boolean checkUserLoginStatus() {
		LoginedUser user = AgentApplication.getApp(mActivity).getLoginedUser();
		if (!user.isLogined() || TextUtils.isEmpty(user.getUserID())) {
			// startActivityForResult(AgentActivity.intentForFragment(mActivity,
			// AgentActivity.FRAGMENT_ACCOUNT_LOGIN),
			// REQUEST_CODE_USER_LOGIN);
			return false;
		}

		return true;
	}

	/**
	 * 初始化headerview
	 * 
	 * @param rootView
	 * @param user
	 */
	public void initAccountHeaderView(View rootView, LoginedUser user) {
		((TextView) rootView.findViewById(R.id.account_header_view_uname))
				.setText(user.getNickName(mActivity));
		// 个人头像
		ImageView avatarView = (ImageView) rootView
				.findViewById(R.id.account_header_view_avatar);
		updateAvatarView(avatarView, user);
	}

	public void updateAvatarView(ImageView avatarView, LoginedUser user) {
		if (user.getAvatarUri() != null) {
//			ImageLoader loader = AgentApplication.getAvatarLoader(mActivity);
			VolleyImageLoader loader = AgentApplication.getApp(mActivity).getImageLoader();
			Uri avatarUri = Uri.parse(user.getAvatarUri());
			avatarView.setTag(avatarUri);
//			loader.showImage(avatarView, avatarUri);
			loader.showImage(avatarView, user.getAvatarUri());
//			VolleyImageLoader imageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
//			imageLoader.showImage(avatarView, user.getAvatarUri());
		}
	}

	/**
	 * 我的可提现金额和提现记录加载成功
	 * 
	 * @param json_str
	 */
	public void onCheckoutHistoryLoaded(String json_str) {
	}

	public class LoadCheckoutHistoryTask implements JsonTaskHandler {
		private int pageNum = 0;

		public LoadCheckoutHistoryTask(int pageNum) {
			this.pageNum = pageNum;
		}

		@Override
		public JsonRequestBean task_request() {
			CustomDialog dialog = getProgressDialog();
			if (dialog == null || !dialog.isShowing())
				showCancelableLoadingDialog();

			return new JsonRequestBean(
					"mobileapi.member.withdrawal").addParams("page_no",
					String.valueOf(pageNum));
		}

		@Override
		public void task_response(String json_str) {
			onCheckoutHistoryLoaded(json_str);
		}
	}

	// 更新店铺封面
	public class UpdateWallpaperTask implements JsonTaskHandler {
		private File file = null;
		private String type = null;
		private JsonRequestCallback callback;

		public UpdateWallpaperTask(File file, String type,
				JsonRequestCallback callback) {
			this.file = file;
			this.type = type;
			this.callback = callback;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.upload_image");
			if (file != null) {
				bean.addParams("type", type);
				bean.files = new File[] { file };
			}
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					LoginedUser user = AgentApplication
							.getLoginedUser(mActivity);
					JSONObject data = user.getUserInfo();
					data.put("avatar", all.optString("data"));
					user.setUserInfo(data);

					if (callback != null)
						callback.task_response(json_str);
					// ImageView view = (ImageView)
					// findViewById(R.id.westore_header_view_avatar);
					// view.setImageBitmap(Run.placeImage(
					// BitmapFactory.decodeFile(file.getAbsolutePath()),
					// app.mAvatarMask, app.mAvatarCover));
				}
			} catch (Exception e) {
			}
		}
	}
}
