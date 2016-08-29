package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class FragmentAddSinaFriends extends BaseDoFragment {
	
	private ListView mListView;
	
	private LayoutInflater mInflater;
	private BaseAdapter mUserItemAdapter;
	private VolleyImageLoader mVolleyImageLoader;
	private LoginedUser mLoginedUser;
	private ArrayList<JSONObject> mUserArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mWeboUserArray = new ArrayList<JSONObject>();

	public FragmentAddSinaFriends() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("微博好友");
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		mInflater = inflater;
		mListView = new ListView(mActivity);
		mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mListView.setDivider(null);
		rootView = mListView;
		mUserItemAdapter = new UserItemAdapter();
		mListView.setAdapter(mUserItemAdapter);
		String dd = Run.loadOptionString(mActivity, AccountLoginFragment.LOGIN_SINA_DATA , "");
		if (!TextUtils.isEmpty(dd)) {
			String[] ut = dd.split("&");
			new JsonTask().execute(new GetWeboFriends(ut[0], ut[1]));
		}
	}
	
	private class UserItemAdapter extends BaseAdapter implements OnClickListener{

		private final int[] ITEM_IDS = { R.id.account_personal_list_item_one,
				R.id.account_personal_list_item_two };
		
		@Override
		public int getCount() {
			return (int) Math.ceil(mUserArray.size() / 2.0);
		}

		@Override
		public JSONObject getItem(int position) {
			return position >= mUserArray.size() ? null : mUserArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.fragment_personal_list_item, null);
				for (int i = 0, c = ITEM_IDS.length; i < c; i++){
					View childView = convertView.findViewById(ITEM_IDS[i]);
					childView.findViewById(R.id.account_click_but).setOnClickListener(this);
					childView.setOnClickListener(this);
				}
			}
			for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
				JSONObject all = getItem(position * c + i);
				View childView = convertView.findViewById(ITEM_IDS[i]);
				childView.findViewById(R.id.account_user_linear).setVisibility(View.GONE);
				if (all != null) {
					((TextView)childView.findViewById(R.id.account_user_name)).setText(all.optString("name"));
					if (all.optInt("is_attention") == 1) {
						((TextView)childView.findViewById(R.id.account_click_but)).setText("已关注");
					} else {
						((TextView)childView.findViewById(R.id.account_click_but)).setText("+关注");
					}
					childView.findViewById(R.id.account_click_but).setTag(all);
					mVolleyImageLoader.showImage((ImageView) childView.findViewById(R.id.attention_item_avd), all.optString("avatar"));
					childView.setTag(all);
					childView.setVisibility(View.VISIBLE);
				} else {
					childView.setVisibility(View.INVISIBLE);
				}
			}
			return convertView;
		}
		
		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				JSONObject obj = (JSONObject) v.getTag();
				if (v.getId() == R.id.account_click_but) {
					if (obj.optInt("is_attention") == 1) {//已关注
//						Run.excuteJsonTask(
//								new JsonTask(),
//								new CalcelAttentionTaskTask(obj
//										.optString("member_id"), mLoginedUser
//										.getMemberId()));
						Run.excuteJsonTask(
								new JsonTask(),
								new CalcelAttentionTaskTask(obj));
					} else {//未关注
//						Run.excuteJsonTask(
//								new JsonTask(),
//								new AddAttentionTask(obj
//										.optString("member_id"), mLoginedUser
//										.getMemberId()));
						Run.excuteJsonTask(
								new JsonTask(),
								new AddAttentionTask(obj));
					}
				} else {//调到用户中心
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra("userId",
									obj.optString("member_id")));
				}
			}
		}
		
	}
	
	private class GetWeboFriends implements JsonTaskHandler{

		private String UID;
		private String token;
		
		public GetWeboFriends(String uid, String token){
			this.UID = uid;
			this.token = token;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (all != null) {
					JSONArray list = all.optJSONArray("users");
					int count = list == null ? 0 : list.length();
					JSONObject temp ;
					for (int i = 0; i < count; i++) {
						temp = new JSONObject();
						temp.put("uid", list.optJSONObject(i).optString("id"));
						temp.put("name", list.optJSONObject(i).optString("screen_name"));
						mWeboUserArray.add(temp);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				if (mWeboUserArray.size() > 0) {
					new JsonTask().execute(new ServerWeboFriends());
				}
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean("https://api.weibo.com/2/friendships/friends.json?");
			req.addParams("uid", UID);
			req.addParams("count", "200");
			req.addParams("access_token", token);
			req.method = JsonRequestBean.METHOD_GET;
			return req;
		}
		
	}
	
	private class ServerWeboFriends implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						JSONArray regList = data.optJSONArray("reg");
						if (regList != null && regList.length() > 0) {
							for (int i = 0; i < regList.length(); i++) {
								mUserArray.add(regList.optJSONObject(i));
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				mUserItemAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.member.weibo_list").addParams("weibos", mWeboUserArray.toString());
//			String ss = "[{\"uid\":\"3539536454\",\"name\":\"张三\"},{\"uid\":\"5643769058\",\"name\":\"樱桃社\"}]";
//			return new JsonRequestBean( "mobileapi.member.weibo_list").addParams("weibos", ss);
		}
		
	}
	
	private class CalcelAttentionTaskTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;
		private JSONObject temp ;

		public CalcelAttentionTaskTask(String meberId, String fansId) {
			this.meberId = meberId;
			this.fansId = fansId;
		}
		
		public CalcelAttentionTaskTask(JSONObject all){
			this.temp = all;
			meberId = temp.optString("member_id");
			fansId = mLoginedUser.getMemberId();
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.un_attention");
			bean.addParams("member_id", meberId);
			bean.addParams("fans_id", fansId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					int index = mUserArray.indexOf(temp);
					temp.put("is_attention", 0);
					mUserArray.set(index, temp);
					mUserItemAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class AddAttentionTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;
		private JSONObject temp ;

		public AddAttentionTask(String meberId, String fansId) {
			this.meberId = meberId;
			this.fansId = fansId;
		}
		
		public AddAttentionTask(JSONObject all){
			this.temp = all;
			meberId = this.temp.optString("member_id");
			fansId = mLoginedUser.getMemberId();
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.attention");
			bean.addParams("member_id", meberId);
			bean.addParams("fans_id", fansId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					int index = mUserArray.indexOf(temp);
					temp.put("is_attention", 1);
					mUserArray.set(index, temp);
					mUserItemAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
