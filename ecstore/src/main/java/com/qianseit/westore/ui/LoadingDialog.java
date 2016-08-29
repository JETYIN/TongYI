package com.qianseit.westore.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import cn.shopex.ecstore.R;

public class LoadingDialog extends Dialog {

	ImageView loadingImg;
	
	public LoadingDialog(Context context) {
		super(context,R.style.Loading_Dialog);
		init();
	}

	public LoadingDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	public LoadingDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	private void init(){
		this.setContentView(R.layout.dialog_loading);
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
		
		loadingImg = (ImageView) this.findViewById(R.id.loading_loading);
		Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.progress_anim);
		a.setDuration(500);
		a.setInterpolator(new Interpolator() {
		    private final int frameCount = 12;

		    @Override
		    public float getInterpolation(float input) {
		        return (float)Math.floor(input*frameCount)/frameCount;
		    }
		});
		loadingImg.startAnimation(a);
	}
	
}
