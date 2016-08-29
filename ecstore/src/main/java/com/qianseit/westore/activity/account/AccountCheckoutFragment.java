package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import cn.shopex.ecstore.R;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class AccountCheckoutFragment extends BaseDoFragment {
	private final int[] item_ids = { R.id.chekout_itemview_text1,
			R.id.chekout_itemview_text2, R.id.chekout_itemview_text3 };
	private final String[] item_keys = { "create_time", "amount",
			"remark" };

	private ListView mListView;
	private EditText mInputMoneyText;
	private EditText mAlipayUserText;

	private int pageNum = 0;

	private ArrayList<JSONObject> mDataList = new ArrayList<JSONObject>();

	public AccountCheckoutFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.checkout_money_title);
	}

	@Override
	public void init(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_checkout, null);
		findViewById(R.id.checkout_submit).setOnClickListener(this);
		mInputMoneyText = (EditText) findViewById(R.id.checkout_input_money);
		mAlipayUserText = (EditText) findViewById(R.id.checkout_alipay_user);
JSONObject AA;

		findViewById(R.id.chekout_itemview).setBackgroundColor(
				mActivity.getResources().getColor(R.color.westore_pink));
		int textColor = mActivity.getResources().getColor(R.color.white);
		for (int i = 0, c = item_ids.length; i < c; i++)
			((TextView) findViewById(item_ids[i])).setTextColor(textColor);

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setEmptyView(findViewById(android.R.id.message));
		mListView.setAdapter(new HistoryAdapter());
		loadNextPageData(pageNum);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.checkout_submit) {
			String moneyText = mInputMoneyText.getText().toString();
			if (TextUtils.isEmpty(moneyText)
					|| TextUtils.isEmpty(mAlipayUserText.getText().toString()))
				return;
			// 检测输入提现金额合法性
			int money = Integer.parseInt(moneyText);
			if (money > 0 && money % 100 == 0)
				Run.excuteJsonTask(new JsonTask(), new CheckoutTask(money));
		} else {
			super.onClick(v);
		}
	}

	// 加载下一页
	private void loadNextPageData(int pageNum) {
		this.pageNum = pageNum + 1;
		if (this.pageNum == 1) {
			mDataList.clear();
			((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
		}

		Run.excuteJsonTask(new JsonTask(), new LoadCheckoutHistoryTask(pageNum));
	}

	private class HistoryAdapter extends BaseAdapter {
		private SimpleDateFormat mDateFormatter;

		public HistoryAdapter() {
			mDateFormatter = new SimpleDateFormat("yy-MM-dd");
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_checkout_item, null);

			if (getItem(position) == null)
				return convertView;

			JSONObject data = getItem(position);
			TextView tv = (TextView) convertView.findViewById(item_ids[0]);
			long time = data.optLong(item_keys[0]);
			tv.setText(mDateFormatter.format(time * 1000));
			tv = (TextView) convertView.findViewById(item_ids[1]);
			tv.setText(data.optString(item_keys[1])
					+ mActivity.getString(R.string.unit_money));
			tv = (TextView) convertView.findViewById(item_ids[2]);
			tv.setText(data.optString(item_keys[2]));
			return convertView;
		}
	}

	private class CheckoutTask implements JsonTaskHandler {
		private int money;

		public CheckoutTask(int money) {
			this.money = money;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.submit_withdrawal").addParams("money",
					String.valueOf(money)).addParams("alipay_user",
					mAlipayUserText.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alertL(mActivity, R.string.checkout_money_success);
					mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onCheckoutHistoryLoaded(String json_str) {
		hideLoadingDialog_mt();
		try {
			JSONObject all = new JSONObject(json_str);
			if (Run.checkRequestJson(mActivity, all)) {
				JSONObject data = all.optJSONObject("data");
				((TextView) findViewById(R.id.checkout_available_money))
						.setText(data.optString("total")
								+ mActivity.getString(R.string.unit_money));
				((TextView) findViewById(R.id.checkout_money_total))
						.setText(data.optString("total_withdrawal")
								+ mActivity.getString(R.string.unit_money));
				// 提现记录
				JSONArray items = data.optJSONArray("witlogs");
				int count = (items != null) ? items.length() : 0;
				if (count > 0) {
					JSONObject item = items.optJSONObject(0);
					String alipay = item.optString("alipay_user");
					if (!TextUtils.isEmpty(alipay)) {
						mAlipayUserText.setEnabled(false);
						mAlipayUserText.setText(alipay);
					} else {
						mAlipayUserText.setEnabled(true);
					}
				} else {
					mAlipayUserText.setEnabled(true);
				}
				for (int i = 0; i < count; i++)
					mDataList.add(items.optJSONObject(i));
			}
		} catch (Exception e) {
		} finally {
			((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
		}
	}

}
