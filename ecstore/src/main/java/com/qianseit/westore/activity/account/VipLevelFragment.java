package com.qianseit.westore.activity.account;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.LevelView;
import cn.shopex.ecstore.R;

public class VipLevelFragment extends BaseDoFragment {
	
	private ListView mListView;
	private ImageView mImg;
	private LevelView mLevelView;

	public VipLevelFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("会员等级");
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.fragment_vip_level, null);
		mListView = (ListView) this.findViewById(R.id.fragment_vip_listview);
		mImg = (ImageView) findViewById(R.id.fragment_vip_avatar);
		mLevelView = (LevelView) findViewById(R.id.fragment_vip_levelView);
		new JsonTask().execute(new GetLevelsTask());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		LoginedUser mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		if (mLoginedUser != null) {
			updateAvatarView(mImg, mLoginedUser);
		}
	}
	
	private void updateData(JSONObject data){
		if (data != null) {
			((TextView)findViewById(R.id.fragment_vip_name)).setText(data.optString("uname"));
			((TextView)findViewById(R.id.fragment_vip_level)).setText(data.optString("levelname"));
			((TextView)findViewById(R.id.fragment_vip_next_lv)).setText(data.optString("next_lv"));
			JSONArray levels = data.optJSONArray("level_list");
			if (levels != null && levels.length() > 0) {
				mLevelView.setData(data.optInt("experience"), levels.optJSONObject(levels.length() - 1).optInt("point"), data.optString("levelname"));
				mListView.setAdapter(new LevelsAdapter(levels));
			}
		}
	}
	
	private class LevelsAdapter extends BaseAdapter{

		private JSONArray listLevels;
		
		public LevelsAdapter(JSONArray listLevels){
			this.listLevels = listLevels;
		}
		
		@Override
		public int getCount() {
			if (listLevels != null) {
				return listLevels.length();
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
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.item_vip_view, null);
			}
			JSONObject obj = listLevels.optJSONObject(position);
			((TextView)convertView.findViewById(R.id.item_vip_name)).setText(obj.optString("name"));
			((TextView)convertView.findViewById(R.id.item_vip_range)).setText(obj.optString("range"));
			return convertView;
		}
		
	}
	
	private class GetLevelsTask implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					updateData(data);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.member.levels_info");
		}
		
	}
	
}
