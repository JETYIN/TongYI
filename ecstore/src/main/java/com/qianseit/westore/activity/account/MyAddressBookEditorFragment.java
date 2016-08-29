package com.qianseit.westore.activity.account;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class MyAddressBookEditorFragment extends BaseDoFragment {
	public final int REQUEST_CODE_REGION_PICKER = 0x1000;

	private JSONObject mAddressInfo;

	private TextView mCityText;
	private EditText mAddressText;
	private EditText mNameText, mPhoneText, mPostText;

	private String valueArea;

	public MyAddressBookEditorFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		mActionBar.setTitle(R.string.my_address_book_editor);

		int layoutid = R.layout.fragment_my_address_book_editor;
		rootView = inflater.inflate(layoutid, null);
		mCityText = (TextView) findViewById(R.id.my_address_book_editor_district);
		mNameText = (EditText) findViewById(R.id.my_address_book_editor_username);
		mPhoneText = (EditText) findViewById(R.id.my_address_book_editor_phone);
		mPostText = (EditText) findViewById(R.id.my_address_book_editor_postal);
		mAddressText = (EditText) findViewById(R.id.my_address_book_editor_address);
		mCityText.setOnClickListener(this);

		if (mAddressInfo != null) {
			mNameText.setText(mAddressInfo.optString("name"));
			mCityText.setText(mAddressInfo.optString("txt_area"));
			mPhoneText.setText(mAddressInfo.optString("mobile"));
			mAddressText.setText(mAddressInfo.optString("addr"));
			mPostText.setText(mAddressInfo.optString("zip"));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_REGION_PICKER
				&& resultCode == Activity.RESULT_OK) {
			String address = data.getStringExtra(Run.EXTRA_VALUE);
			mCityText.setText(address);
			valueArea = data.getStringExtra(Run.EXTRA_DATA);
			valueArea = valueArea.replaceFirst(getString(R.string.select_addr_tips), "");
			if (address.contains("其它")) {
				String ss = data.getStringExtra(Run.EXTRA_ADDR);
				mAddressText.setText(ss);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		if (v == mActionBar.getRightButton()) {
			if (TextUtils.isEmpty(mNameText.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_username);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else if (TextUtils.isEmpty(mPhoneText.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_phone);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else if (TextUtils.isEmpty(mCityText.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_district);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else if (TextUtils.isEmpty(mAddressText.getText().toString())) {
				String label = mActivity
						.getString(R.string.my_address_book_editor_address);
				Run.alert(mActivity,
						mActivity.getString(R.string.please_input, label));
			} else
				Run.excuteJsonTask(new JsonTask(), new SaveAddressTask());
		} else if (v == mCityText) {
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_MY_ADDRESS_PICKER),
					REQUEST_CODE_REGION_PICKER);
		} else {
			super.onClick(v);
		}
	}

	private class SaveAddressTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.save_rec")
					.addParams("name", mNameText.getText().toString())
					.addParams("mobile", mPhoneText.getText().toString())
					.addParams("zip", mPostText.getText().toString())
					.addParams(
							"area",
							TextUtils.isEmpty(valueArea) ? mAddressInfo
									.optString("area") : valueArea)
					.addParams("addr", mAddressText.getText().toString());
			if (mAddressInfo != null
					&& !TextUtils.isEmpty(mAddressInfo.optString("addr_id")))
				bean.addParams("addr_id", mAddressInfo.optString("addr_id"));
			else
				bean.addParams("def_addr", "1");
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mActivity.setResult(Activity.RESULT_OK);
					mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
