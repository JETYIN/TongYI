package com.qianseit.westore;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import cn.shopex.ecstore.R;
import com.qianseit.westore.activity.MainTabFragmentActivity;
import com.qianseit.westore.ui.CustomProgrssDialog;
import com.qianseit.westore.ui.LoadingDialog;
import com.qianseit.westore.util.Util;

public abstract class DoActivity extends TopActivity {
	public static final String EXTRA_SHOW_BACK = "EXTRA_SHOW_BACK";

	private final int HANDLE_HIDE_LOADING_DIALOG = 100;
	private final int HANDLE_SHOW_LOADING_DIALOG = 101;
	private final int HANDLE_SHOW_CANCEL_LOADING_DIALOG = 102;

	private CustomProgrssDialog progress;
	private LoadingDialog progress1;
	public AgentApplication mApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = AgentApplication.getApp(this);
		if (!(this instanceof MainTabFragmentActivity))
			mApp.getRecentActivies().add(this);

		setContentView(R.layout.action_bar_activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Run.changeResourceLocale(getResources(), Locale.CHINA);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mApp.getRecentActivies().remove(this);
	}

	// 关闭所有历史打开的Activity
	public void finishAllRecentActivities() {
		for (Activity activity : mApp.getRecentActivies())
			activity.finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// 不继承父类，防止旋转或者重载Fragment出错
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* 当前Fragment */
	public Fragment getCurrentFragment() {
		return getSupportFragmentManager().findFragmentById(
				R.id.do_activity_fragment);
	}

	/**
	 * 返回ActionBar
	 * 
	 * @return
	 */
	public DoActionBar getDoActionBar() {
		Fragment fragment = getCurrentFragment();
		if (fragment != null && (fragment instanceof DoFragment))
			return ((DoFragment) fragment).getActionBar();
		return null;
	}

	/**
	 * 设置主Fragment
	 * 
	 * @param fragment
	 */
	public void setMainFragment(DoFragment fragment) {
		setMainFragment(fragment, 0, 0);
	}

	/**
	 * 设置主Fragment和切换动画
	 * 
	 * @param fragment
	 * @param enter
	 *            进场动画id
	 * @param exit
	 *            出场动画id
	 */
	public void setMainFragment(DoFragment fragment, int enter, int exit) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		if (enter != 0 && exit != 0) {
			transaction.setCustomAnimations(enter, exit);
		}
		transaction.replace(R.id.do_activity_fragment, fragment);
		transaction.commitAllowingStateLoss();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
		}
		return super.onKeyDown(keyCode, event);
	}

	// 评价应用
	public void evaluateApp() {
		Util.evaluateApp(this);
	}

	private Handler tHandler = new Handler() {
		public void handleMessage(Message msg) {
			Activity activity = DoActivity.this;

			switch (msg.what) {
			case HANDLE_SHOW_LOADING_DIALOG:
//				if (progress != null && progress.isShowing())
//					progress.dismiss();
//				progress = Util.showLoadingDialog(activity, null, null);
//				progress.setCancelable(false);
				if (progress1 != null && progress1.isShowing()) {
					progress1 = new LoadingDialog(activity);
				}
				progress1.show();
				progress1.setCancelable(false);
				break;
			case HANDLE_SHOW_CANCEL_LOADING_DIALOG:
//				if (progress != null && progress.isShowing())
//					progress.dismiss();
//				progress = Util.showLoadingDialog(activity, null, null);
//				progress.setCancelable(true);
				
				if (progress1 != null && progress1.isShowing()) {
					progress1.dismiss();
				}
				progress1 = new LoadingDialog(activity);
				progress1.show();
				progress1.setCancelable(true);
				break;
			case HANDLE_HIDE_LOADING_DIALOG:
//				Util.hideLoading(progress);
				if (progress1 != null) {
					progress1.dismiss();
				}
				break;
			}
		};
	};

	/**
	 * 显示加载提示框
	 */
	public void showLoadingDialog() {
		tHandler.sendEmptyMessage(HANDLE_SHOW_LOADING_DIALOG);
	}

	/**
	 * 显示可以取消的提示框
	 */
	public void showCancelableLoadingDialog() {
		tHandler.sendEmptyMessage(HANDLE_SHOW_CANCEL_LOADING_DIALOG);
	}

	// 隐藏提示框
	public void hideLoadingDialog() {
		tHandler.sendEmptyMessageDelayed(HANDLE_HIDE_LOADING_DIALOG, 1000);
	}

	// 隐藏提示框
	public void hideLoadingDialog_mt() {
//		Util.hideLoading(progress);
		if (progress1 != null) {
			progress1.dismiss();
		}
	}
}
