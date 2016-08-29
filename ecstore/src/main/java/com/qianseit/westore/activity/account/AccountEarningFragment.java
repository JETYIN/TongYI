package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import cn.shopex.ecstore.R;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class AccountEarningFragment extends BaseDoFragment {
	private final int[] title_res = { R.string.account_earning_order_no,
			R.string.account_earning_money, R.string.account_earning_source,
			R.string.account_earning_date };
	private final int[] item_ids = { R.id.account_earning_text1,
			R.id.account_earning_text2, R.id.account_earning_text3,
			R.id.account_earning_text4 };
	private final String[] item_keys = { "order_id", "money",
			"special_name", "format_mtime" };

	private ListView mListView;

	private int pageNum = 0;

	private ArrayList<JSONObject> mDataList = new ArrayList<JSONObject>();

	public AccountEarningFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_earning_title);
	}

	@Override
	public void init(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_account_earnig, null);
		findViewById(R.id.account_earning_itemview).setBackgroundColor(
				mActivity.getResources().getColor(R.color.westore_pink));
		int textColor = mActivity.getResources().getColor(R.color.white);
		for (int i = 0, c = item_ids.length; i < c; i++) {
			((TextView) findViewById(item_ids[i])).setText(title_res[i]);
			((TextView) findViewById(item_ids[i])).setTextColor(textColor);
		}

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(new EarningAdapter());
		mListView.setEmptyView(findViewById(android.R.id.message));
		loadNextPageData(pageNum);
	}

	// 加载下一页
	private void loadNextPageData(int pageNum) {
		this.pageNum = pageNum + 1;
		if (this.pageNum == 1) {
			mDataList.clear();
			((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
		}

		Run.excuteJsonTask(new JsonTask(), new LoadEarningTask());
	}

	private class EarningAdapter extends BaseAdapter {
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
						R.layout.fragment_account_earnig_item, null);

			if (getItem(position) == null)
				return convertView;

			JSONObject data = getItem(position);
			for (int i = 0, c = item_ids.length; i < c; i++) {
				TextView tv = (TextView) convertView.findViewById(item_ids[i]);
				tv.setText(data.optString(item_keys[i])
						+ (TextUtils.equals("money", item_keys[i]) ? "元" : ""));
			}
			return convertView;
		}
	}

	private class LoadEarningTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean(
					"mobileapi.member.promotion_into_logs").addParams(
					"page_no", String.valueOf(pageNum));
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					((TextView) findViewById(R.id.account_earning_total_money))
							.setText(data.optString("total_money"));
					JSONArray items = data.optJSONArray("items");
					int count = (items != null) ? items.length() : 0;
					for (int i = 0; i < count; i++)
						mDataList.add(items.optJSONObject(i));
				}
			} catch (Exception e) {
			} finally {
				((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
			}
		}

	}

}
