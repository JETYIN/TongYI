package com.qianseit.westore.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;

public class LevelView extends LinearLayout {

	private TextView mTextView1;
	private TextView mTextView2;
	private View mProgress;
	private View mProgress_img;

	public LevelView(Context context) {
		super(context);
		init(context);
	}

	public LevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LevelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_level,
				this, true);
		mTextView1 = (TextView) view.findViewById(R.id.view_level_score);
		mTextView2 = (TextView) view.findViewById(R.id.view_level_level);
		mProgress = view.findViewById(R.id.view_level_progress);
		mProgress_img = view.findViewById(R.id.view_level_img);
	}

	public void setData(final int score, final int maxScore, final String level) {
		mTextView1.setText("" + score);
		mTextView2.setText(level);

		getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						float ss = score * .1f / maxScore;
						ss = ss > 1 ? 1 : ss;
						int width = getWidth();
						int progress = (int) (width * ss) * 10;
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mProgress
								.getLayoutParams();
						params.width = progress;
						mProgress.setLayoutParams(params);
						if (progress < mProgress_img.getWidth() / 2) {

						} else if (progress + mProgress_img.getWidth() / 2 > width) {
							mProgress_img.setX(width - mProgress_img.getWidth());
						} else {
							mProgress_img.setX(progress
									- mProgress_img.getWidth() / 2);
						}

						if (progress < mTextView1.getWidth() / 2) {

						} else if (progress + mTextView1.getWidth() / 2 > width) {
							mTextView1.setX(width - mTextView1.getWidth());
						} else {
							mTextView1.setX(progress - mTextView1.getWidth()
									/ 2);
						}

						if (progress < mTextView2.getWidth() / 2) {

						} else if (progress + mTextView2.getWidth() / 2 > width) {
							mTextView2.setX(width - mTextView2.getWidth());
						} else {
							mTextView2.setX(progress - mTextView2.getWidth()
									/ 2);
						}
					}
				});
	}
}
