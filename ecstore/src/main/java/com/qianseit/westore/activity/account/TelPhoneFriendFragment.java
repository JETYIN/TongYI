package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class TelPhoneFriendFragment extends BaseDoFragment {

	private ListView mListView;
	private ListView mListView2;
	private RadioButton mInvitedFriends, mNeedInviteFrinds;
	private NoRegisterAdapter mNRegAdapter;
	private HadInviteAdapter mRegAdapter;
	private JSONArray hadInvitedList;
	private JSONArray needInvitedList;
	private LayoutInflater mInflater;
	private JSONObject indexObject;
	private boolean isInvite;
	private ArrayList<JSONObject> listPosition = new ArrayList<JSONObject>();

	public TelPhoneFriendFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("手机通讯录好友");
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_friend_invition, null);
		mListView = (ListView) rootView
				.findViewById(R.id.fragment_friend_invite_list);
		mListView2 = (ListView) rootView
				.findViewById(R.id.fragment_friend_invite_list2);
		mInvitedFriends = (RadioButton) rootView
				.findViewById(R.id.fragment_friend_invited);
		mNeedInviteFrinds = (RadioButton) rootView
				.findViewById(R.id.fragment_friend_invite);
		mInvitedFriends.setChecked(true);
		mInvitedFriends.setOnCheckedChangeListener(changeListener);
		mNeedInviteFrinds.setOnCheckedChangeListener(changeListener);
		mRegAdapter = new HadInviteAdapter();
		mNRegAdapter = new NoRegisterAdapter();
        mListView.setAdapter(mRegAdapter);
        mListView2.setAdapter(mNRegAdapter);
        mListView2.setVisibility(View.GONE);
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300, true, true, true))
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				mActivity).defaultDisplayImageOptions(defaultOptions)
				.memoryCache(new WeakMemoryCache());
		ImageLoaderConfiguration config = builder.build();
		ImageLoader.getInstance().init(config);
		new JsonTask()
				.execute(new CheckFriendStateTask(getContact().toString()));
	}

	private OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				if (buttonView == mInvitedFriends) {
					// mRegAdapter = new HadInviteAdapter();
					mListView.setVisibility(View.VISIBLE);
					mListView2.setVisibility(View.GONE);
				} else {
					// mNRegAdapter = new NoRegisterAdapter();
					mListView2.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		if (isInvite) {
			mListView2.setVisibility(View.VISIBLE);
			mNRegAdapter.notifyDataSetChanged();
			isInvite = false;
		}
	}

	private class NoRegisterAdapter extends BaseAdapter implements
			View.OnClickListener {

		@Override
		public int getCount() {
			return needInvitedList == null ? 0 : needInvitedList.length();
		}

		@Override
		public JSONObject getItem(int position) {
			return needInvitedList.optJSONObject(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.item_moblie_friend,
						null);
				holder.yaoqing = (Button) convertView
						.findViewById(R.id.fragment_friend_invite_yaoqing);
				holder.name = (TextView) convertView
						.findViewById(R.id.fragment_friend_invite_name);
				convertView.findViewById(R.id.fragment_friend_invite_item)
						.setVisibility(View.VISIBLE);
				holder.yaoqing.setOnClickListener(this);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			JSONObject itemObj = getItem(position);
			if (itemObj != null) {
				holder.name.setText(itemObj.optString("name"));
				if (listPosition.contains(itemObj)) {
					holder.yaoqing.setTag(null);
					holder.yaoqing
							.setTextColor(Color
									.parseColor(getString(R.color.text_textcolor_gray1)));
					holder.yaoqing.setText("已邀请");
				} else {
					holder.yaoqing.setText("邀请");
					holder.yaoqing.setTextColor(Color
							.parseColor(getString(R.color.theme_color)));
					holder.yaoqing.setTag(itemObj);
				}
			}
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				isInvite = true;
				JSONObject obj = (JSONObject) v.getTag();
				listPosition.add(obj);
				new JsonTask()
						.execute(new GetMSComtent(obj.optString("mobile")));
			}
		}

	}

	private class HadInviteAdapter extends BaseAdapter implements
			View.OnClickListener {
		private int pos;

		@Override
		public int getCount() {
			return hadInvitedList == null ? 0 : hadInvitedList.length();
		}

		@Override
		public JSONObject getItem(int position) {
			return hadInvitedList.optJSONObject(position);
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
				convertView = mInflater.inflate(R.layout.item_moblie_friend,
						null);
				holder.avatar = (ImageView) convertView
						.findViewById(R.id.fragment_friend_invited_avator);
				holder.nickName = (TextView) convertView
						.findViewById(R.id.fragment_friend_invited_nickname);
				holder.invitedName = (TextView) convertView
						.findViewById(R.id.fragment_friend_invited_name);
				holder.guanzhu = (Button) convertView
						.findViewById(R.id.fragment_friend_invited_guanzhu);
				convertView.findViewById(R.id.fragment_friend_invited_item)
						.setVisibility(View.VISIBLE);
				holder.guanzhu.setOnClickListener(this);
				holder.avatar.setOnClickListener(this);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			JSONObject itemObj = getItem(position);
			if (itemObj != null) {
				holder.nickName.setText(itemObj.optString("nickname"));
				holder.invitedName
						.setText("手机联系人：" + itemObj.optString("name"));
				holder.avatar.setTag(itemObj);
				ImageLoader.getInstance().displayImage(
						itemObj.optString("avatar"), holder.avatar);
				holder.guanzhu.setText(getGuanzhu());
				holder.guanzhu.setOnClickListener(this);
				holder.guanzhu.setTag(itemObj);
				if (itemObj.optInt("is_attention") == 1) {// 已关注
					holder.guanzhu.setText("已关注");
					holder.guanzhu.setTextColor(Color.parseColor("#ffffff"));
					holder.guanzhu
							.setBackgroundResource(R.drawable.bg_address_add);
				} else {
					holder.guanzhu.setText(getGuanzhu());
					holder.guanzhu.setTextColor(Color
							.parseColor(getString(R.color.theme_color)));
					holder.guanzhu
							.setBackgroundResource(R.drawable.bg_semicircle_white_gray);
				}
			}
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() == null) {
				return;
			}
			JSONObject obj = (JSONObject) v.getTag();
			indexObject = obj;
			if (v.getId() == R.id.fragment_friend_invited_avator) {
				startActivity(AgentActivity.intentForFragment(mActivity,
						AgentActivity.FRAGMENT_PERSONAL_HOME).putExtra(
						"userId", obj.optString("member_id")));
			} else {
				String fansID = AgentApplication.getLoginedUser(mActivity)
						.getMemberId();
				if (!TextUtils.isEmpty(fansID)) {
					showCancelableLoadingDialog();
					if ("0".equals(obj.optString("is_attention").trim())) {
						new JsonTask().execute(new AttentionTask(obj
								.optString("member_id"), fansID, obj));
					} else {
						new JsonTask().execute(new CalcelAttentionTaskTask(obj
								.optString("member_id"), fansID, obj));
					}
				}
			}

		}
	}

	private class ViewHolder {
		private ImageView avatar;
		private TextView nickName;
		private TextView invitedName;
		private TextView name;
		private Button guanzhu;
		private Button yaoqing;
	}

	private SpannableString getGuanzhu() {
		SpannableString sp = new SpannableString("+ 关注");
		ForegroundColorSpan span = new ForegroundColorSpan(0xff999999);
		sp.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sp;
	}

	/**
	 * 检查通讯录的好友状态
	 * 
	 * @CreatTime 2015-7-31 下午5:56:04
	 * 
	 */
	private class CheckFriendStateTask implements JsonTaskHandler {

		private String string;

		public CheckFriendStateTask(String string) {
			this.string = string;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					hadInvitedList = data.optJSONArray("reg");
					needInvitedList = data.optJSONArray("no_reg");
				} else {

				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				// mRegAdapter = new HadInviteAdapter();
				hideLoadingDialog_mt();
				mRegAdapter.notifyDataSetChanged();
				mNRegAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.mobile_list");
			req.addParams("mobiles", string);
			return req;
		}

	}

	private class GetMSComtent implements JsonTaskHandler {

		private String mobile;

		public GetMSComtent(String mobile) {
			this.mobile = mobile;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Uri smsToUri = Uri.parse("smsto:" + mobile + "");// 发送电话
					Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
					intent.putExtra("sms_body", all.optString("data"));
					startActivity(intent);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.get_invite_content");
			return req;
		}

	}

	/**
	 * 关注用户
	 * 
	 * @author chanson
	 * @CreatTime 2015-8-4 上午10:35:19
	 * 
	 */
	private class AttentionTask implements JsonTaskHandler {

		private String menberID, fansID;
		private JSONObject jsonObject;

		public AttentionTask(String menberID, String fansID,
				JSONObject jsonObject) {
			this.fansID = fansID;
			this.menberID = menberID;
			this.jsonObject = jsonObject;
		}

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					if (!jsonObject.isNull("is_attention"))
						jsonObject.remove("is_attention");
					jsonObject.put("is_attention", String.valueOf(1));
					mRegAdapter.notifyDataSetChanged();
				}
				mRegAdapter.notifyDataSetChanged();

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.attention");
			req.addParams("member_id", menberID);// 被关注的用户ID
			req.addParams("fans_id", fansID);// 关注的用户ID
			return req;
		}

	}

	/**
	 * 获取手机通讯录 creatTime 2015-7-31 下午5:55:37
	 * 
	 * @return return ArrayList<ContactMember>
	 */
	private ArrayList<JSONObject> getContact() {
		// ArrayList<ContactMember> listMembers = new
		// ArrayList<ContactMember>();
		ArrayList<JSONObject> listContact = new ArrayList<JSONObject>();
		Cursor cursor = null;
		try {

			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
			// 这里是获取联系人表的电话里的信息 包括：名字，名字拼音，联系人id,电话号码；
			// 然后在根据"sort-key"排序
			cursor = mActivity.getContentResolver().query(
					uri,
					new String[] { "display_name", "sort_key", "contact_id",
							"data1" }, null, null, "sort_key");

			if (cursor.moveToFirst()) {
				do {
					// ContactMember contact = new ContactMember();
					JSONObject json = new JSONObject();
					String contact_phone = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					String name = cursor.getString(0);
					// String sortKey = getSortKey(cursor.getString(1));
					// int contact_id = cursor
					// .getInt(cursor
					// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
					// contact.contact_name = name;
					// contact.sortKey = sortKey;
					// contact.contact_phone = contact_phone;
					// contact.setContact_id(contact_id);
					json.put("name", name);
					json.put("mobile", removeAllSpace(contact_phone));
					if (name != null) {
						// listMembers.add(contact);
						listContact.add(json);
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return listContact;
	}

	public String removeAllSpace(String str) {
		String tmpstr = str.replace(" ", "");
		return tmpstr;
	}

	private class ContactMember {
		private String contact_name;
		// private String sortKey;
		private String contact_phone;
		// private int setContact_id;

		// public void setContact_id(int setContact_id) {
		// this.setContact_id = setContact_id;
		// }
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
					Run.alert(mActivity, "已取消关注");
					if (!jsonObject.isNull("is_attention"))
						jsonObject.remove("is_attention");
					jsonObject.put("is_attention", String.valueOf(0));
					mRegAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
