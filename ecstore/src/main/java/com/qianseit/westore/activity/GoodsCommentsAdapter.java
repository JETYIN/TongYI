package com.qianseit.westore.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class GoodsCommentsAdapter extends BaseAdapter {
	private ArrayList<JSONObject> data = new ArrayList<JSONObject>();
	private JSONArray points;

	private LayoutInflater inflater;
	private Activity activity;
	private ViewGroup pointsArea;

	private int currentPage = 0;
	private String iid = "";
	private int discusstotalpage = 1;
	private boolean isLoadingData;//正在加载数据，避免同时加载多页

	public GoodsCommentsAdapter(Activity context, ViewGroup pointArea) {
		this.inflater = LayoutInflater.from(context);
		this.pointsArea = pointArea;
		this.activity = context;
	}

	public void addItems(ArrayList<JSONObject> items) {
		data.addAll(items);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public JSONObject getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			int layout = R.layout.fragment_goods_detail_comment;
			convertView = inflater.inflate(layout, null);
		}

		JSONObject all = getItem(position);
		if (all == null)
			return convertView;

		fillupItemView(convertView, all);

		return convertView;
	}

	/**
	 * 加载下一页
	 */
	public void loadNextPage(String iid) {
		if (currentPage >= discusstotalpage || isLoadingData) {
			return ;
		}
		this.iid = iid;
		this.currentPage += 1;
		Run.excuteJsonTask(new JsonTask(), new GetCommentsTask());
	}

	private void fillupItemView(View view, JSONObject all) {
		try {
			((TextView) view.findViewById(R.id.goods_detail_comment_uname))
					.setText(all.optString("author"));
			((TextView) view.findViewById(R.id.goods_detail_comment_content))
					.setText(all.optString("comment"));
			long time = all.optLong("time");
			((TextView) view.findViewById(R.id.goods_detail_comment_date))
					.setText(new SimpleDateFormat("MM-dd kk:mm").format(time));
		} catch (Exception e) {
		}
	}

	private void fillupPointArea() {
		try {
			for (int i = 0, c = points.length(); i < c; i++) {
				JSONObject all = points.getJSONObject(i);
				View view = inflater.inflate(
						R.layout.fragment_goods_detail_ratebar, null);
				((TextView) view.findViewById(R.id.goods_detail_ratebar_title))
						.setText(Run.buildString(all.optString("type_name"),
								":"));
				// 评分显示
				String total = all.optString("total");
				if (!TextUtils.isEmpty(total)
						&& !"null".equalsIgnoreCase(total)) {
					if (i == 0) {
						((TextView) pointsArea
								.findViewById(R.id.goods_detail_ratebar_avg_point))
								.setText(all.optString("avg"));
						((RatingBar) pointsArea
								.findViewById(R.id.goods_detail_ratebar_avg_rating))
								.setRating((float) all.optDouble("avg"));
						pointsArea.findViewById(
								R.id.goods_detail_ratebar_avg_item)
								.setVisibility(View.VISIBLE);
						continue;
					}

					((TextView) view
							.findViewById(R.id.goods_detail_ratebar_point))
							.setText(all.optString("total"));
					((RatingBar) view
							.findViewById(R.id.goods_detail_ratebar_point_ratebar))
							.setRating((float) all.optDouble("total"));
					((ViewGroup) pointsArea
							.findViewById(R.id.goods_detail_points_list))
							.addView(view);
				} else if (i == 0) {
					pointsArea.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
		}
	}

	private class GetCommentsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			isLoadingData = true;
			return new JsonRequestBean( "mobileapi.goods.comment")
					.addParams("iid", iid).addParams("page_no",
							String.valueOf(currentPage));
		}

		@Override
		public void task_response(String json_str) {
			isLoadingData = false;
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(activity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (points == null) {
						points = data.optJSONArray("_all_point");
						if (points == null || points.length() == 0) {
							pointsArea.setVisibility(View.GONE);
						} else {
							pointsArea.setVisibility(View.VISIBLE);
							fillupPointArea();
						}
					}
					discusstotalpage = data.optInt("discusstotalpage");
					JSONObject object = data.optJSONObject("list");
					JSONArray goods = object.optJSONArray("discuss");
					ArrayList<JSONObject> comments = new ArrayList<JSONObject>();
					for (int i = 0, c = goods.length(); i < c; i++)
						comments.add(goods.getJSONObject(i));
					addItems(comments);
				}
			} catch (Exception e) {
			}
		}
	}
}