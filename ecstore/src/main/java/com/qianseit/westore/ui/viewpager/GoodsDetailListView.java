package com.qianseit.westore.ui.viewpager;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

public class GoodsDetailListView extends ListView {

	private boolean isScroll = true;//当前的view是否可滚动
	PointF curP = new PointF();
	
	public GoodsDetailListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public GoodsDetailListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public GoodsDetailListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs ,defStyle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// ScrollView被点击到，即可滑动
			isScroll = true;
			curP.x = event.getX();
			curP.y = event.getY();
			// 通知父控件现在进行的是本控件的操作，不要对我的操作进行干扰
			getParent().requestDisallowInterceptTouchEvent(true);
			break;

		case MotionEvent.ACTION_MOVE:
			float lastY = event.getY(event.getPointerCount() - 1);
//			if (isBottom())// 如果到达底部，先设置为不能滚动
//				isScroll = false;
//			// 如果到达底部，但开始向上滚动，那么ScrollView可以滚动
			Log.i("aaa", "<<<----->>> c : " + (curP.y - lastY));
			Log.i("aaa", "<<<----->>> c : " + isBottom());
//			if (isBottom() && (curP.y - lastY <= 0))
//				isScroll = true;
			if (getScrollY() == 0)// 滑到顶部不能再滑
				isScroll = false;
			if ((getScrollY() == 0) && (curP.y - lastY > 0))// 滑动到顶部，向下滑，可以滑到
				isScroll = true;
			getParent().requestDisallowInterceptTouchEvent(isScroll);
			break;
		case MotionEvent.ACTION_UP:
			isScroll = false;
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private boolean isBottom(){
		if(getScrollY() + getHeight() >=  computeVerticalScrollRange()){
			Log.i("", "<<<------>>> : true");
			return true;
		}
		if (getLastVisiblePosition() >= getCount() - 1) {
			Log.i("", "<<<------>>> 1: true");
			return true;
		}
		return false;
	}
}
