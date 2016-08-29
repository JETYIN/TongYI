package com.qianseit.westore.activity.account;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AboutFragment;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.activity.MainTabFragmentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.MyAutoUpdate;
import cn.shopex.ecstore.R;

public class AccountSettingFragment extends BaseDoFragment {
//	private static final int REQUEST_CODE_PICKER_AVATAR = 0x1001;
//
//	private static final int FRAGMENT_SHARE = 1;
//	private static final int FRAGMENT_NULL = 0;
//
//	private ItemBeam[] group1 = {
//			new ItemBeam("头像", AgentActivity.FRAGMENT_ACCOUNT_RESET_AVATAR),
//			new ItemBeam("昵称", AgentActivity.FRAGMENT_ACCOUNT_NICKNAME),
//			new ItemBeam("更改密码", AgentActivity.FRAGMENT_ACCOUNT_RESET_PASSWD) };
//	// ,new ItemBeam("账户", FRAGMENT_NULL) };
//	private ItemBeam[] group2 = { new ItemBeam("会员等级", FRAGMENT_NULL),
////			new ItemBeam("分享给好友", FRAGMENT_SHARE),
//			new ItemBeam("意见反馈", AgentActivity.FRAGMENT_FEEDBACK),
//			new ItemBeam("版本更新", AgentActivity.FRAGMENT_ABOUT_US) };
//	private ItemBeam[] group4 = { new ItemBeam("客服热线",
//			AgentActivity.FRAGMENT_CALL_SERVICE_PHONE) };
//	private ItemBeam[] group5 = { new ItemBeam("清除缓存",
//			AgentActivity.FRAGMENT_CLEAR_CACHE) };

//	private LoginedUser mLoginedUser;

//	private ExpandableListView mListView;
//	private ArrayList<ItemBeam[]> mGroupList = new ArrayList<ItemBeam[]>();

	public static final String WURAOMODE = "WURAU_MODE";
	
	private String mCacheFileSize = Run.EMPTY_STR;
	private Dialog mDialog;
//	private Dialog mDialog = AccountLoginFragment.showAlertDialog(mActivity, getString(R.string.account_setting_clear_cache_summary), R.string.cancel, R.string.account_setting_clear_cache, null,
//            new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    mDialog.dismiss();
////						((TextView)findViewById(R.id.account_setting_cash_count)).setText("0M");
//                    deleteCacheFolder();
//                }
//            }, false, null);
	
	private CheckBox mCheckBox ;
//	private boolean isNeedChage;

	public AccountSettingFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mLoginedUser = AgentApplication.getLoginedUser(mActivity);

		// 添加列表分组
//		mGroupList.add(group1);
//		mGroupList.add(group2);
		// 我的收入只有微店用户和经销商需要显示
//		mGroupList.add(group4);
		// mGroupList.add(group5);

//		if (!mLoginedUser.isLogined())
//			startActivityForResult(AgentActivity.intentForFragment(mActivity,
//					AgentActivity.FRAGMENT_ACCOUNT_LOGIN),
//					REQUEST_CODE_USER_LOGIN);
	}

	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.more);

		rootView = inflater.inflate(R.layout.fragment_account_setting, null);
		if (AgentApplication.getLoginedUser(mActivity).isLogined()) {
			findViewById(R.id.account_logout_button).setOnClickListener(this);
		} else {
			findViewById(R.id.account_logout_button).setVisibility(View.GONE);
		}
		findViewById(R.id.account_setting_aboutus).setOnClickListener(this);
		findViewById(R.id.account_setting_feedback).setOnClickListener(this);
		findViewById(R.id.account_setting_clear_cash).setOnClickListener(this);
		findViewById(R.id.account_setting_update).setOnClickListener(this);
		PackageManager mPm = mActivity.getPackageManager();
		try {
			PackageInfo pi = mPm.getPackageInfo(mActivity.getPackageName(), 0);
			((TextView) findViewById(R.id.account_setting_version_code))
					.setText(getString(R.string.about_version, pi.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mCheckBox = (CheckBox)rootView.findViewById(R.id.account_setting_checkbox);
		mCheckBox.setChecked(Run.loadOptionBoolean(mActivity, WURAOMODE, false));
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				if (!isNeedChage) {
//					Run.excuteJsonTask(new JsonTask(), new WuRaoStatus(isChecked , true));
					Run.savePrefs(mActivity, WURAOMODE, isChecked);
//				}
			}
		});
//		Run.excuteJsonTask(new JsonTask(), new WuRaoStatus(false , false));
//		mListView = (ExpandableListView) findViewById(android.R.id.list);
//		mListView.setAdapter(new GroupAdapter());
//		mListView.setOnGroupClickListener(new OnGroupClickListener() {
//			@Override
//			public boolean onGroupClick(ExpandableListView p, View v, int pos,
//					long id) {
//				return true;
//			}
//		});

		// 计算缓存
		 new Thread() {
			 @Override
			 public void run() {
				 calculateCacheSize();
			 }
		 }.start();

		// 展开所有分组
//		for (int i = 0, c = mGroupList.size(); i < c; i++)
//			mListView.expandGroup(i);
	};
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.account_logout_button) {
			mDialog = AccountLoginFragment.showAlertDialog(mActivity, getString(R.string.account_logout_confirm), R.string.cancel, R.string.ok, null, 
					new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mDialog.dismiss();
							Run.excuteJsonTask(new JsonTask(), new LogoutTask());
						}
					}, false, null);
