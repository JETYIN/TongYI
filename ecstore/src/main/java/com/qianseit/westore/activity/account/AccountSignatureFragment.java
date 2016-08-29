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

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class AccountSignatureFragment extends BaseDoFragment {
    private EditText mEditText;
    private LoginedUser mLoginedUser;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mActionBar.setTitle(R.string.account_signature_title);
		mActionBar.setRightTitleButton(R.string.account_signature_submit, this);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView=inflater.inflate(R.layout.fragment_signature_main, null);
		mEditText=(EditText)findViewById(R.id.account_signature_edit);
		mEditText.setText(mLoginedUser.getRemark());
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v==mActionBar.getRightButton()){
			String strText=mEditText.getText().toString().trim();
			if(!TextUtils.isEmpty(strText)){
				Run.excuteJsonTask(new JsonTask(),new UpdateSignatureTask(strText));
			}
		}
	}
	private class UpdateSignatureTask implements JsonTaskHandler {
		private String content;

		public UpdateSignatureTask(String content) {
			this.content = content;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.member.save_setting")
					.addParams("desc",content);
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					 Intent intent=new Intent();
					 intent.putExtra(Run.EXTRA_VALUE,content);
                     mActivity.setResult(Activity.RESULT_OK,intent);
                     mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
