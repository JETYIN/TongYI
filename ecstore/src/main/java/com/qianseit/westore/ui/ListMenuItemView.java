package com.qianseit.westore.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListMenuItemView extends LinearLayout {
	private ActionMenuItem mItemData;

	private ImageView mIconView;
	private TextView mTitleView;

	private Context mTextAppearanceContext;
	private boolean mPreserveIconSpacing;
	private int mTextAppearance = -1;
	private int mHighlightColor = Color.TRANSPARENT;

	private LayoutInflater mInflater;

	private boolean mForceShowIcon;

	public ListMenuItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		// TypedArray a = context.obtainStyledAttributes(attrs,
		// android.R.styleable, defStyle, 0);
		//
		// mBackground = a
		// .getDrawable(com.android.internal.R.styleable.MenuView_itemBackground);
		// mTextAppearance = a.getResourceId(
		// com.android.internal.R.styleable.MenuView_itemTextAppearance,
		// -1);
		// mPreserveIconSpacing = a.getBoolean(
		// com.android.internal.R.styleable.MenuView_preserveIconSpacing,
		// false);
		mTextAppearanceContext = context;

		// a.recycle();
	}

	public ListMenuItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * 点击时候的颜色
	 * 
	 * @param mHighlightColor
	 */
	public void setHighlightColor(int mHighlightColor) {
		this.mHighlightColor = mHighlightColor;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mIconView = (ImageView) findViewById(android.R.id.icon);
		mTitleView = (TextView) findViewById(android.R.id.title);
		if (mTextAppearance != -1) {
			mTitleView.setTextAppearance(mTextAppearanceContext,
					mTextAppearance);
		}
	}

	public void initialize(ActionMenuItem itemData, int menuType) {
		mItemData = itemData;

		setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);

		setTitle(itemData.getTitle());
		setIcon(itemData.getIcon());
		setEnabled(itemData.isEnabled());
	}

	public void setForceShowIcon(boolean forceShow) {
		mPreserveIconSpacing = mForceShowIcon = forceShow;
	}

	public void setTitle(CharSequence title) {
		if (title != null) {
			mTitleView.setText(title);

			if (mTitleView.getVisibility() != VISIBLE)
				mTitleView.setVisibility(VISIBLE);
		} else {
			if (mTitleView.getVisibility() != GONE)
				mTitleView.setVisibility(GONE);
		}
	}

	public ActionMenuItem getItemData() {
		return mItemData;
	}

	public void setIcon(Drawable icon) {
		final boolean showIcon = mForceShowIcon;
		if (!showIcon && !mPreserveIconSpacing) {
			return;
		}

		if (mIconView == null && icon == null && !mPreserveIconSpacing) {
			return;
		}

		if (icon != null || mPreserveIconSpacing) {
			mIconView.setImageDrawable(showIcon ? icon : null);

			if (mIconView.getVisibility() != VISIBLE) {
				mIconView.setVisibility(VISIBLE);
			}
		} else {
			mIconView.setVisibility(GONE);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (isPressed())
			canvas.drawColor(mHighlightColor);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public boolean prefersCondensedTitle() {
		return false;
	}

	public boolean showsIcon() {
		return mForceShowIcon;
	}

	private LayoutInflater getInflater() {
		if (mInflater == null) {
			mInflater = LayoutInflater.from(getContext());
		}
		return mInflater;
	}
}
