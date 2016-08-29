package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;

public class FragmentSystemMessage extends BaseDoFragment {
	
	private PullToRefreshListView mListView;
	private ArrayList<JSONObject> listMsg = new ArrayList<JSONObject>();
	private SystemMessageAdapter messageAdapter;
	
	private JsonTask mTask;
	private int mPageNum;
	private boolean isLoadEnd;
	
	public FragmentSystemMessage() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("系统消息");
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_praise_comment, null);
		mListView = (PullToRefreshListView) rootView.findViewById(R.id.priase_comment_listview);
		messageAdapter = new SystemMessageAdapter();
//		mListView.setAdapter(messageAdapter);
		mListView.getRefreshableView().setAdapter(messageAdapter);
		mListView.getRefreshableView().setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (isLoadEnd || totalItemCount <= 5 )
					return;
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum);
			}
		});
//		loadNextPage(0);
		new JsonTask().execute(new LoadMessageTask());
	}
	
	private void loadNextPage(int pageNum){
		mPageNum = pageNum + 1 ;
		if (mPageNum == 1) {
			listMsg.clear();
		}
		if (mTask == null) {
			mTask = new JsonTask();
		}
		if (mTask.isExcuting || isLoadEnd) {
			return ;
		}
		Run.excuteJsonTask(mTask, new LoadMessageTask());
	}
	
	/**
	 * 获取系统消息
	 * @author chanson 每次拿10条记录
	 * @CreatTime 2015-8-2 下午4:54:53
	 *
	 */
	private class LoadMessageTask implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray list = all.optJSONArray("data");
					int count = list == null ? 0 : list.length();
					if (count < 10) {
						isLoadEnd = true;
					}
					for (int i = 0; i < count; i++) {
						listMsg.add(list.optJSONObject(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				messageAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.mymessage.mysysmsg");
//			req.addParams("offset", String.valueOf(mPageNum));
			req.addParams("limit", "100");
			return req;
		}
		
	}
	
	/**
	 * 把系统信息标注为已读
	 * @author chanson
	 * @CreatTime 2015-8-5 下午5:32:21
	 *
	 */
	private class SetMsgReaded implements JsonTaskHandler{

		private String msgID;
		
		public SetMsgReaded(String msgID){
			this.msgID = msgID;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.mymessage.toread");
			return req.addParams("message_id", msgID);
		}
		
	}
	
	private class SystemMessageAdapter extends BaseAdapter implements OnClickListener{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		@Override
		public int getCount() {
			return listMsg.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_system_message, null);
//				convertView.setOnClickListener(this);
			}
			JSONObject obj = listMsg.get(position);
			if (obj != null) {
				((TextView)convertView.findViewById(R.id.item_system_date)).setText(sdf.format(new Date(obj.optLong("time") * 1000)));
				((TextView)convertView.findViewById(R.id.item_system_desc)).setText(obj.optString("detail"));
				if (obj.optInt("is_read") == 0) {
//					((TextView)convertView.findViewById(R.id.item_system_new)).setVisibility(View.VISIBLE);
				} else {
//					((TextView)convertView.findViewById(R.id.item_system_new)).setVisibility(View.GONE);
				}
			}
			convertView.setTag(obj);
			return convertView;
		}
		
		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				JSONObject json = (JSONObject) v.getTag();
				try {
					json.put("is_read", 1);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				messageAdapter.notifyDataSetChanged();
				new JsonTask().execute(new SetMsgReaded(json.optString("message_id")));
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", "系统消息")
						.putExtra("article_id", json.optString("message_id")));
			}
		}
		
	}

}
