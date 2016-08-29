package com.qianseit.westore.activity;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.Run;
import com.qianseit.westore.TwoGoodsAdapter;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class GoodsRecommendAdapter extends TwoGoodsAdapter {

	private String iid = "";
	private GestureDetector mGestureDetector;

	public GoodsRecommendAdapter(Activity activity, VolleyImageLoader loader) {
		super(activity, loader, null);
	}

	/**
	 * 填充列表信息
	 * 
	 * @param convertView
	 * @param all
	 * @param key
	 */
	@Override
	public void fillupItemView(View convertView, JSONObject all, String key) {
		convertView.setTag(all.optString("goods_id"));
		((TextView) convertView.findViewById(android.R.id.title)).setText(all
				.optString("name"));
		((TextView) convertView.findViewById(android.R.id.text1)).setText(Run
				.buildString("￥", all.optString("price")));

		String favCount = all.optString("goods_favorite_count");
		favCount = TextUtils.isEmpty(favCount) ? "0" : favCount;
		((TextView) convertView.findViewById(android.R.id.text2))
				.setText(favCount);

		try {
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			String imageUrl = all.optString("image_default");
			Uri imageUri = Uri.parse(imageUrl);
			iconView.setTag(imageUri);
			mIImageLoader.showImage(iconView, imageUrl);
		} catch (Exception e) {
		}
	}

	/* 加载推荐的商品 */
	public void loadRecommendGoods(String iid) {
		this.iid = iid;
		if (!mGoodsList.isEmpty())
			return;

		Run.excuteJsonTask(new JsonTask(), new GetGoodsRecommendTask());
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() != null && !TextUtils.isEmpty((String) v.getTag()))
			openGoodsDetailPage((String) v.getTag());
	}

	private void openGoodsDetailPage(String goodsIID) {
		Intent intent = AgentActivity.intentForFragment(mActivity,
				AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
				Run.EXTRA_CLASS_ID, goodsIID);
		mActivity.startActivity(intent);
		mActivity.finish();
	}

	private class GetGoodsRecommendTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.goods.goodslink")
					.addParams("iid", iid);
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray goods = all.optJSONArray("data");
					for (int i = 0, c = goods.length(); i < c; i++)
						mGoodsList.add(goods.getJSONObject(i));
					notifyDataSetChanged();
				}
			} catch (Exception e) {
			}
		}
	}
}