//			CustomDialog dialog = new CustomDialog(mActivity);
//			dialog.setMessage(R.string.account_logout_confirm);
//			dialog.setNegativeButton(R.string.cancel, null);
//			dialog.setPositiveButton(R.string.ok, new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Run.excuteJsonTask(new JsonTask(), new LogoutTask());
//				}
//			}).setCancelable(true).show();
		} else if(v.getId() == R.id.account_setting_aboutus){
			startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ABOUT_US));
		} else if(v.getId() == R.id.account_setting_feedback){
			startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_FEEDBACK));
		} else if(v.getId() == R.id.account_setting_clear_cash){
			showClearCacheDialog();
		} else if(v.getId() == R.id.account_setting_update){
			new JsonTask().execute(new UpdateTask());
		} else {
			super.onClick(v);
		}
	}
	
	private class UpdateTask implements JsonTaskHandler {
		private boolean isShowDialog;

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean jrb = new JsonRequestBean(
					"mobileapi.info.get_version");
			return jrb.addParams("os", "android");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				JSONObject data = all.optJSONObject("data");
				if (data == null) {
					return;
				}
				String version = data.optString("ver");
				if (TextUtils.isEmpty(version)) {
					return;
				}
				version = version.replaceAll("\\.", "");
				String oldVer = mActivity.getString(R.string.app_version_name).replaceAll("\\.", "");
				if (version.length() > oldVer.length()) {
					for (int i = oldVer.length(); i < version.length(); i++) {
						oldVer += "0"; 
					}
				} else if (version.length() < oldVer.length()) {
					for (int i = version.length(); i < oldVer.length(); i++)
						version += "0";
				}
				int ver = Integer.parseInt(version);
				int old = Integer.parseInt(oldVer);
				if (ver > old) {
					MyAutoUpdate autoUpdate = new MyAutoUpdate(mActivity);
					autoUpdate.checkUpdateInfo(data.optString("down"),data.optInt("ismust"),data.optString("info"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class WuRaoStatus implements JsonTaskHandler{
		
		private boolean state;
		private boolean isChangeState;
		
		public WuRaoStatus(boolean state , boolean isChangeState){
			this.state = state;
			this.isChangeState = isChangeState;
		}
		
		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all ,false)) {
					JSONObject data = all.optJSONObject("data");
					mCheckBox.setChecked(data.optBoolean("queit"));
//					isNeedChage = false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public JsonRequestBean task_request() {
//			isNeedChage = true;
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean( "mobileapi.member.queit");
			if (isChangeState) {
				req.addParams("change", ""+state);
			}
			return req;
		}
		
	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if (requestCode == REQUEST_CODE_USER_LOGIN
//				&& resultCode != Activity.RESULT_OK) {
//			mActivity.finish();
//		} else if (requestCode == REQUEST_CODE_PICKER_AVATAR
//				&& resultCode == Activity.RESULT_OK) {
//			FileOutputStream fos = null;
//			Bitmap bitmap = null;
//			try {
//				ContentResolver resolver = mActivity.getContentResolver();
//				Uri originalUri = data.getData();
//				Cursor cursor = resolver.query(originalUri,
//						new String[] { Images.Media.DATA }, null, null, null);
//				cursor.moveToFirst();
//				File originFile = new File(cursor.getString(0));
//				if (!originFile.exists())
//					return;
//				// 图尺寸大小限制
//				double size = originFile.length() / 1024.0 / 1024.0;
//				if (size > 1) {
//					Run.alert(mActivity, R.string.shop_thumb_large_size);
//					return;
//				}
//
//				bitmap = BitmapFactory.decodeFile(originFile.getAbsolutePath());
//
//				File file = new File(Run.doCacheFolder, "file");
//				if (!file.getParentFile().exists())
//					file.getParentFile().mkdirs();
//				fos = new FileOutputStream(file);
//				bitmap.compress(CompressFormat.JPEG, 60, fos);
//				fos.flush();
//				// 更新到服务器
//				JsonTaskHandler handler = null;
//				handler = new UpdateWallpaperTask(file, "avatar",
//						new JsonRequestBean.JsonRequestCallback() {
//							@Override
//							public void task_response(String jsonStr) {
//								((BaseAdapter) mListView.getAdapter())
//										.notifyDataSetChanged();
//							}
//						});
//				Run.excuteJsonTask(new JsonTask(), handler);
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				// 回收资源
//				if (bitmap != null)
//					bitmap.recycle();
//				try {
//					if (fos != null)
//						fos.close();
//				} catch (Exception e) {
//				}
//			}
//		}
//	}

	private class LogoutTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			return new JsonRequestBean( "mobileapi.passport.logout");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					AgentApplication.getLoginedUser(mActivity).setIsLogined(false);
					AgentApplication.getLoginedUser(mActivity).setUserInfo(null);
					Run.savePrefs(mActivity, Run.pk_logined_user_password, "");
					mActivity.finish();
				}
			} catch (Exception e) {
			}
		}
	}

	// 计算缓存大小
	private void calculateCacheSize() {
		long size = Run.countFileSize(new File(Run.doImageCacheFolder));
		mCacheFileSize = Formatter.formatFileSize(mActivity, size);
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				((GroupAdapter) mListView.getExpandableListAdapter())
//						.notifyDataSetChanged();
//			}
//		});
//		DecimalFormat df = new DecimalFormat("0.#");
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				((TextView)findViewById(R.id.account_setting_cash_count)).setText(mCacheFileSize);
			}
		});
