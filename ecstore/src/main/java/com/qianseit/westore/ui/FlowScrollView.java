package com.qianseit.westore.ui;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class FlowScrollView extends ScrollView {
	private boolean canScroll = true;
	private boolean isCanScroll = false;
	PointF curP = new PointF();
	private ScrollEndListener litener;

	public FlowScrollView(Context context) {
		super(context);
	}

	public FlowScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FlowScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!canScroll) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!canScroll) {
			return false;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isCanScroll = true;
			curP.x = ev.getX();
			curP.y = ev.getY();
			getParent().requestDisallowInterceptTouchEvent(true);
			break;

		case MotionEvent.ACTION_MOVE:
			float lastY = ev.getY(ev.getPointerCount() - 1);
			if (isBottom())
				isCanScroll = false;
			if (isBottom() && (curP.y - lastY < 0))
				isCanScroll = true;
			if (getScrollY() == 0)
				isCanScroll = false;
			if ((getScrollY() == 0) && (curP.y - lastY > 0))
				isCanScroll = true;
			getParent().requestDisallowInterceptTouchEvent(isCanScroll);
			break;
		case MotionEvent.ACTION_UP:
			isCanScroll = false;
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
//		if (isBottom() && litener != null) {
//			litener.scrollEnd(true);
//		}
	}
	
	public void setOnEndListener(ScrollEndListener litener){
		this.litener = litener;
	}
	
	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
			boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		if (scrollY != 0 && litener != null) {
			litener.scrollEnd(clampedY);
		}
	}
	
	private boolean isBottom(){
		if(getScrollY() + getHeight() >=  computeVerticalScrollRange()){
			return true;
		}
		return false;
	}

	public void setCanScroll(boolean canScroll) {
		this.canScroll = canScroll;
	}
	
	public interface ScrollEndListener{
		public void scrollEnd(boolean isEnd);
	}
}
