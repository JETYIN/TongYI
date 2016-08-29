package com.qianseit.westore.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import cn.shopex.ecstore.R;

public class TicketAddFragment extends BaseDoFragment {
	
	private EditText mTicketNoTV;
	
	private ArrayList<JSONObject> mTickets = new ArrayList<JSONObject>();
	private TicketsAdapter mAdapter;

	private PullToRefreshListView mListView;
	private View mEmptyView;
	private View mRemoveView;
	private View mHow2GetTicket;

//	private int mPageNum = 0;
//	private int mTotalResult = 1;
//	private JsonTask mTask;
	
	private List<String> listCouponNum = new ArrayList<String>();
	private JSONArray mListCouponNum = null; 

	public TicketAddFragment() {
		super();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = mActivity.getIntent();
		String coupon = intent.getStringExtra(Run.EXTRA_COUPON_DATA);
		if (!TextUtils.isEmpty(coupon)) {
			try {
				mListCouponNum = new JSONArray(coupon);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_ticket_add_title);

		rootView = inflater.inflate(R.layout.fragment_ticket_add, null);
		findViewById(R.id.ticket_add_submit).setOnClickListener(this);
		mTicketNoTV = (EditText) findViewById(R.id.ticket_add_number);
		mListView = (PullToRefreshListView) findViewById(android.R.id.list);
		View emptyView = inflater.inflate(R.layout.item_ticket_empty_view, null);
		mHow2GetTicket = emptyView.findViewById(R.id.ticket_how_to);
		mHow2GetTicket.setOnClickListener(this);
		mListView.setEmptyView(emptyView);
		mRemoveView = findViewById(R.id.ticket_remove);
		mRemoveView.setOnClickListener(this);
		String selectedCoupon = getArguments().getString(Run.EXTRA_VALUE);
		if (!TextUtils.isEmpty(selectedCoupon)) {
			mRemoveView.setVisibility(View.VISIBLE);
		}
		mEmptyView = findViewById(android.R.id.empty);
		mListView.setEmptyView(mEmptyView);
		mListView.setPullToRefreshEnabled(false);

		mAdapter = new TicketsAdapter();
		mListView.getRefreshableView().setAdapter(mAdapter);
//		mListView.setOnRefreshListener(new OnRefreshListener() {
//			@Override
//			public void onRefresh() {
//				loadNextPage(0);
//			}
//
//			@Override
//			public void onRefreshMore() {
//			}
//		});
//		
//		loadNextPage(0);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.ticket_add_submit) {
			String ticketNo = mTicketNoTV.getText().toString();
			if (!TextUtils.isEmpty(ticketNo)) {
				Intent data = new Intent();
				data.putExtra(Run.EXTRA_DATA, ticketNo);
				mActivity.setResult(Activity.RESULT_OK, data);
				mActivity.finish();
			}
		} else if(v == mRemoveView){
			Intent data = new Intent();
			data.putExtra(Run.EXTRA_DATA, true);
			mActivity.setResult(Activity.RESULT_OK, data);
			mActivity.finish();
		} else if(v == mHow2GetTicket){
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", "如何获取优惠券")
					.putExtra("article_id", "83"));
		} else {
			super.onClick(v);
		}
	}
	
	private class TicketsAdapter extends BaseAdapter implements View.OnClickListener{
		private LayoutInflater inflater;

		public TicketsAdapter() {
			inflater = mActivity.getLayoutInflater();
		}

		@Override
		public int getCount() {
//			return mTickets.size();
			return mListCouponNum.length();
		}

		@Override
		public JSONObject getItem(int position) {
//			return mTickets.get(position);
			return mListCouponNum.optJSONObject(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				int layout = R.layout.fragment_ticket_item;
				convertView = inflater.inflate(layout, null);
				Drawable img_off =mActivity.getResources().getDrawable(R.drawable.counpon_state_green);
				img_off.setBounds(0, 0, img_off.getMinimumWidth(), img_off.getMinimumHeight());
				((TextView)convertView.findViewById(R.id.ticket_item_valid_period))
						.setCompoundDrawables(img_off, null, null, null);
			}
			TextView nameTv = (TextView) convertView.findViewById(R.id.ticket_item_name);
			TextView numTv = (TextView) convertView.findViewById(R.id.ticket_item_number);
			TextView statusTv = (TextView) convertView.findViewById(R.id.ticket_item_valid_period);
			TextView deadlineTv = (TextView) convertView.findViewById(R.id.ticket_item_state);
			//TextView explainTv = (TextView) convertView.findViewById(R.id.ticket_item_summary);
			//TextView moneyTv = (TextView) convertView.findViewById(R.id.ticket_item_money);
			//JSONObject currentObj = mTickets.get(position);
			JSONObject currentObj = getItem(position);
			String memc_code = currentObj.optString("memc_code");
			convertView.setTag(memc_code);
			numTv.setText(getString(R.string.ticket_number, memc_code));
			statusTv.setTextColor(getResources().getColor(R.color.westore_color));
//			if (listCouponNum.contains(memc_code)) {
//				convertView.setAlpha(1.0f);
//			} else {
//				convertView.setAlpha(0.4f);
//			}
			JSONObject couponsInfo = currentObj.optJSONObject("coupons_info");
//			String isValid = couponsInfo.optString("cpns_status");
//			if (isValid.equals("0")) {
//				statusTv.setText(getString(R.string.ticket_used));
//			}else{
//				statusTv.setText(getString(R.string.ticket_unused));
//			}
//			statusTv.setText(currentObj.optString("memc_status"));
			
			statusTv.setText("可使用");//传过来的都是可用的，但是无法判断是不是所有优惠券是符合当前订单的，无法判断是否高亮
			nameTv.setText(couponsInfo.optString("cpns_name"));
			JSONObject timeInfo = currentObj.optJSONObject("time");
			deadlineTv.setText(getString(R.string.ticket_valid_period, formatDate(timeInfo.optLong("to_time"))));
			
//			JSONObject all = getItem(position);
//			convertView.setTag(all);
			convertView.setOnClickListener(this);
//			if (all == null)
//				return convertView;

			return convertView;
		}

		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag != null) {
				//JSONObject all = (JSONObject) tag;
				String ticketNo = (String) tag;
				if (!TextUtils.isEmpty(ticketNo)) {
					Intent data = new Intent();
					data.putExtra(Run.EXTRA_DATA, ticketNo);
					mActivity.setResult(Activity.RESULT_OK, data);
					mActivity.finish();
				}
			}
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private String formatDate(long milliseconds){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds  * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}
	
	// 加载下一页