//		((TextView)findViewById(R.id.account_setting_cash_count)).setText(df.format(size / 1024.0 / 1024.0) + "M");
	}

	// 清理缓存
	private void showClearCacheDialog() {
		
		mDialog = AccountLoginFragment.showAlertDialog(mActivity, getString(R.string.account_setting_clear_cache_summary), R.string.cancel, R.string.account_setting_clear_cache, null, 
				new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
//						((TextView)findViewById(R.id.account_setting_cash_count)).setText("0M");
						deleteCacheFolder();
					}
				}, false, null);
//		CustomDialog dialog = new CustomDialog(mActivity);
//		dialog.setNegativeButton(R.string.cancel, null);
//		dialog.setMessage(R.string.account_setting_clear_cache_summary);
//		dialog.setPositiveButton(R.string.account_setting_clear_cache,
//				new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						deleteCacheFolder();
//					}
//				}).setCancelable(true).show();
	}

	// 删除缓存目录
	private void deleteCacheFolder() {
		new Thread() {
			@Override
			public void run() {
				Run.deleteAllFiles(new File(Run.doImageCacheFolder));
				mActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						AccountLoginFragment.showAlertDialog(mActivity, "清除缓存完成！", "","OK", null, null, false, null);
						((TextView)rootView.findViewById(R.id.account_setting_cash_count)).setText("0M");
					}
				});
			}
		}.start();
	}

