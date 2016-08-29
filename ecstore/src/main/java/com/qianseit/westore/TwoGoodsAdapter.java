package com.qianseit.westore;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public abstract class TwoGoodsAdapter extends BaseAdapter implements
		OnClickListener {
	public final int[] ITEM_IDS = { R.id.goods_list_item_small_one,
			R.id.goods_list_item_small_two };

	public ArrayList<JSONObject> mGoodsList;

	public LayoutInflater inflater;
//	public ImageLoader mImageLoader;
	public Activity mActivity;
	private Resources res;
	public VolleyImageLoader mIImageLoader;

	public TwoGoodsAdapter(Activity activity, VolleyImageLoader imageLoader,
			ArrayList<JSONObject> items) {
		this.inflater = activity.getLayoutInflater();
		this.res = activity.getResources();
		this.mIImageLoader = imageLoader;
		this.mActivity = activity;
		if (items != null)
			this.mGoodsList = items;
		else
			this.mGoodsList = new ArrayList<JSONObject>();
	}

	@Override
	public int getCount() {
		return (int) Math.ceil(mGoodsList.size() / 2.0);
	}

	@Override
	public JSONObject getItem(int position) {
		return position >= mGoodsList.size() ? null : mGoodsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			int layout = R.layout.fragment_goods_list_item_small_image_double;
			convertView = inflater.inflate(layout, null);
			initConvertView(convertView);

			for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
				View childView = convertView.findViewById(ITEM_IDS[i]);
				View iconView = childView.findViewById(android.R.id.icon);
				int screenWidth = Run.getScreenSize(mActivity
						.getWindowManager()).x;
				LayoutParams params = iconView.getLayoutParams();
				params.height = (screenWidth - 3 * convertView.getPaddingLeft()) / 2;
				iconView.setLayoutParams(params);
				childView.setOnClickListener(this);
			}
		}

		for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
			JSONObject all = getItem(position * c + i);
			View childView = convertView.findViewById(ITEM_IDS[i]);
			childView.setVisibility(View.VISIBLE);
			if (all != null) {
				childView.setTag(all.optString("iid"));
				fillupItemView(childView, all, "thisuasm_url");
			} else {
				childView.setVisibility(View.INVISIBLE);
			}
		}

		return convertView;
	}

	/**
	 * convertView初始化
	 * 
	 * @param convertView
	 */
	public void initConvertView(View convertView) {
	}

	/**
	 * 填充列表信息
	 * 
	 * @param convertView
	 * @param all
	 * @param key
	 */
	public void fillupItemView(View convertView, JSONObject all, String key) {
		((TextView) convertView.findViewById(android.R.id.title)).setText(all
				.optString("title"));
		((TextView) convertView.findViewById(android.R.id.text1)).setText(Run
				.buildString("￥", all.optString("price")));
		((TextView) convertView.findViewById(android.R.id.text2)).setText(all
				.optString("goods_favorite_count"));
		try {
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			JSONArray imgJson = new JSONArray(all.optString("item_imgs"));
			JSONObject child = imgJson.getJSONObject(0);
			String imageUrl = child.optString("thisuasm_url");
			Uri imageUri = Uri.parse(imageUrl);
			iconView.setTag(imageUri);
//			mImageLoader.showImage(iconView, imageUri);
			mIImageLoader.showImage(iconView, imageUrl);
		} catch (Exception e) {
		}
	}

}
