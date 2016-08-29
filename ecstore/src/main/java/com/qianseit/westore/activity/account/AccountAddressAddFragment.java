package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.IdCardUtils;

public class AccountAddressAddFragment extends BaseDoFragment {
	private final int REQUEST_CODE = 0x1001;

	private EditText mNameEdt;
	private EditText mTelPhoneEdt;
	private EditText mAddrDetailEdt;
	private EditText mAddrUserID;
	private TextView mAddrProvince;
	private TextView mAddrCity;
	private TextView mAddrTown;

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
//	private JSONArray mAreasList = new JSONArray();
	private JSONArray mTownList = new JSONArray();
	private StringBuilder builder = new StringBuilder();
	private Dialog dialog;
	private String title;

	public AccountAddressAddFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			Intent data = mActivity.getIntent();
			title = data.getStringExtra("com.qianseit.westore.EXTRA_FILE_NAME");
			if (title != null) {
				mActionBar.setTitle(title);
			} else {
				mActionBar.setTitle(R.string.my_address_book_editor);
			}
			boolean bstatus = data.getBooleanExtra(Run.EXTRA_VALUE, false);
			if (bstatus) {
				mActionBar.setShowRightButton(true);
				mActionBar.getRightButton().setText("删除");

				mActionBar.getRightButton().setOnClickListener(this);
			}
			mAddressInfo = new JSONObject(data.getStringExtra(Run.EXTRA_DATA));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_add_address_main, null);
		mNameEdt = (EditText) rootView.findViewById(R.id.fragment_add_reciver_address_name);
		mTelPhoneEdt = (EditText) rootView.findViewById(R.id.fragment_add_reciver_address_tel);
		mAddrDetailEdt = (EditText) rootView.findViewById(R.id.fragment_add_reciver_address_detail);
		mAddrProvince = (TextView) rootView.findViewById(R.id.fragment_add_reciver_address_province);
		mAddrCity = (TextView) rootView.findViewById(R.id.fragment_add_reciver_address_city);
		mAddrTown = (TextView) rootView.findViewById(R.id.fragment_add_reciver_address_town);
		mAddrUserID = (EditText) findViewById(R.id.fragment_add_reciver_address_id);
		findViewById(R.id.account_save_address_text).setOnClickListener(this);
		mAddrUserID.setOnClickListener(this);
		mAddrProvince.setOnClickListener(this);
		mAddrCity.setOnClickListener(this);
		mAddrTown.setOnClickListener(this);

		//身份证号
		mAddrUserID.setKeyListener(new NumberKeyListener() {
			protected char[] getAcceptedChars()
			{
				char[] numberChars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'X' ,'x'};
				return numberChars;
			}
			@Override
			public int getInputType() {
				// TODO 自动生成的方法存根
				return InputType.TYPE_CLASS_TEXT;
			}

		});

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
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mActionBar.getRightButton()) {
			if (mAddressInfo != null) {
				dialog = AccountLoginFragment.showAlertDialog(mActivity, "确定删除此收货信息？", "取消", "确定", new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						String addrId = mAddressInfo.optString("addr_id");
						Run.excuteJsonTask(new JsonTask(), new DelAddressTask(addrId));

					}
				}, false, null);
			}
		}
		if (v.getId() == R.id.account_save_address_text) {
//			if (mAddrUserID.getText().toString().trim().length() > 0) {
//				if (!IdCardUtils.IDCardValidate(mAddrUserID.getText().toString().trim())) {
//
//					dialog = AccountLoginFragment.showAlertDialog(mActivity, "身份证号码有误哦，请核实", "", "OK", new OnClickListener() {
//
//						@Override
//						public void onClick(View v) {
//							dialog.dismiss();
//						}
//					}, null, false, null);
//					return;
//				}
//			} else {
//				AccountLoginFragment.showAlertDialog(mActivity, "请填写身份证信息", "", "OK", null, null, false, null);
//				return;
//			}
			if (TextUtils.isEmpty(mNameEdt.getText().toString())) {
				String label = mActivity.getString(R.string.my_address_book_editor_username);
				dialog = AccountLoginFragment.showAlertDialog(mActivity, label, "", "ok", new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, null, false, null);
			} else if (TextUtils.isEmpty(mTelPhoneEdt.getText().toString()) || !Run.isChinesePhoneNumber(mTelPhoneEdt.getText().toString())) {
				// String label = mActivity
				// .getString(R.string.my_address_book_editor_phone);
				// dialog = AccountLoginFragment.showAlertDialog(mActivity,
				// label,
				// "", "ok", new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// dialog.dismiss();
				// }
				// }, null, false, null);
				mTelPhoneEdt.requestFocus();
				AccountLoginFragment.showAlertDialog(mActivity, "电话号码有误哦,请核实!", "", "OK", null, null, false, null);
			} else if (TextUtils.isEmpty(mAddrCity.getText().toString().trim()) || TextUtils.isEmpty(mAddrProvince.getText().toString().trim())
					|| TextUtils.isEmpty(mAddrTown.getText().toString().trim())) {
				dialog = AccountLoginFragment.showAlertDialog(mActivity, "请填写地区信息", "", "ok", new OnClickListener() {
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, null, false, null);
			} else if (TextUtils.isEmpty(mAddrDetailEdt.getText().toString())) {
				String label = mActivity.getString(R.string.my_address_book_editor_address);
				dialog = AccountLoginFragment.showAlertDialog(mActivity, label, "", "ok", new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, null, false, null);
			} else if (TextUtils.isEmpty(mAddrDetailEdt.getText().toString())) {
				String label = mActivity.getString(R.string.my_address_book_editor_address);
				dialog = AccountLoginFragment.showAlertDialog(mActivity, label, "", "ok", new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, null, false, null);
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
				dialog = AccountLoginFragment.showAlertDialog(mActivity, "请先选择省份", "", "ok", new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, null, false, null);
			}
		} else if (v == mAddrTown) {
			if (mTownList.length() > 0) {
				showDialog(Type.TOWN, mTownList);
			} else {
				dialog = AccountLoginFragment.showAlertDialog(mActivity, "请先选择市", "", "ok", new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				}, null, false, null);
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
			mNameEdt.setSelection(mAddressInfo.optString("name").trim().length());
			mTelPhoneEdt.setText(mAddressInfo.optString("mobile"));
			mAddrDetailEdt.setText(mAddressInfo.optString("addr"));
			mAddrUserID.setText(mAddressInfo.optString("card_num"));
			String tempArea = mAddressInfo.optString("area");
			tempArea = tempArea.replaceAll("mainland:", "");
			String[] item = tempArea.split("/");
			for (int i = 0; i < item.length; i++) {
				if (i == 0) {
					for (int j = 0; j < mSources.get(i).length(); j++) {
						if (item[i].equals(mSources.get(i).optString(j).split(":")[0])) {
							mSelectedProvince = mSources.get(i).optString(j);
							mProvincesList = mSources.get(i);
							mAddrProvince.setText(item[i]);
							break;
						}
					}
				} else if (i == 1) {
					JSONArray list = mSources.get(i).optJSONArray(Integer.parseInt(mSelectedProvince.split(":")[2]));
					mCitiesList = list;
					for (int j = 0; j < list.length(); j++) {
						if (list.optString(j).split(":")[0].equals(item[i])) {
							mSelectedCity = list.optString(j);
							mAddrCity.setText(item[i]);
							break;
						}
					}
				} else if (i == 2) {
					JSONArray list = mSources.get(i).optJSONArray(Integer.parseInt(mSelectedCity.split(":")[2]));
					mTownList = list;
					for (int j = 0; j < list.length(); j++) {
						if (i == item.length - 1) {
							if (list.optString(j).split(":")[0].equals(item[i].split(":")[0])) {
								mSelectedTown = list.optString(j);
								mAddrTown.setText(mSelectedTown.split(":")[0]);
								mAddrTown.setVisibility(View.VISIBLE);
								break;
							}
						} else {
							if (list.optString(j).split(":")[0].equals(item[i])) {
								mSelectedTown = list.optString(j);
								mAddrTown.setText(item[i]);
								break;
							}
						}
					}
				} else if (i == 3) {
					JSONArray list = mSources.get(i).optJSONArray(Integer.parseInt(mSelectedTown.split(":")[2]));
//					mAreasList = list;
					for (int j = 0; j < list.length(); j++) {
						if (list.optString(j).equals(item[i])) {
							mSelectedArea = list.optString(j);
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
			// mAddrTown.setText(Type.TOWN.toString());
			mCitiesList = new JSONArray();
			mTownList = new JSONArray();
//			mAreasList = new JSONArray();
			itemp = mSelectedProvince.split(":");
			mAddrProvince.setText(itemp[0]);
			mAddrTown.setVisibility(View.GONE);
			if (itemp.length == 3) {
				mCitiesList = mSources.get(1).optJSONArray(Integer.parseInt(itemp[2]));
				if (mCitiesList.length() == 1) {
					mSelectedCity = mCitiesList.optString(0);
					parserAddress(mAddrCity);
				} else {
					mAddrCity.setText("");
				}
			} else if (itemp.length == 2) {
				mAddrCity.setVisibility(View.GONE);
			}
		} else if (v == mAddrCity) {
			// mAddrTown.setText(Type.TOWN.toString());
			mTownList = new JSONArray();
//			mAreasList = new JSONArray();
			itemp = mSelectedCity.split(":");
			mAddrCity.setText(itemp[0]);
			if (itemp.length == 3) {
				mAddrTown.setVisibility(View.VISIBLE);
				mTownList = mSources.get(2).optJSONArray(Integer.parseInt(itemp[2]));
				if (mTownList.length() == 1) {
					parserAddress(mAddrTown);
				} else {
					builder.append("");
				}
			} else if (itemp.length == 2) {
				mAddrTown.setVisibility(View.GONE);
				String address = mAddrProvince.getText().toString() + itemp[0];
				builder.append("");

			}
		} else if (v == mAddrTown) {
			;
//			mAreasList = new JSONArray();
			itemp = mSelectedTown.split(":");
			mAddrTown.setText(itemp[0]);
			if (itemp.length == 3) {
//				mAreasList = mSources.get(3).optJSONArray(Integer.parseInt(itemp[2]));
				builder.append("");
			} else if (itemp.length == 2) {
				String address = mAddrProvince.getText().toString() + mAddrCity.getText().toString() + itemp[0];
				builder.append(address);
			}
		}
	}

	private void showDialog(final Type type, JSONArray list) {
		final CustomDialog mDialog = new CustomDialog(mActivity);
		mDialog.setTitle(type.toString());
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_address_picker, null);
		mDialog.setCustomView(v);
		mDialog.setCancelable(true).setCanceledOnTouchOutside(true);
		mDialog.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		mDialog.setNegativeButton(R.string.cancel, null).show();
		mAdapter = new MyAdapter(list);
		final ListView listview = (ListView) v.findViewById(R.id.dialog_address_list);
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MyAdapter adapter = (MyAdapter) listview.getAdapter();
				if (type == Type.PROVINCE) {
					mSelectedProvince = adapter.getItem(position);
					mSelectedCity = "";
					mSelectedTown = "";
					mSelectedArea = "";
					parserAddress(mAddrProvince);
				} else if (type == Type.CITY) {
					mSelectedCity = adapter.getItem(position);
					mSelectedTown = "";
					mSelectedArea = "";
					parserAddress(mAddrCity);
				} else if (type == Type.TOWN) {
					mSelectedTown = adapter.getItem(position);
					mSelectedArea = "";
					parserAddress(mAddrTown);
				} else if (type == Type.AREA) {
					mSelectedArea = adapter.getItem(position);
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
				convertView = mActivity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
				TextView tv = ((TextView) convertView.findViewById(android.R.id.text1));
				tv.setTextColor(mActivity.getResources().getColor(R.color.text_textcolor_gray1));
				tv.setTextSize(16);
			}

			((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position).toString());
			String[] items = getItem(position).split(":");
			if (items[0].contains("其它")) {
				((TextView) convertView.findViewById(android.R.id.text1)).setText(items[0] + getString(R.string.select_addr_tips));
			} else {
				((TextView) convertView.findViewById(android.R.id.text1)).setText(items[0]);
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
							LoginedUser user = AgentApplication.getApp(mActivity).getLoginedUser();
							String username = Run.loadOptionString(mActivity, Run.pk_logined_username, "");
							if (user != null && !TextUtils.isEmpty(username) && Run.isPhoneNumber(username))
								// mTelPhoneEdt.setText(username);
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
			return new JsonRequestBean( "mobileapi.member.get_regions");
		}

	}

	private void getAddrAndArea() {
		if (!TextUtils.isEmpty(mSelectedProvince)) {
			area += "mainland:" + mSelectedProvince.split(":")[0];
		}
		if (!TextUtils.isEmpty(mSelectedCity)) {
			String[] item = mSelectedCity.split(":");
			if (item.length < 3) {
				area += "/" + mSelectedCity.split(":")[1];
			} else {
				area += "/" + mSelectedCity.split(":")[0];
			}
		}
		if (!TextUtils.isEmpty(mSelectedTown)) {
			String[] item = mSelectedTown.split(":");
			if (item.length < 3) {
				area += "/" + mSelectedTown;
			} else {
				area += "/" + mSelectedTown.split(":")[0];
			}
		}
		if (!TextUtils.isEmpty(mSelectedArea)) {
			String[] item = mSelectedArea.split(":");
			// addr += mSelectedArea.split(":")[0];
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
					if (title != null) {
						Toast.makeText(mActivity, "保存成功", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mActivity, "添加成功", Toast.LENGTH_SHORT).show();
					}
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
			JsonRequestBean req = new JsonRequestBean( "mobileapi.member.save_rec");
			req.addParams("name", mNameEdt.getText().toString()).addParams("mobile", mTelPhoneEdt.getText().toString()).addParams("area", area)
					.addParams("addr", mAddrDetailEdt.getText().toString());
			if (mAddressInfo != null && !TextUtils.isEmpty(mAddressInfo.optString("addr_id")))
				req.addParams("addr_id", mAddressInfo.optString("addr_id"));
			else
				req.addParams("def_addr", "1");
			if (mAddressInfo != null) {
				req.addParams("def_addr", mAddressInfo.optString("def_addr"));
			}
//			if (!TextUtils.isEmpty(mAddrUserID.getText().toString().trim()))
//				req.addParams("card_num", mAddrUserID.getText().toString().trim());
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

	private class DelAddressTask implements JsonTaskHandler {
		private String addrId;

		public DelAddressTask(String addrId) {
			this.addrId = addrId;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.member.del_rec").addParams("addr_id", addrId);
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
//					startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ADDRESS_BOOK));
//					mActivity.finish();
					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
