package com.qianseit.westore.ui;

import java.io.File;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import cn.shopex.ecstore.R;
import com.qianseit.westore.Run;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.util.Util;

public class SharedPopupWindow extends PopupWindow implements OnClickListener {
	private View conentView;
	private Activity context;
	private ShareViewDataSource mDataSource;
	private Handler mHandler = new Handler();

	public SharedPopupWindow(Activity context) {
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		conentView = inflater.inflate(R.layout.more_popup_dialog, null);
		conentView.findViewById(R.id.account_shared_wechat).setOnClickListener(
				this);
		conentView.findViewById(R.id.account_shared_qq)
				.setOnClickListener(this);
		conentView.findViewById(R.id.account_shared_xinlang)
				.setOnClickListener(this);
		conentView.findViewById(R.id.account_shared_wechat_circle)
		.setOnClickListener(this);
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int w = dm.widthPixels;// 获取屏幕分辨率宽度
		int h = dm.heightPixels;
		// 设置SelectPicPopupWindow的View
		this.setContentView(conentView);
		// 设置SelectPicPopupWindow弹出窗体的宽
//		this.setWidth(w / 2 + Run.dip2px(context, 70));
		this.setWidth(w );
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		// 刷新状态
		this.update();
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		this.setBackgroundDrawable(dw);
		// mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimationPreview);

	}
	
	public void showPopupWindow(View parent) {
		if (!this.isShowing()) {
//			this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 18);
			int[] location = new int[2];  
			parent.getLocationOnScreen(location); 
			this.showAtLocation(parent, Gravity.NO_GRAVITY, location[0], location[1] + parent.getLayoutParams().width + Util.dip2px(context, 25));
		} else {
			this.dismiss();
		}
	}

	public void setDataSource(ShareViewDataSource mDataSource) {
		this.mDataSource = mDataSource;
	}

	@Override
	public void onClick(View v) {
		String shareImageUrl = mDataSource.getShareImageUrl();
		String shareImageFile = mDataSource.getShareImageFile();
		String shareText = mDataSource.getShareText();
		if (TextUtils.isEmpty(shareText) && TextUtils.isEmpty(shareImageFile))
			return;

		File file = null;
		// 暂时移到这里，开放分享图片后移到最前面
		if (!TextUtils.isEmpty(shareImageFile)) {
			file = new File(mDataSource.getShareImageFile());
			File destFile = new File(Run.doFolder, "share_image.jpg");
			try {
				FileUtils.copyFile(file, destFile);
				file = destFile;
			} catch (Exception e) {
			}
		}

		Platform platform = null;
		String shareUrl = mDataSource.getShareUrl();
		String message = shareText;

		switch (v.getId()) {
		case R.id.account_shared_wechat:
			platform = ShareSDK.getPlatform(context, Wechat.NAME);
			Wechat.ShareParams params = new Wechat.ShareParams();
			if (file != null) {
				params.setImagePath(file.getAbsolutePath());
			}
			if (!TextUtils.isEmpty(shareUrl)) {
				params.setShareType(Platform.SHARE_WEBPAGE);
			} else if (file != null) {
				params.setShareType(Platform.SHARE_IMAGE);
			}
			if (shareUrl != null && shareUrl.contains("opinions")) {
				params.setTitle(message.split("-")[0]);
				params.setText(message.split("-")[1]);
			} else {
				params.setTitle(message);
				params.setText(message);
			}
			params.setTitleUrl(shareUrl);
			params.setImageUrl(shareImageUrl);
			params.setUrl(shareUrl);
			platform.share(params);
			break;
		case R.id.account_shared_qq:
			platform = ShareSDK.getPlatform(context, QQ.NAME);
			QQ.ShareParams paramss = new QQ.ShareParams();
			if (file != null)
				paramss.setImagePath(file.getAbsolutePath());
			if (shareUrl != null && shareUrl.contains("opinions")) {
				paramss.setTitle(message.split("-")[0]);
				paramss.setText(message.split("-")[1]);
			} else {
				paramss.setTitle(message);
				paramss.setText(message);
			}
			paramss.setTitleUrl(shareUrl);
			paramss.setSiteUrl(shareUrl);
			paramss.setUrl(shareUrl);
			platform.share(paramss);
			break;
		case R.id.account_shared_xinlang:
			platform = ShareSDK.getPlatform(context,SinaWeibo.NAME);
//			platform.SSOSetting(true);
			SinaWeibo.ShareParams paramsw = new SinaWeibo.ShareParams();
			if (file != null)
				paramsw.setImagePath(file.getAbsolutePath());
			
//			if (shareUrl != null && shareUrl.contains("opinions")) {
//			} else {
//				paramsw.setText(message);
//			}
			paramsw.setText(message.split("-")[1] +"@樱淘社"+shareUrl);
//			paramsw.setText(message);
			paramsw.setUrl(shareUrl);
			platform.share(paramsw);
			break;
		case R.id.account_shared_wechat_circle:
			platform = ShareSDK.getPlatform(context, WechatMoments.NAME);
			WechatMoments.ShareParams paramsWE = new WechatMoments.ShareParams();
			if (!TextUtils.isEmpty(shareUrl)) {
				paramsWE.setShareType(Platform.SHARE_WEBPAGE);
				if(file!=null){
					paramsWE.setImagePath(file.getAbsolutePath());
				}
				paramsWE.setUrl(shareUrl);
			} else {
				if (file != null) {
					paramsWE.setImagePath(file.getAbsolutePath());
					paramsWE.setShareType(Platform.SHARE_IMAGE);
				}
			}
//			paramsWE.setTitle(shareText);
//			paramsWE.setUrl(shareUrl);
			if (shareUrl != null && shareUrl.contains("opinions")) {
				paramsWE.setTitle(message.split("-")[1]);
			} else {
				paramsWE.setTitle(message);
			}
//			paramsWE.setTitle(message);
//			paramsWE.setText(message);
			platform.share(paramsWE);
			break;
		default:
			break;
		}
		dismiss();
	}
}
