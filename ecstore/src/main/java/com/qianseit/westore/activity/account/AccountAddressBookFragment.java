package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class AccountAddressBookFragment extends BaseDoFragment {
	private final int REQUEST_CODE_EDIT_ADDRESS = 0x100;

	private ListView mlisListView;

	private ArrayList<JSONObject> mAddressList = new ArrayList<JSONObject>();
	private AddressAdapter mAdapter;
	private Dialog dialog ;
	private boolean isPickAddress;
   
	private boolean isSelectAddress=false;
	private int selectedIndex = -1;
	private View textView;
	
	private JSONObject oldAddress;
	public AccountAddressBookFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent data = mActivity.getIntent();
		isPickAddress = data.getBooleanExtra(Run.EXTRA_VALUE, false);
		isSelectAddress=data.getExtras().getBoolean(Run.EXTRA_SCAN_REZULT, false);
		String address=data.getStringExtra("old_address");
		if(address != null && !TextUtils.isEmpty(address)){
			try {
				oldAddress=new JSONObject(address);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
       
//		mActionBar.getBackButton().setOnClickListener(this);

		rootView = inflater.inflate(R.layout.fragment_my_address_book, null);
		textView = rootView.findViewById(R.id.account_add_address_text);
		textView.setVisibility(View.GONE);
		mlisListView = (ListView) findViewById(android.R.id.list);
		textView.setOnClickListener(this);
		mAdapter = new AddressAdapter();
		mlisListView.setAdapter(mAdapter);

		// 加载收获地址列表
		Run.excuteJsonTask(new JsonTask(), new MyAddressTask());
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.account_add_address_text) {
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ADDRESS_BOOK_EDITOR).putExtra("com.qianseit.westore.EXTRA_FILE_NAME",((TextView)v).getText().toString()),
					REQUEST_CODE_EDIT_ADDRESS);
		}
//		if (isPickAddress && v == mActionBar.getBackButton()) {
//			setResult();
//			getActivity().finish();
//		} else {
//			super.onClick(v);
//		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			setResult();
//		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onResume() {
		super.onResume();
		 if(isPickAddress){
			 mActionBar.setTitle(R.string.accout_select_address_book);
	        }
		 else{
			mActionBar.setTitle(R.string.accout_my_address_book);
		  }
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	private void setResult(JSONObject addressObj) {
//		JSONObject addressObj = null;
//		for (int i = 0; i < mAddressList.size(); i++) {
//			if (mAddressList.get(i).optInt("def_addr") == 1) {
//				addressObj = mAddressList.get(i);
//				break;
//			}
//		}
		if (addressObj != null) {
			Intent data = new Intent();
			data.putExtra(Run.EXTRA_DATA, addressObj.toString());
			mActivity.setResult(Activity.RESULT_OK, data);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_EDIT_ADDRESS
				&& resultCode == Activity.RESULT_OK) {
			Run.excuteJsonTask(new JsonTask(), new MyAddressTask());
		}
	}

	private class MyAddressTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean jsb = new JsonRequestBean( "mobileapi.member.receiver");
			return jsb;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			JSONObject all  =null;
			try {
				all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					mAddressList.clear();
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						for (int i = 0, c = child.length(); i < c; i++)
							mAddressList.add(child.getJSONObject(i));
						mAdapter.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
				try {
					JSONObject child = all.getJSONObject("data");
					if (child != null) {
							mAddressList.add(child);
						mAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} finally {
				textView.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public class DefaultAddressTask implements JsonTaskHandler {
		public final int DISABLE_DEFAULT = 1;
		public final int ENABLE_DEFAULT = 2;

		private String addrId;
		private int isDefault;

		public DefaultAddressTask(String addrId, int isDefault) {
			this.addrId = addrId;
			this.isDefault = isDefault;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.set_default")
					.addParams("addr_id", addrId).addParams("disabled",
							String.valueOf(isDefault));
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all))
					Run.excuteJsonTask(new JsonTask(), new MyAddressTask());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class AddressAdapter extends BaseAdapter implements OnClickListener {
		private LayoutInflater inflater;
		private Resources res;

		public AddressAdapter() {
			inflater = mActivity.getLayoutInflater();
			res = mActivity.getResources();
		}

		@Override
		public int getCount() {
			return mAddressList.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mAddressList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				int layout = R.layout.fragment_account_address_item;
				convertView = inflater.inflate(layout, null);
				convertView.setOnClickListener(this);
				convertView.findViewById(R.id.my_address_book_item_edit)
						.setOnClickListener(this);
				convertView.findViewById(R.id.my_address_book_item_default)
						.setOnClickListener(this);
			}

			JSONObject all = getItem(position);
			if (all == null)
				return convertView;

			((TextView) convertView
					.findViewById(R.id.my_address_book_item_name))
					.setText("收货人 ：" + all.optString("name"));
			String strPhone = all.optString("mobile");
//			String phone = strPhone.substring(0, 3);
			((TextView) convertView
					.findViewById(R.id.my_address_book_item_phone))
					.setText(strPhone);
			((TextView) convertView
					.findViewById(R.id.my_address_book_item_address))
					.setText(Run.buildString(
							"收货地址：" + all.optString("txt_area"),
							all.optString("addr")));

			convertView.setTag(all);
			convertView.findViewById(R.id.my_address_book_item_edit)
					.setTag(all);
			convertView.findViewById(R.id.my_address_book_item_default).setTag(
					all);
			String addr_id=all.optString("addr_id");
			if(oldAddress!=null){
				if(TextUtils.equals(addr_id, oldAddress.optString("addr_id"))){
					selectedIndex=position;
				}
			}
			// 是否为默认
//			boolean isDef = (all.optInt("def_addr") == 1);
			boolean isDef = position == selectedIndex;
			ImageView selectImage=((ImageView) convertView
					.findViewById(R.id.my_address_book_item_default));
			selectImage.setImageResource(isDef ? R.drawable.my_address_book_default
							: R.drawable.my_address_book_not_default);
			if(!isPickAddress){
				selectImage.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				final JSONObject all = (JSONObject) v.getTag();
				if (isPickAddress
						&& v.getId() == R.id.my_address_book_item_parent) {
					Intent data = new Intent();
					data.putExtra(Run.EXTRA_DATA, all.toString());
					mActivity.setResult(Activity.RESULT_OK, data);
					mActivity.finish();
				} else if (v.getId() == R.id.my_address_book_item_default) {
					// 设为默认收货地址
//					boolean isDef = (all.optInt("def_addr") == 1);
//					if (!isDef) {
//						int defStatus = DefaultAddressTask.ENABLE_DEFAULT;
//						Run.excuteJsonTask(new JsonTask(),
//								new DefaultAddressTask(
//										all.optString("addr_id"), defStatus));
//					}
					selectedIndex = mAddressList.indexOf(all);
					mAdapter.notifyDataSetChanged();
					setResult((JSONObject) v.getTag());
					getActivity().finish();
				} else if (v.getId() == R.id.my_address_book_item_edit) {
					startActivityForResult(
							AgentActivity.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_ADDRESS_BOOK_EDITOR)
									.putExtra(Run.EXTRA_DATA, all.toString()).putExtra(Run.EXTRA_VALUE, true),
							REQUEST_CODE_EDIT_ADDRESS);
				}
			}
		}
	}
}
