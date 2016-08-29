package com.qianseit.westore.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import cn.shopex.ecstore.R;
import com.qianseit.westore.util.Util;

/**
 * 如果要接收屏幕旋转的通知，则在Application代理中加入以下代码<br />
 * public void onConfigurationChanged(Configuration newConfig) {
 * super.onConfigurationChanged(newConfig);<br />
 * // 屏幕方向改变时候发出通知<br />
 * Intent intent = new Intent(RI.ACTION_SCREEN_ORINET_CHANGED);<br />
 * intent.putExtra(RI.EXTRA_ORIENTATION, newConfig.orientation);<br />
 * RI.sendMyBroadcast(this, intent); <br />
 * }<br />
 * 
 * A ListPopupWindow anchors itself to a host view and displays a list of
 * choices.
 * 
 * <p>
 * ListPopupWindow contains a number of tricky behaviors surrounding
 * positioning, scrolling parents to fit the dropdown, interacting sanely with
 * the IME if present, and others.
 * 
 * @see android.widget.AutoCompleteTextView
 * @see android.widget.Spinner
 */
public class ListPopupWindow implements OnDismissListener {
	private Context mContext;
	private PopupWindow mPopup;
	private ListAdapter mAdapter;
	private DropDownListView mDropDownList;

	private boolean mDropDownAlwaysVisible = false;
	private boolean mForceIgnoreOutsideTouch = false;
	int mListItemExpandMaximum = Integer.MAX_VALUE;

	private View mPromptView;
	public final int POSITION_PROMPT_ABOVE = 0;
	private int mPromptPosition = POSITION_PROMPT_ABOVE;

	private DataSetObserver mObserver;

	private View mDropDownAnchorView;

	private OnDismissListener mDismissListener;
	private AdapterView.OnItemClickListener mItemClickListener;
	private AdapterView.OnItemSelectedListener mItemSelectedListener;

	private final ResizePopupRunnable mResizePopupRunnable = new ResizePopupRunnable();
	private final PopupTouchInterceptor mTouchInterceptor = new PopupTouchInterceptor();
	private final PopupScrollListener mScrollListener = new PopupScrollListener();
	private final ListSelectorHider mHideSelector = new ListSelectorHider();
	private Runnable mShowDropDownRunnable;

	private Handler mHandler = new Handler();
	private Rect mTempRect = new Rect();

	private boolean mModal;
	private int mOrient = -1;
	private int mPopupMaxWidth;
	private boolean mReShow = false;

	private Drawable mListBackground;
	private Drawable mDividerDrawable;

	/**
	 * The provided prompt view should appear above list content.
	 * 
	 * @see #setPromptPosition(int)
	 * @see #getPromptPosition()
	 * @see #setPromptView(View)
	 */

	/**
	 * The provided prompt view should appear below list content.
	 * 
	 * @see #setPromptPosition(int)
	 * @see #getPromptPosition()
	 * @see #setPromptView(View)
	 */
	public final int POSITION_PROMPT_BELOW = 1;

	/**
	 * Alias for {@link ViewGroup.LayoutParams#MATCH_PARENT}. If used to specify
	 * a popup width, the popup will match the width of the anchor view. If used
	 * to specify a popup height, the popup will fill available space.
	 */
	public final int MATCH_PARENT = ViewGroup.LayoutParams.FILL_PARENT;

