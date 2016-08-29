package com.qianseit.westore.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.SimpleAnimListener;
import com.qianseit.westore.util.Util;

/**
 * 弹出层处理类<br/>
 * 通过这个类实现一个弹出层的单独处理
 * 
 */
public class CustomDialog implements OnClickListener {
	private View mAnchor;
	private PopupWindow mPop;
	private Activity mActivity;

	private View popView;
	private View mTopPanel;
	private View mAnimLayout;
	private View mPanelLayout;
	private View mParentPanel;
	private ListView mListView;
	private ImageView mIconView;
	private LinearLayout mContentPanel;
	public TextView mTitleTV, mMessageTV;
	private View mBottomDivider, mTitleDivider;
	private OnClickListener mCurrentClickedListener;
	private View mOkButtonDivider, mMidButtonDivider;
	private Button mOkButton, mCancelButton, mMidButton;
	private OnClickListener mOkListener, mCancelListener, mMiddleListener;

	private boolean mCancelable;
	private Animation mFadeInAnim;
	private Animation mInAnim, mOutAnim;
	private boolean mDimBackground = true;
	private boolean mAniming = false;
	private boolean mDismissed = false;
	private boolean mCanceledOnTouchOutside = false;

	// 底部按钮只有一个时候的背景
	private int mSingleButtonBackground = R.drawable.dialog_btn_single_holo_light;
	private int mLeftButtonBackground = R.drawable.dialog_btn_left_holo_light;
	private int mRightButtonBackground = R.drawable.dialog_btn_right_holo_light;
	private int mMidButtonBackground = R.drawable.dialog_btn_middle_holo_light;

	public CustomDialog(Activity aa) {
		mActivity = aa;
		initCustomDialog();
	}

	public CustomDialog(View anchor) {
		mAnchor = anchor;
		initCustomDialog();
	}

	/**
	 * 获得容器
	 * 
	 * @return
	 */
	public Context getContext() {
		return mActivity == null ? mAnchor.getContext() : mActivity;
	}

	/* 返回确定按钮 */
	public Button getOkButton() {
		return mOkButton;
	}

	/* 返回取消按钮 */
	public Button getCancelButton() {
		return mCancelButton;
	}

	/* 返回中间的按钮 */
	public Button getMidButton() {
		return mMidButton;
	}

	// 初始化popwindow
	public void initCustomDialog() {
		DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
		float width = Math.min(dm.widthPixels, dm.heightPixels), dpi = dm.density;
		int screenDip = (int) Math.ceil(width / dpi);

		popView = LayoutInflater.from(getContext()).inflate(
				R.layout.custom_dialog_holo, null);
		mAnimLayout = findViewById(R.id.custom_dialog_animView);
		mPanelLayout = findViewById(R.id.custom_dialog_panelLayout);
		mParentPanel = findViewById(R.id.custom_dialog_parentPanel);
		mTopPanel = findViewById(R.id.custom_dialog_topPanel);
		mIconView = (ImageView) findViewById(R.id.custom_dialog_icon);
		mTitleTV = (TextView) findViewById(R.id.custom_dialog_title);
		mTitleDivider = findViewById(R.id.custom_dialog_titleDivider);

		// 屏幕宽度超过380dip时，设置最大为360dip
		if (screenDip > 380 && screenDip < 514) {
			int maxWidth = Util.dip2px(getContext(), 360);
			mParentPanel.getLayoutParams().width = maxWidth;
		} else if (screenDip >= 514) {
			// 屏幕宽度超过514di时，设置最大为屏幕宽度的70%
			int maxWidth = (int) Math.ceil(width * 0.7f);
			mParentPanel.getLayoutParams().width = maxWidth;
		}

		// 中间自定义View
		mListView = (ListView) findViewById(R.id.custom_dialog_list);
		mMessageTV = (TextView) findViewById(R.id.custom_dialog_message);
		mContentPanel = (LinearLayout) findViewById(R.id.custom_dialog_middlePanel);

		// 底部按钮View
		mBottomDivider = findViewById(R.id.custom_dialog_bottomDivider);
		mOkButton = (Button) findViewById(R.id.custom_dialog_ok);
		mOkButtonDivider = findViewById(R.id.custom_dialog_ok_divider);
		mMidButton = (Button) findViewById(R.id.custom_dialog_mid_button);
		mMidButtonDivider = findViewById(R.id.custom_dialog_mid_divider);
		mCancelButton = (Button) findViewById(R.id.custom_dialog_cancel);
		mOkButton.setOnClickListener(this);
		mMidButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);