//	private class GroupAdapter extends BaseExpandableListAdapter implements
//			OnClickListener {
//		private Resources res;
//		private int groupHeight;
//
//		public GroupAdapter() {
//			res = mActivity.getResources();
//			groupHeight = res.getDimensionPixelSize(R.dimen.PaddingLarge);
//		}
//
//		@Override
//		public int getGroupCount() {
//			return mGroupList.size();
//		}
//
//		@Override
//		public int getChildrenCount(int groupPosition) {
//			return mGroupList.get(groupPosition).length;
//		}
//
//		@Override
//		public Object getGroup(int groupPosition) {
//			return null;
//		}
//
//		@Override
//		public ItemBeam getChild(int groupPosition, int childPosition) {
//			try {
//				return mGroupList.get(groupPosition)[childPosition];
//			} catch (Exception e) {
//				return null;
//			}
//		}
//
//		@Override
//		public long getGroupId(int groupPosition) {
//			return groupPosition;
//		}
//
//		@Override
//		public long getChildId(int groupPosition, int childPosition) {
//			return childPosition;
//		}
//
//		@Override
//		public boolean hasStableIds() {
//			return false;
//		}
//
//		@Override
//		public View getGroupView(int groupPosition, boolean isExpanded,
//				View convertView, ViewGroup parent) {
//			if (convertView == null) {
//				convertView = new View(mActivity);
//				convertView.setLayoutParams(new AbsListView.LayoutParams(
//						LayoutParams.MATCH_PARENT, 0));
//			}
//			return convertView;
//		}
//
//		@Override
//		public View getChildView(int groupPosition, int childPosition,
//				boolean isLastChild, View convertView, ViewGroup parent) {
//			if (convertView == null) {
//				convertView = mActivity.getLayoutInflater().inflate(
//						R.layout.fragment_account_setting_item, null);
//				convertView.setOnClickListener(this);
//			}
//
//			ItemBeam bean = getChild(groupPosition, childPosition);
//			int childCount = getChildrenCount(groupPosition);
//			convertView.setTag(bean);
//
//			View alertView = convertView.findViewById(android.R.id.icon1);
//			if (bean.fragment == AgentActivity.FRAGMENT_ABOUT_US
//					&& Run.loadOptionLong(mActivity,
//							Run.pk_newest_version_code, 0) > Run
//							.getVersionCode(mActivity))
//				alertView.setVisibility(View.VISIBLE);
//			else
//				alertView.setVisibility(View.GONE);
//
//			View toggle = convertView.findViewById(android.R.id.toggle);
//			ImageView iconView = (ImageView) convertView
//					.findViewById(android.R.id.icon);
//			TextView summary = (TextView) convertView
//					.findViewById(android.R.id.summary);
//			((TextView) convertView.findViewById(android.R.id.text1))
//					.setText(bean.name);
//
//			if (bean.fragment == AgentActivity.FRAGMENT_CALL_SERVICE_PHONE) {
//				summary.setText(R.string.service_phone);
//				toggle.setVisibility(View.GONE);
//			} else {
//				summary.setText(Run.EMPTY_STR);
//				toggle.setVisibility(View.VISIBLE);
//			}
//
//			if (bean.fragment == AgentActivity.FRAGMENT_ACCOUNT_RESET_AVATAR) {
//				iconView.setVisibility(View.VISIBLE);
//				updateAvatarView(iconView, mLoginedUser);
//			} else {
//				iconView.setVisibility(View.INVISIBLE);
//			}
//
//			if (childCount == 1) {
//				convertView
//						.setBackgroundResource(R.drawable.list_item_singlebg);
//			} else if (childPosition == 0) {
//				convertView.setBackgroundResource(R.drawable.list_item_topbg);
//			} else if (childPosition == childCount - 1) {
//				convertView
//						.setBackgroundResource(R.drawable.list_item_bottombg);
//			} else {
//				convertView
//						.setBackgroundResource(R.drawable.list_item_middlebg);
//			}
//			return convertView;
//		}
//
//		@Override
//		public void onClick(View v) {
//			try {
//				ItemBeam bean = (ItemBeam) v.getTag();
//				if (bean.fragment == AgentActivity.FRAGMENT_CLEAR_CACHE) {
//					showClearCacheDialog();
//				} else if (bean.fragment == FRAGMENT_NULL) {
//					startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_VIP_LEVEL));
//				} else if (bean.fragment == FRAGMENT_SHARE) {
//				} else if (bean.fragment == AgentActivity.FRAGMENT_ACCOUNT_RESET_AVATAR) {
//					Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
//					intent.setType("image/*");
//					startActivityForResult(intent, REQUEST_CODE_PICKER_AVATAR);
//				} else if(bean.fragment == AgentActivity.FRAGMENT_CALL_SERVICE_PHONE){
//					final CustomDialog dialog = new CustomDialog(mActivity);
//					dialog.setMessage("确定要拨打客服热线？");
//					dialog.setNegativeButton(getString(R.string.cancel), new OnClickListener() {
//						
//						@Override
//						public void onClick(View v) {
//							dialog.dismiss();
//						}
//					}).setPositiveButton(getString(R.string.ok), new OnClickListener() {
//						
//						@Override
//						public void onClick(View v) {
//							String tel = getString(R.string.service_phone).replace("-", "");
//							Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + tel));
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(intent);
//						}
//					}).setCanceledOnTouchOutside(true).show();
//				} else {
//					Intent intent = AgentActivity.intentForFragment(mActivity,
//							bean.fragment).putExtra(Run.EXTRA_TITLE, bean.name);
//					mActivity.startActivity(intent);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public boolean isChildSelectable(int groupPosition, int childPosition) {
//			return false;
//		}
//	}
//
//	private class ItemBeam {
//		public String name;
//		public int fragment;
//
//		public ItemBeam(String name, int fragment) {
//			this.name = name;
//			this.fragment = fragment;
//		}
//	}
}
