package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.TwoGoodsAdapter;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.PromotionCategoryView;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class CoudanFragment extends BaseDoFragment {
	
	private VolleyImageLoader mVolleyImageLoader;
	private PromotionCategoryView mCategoryView;
	private ListView mListView;
	
	private ArrayList<JSONObject> tabNameList = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> tabGoodsList = new ArrayList<JSONObject>();

	public CoudanFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("凑单商品");
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_coudan, null);
		mCategoryView = (PromotionCategoryView) rootView.findViewById(R.id.fragment_coudan_promotionCategoryView1);
		mCategoryView.setCategoryOnclickListener(new MyOnCategoryClickListener());
		mListView = (ListView) rootView.findViewById(R.id.fragment_coudan_listview);
		new JsonTask().execute(new GetForOrder());
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.addToshopcar) {
			String product_id = (String) v.getTag();
			new JsonTask().execute(new AddToShoppingCar(product_id));
		}
	}
	
	private class MyOnCategoryClickListener implements PromotionCategoryView.OnCategoryClickListener{

		@Override
		public void onClick(View view, int position) {
			new JsonTask().execute(new GetForOrder(tabNameList.get(position).optString("tab_filter")));
		}
		
	}
	
	private class MyListAdapter extends TwoGoodsAdapter{

		public MyListAdapter(Activity activity, VolleyImageLoader imageLoader,
				ArrayList<JSONObject> items) {
			super(activity, imageLoader, items);
		}
		
		@Override
		public void fillupItemView(View convertView, JSONObject all, String key) {
			CoudanFragment.this.fillupItemView(convertView, all, key);
		}

		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_first)  != null) {
				
				String goodsIID = (String) v.getTag(R.id.tag_first);
				Intent intent = AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
								Run.EXTRA_CLASS_ID, goodsIID);
				mActivity.startActivity(intent);
			}
		}
		
	}
	
	private void fillupItemView(View convertView, JSONObject all, String key) {
		TextView titleView = ((TextView) convertView.findViewById(android.R.id.title));
		titleView.setText(all.optString("name"));
		titleView.setSingleLine(false);
		titleView.setLines(2);
		int paddingSmall = mActivity.getResources().getDimensionPixelSize(
				R.dimen.PaddingSmall);
		titleView.setPadding(paddingSmall, 0, paddingSmall, 0);
		convertView.findViewById(android.R.id.summary).setVisibility(View.GONE);
		((TextView) convertView.findViewById(android.R.id.text1)).setText(Run
				.buildString("￥", all.optString("price")));
		ImageView iconView = (ImageView) convertView
				.findViewById(android.R.id.icon);
		View viewBtn = convertView.findViewById(R.id.addToshopcar);
		viewBtn.setVisibility(View.VISIBLE);
		viewBtn.setOnClickListener(this);
		viewBtn.setTag(all.optString("product_id"));
		convertView.setTag(R.id.tag_first , all.optString("goods_id"));
		iconView.setImageBitmap(null);
		mVolleyImageLoader.showImage(iconView, all.optString("default_img_url"));
		((FrameLayout) iconView.getParent()).setForeground(null);
	}
	
	private class GetForOrder implements JsonTaskHandler {

		private String tabName;
		
		public GetForOrder(){}
		
		public GetForOrder(String tab_name){
			tabName = tab_name;
			
		}
		
		
		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						JSONArray fororder_tab = data.optJSONArray("fororder_tab");
						if (tabNameList.size() < 1 &&  fororder_tab != null ) {
							for (int i = 0; i < fororder_tab.length(); i++) {
								tabNameList.add(fororder_tab.optJSONObject(i));
							}
						}
						JSONArray goods_list = data.optJSONArray("goods_list");
						tabGoodsList.clear();
						if (goods_list != null && goods_list.length() > 0) {
							for (int i = 0; i < goods_list.length(); i++) {
								tabGoodsList.add(goods_list.getJSONObject(i));
							}
						}
						if (TextUtils.isEmpty(tabName)) {
							ArrayList<String> tabNameList = new ArrayList<String>();
							if (fororder_tab != null) {
								for (int i = 0; i < fororder_tab.length(); i++) {
									tabNameList.add(fororder_tab.optJSONObject(i).optString("tab_name"));
								}
							}
							mCategoryView.setCategory(tabNameList);
							mCategoryView.setCategoryOnclickListener(new MyOnCategoryClickListener());
							mListView.setAdapter(new MyListAdapter(mActivity, mVolleyImageLoader, tabGoodsList));
						} else {
							mListView.setAdapter(new MyListAdapter(mActivity, mVolleyImageLoader, tabGoodsList));
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.fororder");
			if (!TextUtils.isEmpty(tabName)) {
				req.addParams("tab_name", tabName);
			}
			return req;
		}
	}
	
	public class AddToShoppingCar implements JsonTaskHandler{

		private String product_id;
		
		public AddToShoppingCar(String product_id){
			this.product_id = product_id;
		}
		
		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity, "加入购物车成功");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.add");
			req.addParams("product_id", product_id);
			return req;
		}
	}

}
