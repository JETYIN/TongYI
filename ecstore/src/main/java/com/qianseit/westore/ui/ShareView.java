package com.qianseit.westore.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleAnimListener;
import cn.shopex.ecstore.R;

public class ShareView extends FrameLayout implements OnClickListener,
		PlatformActionListener {
	private boolean isInited = false;

	private ShareViewDataSource mDataSource;
	private Handler mHandler = new Handler();

	private int[] actionIds = { R.id.share_wechat_chat,
			R.id.share_wechat_circle, R.id.share_qq, R.id.share_qzone,
			R.id.share_weibo, R.id.share_qqblog, R.id.share_copy,
			R.id.share_save };

	public ShareView(Context context) {
		super(context);
		this.initShareView();
	}

	public ShareView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initShareView();
	}

	public ShareView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.initShareView();
	}

	private void initShareView() {
		if (!isInited) {
			isInited = true;
			addView(inflate(getContext(), R.layout.share_view, null));
			for (int i = 0, length = actionIds.length; i < length; i++)
				findViewById(actionIds[i]).setOnClickListener(this);
			findViewById(R.id.share_view_cancel).setOnClickListener(this);
		}
	}

	/**
	 * 设置分享数据源
	 * 
	 * @param mDataSource
	 */
	public void setDataSource(ShareViewDataSource mDataSource) {
		this.mDataSource = mDataSource;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.share_view_cancel) {
			dismissShareView();
			return;
		}

		// 没有数据源无法分享
		if (mDataSource == null)
			return;

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
		String message = shareText
				+ (TextUtils.isEmpty(shareUrl) ? "" : shareUrl);

		if (v.getId() == R.id.share_copy) {
			platform = ShareSDK.getPlatform(getContext(), ShortMessage.NAME);
			ShortMessage.ShareParams params = new ShortMessage.ShareParams();
			if (file != null)
				params.setImagePath(file.getAbsolutePath());
			params.setTitle(shareText);
			params.setText(message);
			platform.share(params);
		} else if (v.getId() == R.id.share_qq) {
			platform = ShareSDK.getPlatform(getContext(), QQ.NAME);
			QQ.ShareParams params = new QQ.ShareParams();
			if (file != null)
				params.setImagePath(file.getAbsolutePath());
			params.setTitle(shareText);
			params.setText(message);
			params.setTitleUrl(shareUrl);
			params.setSiteUrl(shareUrl);
			params.setUrl(shareUrl);
			platform.share(params);
		} else if (v.getId() == R.id.share_qzone) {
			platform = ShareSDK.getPlatform(getContext(), QZone.NAME);
			QZone.ShareParams params = new QZone.ShareParams();
			if (!TextUtils.isEmpty(shareImageUrl))
				params.setImageUrl(shareImageUrl);
			params.setTitle(shareText);
			params.setText(message);
			params.setTitleUrl(mDataSource.getShareUrl());
			params.setSite(message);
			params.setSiteUrl(mDataSource.getShareUrl());
			platform.share(params);
		} else if (v.getId() == R.id.share_wechat_chat) {
			platform = ShareSDK.getPlatform(getContext(), Wechat.NAME);
			Wechat.ShareParams params = new Wechat.ShareParams();
			if (file != null) {
				params.setImagePath(file.getAbsolutePath());
			}
			if (!TextUtils.isEmpty(shareUrl)) {
				params.setShareType(Platform.SHARE_WEBPAGE);
			} else if (file != null) {
				params.setShareType(Platform.SHARE_IMAGE);
			}
			params.setText(message);
			params.setTitle(shareText);
			params.setTitleUrl(shareUrl);
			params.setImageUrl(shareImageUrl);
			params.setUrl(shareUrl);
			platform.share(params);
		} else if (v.getId() == R.id.share_wechat_circle) {		//微信朋友圈
			platform = ShareSDK.getPlatform(getContext(), WechatMoments.NAME);
			WechatMoments.ShareParams params = new WechatMoments.ShareParams();
			if (!TextUtils.isEmpty(shareUrl)) {
				params.setShareType(Platform.SHARE_WEBPAGE);
				if(file!=null){
					params.setImagePath(file.getAbsolutePath());
				}
				params.setUrl(shareUrl);
			} else {
				if (file != null) {
					params.setImagePath(file.getAbsolutePath());
					params.setShareType(Platform.SHARE_IMAGE);
				}
			}
			params.setTitle(shareText);
			params.setUrl(shareUrl);
			params.setText(message);
			platform.share(params);
		} else if (v.getId() == R.id.share_weibo) {		//微博
			platform = ShareSDK.getPlatform(getContext(), SinaWeibo.NAME);
//			platform.SSOSetting(true);
			SinaWeibo.ShareParams params = new SinaWeibo.ShareParams();
			if (file != null)
				params.setImagePath(file.getAbsolutePath());
			params.setText(message);
			platform.share(params);
		} else if (v.getId() == R.id.share_qqblog) {
			platform = ShareSDK.getPlatform(getContext(), TencentWeibo.NAME);
			TencentWeibo.ShareParams params = new TencentWeibo.ShareParams();
			if (file != null)
				params.setImagePath(file.getAbsolutePath());
			params.setText(message);
			platform.share(params);
		} else if (v.getId() == R.id.share_save) {
			try {
				if (file != null) {
					File doFolder = new File(Run.doFolder);
					File destFile = new File(doFolder, getSavedFile());
					FileUtils.copyFile(file, destFile);
					alertSuccess(((TextView) v).getText().toString());
				}
			} catch (IOException e) {
				alertFailed(((TextView) v).getText().toString());
			}
		}
		if (platform != null)
			platform.setPlatformActionListener(this);
		this.dismissShareView();
	}

	private void alertSuccess(final String platName) {
		final Context ctx = getContext();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Run.alert(ctx, ctx.getString(R.string.share_success, platName));
			}
		});
	}

	private void alertFailed(final String platName) {
		final Context ctx = getContext();
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Run.alert(ctx, ctx.getString(R.string.share_failed, platName));
			}
		});
	}

	private String getSavedFile() {
		return Run.buildString(new SimpleDateFormat("yyyyMMddkkmmss")
				.format(System.currentTimeMillis()), ".jpg");
	}

	// 显示分享
	public void showShareView() {
		setVisibility(View.VISIBLE);
		View animView = findViewById(R.id.share_view_zone);
		Animation animIn = AnimationUtils.loadAnimation(getContext(),
				R.anim.push_up_in);
		animView.startAnimation(animIn);
	}

	// 隐藏分享
	public void dismissShareView() {
		View animView = findViewById(R.id.share_view_zone);
		Animation animOut = AnimationUtils.loadAnimation(getContext(),
				R.anim.push_down_out);
		animOut.setAnimationListener(new SimpleAnimListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
			}
		});
		animView.startAnimation(animOut);
	}

	@Override
	public void onCancel(Platform arg0, int arg1) {
		alertFailed(getContext().getString(R.string.share));
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
//		alertSuccess(getContext().getString(R.string.share));
	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
		alertFailed(getContext().getString(R.string.share));
	}

	public interface ShareViewDataSource {
		public String getShareText();

		public String getShareImageFile();

		public String getShareImageUrl();

		public String getShareUrl();
	}
}
