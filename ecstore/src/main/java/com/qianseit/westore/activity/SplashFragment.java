package com.qianseit.westore.activity;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.qianseit.frame.widget.pagetabs.ViewPagerTabProvider;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import cn.shopex.ecstore.R;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.Json;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTaskHandler;

public class SplashFragment extends BaseDoFragment {
	private final int[] splash_bgimages = {R.drawable.transparent };

	public SplashFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(false);

		rootView = inflater.inflate(R.layout.fragment_splash, null);
		findViewById(R.id.splash_pages_next).setOnClickListener(this);

		final ViewPager pager = (ViewPager) findViewById(R.id.splash_pages);
		pager.setAdapter(new FPAdapter((DoActivity) mActivity));
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE
						&& pager.getCurrentItem() == splash_bgimages.length - 1) {
					mActivity.startActivity(new Intent(mActivity,
							MainTabFragmentActivity.class));
					Run.saveFlag(mActivity, Run.getVersionCode(mActivity));
					mActivity.finish();
				}
			}
		});
	}

	/* 分页视图适配器 */
	private class FPAdapter extends FragmentPagerAdapter implements
			ViewPagerTabProvider {

		public FPAdapter(DoActivity activity) {
			super(activity.getSupportFragmentManager());
		}

		@Override
		public int getCount() {
			return splash_bgimages.length;
		}

		@Override
		public String getTitle(int position) {
			return Run.EMPTY_STR;
		}

		@Override
		public Fragment getItem(int position) {
			BaseDoFragment fragment;
			fragment = new SplashPageFragment();
			Bundle bundle = new Bundle();
			bundle.putInt(Run.EXTRA_VALUE, splash_bgimages[position]);
			fragment.setArguments(bundle);
			return fragment;
		}
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.splash_pages_next) {
			mActivity.startActivity(new Intent(mActivity,
					MainTabFragmentActivity.class));
			Run.saveFlag(mActivity, Run.getVersionCode(mActivity));
			mActivity.finish();
		}
	}

	private class SplashPageFragment extends BaseDoFragment {
		public SplashPageFragment() {
			super();
		}

		@Override
		public void init(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			mActionBar.setShowTitleBar(false);
			mActionBar.setShowHomeView(false);

			rootView = new ImageView(mActivity);
			rootView.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			try {
				rootView.setBackgroundResource(getArguments().getInt(
						Run.EXTRA_VALUE));
			} catch (Exception e) {
			}
		}
	}

	public static class UpdateJsonTask implements JsonTaskHandler {
		private Context mCtx;

		public UpdateJsonTask(Context context) {
			this.mCtx = context;
		}

		@Override
		public JsonRequestBean task_request() {
			return new com.qianseit.westore.http.JsonRequestBean(
					"");
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);

				// applock版本更新
				JSONArray versions = Json.getJsonArray(all, "version");
				if (versions == null || versions.length() == 0)
					return;

				long myVerCode = Run.getVersionCode(mCtx);
				long newVerCode = all.optLong("version_code");
				if (newVerCode > myVerCode) {
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

}
