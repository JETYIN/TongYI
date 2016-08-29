package com.qianseit.westore.fragment;

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
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.activity.account.FragmentCommentPraise;
import com.qianseit.westore.clipictrue.CommentAtivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.CommonTextView;
import com.qianseit.westore.ui.SharedPopupWindow;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.SelectsUtils;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;


/**
 * 关注
 * 
 * @author Administrator
 * 
 */
@SuppressLint("NewApi")
public class RelatedFragment extends BaseDoFragment {
	private PullToRefreshListView sentimeListView;
	private VolleyImageLoader mVolleyImageLoader;
	private SelectsAdapterss selectsAdapter;
	private JsonTask mTask;
	private int mPageNum;
	private String mUserId;
	private LoginedUser mLoginedUser;
	private ArrayList<SelectsUtils> mGoodsArray = new ArrayList<SelectsUtils>();
	private FragmentActivity mContext;
	private View emptyView;
	private int width;

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowHomeView(false);
		mActionBar.setShowTitleBar(false);
		mContext = getActivity();
		rootView= inflater.inflate(R.layout.fragment_sentime, container,
				false);
		sentimeListView = (PullToRefreshListView) rootView
				.findViewById(R.id.flash_sentiment_listviewsss);
		mVolleyImageLoader = ((AgentApplication) getActivity().getApplication())
				.getImageLoader();
		mLoginedUser = AgentApplication.getLoginedUser(getActivity());
		mUserId = mLoginedUser.getMemberId();

