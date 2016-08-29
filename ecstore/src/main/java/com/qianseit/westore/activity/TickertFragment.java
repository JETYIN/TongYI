package com.qianseit.westore.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import cn.shopex.ecstore.R;

public class TickertFragment extends BaseDoFragment {

	private ArrayList<JSONObject> mTickets = new ArrayList<JSONObject>();
	private TicketsAdapter mAdapter;

	private PullToRefreshListView mListView;
	private View mEmptyView;
	private View mHow2GetTicket;

	private int mPageNum = 0;
	private int mTotalResult = 1;
	private JsonTask mTask;

	public TickertFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.ticket);
		// mActionBar.setRightTitleButton(R.string.add, this);

		rootView = inflater.inflate(R.layout.fragment_ticket, null);
		mListView = (PullToRefreshListView) findViewById(android.R.id.list);
		mEmptyView = findViewById(android.R.id.empty);
		mHow2GetTicket = mEmptyView.findViewById(R.id.ticket_how_to);
		mHow2GetTicket.setOnClickListener(this);
		mListView.setEmptyView(mEmptyView);

		mAdapter = new TicketsAdapter();
		mListView.getRefreshableView().setAdapter(mAdapter);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0);
			}

			@Override
			public void onRefreshMore() {
			}
		});

		loadNextPage(0);
	}

	// 加载下一页
	private void loadNextPage(int oldPageNum) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mTickets.clear();
			mAdapter.notifyDataSetChanged();
			mListView.setRefreshing();
			mTotalResult = 1;
		}
		if (mTotalResult > mTickets.size()) {
			mTask = new JsonTask();
			Run.excuteJsonTask(mTask, new GetTicketsTask());
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mActionBar.getRightButton()) {
			mActivity.startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_TICKET_ADD));
		} else if(v == mHow2GetTicket){
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", "如何获取优惠券")
					.putExtra("article_id", "83"));
		} else {
			super.onClick(v);
		}
	}

	private class TicketsAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public TicketsAdapter() {
			inflater = mActivity.getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mTickets.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mTickets.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				int layout = R.layout.fragment_ticket_item;
				convertView = inflater.inflate(layout, null);
			}
			TextView nameTv = (TextView) convertView.findViewById(R.id.ticket_item_name);
			TextView numTv = (TextView) convertView.findViewById(R.id.ticket_item_number);
			TextView statusTv = (TextView) convertView.findViewById(R.id.ticket_item_valid_period);
			TextView deadlineTv = (TextView) convertView.findViewById(R.id.ticket_item_state);
			//TextView explainTv = (TextView) convertView.findViewById(R.id.ticket_item_summary);
			//TextView moneyTv = (TextView) convertView.findViewById(R.id.ticket_item_money);
			JSONObject currentObj = mTickets.get(position);
			numTv.setText(getString(R.string.ticket_number, currentObj.optString("memc_code")));
			JSONObject couponsInfo = currentObj.optJSONObject("coupons_info");
//			String isValid = couponsInfo.optString("memc_isvalid");
//			if (isValid.equals("0")) {
//				statusTv.setText(getString(R.string.ticket_used));
//			}else{
//				statusTv.setText(getString(R.string.ticket_unused));
//			}
			statusTv.setText(currentObj.optString("memc_status"));
			nameTv.setText(couponsInfo.optString("cpns_name"));
			JSONObject timeInfo = currentObj.optJSONObject("time");
			deadlineTv.setText(getString(R.string.ticket_valid_period, formatDate(timeInfo.optLong("to_time"))));
			
			JSONObject all = getItem(position);
			if (all == null)
				return convertView;

			return convertView;
		}
	}
	
	private String formatDate(long milliseconds){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds  * 1000L);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(c.getTime());
	}

	private class GetTicketsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.coupon").addParams("n_page",
					String.valueOf(mPageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			mListView.onRefreshComplete();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						for (int i = 0, c = child.length(); i < c; i++)
							mTickets.add(child.getJSONObject(i));
						mAdapter.notifyDataSetChanged();
						mTotalResult = mTickets.size() + 1;
					} else {
						mTotalResult = mTickets.size();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

}
