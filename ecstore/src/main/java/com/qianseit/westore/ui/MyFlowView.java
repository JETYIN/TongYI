package com.qianseit.westore.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

/**
 * 水平滚动翻页的View
 * 
 * A horizontally scrollable {@link ViewGroup} with items populated from an
 * {@link Adapter}. The ViewFlow uses a buffer to store loaded {@link View}s in.
 * The default size of the buffer is 3 elements on both sides of the currently
 * visible {@link View}, making up a total buffer size of 3 * 2 + 1 = 7. The
 * buffer size can be changed using the {@code sidebuffer} xml attribute.
 * 
 */
public class MyFlowView extends FlowView {
	onSizeChangedListener mOnSizeChangedListener;

	public MyFlowView(Context context) {
		super(context);
	}

	public MyFlowView(Context context, int sideBuffer) {
		super(context);
	}

	public MyFlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setOnSizeChangedListener(
			onSizeChangedListener mOnSizeChangedListener) {
		this.mOnSizeChangedListener = mOnSizeChangedListener;
	}

	@Override
	public void onScrollChanged(int h, int v, int oldh, int oldv) {
		super.onScrollChanged(h, v, oldh, oldv);
		FlowIndicator indicator = getFlowIndicator();
		if (indicator != null) {
			/*
			 * The actual horizontal scroll origin does typically not match the
			 * perceived one. Therefore, we need to calculate the perceived
			 * horizontal scroll origin here, since we use a view buffer.
			 */
			int hPerceived = h;
			indicator.onScrolled(hPerceived, v, oldh, oldv);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0 && h > 0 && mOnSizeChangedListener != null)
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
	}

	/**
	 * ScrollView的尺寸发生变化的监听器
	 */
	public interface onSizeChangedListener {
		public void onSizeChanged(View view, int newWidth, int newHeight,
				int oldWidth, int oldHeight);
	}

}