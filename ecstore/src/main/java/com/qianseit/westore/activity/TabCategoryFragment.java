package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.google.zxing.CaptureActivity;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class TabCategoryFragment extends BaseDoFragment {
	private ListView mListViewLevel1;

//	private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;
	private BaseAdapter mAdapterLevel1;

	private ArrayList<JSONObject> mTopLevelArray = new ArrayList<JSONObject>();
	private HashMap<String, String> mSubLevels = new HashMap<String, String>();

	private String mCatesJson;

	public TabCategoryFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.actionbar_button_assort);
//		mImageLoader = Run.getDefaultImageLoader(mActivity,
//				mActivity.getResources());
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_tab_category, null);
		findViewById(R.id.fragment_main_button_scan).setOnClickListener(this);
		findViewById(R.id.fragment_main_search).setOnClickListener(this);
		mActionBar.setShowTitleBar(false);

		mListViewLevel1 = (ListView) findViewById(R.id.fragment_category_level1);
		mAdapterLevel1 = new TopLevelAdapter();
		mListViewLevel1.setAdapter(mAdapterLevel1);

		mListViewLevel1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String pid = mTopLevelArray.get(position).optString("cat_id");
				startActivity(AgentActivity
						.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_CATEGORY)
						.putExtra(Run.EXTRA_CLASS_ID, pid)
						.putExtra(Run.EXTRA_DATA, mCatesJson));
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		// 加载第一级分类目录
		if (mTopLevelArray.isEmpty())
			Run.excuteJsonTask(new JsonTask(), new TopLevelTask());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_main_button_scan) {
//			IntentIntegrator integrator = new IntentIntegrator(mActivity);
//			integrator.initiateScan();
			startActivity(new Intent(mActivity, CaptureActivity.class));
		} else if (v.getId() == R.id.fragment_main_search) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_GOODS_SEARCH));
		} else {
			super.onClick(v);
		}
	}

	private class TopLevelAdapter extends BaseAdapter {
		private Resources res;

		public TopLevelAdapter() {
			res = mActivity.getResources();
		}

		@Override
		public int getCount() {
			return mTopLevelArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mTopLevelArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_tab_category_item, null);
			}

			JSONObject all = getItem(position);
			if (all == null)
				return convertView;

			((TextView) convertView.findViewById(android.R.id.title))
					.setText(all.optString("cat_name"));
			((TextView) convertView.findViewById(android.R.id.text1))
					.setText(mSubLevels.get(all.optString("cat_id")));

			// 设置图标
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
//			Uri iconUri = Uri.parse(all.optString("picture"));
//			iconView.setTag(iconUri);
//			mImageLoader.showImage(iconView, iconUri);
			mVolleyImageLoader.showImage(iconView, all.optString("picture"));

			return convertView;
		}
	}

	/**
	 * 解析
	 * 
	 * @param all
	 */
	private void parseCategoryLevels(JSONArray all) {
		mTopLevelArray.clear();

		// 分拆顶级和二级分类
		if (all != null && all.length() > 0) {
			for (int i = 0, c = all.length(); i < c; i++) {
				try {
					JSONObject child = all.getJSONObject(i);
					int pid = child.optInt("pid");
					if (pid == 0) {
						mTopLevelArray.add(child);
					} else {
						String key = String.valueOf(pid);
						if (mSubLevels.containsKey(key)) {
							mSubLevels.put(key, mSubLevels.get(key) + "/"
									+ child.optString("cat_name"));
						} else {
							mSubLevels.put(key, child.optString("cat_name"));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class TopLevelTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.goods.get_cat")
					.addParams("page_no", "1");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mCatesJson = json_str;
					JSONObject child = all.optJSONObject("data");
					if (child != null && child.optJSONArray("datas") != null) {
						parseCategoryLevels(child.optJSONArray("datas"));
						mAdapterLevel1.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
			}
		}
	}
}
