package com.qianseit.westore.ui;

import java.io.File;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
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

public abstract class CommendPopupWindow extends PopupWindow implements
		OnClickListener {
	private View conentView;
	private Activity context;
	private ShareViewDataSource mDataSource;
	private Handler mHandler = new Handler();

	public CommendPopupWindow(Activity context) {
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		conentView = inflater.inflate(R.layout.commend_popup_dialog, null);
		conentView.findViewById(R.id.account_top1).setOnClickListener(this);
		conentView.findViewById(R.id.account_top2).setOnClickListener(this);
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int w = dm.widthPixels;// 获取屏幕分辨率宽度
		int h = dm.heightPixels;
		// 设置SelectPicPopupWindow的View
		this.setContentView(conentView);
		// 设置SelectPicPopupWindow弹出窗体的宽
		// this.setWidth(w / 2 + Run.dip2px(context, 70));
		this.setWidth(w);
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
			this.showAsDropDown(parent, parent.getLayoutParams().width /5, 10);
		} else {
			this.dismiss();
		}
	}

	public void setDataSource(ShareViewDataSource mDataSource) {
		this.mDataSource = mDataSource;
	}
}
