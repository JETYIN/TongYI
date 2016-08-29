package com.qianseit.westore;


import com.qianseit.westore.ui.CustomProgrssDialog;
import com.qianseit.westore.ui.LoadingDialog;
import com.qianseit.westore.util.Util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public abstract class DoFragment extends Fragment implements OnClickListener {
	private final int HANDLE_HIDE_LOADING_DIALOG = 100;
	private final int HANDLE_SHOW_LOADING_DIALOG = 101;
	private final int HANDLE_SHOW_CANCEL_LOADING_DIALOG = 102;

	private CustomProgrssDialog progress;
	public DoActionBar mActionBar;
	public View rootView;

	public FragmentActivity mActivity;

	// 是否显示返回按钮
	private boolean showBackButton;

	/**
	 * Notice:Never use this constructor<br />
	 * 只是防止Fragment重载崩溃
	 */
	public DoFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// fragment一定要有无参数构造方法，我们尽量去适应它
		// 这段代码不能直接放在构造方法中，构造时还没有activiy
		if (mActivity == null)
			mActivity = (FragmentActivity) getActivity();

		// ActionBar为空时，构造ActionBar
		if (mActionBar == null) {
			mActionBar = new DoActionBar(mActivity);
			// 设置返回按钮是否可用
			if (getArguments() != null)
				showBackButton = getArguments().getBoolean(
						DoActivity.EXTRA_SHOW_BACK, false);
			showBackButton = mActivity.getIntent().getBooleanExtra(
					DoActivity.EXTRA_SHOW_BACK, showBackButton);
			mActionBar.setShowBackButton(showBackButton);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// ActionBar已经有父View则从父View移除
		if (mActionBar != null && mActionBar.getParent() != null)
			((ViewGroup) mActionBar.getParent()).removeView(mActionBar);
		mActionBar.setShowBackButton(showBackButton);

		// rootView已存在，说明已经初始化好Fragment则不需要重复初始化
		// 在子方法中不要加入需要重复初始化的方法
		if (rootView != null)
			return mActionBar;

		init(inflater, container, savedInstanceState);
		mActionBar.getContainerView().addView(rootView);
		return mActionBar;
	}

	// 初始化Fragment视图
	public abstract void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState);

	/**
	 * 查找指定Id的View
	 * 
	 * @param resId
	 * @return
	 */
	public View findViewById(int resId) {
		return rootView.findViewById(resId);
	}

	/**
	 * 获得ActionBar
	 * 
	 * @return
	 */
	public DoActionBar getActionBar() {
		return mActionBar;
	}

	/**
	 * 获得进度框
	 * 
	 * @return
	 */
	public CustomProgrssDialog getProgressDialog() {
		return progress;
	}

	/**
	 * 获得LayoutInflater
	 * 
	 * @return
	 */
	public LayoutInflater getLayoutInflater() {
		return getActivity().getLayoutInflater();
	}

	/**
	 * 获得LayoutInflater
	 * 
	 * @return
	 */
	@Override
	public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
		return getActivity().getLayoutInflater();
	}

	/**
	 * 获得MenuInflater
	 * 
	 * @return
	 */
	public MenuInflater getMenuInflater() {
		return getActivity().getMenuInflater();
	}

	@Override
	public void onClick(View v) {
	}

	/**
	 * 主线程执行命令
	 * 
	 * @param what
	 */
	public final void call(int what) {
		call(what, new Message());
	}

	public final void callDelayed(int what, long mills) {
		call(what, new Message(), mills);
	}

	public final void call(int what, Message msg) {
		call(what, msg, 0);
	}

	public final void call(int what, Message msg, long mills) {
		msg.what = what;
		mHandler.sendMessageDelayed(msg, mills);
	}

	/**
	 * 统一操作UI入口
	 * 
	 * @param what
	 */
	public abstract void ui(int what, Message msg);

	public final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ui(msg.what, msg);
		}
	};

	private Handler tHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_SHOW_LOADING_DIALOG:
//				if (progress != null && progress.isShowing())
//					progress.dismiss();
//				progress = Util.showLoadingDialog(mActivity, null, null);
//				if (progress != null)
//					progress.setCancelable(false);
				
				if (progress1 != null && progress1.isShowing())
					progress1.dismiss();
				progress1 = new LoadingDialog(mActivity);
				progress1.show();
				if (progress1 != null) {
					progress1.setCancelable(false);
				}
				break;
			case HANDLE_SHOW_CANCEL_LOADING_DIALOG:
				showCancelableLoadingDialog_mt();
				break;
			case HANDLE_HIDE_LOADING_DIALOG:
				hideLoadingDialog_mt();
				break;
			}
		};
	};
	
	private LoadingDialog progress1;

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

	/**
	 * 主线程直接调用显示可以取消的提示框<br/>
	 */
	public void showCancelableLoadingDialog_mt() {
//		if (progress != null && progress.isShowing())
//			progress.dismiss();
//		progress = Util.showLoadingDialog(getActivity(), null, null);
//		if (progress != null)
//			progress.setCancelable(true);
		if (progress1 != null && progress1.isShowing())
			progress1.dismiss();
		progress1 = new LoadingDialog(mActivity);
		progress1.show();
		if (progress1 != null)
			progress1.setCancelable(true);
		
		
	}

	// 隐藏提示框
	public void hideLoadingDialog() {
		tHandler.sendEmptyMessageDelayed(HANDLE_HIDE_LOADING_DIALOG, 1000);
	}

	/**
	 * 主线程直接调用取消提示框<br/>
	 */
	public void hideLoadingDialog_mt() {
//		Util.hideLoading(progress);
		if (progress1 != null) {
			progress1.dismiss();
		}
	}

	// 隐藏软键盘
	public void hideKeyboard(View v) {
		((InputMethodManager) mActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void onWindowFocusChanged(boolean hasFocus){
		
	}
}