		// 监听返回键消息事件
		popView.setFocusableInTouchMode(true);
		popView.setFocusable(true);
		popView.requestFocus();
		// 点击空白页，隐藏dialog
		mParentPanel.setOnClickListener(this);
		popView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCanceledOnTouchOutside)
					CustomDialog.this.dismiss();
			}
		});
		popView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_BACK
						&& event.getRepeatCount() == 0 && mCancelable) {
					CustomDialog.this.dismiss();
					return true;
				}
				return false;
			}
		});

		mFadeInAnim = AnimationUtils.loadAnimation(getContext(),
				android.R.anim.fade_in);
		mFadeInAnim.setDuration(300);
		mInAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.custom_dialog_appear);
		mOutAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.custom_dialog_disappear);
		mInAnim.setAnimationListener(mAnimCallback);
		mOutAnim.setAnimationListener(mAnimCallback);

		// 初始化popupwindow
		this.mPop = new PopupWindow(popView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, true);
		// this.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		// 默认的对话框样式
		this.setDialogStyle(R.style.custom_dialog_holo_light);
	}

	/**
	 * 查找popview中的子view
	 * 
	 * @param id
	 * @return
	 */
	private View findViewById(int id) {
		return popView.findViewById(id);
	}

	/**
	 * 设置软键盘弹出时，内容adjust的模式<br />
	 * 默认为resize，如果包含可输入的自定义view，最外层使用scrollview可达到最好效果
	 * 
	 * @param mode
	 */
	public void setSoftInputMode(int mode) {
		this.mPop.setSoftInputMode(mode);
	}

	/**
	 * 背景是否需要变暗
	 * 
	 * @param mDimBackground
	 */
	public void setDimBackground(boolean mDimBackground) {
		this.mDimBackground = mDimBackground;
	}

	// 设置标题文字
	public CustomDialog setTitle(String title) {
		// 标题和图标都为空则隐藏Divider
		if (TextUtils.isEmpty(title) && mIconView.getDrawable() == null)
			this.mTopPanel.setVisibility(View.GONE);
		else
			this.mTopPanel.setVisibility(View.VISIBLE);

		this.mTitleTV.setText(title);
		this.mPop.update();
		return this;
	}

	// 设置标题文字
	public CustomDialog setTitle(int titleId) {
		return setTitle(getContext().getString(titleId));
	}

	// 设置标题文字对齐方式
	public CustomDialog setTitleGravity(int gravity) {
		this.mTitleTV.setGravity(gravity);
		this.mPop.update();
		return this;
	}

	/**
	 * 设置标题图标
	 * 
	 * @param drawable
	 * @return
	 */
	public CustomDialog setIcon(Drawable drawable) {
		if (drawable == null) {
			this.mIconView.setVisibility(View.GONE);
			// 标题和图标都为空则隐藏Divider
			if (TextUtils.isEmpty(mTitleTV.getText()))
				mTopPanel.setVisibility(View.GONE);
			return this;
		}

		this.mTitleTV.setPadding(0, 0, 0, 0);
		this.mIconView.setAdjustViewBounds(true);
		this.mIconView.setImageDrawable(drawable);
		this.mIconView.setVisibility(View.VISIBLE);
		this.mTopPanel.setVisibility(View.VISIBLE);
		this.mPop.update();
		return this;
	}

	public CustomDialog setIcon(int iconId) {
		return setIcon(getContext().getResources().getDrawable(iconId));
	}

	// 设置message
	public CustomDialog setMessage(CharSequence msg) {
		if (TextUtils.isEmpty(msg)) {
			this.mMessageTV.setVisibility(View.GONE);
			return this;
		}
		this.mMessageTV.setVisibility(View.VISIBLE);
		this.mMessageTV.setText(msg);
		this.mPop.update();
		return this;
	}

	// 设置message
	public CustomDialog setMessage(int msgId) {
		return setMessage(getContext().getString(msgId));
	}

	/**
	 * message文字对齐方式
	 * 
	 * @param grav
	 * @return
	 */
	public CustomDialog setMessageGravity(int grav) {
		this.mMessageTV.setGravity(grav);
		this.mPop.update();
		return this;
	}

	// 设置message
	public CustomDialog setMessageColor(int color) {
		this.mMessageTV.setTextColor(color);
		return this;
	}

	// 设置自定义的View
	public CustomDialog setCustomView(View v) {
		this.mContentPanel.removeAllViews();
		this.mContentPanel.addView(v, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0, 1));
		((LinearLayout.LayoutParams) mContentPanel.getLayoutParams()).weight = 1;
		this.mPop.update();
		return this;
	}

	// 是否已经显示
	public boolean isShowing() {
		return this.mPop.isShowing();
	}

	// 设置是否可以取消
	public CustomDialog setCancelable(boolean cancelable) {
		this.mCancelable = cancelable;
		return this;
	}

	// 点击空白处是否可以取消
	public CustomDialog setCanceledOnTouchOutside(boolean cancelable) {
		this.mCanceledOnTouchOutside = cancelable;
		return this;
	}

	// 设置确定按钮
	public CustomDialog setNegativeButton(String text, OnClickListener mListener) {
		// 确定按钮文字
		if (!TextUtils.isEmpty(text)) {
			mCancelButton.setText(text);
			mCancelButton.setVisibility(View.VISIBLE);
		} else {
			mCancelButton.setVisibility(View.GONE);
		}

		// 点击事件
		this.mCancelListener = mListener;
		this.mPop.update();
		return this;
	}

	public CustomDialog setNegativeButton(int textId, OnClickListener mListener) {
		return setNegativeButton(getContext().getString(textId), mListener);
	}

	/**
	 * 设置取消按钮的点击事件<br />
	 * 
	 * @param mListener
	 * @param autoDismiss
	 *            是否自动Dismiss
	 * @return
	 */
	public CustomDialog setNegativeButtonListener(OnClickListener mListener,
			boolean autoDismiss) {
		if (autoDismiss)
			mCancelListener = mListener;
		else
			mCancelButton.setOnClickListener(mListener);
		return this;
	}

	// 设置取消按钮
	public CustomDialog setPositiveButton(String text, OnClickListener mListener) {
		// 确定按钮文字
		if (!TextUtils.isEmpty(text)) {
			mOkButton.setText(text);
			mOkButton.setVisibility(View.VISIBLE);
		} else {
			mOkButton.setVisibility(View.GONE);
		}

		// 点击事件
		this.mOkListener = mListener;
		this.mPop.update();
		return this;
	}

	public CustomDialog setPositiveButton(int textId, OnClickListener mListener) {
		return setPositiveButton(getContext().getString(textId), mListener);
	}

	/**
	 * 设置确定按钮的点击事件<br />
	 * 
	 * @param mListener
	 * @param autoDismiss
	 *            是否自动Dismiss
	 * @return
	 */
	public CustomDialog setPositiveButtonListener(OnClickListener mListener,
			boolean autoDismiss) {
		if (autoDismiss)
			mOkListener = mListener;
		else
			mOkButton.setOnClickListener(mListener);
		return this;
	}

	// 设置取消按钮
	public CustomDialog setCenterButton(String text, OnClickListener mListener) {
		// 确定按钮文字
		if (!TextUtils.isEmpty(text)) {
			mMidButton.setText(text);
			mMidButton.setVisibility(View.VISIBLE);
		} else {
			mMidButton.setVisibility(View.GONE);
		}

		// 点击事件
		this.mMiddleListener = mListener;
		this.mPop.update();
		return this;
	}

	public CustomDialog setCenterButton(int textId, OnClickListener mListener) {
		return setCenterButton(getContext().getString(textId), mListener);
	}

	/**
	 * 设置中间按钮的点击事件<br />
	 * 
	 * @param mListener
	 * @param autoDismiss
	 *            是否自动Dismiss
	 * @return
	 */
	public CustomDialog setCenterButtonListener(OnClickListener mListener,
			boolean autoDismiss) {
		if (autoDismiss)
			mMiddleListener = mListener;
		else
			mMidButton.setOnClickListener(mListener);
		return this;
	}

	// 检测是否只有一个按钮，只有一个按钮需要改变其背景
	private void checkOnlyOneButton() {
		int singleBgRes = mSingleButtonBackground;
		boolean isOkGone = (mOkButton.getVisibility() == View.GONE);
		boolean isCancelGone = (mCancelButton.getVisibility() == View.GONE);
		boolean isMiddleGone = (mMidButton.getVisibility() == View.GONE);

		// 重置分隔线与button状态一致
		mOkButtonDivider.setVisibility(mOkButton.getVisibility());
		mMidButtonDivider.setVisibility(mMidButton.getVisibility());

		// 按钮数量大于一个时候，使用各自的样式
		int numOfButton = (isOkGone ? 0 : 1) + (isCancelGone ? 0 : 1)
				+ (isMiddleGone ? 0 : 1);
		if (numOfButton > 1) {
			mCancelButton.setBackgroundResource(mLeftButtonBackground);
			mMidButton.setBackgroundResource(mMidButtonBackground);
			mOkButton.setBackgroundResource(mRightButtonBackground);
			this.mPop.update();
			return;
		}

		// 按钮数量只有一个时候，使用Single样式，所有分隔线消失
		if (!isMiddleGone)
			mMidButton.setBackgroundResource(singleBgRes);
		else if (!isCancelGone)
			mCancelButton.setBackgroundResource(singleBgRes);
		else if (!isOkGone)
			mOkButton.setBackgroundResource(singleBgRes);
		mMidButtonDivider.setVisibility(View.GONE);
		mOkButtonDivider.setVisibility(View.GONE);
		this.mPop.update();
	}

	/**
	 * 显示标题栏的分割线
	 * 
	 * @param isShow
	 */
	public void setShowTitleDivider(boolean isShow) {
		mTitleDivider.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	/**
	 * 显示底部按钮栏的分割线
	 * 
	 * @param isShow
	 */
	public void setShowBottomDivider(boolean isShow) {
		mBottomDivider.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}

	/**
	 * 设置左右两边的padding
	 * 
	 * @param padding
	 */
	public void setMarginHorizontal(int margin) {
		((FrameLayout.LayoutParams) mParentPanel.getLayoutParams()).leftMargin = margin;
		((FrameLayout.LayoutParams) mParentPanel.getLayoutParams()).rightMargin = margin;
	}

	/**
	 * 设置CustomDialog样式
	 * 
	 * @param styleResId
	 */
	public void setDialogStyle(int styleResId) {
		if (styleResId == 0) {
			mParentPanel.setBackgroundDrawable(null);
			return;
		}

		TypedArray ta = getContext().obtainStyledAttributes(styleResId,
				R.styleable.CustomDialog);
		mParentPanel.setBackgroundResource(ta.getResourceId(
				R.styleable.CustomDialog_dialogFullBackground,
				R.drawable.dialog_full_holo_light));
		mTitleTV.setTextColor(ta.getColor(
				R.styleable.CustomDialog_dialogTitleTextColor,
				R.color.custom_dialog_holo_blue));
		mTitleTV.setShadowLayer(1, 0, 1, ta.getColor(
				R.styleable.CustomDialog_dialogTitleShadowColor,
				R.color.custom_dialog_holo_blue));
		mTitleDivider.setBackgroundResource(ta.getResourceId(
				R.styleable.CustomDialog_dialogTitleDivider,
				R.color.custom_dialog_holo_blue));

		// Message
		mMessageTV.setTextColor(ta.getColor(
				R.styleable.CustomDialog_dialogMessageTextColor,
				android.R.color.black));

		// 按钮上面的分割线
		mBottomDivider.setBackgroundResource(ta.getResourceId(
				R.styleable.CustomDialog_dialogBottomDivider,
				R.drawable.list_divider_holo_light));

		// 底部按钮
		mSingleButtonBackground = ta.getResourceId(
				R.styleable.CustomDialog_dialogSingleButtonBackground,
				R.drawable.dialog_btn_single_holo_light);

		// 按钮之间的分隔线颜色
		mOkButtonDivider.setBackgroundResource(ta.getResourceId(
				R.styleable.CustomDialog_dialogButtonDivider,
				R.drawable.dialog_button_divider_light));
		mMidButtonDivider.setBackgroundResource(ta.getResourceId(
				R.styleable.CustomDialog_dialogButtonDivider,
				R.drawable.dialog_button_divider_light));

		// 左边取消按钮样式
		mLeftButtonBackground = ta.getResourceId(
				R.styleable.CustomDialog_dialogLeftButtonBackground,
				R.drawable.dialog_btn_left_holo_light);
		mCancelButton.setTextColor(ta.getColor(
				R.styleable.CustomDialog_dialogLeftButtonTextColor,
				android.R.color.black));
		mCancelButton.setBackgroundResource(mLeftButtonBackground);

		// 中间按钮样式
		mMidButtonBackground = ta.getResourceId(
				R.styleable.CustomDialog_dialogMiddleButtonBackground,
				R.drawable.dialog_btn_middle_holo_light);
		mMidButton.setTextColor(ta.getColor(
				R.styleable.CustomDialog_dialogMiddleButtonTextColor,
				android.R.color.black));
		mMidButton.setBackgroundResource(mMidButtonBackground);

		// 右边确定按钮样式
		mRightButtonBackground = ta.getResourceId(
				R.styleable.CustomDialog_dialogRightButtonBackground,
				R.drawable.dialog_btn_right_holo_light);
		mOkButton.setTextColor(ta.getColor(
				R.styleable.CustomDialog_dialogRightButtonTextColor,
				android.R.color.black));
		mMidButton.setBackgroundResource(mRightButtonBackground);
		ta.recycle();
		mPop.update();
	}

	/* 显示对话框 */
	public synchronized CustomDialog show() {
		mDismissed = false;

		// 检测按钮数量是否为单个按钮
		this.checkOnlyOneButton();

		// Activity已经关闭，或者rootView为null则不显示
		if (mAnchor == null)
			mAnchor = mActivity.getWindow().getDecorView();
		if ((mActivity != null && mActivity.isFinishing()) || mAnchor == null) {
			return this;
		}

		// PopWindow为空则初始化
		if (mPop == null)
			initCustomDialog();

		if (!mPop.isShowing()) {
			// 最大限度避免WindowLeak
			mAnchor.post(mShowRunnable);
		}

		return this;
	}

	/* 隐藏对话框 */
	public void dismiss() {
		mDismissed = true;
		if (mPop != null && mPop.isShowing() && !mAniming) {
			popView.post(new Runnable() {
				@Override
				public void run() {
					mAnimLayout.startAnimation(mOutAnim);
					mPanelLayout.setBackgroundColor(Color.TRANSPARENT);
				}
			});
		}
	}

	/**
	 * 设置对话框消失的事件
	 * 
	 * @param mListener
	 */
	public CustomDialog setOnDismissListener(OnDismissListener mListener) {
		this.mPop.setOnDismissListener(mListener);
		return this;
	}

	@Override
	public void onClick(View v) {
		if (v == mOkButton) {
			dismiss();
			mCurrentClickedListener = mOkListener;
		} else if (v == mCancelButton) {
			dismiss();
			mCurrentClickedListener = mCancelListener;
		} else if (v == mMidButton) {
			dismiss();
			mCurrentClickedListener = mMiddleListener;
		}
	}

	// 显示Dialog的线程
	private Runnable mShowRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				if ((mActivity != null && mActivity.isFinishing())
						|| mAnchor == null) {
					return;
				}
				mPop.showAtLocation(mAnchor, Gravity.CENTER, 0, 0);
				mAnimLayout.startAnimation(mInAnim);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// 动画监听器
	private SimpleAnimListener mAnimCallback = new SimpleAnimListener() {
		@Override
		public void onAnimationStart(Animation animation) {
			if (animation == mOutAnim)
				mAniming = true;
		};

		@Override
		public void onAnimationEnd(Animation animation) {
			mAniming = false;
			if (animation == mInAnim) {
				if (mDimBackground) { // 背景变暗淡
					int bgColor = Color.parseColor("#70000000");
					mPanelLayout.setBackgroundColor(bgColor);
					mPanelLayout.startAnimation(mFadeInAnim);
					mParentPanel.invalidate();
				}
				// 在显示完之前被dismiss，则需要再次dismiss
				if (mDismissed)
					dismiss();
			} else if (animation == mOutAnim) {
				postDismissPopwindow();
				if (mCurrentClickedListener != null)
					mCurrentClickedListener.onClick(null);
				mCurrentClickedListener = null;
			}
		};
	};

	/* 隐藏PopWindow */
	private void postDismissPopwindow() {
		popView.post(new Runnable() {
			@Override
			public void run() {
				try {
					if (isCanDismiss())
						mPop.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 是否可以隐藏
	 * 
	 * @return
	 */
	private boolean isCanDismiss() {
		return popView != null && popView.getParent() != null;
	}

	/**
	 * 多选列表
	 * 
	 * @param names
	 * @param status
	 * @param multiChoiceListener
	 */
	public CustomDialog setMultipleChoiceItems(String[] names,
			final boolean[] mCheckedItems,
			final OnMultiChoiceClickListener multiChoiceListener) {
		if (names == null || mCheckedItems == null)
			return this;

		BaseAdapter adapter = new ArrayAdapter<String>(getContext(),
				R.layout.simple_list_item_multiple_choice, android.R.id.text1,
				names) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				if (mCheckedItems != null) {
					boolean isItemChecked = mCheckedItems[position];
					if (isItemChecked) {
						mListView.setItemChecked(position, true);
					}
				}
				return view;
			}
		};

		mContentPanel.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
		mListView.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setVisibility(View.VISIBLE);
		mMessageTV.setVisibility(View.GONE);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCheckedItems[position] = mListView.isItemChecked(position);
				if (multiChoiceListener != null)
					multiChoiceListener.onClick(CustomDialog.this, position,
							mListView.isItemChecked(position));
			}
		});
		return this;
	}

	/**
	 * 单选列表
	 * 
	 * @param names
	 * @param status
	 * @param multiChoiceListener
	 */
	public CustomDialog setSingleChoiceItems(String[] names,
			final int mCheckedItem, final OnItemClickListener mListener) {
		return setSingleChoiceItems(names, mCheckedItem, null, mListener, true);
	}

	/**
	 * 单选列表
	 * 
	 * @param names
	 * @param status
	 * @param multiChoiceListener
	 */
	public CustomDialog setSingleChoiceItems(String[] names,
			final int mCheckedItem, final int[] drawables, int tintColor,
			final OnItemClickListener mListener) {
		Drawable[] newDrawables = null;
		ColorFilter colorFilter = null;
		if (tintColor != 0)
			colorFilter = new PorterDuffColorFilter(tintColor,
					PorterDuff.Mode.SRC_ATOP);
		if (drawables != null) {
			newDrawables = new Drawable[drawables.length];
			Resources res = getContext().getResources();
			for (int i = 0, c = drawables.length; i < c; i++) {
				int resID = drawables[i];
				if (resID > 0) {
					Drawable drawable = res.getDrawable(resID);
					drawable.setColorFilter(colorFilter);
					newDrawables[i] = drawable;
				}
			}
		}
		return setSingleChoiceItems(names, mCheckedItem, newDrawables,
				mListener, true);
	}

	/**
	 * 单选列表
	 * 
	 * @param names
	 * @param status
	 * @param multiChoiceListener
	 */
	public CustomDialog setSingleChoiceItems(final String[] names,
			final int mCheckedItem, final Drawable[] drawables,
			final OnItemClickListener mListener, final boolean showMark) {
		if (names == null)
			return this;

		BaseAdapter adapter = new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					int padding = Util.dip2px(getContext(), 8);
					int height = Util.dip2px(getContext(), 64);
					CheckedTextView textview = new CheckedTextView(getContext());
					int mark = R.drawable.btn_check_holo_light;
					textview.setCheckMarkDrawable(showMark ? mark
							: R.drawable.transparent);
					textview.setGravity(Gravity.CENTER_VERTICAL);
					textview.setPadding(padding, 0, padding, 0);
					textview.setLayoutParams(new AbsListView.LayoutParams(
							LayoutParams.MATCH_PARENT, height));
					textview.setTextSize(20);
					textview.setSingleLine();
					convertView = textview;
				}

				CheckedTextView textview = (CheckedTextView) convertView;
				textview.setText(names[position]);
				// TextView左边的图标
				if (drawables != null && position < drawables.length) {
					textview.setCompoundDrawablesWithIntrinsicBounds(
							drawables[position], null, null, null);
				}

				return convertView;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return names[position];
			}

			@Override
			public int getCount() {
				return names.length;
			}
		};

		return setSingleChoiceItems(adapter, mCheckedItem, mListener);
	}

	/**
	 * 传入自定义的Adapter
	 * 
	 * @param adapter
	 * @param mCheckedItem
	 * @param mListener
	 * @return
	 */
	public CustomDialog setSingleChoiceItems(final BaseAdapter adapter,
			final int mCheckedItem, final OnItemClickListener mListener) {
		mContentPanel.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
		mListView.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(mListener);
		mListView.setVisibility(View.VISIBLE);
		mMessageTV.setVisibility(View.GONE);
		mListView.setAdapter(adapter);

		// 选中的列
		if (mCheckedItem > -1) {
			mListView.setSelection(mCheckedItem);
			mListView.setItemChecked(mCheckedItem, true);
		}
		return this;
	}

	public interface OnMultiChoiceClickListener {
		/**
		 * This method will be invoked when an item in the dialog is clicked.
		 * 
		 * @param dialog
		 *            The dialog where the selection was made.
		 * @param which
		 *            The position of the item in the list that was clicked.
		 * @param isChecked
		 *            True if the click checked the item, else false.
		 */
		public void onClick(CustomDialog dialog, int which, boolean isChecked);
	}
}
