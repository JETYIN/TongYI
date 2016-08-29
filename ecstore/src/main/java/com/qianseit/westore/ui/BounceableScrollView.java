package com.qianseit.westore.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.qianseit.westore.Run;

public class BounceableScrollView extends NotifyChangedScrollView {
	private final int MAX_Y_OVERSCROLL_DISTANCE = 200;

	private int mMaxYOverscrollDistance;

	public BounceableScrollView(Context context) {
		super(context);
		mMaxYOverscrollDistance = Run
				.dip2px(context, MAX_Y_OVERSCROLL_DISTANCE);
	}

	public BounceableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMaxYOverscrollDistance = Run
				.dip2px(context, MAX_Y_OVERSCROLL_DISTANCE);
	}

	public BounceableScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mMaxYOverscrollDistance = Run
				.dip2px(context, MAX_Y_OVERSCROLL_DISTANCE);
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		// Run.log("onOverScrolled:", deltaX, "  ", deltaY, "  ", scrollX, "  ",
		// scrollY, "  ", scrollRangeX, "  ", scrollRangeY);
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
				scrollRangeX, scrollRangeY, maxOverScrollX,
				mMaxYOverscrollDistance, isTouchEvent);
	}
	
}
