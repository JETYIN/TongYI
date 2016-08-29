package com.qianseit.westore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import cn.shopex.ecstore.R;

public abstract class TopActivity extends FragmentActivity {

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.activity_fadein,
				R.anim.activity_scroll_to_right);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(R.anim.activity_scroll_from_right,
				R.anim.activity_fadeout);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		overridePendingTransition(R.anim.activity_scroll_from_right,
				R.anim.activity_fadeout);
	}

	@SuppressLint("NewApi")
	@Override
	public void startActivityForResult(Intent intent, int requestCode,
			Bundle options) {
		super.startActivityForResult(intent, requestCode, options);
		overridePendingTransition(R.anim.activity_scroll_from_right,
				R.anim.activity_fadeout);
	};
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
}
