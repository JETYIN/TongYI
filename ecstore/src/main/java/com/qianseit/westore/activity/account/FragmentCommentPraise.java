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
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class FragmentCommentPraise extends BaseDoFragment {
	
	private PullToRefreshListView mListView;
	private DataAdapter mAdapter;
	private VolleyImageLoader mImageLoader;
	
	private ArrayList<JSONObject> listData = new ArrayList<JSONObject>();
	private boolean isCommentView;
	private JsonTask mTask;
	private int mPageNum;
	private boolean isLoadEnd;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public FragmentCommentPraise(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageLoader = AgentApplication.getApp(mActivity).getImageLoader();
		Bundle bundle = getArguments();
		if (bundle != null) {
			if(bundle.getBoolean(Run.EXTRA_DATA , false)){
				mActionBar.setTitle("收到的评论");
				isCommentView = true;
			} else {
				mActionBar.setTitle("收到的赞");
			}
		}
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_praise_comment, null);
		mListView = (PullToRefreshListView) rootView.findViewById(R.id.priase_comment_listview);
		mAdapter = new DataAdapter();
		mListView.getRefreshableView().setAdapter(mAdapter);
		mListView.getRefreshableView().setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 20 || isLoadEnd)
					return;
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum);
			}
		});
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		showCancelableLoadingDialog();
		Run.excuteJsonTask(new JsonTask(), new GetComOrPraiseData(true));
//		loadNextPage(0);
	}
	
	private void loadNextPage(int pageNum){
		mPageNum = pageNum + 1 ;
		if (mPageNum == 1) {
			listData.clear();
		}
		if (mTask != null && mTask.isExcuting || isLoadEnd) {
			return ;
		}
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetComOrPraiseData(false));
	}
	
	private class GetComOrPraiseData implements JsonTaskHandler{

		private boolean isFirst;
		public GetComOrPraiseData(boolean isFirst){
			this.isFirst=isFirst;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				if(!isFirst){
					mListView.onRefreshComplete();
				}
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray list = all.optJSONArray("data");
					int count = list == null ? 0 : list.length();
					if (count < 20) {
						isLoadEnd = true;
					}
					for (int i = 0 ; i < count; i++) {
						listData.add(list.getJSONObject(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				mAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req;
			if (isCommentView) {
				req = new JsonRequestBean( "mobileapi.goods.get_comment_for_member");
			} else {
				req = new JsonRequestBean( "mobileapi.goods.get_praise_for_member");
			}
			req.addParams("member_id", AgentApplication.getLoginedUser(mActivity).getMemberId());
			req.addParams("page", String.valueOf(mPageNum));
			req.addParams("limit", "20");
			return req;
		}
		
	}
	
	
	private class DataAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return listData.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return listData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_receive_praise_comment, null);
				holder.userAvatar = (ImageView) convertView.findViewById(R.id.item_receive_pc_title_img);
				holder.typeImage = (ImageView) convertView.findViewById(R.id.item_receive_pc_user_img);
				holder.praiseImg = (ImageView) convertView.findViewById(R.id.item_receive_pc_praise);
				holder.username = (TextView) convertView.findViewById(R.id.item_receive_pc_username);
				holder.comment = (TextView) convertView.findViewById(R.id.item_receive_pc_comment);
				holder.date = (TextView) convertView.findViewById(R.id.item_receive_pc_date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			JSONObject obj = getItem(position);
			if (obj == null) {
				return convertView;
			}
			mImageLoader.showImage(holder.userAvatar, obj.optString("avatar"));
			mImageLoader.showImage(holder.typeImage, obj.optString("image"));
			holder.username.setText(obj.optString("name"));
			holder.date.setText(getFormatTime(obj.optLong("created")));
			if (isCommentView) {
				holder.comment.setVisibility(View.VISIBLE);
				holder.comment.setText(obj.optString("content"));
			} else {
				holder.praiseImg.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
		
	}
	
	public static String getFormatTime(long time){
		long oldTime = time;
		time = System.currentTimeMillis() / 1000 - time;
		if (time < 60) {
			if (time == 0) {
				return Run.buildString(time , "1秒前");
			}
			return Run.buildString(time , "秒前");
		} else if(time < 3600){
			return Run.buildString(time / 60 , "分钟前");
		} else if(time < 86400){
			return Run.buildString(time / 3600 , "小时前");
		} else if(time < 86400 * 2){
			return "昨天";
		}  else if(time < 86400 * 3){
			return "前天";
		} else {
			return sdf.format(new Date(oldTime * 1000));
		}
	}
	
	private static class ViewHolder{
		public ImageView userAvatar;
		public ImageView typeImage;
		public ImageView praiseImg;
		public TextView username;
		public TextView comment;
		public TextView date;
	}
}
