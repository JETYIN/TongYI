package com.qianseit.westore.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;

/**
 * @hide
 */
public class ActionMenuItem implements MenuItem {
	private final int mId;
	private final int mGroup;
	private final int mOrdering;

	private CharSequence mTitle;
	private CharSequence mTitleCondensed;
	private Intent mIntent;
	private char mShortcutNumericChar;
	private char mShortcutAlphabeticChar;

	private Drawable mIconDrawable;
	private ImageButton mTargetButton;

	private Context mContext;

	private MenuItem.OnMenuItemClickListener mClickListener;

	private boolean mVisible = true;
	private final int ENABLED = 0x00000010;
	private int mFlags = ENABLED;
	private final int CHECKABLE = 0x00000001;
	private final int CHECKED = 0x00000002;
	private final int EXCLUSIVE = 0x00000004;
	// private static final int HIDDEN = 0x00000008;

	public ActionMenuItem(Context context, int group, int id,
			int categoryOrder, int ordering, CharSequence title) {
		mContext = context;
		mId = id;
		mGroup = group;
		mOrdering = ordering;
		mTitle = title;
	}

	public char getAlphabeticShortcut() {
		return mShortcutAlphabeticChar;
	}

	public int getGroupId() {
		return mGroup;
	}

	public Drawable getIcon() {
		return mIconDrawable;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public int getItemId() {
		return mId;
	}

	public ContextMenuInfo getMenuInfo() {
		return null;
	}

	public char getNumericShortcut() {
		return mShortcutNumericChar;
	}

	public int getOrder() {
		return mOrdering;
	}

	public SubMenu getSubMenu() {
		return null;
	}

	public CharSequence getTitle() {
		return mTitle;
	}

	public CharSequence getTitleCondensed() {
		return mTitleCondensed;
	}

	/**
	 * 返回对应的按钮
	 * 
	 * @return
	 */
	public ImageButton getTargetButton() {
		return mTargetButton;
	}

	/**
	 * 设置对应的按钮
	 * 
	 * @return
	 */
	public MenuItem setTargetButton(ImageButton targetButton) {
		this.mTargetButton = targetButton;
		return this;
	}

	public boolean hasSubMenu() {
		return false;
	}

	public boolean isCheckable() {
		return (mFlags & CHECKABLE) != 0;
	}

	public boolean isChecked() {
		return (mFlags & CHECKED) != 0;
	}

	public boolean isEnabled() {
		return (mFlags & ENABLED) != 0;
	}

	public boolean isVisible() {
		return mVisible;
	}

	public MenuItem setAlphabeticShortcut(char alphaChar) {
		mShortcutAlphabeticChar = alphaChar;
		return this;
	}

	public MenuItem setCheckable(boolean checkable) {
		mFlags = (mFlags & ~CHECKABLE) | (checkable ? CHECKABLE : 0);
		return this;
	}

	public ActionMenuItem setExclusiveCheckable(boolean exclusive) {
		mFlags = (mFlags & ~EXCLUSIVE) | (exclusive ? EXCLUSIVE : 0);
		return this;
	}

	public MenuItem setChecked(boolean checked) {
		mFlags = (mFlags & ~CHECKED) | (checked ? CHECKED : 0);
		return this;
	}

	public MenuItem setEnabled(boolean enabled) {
		mFlags = (mFlags & ~ENABLED) | (enabled ? ENABLED : 0);
		return this;
	}

	public MenuItem setIcon(Drawable icon) {
		mIconDrawable = icon;
		return this;
	}

	public ActionMenuItem setIcon(int iconRes) {
		mIconDrawable = mContext.getResources().getDrawable(iconRes);
		return this;
	}

	public MenuItem setIntent(Intent intent) {
		mIntent = intent;
		return this;
	}

	public MenuItem setNumericShortcut(char numericChar) {
		mShortcutNumericChar = numericChar;
		return this;
	}

	public MenuItem setOnMenuItemClickListener(
			OnMenuItemClickListener menuItemClickListener) {
		mClickListener = menuItemClickListener;
		return this;
	}

	public MenuItem setShortcut(char numericChar, char alphaChar) {
		mShortcutNumericChar = numericChar;
		mShortcutAlphabeticChar = alphaChar;
		return this;
	}

	public MenuItem setTitle(CharSequence title) {
		mTitle = title;
		return this;
	}

	public MenuItem setTitle(int title) {
		mTitle = mContext.getResources().getString(title);
		return this;
	}

	public MenuItem setTitleCondensed(CharSequence title) {
		mTitleCondensed = title;
		return this;
	}

	public MenuItem setVisible(boolean visible) {
		mVisible = visible;
		return this;
	}

	public boolean invoke() {
		if (mClickListener != null && mClickListener.onMenuItemClick(this)) {
			return true;
		}

		if (mIntent != null) {
			mContext.startActivity(mIntent);
			return true;
		}

		return false;
	}

	public void setShowAsAction(int show) {
		// Do nothing. ActionMenuItems always show as action buttons.
	}

	public MenuItem setActionView(View actionView) {
		throw new UnsupportedOperationException();
	}

	public View getActionView() {
		return null;
	}

	@Override
	public MenuItem setShowAsActionFlags(int actionEnum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setActionView(int resId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuItem setActionProvider(ActionProvider actionProvider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionProvider getActionProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean expandActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean collapseActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isActionViewExpanded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

}