		selectsAdapter = new SelectsAdapterss(getActivity(), mGoodsArray,
				R.layout.item_new_listview, mVolleyImageLoader);
		sentimeListView.getRefreshableView().setAdapter(selectsAdapter);
		selectsAdapter.notifyDataSetChanged();
		sentimeListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {

					}

				});
		sentimeListView.setOnScrollListener(new OnScrollListener() {
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
					inte(mPageNum, false);
			}
		});
		sentimeListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				inte(0, false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		findViewById(R.id.account_loging_go).setOnClickListener(this);
		emptyView=findViewById(R.id.account_loging_null);
		

		if(!isCreate){
			inte(0,true);
			isCreate=true;
		}
		WindowManager wm = (WindowManager) getActivity()
				.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth() - Util.dip2px(mActivity, 10);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.account_loging_go){
			mActivity.startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ADD_ATTENTION));
		}
	}
	private boolean isCreate=false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(emptyView.getVisibility()==View.VISIBLE){
			inte(0,true);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();

	}

	private void inte(int dum,boolean isShow) {
		this.mPageNum = dum + 1;
		if (this.mPageNum == 1) {
			mGoodsArray.clear();
			selectsAdapter.notifyDataSetChanged();
			if(!isShow)
			sentimeListView.setRefreshing();
		} else {
			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;
		}
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new SentimeListData(isShow));
	}

	/**
	 * 最后一次从服务器上获取的数据长度，如果为5则可以加载下一页。
	 */
	private int mDataLength = 5;

	private class SentimeListData implements JsonTaskHandler {
		private JSONObject data;
		private int newQuantity = 1;
		private boolean isShow;
     public SentimeListData(boolean isShow){
    	 this.isShow=isShow;
     }
		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			sentimeListView.onRefreshComplete();
			Log.i("json:", "" + json_str);
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(json_str);
				JSONArray dataJsonArray = dataJson.getJSONArray("data");
				if (dataJsonArray == null){
					return;
				}
				emptyView.setVisibility(View.GONE);
				sentimeListView.setVisibility(View.VISIBLE);
				mDataLength = dataJsonArray.length();
				for (int i = 0; i < dataJsonArray.length(); i++) {
					SelectsUtils selectsUtils = new SelectsUtils();
					JSONObject selectsInfoAObject = dataJsonArray
							.getJSONObject(i);
					String id = selectsInfoAObject.getString("id");

					JSONObject objTag = selectsInfoAObject.getJSONObject("tag");
					if (objTag != null) {
						Iterator it = objTag.keys();
						List<String> keyListstr = new ArrayList<String>();
						while (it.hasNext()) {
							keyListstr.add(it.next().toString());
						}
						if (keyListstr.size() > 0) {
							selectsUtils.setHasTag(true);
							objTag = objTag.getJSONObject(keyListstr.get(0));
							selectsUtils.setImage_type(objTag
									.getString("image_type"));
							selectsUtils.setY(objTag.getString("y"));
							selectsUtils.setImage_tag(objTag
									.getString("image_tag"));
							selectsUtils.setX(objTag.getString("x"));
						} else {
							selectsUtils.setHasTag(false);
						}
					} else {
						selectsUtils.setHasTag(false);
					}

					selectsUtils.setId(id);
					String member_id = selectsInfoAObject
							.getString("member_id"); // 用户id
					selectsUtils.setMember_id(member_id);
					// 解析用户头像
					String head = selectsInfoAObject.getString("avatar");
					selectsUtils.setImg_brand(head);
					// 解析用户名称
					String nick = selectsInfoAObject.getString("name");
					selectsUtils.setTextview_name(nick);
					// 解析发表时间
					String time = selectsInfoAObject.getString("created");
					selectsUtils.setsTime(time);
					// 解析用户等级
					String member = selectsInfoAObject
							.getString("member_lv_id");
					selectsUtils.setTextview_level(member);
					// 是否关注
					String sGuanzhu = selectsInfoAObject
							.getString("is_attention");
					selectsUtils.setButton_related(sGuanzhu);
					// 内容图片
					String image = selectsInfoAObject.getString("image_url");
					selectsUtils.setGoods_detail_images(image);
					// 内容标题
					String goodsName = selectsInfoAObject
							.getString("goods_name");
					selectsUtils.setTextview_title(goodsName);
					// 内容
					String contentn = selectsInfoAObject.getString("content");
					selectsUtils.setTextview_content(contentn);
					// 评论数量
					String num = selectsInfoAObject.getString("c_num");
					selectsUtils.setTextview_comments(num);
					// 点赞数量
					String p_num = selectsInfoAObject.getString("p_num");
					selectsUtils.setP_num(p_num);
					// 是否点赞is_praise
					String is_praise = selectsInfoAObject
							.getString("is_praise");
					selectsUtils.setIs_praise(is_praise);
					selectsUtils.setGoodsId(selectsInfoAObject.optString("goods_id"));
					mGoodsArray.add(selectsUtils);
				}
				// selectsAdapter.addData(mGoodsArray);
				if(mGoodsArray.size()<=0){
					emptyView.setVisibility(View.VISIBLE);
					sentimeListView.setVisibility(View.GONE);
				}
				sentimeListView.onRefreshComplete();
				selectsAdapter.notifyDataSetChanged();

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			if(isShow)
				showCancelableLoadingDialog();
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.getopinions");
			jb.addParams("page", String.valueOf(mPageNum));
			return jb;
		}
	}

	public class SelectsAdapterss extends BaseAdapter {

		private Activity context;
		private VolleyImageLoader mVolleyImageLoader;
		private List<SelectsUtils> selectslistdata;
		private int itemKoubeiRecommentHead;
		private LayoutInflater iLayoutInflater;// 动态布局加载器

		public SelectsAdapterss(Activity context,
				List<SelectsUtils> selectslistdata,
				int itemKoubeiRecommentHead,
				VolleyImageLoader mVolleyImageLoader) {
			this.context = context;
			this.selectslistdata = selectslistdata;
			this.mVolleyImageLoader = mVolleyImageLoader;
			this.itemKoubeiRecommentHead = itemKoubeiRecommentHead;
			iLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return selectslistdata.size();
		}

		public void addData(List<SelectsUtils> goodsArray) {
			if (goodsArray == null)
				return;
			for (int i = 0; i < goodsArray.size(); i++) {
				this.selectslistdata.add(goodsArray.get(i));
			}
			context.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}

		@Override
		public Object getItem(int position) {
			return selectslistdata.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		class ViewHolder{
			CircleImageView img_brand; // 头像
			// 分享
			CommonTextView textview_like; // 喜欢
			CommonTextView textview_comments; // 评论
			CommonTextView textview_time; // 日期
			CommonTextView textview_content; // 详细内容
			CommonTextView textview_title; // 内容标题
			ImageView goods_detail_images; // 详情图片
			CommonTextView button_related; // 关注图标
			// CommonTextView textview_sTime=null; //发布日期
			CommonTextView textview_name; // 用户名
			CommonTextView textview_level; // 等级
			ImageView textview_likes_image;
		}
		
		ViewHolder holder;

		@Override
		public View getView(final int position, View converView,
				ViewGroup parent) {
			CircleImageView img_brand; // 头像
			// 分享
			final CommonTextView textview_like; // 喜欢
			CommonTextView textview_comments; // 评论
			CommonTextView textview_time; // 日期
			CommonTextView textview_content; // 详细内容
			CommonTextView textview_title; // 内容标题
			ImageView goods_detail_images; // 详情图片
			CommonTextView button_related; // 关注图标
			// CommonTextView textview_sTime=null; //发布日期
			CommonTextView textview_name; // 用户名
			CommonTextView textview_level; // 等级
			final ImageView textview_likes_image;
//			if(converView == null){
				converView=iLayoutInflater.inflate(itemKoubeiRecommentHead, null, false);
//				holder = new ViewHolder();
				// 初始化listview的每一项的布局文件中的组件
				goods_detail_images = (ImageView) converView
						.findViewById(R.id.imgfilter); // 详情图片
				img_brand = (CircleImageView) converView
						.findViewById(R.id.img_brand_logos); // 头像
				textview_name = (CommonTextView) converView
						.findViewById(R.id.textview_names); // 用户名
				textview_level = (CommonTextView) converView
						.findViewById(R.id.textview_levels); // 等级
				button_related = (CommonTextView) converView
						.findViewById(R.id.button_relateds); // 关注图标
				textview_title = (CommonTextView) converView
						.findViewById(R.id.textview_titles); // 内容标题
				textview_content = (CommonTextView) converView
						.findViewById(R.id.textview_contents); // 详情内容
				textview_time = (CommonTextView) converView
						.findViewById(R.id.textview_times); // 日期
				textview_comments = (CommonTextView) converView
						.findViewById(R.id.textview_commentss); // 评论
				textview_like = (CommonTextView) converView
						.findViewById(R.id.textview_likes); // 喜欢
				
				// relaylayout=(RelativeLayout)converView.findViewById(R.id.photo_topss);
				textview_likes_image = (ImageView) converView
						.findViewById(R.id.textview_likes_image); // 是否点赞
//				converView.setTag(holder);
//			} else {
//				holder = (ViewHolder) converView.getTag();
//			}
			// 封装listview的每一项的布局文件中的组件
			 final CommonTextView textview_sharead = (CommonTextView) converView
					.findViewById(R.id.textview_shareads); // 分享
			
			textview_level.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					getActivity().startActivity(AgentActivity.intentForFragment(getActivity(),
							AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", "商派等级规则")
							.putExtra("url",  Run.buildString(Run.DOMAIN,"/wap/statics-pointLv.html?from=app")));
				}
			});

			final SelectsUtils selectInfo = selectslistdata.get(position);

			final RelativeLayout ll_position = (RelativeLayout) converView
					.findViewById(R.id.ll_position);
			// ll_position.removeAllViews();
			mVolleyImageLoader.showImage(img_brand, selectInfo.getImg_brand());
			mVolleyImageLoader.showImage(goods_detail_images,
					selectInfo.getGoods_detail_images());
			textview_name.setText(selectInfo.getTextview_name());
			textview_content.setText(selectInfo.getTextview_content());
			textview_title.setText(selectInfo.getTextview_title());
			if(ll_position.getChildCount()<2)
				if (selectInfo.isHasTag()) {
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					float x = (float) (Float.valueOf(selectInfo.getX()) / 100.0);
					float y = (float) (Float.valueOf(selectInfo.getY()) / 100.0);
					int xx = (int) (width * x);
					int yy = (int) (Util.dip2px(mActivity, 320) * y);
					params.topMargin = yy;
					params.leftMargin = xx;
					View view = LayoutInflater.from(context).inflate(
							R.layout.picturetagview, null, true);
					TextView tvPictureTagLabel = (TextView) view
							.findViewById(R.id.tvPictureTagLabel);
					RelativeLayout rrTag = (RelativeLayout) view
							.findViewById(R.id.loTag);
					tvPictureTagLabel.setText(selectInfo.getImage_tag());
					if (selectInfo.getImage_type().equals("1")) {
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
											selectInfo.getGoodsId());
							startActivity(intent);

						}
					});
					ll_position.addView(view, params);

				}
			
				img_brand.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					startActivity(AgentActivity
							.intentForFragment(getActivity(),
									AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra("userId", selectInfo.getMember_id()));
				}
			});

				textview_name.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					startActivity(AgentActivity
							.intentForFragment(getActivity(),
									AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra("userId", selectInfo.getMember_id()));
				}
			});

			/** 分享 */
				textview_sharead.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					SharedPopupWindow morePopWindow = new SharedPopupWindow(
							context);
					ShareViewDataSource dataSource = new ShareViewDataSource() {

						@Override
						public String getShareUrl() {
							return String.format(Run.RECOMMEND_URL,selectInfo.getId());
						}

						@Override
						public String getShareText() {
							return selectInfo.getTextview_title()+"-"+selectInfo.getTextview_content();
						}

						@Override
						public String getShareImageUrl() {
							return selectInfo.getGoods_detail_images();
						}

						@Override
						public String getShareImageFile() {
							return CacheUtils.getImageCacheFile(getShareImageUrl());
						}
					};

					morePopWindow.setDataSource(dataSource);
					morePopWindow.showPopupWindow(textview_sharead);
				}
			});

			mVolleyImageLoader.showImage(img_brand, selectInfo.getImg_brand());
			mVolleyImageLoader.showImage(goods_detail_images,
					selectInfo.getGoods_detail_images());
			textview_name.setText(selectInfo.getTextview_name());
			textview_content.setText(selectInfo.getTextview_content());
			textview_title.setText(selectInfo.getTextview_title());

			Log.i("tentinet-->", "" + selectInfo.getButton_related());
			// if(selectInfo.getButton_related().equals("0")){
