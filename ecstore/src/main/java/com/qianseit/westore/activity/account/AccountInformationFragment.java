package com.qianseit.westore.activity.account;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.FileUtils;

public class AccountInformationFragment extends BaseDoFragment {

	private final int REQUEST_CODE_PICKER_AVATAR = 0x1001;
	private final int REQUEST_NICHENG = 0x1002;
	private final int REQUEST_CODE_SIGNATURE = 0x1003;
	private final int REQUEST_CODE_AddRess = 0x1004;

	private LoginedUser mLoginedUser;
	private ExpandableListView mListView;
	private Dialog dialog;// gender dialog
	private BaseExpandableListAdapter mGroupAdapter;

	private ItemBeam[] group1 = {
			new ItemBeam("修改头像", AgentActivity.FRAGMENT_ACCOUNT_RESET_AVATAR),
			new ItemBeam("昵称", AgentActivity.FRAGMENT_ACCOUNT_NICKNAME),
			new ItemBeam("性别", 5), new ItemBeam("居住地", 4),
			new ItemBeam("个性签名", AgentActivity.FRAGMENT_SIGNATURE) };

	private ArrayList<ItemBeam[]> mGroupList = new ArrayList<ItemBeam[]>();

	public AccountInformationFragment() {
		
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mGroupList.add(group1);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle("个人资料");
		rootView = inflater.inflate(R.layout.fragment_account_personal, null);
		findViewById(R.id.account_logout_button).setVisibility(View.GONE);

		mListView = (ExpandableListView) findViewById(android.R.id.list);
		// mGroupAdapter = new GroupAdapter();
		// mListView.setAdapter(mGroupAdapter);
		// mListView.setOnGroupClickListener(new OnGroupClickListener() {
		// @Override
		// public boolean onGroupClick(ExpandableListView p, View v, int pos,
		// long id) {
		// return true;
		// }
		// });
		//
		// // 展开所有分组
		// for (int i = 0, c = mGroupList.size(); i < c; i++)
		// mListView.expandGroup(i);
		// setListData();
	}

	@Override
	public void onResume() {
		super.onResume();
		setListData();
	}

