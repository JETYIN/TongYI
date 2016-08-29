package com.qianseit.westore.activity.account;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.activity.MainTabFragmentActivity;
import com.qianseit.westore.activity.MeActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

/**
 * 个人中心
 */
public class AccountHomeFragment extends BaseDoFragment {
	private final int REQUEST_CODE_PICKER_AVATAR = 0x1001;
	private boolean isLoging = true;

	// private boolean fromLoginPage = false;
	// MobclickAgent 是友盟统计

	private ListView mListView;

	private LoginedUser mLoginedUser;
	private ArrayList<ItemBeam> mItems = new ArrayList<ItemBeam>();
	private View mHeaderView;
	private BaseAdapter mBaseHomeAdaptr;
	private VolleyImageLoader mImageLoader;
	private int payNum;
	private int shippingNum;
	private int receivingNum;
	private int recommendNum;

	public AccountHomeFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		mImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		isLoging = mLoginedUser.isLogined();
		mItems.add(new ItemBeam(R.drawable.me_item_order,
				R.string.me_item_orders, AgentActivity.FRAGMENT_ACCOUNT_ORDERS));
		mItems.add(new ItemBeam(R.drawable.me_item_back,
				R.string.me_item_back, AgentActivity.FRAGMENT_ACCOUNT_BACK));
		mItems.add(new ItemBeam(R.drawable.me_itme_back_order,
				R.string.me_item_back_order, AgentActivity.FRAGMENT_ACCOUNT_BACK_ORDER));
		mItems.add(new ItemBeam(R.drawable.me_item_my_recommend,
				R.string.me_item_recomment,
				AgentActivity.FRAGMENT_PERSONAL_HOME));
		mItems.add(new ItemBeam(R.drawable.me_item_coupon,
				R.string.me_item_coupon, AgentActivity.FRAGMENT_TICKET_LIST));
		mItems.add(new ItemBeam(R.drawable.me_item_coin,
				R.string.me_item_yingbang, AgentActivity.FRAGMENT_SHOP_SERVICE));
		mItems.add(new ItemBeam(R.drawable.me_item_collect,
				R.string.me_item_collect, AgentActivity.FRAGMENT_PERSONAL_HOME));
		mItems.add(new ItemBeam(R.drawable.me_item_msg, R.string.me_item_mymsg,
				AgentActivity.FRAGMENT_MY_MASSAGE));
		mItems.add(new ItemBeam(R.drawable.me_item_address,
				R.string.me_item_address, AgentActivity.FRAGMENT_ADDRESS_BOOK));
		mItems.add(new ItemBeam(R.drawable.me_item_setting,
				R.string.me_item_setting,
				AgentActivity.FRAGMENT_ACCOUNT_SETTING));
	}

	@Override
	public void onResume() {
		super.onResume();
		isLoging = mLoginedUser.isLogined();
		if (!mLoginedUser.isLogined()) {
			findViewById(R.id.account_header_view_login_view).setVisibility(
					View.VISIBLE);
			findViewById(R.id.account_header_view_logined_view).setVisibility(
					View.INVISIBLE);
			payNum = 0;
			shippingNum = 0;
			receivingNum = 0;
			recommendNum = 0;
			mBaseHomeAdaptr.notifyDataSetChanged();

		} else {
			Run.excuteJsonTask(new JsonTask(),
					new GetUserInfoTask(mLoginedUser.getMemberId()));
			findViewById(R.id.account_header_view_login_view).setVisibility(
					View.INVISIBLE);
			findViewById(R.id.account_header_view_logined_view).setVisibility(
					View.VISIBLE);
			((TextView) mHeaderView
					.findViewById(R.id.account_header_view_uname))
					.setText(mLoginedUser.getNickName(mActivity));
			((TextView) mHeaderView.findViewById(R.id.account_header_view_lv))
					.setText(mLoginedUser.getVipNum());
			mHeaderView.findViewById(R.id.account_header_view_lv)
					.setOnClickListener(this);
			ImageView sexImage = ((ImageView) mHeaderView
					.findViewById(R.id.account_header_view_sex));
			sexImage.setVisibility(View.VISIBLE);
			if (mLoginedUser.getSex() == 1) {
				sexImage.setImageResource(R.drawable.home_nan);
			} else if (mLoginedUser.getSex() == 0) {
				sexImage.setImageResource(R.drawable.home_nv);
			} else {
				sexImage.setVisibility(View.GONE);
			}
			((TextView) mHeaderView.findViewById(R.id.account_header_view_sign))
					.setText(mLoginedUser.getRemark());
			// 个人头像
			ImageView avatarView = (CircleImageView) mHeaderView
					.findViewById(R.id.account_header_view_avatar);
			if (mLoginedUser.getAvatarUri() != null) {
				// ImageLoader loader = AgentApplication
				// .getAvatarLoader(mActivity);
				Uri avatarUri = Uri.parse(mLoginedUser.getAvatarUri());
				avatarView.setTag(avatarUri);
				// loader.showImage(avatarView, avatarUri);
				mImageLoader.showImage(avatarView, mLoginedUser.getAvatarUri());
			}
		}
	}

	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.account_center);
		mActionBar.setShowRightButton(true);

		rootView = inflater.inflate(R.layout.fragment_account_homepage, null);
		mHeaderView = rootView.findViewById(R.id.account_home_headerview);
		mHeaderView.findViewById(R.id.account_header_view_avatar)
				.setOnClickListener(this);
		mHeaderView.findViewById(R.id.account_header_view_edit)
				.setOnClickListener(this);
		mHeaderView.findViewById(R.id.account_header_view_login)
				.setOnClickListener(this);
		mHeaderView.findViewById(R.id.account_header_view_regist)
				.setOnClickListener(this);
		Run.removeFromSuperView(mHeaderView);
		mListView = (ListView) findViewById(android.R.id.list);
		mHeaderView.setLayoutParams(new AbsListView.LayoutParams(mHeaderView
				.getLayoutParams()));
		mListView.addHeaderView(mHeaderView);
		mBaseHomeAdaptr = new HomeItemAdapter();
		mListView.setAdapter(mBaseHomeAdaptr);

	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.account_header_view_avatar) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_PERSONAL_HOME).putExtra("userId",
					mLoginedUser.getMemberId()));
			// startActivity(AgentActivity.intentForFragment(mActivity,
			// AgentActivity.FRAGMENT_FRINENT));
		} else if (v.getId() == R.id.account_orders_rating) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ORDER_RATING));
		} else if (v.getId() == R.id.account_home_item_phone) {
			final CustomDialog dialog = new CustomDialog(mActivity);
			dialog.setMessage("确定要拨打客服热线？");
			dialog.setNegativeButton(getString(R.string.cancel),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					})
					.setPositiveButton(getString(R.string.ok),
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									String tel = getString(
											R.string.service_phone).replace(
											"-", "");
									Intent intent = new Intent(
											Intent.ACTION_DIAL, Uri
													.parse("tel:" + tel));
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							}).setCanceledOnTouchOutside(true).show();
		} else if (v.getId() == R.id.account_header_view_login) {
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
			// this.startActivity(new Intent(getActivity(), MeActivity.class));

		} else if (v.getId() == R.id.account_header_view_regist) {

			//关闭会员注册接口
			//Toast.makeText(getActivity(), "暂时关闭会员注册功能", Toast.LENGTH_SHORT).show();
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ACCOUNT_REGIST));
		}
		// else if (v.getId() == R.id.account_header_view_edit) {// 个人主页
		//
		// this.startActivity(new Intent(getActivity(), MeActivity.class));
		// /*startActivity(AgentActivity.intentForFragment(mActivity,
		// AgentActivity.FRAGMENT_ACCOUNT_REGIST));*/
		// }
		else if (v.getId() == R.id.account_header_view_login_view) {
			this.startActivity(new Intent(getActivity(), MeActivity.class));

		} else if (v.getId() == R.id.account_header_view_edit) {// 个人主页
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_MY_INFORMATION));
			// startActivity(AgentActivity.intentForFragment(mActivity,
			// AgentActivity.FRAGMENT_TEST));
		} else if (v.getId() == R.id.account_header_view_lv) {
			getActivity().startActivity(
					AgentActivity
							.intentForFragment(getActivity(),
									AgentActivity.FRAGMENT_HELP_ARTICLE)
							.putExtra("title", "商派等级规则")
							.putExtra(
									"url",
									Run.buildString(Run.DOMAIN,"/wap/statics-pointLv.html?from=app&member_id=", mLoginedUser.getMemberId())));
		} else {
			super.onClick(v);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// fromLoginPage = (requestCode == REQUEST_CODE_USER_LOGIN);

		if (requestCode == REQUEST_CODE_USER_LOGIN
				&& resultCode != Activity.RESULT_OK) {
			// MainTabFragmentActivity.mTabActivity.setCurrentTabByIndex(0);
			// fromLoginPage = false;
		} else if (requestCode == REQUEST_CODE_PICKER_AVATAR
				&& resultCode == Activity.RESULT_OK) {
			FileOutputStream fos = null;
			Bitmap bitmap = null;
			try {
				ContentResolver resolver = mActivity.getContentResolver();
				Uri originalUri = data.getData();
				Cursor cursor = resolver.query(originalUri,
						new String[] { Images.Media.DATA }, null, null, null);
				cursor.moveToFirst();
				File originFile = new File(cursor.getString(0));
				if (!originFile.exists())
					return;
				// 图尺寸大小限制
				double size = originFile.length() / 1024.0 / 1024.0;
				if (size > 1) {
					Run.alert(mActivity, R.string.shop_thumb_large_size);
					return;
				}

				bitmap = BitmapFactory.decodeFile(originFile.getAbsolutePath());

				File file = new File(Run.doCacheFolder, "file");
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				fos = new FileOutputStream(file);
				bitmap.compress(CompressFormat.JPEG, 60, fos);
				fos.flush();
				// 更新到服务器
				JsonTaskHandler handler = null;
				handler = new UpdateWallpaperTask(file, "avatar",
						new JsonRequestCallback() {
							@Override
							public void task_response(String jsonStr) {
								initAccountHeaderView(rootView, mLoginedUser);
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
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class HomeItemAdapter extends BaseAdapter implements
			OnClickListener {

		private View arrowView;
		private ItemBeam bean;

		public HomeItemAdapter() {
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public ItemBeam getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if (convertView == null) {
			// convertView = mActivity.getLayoutInflater().inflate(
			// R.layout.fragment_account_home_item, null);
			// }
			//
			// convertView.findViewById(R.id.account_home_item_divider)
			// .setVisibility(View.VISIBLE);
			// convertView.findViewById(R.id.account_home_item_head)
			// .setVisibility(View.GONE);
			// convertView.findViewById(R.id.account_home_item_tail)
			// .setVisibility(View.GONE);
			// if (position == 0) {
			// convertView.findViewById(R.id.account_home_item_head)
			// .setVisibility(View.VISIBLE);
			// } else if (position == getCount() - 1) {
			// convertView.findViewById(R.id.account_home_item_divider)
			// .setVisibility(View.GONE);
			// convertView.findViewById(R.id.account_home_item_tail)
			// .setVisibility(View.VISIBLE);
			// }
			//
			// TextView textview = (TextView) convertView
			// .findViewById(android.R.id.text1);
			// ImageView iconView = (ImageView) convertView
			// .findViewById(android.R.id.icon);
			// textview.setText(getItem(position).name);
			// if (getItem(position).name == R.string.me_item_help) {
			// View view =
			// convertView.findViewById(R.id.account_home_item_phone);
			// view.setVisibility(View.VISIBLE);
			// view.setOnClickListener(AccountHomeFragment.this);
			// } else {
			// convertView.findViewById(R.id.account_home_item_phone).setVisibility(View.GONE);
			// }
			// iconView.setImageResource(getItem(position).icon);

			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.account_home_item, null);
			}
			convertView.findViewById(R.id.account_home_item_head)
					.setVisibility(View.GONE);
			arrowView = convertView.findViewById(R.id.account_home_item_top);
			arrowView.setOnClickListener(this);
			bean = getItem(position);
			if (position == 0) {
				arrowView.setTag(null);
				convertView.findViewById(R.id.account_home_item_head)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_home_item_tail_top)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_home_item_tail)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_home_item_tail)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_orders_paying)
						.setOnClickListener(this);
				convertView.findViewById(R.id.account_orders_shipping)
						.setOnClickListener(this);
				convertView.findViewById(R.id.account_orders_receiving)
						.setOnClickListener(this);
				convertView.findViewById(R.id.account_orders_recommend)
						.setOnClickListener(this);
				TextView textViewPaying = ((TextView) convertView
						.findViewById(R.id.account_orders_paying_statue));
				TextView textViewShipping = ((TextView) convertView
						.findViewById(R.id.account_orders_shipping_statue));
				TextView textViewReceiving = ((TextView) convertView
						.findViewById(R.id.account_orders_receiving_statue));
				TextView textViewRecommend = ((TextView) convertView
						.findViewById(R.id.account_orders_recommend_statue));
				if (payNum == 0){
					textViewPaying.setVisibility(View.GONE);
				} else {
					textViewPaying.setVisibility(View.VISIBLE);
					String str = String.valueOf(payNum);
//					int width = (int) getTextViewLength(textViewPaying,str)*3;
//					RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) textViewPaying
//							.getLayoutParams();
//					layoutParams.width = width;
//					layoutParams.height = width;
//					textViewPaying.setLayoutParams(layoutParams);
					textViewPaying.setText(str);
				}
				if (0 == shippingNum){
					textViewShipping.setVisibility(View.GONE);
				} else {
					textViewShipping.setVisibility(View.VISIBLE);
					String str1 = String.valueOf(shippingNum);
//					int width1 =(int) getTextViewLength(textViewShipping,str1);
//					RelativeLayout.LayoutParams layoutParams1 = (android.widget.RelativeLayout.LayoutParams) textViewPaying
//							.getLayoutParams();
//					layoutParams1.width = width1;
//					layoutParams1.height = width1;
//					textViewShipping.setLayoutParams(layoutParams1);
					textViewShipping.setText(str1);
				}
				if (0 == receivingNum){
					textViewReceiving.setVisibility(View.GONE);
				}else {
					textViewReceiving.setVisibility(View.VISIBLE);
					String str2 = String.valueOf(receivingNum);
//					int width2 = (int) getTextViewLength(textViewReceiving,str2);
//					RelativeLayout.LayoutParams layoutParams2 = (android.widget.RelativeLayout.LayoutParams) textViewPaying
//							.getLayoutParams();
//					layoutParams2.width = width2;
//					layoutParams2.height = width2;
//					textViewReceiving.setLayoutParams(layoutParams2);
					textViewReceiving.setText(str2);
				}
				if (0 == recommendNum){
					textViewRecommend.setVisibility(View.GONE);
				} else {
					textViewRecommend.setVisibility(View.VISIBLE);
					String str3 = String.valueOf(recommendNum);
//					int width3 = (int) getTextViewLength(textViewRecommend,str3);
//					RelativeLayout.LayoutParams layoutParams3 = (android.widget.RelativeLayout.LayoutParams) textViewPaying
//							.getLayoutParams();
//					layoutParams3.width = width3;
//					layoutParams3.height = width3;
//					textViewRecommend.setLayoutParams(layoutParams3);
					textViewRecommend.setText(str3);
				}
			} else if (position == 1) {
				arrowView.setTag(bean);
				convertView.findViewById(R.id.account_home_item_bottom)
						.setVisibility(View.GONE);
				convertView.findViewById(R.id.account_home_item_tail)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_home_item_tail_top)
						.setVisibility(View.VISIBLE);
			} else {
				arrowView.setTag(bean);
				convertView.findViewById(R.id.account_home_item_bottom)
						.setVisibility(View.GONE);
				convertView.findViewById(R.id.account_home_item_tail)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_home_item_tail_top)
						.setVisibility(View.GONE);
			}

			TextView textview = (TextView) convertView
					.findViewById(android.R.id.text1);
			textview.setText(getItem(position).name);
			ImageView iconView = (ImageView) convertView
					.findViewById(android.R.id.icon);
			iconView.setImageResource(getItem(position).icon);
			if (position == 3) {
				if (isLoging) {
					((TextView) convertView.findViewById(R.id.item_tip))
							.setText(mLoginedUser.getIntegral());
				} else {
					((TextView) convertView.findViewById(R.id.item_tip))
					.setText("");
				}
			} else if (position == 5) {
				if (isLoging) {
					((TextView) convertView.findViewById(R.id.item_tip))
							.setText(mLoginedUser.getMessage());
				} else {
					((TextView) convertView.findViewById(R.id.item_tip))
						.setText("");
				}
			} else if (position == 7) {
				// ((TextView) convertView.findViewById(R.id.item_tip))
				// .setText("●");
			} else {
				((TextView) convertView.findViewById(R.id.item_tip))
						.setText("");
			}
			convertView.findViewById(R.id.item_tip);
			return convertView;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				ItemBeam bean = (ItemBeam) v.getTag();
				if (bean.fragment != AgentActivity.FRAGMENT_ACCOUNT_SETTING
						&& !isLoging) {
					startActivityForResult(AgentActivity.intentForFragment(
							mActivity, AgentActivity.FRAGMENT_ACCOUNT_LOGIN),
							REQUEST_CODE_USER_LOGIN);
				} else {
					if (bean.fragment == AgentActivity.FRAGMENT_PERSONAL_HOME) {
						if (bean.name == R.string.me_item_collect) {
							startActivity(AgentActivity
									.intentForFragment(mActivity, bean.fragment)
									.putExtra(Run.EXTRA_DATA, true)
									.putExtra("userId",
											mLoginedUser.getMemberId()));
						} else {// 我的推荐
							startActivity(AgentActivity
									.intentForFragment(mActivity, bean.fragment)
									.putExtra(Run.EXTRA_DATA, false)
									.putExtra("userId",
											mLoginedUser.getMemberId()));
						}
					} else {

						startActivity(AgentActivity.intentForFragment(
								mActivity, bean.fragment));
					}
				}
			} else if (v.getId() == R.id.account_orders_receiving
					|| v.getId() == R.id.account_orders_shipping
					|| v.getId() == R.id.account_orders_paying
					|| v.getId() == R.id.account_orders_recommend) {

				if (!isLoging) {
					startActivityForResult(AgentActivity.intentForFragment(
							mActivity, AgentActivity.FRAGMENT_ACCOUNT_LOGIN),
							REQUEST_CODE_USER_LOGIN);
				} else {
					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_ACCOUNT_ORDERS).putExtra(
							Run.EXTRA_VALUE, v.getId()));
				}
			} else if (v.getId() == R.id.account_home_item_top) {
				if (!isLoging) {
					startActivityForResult(AgentActivity.intentForFragment(
							mActivity, AgentActivity.FRAGMENT_ACCOUNT_LOGIN),
							REQUEST_CODE_USER_LOGIN);
				} else {

					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_ACCOUNT_ORDERS).putExtra(
							Run.EXTRA_VALUE, true));
				}
			}
		}
	}

	private class ItemBeam {
		public int icon;
		public int name;
		public int fragment;

		public ItemBeam(int icon, int name, int fragment) {
			this.icon = icon;
			this.name = name;
			this.fragment = fragment;
		}
	}

	/**
	 * 获取线下积分
	 * 
	 * @author chanson
	 * 
	 */
	// private class GetOfflineNo implements JsonTaskHandler{
	//
	// @Override
	// public void task_response(String json_str) {
	// try {
	// JSONObject all = new JSONObject(json_str);
	// if (Run.checkRequestJson(mActivity, all)) {
	// JSONObject data = all.optJSONObject("data");
	// if (data != null && !TextUtils.isEmpty(data.optString("CardNo"))) {
	// ((ImageView) findViewById(R.id.account_header_view_qrcode))
	// .setImageBitmap(Util.CreateOneDCode(mActivity,data.optString("CardNo")));
	// }else{
	// ((ImageView)
	// findViewById(R.id.account_header_view_qrcode)).setImageBitmap(null);
	// }
	// } else {
	// ((ImageView)
	// findViewById(R.id.account_header_view_qrcode)).setImageBitmap(null);
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// @Override
	// public JsonRequestBean task_request() {
	// return new JsonRequestBean( "mobileapi.point.offline_point");
	// }
	// }
	private class GetUserInfoTask implements JsonTaskHandler {
		String strUser;

		public GetUserInfoTask(String userId) {
			strUser = userId;
		}

		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.get_member_info");
			bean.addParams("member_id", strUser);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						payNum = data.optInt("unpay_num");
						shippingNum = data.optInt("unship_num");
						receivingNum = data.optInt("unfinish_num");
						recommendNum = data.optInt("unopinions_num");
						mLoginedUser.setIntegral(data.optString("point"));
						mLoginedUser.setMessage(data.optString("messagecount"));
						mBaseHomeAdaptr.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
			}
		}
	}

	public  float getTextViewLength(TextView textView,String text){
		  TextPaint paint = textView.getPaint();
		  float textLength = paint.measureText(text);
		  return textLength;
		}
	public float getStatueWidth(String text) {
		Paint mPaint = new Paint();
		mPaint.setTextSize(13);
		float FontSpace = mPaint.getFontSpacing();
		return text.length() * FontSpace;
	}
}
