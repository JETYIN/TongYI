package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;

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
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountAttentionFragment extends BaseDoFragment {
	private PullToRefreshListView mAttentionListView;
	private LayoutInflater mInflater;
	private BaseAdapter mUserItemAdapter;
	private ArrayList<JSONObject> mUserArray = new ArrayList<JSONObject>();
	private LoginedUser mLoginedUser;
	private String strId;
	private int mPageNum;
	private JsonTask mTask;
	private String mUserId;
	private VolleyImageLoader mVolleyImageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mUserId = mLoginedUser.getMemberId();
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		Intent mIntent = mActivity.getIntent();
		strId = mIntent.getStringExtra("userId");

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (strId.equals(mLoginedUser.getMemberId())) {
			mActionBar.setTitle("我的关注");
			mActionBar.setRightImageButton(R.drawable.account_person_attention,
					new OnClickListener() {

						@Override
						public void onClick(View v) {

							startActivity(AgentActivity.intentForFragment(
									mActivity,
									AgentActivity.FRAGMENT_ADD_ATTENTION));
						}
					});
		} else {
			mActionBar.setTitle("TA的关注");
		}

		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_attention_main, null);
		mAttentionListView = (PullToRefreshListView) findViewById(R.id.personal_listview);
		mUserItemAdapter = new UserItemAdapter(mActivity, mVolleyImageLoader,
				mUserArray);
		mAttentionListView.getRefreshableView().setAdapter(mUserItemAdapter);
		mAttentionListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum, false);
			}
		});
		mAttentionListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0, false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		loadNextPage(mPageNum, true);
	}

	private void loadNextPage(int oldPageNum, boolean isShow) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mUserArray.clear();
			mUserItemAdapter.notifyDataSetChanged();
			if (!isShow)
				mAttentionListView.setRefreshing();
		} else {
			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;
		}
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetUserinforListTask(isShow));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	private class UserItemAdapter extends TwoPersalAdapter {

		public UserItemAdapter(Activity activity,
				VolleyImageLoader imageLoader, ArrayList<JSONObject> items) {
			super(activity, imageLoader, items, mUserId, false);
		}

		@Override
		public void onClick(View v) {
			JSONObject dataJSON = (JSONObject) v.getTag();
			if (dataJSON != null) {
				switch (v.getId()) {
				case R.id.account_click_but:
					showCancelableLoadingDialog();
					Run.excuteJsonTask(
							new JsonTask(),
							new CalcelAttentionTaskTask(dataJSON
									.optString("member_id"), mUserId, dataJSON));
					break;
				case R.id.account_attention_linear:
					showCancelableLoadingDialog();
					Run.excuteJsonTask(new JsonTask(), new AddAttentionTask(
							dataJSON.optString("member_id"), mUserId, dataJSON));
					break;
				case R.id.attention_item_avd:
					if (dataJSON != null) {
						startActivity(AgentActivity
								.intentForFragment(mActivity,
										AgentActivity.FRAGMENT_PERSONAL_HOME)
								.putExtra("userId",
										dataJSON.optString("member_id")));
					}
					break;
				default:
					break;
				}

			}

		}

	}

	private class GetUserinforListTask implements JsonTaskHandler {
		private boolean isShow;

		GetUserinforListTask(boolean isShow) {
			this.isShow = isShow;
		}

		@Override
		public JsonRequestBean task_request() {
			if (isShow)
				showCancelableLoadingDialog();

			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.get_attention");
			bean.addParams("member_id", strId);
			bean.addParams("limit", "20");
			bean.addParams("page", String.valueOf(mPageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				mAttentionListView.onRefreshComplete();
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

	private class CalcelAttentionTaskTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;
		private JSONObject jsonObject;

		public CalcelAttentionTaskTask(String meberId, String fansId,
				JSONObject jsonObject) {
			this.meberId = meberId;
			this.fansId = fansId;
			this.jsonObject = jsonObject;
		}

		@Override
		public JsonRequestBean task_request() {
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
					Run.alert(mActivity, "已取消关注");
					if (!jsonObject.isNull("is_attention"))
						jsonObject.remove("is_attention");
					jsonObject.put("is_attention", String.valueOf(0));
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

		public AddAttentionTask(String meberId, String fansId,
				JSONObject jsonObject) {
			this.meberId = meberId;
			this.fansId = fansId;
			this.jsonObject = jsonObject;
		}

		@Override
		public JsonRequestBean task_request() {
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
					Run.alert(mActivity, "关注成功");
					if (!jsonObject.isNull("is_attention"))
						jsonObject.remove("is_attention");
					jsonObject.put("is_attention", String.valueOf(1));
					mUserItemAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
