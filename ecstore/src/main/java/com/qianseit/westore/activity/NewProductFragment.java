package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.android.volley.toolbox.NetworkImageView;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.RushBuyCountDownTimerView;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;


public class NewProductFragment extends BaseDoFragment {

	private PullToRefreshListView mListView;
	private BaseAdapter mGoodsListAdapter;

	private LayoutInflater mLayoutInflater;
	private VolleyImageLoader mVolleyImageLoader;
	private int mPageNum;
	private int mTotalGoods = 20;
	private int mPageGoods = 20;
	private boolean isScrolling = false;
	private  float width;

	private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = Float.valueOf(dm.widthPixels);
		mLayoutInflater = this.getLayoutInflater();
		mActionBar.setShowTitleBar(false);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_new_product, null);
		findViewById(R.id.new_product_back).setOnClickListener(this);
		mListView = (PullToRefreshListView) findViewById(R.id.new_product_listview);

		mGoodsListAdapter = new GoodsListAdapter();
		mListView.getRefreshableView().setAdapter(mGoodsListAdapter);

		mListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						JSONObject json = (JSONObject) view
								.getTag(R.id.tag_object);
						String goodsIID = json.optString("iid");
						Intent intent = AgentActivity.intentForFragment(
								mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
								.putExtra(Run.EXTRA_CLASS_ID, goodsIID);
						startActivity(intent);

					}

				});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					isScrolling = false;
					mGoodsListAdapter.notifyDataSetChanged();
				} else {
					isScrolling = true;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5
						|| (mPageGoods * (mPageNum + 1)) >= mTotalGoods)
					return;
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum,false);
			}
		});
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0,false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		showCancelableLoadingDialog();
		loadNextPage(mPageNum,true);
	}

	private void loadNextPage(int oldPageNum,boolean isFirst) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mTotalGoods = 20;
			mGoodsArray.clear();
			mGoodsListAdapter.notifyDataSetChanged();
			if(!isFirst){
				mListView.setRefreshing();
			}
		}
		Run.excuteJsonTask(new JsonTask(), new GetGoodsTask(isFirst));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.new_product_back:
			getActivity().finish();
			break;

		default:
			break;
		}
		super.onClick(v);
	}

	@Override
	public void onResume() {
		super.onResume();

	}
	
	@Override
	public void onPause() {
		super.onPause();

	}

	private class GoodsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGoodsArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mGoodsArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mLayoutInflater
						.inflate(R.layout.goods_item, null);
				viewHolder.imageFragme=(FrameLayout)convertView.findViewById(R.id.fragment_goods_item_image);
				viewHolder.imageFragme.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int)(width/2)));
				viewHolder.iconImage = (NetworkImageView) convertView
						.findViewById(R.id.fragment_goods_item_icon);
				viewHolder.titleTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_title);
				viewHolder.priceTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_price);
				viewHolder.statusTextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_status);
				viewHolder.timeTextView = (RushBuyCountDownTimerView) convertView
						.findViewById(R.id.fragment_goods_item_time_buy);
				viewHolder.status2TextView = (TextView) convertView
						.findViewById(R.id.fragment_goods_item_status2);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			JSONObject goodsObject = getItem(position);
			convertView.setTag(R.id.tag_object, goodsObject);
			viewHolder.titleTextView.setText(goodsObject.optString("title"));
			viewHolder.priceTextView.setText("￥"
					+ goodsObject.optString("price"));
			mVolleyImageLoader.showImage(viewHolder.iconImage,
					goodsObject.optString("ipad_image_url"));
			String strStatue = goodsObject.optString("pmt_text").trim();
			if ("NEW".equals(strStatue)) {
				viewHolder.status2TextView.setVisibility(View.VISIBLE);
				viewHolder.statusTextView.setVisibility(View.GONE);
				viewHolder.status2TextView.setText(strStatue);
			} else {
				viewHolder.status2TextView.setVisibility(View.GONE);
				viewHolder.statusTextView.setVisibility(View.VISIBLE);
				viewHolder.statusTextView.setText(strStatue);
			}
			return convertView;
		}

	}

	private class ViewHolder {
		private FrameLayout imageFragme;
		private NetworkImageView iconImage;
		private RushBuyCountDownTimerView timeTextView;
		private TextView titleTextView;
		private TextView priceTextView;
		private TextView statusTextView;
		private TextView status2TextView;

	}

	class GetGoodsTask implements JsonTaskHandler {
		private boolean isFirst;
		
		public GetGoodsTask(boolean isFirst){
			this.isFirst=isFirst;
		}
		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				if(!isFirst){
					mListView.onRefreshComplete();
				}
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject child = all.optJSONObject("data");
					if (child != null) {
						JSONObject goodsJson = child.optJSONObject("0");
						JSONArray goodsArray = goodsJson.optJSONArray("goods");
						if (goodsArray != null && goodsArray.length() > 0) {
							loadLocalGoods(goodsArray);

						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.salesgoods.sales_list");
			req.addParams("page_no", String.valueOf(mPageNum));
			req.addParams("son_object", "json");
			req.addParams("rule_id", "7");
			return req;
		}
	}

	private void loadLocalGoods(JSONArray jsonArray) {

		for (int i = 0; i < jsonArray.length(); i++) {
			mGoodsArray.add(jsonArray.optJSONObject(i));
		}
		mGoodsListAdapter.notifyDataSetChanged();
	}

}
