package com.qianseit.westore.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import cn.shopex.ecstore.R;
import com.qianseit.westore.Run;

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
public class FlowView extends AdapterView<Adapter> {
	private final int TAG_GAP = 10011;

	private final int SNAP_VELOCITY = 600;
	private final int INVALID_SCREEN = -1;
	private final int TOUCH_STATE_REST = 0;
	private final int TOUCH_STATE_SCROLLING = 1;

	private FlowScrollView parentScrollView;

	private int mTouchSlop;
	private float mLastMotionX;
	private int mCurrentScreen;
	private int mLastScreen = -1;
	private int mMaximumVelocity;
	private int mNextScreen = INVALID_SCREEN;
	private int mTouchState = TOUCH_STATE_REST;

	private Adapter mAdapter;
	private Scroller mScroller;
	private FlowIndicator mIndicator;
	private VelocityTracker mVelocityTracker;
	private AdapterDataSetObserver mDataSetObserver;
	private ViewSwitchListener mViewSwitchListener;

	// view之间的分隔线
	private int mPagerMarginWidth = 0;
	private int mPagerMarginColor = Color.TRANSPARENT;

	// 预估的View宽度，用于View未绘制时候计算偏移量
	private int mMearsureWidth = 0;

	private int mLastScrollDirection;
	private int mLastOrientation = -1;
	private int mDefaultSelection = 0;
	private boolean mFirstLayout = true;
	private boolean mCanScrollable = true;
	private boolean isMask = true;

	private OnGlobalLayoutListener orientationChangeListener = new OnGlobalLayoutListener() {

		public void onGlobalLayout() {
			getViewTreeObserver().removeGlobalOnLayoutListener(
					orientationChangeListener);
			setSelection(mCurrentScreen);
		}
	};


	/**
	 * Receives call backs when a new {@link View} has been scrolled to.
	 */
	public static interface ViewSwitchListener {

		/**
		 * This method is called when a new View has been scrolled to.
		 * 
		 * @param view
		 *            the {@link View} currently in focus.
		 * @param position
		 *            The position in the adapter of the {@link View} currently
		 *            in focus.
		 */
		void onSwitched(int position);
	}

	public FlowView(Context context) {
		super(context);
		init();
	}

	public FlowView(Context context, int sideBuffer) {
		super(context);
		init();
	}

