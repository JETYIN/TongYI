package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import cn.shopex.ecstore.R;

public class MyAddressEditor extends BaseDoFragment {

	private final int REQUEST_CODE = 0x1001;

	private EditText mNameEdt;
	private EditText mTelPhoneEdt;
	private EditText mAddrDetailEdt;
	private EditText mPostNumEdt;
	private TextView mAddrTV;
	private TextView mAddrProvince;
	private TextView mAddrCity;
	private TextView mAddrTown;
	private TextView mAddrArea;
	private TextView mAddrDetailTv;

	private String mSelectedProvince;
	private String mSelectedCity;
	private String mSelectedTown;
	private String mSelectedArea;
	private MyAdapter mAdapter;

	private String area = "";
	private JSONObject mAddressInfo;

	private ArrayList<JSONArray> mSources = new ArrayList<JSONArray>();

	private JSONArray mProvincesList = new JSONArray();
	private JSONArray mCitiesList = new JSONArray();
	private JSONArray mAreasList = new JSONArray();
	private JSONArray mTownList = new JSONArray();

	public MyAddressEditor() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.my_address_book_editor);
		try {
			Intent data = mActivity.getIntent();
			mAddressInfo = new JSONObject(data.getStringExtra(Run.EXTRA_DATA));
		} catch (Exception e) {

		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setRightTitleButton(android.R.string.ok, this);
		rootView = inflater.inflate(R.layout.fragment_add_reciever_address,
				null);
		mNameEdt = (EditText) rootView
				.findViewById(R.id.fragment_add_reciver_address_name);
		mTelPhoneEdt = (EditText) rootView
				.findViewById(R.id.fragment_add_reciver_address_tel);
		mAddrDetailEdt = (EditText) rootView
				.findViewById(R.id.fragment_add_reciver_address_detail);
		mPostNumEdt = (EditText) rootView
				.findViewById(R.id.fragment_add_reciver_address_postnum);
		mAddrProvince = (TextView) rootView
				.findViewById(R.id.fragment_add_reciver_address_province);
		mAddrCity = (TextView) rootView
				.findViewById(R.id.fragment_add_reciver_address_city);
		mAddrTown = (TextView) rootView
				.findViewById(R.id.fragment_add_reciver_address_town);
		mAddrArea = (TextView) rootView
				.findViewById(R.id.fragment_add_reciver_address_area);
		mAddrTV = (TextView) rootView
				.findViewById(R.id.fragment_add_reciver_address_addr);
		mAddrDetailTv = (TextView) rootView.findViewById(R.id.fragment_add_reciver_address_detail_tv);
		mAddrProvince.setOnClickListener(this);
		mAddrCity.setOnClickListener(this);
		mAddrTown.setOnClickListener(this);
		mAddrArea.setOnClickListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mSources.size() < 1) {
			new JsonTask().execute(new GetRegionsTask());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
			String result = data.getStringExtra(Run.EXTRA_DATA);
			mSelectedArea = result.split("-")[0];
			mAddrDetailEdt.setText(result.split("-")[1]);
			mAddrDetailTv.setText(result.split("-")[1]);
			parserAddress(mAddrArea);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mActionBar.getRightButton()) {
			if (TextUtils.isEmpty(mNameEdt.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_username);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else if (TextUtils.isEmpty(mTelPhoneEdt.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_phone);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else if (TextUtils.isEmpty(mAddrTV.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_district);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else if (TextUtils.isEmpty(mAddrDetailEdt.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_address);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else {
				Run.excuteJsonTask(new JsonTask(), new SaveAddressTask());
			}
		} else if (v == mAddrProvince) {
			if (mProvincesList.length() > 0) {
				showDialog(Type.PROVINCE, mProvincesList);
			}
		} else if (v == mAddrCity) {
			if (mCitiesList.length() > 0) {
				showDialog(Type.CITY, mCitiesList);
			} else {
				Run.alert(mActivity, "请先选择省份");
			}
		} else if (v == mAddrTown) {
			if (mTownList.length() > 0) {
				showDialog(Type.TOWN, mTownList);
			} else {
				Run.alert(mActivity, "请先选择市");
			}
		} else if (v == mAddrArea) {
			if (mAreasList.length() > 0) {
				// showDialog(Type.AREA, mAreasList);
				startActivityForResult(
						AgentActivity.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_PICKER_STREET).putExtra(
								Run.EXTRA_ADDR, mAreasList.toString()),
						REQUEST_CODE);
			} else {
				Run.alert(mActivity, "请先选择县/区");
			}
		} else {
			super.onClick(v);
		}
	}

	/**
	 * 修改收货地址，重新匹配地址
	 */
	private void setEditData() {
		if (mAddressInfo != null) {
			mNameEdt.setText(mAddressInfo.optString("name"));
			mTelPhoneEdt.setText(mAddressInfo.optString("mobile"));
			mAddrTV.setText(mAddressInfo.optString("txt_area"));
			mAddrDetailEdt.setText(mAddressInfo.optString("addr"));
			mAddrDetailTv.setText(mAddressInfo.optString("addr"));
			String tempArea = mAddressInfo.optString("area");
			if (!TextUtils.isEmpty(mAddressInfo.optString("zip"))) {
				mPostNumEdt.setText(mAddressInfo.optString("zip"));
			}
			tempArea = tempArea.replaceAll("mainland:", "");
			String[] item = tempArea.split("/");
			for (int i = 0; i < item.length; i++) {
				if (i == 0) {
					for (int j = 0; j < mSources.get(i).length(); j++) {
						if (item[i].equals(mSources.get(i).optString(j)
								.split(":")[0])) {
							mSelectedProvince = mSources.get(i).optString(j);
							mProvincesList = mSources.get(i);
							mAddrProvince.setText(item[i]);
							break;
						}
					}
				} else if (i == 1) {
					JSONArray list = mSources.get(i).optJSONArray(
							Integer.parseInt(mSelectedProvince.split(":")[2]));
					mCitiesList = list;
					for (int j = 0; j < list.length(); j++) {
						if (list.optString(j).split(":")[0].equals(item[i])) {
							mSelectedCity = list.optString(j);
							mAddrCity.setText(item[i]);
							break;
						}
					}
				} else if (i == 2) {
					JSONArray list = mSources.get(i).optJSONArray(
							Integer.parseInt(mSelectedCity.split(":")[2]));
					mTownList = list;
					for (int j = 0; j < list.length(); j++) {
						if (i == item.length - 1) {
							if (list.optString(j).split(":")[0].equals(item[i].split(":")[0])) {
								mSelectedTown = list.optString(j);
								mAddrTown.setText(mSelectedTown.split(":")[0]);
								mAddrTown.setVisibility(View.VISIBLE);
								mAddrArea.setVisibility(View.GONE);
								break;
							}
						} else {
							if (list.optString(j).split(":")[0].equals(item[i])) {
								mSelectedTown = list.optString(j);
								mAddrTown.setText(item[i]);
								mAddrArea.setVisibility(View.VISIBLE);
								break;
							}
						}
					}
				} else if (i == 3) {
					JSONArray list = mSources.get(i).optJSONArray(
							Integer.parseInt(mSelectedTown.split(":")[2]));
					mAreasList = list;
					for (int j = 0; j < list.length(); j++) {
						if (list.optString(j).equals(item[i])) {
							mSelectedArea = list.optString(j);
							mAddrArea.setText(mSelectedArea.split(":")[0]);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * 得到下一级的地址
	 * 
	 * @param v
	 */
	private void parserAddress(View v) {
		String[] itemp = null;
		if (v == mAddrProvince) {
			mAddrCity.setText(Type.CITY.toString());
			mAddrTown.setText(Type.TOWN.toString());
			mAddrArea.setText(Type.AREA.toString());
			mCitiesList = new JSONArray();
			mTownList = new JSONArray();
			mAreasList = new JSONArray();
			itemp = mSelectedProvince.split(":");
			mAddrProvince.setText(itemp[0]);
			mAddrTown.setVisibility(View.GONE);
			if (itemp.length == 3) {
				mCitiesList = mSources.get(1).optJSONArray(
						Integer.parseInt(itemp[2]));
				if (mCitiesList .length() == 1) {
					mSelectedCity = mCitiesList.optString(0);
					parserAddress(mAddrCity);
				} else {
					mAddrCity.setText("");
				}
			} else if (itemp.length == 2) {
				mAddrCity.setVisibility(View.GONE);
			}
		} else if (v == mAddrCity) {
			mAddrTown.setText(Type.TOWN.toString());
			mAddrArea.setText(Type.AREA.toString());
			mTownList = new JSONArray();
			mAreasList = new JSONArray();
			itemp = mSelectedCity.split(":");
			mAddrCity.setText(itemp[0]);
			if (itemp.length == 3) {
				mAddrTown.setVisibility(View.VISIBLE);
				mTownList = mSources.get(2).optJSONArray(
						Integer.parseInt(itemp[2]));
				if (mTownList.length() == 1) {
					parserAddress(mAddrTown);
				} else {
					mAddrTV.setText("");
				}
			} else if (itemp.length == 2) {
				mAddrTown.setVisibility(View.GONE);
				String address = mAddrProvince.getText().toString() + itemp[0];
				mAddrTV.setText(address);
			}
		} else if (v == mAddrTown) {
			mAddrArea.setText(Type.AREA.toString());
			mAreasList = new JSONArray();
			itemp = mSelectedTown.split(":");
			mAddrTown.setText(itemp[0]);
			if (itemp.length == 3) {
				mAddrArea.setVisibility(View.VISIBLE);
				mAreasList = mSources.get(3).optJSONArray(
						Integer.parseInt(itemp[2]));
				mAddrTV.setText("");
			} else if (itemp.length == 2) {
				mAddrArea.setVisibility(View.GONE);
				String address = mAddrProvince.getText().toString()
						+ mAddrCity.getText().toString() + itemp[0];
				mAddrTV.setText(address);
			}
		} else if (v == mAddrArea) {
			itemp = mSelectedArea.split(":");
			mAddrArea.setText(itemp[0]);
			if (itemp.length == 3) {
				mAreasList = mSources.get(4).optJSONArray(
						Integer.parseInt(itemp[2]));
			}
			String address = mAddrProvince.getText().toString()
					+ mAddrCity.getText().toString()
					+ mAddrTown.getText().toString() + itemp[0];
			mAddrTV.setText(address);
		}
	}

	private void showDialog(final Type type, JSONArray list) {
		final CustomDialog mDialog = new CustomDialog(mActivity);
		mDialog.setTitle(type.toString());
		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.dialog_address_picker, null);
		mDialog.setCustomView(v);
		mDialog.setCancelable(true).setCanceledOnTouchOutside(true);
		mDialog.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		mDialog.setNegativeButton(R.string.cancel, null).show();
		mAdapter = new MyAdapter(list);
		final ListView listview = (ListView) v
				.findViewById(R.id.dialog_address_list);
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MyAdapter adapter = (MyAdapter) listview.getAdapter();
				if (type == Type.PROVINCE) {
					// mSelectedProvince = mProvincesList.optString(position);
					mSelectedProvince = adapter.getItem(position);
					mSelectedCity = "";
					mSelectedTown = "";
					mSelectedArea = "";
					parserAddress(mAddrProvince);
				} else if (type == Type.CITY) {
					// mSelectedCity = mCitiesList.optString(position);
					mSelectedCity = adapter.getItem(position);
					mSelectedTown = "";
					mSelectedArea = "";
					parserAddress(mAddrCity);
				} else if (type == Type.TOWN) {
					// mSelectedTown = mTownList.optString(position);
					mSelectedTown = adapter.getItem(position);
					mSelectedArea = "";
					parserAddress(mAddrTown);
				} else if (type == Type.AREA) {
					// mSelectedArea = mAreasList.optString(position);
					mSelectedArea = adapter.getItem(position);
					parserAddress(mAddrArea);
				}
				mDialog.dismiss();
			}
		});

	}

	private class MyAdapter extends BaseAdapter {

		private JSONArray array;
		private ArrayList<String> showingData = new ArrayList<String>();

		public MyAdapter(JSONArray list) {
			this.array = list;
			for (int i = 0, c = array.length(); i < c; i++)
				if (!TextUtils.isEmpty(array.optString(i)))
					this.showingData.add(array.optString(i));
		}

//		public void resetShowingData(ArrayList<String> data) {
//			this.showingData = data;
//			this.notifyDataSetChanged();
//		}
//
//		public JSONArray getSource() {
//			return array;
//		}

		@Override
		public int getCount() {
			return showingData.size();
		}

		@Override
		public String getItem(int position) {
			return showingData.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						android.R.layout.simple_list_item_1, null);
				TextView tv = ((TextView) convertView
						.findViewById(android.R.id.text1));
				tv.setTextColor(mActivity.getResources().getColor(
						R.color.text_textcolor_gray1));
				tv.setTextSize(16);
			}

			((TextView) convertView.findViewById(android.R.id.text1))
					.setText(getItem(position).toString());
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
						mProvincesList = mSources.get(0);
						if (mAddressInfo != null) {
							setEditData();
						} else {
							LoginedUser user = AgentApplication.getApp(mActivity)
									.getLoginedUser();
							String username = Run.loadOptionString(mActivity,
									Run.pk_logined_username, "");
							if (user != null && !TextUtils.isEmpty(username)
									&& Run.isPhoneNumber(username))
								mTelPhoneEdt.setText(username);
							if (mProvincesList.length() == 1) {
								mSelectedProvince = mProvincesList.optString(0);
								parserAddress(mAddrProvince);
							}
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

	private void getAddrAndArea() {
		if (!TextUtils.isEmpty(mSelectedProvince)) {
//			addr += mSelectedProvince.split(":")[0];
			area += "mainland:" + mSelectedProvince.split(":")[0];
		}
		if (!TextUtils.isEmpty(mSelectedCity)) {
			String[] item = mSelectedCity.split(":");
//			addr += mSelectedCity.split(":")[0];
			if (item.length < 3) {
				area += "/" + mSelectedCity.split(":")[1];
			} else {
				area += "/" + mSelectedCity.split(":")[0];
			}
		}
		if (!TextUtils.isEmpty(mSelectedTown)) {
			String[] item = mSelectedTown.split(":");
//			addr += mSelectedTown.split(":")[0];
			if (item.length < 3) {
				area += "/" + mSelectedTown;
			} else {
				area += "/" + mSelectedTown.split(":")[0];
			}
		} 
		if (!TextUtils.isEmpty(mSelectedArea)) {
			String[] item = mSelectedArea.split(":");
//			addr += mSelectedArea.split(":")[0];
			area += "/" + mSelectedArea;
		}
	}

	private class SaveAddressTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mActivity.setResult(Activity.RESULT_OK);
					Toast.makeText(mActivity, "添加成功", Toast.LENGTH_SHORT)
							.show();
					mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			getAddrAndArea();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.save_rec");
			req.addParams("name", mNameEdt.getText().toString())
					.addParams("mobile", mTelPhoneEdt.getText().toString())
					.addParams("zip", mPostNumEdt.getText().toString())
					.addParams("area", area)
					.addParams("addr", mAddrDetailEdt.getText().toString());
			if (mAddressInfo != null
					&& !TextUtils.isEmpty(mAddressInfo.optString("addr_id")))
				req.addParams("addr_id", mAddressInfo.optString("addr_id"));
			else
				req.addParams("def_addr", "1");
			if (mAddressInfo != null) {
				req.addParams("def_addr", mAddressInfo.optString("def_addr"));
			}
			Log.i("", "---->>>>---"+req.toString());
			return req;
		}
	}

	public enum Type {
		PROVINCE {
			@Override
			public String toString() {
				return "省份";
			}
		},
		CITY {
			@Override
			public String toString() {
				return "城市";
			}
		},
		TOWN {
			@Override
			public String toString() {
				return "区/县";
			}
		},
		AREA {
			@Override
			public String toString() {
				return "街道";
			}
		};
	}
}
