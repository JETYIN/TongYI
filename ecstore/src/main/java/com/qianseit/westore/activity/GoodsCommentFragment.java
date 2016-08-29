package com.qianseit.westore.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.FragmentCommentPraise;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.CommendPopupWindow;
import com.qianseit.westore.ui.CommonTextView;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.ui.SharedPopupWindow;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.ChooseUtils;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class GoodsCommentFragment extends BaseDoFragment {
	private int mPageNum;
	private JsonTask mTask;
	private LayoutInflater mInflater;
	private BaseAdapter mCommendAdapter;
	private ListView mCommendListView;
	private VolleyImageLoader mVolleyImageLoader;
	private ArrayList<JSONObject> mCommentArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mWebArray = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mEndArray = new ArrayList<JSONObject>();
	private ImageView sImageView;
	private ImageButton sBack;
	private MyClick click;
	private boolean isEnd = false;
	private RelativeLayout picture;
	private ChooseUtils chooseInfo;
	private String goodsId;
	private String imagePath;
	private FragmentActivity mContext;
	private TextView mPhoto_album_title;
	private TextView mRatingNum;
	private LinearLayout mLinearLayout;
	private String id;
	private String mId;
	private int mCommendNum;
	private LinearLayout mLinearLayout2;
	private RelativeLayout mRl_position;
	private CommonTextView mShare;
	private TextView mLike;
	private EditText mEt_comment;
	private Button mSendBut;
	private ImageView mImage;
	private String mUserId;
	public JsonTask mTask1;
	public int width;
	public LoginedUser mLoginedUser;
	public int imageWidth;
	private WindowManager wm;
	private JSONArray mPraseArray;
	private boolean isPerson = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		if (mLoginedUser != null)
			mUserId = mLoginedUser.getMemberId();
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		mActionBar.setShowTitleBar(true);
		mActionBar.setTitle("评价");
		Intent intent = getActivity().getIntent();
		isPerson = intent.getBooleanExtra(Run.EXTRA_DATA, false);
		mActionBar.setRightImageButton(R.drawable.recommend_mor,
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						MyCommendPopupWindow morePopWindow = new MyCommendPopupWindow(
								mActivity);
						morePopWindow.showPopupWindow(v);
					}
				});
		mActionBar.setShowRightButton(isPerson);
		mId = intent.getStringExtra("id");
		wm = getActivity().getWindowManager();
		DisplayMetrics display = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(display);
		width = display.widthPixels - Util.dip2px(mContext, 10);;
		imageWidth = width / 13;

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_goods_commend_main, null);
		initView();
		mCommendListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;
				if (totalItemCount == (firstVisibleItem + visibleItemCount)) {
					isEnd = true;
				} else {
					isEnd = false;
				}
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 1)
					loadNextPage(mPageNum);
			}
		});
		loadNextPage(0);
		findViewById(R.id.view_loading).setVisibility(View.VISIBLE);
		findViewById(R.id.rr_comm).setVisibility(View.GONE);
		Run.excuteJsonTask(new JsonTask(), new ShopDetailData());
	}

	private void loadNextPage(int oldPageNum) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mCommentArray.clear();
			mCommendAdapter.notifyDataSetChanged();
		} else {
			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;
		}
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetCommendTask(mId));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		click = new MyClick();
		mCommendListView = (ListView) findViewById(R.id.comment_goods_listview);
		sImageView = (ImageView) findViewById(R.id.imgfilter);
		sBack = (ImageButton) findViewById(R.id.action_bar_titlebar_lefts);
		chooseInfo = (ChooseUtils) getActivity().getIntent()
				.getSerializableExtra(getString(R.string.intent_key_chooses));
		picture = (RelativeLayout) findViewById(R.id.picturess);
		picture.setDrawingCacheEnabled(true);
		mPhoto_album_title = (TextView) findViewById(R.id.photo_album_title);
		mRatingNum = (TextView) findViewById(R.id.pingpai_rating_num);
		mRl_position = (RelativeLayout) findViewById(R.id.ll_position);
		sBack.setOnClickListener(click);
		mShare = (CommonTextView) findViewById(R.id.textview_shareads);
		mLike = (TextView) findViewById(R.id.textview_likes);
		mImage = (ImageView) findViewById(R.id.textview_likes_image);
		mEt_comment = (EditText) findViewById(R.id.et_comment);
		mSendBut = ((Button) findViewById(R.id.send));
		mSendBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mUserId == null) {
					Toast.makeText(getActivity(), "请登录", 1).show();
					return;
				}
				if (mEt_comment.getText().toString().trim().equals("")) {
					Toast.makeText(getActivity(), "请输入评论内容", 1).show();
					return;
				}
				mSendBut.setEnabled(false);
				Run.excuteJsonTask(new JsonTask(), new SendCommentData(
						mEt_comment.getText().toString()));

			}
		});
		View topView = findViewById(R.id.goods_comment_top);
		topView.setLayoutParams(new AbsListView.LayoutParams(topView
				.getLayoutParams()));
		Run.removeFromSuperView(topView);
		mCommendListView.addHeaderView(topView);
		mCommendAdapter = new CommendAdapter();
		mCommendListView.setAdapter(mCommendAdapter);

	}

	public class MyClick implements OnClickListener {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.action_bar_titlebar_lefts:
				getActivity().finish();
				break;

			}
		}

	}

	private class CommendAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mCommentArray.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mCommentArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.commend_item, null);
			}
			final JSONObject dataJsonObject = getItem(position);
			CircleImageView roundImage = (CircleImageView) convertView
					.findViewById(R.id.iv_head);
			TextView tv_nikeName = (TextView) convertView
					.findViewById(R.id.tv_nikename);
			TextView tv_comtent = (TextView) convertView
					.findViewById(R.id.tv_comtent);
			TextView tv_time = (TextView) convertView
					.findViewById(R.id.tv_time);
			mVolleyImageLoader.showImage(roundImage,
					dataJsonObject.optString("avatar"));
			roundImage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(AgentActivity
							.intentForFragment(getActivity(),
									AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra("userId",
									dataJsonObject.optString("member_id")));

				}
			});
			tv_nikeName.setText(dataJsonObject.optString("name"));
			tv_comtent.setText(dataJsonObject.optString("content"));
			tv_time.setText(getSendTime(dataJsonObject.optString("created")));
			return convertView;
		}

	}

	SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String getSendTime(String creadTime) {
		Date begin;
		try {
			begin = dfs.parse(creadTime);
			return FragmentCommentPraise.getFormatTime(begin.getTime() / 1000);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return "";
	}

	private class SendCommentData implements JsonTaskHandler {
		private String content;

		public SendCommentData(String content) {
			this.content = content;
		}

		@Override
		public void task_response(String json_str) {
			JSONObject all;
			try {
				all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext, all)) {
					mCommendNum++;
					mRatingNum.setText("共有" + mCommendNum + "条评论");
					mSendBut.setEnabled(true);
					Run.alert(getActivity(), "评论成功");
					JSONObject mCommendJson = new JSONObject();
					mCommendJson.put("member_id", mUserId);
					mCommendJson.put("name",
							mLoginedUser.getNickName(getActivity()));
					mCommendJson.put("avatar", mLoginedUser.getAvatarUri());
					mCommendJson.put("content", content);
					Date date = new Date(System.currentTimeMillis());
					SimpleDateFormat sf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					mCommendJson.put("created", sf.format(date));
					mCommentArray.add(mCommendJson);
					mCommendAdapter.notifyDataSetChanged();
					mEt_comment.setText("");
					if (isEnd)
						mCommendListView.setSelection(mCommentArray.size());
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.add_opinions_comment");
			jb.addParams("member_id", mUserId);
			jb.addParams("opinions_id", mId);
			jb.addParams("content", content);

			return jb;
		}
	}

	/**
	 * 点赞
	 * 
	 * @author Administrator
	 * 
	 */
	private class AddPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;// 商品推荐id

		public AddPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.add_opinions_praise");
			bean.addParams("member_id", meber_Id);
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext, all)) {
					Toast.makeText(getActivity(), "点赞成功", 5000).show();
					Run.excuteJsonTask(new JsonTask(), new ShopDetailData());
					// JSONObject jsonObject=new JSONObject();
					// jsonObject.put("member_id", mLoginedUser.getMemberId());
					// jsonObject.put("avatar", mLoginedUser.getAvatarUri());
					// mPraseArray.put(jsonObject);
					// initPrase(mPraseArray);
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
	private class CalcelPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;// 商品推荐id

		public CalcelPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.del_opinions_praise");
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext, all)) {
					Toast.makeText(getActivity(), "已取消点赞", 5000).show();
					Run.excuteJsonTask(new JsonTask(), new ShopDetailData());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class ShopDetailData implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			if (json_str == null || json_str.length() < 60)
				return;
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(json_str);
				JSONObject dataJsonObject = dataJson.getJSONObject("data");
				goodsId=dataJsonObject.getString("goods_id");
				if (dataJsonObject != null) {
					initData(dataJsonObject);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.get_opinions_info");
			jb.addParams("opinions_id", mId);
			return jb;
		}
	}

	private class GetCommendTask implements JsonTaskHandler {
		private String strId;

		public GetCommendTask(String strId) {
			this.strId = strId;
		}

		@Override
		public void task_response(String json_str) {
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(json_str);
				JSONArray dataArray = dataJson.getJSONArray("data");
				if (dataArray != null && dataArray.length() > 0) {
					for (int i = 0; i < dataArray.length(); i++)
						mWebArray.add(dataArray.optJSONObject(i));
					mCommentArray.clear();
					mCommentArray.addAll(mWebArray);
					mCommendAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.get_comment_list");
			jb.addParams("opinions_id", strId);
			jb.addParams("page", String.valueOf(mPageNum));
			jb.addParams("limit", String.valueOf(20));
			return jb;
		}
	}

	private void initData(final JSONObject obj) throws JSONException {
		JSONObject objTag = obj.getJSONObject("tag");
		final String goodId = obj.getString("goods_id");
		if (objTag != null) {
			findViewById(R.id.view_loading).setVisibility(View.GONE);
			findViewById(R.id.rr_comm).setVisibility(View.VISIBLE);

			Iterator it = objTag.keys();
			List<String> keyListstr = new ArrayList<String>();
			while (it.hasNext()) {
				keyListstr.add(it.next().toString());
			}
			if (keyListstr.size() > 0) {
				objTag = objTag.getJSONObject(keyListstr.get(0));
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				float width = Float.valueOf(wm.getDefaultDisplay().getWidth());
				float x = (float) (Float.valueOf(objTag.getString("x")) / 100.0);
				float y = (float) (Float.valueOf(objTag.getString("y")) / 100.0);
				int xx = (int) (width * x);
				int yy = (int) (width * y);
				params.topMargin = yy;
				params.leftMargin = xx;
				View view = mInflater.inflate(R.layout.picturetagview, null,
						true);
				TextView tvPictureTagLabel = (TextView) view
						.findViewById(R.id.tvPictureTagLabel);
				RelativeLayout rrTag = (RelativeLayout) view
						.findViewById(R.id.loTag);
				tvPictureTagLabel.setText(objTag.getString("image_tag"));
				if (objTag.getString("image_type").equals("1")) {
					rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_right);
				} else {
					rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_left);
				}
				mRl_position.addView(view, params);
				/**
				 * 跳转至商品详情
				 */
				rrTag.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent intent = AgentActivity.intentForFragment(
								getActivity(),
								AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
								Run.EXTRA_CLASS_ID, goodId);
						startActivity(intent);
					}
				});
			}

		}
		mCommendNum = obj.optInt("c_num");
		mRatingNum.setText("共有" + mCommendNum + "条评论");
		mPhoto_album_title
				.setText(Html.fromHtml("<font size=\"6\" color=\"#666666\">"
						+ obj.getString("goods_name")
						+ "</font><br/><font size=\"4\" color=\"red\">[好物推荐]</font><font size=\"4\" color=\"#9b9b9b\">"
						+ obj.getString("content") + "</font>"));
		mLike.setText(obj.getString("p_num"));

		/**
		 * 点赞 1 已点赞 0未点赞
		 */
		if (obj.getString("is_praise").equals("0")) {
			mImage.setImageResource(R.drawable.my_msg_praise);
		} else {
			mImage.setImageResource(R.drawable.my_new_fans);
		}
		mShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SharedPopupWindow morePopWindow = new SharedPopupWindow(
						mContext);
				ShareViewDataSource dataSource = new ShareViewDataSource() {

					@Override
					public String getShareUrl() {
						// try {
						// return obj.getString("image_url");
						// } catch (JSONException e) {
						// e.printStackTrace();
						// }
						return String.format(Run.RECOMMEND_URL,
								obj.optString("id"));
					}

					@Override
					public String getShareText() {
						try {
							return obj.getString("goods_name")+"-"+obj.getString("content");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public String getShareImageUrl() {
						try {
							return obj.getString("image_url");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public String getShareImageFile() {
						return CacheUtils.getImageCacheFile(obj
								.optString("image_url"));
					}
				};

				morePopWindow.setDataSource(dataSource);
				morePopWindow.showPopupWindow(mShare);
			}
		});
		findViewById(R.id.ll_dianzan).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					LoginedUser mLoginedUser = AgentApplication
							.getLoginedUser(mContext);
					if (mLoginedUser == null) {
						startActivity(AgentActivity
								.intentForFragment(mActivity,
										AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
						return;
					}
					mUserId = mLoginedUser.getMemberId();
					if (mUserId == null || mUserId.equals("")) {
						startActivity(AgentActivity
								.intentForFragment(mActivity,
										AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
						return;
					}
					if (obj.getString("is_praise").equals("0")) {
						Run.excuteJsonTask(new JsonTask(), new AddPraiseTask(
								mUserId, mId));
						mImage.setImageResource(R.drawable.my_new_fans);
						int like = Integer.valueOf(mLike.getText().toString()) + 1;
						mLike.setText(like + "");
						obj.put("is_praise", "1");
					} else {
						Run.excuteJsonTask(new JsonTask(),
								new CalcelPraiseTask(mUserId, mId));
						mImage.setImageResource(R.drawable.my_msg_praise);
						int like = Integer.valueOf(mLike.getText().toString()) - 1;
						mLike.setText(like + "");
						obj.put("is_praise", like + "");
						obj.put("is_praise", "0");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		if (null != obj.getString("image_url")) {
			mVolleyImageLoader
					.showImage(sImageView, obj.getString("image_url"));
			if (obj.has("praise")) {
				mPraseArray = obj.getJSONArray("praise");
				if (mPraseArray != null && mPraseArray.length() > 0)
					initPrase(mPraseArray);

			}
		}

	}

	public void initPrase(JSONArray array) {
		mLinearLayout = (LinearLayout) findViewById(R.id.main_ll);
		mLinearLayout.removeAllViews();
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonParse = array.optJSONObject(i);
			if (i == array.length()) {
			} else {
				final View convertView = mInflater.inflate(
						R.layout.comment_personal_item, null);
				CircleImageView iv = (CircleImageView) convertView
						.findViewById(R.id.circle_imageview);
				LayoutParams params = new RelativeLayout.LayoutParams(
						imageWidth, imageWidth);
				iv.setLayoutParams(params);
				String header=jsonParse.optString("avatar");
				if("".equals(header)){
					iv.setImageResource(R.drawable.account_avatar);
				}else{
				mVolleyImageLoader.showImage(iv,header);
				}
				convertView.setTag(jsonParse);
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						try {
							JSONObject obj = (JSONObject) arg0.getTag();
							String member_id = obj.getString("member_id");
							startActivity(AgentActivity.intentForFragment(
									mActivity,
									AgentActivity.FRAGMENT_PERSONAL_HOME)
									.putExtra("userId", member_id));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				mLinearLayout.addView(convertView);
			}

		}
		if (array.length() == 10) {
			ImageView imageView = new ImageView(mActivity);
			LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, imageWidth);
			imageView.setLayoutParams(params);
			imageView.setPadding(20, 10, 20, 10);
			imageView.setImageResource(R.drawable.account_add_right);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_PRAISE).putExtra("id", mId));
				}
			});
			mLinearLayout.addView(imageView);
		}
	}

	public class MyCommendPopupWindow extends CommendPopupWindow {

		public MyCommendPopupWindow(Activity context) {
			super(context);
		}

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.account_top1) {
				Run.excuteJsonTask(new JsonTask(), new DelectTask());
			} else if (v.getId() == R.id.account_top2) {
				Run.excuteJsonTask(new JsonTask(), new ToTopTask());
			}
			dismiss();
		}

	}

	public class DelectTask implements JsonTaskHandler {

		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			// if (isShowDialog) {
			// activity.showCancelableLoadingDialog();
			// }
			JsonRequestBean jrb = new JsonRequestBean(
					"mobileapi.goods.del_opinions");
			jrb.addParams("opinions_id", mId);
			return jrb;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity,"删除成功");
					getActivity().finish();
				}
			} catch (Exception e) {
			}
		}
	}

	public class ToTopTask implements JsonTaskHandler {

		public JsonRequestBean task_request() {
            showCancelableLoadingDialog();
			JsonRequestBean jrb = new JsonRequestBean(
					"mobileapi.goods.top_opinions");
			jrb.addParams("opinions_id",mId);
			return jrb;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity,"置顶成功");
					getActivity().finish();
				}
			} catch (Exception e) {
			}
		}
	}
}