	private void setListData() {
		// mGroupAdapter = new GroupAdapter();
		mListView.setAdapter(new GroupAdapter());
		mListView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView p, View v, int pos,
					long id) {
				return true;
			}
		});

		// 展开所有分组
		for (int i = 0, c = mGroupList.size(); i < c; i++)
			mListView.expandGroup(i);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(!(resultCode==Activity.RESULT_OK))
			return;
		if ( requestCode == REQUEST_CODE_SIGNATURE) {
			String value = data.getStringExtra(Run.EXTRA_VALUE);
			AgentApplication.getLoginedUser(mActivity).setRemark(value);
			setListData();
		} else if (requestCode==REQUEST_CODE_AddRess) {
			String str=data.getStringExtra(Run.EXTRA_VALUE);
			Run.excuteJsonTask(new JsonTask(),
					new UpdateAccountInfoTask("",str));
		} else if (requestCode == REQUEST_CODE_PICKER_AVATAR
				&& resultCode == Activity.RESULT_OK) {

			FileOutputStream fos = null;
			Bitmap bitmap = null;
			try {
				String path = data.getStringExtra("imagePath");
				File originFile = new File(path);
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					if (originFile.exists()) {
						if (!originFile.exists())
							return;
					}
				}

				bitmap = BitmapFactory.decodeFile(originFile.getAbsolutePath());

				File file = new File(Run.doCacheFolder, "file");
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				fos = new FileOutputStream(file);
				bitmap.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				// 更新到服务器
				JsonTaskHandler handler = null;
				handler = new UpdateWallpaperTask(file, "avatar",
						new JsonRequestBean.JsonRequestCallback() {
							@Override
							public void task_response(String jsonStr) {
								setListData();
							}
						});
				Run.excuteJsonTask(new JsonTask(), handler);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 回收资源
				if (bitmap != null)
					bitmap.recycle();
				try {
					if (fos != null)
						fos.close();
				} catch (Exception e) {
				}
			}

		}
	}

	private class GroupAdapter extends BaseExpandableListAdapter implements
			OnClickListener {

		private TextView title;
		private TextView content;
		private TextView detail;
		private ImageView avatar;
		private ImageView arrow;
		private int groupHeight;
		private Resources res;

		public GroupAdapter() {
			res = mActivity.getResources();
			groupHeight = res.getDimensionPixelSize(R.dimen.PaddingLarge);
		}

		@Override
		public int getGroupCount() {
			return mGroupList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mGroupList.get(groupPosition).length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public ItemBeam getChild(int groupPosition, int childPosition) {
			try {
				return mGroupList.get(groupPosition)[childPosition];
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new View(mActivity);
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						LayoutParams.MATCH_PARENT, groupHeight));
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.item_my_infomation, null);
				convertView.setOnClickListener(this);
				title = (TextView) convertView
						.findViewById(R.id.item_my_info_title);
				content = (TextView) convertView
						.findViewById(R.id.item_my_info_content);
				detail = (TextView) convertView
						.findViewById(R.id.item_my_info_detail);
				avatar = (ImageView) convertView
						.findViewById(R.id.item_my_info_avatar);
				arrow = (ImageView) convertView
						.findViewById(R.id.item_my_info_arrow);
			}
			ItemBeam bean = getChild(groupPosition, childPosition);
			convertView.setTag(bean);
			if (childPosition == 0) {
				avatar.setVisibility(View.VISIBLE);
				title.setVisibility(View.GONE);
				content.setVisibility(View.VISIBLE);
				arrow.setVisibility(View.VISIBLE);
				detail.setVisibility(View.GONE);
				content.setTextColor(Color.parseColor("#333333"));
				content.setText(bean.name);
				updateAvatarView(avatar, mLoginedUser);
			} else if (getChildrenCount(groupPosition) == childPosition + 1) {
				avatar.setVisibility(View.GONE);
				title.setVisibility(View.VISIBLE);
				content.setVisibility(View.GONE);
				arrow.setVisibility(View.INVISIBLE);
				detail.setVisibility(View.VISIBLE);
				title.setText(bean.name);
				detail.setText(!TextUtils.isEmpty(mLoginedUser.getRemark())?mLoginedUser.getRemark():"[未设置签名]");
			} else {
				avatar.setVisibility(View.GONE);
				title.setVisibility(View.VISIBLE);
				content.setVisibility(View.VISIBLE);
				arrow.setVisibility(View.VISIBLE);
				detail.setVisibility(View.GONE);
				if (bean.fragment == AgentActivity.FRAGMENT_ACCOUNT_NICKNAME) {
					content.setText(mLoginedUser.getNickName(mActivity));
					title.setText(bean.name);
				} else if (bean.fragment == -1) {
					title.setText(bean.name);
					content.setText(mLoginedUser.getAddress());

				} else if (bean.fragment == 5) {
					title.setText(bean.name);
					String strSex = "";
					if (mLoginedUser.getSex() == 1) {
						strSex = "男";
					} else if (mLoginedUser.getSex() == 0) {
						strSex = "女";
					} else {
						strSex = "其他";
					}
					content.setText(strSex);
				} else {
					title.setText(bean.name);
					String address=TextUtils.isEmpty(mLoginedUser.getAddress())?"[未设置]":mLoginedUser.getAddress();
					content.setText(address);

				}
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		@Override
		public void onClick(View v) {
			ItemBeam bean = (ItemBeam) v.getTag();
			if (bean != null) {
				if (bean.fragment == AgentActivity.FRAGMENT_ACCOUNT_RESET_AVATAR) {
//					Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					//intent.setType("image/*");

//					startActivityForResult(intent, REQUEST_CODE_PICKER_AVATAR);
//					FRAGMENT_PHOTO
					startActivityForResult(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_PHOTO)
							.putExtra("ID", "AVATAR"), REQUEST_CODE_PICKER_AVATAR);

				} else if (bean.fragment == 5) {
					dialog = AccountLoginFragment.showAlertDialog(mActivity,
							"性别选择", "", "", null, null, true,
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									dialog.dismiss();
									if (v.getId() == R.id.dialog_gender1) {

										Run.excuteJsonTask(new JsonTask(),
												new UpdateAccountInfoTask("1",""));
									} else if (v.getId() == R.id.dialog_gender2) {
										Run.excuteJsonTask(new JsonTask(),
												new UpdateAccountInfoTask("0",""));
									} else if (v.getId() == R.id.dialog_gender3) {
										Run.excuteJsonTask(new JsonTask(),
												new UpdateAccountInfoTask("2",""));
									}
								}
							});
				} else if (bean.fragment == AgentActivity.FRAGMENT_SIGNATURE) {
					Intent intent = AgentActivity.intentForFragment(mActivity,
							bean.fragment);
					startActivityForResult(intent,REQUEST_CODE_SIGNATURE);
				} else if (bean.fragment == AgentActivity.FRAGMENT_ACCOUNT_NICKNAME) {
					Intent intent = AgentActivity.intentForFragment(mActivity,
							bean.fragment);
					mActivity.startActivityForResult(intent, REQUEST_NICHENG);
				} else if(bean.fragment==4){
					startActivityForResult(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_MY_ADDRESS_PICKER),REQUEST_CODE_AddRess);
				}else {
					Intent intent = AgentActivity.intentForFragment(mActivity,
							bean.fragment);
					mActivity.startActivity(intent);
				}
			}
		}
	}

	private class ItemBeam {
		public String name;
		public int fragment;

		public ItemBeam(String name, int fragment) {
			this.name = name;
			this.fragment = fragment;
		}
	}

	private class UpdateAccountInfoTask implements JsonTaskHandler {
		private String strSex;
		private String strAddress;

		public UpdateAccountInfoTask(String sex,String address) {
			strSex = sex;
			strAddress=address;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					// 重新登录
					if(!TextUtils.isEmpty(strSex))
					AgentApplication.getLoginedUser(mActivity).setSex(
							Integer.parseInt(strSex));
					if(!TextUtils.isEmpty(strAddress))
					AgentApplication.getLoginedUser(mActivity).setAddress(strAddress);
					
					setListData();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean changeName = new JsonRequestBean(
					"mobileapi.member.save_setting");
			if(!TextUtils.isEmpty(strSex))
			changeName.addParams("sex", strSex);
			if(!TextUtils.isEmpty(strAddress))
			changeName.addParams("addr", strAddress);
			return changeName;
		}
	}

}
