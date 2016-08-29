package com.qianseit.westore.ui;

import com.qianseit.westore.AgentApplication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class CommonRadioButton extends RadioButton {

	public CommonRadioButton(Context context) {
		super(context);
		this.setTypeface(AgentApplication.getApp(getContext()).getTypeface());
	}

	public CommonRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setTypeface(AgentApplication.getApp(getContext()).getTypeface());
	}

	public CommonRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setTypeface(AgentApplication.getApp(getContext()).getTypeface());
	}

}