	public FlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.FlowView);
		mPagerMarginWidth = (int) ta.getDimensionPixelSize(
				R.styleable.FlowView_pagerMarginWidth, 0);
		mPagerMarginColor = ta.getColor(R.styleable.FlowView_pagerMarginColor,
				Color.TRANSPARENT);
		ta.recycle();
		init();
	}

	private void init() {
		mScroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != mLastOrientation) {
			mLastOrientation = newConfig.orientation;
			getViewTreeObserver().addOnGlobalLayoutListener(
					orientationChangeListener);
		}
	}

	/**
	 * 设置每页之间的分隔线宽度
	 * 
	 * @param mPagerMarginWidth
	 */
	public void setPagerMarginWidth(int mPagerMarginWidth) {
		this.mPagerMarginWidth = mPagerMarginWidth;
	}

	/**
	 * 获取每页之间的分隔线宽度
	 * 
	 * @param mPagerMarginWidth
	 */
	public int getPagerMarginWidth() {
		return mPagerMarginWidth;
	}

	public FlowIndicator getFlowIndicator() {
		return mIndicator;
	}

	/**
	 * 设置每页之间的分隔线颜色
	 * 
	 * @param mPagerMarginColor
	 */
	public void setPagerMarginColor(int mPagerMarginColor) {
		this.mPagerMarginColor = mPagerMarginColor;
	}

	/**
	 * 获取每页之间的分隔线颜色
	 * 
	 * @param mPagerMarginColor
	 */
	public int getPagerMarginColor() {
		return mPagerMarginColor;
	}
	
	public void setMaskParentOntouch(boolean isMask){
		this.isMask = isMask;
	}

	/**
	 * 设置默认选中的页面
	 * 
	 * @param mDefaultSelection
	 */
	public void setDefaultSelection(int mDefaultSelection) {
		this.mDefaultSelection = mDefaultSelection;
	}

	public int getViewsCount() {
		if (mAdapter == null)
			return 0;
		return mAdapter.getCount();
	}

	// 重载FlowView的Adapter
	public void reloadFlowAdapter() {
		if (mAdapter != null && (mAdapter instanceof BaseAdapter)) {
			((BaseAdapter) mAdapter).notifyDataSetChanged();
		}
	}

	// 重载所有AdapterView
	public void reloadAllAdapterView() {
		for (int i = 0, c = getViewsCount(); i < c; i++) {
			reloadAdapterViewAt(i);
		}
	}

	// 重载某个AdapterView
	public void reloadAdapterViewAt(int position) {
		if (position > -1 && position < getChildCount()) {
			View view = getChildAt(position);
			reloadAdapterView(view);
		}
	}

	// 重载AdapterView
	private void reloadAdapterView(View v) {
		if (v instanceof ExpandableListView) {
			// 通知adapter更改
			((BaseExpandableListAdapter) ((ExpandableListView) v)
					.getExpandableListAdapter()).notifyDataSetChanged();
		} else if (v instanceof AdapterView) {
			Object adapter = ((AdapterView) v).getAdapter();
			// ListView的Adapter为HeaderViewListAdapter
			if (adapter instanceof HeaderViewListAdapter)
				adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
			// 通知adapter更改
			if (adapter instanceof BaseAdapter)
				((BaseAdapter) adapter).notifyDataSetChanged();
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			for (int i = 0, c = vg.getChildCount(); i < c; i++) {
				reloadAdapterView(vg.getChildAt(i));
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mPagerMarginWidth <= 0)
			return;

		// 绘制页之间的分割线
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(mPagerMarginColor);
		int width = getWidth(), height = getHeight();
		for (int i = 1; i < getChildCount(); i++) {
			int right = i * (width + mPagerMarginWidth);
			int left = right - mPagerMarginWidth;
			canvas.drawRect(new Rect(left, 0, right, height), paint);
		}
	}

	@Override
	public boolean isInEditMode() {
		return true;
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);

		mMearsureWidth = MeasureSpec.getSize(widthSpec);
		final int widthMode = MeasureSpec.getMode(widthSpec);
		if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightSpec);
		if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			child.measure(widthSpec, heightSpec);
		}

		if (mFirstLayout) {
			int newX = getDestScrollX(mCurrentScreen);
			mScroller.startScroll(0, 0, newX, 0, 0);
			mFirstLayout = false;
		}
	}

	/**
	 * 计算到达该屏需要滚动的距离
	 * 
	 * @param whichScreen
	 */
	private int getDestScrollX(int whichScreen) {
		// 未计算出width时，先使用预估的width
		int viewWidth = (getWidth() == 0) ? mMearsureWidth : getWidth();
		int gapWidth = whichScreen * mPagerMarginWidth;
		int newX = whichScreen * viewWidth + gapWidth;
		return newX;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += (childWidth + mPagerMarginWidth);
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0 || !mCanScrollable)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;
			Run.log("mTouchSlop:", mTouchSlop, "  xDiff:", xDiff);

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
				if (parentScrollView != null)
					parentScrollView.setCanScroll(false);
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(
							getChildCount() - 1).getRight()
							- scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0 || !mCanScrollable)
			return false;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		getParent().requestDisallowInterceptTouchEvent(isMask);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			// Remember where the motion event started
			mLastMotionX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			boolean xMoved = xDiff > mTouchSlop;

			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
				if (parentScrollView != null)
					parentScrollView.setCanScroll(false);
			}

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(
							getChildCount() - 1).getRight()
							- scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			if (parentScrollView != null)
				parentScrollView.setCanScroll(true);
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				Run.log("velocityX:", velocityX);
				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}

			mTouchState = TOUCH_STATE_REST;

			break;
		case MotionEvent.ACTION_CANCEL:
			snapToDestination();
			mTouchState = TOUCH_STATE_REST;
		}
		return true;
	}

	@Override
	public void onScrollChanged(int h, int v, int oldh, int oldv) {
		super.onScrollChanged(h, v, oldh, oldv);
		if (mIndicator != null) {
			/*
			 * The actual horizontal scroll origin does typically not match the
			 * perceived one. Therefore, we need to calculate the perceived
			 * horizontal scroll origin here, since we use a view buffer.
			 */
			int hPerceived = mCurrentScreen * getWidth();
			mIndicator.onScrolled(hPerceived, v, oldh, oldv);
		}
	}

	/**
	 * 缓慢滑动到指定页面
	 * 
	 * @param whichScreen
	 * @return 是否可以滑动，当目标页码等于当前页码则返回false
	 */
	public boolean smoothScrollToScreen(int whichScreen) {
		if (whichScreen == mCurrentScreen)
			return false;

		snapToScreen(whichScreen);
		return true;
	}

	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2))
				/ screenWidth;

		snapToScreen(whichScreen);
	}

	private void snapToScreen(int whichScreen) {
		mLastScrollDirection = whichScreen - mCurrentScreen;
		if (!mScroller.isFinished())
			return;

		// 不能超过总页数
		whichScreen = Math.max(0, whichScreen);
		whichScreen = Math.min(whichScreen, getChildCount() - 1);
		mNextScreen = whichScreen;

		// 滚屏进行翻页
		setCanScrollable(false);

		int delta = getDestScrollX(whichScreen) - getScrollX();
		int duration = Math.min(Math.abs(delta), 1000);
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			mLastScreen = mCurrentScreen;
			mCurrentScreen = Math.max(0,
					Math.min(mNextScreen, getChildCount() - 1));
			mNextScreen = INVALID_SCREEN;
			postViewSwitched(mLastScrollDirection);
		}
	}

	/**
	 * Scroll to the {@link View} in the view buffer specified by the index.
	 * 
	 * @param indexInBuffer
	 *            Index of the view in the view buffer.
	 */
	public void setVisibleView(int indexInBuffer, boolean uiThread) {
		mCurrentScreen = Math.max(0,
				Math.min(indexInBuffer, getChildCount() - 1));
		// 修正滚动发生偏移的情况，强制滚到正确的距离
		int curX = mScroller.getCurrX(), curY = mScroller.getCurrY();
		int dx = getDestScrollX(mCurrentScreen) - curX;
		mScroller.startScroll(curX, curY, dx, 0, 0);
		if (dx == 0)
			onScrollChanged(curX + dx, curY, curX + dx, curY);
		if (uiThread)
			invalidate();
		else
			postInvalidate();
	}

	/**
	 * Set the listener that will receive notifications every time the {code
	 * ViewFlow} scrolls.
	 * 
	 * @param l
	 *            the scroll listener
	 */
	public void setOnViewSwitchListener(ViewSwitchListener l) {
		mViewSwitchListener = l;
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		setAdapter(adapter, mDefaultSelection);
	}

	public void setAdapter(Adapter adapter, int initialPosition) {
		if (mAdapter != null)
			mAdapter.unregisterDataSetObserver(mDataSetObserver);

		mAdapter = adapter;
		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);
		}

		if (mAdapter == null || mAdapter.getCount() == 0)
			return;

		// 插入视图
		removeAllViewsInLayout();
		for (int i = 0, c = mAdapter.getCount(); i < c; i++)
			makeAndAddView(i, true, null);
		setSelection(initialPosition);
	}

	@Override
	public View getSelectedView() {
		return getChildAt(mCurrentScreen);
	}

	@Override
	public int getSelectedItemPosition() {
		return mCurrentScreen;
	}

	// 获得指定屏的View
	public View getViewAtScreen(int screen) {
		return getChildAt(screen);
	}

	/**
	 * Set the FlowIndicator
	 * 
	 * @param flowIndicator
	 */
	public void setFlowIndicator(FlowIndicator flowIndicator) {
		mIndicator = flowIndicator;
		mIndicator.setViewFlow(this);
	}

	public void setParentScrollView(FlowScrollView parentScrollView) {
		this.parentScrollView = parentScrollView;
	}

	@Override
	public void setSelection(int position) {
		mLastScreen = mCurrentScreen;
		mNextScreen = INVALID_SCREEN;
		mScroller.forceFinished(true);
		if (mAdapter == null)
			return;

		position = Math.max(position, 0);
		position = Math.min(position, mAdapter.getCount() - 1);

		this.requestLayout();
		setVisibleView(position, false);

		// 通知当前页发生改变
		if (mIndicator != null && mCurrentScreen != mLastScreen)
			mIndicator.onSwitched(mCurrentScreen);
		if (mViewSwitchListener != null && mCurrentScreen != mLastScreen)
			mViewSwitchListener.onSwitched(mCurrentScreen);
	}

	/**
	 * 是否可以滑动翻页
	 * 
	 * @param mCanScrollable
	 */
	public void setCanScrollable(boolean mCanScrollable) {
		this.mCanScrollable = mCanScrollable;
	}

	public boolean getCanScrollable() {
		return mCanScrollable;
	}

	/**
	 * 通知选中某页
	 * 
	 * @param direction
	 */
	private void postViewSwitched(int direction) {
		setCanScrollable(true);

		if (direction == 0)
			return;

		requestLayout();
		setVisibleView(mCurrentScreen, true);

		// 通知当前页发生改变
		if (mIndicator != null && mCurrentScreen != mLastScreen)
			mIndicator.onSwitched(mCurrentScreen);
		if (mViewSwitchListener != null && mCurrentScreen != mLastScreen)
			mViewSwitchListener.onSwitched(mCurrentScreen);
	}

	private View setupChild(View child, boolean addToEnd, boolean recycle) {
		ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child
				.getLayoutParams();
		if (p == null)
			p = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT, 0);

		if (recycle) {
			attachViewToParent(child, (addToEnd ? -1 : 0), p);
		} else {
			addViewInLayout(child, (addToEnd ? -1 : 0), p, true);
		}
		return child;
	}

	// 构造、添加FlowView的子View
	private View makeAndAddView(int position, boolean addToEnd, View convertView) {
		View view = mAdapter.getView(position, convertView, this);
		return setupChild(view, addToEnd, convertView != null);
	}

	private class AdapterDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			removeAllViewsInLayout();
			// 插入视图
			for (int i = 0, c = mAdapter.getCount(); i < c; i++)
				makeAndAddView(i, true, null);
			setSelection(mDefaultSelection);
		}

		@Override
		public void onInvalidated() {
		}
	}

	private void logBuffer() {
		Log.d("viewflow", "current screen: " + mCurrentScreen);
	}
}
