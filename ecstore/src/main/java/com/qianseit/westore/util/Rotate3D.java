package com.qianseit.westore.util;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class Rotate3D extends Animation {
	public final int X_AXIS = 0;
	public final int Y_AXIS = 1;

	private float mTop;
	private float mLeft;
	private float mFromDegree;
	private float mToDegree;
	private float mCenterX;
	private float mCenterY;
	private Camera mCamera;
	private int mAxis = Y_AXIS;

	public Rotate3D(float fromDegree, float toDegree, float left, float top,
			float centerX, float centerY) {
		this.mFromDegree = fromDegree;
		this.mToDegree = toDegree;
		this.mLeft = left;
		this.mTop = top;
		this.mCenterX = centerX;
		this.mCenterY = centerY;
	}

	/**
	 * 设置动画播放的坐标轴
	 * 
	 * @param axis
	 */
	public void setRotateAxis(int axis) {
		this.mAxis = axis;
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final float FromDegree = mFromDegree;
		float degrees = FromDegree + (mToDegree - mFromDegree)
				* interpolatedTime;
		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Matrix matrix = t.getMatrix();

		if (degrees <= -76.0f) {
			degrees = -90.0f;
			mCamera.save();
			makeRotateAxis(degrees);
			mCamera.getMatrix(matrix);
			mCamera.restore();
		} else if (degrees >= 76.0f) {
			degrees = 90.0f;
			mCamera.save();
			makeRotateAxis(degrees);
			mCamera.getMatrix(matrix);
			mCamera.restore();
		} else {
			mCamera.save();
			// 这里很重要哦。
			mCamera.translate(0, 0, (mAxis == Y_AXIS) ? centerX : centerY);
			makeRotateAxis(degrees);
			mCamera.translate(0, 0, (mAxis == Y_AXIS) ? -centerX : -centerY);
			mCamera.getMatrix(matrix);
			mCamera.restore();
		}

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}

	// 设置旋转轴
	private void makeRotateAxis(float degrees) {
		if (mAxis == Y_AXIS)
			mCamera.rotateY(degrees);
		else
			mCamera.rotateX(degrees);
	}
}