//			String timase = selectInfo.getsTime();
//			
//			String time[] = timase.split(" ");
			button_related.setText(getSendTime(selectInfo.getsTime()));
			button_related.setBackgroundResource(R.drawable.bais);
			button_related.setTextColor(Color.BLACK);

			textview_content
					.setText(Html
							.fromHtml("<font size=\"4\" color=\"red\">[好物推荐]</font><font size=\"4\" color=\"#9b9b9b\"></font>"
									+ selectInfo.getTextview_content()));
			textview_time.setText(selectInfo.getsTime());
			String pinglun = "评论(" + selectInfo.getTextview_comments() + ")";
			textview_comments.setText(pinglun);
			textview_like.setText(selectInfo.getP_num());
			if(!selectInfo.getTextview_level().equals("null"))
			textview_level.setText("LV." + selectInfo.getTextview_level());

			/**
			 * 点赞 1 已点赞 0未点赞
			 */
			if (selectInfo.getIs_praise().equals("0")) {
				textview_likes_image.setImageResource(R.drawable.my_msg_praise);
			} else {
				textview_likes_image.setImageResource(R.drawable.my_new_fans);
			}
			textview_likes_image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					LoginedUser mLoginedUser = AgentApplication.getLoginedUser(mContext);
					if(mLoginedUser==null){
						startActivity(AgentActivity.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
						return;}
					mUserId = mLoginedUser.getMemberId();
					if(mUserId==null||mUserId.equals("")){
						startActivity(AgentActivity.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
						return;
					}
					
					
					if (selectInfo.getIs_praise().equals("0")) {
						Run.excuteJsonTask(new JsonTask(), new AddPraiseTask(
								mUserId, selectInfo.getId()));
						textview_likes_image
								.setImageResource(R.drawable.my_new_fans);
						int like = Integer.valueOf(textview_like.getText()
								.toString()) + 1;
						textview_likes_image.setClickable(false);
						textview_like.setText(like + "");
						selectInfo.setIs_praise("1");
						selectInfo.setP_num(like + "");
						selectslistdata.remove(position);
						selectslistdata.add(position, selectInfo);
						//Toast.makeText(context, "点赞成功", 5000).show();
						textview_likes_image.setClickable(true);
					} else {
						Run.excuteJsonTask(
								new JsonTask(),
								new CalcelPraiseTask(mUserId, selectInfo
										.getId()));
						textview_likes_image
								.setImageResource(R.drawable.my_msg_praise);
						selectInfo.setIs_praise("0");
						int like = Integer.valueOf(textview_like.getText()
								.toString()) - 1;
						textview_likes_image.setClickable(false);
						textview_like.setText(like + "");
						selectInfo.setP_num(like + "");
						selectslistdata.remove(position);
						selectslistdata.add(position, selectInfo);
						//Toast.makeText(context, "取消点赞", 5000).show();
						textview_likes_image.setClickable(true);
					}
				}
			});

			goods_detail_images.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					context.startActivity(AgentActivity.intentForFragment(context,
							AgentActivity.FRAGMENT_COMMEND).putExtra("id", selectInfo.getId()));
				}
			});
			textview_comments.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					context.startActivity(AgentActivity.intentForFragment(context,
							AgentActivity.FRAGMENT_COMMEND).putExtra("id", selectInfo.getId()));
				}
			});
			/**
			 * 跳转个人资料
			 */
			img_brand.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startActivity(AgentActivity.intentForFragment(context,
							AgentActivity.FRAGMENT_PERSONAL_HOME).putExtra(
							"userId", selectInfo.getMember_id()));
				}
			});
			return converView;
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
				showCancelableLoadingDialog();
				JsonRequestBean bean = new JsonRequestBean(
						"mobileapi.goods.add_opinions_praise");
				bean.addParams("member_id", meber_Id);
				bean.addParams("opinions_id", opinions_Id);
				return bean;
			}

			@Override
			public void task_response(String json_str) {
				hideLoadingDialog_mt();
				try {
					JSONObject all = new JSONObject(json_str);
					if (Run.checkRequestJson(context, all)) {
						Log.i("tentinet:zan:", "" + json_str);
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
				showCancelableLoadingDialog();
				JsonRequestBean bean = new JsonRequestBean(
						"mobileapi.goods.del_opinions_praise");
				// bean.addParams("member_id", meber_Id);
				bean.addParams("opinions_id", opinions_Id);
				return bean;
			}

			@Override
			public void task_response(String json_str) {
				hideLoadingDialog_mt();
				try {
					JSONObject all = new JSONObject(json_str);
					if (Run.checkRequestJson(context, all)) {
						Log.i("tentinet:quxiaozan:", "" + json_str);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public class SelectsWrapper {
			CircleImageView img_brand; // 头像
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
					CommonTextView textview_like,
					CommonTextView textview_comments,
					CommonTextView textview_time,
					CommonTextView textview_content,
					CommonTextView textview_title,
					ImageView goods_detail_images,
					CommonTextView button_related,
					CommonTextView textview_name,
					CommonTextView textview_level,
					ImageView textview_likes_image) {
				this.img_brand = img_brand;
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

}