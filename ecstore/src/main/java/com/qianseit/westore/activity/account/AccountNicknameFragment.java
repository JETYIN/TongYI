package com.qianseit.westore.activity.account;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class AccountNicknameFragment extends BaseDoFragment {

	private EditText mNewNameEdt;
	private JSONArray sttrs;

	public AccountNicknameFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_nickname_change);
		rootView = inflater.inflate(R.layout.fragment_account_nickname, null);
		mNewNameEdt = (EditText) findViewById(android.R.id.text1);
		mActionBar.setRightTitleButton("确定", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(TextUtils.isEmpty(mNewNameEdt.getText().toString())){
					Run.alert(mActivity, "输入不能为空");
				}else{
					new JsonTask().execute(new UpdateAccountNameTask());
				}
			}
		});
		mNewNameEdt.setText(AgentApplication.getApp(mActivity).getLoginedUser()
				.getNickName(mActivity));
		mNewNameEdt.setSelection(mNewNameEdt.length());
		mNewNameEdt.addTextChangedListener(textWatcher);
	}
	private TextWatcher textWatcher=new TextWatcher() {
		 private int editStart;
		 private int editEnd;
		 private int maxLen = 16; 
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			editStart = mNewNameEdt.getSelectionStart();
			editEnd = mNewNameEdt.getSelectionEnd();
			// 先去掉监听器，否则会出现栈溢出
			mNewNameEdt.removeTextChangedListener(textWatcher);
			if (!TextUtils.isEmpty(mNewNameEdt.getText())) {
				String etstring = mNewNameEdt.getText().toString().trim();
				while (calculateLength(s.toString()) > maxLen) {
					s.delete(editStart - 1, editEnd);
					editStart--;
					editEnd--;
				}
			}

			mNewNameEdt.setText(s);
			mNewNameEdt.setSelection(editStart);

			// 恢复监听器
			mNewNameEdt.addTextChangedListener(textWatcher);
		}
		private int calculateLength(String etstring) {
			char[] ch = etstring.toCharArray();

			int varlength = 0;
			for (int i = 0; i < ch.length; i++) {
				// changed by zyf 0825 , bug 6918，加入中文标点范围 ， TODO 标点范围有待具体化
				if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb
					varlength = varlength + 2;
				} else {
					varlength++;
				}
			}
			// 这里也可以使用getBytes,更准确嘛
	        // varlength = etstring.getBytes(CharSet.forName("GBK")).lenght;// 编码根据自己的需求，注意u8中文占3个字节...
			return varlength;
		}
	};
	
	private class GetAccountNameTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				// {"rsp":"succ","data":{"mem":"null","attr":[]},"res":""}
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.getJSONObject("data");
					if (data != null) { // null --> 没有可修改的属性
						sttrs = data.getJSONArray("attr");
						if (sttrs != null && sttrs.length() > 0) {
							// [{"attr_id":1,"attr_option":"","attr_show":"true","attr_column":"contact[name]","attr_required":"false","attr_value":null,"attr_type":"text","attr_tyname":"系统默认","attr_name":"姓名","attr_valtype":""}]
							for (int i = 0; i < sttrs.length(); i++) {
								JSONObject sttr = sttrs.optJSONObject(i);
								if (sttr.optString("attr_name").equals("姓名")) { // true
																				// 姓名属性可修改
									new JsonTask()
											.execute(new UpdateAccountNameTask());
									break;
								}
							}
						} else {
							hideLoadingDialog();
							Toast.makeText(mActivity, "商家暂时不支持修改昵称",
									Toast.LENGTH_SHORT).show();
						}
					}
				} else {

				}
			} catch (Exception e) {

			}
		}

		@Override
		public JsonRequestBean task_request() {
			showLoadingDialog();
			JsonRequestBean changeName = new JsonRequestBean(
					"mobileapi.member.setting");
			return changeName;
		}

	}

	private class UpdateAccountNameTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();			
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					// 重新登录
				AgentApplication.getLoginedUser(mActivity).setNickName(mNewNameEdt.getText()
						.toString());
				  mActivity.setResult(Activity.RESULT_OK);
				  mActivity.finish();
				  
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean changeName = new JsonRequestBean(
					"mobileapi.member.save_setting");
			changeName.addParams("name", mNewNameEdt.getText()
					.toString());
			return changeName;
		}
	}
}