	/**
	 * Alias for {@link ViewGroup.LayoutParams#WRAP_CONTENT}. If used to specify
	 * a popup width, the popup will use the width of its content.
	 */
	public final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 */
	public ListPopupWindow(Context context) {
		this(context, null, 0);
	}

	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 * @param attrs
	 *            Attributes from inflating parent views used to style the
	 *            popup.
	 */
	public ListPopupWindow(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 * @param attrs
	 *            Attributes from inflating parent views used to style the
	 *            popup.
	 * @param defStyleAttr
	 *            Style attribute to read for default styling of popup content.
	 * @param defStyleRes
	 *            Style resource ID to use for default styling of popup content.
	 */
	public ListPopupWindow(Context context, AttributeSet attrs, int defStyleRes) {
		mContext = context;
		mPopup = new PopupWindow(context, attrs, defStyleRes);
		mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

		// 弹出层的最大宽度
		Resources res = mContext.getResources();
		mPopupMaxWidth = Math.max(res.getDisplayMetrics().widthPixels / 2,
				res.getDimensionPixelSize(R.dimen.menu_popup_maxwidth));
		mListBackground = res
				.getDrawable(R.drawable.popupwindow_full_holo_light);
		mDividerDrawable = res
				.getDrawable(R.drawable.popupwindow_divider_holo_light);
	}

	/**
	 * Sets the adapter that provides the data and the views to represent the
	 * data in this popup window.
	 * 
	 * @param adapter
	 *            The adapter to use to create this window's content.
	 */
	public void setAdapter(ListAdapter adapter) {
		if (mObserver == null) {
			mObserver = new PopupDataSetObserver();
		} else if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mObserver);
		}
		mAdapter = adapter;
		if (mAdapter != null) {
			adapter.registerDataSetObserver(mObserver);
		}

