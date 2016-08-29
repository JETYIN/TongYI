package com.qianseit.westore.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 发生变化时候会进行通知的ScrollView
 * 
 */
public class NotifyChangedScrollView extends FlowScrollView implements
		NotifyChangedView {
	private onSizeChangedListener mSizeChangedListener;
	private onScrollChangedListener mOnScrollChangedListener;

	private boolean mCanScroll = true;

	public NotifyChangedScrollView(Context context) {
		super(context);
	}

	public NotifyChangedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NotifyChangedScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mCanScroll)
			return super.onInterceptTouchEvent(ev);
		return false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mSizeChangedListener != null)
			mSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
	}

	/**
	 * 设置尺寸变化时的监听器
	 * 
	 * @param listener
	 */
	@Override
	public void setOnSizeChangedListener(onSizeChangedListener listener) {
		this.mSizeChangedListener = listener;
	}

	public void setOnScrollChangedListener(
			onScrollChangedListener mOnScrollChangedListener) {
		this.mOnScrollChangedListener = mOnScrollChangedListener;
	}

	/**
	 * 设置ScrollView是否可以滚动
	 * 
	 * @param mCanScroll
	 */
	public void setCanScroll(boolean mCanScroll) {
		this.mCanScroll = mCanScroll;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mOnScrollChangedListener != null)
			mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
	}

	public interface onScrollChangedListener {
		public void onScrollChanged(int l, int t, int oldl, int oldt);
	}
}
