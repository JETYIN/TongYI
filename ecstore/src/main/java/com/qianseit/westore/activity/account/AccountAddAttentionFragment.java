package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.qianseit.westore.ui.HeaderGridView;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import com.tencent.android.tpush.horse.data.StrategyItem;

/**
 * 添加关注
 */
public class AccountAddAttentionFragment extends BaseDoFragment {
	private ListView mAttentionListView;
	private LayoutInflater mInflater;
	private BaseAdapter mUserItemAdapter;
	private VolleyImageLoader mVolleyImageLoader;
	private LoginedUser mLoginedUser;
	private ArrayList<JSONObject> mUserArray = new ArrayList<JSONObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_add_attention_title);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_add_attention_main, null);
		mInflater = inflater;
		View view = findViewById(R.id.account_add_attention_top);
		view.setLayoutParams(new AbsListView.LayoutParams(view.getLayoutParams()));
		mAttentionListView = (ListView)findViewById(R.id.attention_add_listView);
		findViewById(R.id.attention_add_search).setOnClickListener(this);
		if (Run.loadOptionBoolean(mActivity, AccountLoginFragment.LOGIN_SINA, false)) {
			findViewById(R.id.account_add_attention_web).setVisibility(View.VISIBLE);
			findViewById(R.id.account_add_attention_web).setOnClickListener(this);
		}
		findViewById(R.id.account_add_attention_book).setOnClickListener(this);
		Run.removeFromSuperView(view);
		mAttentionListView.removeHeaderView(view);
		mAttentionListView.addHeaderView(view);
		mUserItemAdapter = new UserItemAdapter(mActivity,mVolleyImageLoader,mUserArray,mLoginedUser.getMemberId(),false);
		mAttentionListView.setAdapter(mUserItemAdapter);
		Run.excuteJsonTask(new JsonTask(), new GetUserinforListTask());
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.attention_add_search:
			startActivity(AgentActivity.intentForFragment(mActivity,AgentActivity.FRAGMENT_ATTENTION_SEARCH));
			break;
		case R.id.account_add_attention_web:
			startActivity(AgentActivity.intentForFragment(mActivity,AgentActivity.FRAGMENT_ADD_WEBO_FRIENDS));
			break;
		case R.id.account_add_attention_book:
			startActivity(AgentActivity.intentForFragment(mActivity,AgentActivity.FRAGMENT_FRINENT));
			break;

		default:
			break;
		}
	}

	private class UserItemAdapter extends TwoPersalAdapter{
		

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

	private class GetUserinforListTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.get_members_for_doyen");
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray dataArray = all.optJSONArray("data");
					if (dataArray != null && dataArray.length() > 0) {
						for (int i = 0; i < dataArray.length(); i++)
							mUserArray.add(dataArray.optJSONObject(i));
						mUserItemAdapter.notifyDataSetInvalidated();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

	//取消关注
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
			try {
				hideLoadingDialog();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity,"已取消关注");
					if(!jsonObject.isNull("is_attention"))
					jsonObject.remove("is_attention");
					jsonObject.put("is_attention",String.valueOf(0));
					mUserItemAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 加关注
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
			try {
				hideLoadingDialog();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity,"关注成功");
					if(!jsonObject.isNull("is_attention"))
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
