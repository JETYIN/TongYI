package com.qianseit.westore.ui;

import android.view.View;

/**
 * 发生变化时候会进行通知的ScrollView
 * 
 */
public interface NotifyChangedView {

	/**
	 * 设置尺寸变化时的监听器
	 * 
	 * @param listener
	 */
	public void setOnSizeChangedListener(onSizeChangedListener listener);

	/**
	 * ScrollView的尺寸发生变化的监听器
	 */
	public interface onSizeChangedListener {
		public void onSizeChanged(View view, int newWidth, int newHeight,
				int oldWidth, int oldHeight);
	}

}
