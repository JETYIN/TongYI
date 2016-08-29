package com.qianseit.westore.activity.account;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import cn.shopex.ecstore.R;

public class InvoiceEditorFragment extends BaseDoFragment implements
		OnCheckedChangeListener {
	private JSONObject mInvoiceInfo = new JSONObject();

	private EditText mInvoiceTitleText;
	private Button mInvoiceContent;
	private RadioButton mNormalRadio;
	private RadioButton mCompanyRadio;
	private RadioButton mNulllRadio;
	private String[] types = new String[]{};

	public InvoiceEditorFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.invoice_info);

		rootView = inflater.inflate(R.layout.fragment_invoice, null);
		mInvoiceTitleText = (EditText) findViewById(R.id.invoice_title);
		findViewById(R.id.invoice_submit).setOnClickListener(this);
		findViewById(R.id.invoice_content).setOnClickListener(this);
		mInvoiceContent = (Button) findViewById(R.id.invoice_content);
		mNormalRadio = (RadioButton) findViewById(R.id.invoice_person_radio);
		mCompanyRadio = (RadioButton) findViewById(R.id.invoice_company_radio);
		mNulllRadio = (RadioButton) findViewById(R.id.invoice_null_radio);
		mNulllRadio.setOnCheckedChangeListener(this);
		mNormalRadio.setOnCheckedChangeListener(this);
		mCompanyRadio.setOnCheckedChangeListener(this);
		try {
			String jsonStr = mActivity.getIntent().getStringExtra(
					Run.EXTRA_DATA);
			mInvoiceInfo = new JSONObject(jsonStr);
			mInvoiceContent.setText(mInvoiceInfo.optString("content"));
			mInvoiceTitleText.setText(mInvoiceInfo.optString("dt_name"));
			if (TextUtils.equals("company", mInvoiceInfo.optString("type")))
				mCompanyRadio.setChecked(true);
			else
				mNormalRadio.setChecked(true);
		} catch (Exception e) {
		}
		new JsonTask().execute(new GetContent());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			int visible = (buttonView == mNulllRadio) ? View.GONE
					: View.VISIBLE;
			findViewById(R.id.invoice_content_item).setVisibility(visible);
			findViewById(R.id.invoice_title_item).setVisibility(visible);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.invoice_submit) {
			Intent data = new Intent();
			// 不开发票，直接返回
			if (mNulllRadio.isChecked()) {
				mActivity.setResult(Activity.RESULT_OK, data);
				mActivity.finish();
				return;
			}

			RadioButton checkedRadio = mNormalRadio.isChecked() ? mNormalRadio
					: mCompanyRadio;
			if (!TextUtils.isEmpty(mInvoiceTitleText.getText())
					&& !TextUtils.isEmpty(mInvoiceContent.getText())) {
				try {
					mInvoiceInfo.put("type", checkedRadio.getTag().toString());
					mInvoiceInfo.put("type_name", checkedRadio.getText()
							.toString());
					mInvoiceInfo.put("dt_name", mInvoiceTitleText.getText()
							.toString());
					mInvoiceInfo.put("content", mInvoiceContent.getText()
							.toString());

					data.putExtra(Run.EXTRA_DATA, mInvoiceInfo.toString());
					mActivity.setResult(Activity.RESULT_OK, data);
					mActivity.finish();
				} catch (Exception e) {
				}
			}
		} else if (v.getId() == R.id.invoice_content) {
//			types = { "食品", "日用品", "劳保用品", "商品明细" };
			final CustomDialog mDialog = new CustomDialog(mActivity);
			mDialog.setTitle(R.string.invoice_content);
			mDialog.setSingleChoiceItems(types, -1, new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int pos, long id) {
					mDialog.dismiss();
					try {
						mInvoiceInfo.put("detail", types[pos]);
						((TextView) findViewById(R.id.invoice_content))
								.setText(types[pos]);
					} catch (Exception e) {
					}
				}
			}).setCancelable(true).show();
		} else {
			super.onClick(v);
		}
	}
	
	private class GetContent implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						JSONArray contents = data.optJSONArray("tax_content");
						int count = contents == null ? 0 : contents.length();
						types = new String[count];
						for (int i = 0; i < count; i++) {
							types[i] = contents.optString(i);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.get_invoice");
			return req;
		}
		
	}
}
