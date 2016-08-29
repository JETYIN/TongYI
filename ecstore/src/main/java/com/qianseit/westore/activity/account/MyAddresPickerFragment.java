package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleTextWatcher;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class MyAddresPickerFragment extends BaseDoFragment implements
		OnItemClickListener {
	private ListView mListView;
	private EditText mKeywodsText;
	private int mClickNum = 0;
	private RegionAdapter mAdapter;

	private ArrayList<String> mValues = new ArrayList<String>();
	private ArrayList<JSONArray> mSources = new ArrayList<JSONArray>();

	public MyAddresPickerFragment() {
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.province);
		rootView = inflater.inflate(R.layout.fragment_myaddress_picker, null);

		mListView = (ListView) findViewById(android.R.id.list);
		mKeywodsText = (EditText) findViewById(android.R.id.text1);
		mKeywodsText.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mAdapter == null)
					return;

				String keywords = s.toString();
				ArrayList<String> showData = new ArrayList<String>();
				if (mAdapter.getSource() != null) {
					JSONArray array = mAdapter.getSource();
					// for (int i = 0, c = array.length(); i < c; i++) {
					// String srcStr = array.optString(i);
					// if (!TextUtils.isEmpty(srcStr)
					// && srcStr.contains(keywords))
					// showData.add(srcStr);
					// }
					int size = mValues.size();
					if (size == 3) {
						mKeywodsText.setHint(R.string.zone);
					} else {
						mKeywodsText.setHint(R.string.search);
					}
					if (size == 3 && s.toString().length() == 0) {

					} else {
						for (int i = 0, c = array.length(); i < c; i++) {
							String srcStr = array.optString(i);
							if (!TextUtils.isEmpty(srcStr)
									&& srcStr.contains(keywords))
								showData.add(srcStr);
						}
					}
					if (size == 3 && showData.isEmpty()
							&& s.toString().length() != 0) {
						for (int i = 0, c = array.length(); i < c; i++) {
							String srcStr = array.optString(i);
							if (srcStr.contains("其它"))
								showData.add(srcStr
										+ getString(R.string.select_addr_tips));
						}
					}
					mAdapter.resetShowingData(showData);
				}
			}
		});
		mListView.setOnItemClickListener(this);

		mActionBar.getBackButton().setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!goBackLastLavel()) {
							getActivity().finish();
						}
					}
				});

		// 获取地区列表
		Run.excuteJsonTask(new JsonTask(), new GetRegionsTask());
	}

	@Override
	public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
		RegionAdapter adapter = (RegionAdapter) mListView.getAdapter();
		String[] items = adapter.getItem(pos).split(":");
		mValues.add(items[0]);
		++mClickNum;
		if (mClickNum == 2) {
			String textArea = "";
			String area = "mainland:";
			for (int i = 0, c = mValues.size(); i < c; i++) {
				textArea += mValues.get(i);
//				area += mValues.get(i)
//						+ ((i == c - 1) ? ":" + items[1] : "/");
			}
			Intent data = new Intent();
			data.putExtra(Run.EXTRA_VALUE, textArea);
			//data.putExtra(Run.EXTRA_DATA, area);
			mActivity.setResult(Activity.RESULT_OK, data);
			mActivity.finish();
		} else {
			if (items.length == 2) {
				String textArea = "", area = "mainland:";
				for (int i = 0, c = mValues.size(); i < c; i++) {
					textArea += mValues.get(i);
					area += mValues.get(i)
							+ ((i == c - 1) ? ":" + items[1] : "/");
				}
				Intent data = new Intent();
				data.putExtra(Run.EXTRA_VALUE, textArea);
				data.putExtra(Run.EXTRA_DATA, area);
				data.putExtra(Run.EXTRA_ADDR, mKeywodsText.getText().toString());
				mActivity.setResult(Activity.RESULT_OK, data);
				mActivity.finish();
			} else if (items.length == 3) {
				try {
					mActionBar.setTitle(items[0]);
					int index = Integer.parseInt(items[2]);
					JSONArray source = mSources.get(mValues.size());
					source = source.getJSONArray(index);
					mAdapter = new RegionAdapter(source);
					mListView.setAdapter(mAdapter);
				} catch (Exception e) {
				}
			}
		}
		mKeywodsText.setText("");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mClickNum--;
			if (goBackLastLavel()) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean goBackLastLavel() {
		if (mValues.size() > 0) {
			mValues.remove(mValues.size() - 1);
			int size = mValues.size();
			if (size == 3) {
				mKeywodsText.setHint(R.string.zone);
			} else {
				mKeywodsText.setHint(R.string.search);
			}
			if (size > 0) {
				mActionBar.setTitle(mValues.get(size - 1));
			} else {
				mActionBar.setTitle(R.string.province);
			}
			JSONArray source = mSources.get(size);
			if (mValues.size() > 0) {
				try {
					source = source.getJSONArray(0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			mAdapter = new RegionAdapter(source);
			mListView.setAdapter(mAdapter);
			return true;
		}
		return false;
	}

	private class RegionAdapter extends BaseAdapter {
		private JSONArray array;
		private ArrayList<String> showingData = new ArrayList<String>();

		public RegionAdapter(JSONArray array) {
			this.array = array;
			for (int i = 0, c = array.length(); i < c; i++)
				if (!TextUtils.isEmpty(array.optString(i)))
					this.showingData.add(array.optString(i));
		}

		public void resetShowingData(ArrayList<String> data) {
			this.showingData = data;
			this.notifyDataSetChanged();
		}

		public JSONArray getSource() {
			return array;
		}

		@Override
		public int getCount() {
			return showingData.size();
		}

		@Override
		public String getItem(int position) {
			return showingData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.simple_list_item1, null);
				((TextView) convertView.findViewById(R.id.text1))
						.setTextSize(18);
			}

			String[] items = getItem(position).split(":");
			if (items[0].contains("其它")) {
				((TextView) convertView.findViewById(R.id.text1))
						.setText(items[0]
								+ getString(R.string.select_addr_tips));
			} else {
				((TextView) convertView.findViewById(R.id.text1))
						.setText(items[0]);
			}

			return convertView;
		}
	}

	private class GetRegionsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.get_regions");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						for (int i = 0, c = child.length(); i < c; i++)
							mSources.add(child.getJSONArray(i));
						JSONArray list = mSources.get(0);
						mAdapter = new RegionAdapter(list);
						mListView.setAdapter(mAdapter);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
