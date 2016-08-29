package com.qianseit.westore.activity;

import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.android.volley.toolbox.NetworkImageView;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;


public class GoodsDetailBrandFragment extends BaseDoFragment {

//	private LayoutInflater mLayoutInflater;
	private String title;
	private TextView tvSales;
	private TextView tvPrice;
//	private TextView tvDescription;// 品牌描述
	private ListView mListView;
	private BrandAdapter mAdapter;
	private VolleyImageLoader mImageLoader;

	private JsonTask mTask;
	private int mPageNum;
	private boolean isLoadEnd;
	private boolean isSaleOrderby = true;

	private ArrayList<JSONObject> listgoods = new ArrayList<JSONObject>();

	private String brandId;// 品牌ID
	View headerView;

	public GoodsDetailBrandFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent data = mActivity.getIntent();
		title = data.getStringExtra(Run.EXTRA_TITLE);
		brandId = data.getStringExtra(Run.EXTRA_GOODS_DETAIL_BRAND);
		mActionBar.setShowTitleBar(true);
		mActionBar.setTitle(title);
		mImageLoader = AgentApplication.getApp(mActivity).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		mLayoutInflater = inflater;

		rootView = inflater.inflate(R.layout.fragment_goods_detail_brand, null);
		headerView = rootView.findViewById(R.id.grand_detail_header);
		headerView.setVisibility(View.INVISIBLE);
		tvSales = (TextView) headerView.findViewById(R.id.tv_sales);
		tvPrice = (TextView) headerView.findViewById(R.id.tv_price);
//		tvDescription = (TextView) headerView
//				.findViewById(R.id.grand_detail_desc);
		Run.removeFromSuperView(headerView);
		headerView.setLayoutParams(new AbsListView.LayoutParams(headerView
				.getLayoutParams()));
		mListView = (ListView) rootView.findViewById(R.id.grand_detail_list);
		mListView.addHeaderView(headerView);
		mAdapter = new BrandAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if ((mTask != null && mTask.isExcuting) || isLoadEnd
						|| totalItemCount < 10)
					return;
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 2)
					loadNextPage(mPageNum);
			}
		});

		tvSales.setOnClickListener(this);
		tvPrice.setOnClickListener(this);

		onClick(tvSales);
	}
	
	@Override
	public void onResume() {
		super.onResume();

	}
	
	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onClick(View v) {
		if (v == tvSales) {
			showAndHide(tvSales, true);
			showAndHide(tvPrice, false);
			isSaleOrderby = true;
			loadNextPage(0);
		} else if (v == tvPrice) {
			showAndHide(tvSales, false);
			showAndHide(tvPrice, true);
			isSaleOrderby = false;
			loadNextPage(0);
		} else {
			super.onClick(v);
		}
	}

	private void showAndHide(TextView view, boolean isShow) {
		Drawable drawable = mActivity.getResources().getDrawable(
				R.drawable.icon_red_line_horizontal);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		if (isShow) {
			view.setTextColor(mActivity.getResources().getColor(
					R.color.westore_red));
			view.setCompoundDrawables(null, null, null, drawable);
		} else {
			view.setTextColor(mActivity.getResources().getColor(
					R.color.westore_secondary_textcolor));
			view.setCompoundDrawables(null, null, null, null);

		}
	}

	private void loadNextPage(int pageNum) {
		mPageNum = pageNum + 1;
		if (mPageNum == 1) {
			isLoadEnd = false;
			listgoods.clear();
		}
		if ((mTask != null && mTask.isExcuting) || isLoadEnd) {
			return;
		}
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetBrandData());
	}

	/**
	 * 与个人主页的差不多显示 item 使用 个人主页的
	 * 
	 * @author chanson
	 * @CreatTime 2015-7-23 下午1:56:50
	 * 
	 */
	private class BrandAdapter extends BaseAdapter implements OnClickListener {

		private final int[] ITEM_IDS = { R.id.collect_goods_list_item_one,
				R.id.collect_goods_list_item_two };

		@Override
		public int getCount() {
			// return 0;
//			return listgoods.size();
			return (int) Math.ceil(listgoods.size() / 2.0);
		}

		@Override
		public JSONObject getItem(int position) {
			return position >= listgoods.size() ? null : listgoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(
						R.layout.fragment_personal_goods_collect_list_item,
						null);
				for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
					View childView = convertView.findViewById(ITEM_IDS[i]);
					View iconView = childView
							.findViewById(R.id.goods_item_icon);
					int screenWidth = Run.getScreenSize(mActivity
							.getWindowManager()).x;
					LayoutParams params = iconView.getLayoutParams();
					params.height = (screenWidth - 3 * convertView
							.getPaddingLeft()) / 2;
					iconView.setLayoutParams(params);
					childView.setOnClickListener(this);
					childView.findViewById(R.id.goods_item_time_relative).setVisibility(View.GONE);
				}
			}
			//
//			obj.optString("iid");// goods_id
//			obj.optString("title");// goods_title
//			obj.optString("price");// goods_price
			for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
				JSONObject all = getItem(position * c + i);
				View childView = convertView.findViewById(ITEM_IDS[i]);
				if (all != null) {
					childView.setTag(all);
					childView.setVisibility(View.VISIBLE);
					NetworkImageView imageIcon = (NetworkImageView) childView
							.findViewById(R.id.goods_item_icon);
					mImageLoader.showImage(imageIcon, all.optString("default_img_url"));
					((TextView) childView.findViewById(R.id.goods_item_price))
							.setText(Run.buildString("￥",all.optString("price")));
					((TextView) childView.findViewById(R.id.goods_item_title))
							.setText(all.optString("title"));
					childView.setTag(all);
				} else {
					childView.setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				JSONObject obj = (JSONObject) v.getTag();
				startActivity(AgentActivity.intentForFragment(
						mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
						.putExtra(Run.EXTRA_CLASS_ID,obj.optString("iid")));
			}
		}

	}

	/**
	 * 获取品牌列表数据
	 * 
	 * @author chanson
	 * @CreatTime 2015-7-23 下午1:52:09
	 * 
	 */
	private class GetBrandData implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						JSONObject items = data.optJSONObject("items");
						JSONArray list = items.optJSONArray("item");
						int count = list == null ? 0 : list.length();
						for (int i = 0; i < count; i++) {
							listgoods.add(list.getJSONObject(i));
						}
					}
					if (data.optInt("total_results") <= listgoods.size()) {
						isLoadEnd = true;
					}
					if (mPageNum == 1) {
						JSONObject band = data.optJSONObject("brand");
						AgentApplication
								.getApp(mActivity)
								.getImageLoader()
								.showImage(
										(ImageView) headerView
												.findViewById(R.id.goods_detail_buy_thumb),
										band.getString("logo_src"));
						((TextView) headerView
								.findViewById(R.id.grand_detail_desc))
								.setText(band.getString("brand_desc"));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				headerView.setVisibility(View.VISIBLE);
				mAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.brand");
			req.addParams("brand_id", brandId);
			req.addParams("page_no", String.valueOf(mPageNum));
			req.addParams("son_object", "json");
			if (isSaleOrderby) {
				req.addParams("orderby", "buy_count desc");
			} else {
				req.addParams("orderby", "price asc");
			}
			return req;
		}

	}
}
