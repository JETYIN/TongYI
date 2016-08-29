package com.qianseit.westore.imageloader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import cn.shopex.ecstore.R;


/**
 * 图片裁剪视图，绘制阴影区域
 * 
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2014年6月10日
 * @Copyright: Copyright (c) 2014 Shenzhen Utoow Technology Co., Ltd. All rights
 *             reserved.
 * 
 */
public class ClipView extends View {

	/** 裁剪图片属性 */
	private ClipPictureBean clipPictureBean = new ClipPictureBean();
	/** 最小边距 */
	private final int MINPADDING = 50;

	/** 裁剪区域的宽度 */
	private int clipWidth = 0;
	/** 裁剪区域的高度 */
	private int clipHeight = 0;

	/** 屏幕的宽度 */
	private int screenWidth = 0;
	/** 屏幕的高度 */
	private int screenHeight = 0;

	public ClipView(Context context) {
		super(context);
	}

	public ClipView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置图片裁剪属性
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午3:34:21
	 * @updateTime 2014年6月10日,下午3:34:21
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param bean
	 *            图片裁剪属性
	 */
	public void setClipPictureBean(ClipPictureBean bean) {
		this.clipPictureBean = bean;
		this.invalidate();
	}

	/**
	 * 初始化大小
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午3:35:02
	 * @updateTime 2014年6月10日,下午3:35:02
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 */
	private void initSize() {
		screenHeight = this.getHeight();
		screenWidth = this.getWidth();

		if (clipPictureBean.getAspectX() >= clipPictureBean.getAspectY()) {// 横向矩形
			clipWidth = screenWidth - 2 * MINPADDING;
//			clipWidth = screenWidth;
			clipHeight = clipWidth * clipPictureBean.getAspectY() / clipPictureBean.getAspectX();
		} else {// 竖向矩形
			clipHeight = screenHeight - 2 * MINPADDING;
//			clipHeight = screenHeight;
			clipWidth = clipHeight * clipPictureBean.getAspectX() / clipPictureBean.getAspectY();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		initSize();

		Paint paint = new Paint();
		paint.setColor(0xaa000000);

		canvas.drawRect(0, 0, screenWidth, (screenHeight - clipHeight) / 2, paint);
		canvas.drawRect(0, (screenHeight - clipHeight) / 2, (screenWidth - clipWidth) / 2, (screenHeight - clipHeight) / 2 + clipHeight, paint);
		canvas.drawRect((screenWidth - clipWidth) / 2 + clipWidth, (screenHeight - clipHeight) / 2, screenWidth, (screenHeight - clipHeight) / 2
				+ clipHeight, paint);
		canvas.drawRect(0, (screenHeight - clipHeight) / 2 + clipHeight, screenWidth, screenHeight, paint);

		paint.setColor(getResources().getColor(R.color.white));
		canvas.drawRect((screenWidth - clipWidth) / 2 - 1, (screenHeight - clipHeight) / 2 - 1, (screenWidth - clipWidth) / 2 + clipWidth + 1,
				((screenHeight - clipHeight) / 2), paint);
		canvas.drawRect((screenWidth - clipWidth) / 2 - 1, (screenHeight - clipHeight) / 2, (screenWidth - clipWidth) / 2,
				(screenHeight - clipHeight) / 2 + clipHeight, paint);
		canvas.drawRect((screenWidth - clipWidth) / 2 + clipWidth, (screenHeight - clipHeight) / 2, (screenWidth - clipWidth) / 2 + clipWidth + 1,
				(screenHeight - clipHeight) / 2 + clipHeight, paint);
		canvas.drawRect((screenWidth - clipWidth) / 2 - 1, (screenHeight - clipHeight) / 2 + clipHeight, (screenWidth - clipWidth) / 2 + clipWidth
				+ 1, (screenHeight - clipHeight) / 2 + clipHeight + 1, paint);
	}

	/**
	 * 裁剪区域宽度
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午3:52:38
	 * @updateTime 2014年6月10日,下午3:52:38
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @return
	 */
	public int getClipWidth() {
		return clipWidth;
	}

	/**
	 * 裁剪区域高度
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午3:52:53
	 * @updateTime 2014年6月10日,下午3:52:53
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @return
	 */
	public int getClipHeight() {
		return clipHeight;
	}

	/**
	 * 屏幕宽度
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午3:53:03
	 * @updateTime 2014年6月10日,下午3:53:03
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @return
	 */
	public int getScreenWidth() {
		return screenWidth;
	}

	/**
	 * 屏幕高度
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午3:53:12
	 * @updateTime 2014年6月10日,下午3:53:12
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @return
	 */
	public int getScreenHeight() {
		return screenHeight;
	}

}
