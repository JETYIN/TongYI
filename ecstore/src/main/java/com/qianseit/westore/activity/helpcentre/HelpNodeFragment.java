package com.qianseit.westore.activity.helpcentre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class HelpNodeFragment extends BaseDoFragment {

	private LinearLayout mContentLayout;
	private String ids;
	private Map<String, List<JSONObject>> map = new HashMap<String, List<JSONObject>>();
	private List<String> mNodesList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		String title = "";
		if (b != null) {
			title = b.getString("title");
			ids = b.getString("ids");
		}
		mActionBar.setTitle(title);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_help_using, null);
		mContentLayout = (LinearLayout) rootView
				.findViewById(R.id.fragment_help_using_content_layout);
//		initContentView();
		new JsonTask().execute(new GetData());
	}

	private void initContentView() {
		LinearLayout.LayoutParams lp = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		TextView tv = new TextView(mActivity);
		tv.setText(R.string.safe_pay);
		tv.setPadding(20, 20, 0, 20);
		tv.setLayoutParams(lp);
		tv.setBackgroundColor(0xffcccccc);
		mContentLayout.addView(tv);
		List<String> list = new ArrayList<String>();
		list.add(getString(R.string.arrive_pay));
		list.add(getString(R.string.online_pay));
		HelpAdapter adapter = new HelpAdapter(mActivity, list, false);
		for (int i = 0; i < list.size(); i++) {
			View view = adapter.getView(i, null, null);
			mContentLayout.addView(view);
		}

		tv = new TextView(mActivity);
		tv.setText(R.string.delivery_flow);
		tv.setPadding(20, 20, 0, 20);
		tv.setLayoutParams(lp);
		tv.setBackgroundColor(0xffcccccc);
		mContentLayout.addView(tv);
		list = new ArrayList<String>();
		list.add(getString(R.string.delivery_zone_time));
		list.add(getString(R.string.qian_shou));
		adapter = new HelpAdapter(mActivity, list, false);
		for (int i = 0; i < list.size(); i++) {
			View view = adapter.getView(i, null, null);
			mContentLayout.addView(view);
		}

		tv = new TextView(mActivity);
		tv.setText(R.string.saled_service);
		tv.setPadding(20, 20, 0, 20);
		tv.setTag("23");
		tv.setBackgroundColor(0xffcccccc);
		tv.setLayoutParams(lp);
		mContentLayout.addView(tv);
		list = new ArrayList<String>();
		list.add(getString(R.string.exchange_tip));
		adapter = new HelpAdapter(mActivity, list, false);
		for (int i = 0; i < list.size(); i++) {
			View view = adapter.getView(i, null, null);
			mContentLayout.addView(view);
		}
		for (int i = 0; i < mContentLayout.getChildCount(); i++) {
			mContentLayout.getChildAt(i).setOnClickListener(listener);
		}
	}
	
	private void fillUpView(){
		LinearLayout.LayoutParams lp = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < mNodesList.size(); i++) {
			TextView tv = new TextView(mActivity);
			tv.setText(mNodesList.get(i));
			tv.setPadding(20, 20, 0, 20);
			tv.setLayoutParams(lp);
			tv.setBackgroundColor(0xffcccccc);
			mContentLayout.addView(tv);
			List<JSONObject> listItem = map.get(mNodesList.get(i));
			List<String> list = new ArrayList<String>();
			for (int j = 0; j < listItem.size(); j++) {
				list.add(listItem.get(j).optString("title"));
			}
			HelpAdapter adapter = new HelpAdapter(mActivity, list, false);
			for (int k = 0; k < list.size(); k++) {
				View view = adapter.getView(k, null, null);
				view.setTag(listItem.get(k));
				mContentLayout.addView(view);
			}
		}
		for (int i = 0; i < mContentLayout.getChildCount(); i++) {
			mContentLayout.getChildAt(i).setOnClickListener(listener);
		}
	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			Object obj = view.getTag();
			if (obj != null) {
				JSONObject jsonObject = (JSONObject) obj;
				String title = jsonObject.optString("title");
				String id = jsonObject.optString("article_id");
				String c = jsonObject.optString("content");
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", title)
						.putExtra("article_id", id).putExtra(Run.EXTRA_DATA, c));
//				Toast.makeText(mActivity, jsonObject.optString("article_id"), Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private class GetData implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray nodeList = all.getJSONArray("data");
					for (int i = 0; i < nodeList.length(); i++) {
						JSONObject obj = nodeList.getJSONObject(i);
						JSONArray nodeItem = obj.getJSONArray("article");
						List<JSONObject> list = new ArrayList<JSONObject>();
						for (int j = 0; j < nodeItem.length(); j++) {
							list.add(nodeItem.getJSONObject(j));
						}
						String nodeName = obj.getString("node_name");
						map.put(nodeName, list);
						mNodesList.add(nodeName);
					}
					fillUpView();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showLoadingDialog();
//			JsonRequestBean req = new JsonRequestBean( "mobileapi.article.get_article_list");
//			req.addParams("ids", ids);
			JsonRequestBean req = new JsonRequestBean( "mobileapi.article.help_item");
			req.addParams("id", ids);
			return req;
		}
		
	}
}
