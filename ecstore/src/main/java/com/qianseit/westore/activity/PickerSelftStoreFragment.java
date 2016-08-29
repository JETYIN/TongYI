package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class PickerSelftStoreFragment extends BaseDoFragment implements OnItemClickListener{

	private ListView mListView;
	private JSONArray listArea = new JSONArray();
	private JSONArray listStores = new JSONArray();
	
	public PickerSelftStoreFragment() {
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_picker_selfstore, null);
		mActionBar.setTitle("区域");
		mActionBar.getBackButton().setOnClickListener(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setOnItemClickListener(this);
		new JsonTask().execute(new GetRegionsTask());
	}
	
	@Override
	public void onClick(View v) {
		if (v == mActionBar.getBackButton()) {
			if (mListView.getAdapter() != null && mListView.getAdapter() instanceof AddressAdapter) {
				mActionBar.setTitle("区域");
				setAreaData();
				return;
			}
		}
		super.onClick(v);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mListView.getAdapter() != null && mListView.getAdapter() instanceof AddressAdapter) {
				mActionBar.setTitle("区域");
				setAreaData();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mListView.getAdapter() instanceof AreaAdapter) {
			mActionBar.setTitle(listArea.optString(position).split(":")[0]);
			String areaId = listArea.optString(position).split(":")[1];
			new JsonTask().execute(new GetStoresList(areaId));
		} else if(mListView.getAdapter() instanceof AddressAdapter){
			Intent intent = new Intent();
			intent.putExtra(Run.EXTRA_DATA, listStores.optJSONObject(position).toString());
			mActivity.setResult(AgentActivity.RESULT_OK, intent);
			getActivity().finish();
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mListView = null;
	}
	
	private class GetRegionsTask implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						listArea = child.optJSONArray(2).optJSONArray(0);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				hideLoadingDialog_mt();
				setAreaData();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.get_regions");
		}
		
	}
	
	private class GetStoresList implements JsonTaskHandler{

		private String areaId;
		
		public GetStoresList(String areaId){
			this.areaId = areaId;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					listStores = all.optJSONArray("data");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				hideLoadingDialog_mt();
				if (listStores.length() > 0) {
					setStoresData();
				} else{
					Run.alert(mActivity, "该区域暂无门店");
				}
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.get_stores_list");
			req.addParams("area_id", areaId);
			return req;
		}
		
	}
	
	private void setAreaData(){
		if (listArea == null) {
			return ;
		}
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < listArea.length(); i++) {
			list.add(listArea.optString(i).split(":")[0]);
		}
		AreaAdapter mAdapter = new AreaAdapter();
		mListView.setAdapter(mAdapter);
	}
	
	private void setStoresData(){
		if (listStores == null) {
			return ;
		}
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < listStores.length(); i++) {
			list.add(listStores.optJSONObject(i).optString("name"));
		}
		AddressAdapter mAdapter = new AddressAdapter();
		mListView.setAdapter(mAdapter);
	}
	
	private class AreaAdapter extends BaseAdapter{
		
		public AreaAdapter(){}
		
		@Override
		public int getCount() {
			return listArea.length();
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
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.simple_list_item1, null);
				((TextView) convertView.findViewById(android.R.id.text1))
						.setTextSize(18);
			}
			((TextView) convertView.findViewById(android.R.id.text1)).setText(listArea.optString(position).split(":")[0]);
			return convertView;
		}
		
	}
	
	private class AddressAdapter extends BaseAdapter{
		
		public AddressAdapter(){}
		
		@Override
		public int getCount() {
			return listStores.length();
		}

		@Override
		public Object getItem(int position) {
			return listStores.optJSONObject(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.view_name_address, null);
			}
			JSONObject obj = listStores.optJSONObject(position);
			((TextView)convertView.findViewById(R.id.express_picker_shop_name)).setText(obj.optString("name"));
			((TextView)convertView.findViewById(R.id.express_picker_shop_addr)).setText(obj.optString("address"));
			return convertView;
		}
		
	}
	
}
