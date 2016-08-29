package com.qianseit.westore.picturetagview;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import cn.shopex.ecstore.R;

public class PictureTagView extends RelativeLayout implements
		OnEditorActionListener {

	private Context context;
	private TextView tvPictureTagLabel;
	private View loTag;

	public enum Status {
		Normal, Edit
	}

	public static enum Direction {
		Left, Right
	}

	private Direction direction = Direction.Left;
	private InputMethodManager imm;
	private ImageView ivTip;
	private static final int ViewWidth = 80;
	private static final int ViewHeight = 50;

	public PictureTagView(Context context, Direction direction) {
		super(context);
		this.context = context;
		this.direction = direction;
		initViews();
		init();
		// initEvents();
	}

	/**
	 * 获取当前方向
	 * 
	 * @return
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * 改变当前方向
	 * 
	 * @param d
	 */
	public void setDirection(Direction d) {
		direction = d;
	}

	/** 初始化视图 **/
	protected void initViews() {
		LayoutInflater.from(context).inflate(R.layout.picturetagview, this,
				true);
		ivTip = (ImageView) findViewById(R.id.iv_tip);
		 tvPictureTagLabel = (TextView) findViewById(R.id.tvPictureTagLabel);
		loTag = findViewById(R.id.loTag);
	}
	public void setImageGone() {
		if(ivTip!=null)ivTip.setVisibility(View.GONE);
	}
	public void setImageVisiable() {
		if(ivTip!=null)ivTip.setVisibility(View.VISIBLE);
	}
	/** 初始化 **/
	protected void init() {
		imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		directionChange();
	}

	/** 初始化事件 **/
	protected void initEvents() {
	//	etPictureTagLabel.setOnEditorActionListener(this);
	}

	public void setStatus(Status status) {
		switch (status) {
		// case Normal:
		// tvPictureTagLabel.setVisibility(View.VISIBLE);
		// etPictureTagLabel.clearFocus();
		// tvPictureTagLabel.setText(etPictureTagLabel.getText());
		// etPictureTagLabel.setVisibility(View.GONE);
		// //隐藏键盘
		// imm.hideSoftInputFromWindow(etPictureTagLabel.getWindowToken() , 0);
		// break;
		// case Edit:
		// tvPictureTagLabel.setVisibility(View.GONE);
		// etPictureTagLabel.setVisibility(View.VISIBLE);
		// etPictureTagLabel.requestFocus();
		// //弹出键盘
		// imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
		// break;
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		setStatus(Status.Normal);
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		View parent = (View) getParent();
//		int halfParentW = (int) (parent.getWidth() * 0.5);
//		int center = (int) (l + (this.getWidth() * 0.5));
//		if (center <= halfParentW) {
//		//	direction = Direction.Left;
//		} else {
//			direction = Direction.Right;
//		}
		directionChange();
	}

	public void directionChange() {
		switch (direction) {
		case Left:
			loTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_left);
			break;
		case Right:
			loTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_right);
			break;
		}
	}

	public static int getViewWidth() {
		return ViewWidth;
	}

	public static int getViewHeight() {
		return ViewHeight;
	}
	/**
	 * 设置标签文本
	 * @param title
	 */
	public void setText(String  title){
		if(title==null)return;
		tvPictureTagLabel.setText(title);
	}
}
