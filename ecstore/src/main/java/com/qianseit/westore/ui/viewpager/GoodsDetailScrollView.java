package com.qianseit.westore.ui.viewpager;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class GoodsDetailScrollView extends ScrollView {

	private boolean isScroll = true;
	PointF curP = new PointF();

	public GoodsDetailScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return isScroll;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// ScrollView������������ɻ���
			isScroll = true;
			curP.x = event.getX();
			curP.y = event.getY();
			// ֪ͨ���ؼ����ڽ��е��Ǳ��ؼ��Ĳ�������Ҫ���ҵĲ������и���
			getParent().requestDisallowInterceptTouchEvent(true);
			break;

		case MotionEvent.ACTION_MOVE:
			float lastY = event.getY(event.getPointerCount() - 1);
			if (isBottom())// ����ײ���������Ϊ���ܹ���
				isScroll = false;
			// ����ײ�������ʼ���Ϲ�������ôScrollView���Թ���
			if (isBottom() && (curP.y - lastY < 0))
				isScroll = true;
			if (getScrollY() == 0)// �������������ٻ�
				isScroll = false;
			if ((getScrollY() == 0) && (curP.y - lastY > 0))// ���������������»������Ի���
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
			return true;
		}
		return false;
	}
}
