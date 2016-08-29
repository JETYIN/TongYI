package com.qianseit.westore.activity.account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.PersonalTwoAdapter;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.CommonTextView;
import com.qianseit.westore.ui.ShareView;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.ui.SharedPopupWindow;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountPersonalFragment extends BaseDoFragment implements
		ShareViewDataSource {
	private int WHTH = 0x200;
	private int WHTHRECOMMEND = 0x100;
	private int WHTHRECOMMENDVISBLE = 0x101;
	private int WHTHCOLLECT = 0x110;
	private int WHTHCOLLECTVISBLE = 0x111;
	private LayoutInflater mInflater;
	private PullToRefreshListView mRefreshListView;
	private TextView mListNullText;
	// private ImageLoader loader;
	private int mPageNum = 0;
	private boolean isNewStart = true;
	private JsonTask mTask;
	private String userId;
	private boolean isMy = true;
	private boolean isFrist = true;
	private VolleyImageLoader mVolleyImageLoader;
	private RelativeLayout mSelectView;
	private RelativeLayout mPersonalCollect;
	private RelativeLayout mPersonalRecommend;
	private boolean isCollectList = true;
	private BaseAdapter mGoodsListAdapter;
	private JSONObject userData;
	private LinearLayout topView;
	private String mUserId;
	private int width;
	private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();
	// private SimpleDateFormat sm = new SimpleDateFormat("yy-mm-dd");
	// private SimpleDateFormat sb = new SimpleDateFormat("yy-mm-dd");
	private ImageView avatarView;
	private TextView mUserLv;
	private TextView mUserName;
	private TextView mLiked;
	private TextView mRecommend;
	private TextView mFans;
	private TextView mAttention;
	private TextView mInfo;
	private ImageView sexIcon;
	private Button cancelBut;
	private LinearLayout addLinear;
	private LoginedUser mLoginedUser;
	private LinearLayout mAttentLayout;
	private ShareView mSharedView;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == WHTHRECOMMEND) {
				mListNullText.setVisibility(View.INVISIBLE);
			} else if (msg.what == WHTHRECOMMENDVISBLE) {
				mListNullText.setVisibility(View.VISIBLE);
				if (isMy)
					mListNullText.setText("你目前还没有推荐过好物哦");
				else
					mListNullText.setText("TA目前还没有推荐过好物哦");
			} else if (msg.what == WHTHCOLLECT) {
				mListNullText.setVisibility(View.INVISIBLE);
			} else if (msg.what == WHTHCOLLECTVISBLE) {
				mListNullText.setVisibility(View.VISIBLE);
				if (isMy)
					mListNullText.setText("你目前还没有收藏过好物哦");
				else
					mListNullText.setText("TA目前还没有收藏过好物哦");

			} else if (msg.what == WHTH) {
				topView.setVisibility(View.VISIBLE);
				mGoodsListAdapter.notifyDataSetChanged();
				userData = (JSONObject) msg.obj;
				mUserLv.setText("LV." + userData.optString("member_lv_id"));
				Uri avatarUri = Uri.parse(userData.optString("avatar"));
				avatarView.setTag(avatarUri);
				// loader.showImage(avatarView, avatarUri);
				mVolleyImageLoader.showImage(avatarView,
						userData.optString("avatar"));
				mUserName
						.setText("null".equals(userData.optString("name")) ? "未设置昵称"
								: userData.optString("name"));
				int sex = userData.optInt("sex");
				if (sex == 0) {
					sexIcon.setImageResource(R.drawable.home_nv);
				} else if (sex == 1) {
					sexIcon.setImageResource(R.drawable.home_nan);

				} else {
					sexIcon.setVisibility(View.GONE);
				}
				if ("0".equals(userData.optString("is_attention"))) {
					addLinear.setVisibility(View.VISIBLE);
					cancelBut.setVisibility(View.GONE);
				} else {
					addLinear.setVisibility(View.GONE);
					cancelBut.setVisibility(View.VISIBLE);
				}
				mRecommend.setText(userData.optString("opinions_num"));
				mLiked.setText(userData.optString("praise_num"));
				mFans.setText(userData.optString("fans_num"));
				mAttention.setText(userData.optString("follow_num"));
				mInfo = (TextView) topView.findViewById(R.id.personal_info);
				if ("null".equals(userData.optString("desc"))
						|| "".equals(userData.optString("desc"))) {

				} else {
					mInfo.setText(userData.optString("desc"));
				}
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_person_title);
		WindowManager wm = (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = wm.getDefaultDisplay().getWidth() - Util.dip2px(mActivity, 10);
		;
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		// loader = AgentApplication.getAvatarLoader(mActivity);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);

		mUserId = mLoginedUser.getMemberId();
		Intent mIntent = mActivity.getIntent();
		isCollectList = mIntent.getBooleanExtra(Run.EXTRA_DATA, true);
		userId = mIntent.getStringExtra("userId");
		boolean b = TextUtils.isEmpty(userId);
		if (!b) {
			isMy = TextUtils.equals(userId, mLoginedUser.getMemberId());
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_account_personal_main,
				null);
		mSharedView = (ShareView) findViewById(R.id.share_view);
		mListNullText = (TextView) findViewById(R.id.personal_listview_null);
		mRefreshListView = (PullToRefreshListView) findViewById(R.id.personal_listview);
		mPersonalCollect = (RelativeLayout) findViewById(R.id.personal_collect_rel);
		mPersonalCollect.setOnClickListener(mSaleClickListener);
		mPersonalRecommend = (RelativeLayout) findViewById(R.id.personal_recommend_rel);
		mLiked = (TextView) findViewById(R.id.personal_liked);
		mFans = (TextView) findViewById(R.id.personal_fans);
		mAttention = (TextView) findViewById(R.id.personal_attention);
		mRecommend = (TextView) findViewById(R.id.personal_recommend);
		avatarView = (ImageView) findViewById(R.id.account_personal_avatar);
		mUserLv = (TextView) findViewById(R.id.account_personal_lv);
		mUserLv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = AgentActivity
						.intentForFragment(
								getActivity(),
								AgentActivity.FRAGMENT_HELP_ARTICLE)
						.putExtra("title", "商派等级规则");
				if (isMy) {
					intent.putExtra(
							"url",
							Run.buildString(Run.DOMAIN,"/wap/statics-pointLv.html?from=app&member_id=", mLoginedUser.getMemberId()));
				} else {
					intent.putExtra(
							"url",Run.buildString(Run.DOMAIN,"/wap/statics-pointLv.html?from=app"));
				}
				getActivity().startActivity(intent);


			}
		});
		mUserName = (TextView) findViewById(R.id.account_personal_name);
		sexIcon = (ImageView) findViewById(R.id.account_personal_sex);
		findViewById(R.id.account_personal_fans_linear)
				.setOnClickListener(this);
		findViewById(R.id.account_personal_attention_linear)
				.setOnClickListener(this);
		cancelBut = (Button) findViewById(R.id.account_click_but);
		cancelBut.setOnClickListener(this);
		addLinear = (LinearLayout) findViewById(R.id.account_attention_linear);
		addLinear.setOnClickListener(this);
		mAttentLayout = (LinearLayout) findViewById(R.id.account_personal_attention);
		mAttentLayout.setOnClickListener(this);
		/*
		 * mActionBar.setRightImageButton(R.drawable.account_personal_shared,
		 * new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // SharedPopupWindow
		 * morePopWindow = new SharedPopupWindow( // mActivity); //
		 * morePopWindow // .setDataSource(AccountPersonalFragment.this); //
		 * morePopWindow.showPopupWindow(v);
		 * 
		 * // 显示分享view mSharedView.setDataSource(AccountPersonalFragment.this);
		 * mSharedView.showShareView();
		 * 
		 * } });
		 */

		if (isCollectList) {
			mSelectView = mPersonalCollect;
		} else {
			mSelectView = mPersonalRecommend;
		}
		mSelectView.setSelected(true);
		mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
		mPersonalRecommend.setOnClickListener(mSaleClickListener);
		topView = (LinearLayout) findViewById(R.id.personal_top);
		if (mLoginedUser.getMemberId().equals(userId)) {
			findViewById(R.id.account_personal_like_linear).setOnClickListener(
					this);
			mAttentLayout.setVisibility(View.GONE);
		}
		Run.removeFromSuperView(topView);
		Run.removeFromSuperView(mListNullText);
		mListNullText.setLayoutParams(new AbsListView.LayoutParams(
				mListNullText.getLayoutParams()));
		topView.setLayoutParams(new AbsListView.LayoutParams(topView
				.getLayoutParams()));
		mRefreshListView.getRefreshableView().addHeaderView(topView);
		mRefreshListView.getRefreshableView().addFooterView(mListNullText);
		mRefreshListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// JSONObject json = (JSONObject) view
						// .getTag(R.id.tag_object);
						// String goodsIID = json.optString("goods_id");
						// Intent intent = AgentActivity.intentForFragment(
						// mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
						// .putExtra(Run.EXTRA_CLASS_ID, goodsIID);
						// startActivity(intent);

					}

				});
		mRefreshListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum, false);
			}
		});
		mRefreshListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				isNewStart = true;
				loadNextPage(0, false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		topView.setVisibility(View.INVISIBLE);
		mListNullText.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		isNewStart=true;
		loadNextPage(0, true);

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	private void loadNextPage(int oldPageNum, boolean isShow) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mGoodsArray.clear();
			mListNullText.setVisibility(View.INVISIBLE);
			if (!isShow) {
				mRefreshListView.setRefreshing();
			}
			if (isNewStart) {
				Run.excuteJsonTask(new JsonTask(), new GetUserInfoTask());
				isNewStart = false;
			}
			isFrist = true;
		} else {
			isFrist = false;
			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;
		}
		mTask = new JsonTask();
		if (isCollectList) {
			if (isFrist) {
				mGoodsListAdapter = new PersonalTwoAdapter(
						AccountPersonalFragment.this, mActivity,
						mVolleyImageLoader, mGoodsArray, isMy);
				mRefreshListView.getRefreshableView().setAdapter(
						mGoodsListAdapter);
			}
			Run.excuteJsonTask(mTask, new GetCollectTask(userId, isShow));
		} else {
			if (isFrist) {
				mGoodsListAdapter = new GoodsListAdapter();
				mRefreshListView.getRefreshableView().setAdapter(
						mGoodsListAdapter);
			}
			Run.excuteJsonTask(mTask, new GetRecommendGoodsTask(userId,isShow));
		}

	}

	private OnClickListener mSaleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mSelectView.setSelected(false);
			mSelectView.getChildAt(1).setVisibility(View.GONE);
			if (v == mPersonalCollect) {
				mSelectView = mPersonalCollect;
				isCollectList = true;
			} else if (v == mPersonalRecommend) {
				mSelectView = mPersonalRecommend;
				isCollectList = false;

			}
			mSelectView.setSelected(true);
			mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
			loadNextPage(0, true);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.account_personal_fans_linear:
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_FANS).putExtra("userId", userId));
			break;
		case R.id.account_personal_like_linear:
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_PRAISE_COMMENT).putExtra(
					Run.EXTRA_DATA, false));
			break;
		case R.id.account_personal_attention_linear:
			startActivity(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ATTENTION)
					.putExtra("userId", userId));
			break;
		case R.id.account_click_but:
			showCancelableLoadingDialog();
			Run.excuteJsonTask(new JsonTask(), new CalcelAttentionTaskTask(
					userId, mLoginedUser.getMemberId()));
			break;
		case R.id.account_attention_linear:
			showCancelableLoadingDialog();
			Run.excuteJsonTask(new JsonTask(), new AddAttentionTask(userId,
					mLoginedUser.getMemberId()));
			break;
		default:
			break;
		}
	}

	// 宽
	private int getViewWidth(View view) {
		view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		return view.getMeasuredWidth();
	};

	// 高
	private int getViewHeight(View view) {
		view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		return view.getMeasuredHeight();
	};

	private class GoodsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mGoodsArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mGoodsArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CircleImageView img_brand; // 头像
			final CommonTextView textview_like; // 喜欢
			CommonTextView textview_comments; // 评论
			CommonTextView textview_time; // 日期
			CommonTextView textview_content; // 详细内容
			CommonTextView textview_title; // 内容标题
			ImageView goods_detail_images; // 详情图片
			final CommonTextView button_related; // 关注图标
			// CommonTextView textview_sTime=null; //发布日期
			CommonTextView textview_name; // 用户名
			CommonTextView textview_level; // 等级
			final ImageView textview_likes_image;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_new_listview,
						null);
				// 初始化listview的每一项的布局文件中的组件
				goods_detail_images = (ImageView) convertView
						.findViewById(R.id.imgfilter); // 详情图片
				img_brand = (CircleImageView) convertView
						.findViewById(R.id.img_brand_logos); // 头像
				textview_name = (CommonTextView) convertView
						.findViewById(R.id.textview_names); // 用户名
				textview_level = (CommonTextView) convertView
						.findViewById(R.id.textview_levels); // 等级
				button_related = (CommonTextView) convertView
						.findViewById(R.id.button_relateds); // 关注图标
				textview_title = (CommonTextView) convertView
						.findViewById(R.id.textview_titles); // 内容标题
				textview_content = (CommonTextView) convertView
						.findViewById(R.id.textview_contents); // 详情内容
				textview_time = (CommonTextView) convertView
						.findViewById(R.id.textview_times); // 日期
				textview_comments = (CommonTextView) convertView
						.findViewById(R.id.textview_commentss); // 评论
				textview_like = (CommonTextView) convertView
						.findViewById(R.id.textview_likes); // 喜欢

				textview_likes_image = (ImageView) convertView
						.findViewById(R.id.textview_likes_image); // 是否点赞
				// relaylayout=(RelativeLayout)convertView.findViewById(R.id.photo_topss);
				// 封装listview的每一项的布局文件中的组件
				convertView.setTag(new SelectsWrapper(img_brand, textview_like,
						textview_comments, textview_time, textview_content,
						textview_title, goods_detail_images, button_related,
						textview_name, textview_level, textview_likes_image));

			} else {
				SelectsWrapper dataWrapper = (SelectsWrapper) convertView
						.getTag();
				goods_detail_images = dataWrapper.goods_detail_images;
				img_brand = dataWrapper.img_brand;
				textview_level = dataWrapper.textview_level;
				button_related = dataWrapper.button_related;
				textview_title = dataWrapper.textview_title;
				textview_content = dataWrapper.textview_content;
				textview_time = dataWrapper.textview_time;
				textview_comments = dataWrapper.textview_comments;
				textview_like = dataWrapper.textview_like;
				textview_name = dataWrapper.textview_name;
				textview_likes_image = dataWrapper.textview_likes_image;
			}
			final JSONObject goodsData = getItem(position);
			textview_likes_image.setTag(goodsData);
			final CommonTextView textview_sharead = (CommonTextView) convertView
					.findViewById(R.id.textview_shareads); // 分享
			final RelativeLayout ll_position = (RelativeLayout) convertView
					.findViewById(R.id.ll_position);

			mVolleyImageLoader.showImage(img_brand,
					goodsData.optString("avatar"));
			mVolleyImageLoader.showImage(goods_detail_images,
					goodsData.optString("image_url"));
			textview_name.setText(goodsData.optString("name"));
			textview_content.setText(goodsData.optString("content"));
			textview_title.setText(goodsData.optString("goods_name"));
			JSONObject tagJSON = goodsData.optJSONObject("tag");
			if (ll_position.getChildCount() >= 2)
				ll_position.removeViewAt(1);
			if (tagJSON != null) {

				Iterator it = tagJSON.keys();
				List<String> keyListstr = new ArrayList<String>();
				while (it.hasNext()) {
					keyListstr.add(it.next().toString());
				}
				if (keyListstr.size() > 0) {
					JSONObject objTag = tagJSON
							.optJSONObject(keyListstr.get(0));

					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					float x = (float) (Float.valueOf(objTag.optString("x")) / 100.0);
					float y = (float) (Float.valueOf(objTag.optString("y")) / 100.0);
					int xx = (int) (width * x);
					int yy = (int) (Util.dip2px(mActivity, 320) * y);
					params.topMargin = yy;
					params.leftMargin = xx;
					final View view = mInflater.inflate(
							R.layout.picturetagview, null, true);
					// ll_position.removeAllViews();
					TextView tvPictureTagLabel = (TextView) view
							.findViewById(R.id.tvPictureTagLabel);
					RelativeLayout rrTag = (RelativeLayout) view
							.findViewById(R.id.loTag);
					if (objTag.optString("image_type").equals("1")) {
						rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_right);
					} else {
						rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_left);
					}
					rrTag.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							Intent intent = AgentActivity.intentForFragment(
									getActivity(),
									AgentActivity.FRAGMENT_GOODS_DETAIL)
									.putExtra(Run.EXTRA_CLASS_ID,
											goodsData.optString("goods_id"));
							startActivity(intent);

						}
					});
					tvPictureTagLabel.setText(objTag.optString("image_tag"));
					ll_position.addView(view, params);
				}

			}

			/** 分享 */
			textview_sharead.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					SharedPopupWindow morePopWindow = new SharedPopupWindow(
							mActivity);
					ShareViewDataSource dataSource = new ShareViewDataSource() {

						@Override
						public String getShareUrl() {
							// return goodsData.optString("image_url");
							return String.format(Run.RECOMMEND_URL,
									goodsData.optString("image_url"));
						}

						@Override
						public String getShareText() {
							return goodsData.optString("goods_name") + "-"
									+ goodsData.optString("content");
						}

						@Override
						public String getShareImageUrl() {
							return goodsData.optString("image");
						}

						@Override
						public String getShareImageFile() {
							return CacheUtils.getImageCacheFile(goodsData
									.optString("image_url"));
						}
					};

					morePopWindow.setDataSource(dataSource);
					morePopWindow.showPopupWindow(textview_sharead);
				}
			});

			mVolleyImageLoader.showImage(img_brand,
					goodsData.optString("brand_name"));
			mVolleyImageLoader.showImage(goods_detail_images,
					goodsData.optString("image_url"));
			textview_name.setText(goodsData.optString("name"));
			textview_content.setText(goodsData.optString("content"));
			textview_title.setText(goodsData.optString("goods_name"));
			textview_content
					.setText(Html
							.fromHtml("<font size=\"4\" color=\"red\">[好物推荐]</font><font size=\"4\" color=\"#9b9b9b\"></font>"
									+ goodsData.optString("content")));
			textview_time.setText(Conver.getFotTime(goodsData
					.optString("created")));
			button_related.setBackgroundColor(Color.parseColor("#ffffff"));
			button_related.setTextColor(Color.parseColor("#666666"));
			button_related.setText(Conver.getTopTime(goodsData
					.optString("created")));
			String pinglun = "评论(" + goodsData.optString("c_num") + ")";
			textview_comments.setText(pinglun);
			textview_like.setText(goodsData.optString("p_num"));
			textview_level.setText("LV." + goodsData.optString("member_lv_id"));

			/**
			 * 点赞 1 已点赞 0未点赞
			 */
			if (goodsData.optString("is_praise").equals("0")) {
				textview_likes_image.setImageResource(R.drawable.my_msg_praise);
			} else {
				textview_likes_image.setImageResource(R.drawable.my_new_fans);
			}
			textview_likes_image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (goodsData.optString("is_praise").equals("0")) {
						Run.excuteJsonTask(new JsonTask(), new AddPraiseTask(
								mUserId, goodsData.optString("id")));
						textview_likes_image
								.setImageResource(R.drawable.my_new_fans);
						int like = Integer.valueOf(textview_like.getText()
								.toString()) + 1;
						textview_like.setText(like + "");
						goodsData.remove("is_praise");
						try {
							goodsData.put("is_praise", 1);
							goodsData.remove("p_num");
							goodsData.put("p_num", like + "");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						Run.excuteJsonTask(
								new JsonTask(),
								new CalcelPraiseTask(mUserId, goodsData
										.optString("id")));
						textview_likes_image
								.setImageResource(R.drawable.my_msg_praise);
						int like = Integer.valueOf(textview_like.getText()
								.toString()) - 1;
						textview_like.setText(like + "");
						textview_like.setText(like + "");
						goodsData.remove("is_praise");
						try {
							goodsData.put("is_praise", 0);
							goodsData.remove("p_num");
							goodsData.put("p_num", like + "");
							Toast.makeText(mActivity, "取消点赞", 5000).show();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});

			/**
			 * 推荐详情
			 */
			goods_detail_images.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_COMMEND)
							.putExtra("id", goodsData.optString("id"))
							.putExtra(Run.EXTRA_DATA, true));
					// Intent intent = new
					// Intent(mActivity,CommentAtivity.class);
					// intent.putExtra("id",goodsData.optString("id"));
					// mActivity.startActivity(intent);
				}
			});
			/**
			 * 评论
			 */
			textview_comments.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_COMMEND)
							.putExtra("id", goodsData.optString("id"))
							.putExtra(Run.EXTRA_DATA, true));
				}
			});
			/**
			 * 跳转个人资料
			 */
			img_brand.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_PERSONAL_HOME).putExtra(
							"userId", userId));
				}
			});
			return convertView;
		}
	}

	private class ViewHolder {
		private ImageView avatar;
		private TextView nameText;
		private TextView timeText;
		private TextView lvText;
		private ImageView goodsIcon;
	}

	private class GetRecommendGoodsTask implements JsonTaskHandler {
		private boolean isShow;
		private String userId;

		public GetRecommendGoodsTask(String userId,boolean isShow) {
			this.isShow = isShow;
			this.userId=userId;
		}

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				mRefreshListView.onRefreshComplete();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray dataArray = all.optJSONArray("data");
					if (dataArray != null && dataArray.length() > 0) {
						for (int i = 0; i < dataArray.length(); i++)
							mGoodsArray.add(dataArray.optJSONObject(i));
						mGoodsListAdapter.notifyDataSetChanged();
					}
				}
				if (mGoodsArray.size() <= 0) {
					handler.sendEmptyMessage(WHTHRECOMMENDVISBLE);
				} else {
					handler.sendEmptyMessage(WHTHCOLLECT);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			if (isShow) {
				showCancelableLoadingDialog();
			}
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.getopinionsformember");
			req.addParams("page", String.valueOf(mPageNum));
			req.addParams("son_object", "json");
			req.addParams("member_id", userId);
			return req;
		}
	}

	class GetUserInfoTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				mRefreshListView.onRefreshComplete();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					if (data != null) {
						Message message = new Message();
						message.what = WHTH;
						message.obj = data;
						handler.sendMessage(message);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.get_member_info");
			req.addParams("son_object", "json");
			req.addParams("member_id", userId);
			return req;
		}
	}

	private class CalcelAttentionTaskTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;

		public CalcelAttentionTaskTask(String meberId, String fansId) {
			this.meberId = meberId;
			this.fansId = fansId;
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
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					// Run.alert(mActivity, "已取消关注");
					Run.excuteJsonTask(new JsonTask(), new GetUserInfoTask());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class AddAttentionTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;

		public AddAttentionTask(String meberId, String fansId) {
			this.meberId = meberId;
			this.fansId = fansId;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.attention");
			bean.addParams("member_id", meberId);
			bean.addParams("fans_id", fansId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					// Run.alert(mActivity, "关注成功");
					Run.excuteJsonTask(new JsonTask(), new GetUserInfoTask());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private class GetCollectTask implements JsonTaskHandler {
		private String meberId;
		private boolean isShow;

		public GetCollectTask(String meberId, boolean isShow) {
			this.meberId = meberId;
			this.isShow = isShow;
		}

		@Override
		public JsonRequestBean task_request() {
			if (isShow) {
				showCancelableLoadingDialog();
			}
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.info.favorite");
			bean.addParams("member_id", meberId);
			bean.addParams("n_page", String.valueOf(mPageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				JSONObject all = new JSONObject(json_str);
				mRefreshListView.onRefreshComplete();
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray dataArray = all.optJSONArray("data");
					if (dataArray != null && dataArray.length() > 0) {
						for (int i = 0; i < dataArray.length(); i++)
							mGoodsArray.add(dataArray.optJSONObject(i));
						mGoodsListAdapter.notifyDataSetChanged();
					}
				}
				if (mGoodsArray.size() <= 0) {
					handler.sendEmptyMessage(WHTHCOLLECTVISBLE);
				} else {
					handler.sendEmptyMessage(WHTHCOLLECT);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public String getShareText() {

		return "我是你的他";
	}

	@Override
	public String getShareImageFile() {
		String imageUrl = getShareImageUrl();
		if (!TextUtils.isEmpty(imageUrl))
			// return CacheUtils.getCacheFile(imageUrl);
			return CacheUtils.getImageCacheFile(imageUrl);
		return null;
	}

	@Override
	public String getShareImageUrl() {

		return "http://www.zhuoku.com/zhuomianbizhi/jing-car/20110221185337(9).htm";
	}

	@Override
	public String getShareUrl() {
		// TODO Auto-generated method stub
		return "http://www.zhuoku.com/zhuomianbizhi/jing-car/20110221185337(9).htm";
	}

	public class SelectsWrapper {
		CircleImageView img_brand; // 头像
		CommonTextView textview_sharead; // 分享
		CommonTextView textview_like; // 喜欢
		CommonTextView textview_comments; // 评论
		CommonTextView textview_time; // 日期
		CommonTextView textview_content; // 详细内容
		CommonTextView textview_title; // 内容标题
		ImageView goods_detail_images; // 详情图片
		CommonTextView button_related; // 关注图标
		CommonTextView textview_name; // 用户名
		CommonTextView textview_level; // 等级
		ImageView textview_likes_image; // 详情图片

		public SelectsWrapper(CircleImageView img_brand,
				CommonTextView textview_like, CommonTextView textview_comments,
				CommonTextView textview_time, CommonTextView textview_content,
				CommonTextView textview_title, ImageView goods_detail_images,
				CommonTextView button_related, CommonTextView textview_name,
				CommonTextView textview_level, ImageView textview_likes_image) {
			this.img_brand = img_brand;
			this.textview_sharead = textview_sharead;
			this.textview_like = textview_like;
			this.textview_comments = textview_comments;
			this.textview_time = textview_time;
			this.textview_content = textview_content;
			this.textview_title = textview_title;
			this.goods_detail_images = goods_detail_images;
			this.button_related = button_related;
			this.textview_name = textview_name;
			this.textview_level = textview_level;
			this.textview_likes_image = textview_likes_image;
		}
	}

	public static class Conver {

		// 把日期转为字符串
		public static String ConverToString(Date date) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.format(date);
		}

		public static String ConverFotString(Date date) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy/M/d hh:mm");
			return df.format(date);
		}

		// 把字符串转为日期
		public static Date ConverDate(String strDate) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				return df.parse(strDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		public static Date ConverFotDate(String strDate) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				return df.parse(strDate);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}

		public static String getTopTime(String time) {
			Date date = ConverDate(time);
			if (date != null) {
				return ConverToString(date);
			}
			return "";
		}

		public static String getFotTime(String time) {
			Date date = ConverFotDate(time);
			if (date != null) {
				return ConverFotString(date);
			}
			return "";
		}
	}

	class AddPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;// 商品推荐id

		public AddPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.add_opinions_praise");
			bean.addParams("member_id", meber_Id);
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Toast.makeText(mActivity, "点赞成功", 5000).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 取消点赞
	 * 
	 * @author Administrator
	 * 
	 */
	class CalcelPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;// 商品推荐id

		public CalcelPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.del_opinions_praise");
			// bean.addParams("member_id", meber_Id);
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Toast.makeText(mActivity, "已取消点赞", 5000).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mSharedView.getVisibility() == View.VISIBLE) {
				mSharedView.dismissShareView();
				return true;
			} else
				return super.onKeyDown(keyCode, event);
		} else
			return super.onKeyDown(keyCode, event);
	}
}
