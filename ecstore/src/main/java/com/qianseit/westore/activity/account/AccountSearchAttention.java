package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.TwoPersalAdapter;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountSearchAttention extends BaseDoFragment {
	private ListView mSearchListView;
	private BaseAdapter mUserItemAdapter;
	private VolleyImageLoader mVolleyImageLoader;
	private LoginedUser mLoginedUser;
	private ArrayList<JSONObject> mUserArray = new ArrayList<JSONObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setShowTitleBar(false);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragent_search_attention_main,
				null);
		mSearchListView = (ListView) findViewById(R.id.attention_search_listview);
		mUserItemAdapter = new UserItemAdapter(mActivity, mVolleyImageLoader,
				mUserArray, mLoginedUser.getMemberId(), false);
		findViewById(R.id.attention_search_back).setOnClickListener(this);
		EditText editText = (EditText) findViewById(R.id.attention_search_edittext);
		mSearchListView.setAdapter(mUserItemAdapter);
		mUserItemAdapter.notifyDataSetChanged();
		editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				mUserArray.clear();
				mUserItemAdapter.notifyDataSetChanged();
				if (actionId == EditorInfo.IME_ACTION_SEND
						|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					String Value=v.getText().toString().trim();
					if(!TextUtils.isEmpty(Value)){
						findViewById(R.id.tv_empty_result).setVisibility(View.GONE);
						Run.excuteJsonTask(new JsonTask(), new SearchUserTaskTask(Value));
					}
					return true;
				}
				return false;
			}
		});
//		editText.addTextChangedListener(new TextWatcher() {
//			
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
////				mUserArray.clear();
////				Run.excuteJsonTask(new JsonTask(), new SearchUserTaskTask(String.valueOf(s)));		
//			}
//			
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void afterTextChanged(Editable s) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.attention_search_back:
			mActivity.finish();
			break;
		default:
			break;
		}
	}

	private class UserItemAdapter extends TwoPersalAdapter {

		public UserItemAdapter(Activity activity,
				VolleyImageLoader imageLoader, ArrayList<JSONObject> items,
				String userId, boolean isfans) {
			super(activity, imageLoader, items, userId, isfans);
			// TODO Auto-generated constructor stub
		}

		public void onClick(View v) {
			JSONObject dataJSON = (JSONObject) v.getTag();
			if (dataJSON != null) {
				switch (v.getId()) {
				case R.id.account_click_but:
					Run.excuteJsonTask(
							new JsonTask(),
							new CalcelAttentionTaskTask(dataJSON
									.optString("member_id"), mLoginedUser
									.getMemberId(),dataJSON));
					break;
				case R.id.account_attention_linear:
					Run.excuteJsonTask(
							new JsonTask(),
							new AddAttentionTask(dataJSON
									.optString("member_id"), mLoginedUser
									.getMemberId(),dataJSON));
					break;
				case R.id.attention_item_avd:
					if (dataJSON != null) {
						startActivity(AgentActivity
								.intentForFragment(mActivity,
										AgentActivity.FRAGMENT_PERSONAL_HOME)
								.putExtra("userId",
										dataJSON.optString("member_id")));
					}
				default:
					break;
				}
			}
		}

	}

	private class SearchUserTaskTask implements JsonTaskHandler {
		private String  condition;


		public SearchUserTaskTask(String  condition) {
			this.condition = condition;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.search_member");
			bean.addParams("key", condition);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
		   hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
                   JSONArray dataArray=all.optJSONArray("data");
                   if(dataArray!=null&&dataArray.length()>0){
                	   for(int i=0;i<dataArray.length();i++)
                		   mUserArray.add(dataArray.optJSONObject(i));
                   }else{
                	   findViewById(R.id.tv_empty_result).setVisibility(View.VISIBLE);
                   }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				mUserItemAdapter.notifyDataSetChanged(); 
			}
		}
	}
	private class CalcelAttentionTaskTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;
		private JSONObject jsonObject;

		public CalcelAttentionTaskTask(String meberId, String fansId,JSONObject jsonObject) {
			this.meberId = meberId;
			this.fansId = fansId;
			this.jsonObject=jsonObject;
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
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity,"已取消关注");
					jsonObject.remove("is_attention");
					jsonObject.put("is_attention",String.valueOf(0));
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
		private JSONObject jsonObject;

		public AddAttentionTask(String meberId, String fansId,JSONObject jsonObject) {
			this.meberId = meberId;
			this.fansId = fansId;
			this.jsonObject=jsonObject;
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
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity,"关注成功");
					jsonObject.remove("is_attention");
					jsonObject.put("is_attention",String.valueOf(1));
					mUserItemAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
