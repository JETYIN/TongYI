package com.qianseit.westore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import cn.shopex.ecstore.R;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;


public class LauncherActivity extends DoActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		if (Run.loadFlag(this) < Run.getVersionCode(this)) {
			setMainFragment(new SplashFragment());
		} else {
			mHandler.sendEmptyMessageDelayed(0, 1500);
			getWindow().getDecorView()
					.setBackgroundResource(R.drawable.launcher);
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			startActivity(new Intent(getBaseContext(),
					MainTabFragmentActivity.class));
			finish();
		};
	};
	
	@Override
	protected void onResume() {
		super.onResume();

	}

	public void onPause() {
		super.onPause();

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

}
