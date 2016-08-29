package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;

public class MyAddressBookFragment extends BaseDoFragment {
	private final int REQUEST_CODE_EDIT_ADDRESS = 0x100;

	private ListView mlisListView;

	private ArrayList<JSONObject> mAddressList = new ArrayList<JSONObject>();
	private AddressAdapter mAdapter;

	private boolean isPickAddress;

	public MyAddressBookFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent data = mActivity.getIntent();
		isPickAddress = data.getBooleanExtra(Run.EXTRA_VALUE, false);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setRightTitleButton(R.string.add, this);
		mActionBar.setTitle(R.string.my_address_book);
		mActionBar.getBackButton().setOnClickListener(this);

		rootView = inflater.inflate(R.layout.fragment_my_address_book, null);
		mlisListView = (ListView) findViewById(android.R.id.list);
		mAdapter = new AddressAdapter();
		mlisListView.setAdapter(mAdapter);

		// 加载收获地址列表
		Run.excuteJsonTask(new JsonTask(), new MyAddressTask());
	}

	@Override
	public void onClick(View v) {
		if (v == mActionBar.getRightButton()) {
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ADDRESS_BOOK_EDITOR),
					REQUEST_CODE_EDIT_ADDRESS);
		} if(isPickAddress && v == mActionBar.getBackButton()){
			setResult();
			getActivity().finish();
		} else {
			super.onClick(v);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setResult(){
		JSONObject addressObj = null;
		for (int i = 0; i < mAddressList.size(); i++) {
			if (mAddressList.get(i).optInt("def_addr") == 1) {
				addressObj = mAddressList.get(i);
				break;
			}
		}
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
			return new JsonRequestBean( "mobileapi.member.receiver");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
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
				e.printStackTrace();
			}
		}
	}

	private class DelAddressTask implements JsonTaskHandler {
		private String addrId;

		public DelAddressTask(String addrId) {
			this.addrId = addrId;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.member.del_rec")
					.addParams("addr_id", addrId);
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					for (JSONObject data : mAddressList) {
						if (TextUtils.equals(data.optString("addr_id"), addrId)) {
							mAddressList.remove(data);
							mAdapter.notifyDataSetChanged();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public final int DISABLE_DEFAULT = 1;
	public  final int ENABLE_DEFAULT = 2;
	public class DefaultAddressTask implements JsonTaskHandler {

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
				int layout = R.layout.fragment_my_address_book_item;
				convertView = inflater.inflate(layout, null);
				convertView.setOnClickListener(this);

				convertView.findViewById(R.id.my_address_book_item_delete)
						.setOnClickListener(this);
				convertView.findViewById(R.id.my_address_book_item_edit)
						.setOnClickListener(this);
				convertView.findViewById(R.id.my_address_book_item_default)
						.setOnClickListener(this);
			}

			JSONObject all = getItem(position);
			if (all == null)
				return convertView;

			((TextView) convertView
					.findViewById(R.id.my_address_book_item_name)).setText(all
					.optString("name"));
			((TextView) convertView
					.findViewById(R.id.my_address_book_item_phone)).setText(all
					.optString("mobile"));
			((TextView) convertView
					.findViewById(R.id.my_address_book_item_address))
					.setText(Run.buildString(all.optString("txt_area"), "\n",
							all.optString("addr")));

			convertView.setTag(all);
			convertView.findViewById(R.id.my_address_book_item_delete).setTag(
					all);
			convertView.findViewById(R.id.my_address_book_item_edit)
					.setTag(all);
			convertView.findViewById(R.id.my_address_book_item_default).setTag(
					all);

			// 是否为默认
			boolean isDef = (all.optInt("def_addr") == 1);
			((TextView) convertView
					.findViewById(R.id.my_address_book_item_default))
					.setCompoundDrawablesWithIntrinsicBounds(
							isDef ? R.drawable.my_address_book_default
									: R.drawable.my_address_book_not_default,
							0, 0, 0);

			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				final JSONObject all = (JSONObject) v.getTag();
				if (v.getId() == R.id.my_address_book_item_delete) {
					CustomDialog dialog = new CustomDialog(mActivity);
					dialog.setMessage(mActivity.getString(
							R.string.confirm_delete_address,
							all.optString("name")));
					dialog.setNegativeButton(R.string.cancel, null);
					dialog.setPositiveButton(R.string.ok,
							new OnClickListener() {
								@Override
								public void onClick(View v) {
									// 删除收货地址
									String addrId = all.optString("addr_id");
									Run.excuteJsonTask(new JsonTask(),
											new DelAddressTask(addrId));
								}
							}).setCancelable(true).show();
				} else if (isPickAddress
						&& v.getId() == R.id.my_address_book_item_parent) {
					Intent data = new Intent();
					data.putExtra(Run.EXTRA_DATA, all.toString());
					mActivity.setResult(Activity.RESULT_OK, data);
					mActivity.finish();
				} else if (v.getId() == R.id.my_address_book_item_default) {
					// 设为默认收货地址
					boolean isDef = (all.optInt("def_addr") == 1);
					if (!isDef) {
						int defStatus = ENABLE_DEFAULT;
						Run.excuteJsonTask(new JsonTask(),
								new DefaultAddressTask(
										all.optString("addr_id"), defStatus));
					}
				} else if (v.getId() == R.id.my_address_book_item_edit) {
					startActivityForResult(
							AgentActivity.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_ADDRESS_BOOK_EDITOR)
									.putExtra(Run.EXTRA_DATA, all.toString()),
							REQUEST_CODE_EDIT_ADDRESS);
				}
			}
		}
	}
}
