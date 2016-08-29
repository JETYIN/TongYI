package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
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

public class CategoryFragment extends BaseDoFragment {
	private ListView mListViewLevel1;
	// private ListView mListViewLevel2;
	private ExpandableListView mListViewLevel3;

	private VolleyImageLoader mVolleyImageLoader;
	// private ImageLoader mImageLoader;
	private BaseAdapter mAdapterLevel1;
	// private BaseAdapter mAdapterLevel2;
	private MyExpandableListAdapter mExpandableListAdapter;
	private String mSelectedTopLevelId;
	private String mDataJson;

	private ArrayList<JSONObject> mTopLevelArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mSubLevelArray = new ArrayList<JSONObject>();
	private HashMap<String, ArrayList<JSONObject>> mSubLevels = new HashMap<String, ArrayList<JSONObject>>();

	public CategoryFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.actionbar_button_assort);
		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(false);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();

		Intent data = mActivity.getIntent();
		mSelectedTopLevelId = data.getStringExtra(Run.EXTRA_CLASS_ID);
		String cat_name = data.getStringExtra(Run.EXTRA_TITLE);
		if (!TextUtils.isEmpty(cat_name))
			mActionBar.setTitle(cat_name);

		// mImageLoader = Run.getDefaultImageLoader(mActivity,
		// mActivity.getResources());
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_category, null);

		findViewById(R.id.fragment_main_button_scan).setOnClickListener(this);
		findViewById(R.id.fragment_main_search).setOnClickListener(this);

		mListViewLevel1 = (ListView) findViewById(R.id.fragment_category_level1);
		// mListViewLevel2 = (ListView)
		// findViewById(R.id.fragment_category_level2);
		mListViewLevel3 = (ExpandableListView) findViewById(R.id.fragment_category_level3);
		mAdapterLevel1 = new TopLevelAdapter();
		// mAdapterLevel2 = new SubLevelAdapter();
		mExpandableListAdapter = new MyExpandableListAdapter();
		mListViewLevel1.setAdapter(mAdapterLevel1);
		// mListViewLevel2.setAdapter(mAdapterLevel2);
		mListViewLevel3.setAdapter(mExpandableListAdapter);

		mListViewLevel1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
				mSubLevelArray.clear();
				JSONObject data = mTopLevelArray.get(pos);
				String pid = data.optString("cat_id");
				mActionBar.setTitle(data.optString("cat_name"));

				if (mSubLevels.get(pid) != null)
					mSubLevelArray.addAll(mSubLevels.get(pid));
				mSelectedTopLevelId = pid;
				mAdapterLevel1.notifyDataSetChanged();
				// mAdapterLevel2.notifyDataSetChanged();
				// mListViewLevel2.setSelection(0);
				position = 0;
				mListViewLevel3.smoothScrollToPosition(0);
				mExpandableListAdapter.notifyDataSetChanged();
				if (mExpandableListAdapter.getChildrenCount(0) > 1)
					mListViewLevel3.expandGroup(0);
			}
		});

		mListViewLevel3.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				for (int i = 0; i < mExpandableListAdapter.getGroupCount(); i++) {
					if (groupPosition != i)
						mListViewLevel3.collapseGroup(i);
				}

				mExpandableListAdapter.notifyDataSetChanged();
			}
		});

		mListViewLevel3.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				if (mExpandableListAdapter.getChildrenCount(groupPosition) == 1) {
					JSONObject data = mExpandableListAdapter
							.getGroup(groupPosition);
					String cat_id = data.optString("cat_id");
					String title = data.optString("cat_name");
					mActivity.startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_GOODS_LIST)
							.putExtra(Run.EXTRA_CLASS_ID, cat_id)
							.putExtra(Run.EXTRA_TITLE, title));
					return true;
				}

				if (position == -1) {
					// 展开被选的group
					mListViewLevel3.expandGroup(groupPosition);
					// 设置被选中的group置于顶端
					mListViewLevel3.setSelectedGroup(groupPosition);
					position = groupPosition;
				} else if (position == groupPosition) {
					mListViewLevel3.collapseGroup(position);
					position = -1;
				} else {
					mListViewLevel3.collapseGroup(position);
					// 展开被选的group
					mListViewLevel3.expandGroup(groupPosition);
					// 设置被选中的group置于顶端
					mListViewLevel3.setSelectedGroup(groupPosition);
					position = groupPosition;
				}
				return true;
			}
		});

		// mListViewLevel2.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// String cat_id = mSubLevelArray.get(position)
		// .optString("cat_id");
		// String title = mSubLevelArray.get(position).optString(
		// "cat_name");
		// JSONObject obj = mSubLevelArray.get(position);
		// ArrayList<JSONObject> subLevels = mSubLevels.get(obj
		// .optString("cat_id"));
		// if (subLevels.size() > 1) {
		// mActivity.startActivity(AgentActivity
		// .intentForFragment(mActivity,
		// AgentActivity.FRAGMENT_CATEGORY_THIRD)
		// .putExtra(Run.EXTRA_CLASS_ID, cat_id)
		// .putExtra(Run.EXTRA_TITLE, title)
		// .putExtra(Run.EXTRA_DATA, mDataJson));
		// } else {
		// mActivity.startActivity(AgentActivity
		// .intentForFragment(mActivity,
		// AgentActivity.FRAGMENT_GOODS_LIST)
		// .putExtra(Run.EXTRA_CLASS_ID, cat_id)
		// .putExtra(Run.EXTRA_TITLE, title));
		// }
		// ArrayList<JSONObject> subLevels = mSubLevels.get(cat_id);
		// mActivity.startActivity(AgentActivity
		// .intentForFragment(mActivity,
		// AgentActivity.FRAGMENT_CATEGORY_THIRD)
		// .putExtra(Run.EXTRA_CLASS_ID, cat_id)
		// .putExtra(Run.EXTRA_TITLE, title)
		// .putExtra(Run.EXTRA_DATA, mDataJson));
		// }
		// });

		// 加载第一级分类目录
		// String dataJson =
		// mActivity.getIntent().getStringExtra(Run.EXTRA_DATA);
		// if (TextUtils.isEmpty(dataJson))
		// Run.excuteJsonTask(new JsonTask(), new TopLevelTask());
		// else
		// dataJsonLoaded(dataJson);
	}

	private int position = -1;

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

			// 设置图标
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			// Uri iconUri = Uri.parse(all.optString("picture"));
			// iconView.setTag(iconUri);
			// mImageLoader.showImage(iconView, iconUri);
			if (!all.optString("picture").contains("http"))
				iconView.setImageBitmap(null);
			else 
				mVolleyImageLoader.showImage(iconView, all.optString("picture"));

			return convertView;
		}
	}

	private class SubLevelAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mSubLevelArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mSubLevelArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_category_sublevel, null);
			}

			JSONObject all = getItem(position);
			if (all == null)
				return convertView;

			((TextView) convertView.findViewById(android.R.id.text1))
					.setText(all.optString("cat_name"));

			// 子分类
			TextView textSubCat = (TextView) convertView
					.findViewById(android.R.id.text2);
			textSubCat.setText(Run.EMPTY_STR);
			ArrayList<JSONObject> subLevels = mSubLevels.get(all
					.optString("cat_id"));
			int c = (subLevels == null) ? 0 : subLevels.size();
			for (int i = 0; (i < 5 && i < c); i++) {
				textSubCat.append(subLevels.get(i).optString("cat_name"));
				if (i != (c - 1) && c > 1)
					textSubCat.append("/");
			}

			// 设置图标
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			// Uri iconUri = Uri.parse(all.optString("picture"));
			// iconView.setTag(iconUri);
			// mImageLoader.showImage(iconView, iconUri);
			mVolleyImageLoader.showImage(iconView, all.optString("picture"));

			return convertView;
		}
	}

	private class MyExpandableListAdapter extends BaseExpandableListAdapter
			implements View.OnClickListener {

		@Override
		public int getGroupCount() {
			return mSubLevelArray.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			String catId = "";
			if (mSubLevelArray.size() > groupPosition) {
				if (mSubLevelArray.get(groupPosition) != null) {
					catId = mSubLevelArray.get(groupPosition).optString("cat_id");
					if (!TextUtils.isEmpty(catId)) {
						if (mSubLevels.get(catId) == null) {
							return 1;
						}
						int size = mSubLevels.get(mSubLevelArray.get(groupPosition).optString("cat_id")).size();
						if (size > 1) {
							return size + 1;
						} else {
							return size;
						}
					}
				}
			}
			return 0;
		}

		@Override
		public JSONObject getGroup(int groupPosition) {
			return mSubLevelArray.get(groupPosition);
		}

		@Override
		public JSONObject getChild(int groupPosition, int childPosition) {
			return mSubLevels.get(
					mSubLevelArray.get(groupPosition).optString("cat_id")).get(
					childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_category_sublevel, null);
			}
			JSONObject all = getGroup(groupPosition);
			if (all == null)
				return convertView;
			((TextView) convertView.findViewById(android.R.id.text1))
					.setText(all.optString("cat_name"));
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			if (!all.optString("picture").contains("http"))
				iconView.setImageBitmap(null); 
			else
				mVolleyImageLoader.showImage(iconView, all.optString("picture"));
			ImageView arrowView = (ImageView) convertView
					.findViewById(android.R.id.icon1);
			if (getChildrenCount(groupPosition) == 1) {
				arrowView.setImageResource(R.drawable.arrow_right_pink);
			} else if (groupPosition == position) {
				arrowView.setImageResource(R.drawable.arrow_up_pink);
			} else {
				arrowView.setImageResource(R.drawable.arrow_down_pink);
			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.item_expand_child_view, null);
			}
			convertView.setOnClickListener(this);
			JSONObject all = null;
			// if (getChildrenCount(groupPosition) == 1) {
			// convertView.setTag(getGroup(groupPosition));
			// } else {
			// convertView.setTag(all);
			// }
			if (childPosition == getChildrenCount(groupPosition) - 1) {
				all = getGroup(groupPosition);
				convertView.setTag(all);
				((TextView) convertView
						.findViewById(R.id.item_expand_child_view_name))
						.setText("全部");
			} else {
				all = getChild(groupPosition, childPosition);
				convertView.setTag(all);
				((TextView) convertView
						.findViewById(R.id.item_expand_child_view_name))
						.setText(all.optString("cat_name"));
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				JSONObject data = (JSONObject) v.getTag();
				String cat_id = data.optString("cat_id");
				String title = data.optString("cat_name");
				mActivity.startActivity(AgentActivity
						.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_GOODS_LIST)
						.putExtra(Run.EXTRA_CLASS_ID, cat_id)
						.putExtra(Run.EXTRA_TITLE, title));
			}
		}

	}

	/**
	 * 解析
	 * 
	 * @param all
	 */
	private void parseCategoryLevels(JSONArray all) {
		mTopLevelArray.clear();
		mSubLevelArray.clear();

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
				mSelectedTopLevelId = pid;
			}
			mSubLevelArray.addAll(mSubLevels.get(mSelectedTopLevelId));
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
					mAdapterLevel1.notifyDataSetChanged();
					// mAdapterLevel2.notifyDataSetChanged();
					mExpandableListAdapter.notifyDataSetChanged();
					if (mExpandableListAdapter.getChildrenCount(0) > 1) {
						position = 0;
						mListViewLevel3.expandGroup(0);
					}
				}
			}
		} catch (Exception e) {
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
			dataJsonLoaded(json_str);
		}
	}
}
