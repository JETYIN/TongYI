package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

public class CategoryThirdFragment extends BaseDoFragment {
	private ListView mListViewLevel1;

	private BaseAdapter mAdapterLevel1;
//	private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;
	

	private String mParentClassId;
	private ArrayList<JSONObject> mTopLevelArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mTopGoods = new ArrayList<JSONObject>();

	public CategoryThirdFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mImageLoader = Run.getDefaultImageLoader(mActivity,
//				mActivity.getResources());
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();

		Intent data = mActivity.getIntent();
		mParentClassId = data.getStringExtra(Run.EXTRA_CLASS_ID);
		String cat_name = data.getStringExtra(Run.EXTRA_TITLE);
		String dataJson = data.getStringExtra(Run.EXTRA_DATA);
		if (!TextUtils.isEmpty(cat_name))
			mActionBar.setTitle(cat_name);
		dataJsonLoaded(dataJson);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_category_third, null);
		findViewById(R.id.fragment_category_topgoods).setVisibility(
				View.VISIBLE);

		mListViewLevel1 = (ListView) findViewById(R.id.fragment_category_level1);
		mAdapterLevel1 = new TopLevelAdapter();
		mListViewLevel1.setAdapter(mAdapterLevel1);
		mListViewLevel1.setDividerHeight(0);

		mListViewLevel1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JSONObject data = mTopLevelArray.get(position);
				String cat_id = data.optString("cat_id");
				String title = data.optString("cat_name");
				mActivity.startActivity(AgentActivity
						.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_GOODS_LIST)
						.putExtra(Run.EXTRA_CLASS_ID, cat_id)
						.putExtra(Run.EXTRA_TITLE, title));
			}
		});

		Run.excuteJsonTask(new JsonTask(), new TopGoodsTask());
	}

	private void dataJsonLoaded(String json_str) {
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all)) {
				JSONObject child = all.optJSONObject("data");
				if (child != null && child.optJSONArray("datas") != null) {
					parseCategoryLevels(child.optJSONArray("datas"));
					mAdapterLevel1.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
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
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				view = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_tab_category_item, null);
				view.findViewById(android.R.id.icon).setVisibility(View.GONE);
				view.findViewById(android.R.id.title).setVisibility(View.GONE);
			}

			JSONObject all = getItem(position);
			if (all == null)
				return view;

			((TextView) view.findViewById(android.R.id.text1)).setText(all
					.optString("cat_name"));
			if (getCount() == 1) {
				view.setBackgroundResource(R.drawable.account_login_list_single);
			} else if (position == 0) {
				view.setBackgroundResource(R.drawable.account_login_list_topbg);
			} else if (position == getCount() - 1) {
				view.setBackgroundResource(R.drawable.account_login_list_bottombg);
			} else {
				view.setBackgroundResource(R.drawable.account_login_list_middlebg);
			}

			// 设置图标

			return view;
		}
	}

	/**
	 * 解析
	 * 
	 * @param all
	 */
	private void parseCategoryLevels(JSONArray all) {
		if (all != null && all.length() > 0) {
			mTopLevelArray.clear();
			int parentID = Integer.parseInt(mParentClassId);
			for (int i = 0, c = all.length(); i < c; i++) {
				try {
					JSONObject child = all.getJSONObject(i);
					int pid = child.optInt("pid");
					if (pid == parentID)
						mTopLevelArray.add(child);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			mAdapterLevel1.notifyDataSetChanged();
		}
	}

	/**
	 * 解析
	 * 
	 * @param all
	 */
	private void parseGoodsList(JSONObject top) {
		JSONArray all = top.optJSONArray("item");
		if (all != null && all.length() > 0) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			ViewGroup topGoodsLayout = (ViewGroup) findViewById(R.id.fragment_category_topgoods);
			for (int i = 0, c = all.length(); i < c; i++) {
				try {
					JSONObject child = all.getJSONObject(i);
					View itemView = inflater.inflate(
							R.layout.fragment_category_third_topgoods_item,
							null);
					itemView.setTag(child);
					itemView.setOnClickListener(mItemClickListener);

					((TextView) itemView.findViewById(android.R.id.text1))
							.setText(child.optString("title"));
					((TextView) itemView.findViewById(android.R.id.text2))
							.setText(Run.buildString("￥",
									child.optString("price")));
					try {
						ImageView iconView = (ImageView) itemView
								.findViewById(android.R.id.icon);
						//String imageUrl = child.optString("default_img_url");
						JSONArray imgJson = child.optJSONArray("item_imgs");
						JSONObject child1 = null;
						String imageUrl = "";
						if (imgJson != null && imgJson.length() > 0) {
							child1 = imgJson.getJSONObject(0);
							imageUrl = child1.optString("thisuasm_url");
						}
						if (TextUtils.isEmpty(imageUrl)) {
							imageUrl = child.optString("small_url");
						}
//						Uri imageUri = Uri.parse(imageUrl);
//						iconView.setTag(imageUri);
//						mImageLoader.showImage(iconView, imageUri);
						mVolleyImageLoader.showImage(iconView, imageUrl);
					} catch (Exception e) {
					}
					topGoodsLayout.addView(itemView);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private OnClickListener mItemClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() == null)
				return;

			JSONObject obj = (JSONObject) v.getTag();
			String goodsIID = obj.optString("iid");
			JSONArray skus = obj.optJSONArray("skus");
			String product_id = skus.optJSONObject(0).optString("sku_id");
			Intent intent = AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
					Run.EXTRA_CLASS_ID, goodsIID).putExtra(Run.EXTRA_PRODUCT_ID, product_id);
			mActivity.startActivity(intent);
		}
	};

	private class TopGoodsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.get_all_list").addParams("page_no", "1")
					.addParams("page_size", "10");
			if (!TextUtils.isEmpty(mParentClassId))
				bean.addParams("cat_id", mParentClassId);
			bean.addParams("son_object", "json");
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject child = all.optJSONObject("data");
					if (child != null
							&& !TextUtils.isEmpty(child.optString("items"))) {
						parseGoodsList(new JSONObject(child.optString("items")));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

}
