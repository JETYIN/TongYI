package com.qianseit.westore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.android.volley.toolbox.NetworkImageView;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class PersonalTwoAdapter extends BaseAdapter implements OnClickListener {
	public final int[] ITEM_IDS = { R.id.collect_goods_list_item_one,
			R.id.collect_goods_list_item_two };

	public ArrayList<JSONObject> mGoodsList;
	public Resources res;
	private boolean isMy;
	public LayoutInflater mInflater;
	public Activity mActivity;
	public VolleyImageLoader mImageLoader;
	public SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    private BaseDoFragment fragment;
	public PersonalTwoAdapter(BaseDoFragment fragment,Activity activity, VolleyImageLoader imageLoader,
			ArrayList<JSONObject> items, boolean isMy) {
		this.fragment=fragment;
		this.mInflater = activity.getLayoutInflater();
		this.res = activity.getResources();
		this.mImageLoader = imageLoader;
		this.isMy = isMy;
		this.mActivity = activity;
		if (items != null)
			this.mGoodsList = items;
		else
			this.mGoodsList = new ArrayList<JSONObject>();
	}

	@Override
	public int getCount() {
		int t=mGoodsList.size();
		return  t%2>0?(t/2+1):t/2;
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
			int layout = R.layout.fragment_personal_goods_collect_list_item;
			convertView = mInflater.inflate(layout, null);
			for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
				View childView = convertView.findViewById(ITEM_IDS[i]);
				View iconView = childView.findViewById(R.id.goods_item_icon);
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
			if (all != null) {
				childView.setTag(all);
				childView.setVisibility(View.VISIBLE);
				fillupItemView(childView, all);
			} else {
				childView.setVisibility(View.INVISIBLE);
			}
		}

		return convertView;
	}

	public void fillupItemView(View convertView, JSONObject all) {
		NetworkImageView imageIcon = (NetworkImageView) convertView
				.findViewById(R.id.goods_item_icon);
		((TextView) convertView.findViewById(R.id.goods_item_price))
				.setText(Run.buildString("￥", all.optString("price")));
		((TextView) convertView.findViewById(R.id.goods_item_title))
				.setText(all.optString("name"));
		String time=all.optString("fav_add_time").trim();
		 Pattern p = Pattern.compile("[0-9]*"); 
	     Matcher m = p.matcher(time); 
	     if(m.matches()&&!"".equals(time)){
	    	 ((TextView) convertView.findViewById(R.id.goods_item_time)).setText(sd
	 				.format(new Date(
	 						Long.parseLong(time) * 1000)));
	      } 
		View viewCalcel = convertView.findViewById(R.id.goods_item_time_calcel);
		View viewRelative = convertView.findViewById(R.id.goods_item_time_relative);		
		viewCalcel.setOnClickListener(this);
		View viewAdd = convertView.findViewById(R.id.goods_item_time_add);
		viewAdd.setOnClickListener(this);
		if (isMy) {
			viewRelative.setVisibility(View.VISIBLE);
			if (all.isNull("isFav")) {
				viewCalcel.setVisibility(View.VISIBLE);
				viewAdd.setVisibility(View.GONE);
			} else {
				viewAdd.setVisibility(View.VISIBLE);
				viewCalcel.setVisibility(View.GONE);
			}
		}else{
			//viewRelative.setVisibility(View.GONE);
			viewAdd.setVisibility(View.GONE);
			viewCalcel.setVisibility(View.GONE);
		}
		viewCalcel.setTag(all);
		viewAdd.setTag(all);
		mImageLoader.showImage(imageIcon, all.optString("image_default_url"));
	}

	@Override
	public void onClick(View v) {
		JSONObject jsonObject = (JSONObject) v.getTag();
		switch (v.getId()) {
		case R.id.goods_item_icon:
			break;
		case R.id.goods_item_time_add:
			Run.excuteJsonTask(new JsonTask(), new AddCollectTask(jsonObject));
			break;
		case R.id.goods_item_time_calcel:
			Run.excuteJsonTask(new JsonTask(),
					new CalcelCollectTask(jsonObject));
			break;
		default:
			JSONObject json = (JSONObject) v.getTag();
			if (json != null) {
				String goodsIID = json.optString("goods_id");
				Intent intent = AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
						Run.EXTRA_CLASS_ID, goodsIID);
				mActivity.startActivity(intent);
			}
			break;
		}

	}

	private class AddCollectTask implements JsonTaskHandler {
		private String goodsId;
		private JSONObject jsonObject;

		public AddCollectTask(JSONObject jsonObject) {
			this.goodsId = jsonObject.optString("goods_id");
			this.jsonObject = jsonObject;
		}

		@Override
		public JsonRequestBean task_request() {
			fragment.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.add_fav");
			bean.addParams("gid", goodsId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			fragment.hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					try {
						Run.alert(mActivity, "收藏成功");
						jsonObject.remove("isFav");
					} catch (Exception e) {
						e.printStackTrace();
					}
					PersonalTwoAdapter.this.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private class CalcelCollectTask implements JsonTaskHandler {
		private String goodsId;
		private JSONObject jsonObject;

		public CalcelCollectTask(JSONObject jsonObject) {
			this.goodsId = jsonObject.optString("goods_id");
			this.jsonObject = jsonObject;
		}

		@Override
		public JsonRequestBean task_request() {
			fragment.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.del_fav");
			bean.addParams("gid", goodsId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			fragment.hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					try {
						Run.alert(mActivity, "已取消收藏");
						jsonObject.put("isFav", "add");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					PersonalTwoAdapter.this.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
