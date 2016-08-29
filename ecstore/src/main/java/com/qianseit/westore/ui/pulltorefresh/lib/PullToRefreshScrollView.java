package com.qianseit.westore.ui.pulltorefresh.lib;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.qianseit.westore.ui.FlowScrollView;
import cn.shopex.ecstore.R;

public class PullToRefreshScrollView extends PullToRefreshBase<ScrollView> {

	public PullToRefreshScrollView(Context context) {
		super(context);
	}

	public PullToRefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshScrollView(Context context, int mode) {
		super(context, mode);
	}

	@Override
	protected ScrollView createRefreshableView(Context context,
			AttributeSet attrs) {
		FlowScrollView scrollView;
		scrollView = new FlowScrollView(context, attrs);

		scrollView.setId(R.id.scrollview);
		return scrollView;
	}

	@Override
	protected boolean isReadyForPullDown() {
		return refreshableView.getScrollY() == 0;
	}

	@Override
	protected boolean isReadyForPullUp() {
		int childCount = refreshableView.getChildCount();
		if (childCount == 0)
			return false;

		View scrollViewChild = refreshableView.getChildAt(childCount - 1);
		if (null != scrollViewChild)
			return refreshableView.getScrollY() >= scrollViewChild.getTop();

		return false;
	}
}