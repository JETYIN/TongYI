package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;

public class AccountTickertFragment extends BaseDoFragment {
	private ArrayList<JSONObject> mTickets = new ArrayList<JSONObject>();
	private BaseAdapter mTicetsAdapter;
	private LayoutInflater mLayoutInflater;
	private PullToRefreshListView mListView;
	private int mSelectPosit = -1;
	private int mPageNum;
	private JsonTask mTask;
	private boolean isGoodsBuy = false; // false:不是购买界面进入
	private EditText mTicketAddNum;
	private String oldCoupun;
	private String isFastBuy;
	private boolean isFirst = true;
	private String removeConpun;

	private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_ticket_add_title);
		Intent intent = mActivity.getIntent();
		isGoodsBuy = intent.getBooleanExtra(Run.EXTRA_DATA, false);
		String s = intent.getStringExtra(Run.EXTRA_COUPON_DATA);
		if (s != null && s.contains("&")) {
			oldCoupun = s.split("&")[0];
			if (oldCoupun.endsWith("null")) {
				oldCoupun = "";
			}
			isFastBuy = s.split("&")[1];
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_ticket_main, null);
		mListView = (PullToRefreshListView) findViewById(android.R.id.list);
		mTicetsAdapter = new TicertAdapter();
		findViewById(R.id.ticket_history).setOnClickListener(this);
		mTicketAddNum = (EditText) findViewById(R.id.ticket_add_num);
		findViewById(R.id.ticket_add_submit).setOnClickListener(this);
		View topView = findViewById(R.id.ticket_add_top);
		View historyView = findViewById(R.id.ticket_history);

		Run.removeFromSuperView(topView);
		Run.removeFromSuperView(historyView);
		topView.setLayoutParams(new AbsListView.LayoutParams(topView
				.getLayoutParams()));
		historyView.setLayoutParams(new AbsListView.LayoutParams(historyView
				.getLayoutParams()));
		mListView.getRefreshableView().addHeaderView(topView);
		mListView.getRefreshableView().addFooterView(historyView);
		mListView.getRefreshableView().setAdapter(mTicetsAdapter);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0, false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// isScrolling = false;
					mTicetsAdapter.notifyDataSetChanged();
				} else {
					// isScrolling = true;
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem > visibleItemCount) {
					rootView.findViewById(R.id.fragment_ticket_goto_top)
							.setVisibility(View.VISIBLE);
				} else {
					rootView.findViewById(R.id.fragment_ticket_goto_top)
							.setVisibility(View.GONE);
				}
				if (totalItemCount < 5)
					return;
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum, false);

			}
		});
		mActionBar.getBackButton().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(removeConpun)) {
					Intent data = new Intent();
					data.putExtra(Run.EXTRA_DATA, removeConpun);
					data.putExtra(Run.EXTRA_VALUE, true);
					mActivity.setResult(Activity.RESULT_OK, data);
				}
				mActivity.finish();
			}
		});
		loadNextPage(0, true);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!TextUtils.isEmpty(removeConpun)) {
				Intent data = new Intent();
				data.putExtra(Run.EXTRA_DATA, removeConpun);
				data.putExtra(Run.EXTRA_VALUE, true);
				mActivity.setResult(Activity.RESULT_OK, data);
			}
			mActivity.finish();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 加载下一页
	private void loadNextPage(int oldPageNum, boolean isShow) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mTickets.clear();
			mTicetsAdapter.notifyDataSetChanged();
			if (!isShow) {
				mListView.setRefreshing();
			}
			mSelectPosit = -1;

		}
		if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
			return;
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetTicketsTask(isShow));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.ticket_history:
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_HISTORY_TICKET));
			break;
		case R.id.ticket_add_submit:
			String strNum = mTicketAddNum.getText().toString().toString();
			if (!TextUtils.isEmpty(strNum)) {
				JsonTask mGetTask = new JsonTask();
				Run.excuteJsonTask(mGetTask, new ConvertTicketsTask());
			}
			break;

		default:
			break;
		}
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
			final int selectPostition = position;
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
				viewHolder.tickerDiscountTextView = (TextView) convertView
						.findViewById(R.id.ticket_item_discount_value_type);
				viewHolder.tickerNametView = (TextView) convertView
						.findViewById(R.id.ticket_item_name);
				viewHolder.tickerExplainView = (TextView) convertView
						.findViewById(R.id.ticket_item_explain);
				viewHolder.tickerTimeView = (TextView) convertView
						.findViewById(R.id.ticket_item_time);
				if (isGoodsBuy) {
					viewHolder.radioImage.setVisibility(View.VISIBLE);
				} else {
					viewHolder.radioImage.setVisibility(View.INVISIBLE);
				}
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			// if (isScrolling){
			// return convertView;
			// }
			JSONObject data = getItem(position);
			if (data.optString("memc_code").equals(oldCoupun) && isFirst) {
				mSelectPosit = position;
				isFirst = false;
			}
			if (position == mSelectPosit) {
				viewHolder.radioImage
						.setImageResource(R.drawable.my_address_book_default);
			} else {
				viewHolder.radioImage
						.setImageResource(R.drawable.my_address_book_not_default);
			}
			viewHolder.radioImage.setTag(data.optString("memc_code"));

			viewHolder.radioImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mSelectPosit == selectPostition) {
						Run.excuteJsonTask(new JsonTask(),
								new CancelCounponTask((String) v.getTag()));
					} else {
						mSelectPosit = selectPostition;
						mTicetsAdapter.notifyDataSetChanged();
						Run.excuteJsonTask(new JsonTask(), new AddCounponTask(
								(String) v.getTag()));
					}
				}
			});
			if (data != null) {
				JSONObject time = data.optJSONObject("time");
				if (time != null) {
					Long t = time.optLong("to_time") * 1000;
					Long fromeTime = time.optLong("from_time") * 1000;
					Date date = new Date(t);
					Date fromData = new Date(fromeTime);
					viewHolder.tickerTimeView.setText("有效期:"
							+ mFormat.format(fromData) + "至"
							+ mFormat.format(date));
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
		private TextView tickerDiscountTextView;
		private TextView tickerNametView;
		private TextView tickerExplainView;
		private TextView tickerTimeView;
	}

	private class GetTicketsTask implements JsonTaskHandler {
		private boolean isShow;

		public GetTicketsTask(boolean isShow) {
			this.isShow = isShow;
		}

		public JsonRequestBean task_request() {
			if (isShow)
				showCancelableLoadingDialog();

			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.coupon").addParams("n_page",
					String.valueOf(mPageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
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
								if ("0".equals(data
										.optString("memc_used_times"))
										&& newDate < t) {
									mTickets.add(data);
								}
							}
						}
						mTicetsAdapter.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

	private class ConvertTicketsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.coupon_code").addParams("coupon",
					mTicketAddNum.getText().toString().trim());
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			mListView.onRefreshComplete();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray data = all.optJSONArray("data");
					if (data != null & data.length() > 0) {
						JSONObject tick = data.optJSONObject(0);
						if (tick != null) {
							if ("1".equals(tick.optString("can_use")))
								Run.alert(mActivity, "该优惠券可用");
							else
								Run.alert(mActivity, "该优惠券不可用");
						}
					}
					loadNextPage(0, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

	/**
	 * 添加使用优惠券
	 * 
	 * @author chanson
	 * @CreatTime 2015-8-15 下午10:07:19
	 * 
	 */
	private class AddCounponTask implements JsonTaskHandler {
		private String counpon;

		public AddCounponTask(String counpon) {
			this.counpon = counpon;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.cart.add_coupon");
			if (!TextUtils.isEmpty(oldCoupun)) {
				req.addParams("old_coupon", oldCoupun);
			}
			return req.addParams("coupon", this.counpon).addParams("isfastbuy",
					isFastBuy);
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					removeConpun = "";
					Intent data = new Intent();
					data.putExtra(Run.EXTRA_DATA, all.optJSONObject("data")
							.toString());
					mActivity.setResult(Activity.RESULT_OK, data);
					mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 隐藏等待框
			hideLoadingDialog_mt();
		}
	}

	/**
	 * 取消使用优惠券
	 * 
	 * @author chanson
	 * @CreatTime 2015-8-15 下午10:12:03
	 * 
	 */
	private class CancelCounponTask implements JsonTaskHandler {

		private String conpun;

		public CancelCounponTask(String conpon) {
			this.conpun = conpon;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all, false)) {
					removeConpun = all.optJSONObject("data").toString();
					mSelectPosit = -1;
					mTicetsAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.cart.add_coupon");
			req.addParams("coupon", "null");
			req.addParams("old_coupon", conpun);
			return req.addParams("isfastbuy", isFastBuy);
		}

	}
}
