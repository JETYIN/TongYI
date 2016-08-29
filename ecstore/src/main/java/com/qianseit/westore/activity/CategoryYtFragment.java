package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

/**
 * 分类
 */
public class CategoryYtFragment extends BaseDoFragment {
	private ListView mListView;

	private VolleyImageLoader mVolleyImageLoader;
	private BaseAdapter mAdapterListview;
	private String mSelectedTopLevelId;
	private String mDataJson;
	private GridView mGridView;
	private LayoutInflater mInflater;
	private BaseAdapter mAdapterGridview;

	private ArrayList<JSONObject> mTopLevelArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mGridArray = new ArrayList<JSONObject>();
	private HashMap<String, ArrayList<JSONObject>> mSubLevels = new HashMap<String, ArrayList<JSONObject>>();

	public CategoryYtFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.actionbar_button_assort);
		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(false);
		mInflater = getLayoutInflater();
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		Intent data = mActivity.getIntent();
		mSelectedTopLevelId = data.getStringExtra(Run.EXTRA_CLASS_ID);
		String cat_name = data.getStringExtra(Run.EXTRA_TITLE);
		if (!TextUtils.isEmpty(cat_name))
			mActionBar.setTitle(cat_name);

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_shangcheng_category, null);

		mListView = (ListView) findViewById(R.id.fragment_category_lisetview);
		mGridView = (GridView) findViewById(R.id.fragment_category_gridview);
		findViewById(R.id.fragment_category_back).setOnClickListener(this);
		mAdapterListview = new TopLevelAdapter();
		mAdapterGridview = new GridAdapter();

		mListView.setAdapter(mAdapterListview);
		mGridView.setAdapter(mAdapterGridview);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JSONObject data = (JSONObject) view.getTag();
				if (data != null) {
					String cat_id = data.optString("cat_id");
					String title = data.optString("cat_name");

					mActivity.startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_GOODS_LIST)
							.putExtra(Run.EXTRA_CLASS_ID, cat_id)
							.putExtra(Run.EXTRA_TITLE, title));
				}
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
				mGridArray.clear();
				JSONObject data = mTopLevelArray.get(pos);
				String pid = data.optString("cat_id");
				mGridArray.add(data);
				String catName = data.optString("cat_name");

				mActionBar.setTitle(data.optString("cat_name"));
				if (mSubLevels.get(pid) != null)
					mGridArray.addAll(mSubLevels.get(pid));
				mSelectedTopLevelId = pid;
				mAdapterListview.notifyDataSetChanged();
				mAdapterGridview.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (TextUtils.isEmpty(mDataJson)) {
			Run.excuteJsonTask(new JsonTask(), new TopLevelTask());
		} else {
			// dataJsonLoaded(mDataJson);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_category_back) {
			getActivity().finish();
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
						R.layout.fragment_category_toplevel, null);
			}

			JSONObject all = getItem(position);
			if (all == null)
				return convertView;

			boolean isSelected = TextUtils.equals(mSelectedTopLevelId,
					all.optString("cat_id"));
			View text1 = convertView.findViewById(android.R.id.text1);
			((TextView) text1).setText(all.optString("cat_name"));
			text1.setSelected(isSelected);

			convertView.findViewById(R.id.fragment_category_toplevel_marker)
					.setVisibility(!isSelected ? View.INVISIBLE : View.VISIBLE);
			convertView.setBackgroundColor(!isSelected ? Color
					.parseColor("#f8f9fb") : Color.parseColor("#ffffff"));

			// 设置图标
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			if (!all.optString("picture").contains("http"))
				iconView.setImageBitmap(null);
			else
				mVolleyImageLoader
						.showImage(iconView, all.optString("picture"));

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
		mGridArray.clear();

		// 分拆顶级和二级分类
		if (all != null && all.length() > 0) {
			ArrayList<JSONObject> item;
			for (int i = 0, c = all.length(); i < c; i++) {
				try {
					JSONObject child = all.getJSONObject(i);
					int pid = child.optInt("pid");
					if (pid == 0) {
						mTopLevelArray.add(child);
					} else {
						String key = String.valueOf(pid);
						if (mSubLevels.containsKey(key)) {
							mSubLevels.get(key).add(child);
						} else {
							item = new ArrayList<JSONObject>();
							item.add(child);
							mSubLevels.put(key, item);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// 选中第一个顶级分类
		if (!mTopLevelArray.isEmpty()) {
			if (TextUtils.isEmpty(mSelectedTopLevelId)) {
				String pid = mTopLevelArray.get(0).optString("cat_id");
				mGridArray.add(mTopLevelArray.get(0));
				mSelectedTopLevelId = pid;
			}
			mGridArray.addAll(mSubLevels.get(mSelectedTopLevelId));
		}
	}

	private void dataJsonLoaded(String json_str) {
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all)) {
				this.mDataJson = json_str;
				JSONObject child = all.optJSONObject("data");
				if (child != null && child.optJSONArray("datas") != null) {
					parseCategoryLevels(child.optJSONArray("datas"));
					mAdapterListview.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 获取所有分类
	 */
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
			dataJsonLoaded(json_str);
		}
	}

	private class GridAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			return mGridArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			// TODO Auto-generated method stub
			return mGridArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.fragment_gridview_item, null);
			}
			ImageView iconView = (ImageView) convertView
					.findViewById(R.id.gridview_item_icon);
			TextView title = (TextView) convertView
					.findViewById(R.id.gridview_item_title);
			JSONObject all = getItem(position);
			convertView.setTag(all);
			if (position == 0) {
				iconView.setImageResource(R.drawable.goods_category_my);
				title.setText("本类商品");
				return convertView;

			}
			if (!all.optString("picture").contains("http"))
				iconView.setImageBitmap(null);
			else
				mVolleyImageLoader
						.showImage(iconView, all.optString("picture"));
			title.setText(all.optString("cat_name"));

			return convertView;
		}

	}
}