//	private void loadNextPage(int oldPageNum) {
//		this.mPageNum = oldPageNum + 1;
//		if (this.mPageNum == 1) {
//			mTickets.clear();
//			mAdapter.notifyDataSetChanged();
//			mListView.setRefreshing();
//			mTotalResult = 1;
//		}
//
//		if (mTotalResult > mTickets.size()) {
//			mTask = new JsonTask();
//			Run.excuteJsonTask(mTask, new GetTicketsTask());
//		}
//	}
	
//	private class GetTicketsTask implements JsonTaskHandler {
//		@Override
//		public JsonRequestBean task_request() {
//			JsonRequestBean bean = new JsonRequestBean(
//					"mobileapi.member.coupon").addParams("n_page",
//					String.valueOf(mPageNum));
//			return bean;
//		}
//
//		@Override
//		public void task_response(String json_str) {
//			mListView.onRefreshComplete();
//
//			try {
//				JSONObject all = new JSONObject(json_str);
//				if (Run.checkRequestJson(mActivity, all)) {
//					JSONArray child = all.optJSONArray("data");
//					if (child != null && child.length() > 0) {
//						for (int i = 0, c = child.length(); i < c; i++)
//							mTickets.add(child.getJSONObject(i));
//						mAdapter.notifyDataSetChanged();
//						mTotalResult = mTickets.size() + 1;
//					} else {
//						mTotalResult = mTickets.size();
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//			}
//		}
//	}
	
}
