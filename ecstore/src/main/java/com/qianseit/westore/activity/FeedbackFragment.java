package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

/**
 * 反馈
 */
public class FeedbackFragment extends BaseDoFragment implements OnClickListener {

	private EditText mPhoneText, mContentText;
	private ArrayList<RadioButton> mRadioButton = new ArrayList<RadioButton>();
	private String subObject;
	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.feedback);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_feedback, null);
		mRadioButton.add((RadioButton) rootView
				.findViewById(R.id.feedback_radiobtn1));
		mRadioButton.add((RadioButton) rootView
				.findViewById(R.id.feedback_radiobtn2));
		mRadioButton.add((RadioButton) rootView
				.findViewById(R.id.feedback_radiobtn3));
		mRadioButton.add((RadioButton) rootView
				.findViewById(R.id.feedback_radiobtn4));
		mRadioButton.add((RadioButton) rootView
				.findViewById(R.id.feedback_radiobtn5));
		mRadioButton.add((RadioButton) rootView
				.findViewById(R.id.feedback_radiobtn6));
		mPhoneText = (EditText) findViewById(R.id.feedback_phone_number);
		mContentText = (EditText) findViewById(R.id.feedback_content);
		findViewById(R.id.feedback_submit).setOnClickListener(this);
		for (int i = 0; i < mRadioButton.size(); i++) {
			mRadioButton.get(i).setOnCheckedChangeListener(changeListener);
		}
		mRadioButton.get(0).setChecked(true);
		String s1 = "您也可以直接致电客服电话：400-888-9739";
		SpannableString sp = new SpannableString(s1);
		ForegroundColorSpan fcs = new ForegroundColorSpan(
				Color.parseColor(getString(R.color.theme_color)));
		sp.setSpan(fcs, s1.indexOf("：") + 1, s1.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		((TextView) findViewById(R.id.feedback_service)).setText(sp);
		findViewById(R.id.feedback_service).setOnClickListener(this);
		new JsonTask().execute(new GetSuggestType());
	}

	private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				subObject = buttonView.getText().toString();
				for (int i = 0; i < mRadioButton.size(); i++) {
					if (buttonView != mRadioButton.get(i)) {
						mRadioButton.get(i).setChecked(false);
					} else {
						mRadioButton.get(i).setChecked(true);
					}
				}
			}
		}
	};

	@Override
	public void onClick(View v) {
		if (R.id.feedback_submit == v.getId()) {
			if (TextUtils.isEmpty(mContentText.getText()))
				return;
			Run.excuteJsonTask(new JsonTask(), new FeedbackTask());
		} else if (v.getId() == R.id.feedback_service) {
			String text = ((TextView) v).getText().toString();
			final String photo[] = text.split("：");
			dialog = AccountLoginFragment.showAlertDialog(mActivity, "是否拨打电话！",
					"取消", "确定", null, new OnClickListener() {

						@Override
						public void onClick(View v) {
							String pNum = photo[1];
							String tel = pNum.replace("-", "");
							Intent intent = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel:" + tel));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						}
					}, false, null);

		} else {
			super.onClick(v);
		}
	}

	private class FeedbackTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.member.send_msg")
					.addParams("comment", mContentText.getText().toString())
					.addParams("subject", subObject)
					.addParams("contact", mPhoneText.getText().toString());
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					// Run.alert(mActivity, R.string.feedback_success);
					dialog = AccountLoginFragment.showAlertDialog(mActivity,
							"您的意见已经提交成功，谢谢！", "", "OK", null,
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									mActivity.finish();
								}
							}, false, null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class GetSuggestType implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray list = all.optJSONArray("data");
					for (int i = 0; i < mRadioButton.size(); i++) {
						mRadioButton.get(i).setText(list.optString(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean("mobileapi.info.get_suggest_type");
		}
		
	}

}
