package com.qianseit.westore.activity.account;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class ChargeMethods extends BaseDoFragment {

	private VolleyImageLoader mVolleyImageLoader;
	private LayoutInflater mInflater;
	private LinearLayout mContainer;

	public ChargeMethods() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.charge_methods);
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_charge_methods, null);
		mContainer = (LinearLayout) rootView.findViewById(R.id.fragment_charge_methods_container);
		new JsonTask().execute(new getChargeTerms());
	}

	private void updatePayView(JSONArray list) {
		JSONObject obj = null;
		for (int i = 0; i < list.length(); i++) {
			obj = list.optJSONObject(i);
			View v = mInflater.inflate(R.layout.view_charge_item, null);
			ImageView img = (ImageView) v.findViewById(R.id.view_charge_item_pay_icon);
			mVolleyImageLoader.showImage(img, obj.optString("icon_src"));
			TextView txt = (TextView) v.findViewById(R.id.view_charge_item_pay_name);
			txt.setText(obj.optString("app_name"));
			v.setTag(obj);
			v.setOnClickListener(this);
			mContainer.addView(v);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			JSONObject obj = (JSONObject) v.getTag();
			startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_CAHARGE_COUNT)
					.putExtra(Run.EXTRA_DATA, obj.optString("app_id"))
					.putExtra(Run.EXTRA_TITLE, obj.optString("app_name")));
		}
		super.onClick(v);
	}

	private class getChargeTerms implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						updatePayView(child);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {

			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.order.select_payment");
			req.addParams("platform", "wap");
			return req;
		}

	}
}
