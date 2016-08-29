package com.qianseit.westore.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;

public class CommonTextView extends TextView {

	public CommonTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(AgentApplication.getApp(getContext()).getTypeface());
	}

	public CommonTextView(Context context) {
		super(context);
		setTypeface(AgentApplication.getApp(getContext()).getTypeface());
	}
	
	public CommonTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(AgentApplication.getApp(getContext()).getTypeface());
	}

}
