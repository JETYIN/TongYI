package com.qianseit.westore.ui;

import java.util.List;

import cn.shopex.ecstore.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PromotionCategoryView extends HorizontalScrollView {

	private LinearLayout mContainerLayout;
	private LayoutInflater layoutInflater;
	private OnCategoryClickListener mOnclickListener;
	private int mCurrentIndex = 0;

	public PromotionCategoryView(Context context) {
		super(context);
		init();
	}

	public PromotionCategoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		layoutInflater = LayoutInflater.from(getContext());
		View view = layoutInflater.inflate(R.layout.view_promotion_category,
				this, true);
		mContainerLayout = (LinearLayout) view
				.findViewById(R.id.view_promotion_category_container);
		this.setHorizontalScrollBarEnabled(false);
	}

	public void setCategory(List<String> names) {
		mContainerLayout.removeAllViews();
		int size = (names == null) ? 0 : names.size();
		for (int i = 0; i < size; i++) {
			View view = layoutInflater.inflate(
					R.layout.item_promotion_category, null);
			((TextView) view.findViewById(R.id.item_promotion_category_tv))
					.setText(names.get(i));
			view.setOnClickListener(listener);
			mContainerLayout.addView(view);
		}
		chageState(mCurrentIndex);
	}
	
	public void setCategory(List<String> names , boolean isResetData){
		if (isResetData) {
			mCurrentIndex = 0;
		}
		setCategory(names);
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (mOnclickListener != null) {
				for (int i = 0; i < mContainerLayout.getChildCount(); i++) {
					if (view == mContainerLayout.getChildAt(i)) {
						if (i != mCurrentIndex) {
							mCurrentIndex = i;
							mOnclickListener.onClick(view, mCurrentIndex);
							chageState(mCurrentIndex);
						}
						break;
					}
				}
			}
		}
	};

	private void chageState(int position) {
		for (int i = 0; i < mContainerLayout.getChildCount(); i++) {
			if (i == position) {
				mContainerLayout.getChildAt(i)
						.findViewById(R.id.item_promotion_category_state)
						.setVisibility(VISIBLE);
			} else {
				mContainerLayout.getChildAt(i)
						.findViewById(R.id.item_promotion_category_state)
						.setVisibility(INVISIBLE);
			}
		}
	}

	public void setCategoryOnclickListener(OnCategoryClickListener listener) {
		if (listener != null) {
			mOnclickListener = listener;
		}
	}

	public interface OnCategoryClickListener {
		public void onClick(View view, int position);
	}

}