		if (mDropDownList != null) {
			mDropDownList.setAdapter(mAdapter);
		}
	}

	public void setListBackground(int mListBackground) {
		this.mListBackground = mContext.getResources().getDrawable(
				mListBackground);
	}

	public void setListBackground(Drawable mListBackground) {
		this.mListBackground = mListBackground;
	}

	public void setDividerDrawable(int mDividerDrawable) {
		this.mDividerDrawable = mContext.getResources().getDrawable(
				mDividerDrawable);
	}

	public void setDividerDrawable(Drawable mDividerDrawable) {
		this.mDividerDrawable = mDividerDrawable;
	}

	/**
	 * Set where the optional prompt view should appear. The default is
	 * {@link #POSITION_PROMPT_ABOVE}.
	 * 
	 * @param position
	 *            A position constant declaring where the prompt should be
	 *            displayed.
	 * 
	 * @see #POSITION_PROMPT_ABOVE
	 * @see #POSITION_PROMPT_BELOW
	 */
	public void setPromptPosition(int position) {
		mPromptPosition = position;
	}

	/**
	 * @return Where the optional prompt view should appear.
	 * 
	 * @see #POSITION_PROMPT_ABOVE
	 * @see #POSITION_PROMPT_BELOW
	 */
	public int getPromptPosition() {
		return mPromptPosition;
	}

	/**
	 * Set whether this window should be modal when shown.
	 * 
	 * <p>
	 * If a popup window is modal, it will receive all touch and key input. If
	 * the user touches outside the popup window's content area the popup window
	 * will be dismissed.
	 * 
	 * @param modal
	 *            {@code true} if the popup window should be modal,
	 *            {@code false} otherwise.
	 */
	public void setModal(boolean modal) {
		mModal = true;
		mPopup.setFocusable(modal);
	}

	/**
	 * Returns whether the popup window will be modal when shown.
	 * 
	 * @return {@code true} if the popup window will be modal, {@code false}
	 *         otherwise.
	 */
	public boolean isModal() {
		return mModal;
	}

	/**
	 * Forces outside touches to be ignored. Normally if
	 * {@link #isDropDownAlwaysVisible()} is false, we allow outside touch to
	 * dismiss the dropdown. If this is set to true, then we ignore outside
	 * touch even when the drop down is not set to always visible.
	 * 
	 * @hide Used only by AutoCompleteTextView to handle some internal special
	 *       cases.
	 */
	public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
		mForceIgnoreOutsideTouch = forceIgnoreOutsideTouch;
	}

	/**
	 * Sets whether the drop-down should remain visible under certain
	 * conditions.
	 * 
	 * The drop-down will occupy the entire screen below {@link #getAnchorView}
	 * regardless of the size or content of the list. {@link #getBackground()}
	 * will fill any space that is not used by the list.
	 * 
	 * @param dropDownAlwaysVisible
	 *            Whether to keep the drop-down visible.
	 * 
	 * @hide Only used by AutoCompleteTextView under special conditions.
	 */
	public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
		mDropDownAlwaysVisible = dropDownAlwaysVisible;
	}

	/**
	 * @return Whether the drop-down is visible under special conditions.
	 * 
	 * @hide Only used by AutoCompleteTextView under special conditions.
	 */
	public boolean isDropDownAlwaysVisible() {
		return mDropDownAlwaysVisible;
	}

	/**
	 * Sets the operating mode for the soft input area.
	 * 
	 * @param mode
	 *            The desired mode, see
	 *            {@link android.view.WindowManager.LayoutParams#softInputMode}
	 *            for the full list
	 * 
	 * @see android.view.WindowManager.LayoutParams#softInputMode
	 * @see #getSoftInputMode()
	 */
	public void setSoftInputMode(int mode) {
		mPopup.setSoftInputMode(mode);
	}

	/**
	 * Returns the current value in {@link #setSoftInputMode(int)}.
	 * 
	 * @see #setSoftInputMode(int)
	 * @see android.view.WindowManager.LayoutParams#softInputMode
	 */
	public int getSoftInputMode() {
		return mPopup.getSoftInputMode();
	}

	/**
	 * @return The background drawable for the popup window.
	 */
	public Drawable getBackground() {
		return mPopup.getBackground();
	}

	/**
	 * Sets a drawable to be the background for the popup window.
	 * 
	 * @param d
	 *            A drawable to set as the background.
	 */
	// public void setBackgroundDrawable(Drawable d) {
	// mPopup.setBackgroundDrawable(d);
	// }

	/**
	 * Set an animation style to use when the popup window is shown or
	 * dismissed.
	 * 
	 * @param animationStyle
	 *            Animation style to use.
	 */
	public void setAnimationStyle(int animationStyle) {
		mPopup.setAnimationStyle(animationStyle);
	}

	/**
	 * Returns the animation style that will be used when the popup window is
	 * shown or dismissed.
	 * 
	 * @return Animation style that will be used.
	 */
	public int getAnimationStyle() {
		return mPopup.getAnimationStyle();
	}

	/**
	 * Returns the view that will be used to anchor this popup.
	 * 
	 * @return The popup's anchor view
	 */
	public View getAnchorView() {
		return mDropDownAnchorView;
	}

	/**
	 * Sets the popup's anchor view. This popup will always be positioned
	 * relative to the anchor view when shown.
	 * 
	 * @param anchor
	 *            The view to use as an anchor.
	 */
	public void setAnchorView(View anchor) {
		mDropDownAnchorView = anchor;
	}

	/**
	 * Sets a listener to receive events when a list item is clicked.
	 * 
	 * @param clickListener
	 *            Listener to register
	 * 
	 * @see ListView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)
	 */
	public void setOnItemClickListener(
			AdapterView.OnItemClickListener clickListener) {
		mItemClickListener = clickListener;
	}

	/**
	 * Sets a listener to receive events when a list item is selected.
	 * 
	 * @param selectedListener
	 *            Listener to register.
	 * 
	 * @see ListView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)
	 */
	public void setOnItemSelectedListener(
			AdapterView.OnItemSelectedListener selectedListener) {
		mItemSelectedListener = selectedListener;
	}

	/**
	 * Set a view to act as a user prompt for this popup window. Where the
	 * prompt view will appear is controlled by {@link #setPromptPosition(int)}.
	 * 
	 * @param prompt
	 *            View to use as an informational prompt.
	 */
	public void setPromptView(View prompt) {
		boolean showing = isShowing();
		if (showing) {
			removePromptView();
		}
		mPromptView = prompt;
		if (showing) {
			show();
		}
	}

	/**
	 * Post a {@link #show()} call to the UI thread.
	 */
	public void postShow() {
		mHandler.post(mShowDropDownRunnable);
	}

	/**
	 * Show the popup list. If the list is already showing, this method will
	 * recalculate the popup's size and position.
	 */
	public void show() {
		if (!mPopup.isShowing()) {
			buildDropDown();

			// 以此View为中心显示
			View anchor = getAnchorView();
			int[] location = new int[2];
			anchor.getLocationOnScreen(location);
			Rect anchorRect = new Rect(location[0], location[1], location[0]
					+ anchor.getWidth(), location[1] + anchor.getHeight());
			int xPos = anchorRect.left, yPos = anchorRect.bottom;

			// 屏幕大小
			DisplayMetrics metric = mContext.getResources().getDisplayMetrics();
			int screenWidth = metric.widthPixels, screenHeight = metric.heightPixels;
			int margin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 30, metric);

			// anchor底部以下空间高度
			int dyBottom = screenHeight - anchorRect.bottom;
			boolean onTop = anchorRect.top > dyBottom;
			int maxPopHeight = (onTop ? anchorRect.top
					: (screenHeight - anchorRect.bottom)) - margin;

			mPopup.setOnDismissListener(this);
			mDropDownList.setVerticalScrollBarEnabled(false);
			mDropDownList.measure(screenWidth, maxPopHeight);
			int measureHeight = mDropDownList.getMeasuredHeight();
			int measureWidth = measureContentWidth(mAdapter);
			// 计算popwindow的大小、动画
			mPopup.setBackgroundDrawable(new BitmapDrawable());
			mPopup.setHeight(Math.min(maxPopHeight, measureHeight));
			mPopup.setWidth(Math.min(measureWidth, mPopupMaxWidth));
			mPopup.setAnimationStyle(onTop ? R.style.menu_animations_popup
					: R.style.menu_animations_popdown);

			LayoutParams params = mDropDownList.getLayoutParams();
			params.width = Math.min(measureWidth, mPopupMaxWidth);
			params.height = mPopup.getHeight();
			mDropDownList.setLayoutParams(params);

			// 自动适应popwindow的显示位置
			int sideMargin = mContext.getResources().getDimensionPixelSize(
					R.dimen.PaddingMedium);
			yPos = onTop ? anchorRect.top - mPopup.getHeight()
					: anchorRect.bottom;
			if ((anchorRect.left + mPopup.getWidth() + sideMargin) >= screenWidth
					&& anchorRect.right > mPopup.getWidth()) {
				// popwindow靠anchor的右边显示
				xPos = anchorRect.right - mPopup.getWidth() - sideMargin;
			} else if (anchorRect.centerX() > mPopup.getWidth() / 2) {
				// popwindow从anchor的中间显示
				xPos = anchorRect.left + anchorRect.width() / 2
						- mPopup.getWidth() / 2;
			}

			// 设置可点击popupwindow内容区域外的视图隐藏popupwindow
			mPopup.setTouchInterceptor(mTouchInterceptor);
			mPopup.setOutsideTouchable(!mForceIgnoreOutsideTouch
					&& !mDropDownAlwaysVisible);
			mDropDownList.setSelection(ListView.INVALID_POSITION);
			mPopup.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

			if (!mModal || mDropDownList.isInTouchMode())
				clearListSelection();

			if (!mModal)
				mHandler.post(mHideSelector);

			// 注册屏幕旋转的监听器
			IntentFilter filter = new IntentFilter();
			filter.addAction(Util.ACTION_SCREEN_ORINET_CHANGED);
			mContext.registerReceiver(mReceiver, filter);
		}
	}

	/**
	 * Dismiss the popup window.
	 */
	public void dismiss() {
		Util.unregistReceiverSafety(mContext, mReceiver);

		mPopup.dismiss();
		removePromptView();
		mPopup.setContentView(null);
		mDropDownList = null;
		mHandler.removeCallbacks(mResizePopupRunnable);
	}

	@Override
	public void onDismiss() {
		if (mReShow) {
			mReShow = false;
			Runnable mShowRunnable = new ShowPopupRunnable();
			mHandler.postDelayed(mShowRunnable, 200);
		} else if (mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}

	/**
	 * Set a listener to receive a callback when the popup is dismissed.
	 * 
	 * @param listener
	 *            Listener that will be notified when the popup is dismissed.
	 */
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		mDismissListener = listener;
	}

	private void removePromptView() {
		if (mPromptView != null) {
			final ViewParent parent = mPromptView.getParent();
			if (parent instanceof ViewGroup) {
				final ViewGroup group = (ViewGroup) parent;
				group.removeView(mPromptView);
			}
		}
	}

	/**
	 * calc max width of ListAdapter child view
	 * 
	 * @param adapter
	 * @return
	 */
	private int measureContentWidth(ListAdapter adapter) {
		// Menus don't tend to be long, so this is more sane than it looks.
		View itemView = null;
		int width = 0, itemType = 0;
		FrameLayout mMeasureParent = new FrameLayout(mContext);
		final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}

			itemView = adapter.getView(i, itemView, mMeasureParent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}
		return width;
	}

	/**
	 * Set the selected position of the list. Only valid when
	 * {@link #isShowing()} == {@code true}.
	 * 
	 * @param position
	 *            List position to set as selected.
	 */
	public void setSelection(int position) {
		DropDownListView list = mDropDownList;
		if (isShowing() && list != null) {
			list.mListSelectionHidden = false;
			list.setSelection(position);
			if (list.getChoiceMode() != ListView.CHOICE_MODE_NONE) {
				list.setItemChecked(position, true);
			}
		}
	}

	/**
	 * Clear any current list selection. Only valid when {@link #isShowing()} ==
	 * {@code true}.
	 */
	public void clearListSelection() {
		final DropDownListView list = mDropDownList;
		if (list != null) {
			list.mListSelectionHidden = true;
			list.requestLayout();
		}
	}

	/**
	 * @return {@code true} if the popup is currently showing, {@code false}
	 *         otherwise.
	 */
	public boolean isShowing() {
		return mPopup.isShowing();
	}

	/**
	 * Perform an item click operation on the specified list adapter position.
	 * 
	 * @param position
	 *            Adapter position for performing the click
	 * @return true if the click action could be performed, false if not. (e.g.
	 *         if the popup was not showing, this method would return false.)
	 */
	public boolean performItemClick(int position) {
		if (isShowing()) {
			if (mItemClickListener != null) {
				final DropDownListView list = mDropDownList;
				final View child = list.getChildAt(position
						- list.getFirstVisiblePosition());
				final ListAdapter adapter = list.getAdapter();
				mItemClickListener.onItemClick(list, child, position,
						adapter.getItemId(position));
			}
			return true;
		}
		return false;
	}

	/**
	 * @return The currently selected item or null if the popup is not showing.
	 */
	public Object getSelectedItem() {
		if (!isShowing()) {
			return null;
		}
		return mDropDownList.getSelectedItem();
	}

	/**
	 * @return The position of the currently selected item or
	 *         {@link ListView#INVALID_POSITION} if {@link #isShowing()} ==
	 *         {@code false}.
	 * 
	 * @see ListView#getSelectedItemPosition()
	 */
	public int getSelectedItemPosition() {
		if (!isShowing()) {
			return ListView.INVALID_POSITION;
		}
		return mDropDownList.getSelectedItemPosition();
	}

	/**
	 * @return The ID of the currently selected item or
	 *         {@link ListView#INVALID_ROW_ID} if {@link #isShowing()} ==
	 *         {@code false}.
	 * 
	 * @see ListView#getSelectedItemId()
	 */
	public long getSelectedItemId() {
		if (!isShowing()) {
			return ListView.INVALID_ROW_ID;
		}
		return mDropDownList.getSelectedItemId();
	}

	/**
	 * @return The View for the currently selected item or null if
	 *         {@link #isShowing()} == {@code false}.
	 * 
	 * @see ListView#getSelectedView()
	 */
	public View getSelectedView() {
		if (!isShowing()) {
			return null;
		}
		return mDropDownList.getSelectedView();
	}

	/**
	 * @return The {@link ListView} displayed within the popup window. Only
	 *         valid when {@link #isShowing()} == {@code true}.
	 */
	public ListView getListView() {
		return mDropDownList;
	}

	/**
	 * The maximum number of list items that can be visible and still have the
	 * list expand when touched.
	 * 
	 * @param max
	 *            Max number of items that can be visible and still allow the
	 *            list to expand.
	 */
	void setListItemExpandMax(int max) {
		mListItemExpandMaximum = max;
	}

	/**
	 * <p>
	 * Builds the popup window's content and returns the height the popup should
	 * have. Returns -1 when the content already exists.
	 * </p>
	 * 
	 * @return the content's height or -1 if content already exists
	 */
	private int buildDropDown() {
		ViewGroup dropDownView;
		int otherHeights = 0;

		if (mDropDownList == null) {
			Context context = mContext;

			mShowDropDownRunnable = new Runnable() {
				public void run() {
					View view = getAnchorView();
					if (view != null && view.getWindowToken() != null)
						show();
				}
			};

			mDropDownList = new DropDownListView(context, !mModal);
			mDropDownList.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mDropDownList.setSelector(R.drawable.transparent);
			mDropDownList.setBackgroundDrawable(mListBackground);
			mDropDownList.setDivider(mDividerDrawable);
			mDropDownList.setDividerHeight(1);
			mDropDownList.setFocusable(true);
			mDropDownList.setAdapter(mAdapter);
			mDropDownList.setFocusableInTouchMode(true);
			mDropDownList.setOnScrollListener(mScrollListener);
			mDropDownList.setOnItemClickListener(mItemClickListener);
			if (mItemSelectedListener != null)
				mDropDownList.setOnItemSelectedListener(mItemSelectedListener);
			mDropDownList
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							if (position != -1) {
								DropDownListView dropDownList = mDropDownList;
								if (dropDownList != null)
									dropDownList.mListSelectionHidden = false;
							}
						}

						public void onNothingSelected(AdapterView<?> parent) {
						}
					});

			dropDownView = mDropDownList;

			View hintView = mPromptView;
			if (hintView != null) {
				// 在DropdownListview下面加描述文字
				LinearLayout hintContainer = new LinearLayout(context);
				hintContainer.setOrientation(LinearLayout.VERTICAL);

				LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
						MATCH_PARENT, 0, 1.0f);

				switch (mPromptPosition) {
				case POSITION_PROMPT_BELOW:
					hintContainer.addView(dropDownView, hintParams);
					hintContainer.addView(hintView);
					break;
				case POSITION_PROMPT_ABOVE:
					hintContainer.addView(hintView);
					hintContainer.addView(dropDownView, hintParams);
					break;
				}

				// measure the hint's height to find how much more vertical
				// space
				// we need to add to the drop down's height
				int widthSpec = MeasureSpec.makeMeasureSpec(WRAP_CONTENT,
						MeasureSpec.AT_MOST);
				int heightSpec = MeasureSpec.UNSPECIFIED;
				hintView.measure(widthSpec, heightSpec);

				hintParams = (LinearLayout.LayoutParams) hintView
						.getLayoutParams();
				otherHeights = hintView.getMeasuredHeight()
						+ hintParams.topMargin + hintParams.bottomMargin;
				dropDownView = hintContainer;
			} else {
				// 在DropdownListview下面加描述文字
				LinearLayout hintContainer = new LinearLayout(context);
				hintContainer.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
						0, LayoutParams.MATCH_PARENT);
				hintContainer.addView(dropDownView, hintParams);
				dropDownView = hintContainer;
			}

			mPopup.setContentView(dropDownView);
		} else {
			dropDownView = (ViewGroup) mPopup.getContentView();
			final View view = mPromptView;
			if (view != null) {
				LinearLayout.LayoutParams hintParams = (LinearLayout.LayoutParams) view
						.getLayoutParams();
				otherHeights = view.getMeasuredHeight() + hintParams.topMargin
						+ hintParams.bottomMargin;
			}
		}

		// getMaxAvailableHeight() subtracts the padding, so we put it back
		// to get the available height for the whole window
		int padding = 0;
		Drawable background = mPopup.getBackground();
		if (background != null) {
			background.getPadding(mTempRect);
			padding = mTempRect.top + mTempRect.bottom;
		}

		final int listContent = mDropDownList.getMeasuredHeight();
		if (listContent > 0)
			otherHeights += padding;

		return listContent + otherHeights;
	}

	/**
	 * <p>
	 * Wrapper class for a ListView. This wrapper can hijack the focus to make
	 * sure the list uses the appropriate drawables and states when displayed on
	 * screen within a drop down. The focus is never actually passed to the drop
	 * down in this mode; the list only looks focused.
	 * </p>
	 */
	private static class DropDownListView extends ListView {
		private boolean mListSelectionHidden;

		/**
		 * True if this wrapper should fake focus.
		 */
		private boolean mHijackFocus;

		/**
		 * <p>
		 * Creates a new list view wrapper.
		 * </p>
		 * 
		 * @param context
		 *            this view's context
		 */
		public DropDownListView(Context context, boolean hijackFocus) {
			super(context, null, android.R.attr.dropDownListViewStyle);
			mHijackFocus = hijackFocus;
			setCacheColorHint(0);
		}

		@Override
		public boolean isInTouchMode() {
			return (mHijackFocus && mListSelectionHidden)
					|| super.isInTouchMode();
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			// widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthMeasureSpec,
			// MeasureSpec.UNSPECIFIED);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeasureSpec,
					MeasureSpec.AT_MOST);
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		/**
		 * <p>
		 * Returns the focus state in the drop down.
		 * </p>
		 * 
		 * @return true always if hijacking focus
		 */
		@Override
		public boolean hasWindowFocus() {
			return mHijackFocus || super.hasWindowFocus();
		}

		/**
		 * <p>
		 * Returns the focus state in the drop down.
		 * </p>
		 * 
		 * @return true always if hijacking focus
		 */
		@Override
		public boolean isFocused() {
			return mHijackFocus || super.isFocused();
		}

		/**
		 * <p>
		 * Returns the focus state in the drop down.
		 * </p>
		 * 
		 * @return true always if hijacking focus
		 */
		@Override
		public boolean hasFocus() {
			return mHijackFocus || super.hasFocus();
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Util.ACTION_SCREEN_ORINET_CHANGED.equals(intent.getAction())) {
				int orient = intent.getIntExtra(Util.EXTRA_ORIENTATION, -1);
				if (mOrient != orient) {
					mOrient = orient;

					// 变为水平方向不发生改变
					// if (mOrient == Configuration.ORIENTATION_PORTRAIT)
					// return;

					// 隐藏后不自动显示
					mReShow = false;
					dismiss();
				}
			}
		}
	};

	private class PopupDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			if (isShowing()) {
				// Resize the popup to fit new content
				show();
			}
		}

		@Override
		public void onInvalidated() {
			dismiss();
		}
	}

	private class ListSelectorHider implements Runnable {
		public void run() {
			clearListSelection();
		}
	}

	private class ResizePopupRunnable implements Runnable {
		public void run() {
			if (mDropDownList != null
					&& mDropDownList.getCount() > mDropDownList.getChildCount()
					&& mDropDownList.getChildCount() <= mListItemExpandMaximum) {
				mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
				show();
			}
		}
	}

	private class ShowPopupRunnable implements Runnable {
		public void run() {
			show();
		}
	}

	private class PopupTouchInterceptor implements OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				dismiss();
				return true;
			}
			return false;
		}
	}

	private class PopupScrollListener implements ListView.OnScrollListener {
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == SCROLL_STATE_TOUCH_SCROLL
					&& mPopup.getContentView() != null) {
				mHandler.removeCallbacks(mResizePopupRunnable);
				mResizePopupRunnable.run();
			}
		}
	}
}
