package com.qianseit.westore.activity.account;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class MyScoreDetailFragment extends BaseDoFragment {
	
	private ListView mListView;
	private List<JSONObject> mDetailScoreList;
	private MyScoreDetail mAdapter;
	private int mPageNum = 1;
	private int mTotalPageNum;//总页数
	private boolean isLoadEnded;
	private boolean isLoading;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.myscore_recored);
		mDetailScoreList = new ArrayList<JSONObject>();
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_myscore_detail, null);
		mListView = (ListView) rootView.findViewById(R.id.fragment_myscore_detail_listview);
		mAdapter = new MyScoreDetail();
		mListView.setAdapter(mAdapter);
		Bundle b = getArguments();
		if (b != null) {
			String data =  b.getString(Run.EXTRA_DATA);
			parserData(data);
		}
		mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5 || isLoadEnded)
					return;
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5 && !isLoading){
					if (mPageNum < mTotalPageNum) {
						mPageNum += 1;
						new JsonTask().execute(new GetScoreListByPages());
					}
				}
			}
		});
	}
	
	private void parserData(String resp){
		try {
			JSONObject all = new JSONObject(resp);
			if (Run.checkRequestJson(mActivity, all)) {
				JSONObject data = all.optJSONObject("data");
				mTotalPageNum = data.optInt("page");
				JSONArray list = data.optJSONArray("historys");
				if (list != null && list.length() > 0) {
					for (int i = 0; i < list.length(); i++) {
						mDetailScoreList.add(list.getJSONObject(i));
					}
				}else {
					isLoadEnded = true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally{
			mAdapter.notifyDataSetChanged();
		}
	}
	
	private class GetScoreListByPages implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			isLoading = false;
			parserData(json_str);
		}

		@Override
		public JsonRequestBean task_request() {
			isLoading = true;
			JsonRequestBean req = new JsonRequestBean( "mobileapi.point.point_detail");
			req.addParams("n_page", String.valueOf(mPageNum));
			return req;
		}
		
	}
	
	private class MyScoreDetail extends BaseAdapter{

		@Override
		public int getCount() {
			if (mDetailScoreList != null) {
				return mDetailScoreList.size();
			}
			return 0;
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
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_myscore_detail, null);
			}
			JSONObject data = mDetailScoreList.get(position);
			((TextView) convertView.findViewById(R.id.item_myscore_detail_name)).setText(data.optString("reason"));
			String score = data.optString("change_point");
			int textColor = 0xffE66976;
			if (score.contains("-")) {
				textColor = 0xff004b99;
			}else{
				score = "+" + score;
			}
			TextView textView = (TextView) convertView.findViewById(R.id.item_myscore_detail_score);
			textView.setTextColor(textColor);
			textView.setText(score);
			String date = formatDate(Long.parseLong(data.optString("addtime")));
			((TextView) convertView.findViewById(R.id.item_myscore_detail_date)).setText(date);
			//((ImageView) convertView.findViewById(R.id.item_myscore_detail_img)).setText("");
			return convertView;
		}
		
	}
	
	@SuppressLint("SimpleDateFormat")
	private String formatDate(long milliseconds){
		milliseconds = milliseconds * 1000;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date =  new Date(milliseconds);
		String formatDate = sdf.format(date);
		return formatDate;
	}
	
}
