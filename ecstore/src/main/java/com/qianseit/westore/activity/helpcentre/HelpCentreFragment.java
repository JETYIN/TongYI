package com.qianseit.westore.activity.helpcentre;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class HelpCentreFragment extends BaseDoFragment {

	private ListView mListView;
	private List<String> nameList;
	private List<JSONObject> jsonList;
	String id = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		if (b != null) {
			id = b.getString(Run.EXTRA_DATA);
		}
		if (TextUtils.isEmpty(id)) {
			mActionBar.setTitle(R.string.me_item_help);
		} else {
			mActionBar.setTitle(b.getString("title"));
		}
		nameList = new ArrayList<String>();
		jsonList = new ArrayList<JSONObject>();
//		nameList.add(getString(R.string.using_help));
//		nameList.add(getString(R.string.self_twilling_help));
//		nameList.add(getString(R.string.about));
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_help_centre, null);
		mListView = (ListView) rootView
				.findViewById(R.id.fragment_helpcentre_listview);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					JSONObject obj = jsonList.get(position);
					String type = obj.optString("type");
					if (type.equals("node")) {
						startActivity(AgentActivity.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_HELP_CENTRE).putExtra("title", obj.optString("title"))
								.putExtra(Run.EXTRA_DATA, obj.optString("node_id")));
					}else if (type.equals("article")){
						startActivity(AgentActivity.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", obj.optString("title"))
								.putExtra(Run.EXTRA_DATA, obj.optString("content"))
								.putExtra("article_id", obj.optString("ids")));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
//				if (position == 0) {
//					startActivity(AgentActivity.intentForFragment(mActivity,
//							AgentActivity.FRAGMENT_HELP_USING).putExtra("title", "使用帮助")
//							.putExtra("ids", "27,28,29"));
//				} else if (position == 1) {
//					startActivity(AgentActivity.intentForFragment(mActivity,
//							AgentActivity.FRAGMENT_HELP_ZITI));
//				} 
			}
		});
		
		new JsonTask().execute(new GetHelpCentre());
	}
	
	private class GetHelpCentre implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			try {
				JSONObject obj = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, obj)) {
					JSONArray list = obj.getJSONArray("data");
					for (int i = 0; i < list.length(); i++) {
						jsonList.add(list.getJSONObject(i));
						nameList.add(list.getJSONObject(i).optString("title"));
					}
					mListView.setAdapter(new HelpAdapter(mActivity, nameList));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public JsonRequestBean task_request() {
			showLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.article.help_item");
			if (!TextUtils.isEmpty(id)){
				req.addParams("id", id);
			}
			return req;
		}
		
	}
	
}
