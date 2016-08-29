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

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleTextWatcher;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.Util;
import cn.shopex.ecstore.R;

public class MyAddressPickerStreetFragment extends BaseDoFragment implements
		OnItemClickListener {

	private ListView mListView;
	private EditText mKeywodsText;
	private RegionAdapter mAdapter;
	private JSONArray mStreetList;
	private TextView mTextView;
	private String mSelectedStreet;
	private ArrayList<JSONArray> mSources = new ArrayList<JSONArray>();

	public MyAddressPickerStreetFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = mActivity.getIntent();
		String streetStr = intent.getStringExtra(Run.EXTRA_ADDR);
		mActionBar.setTitle("街道、区域");
		mActionBar.getRightButton().setText(R.string.ok);
		mActionBar.getRightButton().setOnClickListener(this);
		//mActionBar.getBackButton().setOnClickListener(this);
		if (!TextUtils.isEmpty(streetStr)) {
			try {
				mStreetList = new JSONArray(streetStr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			mStreetList=new JSONArray();
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_myaddress_picker, null);
		mListView = (ListView) findViewById(android.R.id.list);
		mAdapter = new RegionAdapter(mStreetList);
		mAdapter.resetShowingData(new ArrayList<String>());
		mListView.setAdapter(mAdapter);
		mKeywodsText = (EditText) findViewById(android.R.id.text1);
		mTextView = (TextView) findViewById(android.R.id.text2);
		mTextView.setOnClickListener(this);
		mKeywodsText.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mAdapter == null || mTextView.getVisibility() == View.VISIBLE)
					return;
				String keywords = s.toString();
				if (TextUtils.isEmpty(keywords)) {
					ArrayList<String> showData = new ArrayList<String>();
					mAdapter.resetShowingData(showData);
				} else {
					ArrayList<String> showData = new ArrayList<String>();
					if (mAdapter.getSource() != null) {
						JSONArray array = mAdapter.getSource();
						for (int i = 0, c = array.length(); i < c; i++) {
							String srcStr = array.optString(i);
							if (!TextUtils.isEmpty(srcStr)
									&& srcStr.contains(keywords))
								showData.add(srcStr);
						}
						if (showData.isEmpty() && s.toString().length() != 0) {
							for (int i = 0, c = array.length(); i < c; i++) {
								String srcStr = array.optString(i);
								if (srcStr.contains("其它"))
									showData.add(srcStr);
							}
						}
						mAdapter.resetShowingData(showData);
					}
				}
			}
		});
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RegionAdapter adapter = (RegionAdapter) mListView.getAdapter();
		mActionBar.setShowRightButton(true);
		mTextView.setVisibility(View.VISIBLE);
		mSelectedStreet = adapter.getItem(position);
		mTextView.setText(mSelectedStreet.split(":")[0]);
		ArrayList<String> showData = new ArrayList<String>();
		mAdapter.resetShowingData(showData);
		mKeywodsText.setText("");
		mKeywodsText.setHint("详细地址");
//		Intent data = new Intent();
//		data.putExtra(Run.EXTRA_DATA, adapter.getItem(position));
//		mActivity.setResult(Activity.RESULT_OK, data);
//		mActivity.finish();
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == android.R.id.text2) {
			resetListView();
		} else if(v == mActionBar.getRightButton()){
			if (!TextUtils.isEmpty(mSelectedStreet) &&
					!TextUtils.isEmpty(mKeywodsText.getText().toString().trim())) {
				Intent data = new Intent();
				data.putExtra(Run.EXTRA_DATA, mSelectedStreet+"-"+mKeywodsText.getText().toString());
				mActivity.setResult(Activity.RESULT_OK, data);
				mActivity.finish();
			} else {
				Util.alert(mActivity, "请输入详细地址");
			}
		} else if(v == mActionBar.getBackButton()){
			if (mTextView.getVisibility() == View.VISIBLE) {
				resetListView();
			} else {
				getActivity().finish();
			}
		}else{
			super.onClick(v);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (mTextView.getVisibility() == View.VISIBLE) {
//			resetListView();
//			return true;
//		} 
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 重置 editText 和 listview
	 * @author chesonqin
	 * 2014-12-8
	 */
	private void resetListView(){
		mTextView.setVisibility(View.GONE);
		mKeywodsText.setHint(R.string.zone);
		mKeywodsText.setText(mSelectedStreet.split(":")[0]);
		mKeywodsText.setSelection(mSelectedStreet.split(":")[0].length());
		mActionBar.setShowRightButton(false);
		mTextView.setText("");
		mSelectedStreet = "";
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
		
		public void reloadData(){
			for (int i = 0, c = array.length(); i < c; i++){
				if (!TextUtils.isEmpty(array.optString(i))){
					this.showingData.add(array.optString(i));
				}
			}
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
				((TextView) convertView.findViewById(android.R.id.text1))
						.setTextSize(18);
			}

			String[] items = getItem(position).split(":");
			if (items[0].contains("其它")) {
				((TextView) convertView.findViewById(android.R.id.text1))
						.setText(items[0]
								+ getString(R.string.select_addr_tips));
			} else {
				((TextView) convertView.findViewById(android.R.id.text1))
						.setText(items[0]);
			}

			return convertView;
		}
	}
	
	private class GetRegionsTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						for (int i = 0, c = child.length(); i < c; i++) {
							mSources.add(child.optJSONArray(i));
						}
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				hideLoadingDialog_mt();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.get_regions");
		}

	}
}
