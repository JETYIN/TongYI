package com.qianseit.westore.activity.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.PromotionCategoryView;
import com.qianseit.westore.ui.PromotionCategoryView.OnCategoryClickListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class AccountFavoriteGoodsFragment extends BaseDoFragment {

	private PullToRefreshListView mListView;
	private PromotionCategoryView mCategoryView;

	private int mPageNum = 0;
	private boolean isEditing;
	private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;
	private ArrayList<JSONObject> mFavGoods = new ArrayList<JSONObject>();
	private List<String> list = new ArrayList<String>();
	private List<JSONObject> mFavGoodsList = new ArrayList<JSONObject>();
	private int mCurrentIndex;
	private int totalCount;

	public AccountFavoriteGoodsFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageLoader = Run.getDefaultImageLoader(mActivity,
				mActivity.getResources());
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inf, ViewGroup container,
			Bundle savedInstanceState) {
		// mActionBar.setRightTitleButton(R.string.edit, null);
		mActionBar.setTitle(R.string.account_favorites);
		rootView = inf.inflate(R.layout.fragment_account_favorite_goods, null);
		mListView = (PullToRefreshListView) findViewById(android.R.id.list);
		mListView.getRefreshableView().setAdapter(new FavGoodsAdapter());
		mCategoryView = (PromotionCategoryView) rootView
				.findViewById(R.id.fragment_favourite_category);
		mCategoryView.setCategoryOnclickListener(clickListener);

		Run.excuteJsonTask(new JsonTask(), new FavGoodsTask());
	}

	private OnCategoryClickListener clickListener = new OnCategoryClickListener() {

		@Override
		public void onClick(View view, int position) {
			try {
				mCurrentIndex = position;
				mFavGoods.clear();
				JSONObject obj = mFavGoodsList.get(position);
				JSONArray products = obj.optJSONArray("product");
				int size = products == null ? 0 : products.length();
				for (int i = 0; i < size; i++) {
					mFavGoods.add(products.getJSONObject(i));
				}
				((BaseAdapter) mListView.getRefreshableView().getAdapter())
						.notifyDataSetChanged();
			} catch (Exception e) {

			} finally {
				findViewById(android.R.id.empty).setVisibility(
						mFavGoods.isEmpty() ? View.VISIBLE : View.GONE);
			}

		}
	};

	private class FavGoodsAdapter extends BaseAdapter implements
			OnClickListener {

		@Override
		public int getCount() {
			return mFavGoods.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mFavGoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_account_favorite_goods_item, null);

			fillupItemView(convertView, getItem(position));
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == android.R.id.toggle) {
				final JSONObject goods = (JSONObject) v.getTag();
				Run.excuteJsonTask(new JsonTask(), new JsonTaskHandler() {

					@Override
					public void task_response(String json_str) {
						hideLoadingDialog_mt();

						try {
							JSONObject res = new JSONObject(json_str);
							if (Run.checkRequestJson(mActivity, res)) {
								mFavGoods.remove(goods);
								findViewById(android.R.id.empty).setVisibility(
										mFavGoods.isEmpty() ? View.VISIBLE
												: View.GONE);
								String temp = list.get(mCurrentIndex);
								String s = temp.replaceAll("\\D", "");
								String count = Integer.parseInt(s) - 1 + "";
								temp = list.get(mCurrentIndex).replaceAll(
										"\\d", count);
								list.set(mCurrentIndex, temp);
								mCategoryView.setCategory(list);
								((BaseAdapter) mListView.getRefreshableView()
										.getAdapter()).notifyDataSetChanged();
								totalCount -= 1;
								mActionBar
										.setTitle(getString(R.string.account_favorites)
												+ "(" + totalCount + ")");
							}
						} catch (Exception e) {
						}
					}

					@Override
					public JsonRequestBean task_request() {
						showCancelableLoadingDialog();
						return new JsonRequestBean(
								"mobileapi.member.del_fav").addParams("gid",
								goods.optString("goods_id"));
					}
				});
			} else {
				String goodsIID = (String) v.getTag();
				Intent intent = AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
						Run.EXTRA_CLASS_ID, goodsIID);
				mActivity.startActivity(intent);
			}
		}

		public void fillupItemView(View view, JSONObject all) {
			view.setTag(all.optString("goods_id"));
			((TextView) view.findViewById(android.R.id.title)).setText(all
					.optString("name"));
			if (!all.isNull("price"))
				((TextView) view.findViewById(android.R.id.text1)).setText(Run
						.buildString("￥", all.optString("price")));
			view.findViewById(android.R.id.toggle).setOnClickListener(this);
			view.findViewById(android.R.id.toggle).setTag(all);
			view.setOnClickListener(this);

			try {
				ImageView iconView = (ImageView) view
						.findViewById(android.R.id.icon);
				String imageUrl = all.optString("image_default_url");
//				Uri imageUri = Uri.parse(imageUrl);
//				iconView.setTag(imageUri);
//				mImageLoader.showImage(iconView, imageUri);
				mVolleyImageLoader.showImage(iconView, imageUrl);
				
			} catch (Exception e) {
			}
		}
	}

	// 加载我喜欢的商品
	private class FavGoodsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.favorite").addParams("n_page",
					String.valueOf(mPageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray data = all.optJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject obj = data.getJSONObject(i);
						JSONArray products = obj.optJSONArray("product");
						int size = products == null ? 0 : products.length();
						if (mCurrentIndex == i) {
							for (int j = 0; j < size; j++) {
								mFavGoods.add(products.optJSONObject(j));
							}
						}
						list.add(obj.optString("cat_name") + "(" + size + ")");
						totalCount += size;
						mFavGoodsList.add(obj);
					}
					mActionBar.setTitle(getString(R.string.account_favorites)
							+ "(" + totalCount + ")");
					mCategoryView.setCategory(list);
					// JSONObject data = all.optJSONObject("data");
					// Iterator<String> keys = data.keys();
					// while (keys.hasNext()) {
					// String key = keys.next();
					// mFavGoods.add(data.optJSONObject(key));
					// }
					((BaseAdapter) mListView.getRefreshableView().getAdapter())
							.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				findViewById(android.R.id.empty).setVisibility(
						mFavGoods.isEmpty() ? View.VISIBLE : View.GONE);
			}
		}
	}

}
