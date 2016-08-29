package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import cn.sharesdk.wechat.utils.WechatHandlerActivity;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;

public class HistoryTicketFragment extends BaseDoFragment {
	private final int WHATVISBLE=1;
	public final int WHATGONE=0;
	private ArrayList<JSONObject> mTickets = new ArrayList<JSONObject>();
	private BaseAdapter mHistoryTictAdapter;
	private LayoutInflater mLayoutInflater;
	private PullToRefreshListView mListView;
	private int mPageNum;
	private JsonTask mTask;
	private TextView mTicketNull;
	private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Point screenSize;
	
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case WHATGONE:
				mTicketNull.setVisibility(View.GONE);
				break;
			case WHATVISBLE:
				mTicketNull.setVisibility(View.VISIBLE);
			default:
				break;
			}
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.history_ticket);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		rootView = inflater
				.inflate(R.layout.fragment_history_ticket_main, null);
		mListView = (PullToRefreshListView) findViewById(android.R.id.list);
		mTicketNull = (TextView) findViewById(R.id.fragment_history_ticket_null);
		Run.removeFromSuperView(mTicketNull);
		mTicketNull.setLayoutParams(new AbsListView.LayoutParams(
				mTicketNull.getLayoutParams()));
		mListView.getRefreshableView().addFooterView(mTicketNull);
		mHistoryTictAdapter = new TicertAdapter();
		mListView.getRefreshableView().setAdapter(mHistoryTictAdapter);
		screenSize = Run.getScreenSize(mActivity.getWindowManager());

		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem > visibleItemCount) {
					rootView.findViewById(R.id.fragment_history_ticket_goto_top)
							.setVisibility(View.VISIBLE);
				} else {
					rootView.findViewById(R.id.fragment_history_ticket_goto_top)
							.setVisibility(View.GONE);
				}
				if (totalItemCount < 5)
					return;
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum);

			}
		});

		loadNextPage(0);
	}

	// 加载下一页
	private void loadNextPage(int oldPageNum) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mTickets.clear();
			mHistoryTictAdapter.notifyDataSetChanged();
			mListView.setRefreshing();

		}
		if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
			return;

		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetHistoryTicketsTask());
	}

	@Override
	public void onClick(View v) {

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

	private class TicertAdapter extends BaseAdapter {

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
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.tickert_item,
						null);
				viewHolder.radioImage = (ImageView) convertView
						.findViewById(R.id.ticket_item_radio);
				viewHolder.tickerTypeTextView = (TextView) convertView
						.findViewById(R.id.ticket_item_value_type);
				viewHolder.tickerValueTextView = (TextView) convertView
						.findViewById(R.id.ticket_item_value);
				viewHolder.tickerNametView = (TextView) convertView
						.findViewById(R.id.ticket_item_name);
				viewHolder.tickerExplainView = (TextView) convertView
						.findViewById(R.id.ticket_item_explain);
				viewHolder.tickerTimeView = (TextView) convertView
						.findViewById(R.id.ticket_item_time);
				viewHolder.tickerDiscountTextView = (TextView) convertView
						.findViewById(R.id.ticket_item_discount_value_type);
				viewHolder.tickerLinear = (LinearLayout) convertView
						.findViewById(R.id.ticket_item_linear);
	
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			JSONObject data = getItem(position);
			viewHolder.radioImage.setVisibility(View.INVISIBLE);
			viewHolder.tickerLinear
					.setBackgroundResource(R.drawable.bg_tickert_item_history);
			int height=screenSize.x*405/1025;
			viewHolder.tickerLinear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height-10));
			if (data != null) {
				JSONObject time = data.optJSONObject("time");
				if (time != null) {
					Long t = time.optLong("to_time") * 1000;
					Date date = new Date(t);
					viewHolder.tickerTimeView.setText("有效期至 "
							+ mFormat.format(date));
					Long newDate = System.currentTimeMillis() / 1000;
					if(newDate>t){
						viewHolder.tickerLinear
						.setBackgroundResource(R.drawable.bg_tickert_item_history);	
					}else{
						if(data.optInt("memc_used_times")>0){
							viewHolder.tickerLinear
							.setBackgroundResource(R.drawable.bg_tickert_item_use_history);	
						}
					}
				}
				JSONObject ruleInfo = data.optJSONObject("rule_info");
				if (ruleInfo != null) {
					viewHolder.tickerNametView.setText(ruleInfo 
							.optString("name"));
					viewHolder.tickerExplainView.setText(ruleInfo
							.optString("description"));
					String tiketType = ruleInfo.optString("discount_type");
					// 抵扣金额
					if ("byfixed".equals(tiketType)) {
						viewHolder.tickerTypeTextView
								.setVisibility(View.VISIBLE);
						viewHolder.tickerDiscountTextView
								.setVisibility(View.GONE);
						viewHolder.tickerValueTextView.setText(ruleInfo
								.optString("discount_value"));
					} else if ("topercent".equals(tiketType)) { // 打折
						viewHolder.tickerDiscountTextView
								.setVisibility(View.VISIBLE);
						viewHolder.tickerTypeTextView.setVisibility(View.GONE);
						viewHolder.tickerValueTextView
								.setText(String.valueOf((ruleInfo
										.optDouble("discount_value") / 10)));
					}
				}

			}
			return convertView;
		}
	}

	private class ViewHolder {
		private ImageView radioImage;
		private TextView tickerTypeTextView;
		private TextView tickerValueTextView;
		private TextView tickerNametView;
		private TextView tickerExplainView;
		private TextView tickerTimeView;
		private TextView tickerDiscountTextView;
		private LinearLayout tickerLinear;

	}

	private class GetHistoryTicketsTask implements JsonTaskHandler {
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
						for (int i = 0, c = child.length(); i < c; i++) {
							JSONObject data = child.getJSONObject(i);
							JSONObject time = data.optJSONObject("time");
							if (time != null) {
								Long t = time.optLong("to_time");
								Long newDate = System.currentTimeMillis() / 1000;
								if (!("0".equals(data
										.optString("memc_used_times")) && newDate < t)) {
									mTickets.add(data);
								}
							}
						}						
						mHistoryTictAdapter.notifyDataSetChanged();
					}
					if(mTickets.size()<=0){
						handler.sendEmptyMessage(WHATVISBLE);
					}else{
						handler.sendEmptyMessage(WHATGONE);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

}
