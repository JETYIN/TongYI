package com.qianseit.westore.clipictrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CommonTextView;
import com.qianseit.westore.ui.RoundImage;
import com.qianseit.westore.ui.SharedPopupWindow;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.util.ChooseUtils;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class CommentAtivity extends Activity {

	private ImageView sImageView;
	private ImageButton sBack;
	private MyClick click;
	@SuppressWarnings("unused")
	private RelativeLayout picture;
	private ChooseUtils chooseInfo;
	private String imagePath;
	private CommentAtivity mContext;
	private TextView mPhoto_album_title;
	private TextView mRatingNum;
	private LinearLayout mLinearLayout;
	//private PullToRefreshListView mListView;
	private JsonTask mTask;
	private String id;
	private VolleyImageLoader mVolleyImageLoader;
	private String mId;   
	private LinearLayout mLinearLayout2;
	private RelativeLayout mRl_position;
	private CommonTextView mShare;
	private TextView mLike;
	private EditText mEt_comment;
	private ImageView mImage;
	private String mUserId;
	public JsonTask mTask1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fragment_account_comment);
		mContext = this;
		LoginedUser mLoginedUser = AgentApplication.getLoginedUser(mContext);
		mUserId = mLoginedUser.getMemberId();
		initView();
		getIntentContent();
		getData();  
	}
	
	private void getIntentContent() { 
		
		
		if (this.getIntent().getExtras() != null) {
		   mId = getIntent().getStringExtra("id");
		}

	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		click = new MyClick();
		sImageView = (ImageView) this.findViewById(R.id.imgfilter);
		sBack = (ImageButton) this.findViewById(R.id.action_bar_titlebar_lefts);

		chooseInfo = (ChooseUtils) this.getIntent().getSerializableExtra(
				getString(R.string.intent_key_chooses));
		picture = (RelativeLayout) this.findViewById(R.id.picturess);
		picture.setDrawingCacheEnabled(true);
		mPhoto_album_title = (TextView) this
				.findViewById(R.id.photo_album_title);
		mRatingNum = (TextView) this
				.findViewById(R.id.pingpai_rating_num);
		//mListView = (PullToRefreshListView) findViewById(R.id.flash_sale_listviews);
	    mRl_position = (RelativeLayout)this.findViewById(R.id.ll_position);
		sBack.setOnClickListener(click);
		mVolleyImageLoader = ((AgentApplication) getApplication())
				.getImageLoader();
		mShare = (CommonTextView) this.findViewById(R.id.textview_shareads);
		mLike = (TextView) this.findViewById(R.id.textview_likes);
		mImage = (ImageView) this.findViewById(R.id.textview_likes_image);
		mEt_comment = (EditText) this.findViewById(R.id.et_comment);
		((TextView) this.findViewById(R.id.send)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mUserId==null){Toast.makeText(mContext, "请登录", 1).show();return;}
				if(mEt_comment.getText().toString().trim().equals(""))
				{Toast.makeText(mContext, "请输入评论内容", 1).show();return;}
				InputMethodManager imm = (InputMethodManager) getApplication()
					    .getSystemService(Context.INPUT_METHOD_SERVICE);
					  // imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
					  if (imm.isActive())  //一直是true
					   imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					     InputMethodManager.HIDE_NOT_ALWAYS);

				if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
					return;
				mTask = new JsonTask();
				Run.excuteJsonTask(mTask, new SendCommentData());
				
			}
		});
		
		
		
	}

	/**
	 * 获取数据
	 */
	private void getData() {

		if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
			return;
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new ShopDetailData());

	}

	/**
	 * 初始化数据
	 * 
	 * @throws JSONException
	 */
	private void initData(final JSONObject obj) throws JSONException {
		JSONObject objTag = obj.getJSONObject("tag");
		if (objTag != null) {
			Iterator it = objTag.keys();
			List<String> keyListstr = new ArrayList<String>();
			while (it.hasNext()) {
				keyListstr.add(it.next().toString());
			}
			if (keyListstr.size() > 0) {
				objTag = objTag.getJSONObject(keyListstr.get(0));
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				WindowManager wm = (WindowManager) this
						.getSystemService(Context.WINDOW_SERVICE);
				float width = Float.valueOf(wm.getDefaultDisplay().getWidth());
				float x = (float) (Float.valueOf(objTag.getString("x")) / 100.0);
				float y = (float) (Float.valueOf(objTag.getString("y")) / 100.0);
				int xx = (int)(width*x);
				int yy = (int)(width*y);
				params.topMargin = yy;
				params.leftMargin =xx;
				View view =LayoutInflater.from(this).inflate(R.layout.picturetagview, null,
						true);
				TextView tvPictureTagLabel =(TextView)view.findViewById(R.id.tvPictureTagLabel);
				RelativeLayout rrTag =(RelativeLayout)view.findViewById(R.id.loTag);
				tvPictureTagLabel.setText(objTag.getString("image_tag"));
				if(objTag.getString("image_type").equals("1")){
					rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_right);
				}else{
					rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_left);
				}
				mRl_position.addView(view, params);
				/**
				 * 跳转至商品详情
				 */
				rrTag.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						
						Intent intent = AgentActivity.intentForFragment(CommentAtivity.this, AgentActivity.FRAGMENT_GOODS_DETAIL)
								.putExtra(Run.EXTRA_CLASS_ID, id);
						startActivity(intent);
					}   
				});  
			} 
//			final String goodsIID =objTag.getString("goods_id");

			
			
			
		} 
		
		mPhoto_album_title
				.setText(Html    
						.fromHtml("<font size=\"6\" color=\"#666666\">"+obj.getString("name")+"</font><br/><font size=\"4\" color=\"red\">[好物推荐]</font><font size=\"4\" color=\"#9b9b9b\">"+obj.getString("content")+"</font>"));
		mLike.setText(obj.getString("p_num"));
		/**
		 * 点赞  1 已点赞  0未点赞
		 */
		if(obj.getString("is_praise").equals("0")){
			mImage.setImageResource(R.drawable.my_msg_praise);
		}else{
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
							try {
								return obj.getString("image_url");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}

						@Override
						public String getShareText() {
							try {
								return obj.getString("content");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}

						@Override
						public String getShareImageUrl() {
							try {
								return obj.getString("image_url");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						}

						@Override
						public String getShareImageFile() {
							// TODO Auto-generated method stub
							return null;
						}
					};

					morePopWindow.setDataSource(dataSource);
					morePopWindow.showPopupWindow(mShare);
				}
			});
		findViewById(R.id.ll_dianzan).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try{
					LoginedUser mLoginedUser = AgentApplication.getLoginedUser(mContext);
					if(mLoginedUser==null){
						startActivity(AgentActivity.intentForFragment(CommentAtivity.this,
								AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
						return;}
					mUserId = mLoginedUser.getMemberId();
					if(mUserId==null||mUserId.equals("")){
						startActivity(AgentActivity.intentForFragment(CommentAtivity.this,
								AgentActivity.FRAGMENT_ACCOUNT_LOGIN));
						return;
					}
				
				if(obj.getString("is_praise").equals("0")){
					Run.excuteJsonTask(new JsonTask(), new AddPraiseTask(mUserId,mId));
					mImage.setImageResource(R.drawable.my_new_fans);
					int like = Integer.valueOf(mLike.getText().toString())+1;
					mLike.setText(like+"");
					obj.put("is_praise", "1");
//					selectInfo.setIs_praise("1");
//					selectInfo.setTextview_like(like+"");
//					selectslistdata.add(position,selectInfo );
					Toast.makeText(CommentAtivity.this, "点赞成功", 5000).show();
				}else{
					Run.excuteJsonTask(new JsonTask(), new CalcelPraiseTask(mUserId,mId));
					mImage.setImageResource(R.drawable.my_msg_praise);
//					selectInfo.setIs_praise("0");
					int like = Integer.valueOf(mLike.getText().toString())-1;
					mLike.setText(like+"");
					obj.put("is_praise", like+"");
					obj.put("is_praise", "0");
//					selectInfo.setTextview_like(like+"");
//					selectslistdata.add(position,selectInfo );
					Toast.makeText(CommentAtivity.this, "取消点赞", 5000).show();
				}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		
		if (null != obj.getString("image_url")) {
			
			mVolleyImageLoader.showImage(sImageView, obj.getString("image_url"));
			if(obj.has("praise")){
				 JSONArray array = obj.getJSONArray("praise");
				  if(array!=null&&array.length()>0)
				  initPrase(array);
				
			}
			if(obj.has("comment")){
			  JSONArray array = obj.getJSONArray("comment");
			  if(array!=null&&array.length()>0)
			  initComentView(array);
			}
		}
		
		
	}


	


	/**
	 * 单击事件
	 * 
	 * @author E431
	 * 
	 */
	public class MyClick implements OnClickListener {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.action_bar_titlebar_lefts:
				CommentAtivity.this.finish();
				break;
			
			}
		}

	}

	/**
	 * 保存图片
	 * 
	 * @param saveParentPath
	 *            图片保存路径
	 * @param bitmap
	 *            位图对象
	 */
	public boolean saveBitmapToFile(String saveParentPath, Bitmap bitmap) {

		try {
			File saveimg = new File(saveParentPath);
			if (!saveimg.exists())
				saveimg.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(saveimg));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			return true;
		} catch (IOException e) {
			Log.i("tentinet", e.toString());

			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 正方形
	 * 
	 * @param bmp
	 * @return
	 */
	public Bitmap cutBitmap(Bitmap bmp) {
		Bitmap result;
		int w = bmp.getWidth();// 输入长方形宽
		int h = bmp.getHeight();// 输入长方形高
		int nw;// 输出正方形宽
		if (w > h) {
			// 宽大于高
			nw = h;
			result = Bitmap.createBitmap(bmp, (w - nw) / 2, 0, nw, nw);
		} else {
			// 高大于宽
			nw = w;
			result = Bitmap.createBitmap(bmp, 0, (h - nw) / 2, nw, nw);
		}
		return result;
	}

	/**
	 * 初始化评论
	 * @throws JSONException 
	 */
	private void initComentView(JSONArray array) throws JSONException {
	
		mLinearLayout2 = (LinearLayout) findViewById(R.id.main_ll2);
		
		mLinearLayout2.removeAllViews();
		for (int i = 0; i < array.length(); i++) {
			 final JSONObject obj = array.getJSONObject(i);
			View view = LayoutInflater.from(this).inflate(R.layout.comment_list_item, null);
			RoundImage roundImage =(RoundImage)view.findViewById(R.id.iv_head); 
			TextView tv_nikeName =(TextView)view.findViewById(R.id.tv_nikename); 
			TextView tv_comtent =(TextView)view.findViewById(R.id.tv_comtent); 
			TextView tv_time =(TextView)view.findViewById(R.id.tv_time); 
			CommonTextView textview_likes =(CommonTextView)view.findViewById(R.id.textview_likes); 
			mVolleyImageLoader.showImage(roundImage,  obj.getString("avatar"));
			roundImage.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivity(AgentActivity
							.intentForFragment(CommentAtivity.this,
									AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra("userId",obj.optString("member_id")));
					
				}
			});
			tv_nikeName.setText(obj.getString("name")); 
			tv_comtent.setText(obj.getString("content"));
			tv_time.setText(obj.getString("created"));
			mLinearLayout2.addView(view);
		}

	}
	private void initPrase(JSONArray array) {
		try{
		mLinearLayout = (LinearLayout) findViewById(R.id.main_ll);
		mLinearLayout.removeAllViews();
		for (int i = 0; i < array.length(); i++) {
			if(i==9){
				 final JSONObject obj = array.getJSONObject(i);
					final View convertView = LayoutInflater.from(this).inflate(
							R.layout.comment_item, null);
					RoundImage iv = (RoundImage) convertView
							.findViewById(R.id.c_imageview);
					Drawable a = getResources().getDrawable(R.drawable.more);
					BitmapDrawable bd = (BitmapDrawable)a;

					Bitmap bm = bd.getBitmap();
					iv.setImageBitmap(bm);
					convertView.setTag(obj);
					convertView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							try{
							JSONObject obj = (JSONObject)arg0.getTag();
							String member_id =obj.getString("member_id");
							startActivity(AgentActivity
									.intentForFragment(getApplication(),
									AgentActivity.FRAGMENT_PERSONAL_HOME)
									.putExtra("userId",member_id));
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					});
					mLinearLayout.addView(convertView);
				return;
			}else if(i>10){
				return;
			}
			 final JSONObject obj = array.getJSONObject(i);
			final View convertView = LayoutInflater.from(this).inflate(
					R.layout.comment_item, null);
			RoundImage iv = (RoundImage) convertView
					.findViewById(R.id.c_imageview);
			mVolleyImageLoader.showImage(iv, obj.getString("avatar"));
			convertView.setTag(obj);
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try{
					JSONObject obj = (JSONObject)arg0.getTag();
					String member_id =obj.getString("member_id");
					startActivity(AgentActivity
							.intentForFragment(getApplication(),
							AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra("userId",member_id));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			});
			
			
			mLinearLayout.addView(convertView);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private class ShopDetailData implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
		//	mListView.onRefreshComplete();
			Log.i("jsonss:", "" + json_str);
			if (json_str == null || json_str.length() < 60)
				return;
			JSONObject dataJson;
			try {
				dataJson = new JSONObject(json_str);
				JSONObject dataJsonObject = dataJson.getJSONObject("data");
				if (dataJsonObject != null){
					id=dataJsonObject.getString("goods_id");
					initData(dataJsonObject);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
  
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.get_opinions_info");
			jb.addParams("opinions_id", mId);
			return jb;
		}
	}
	
	private class SendCommentData implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {  
			mEt_comment.setText("");
			
			mTask1 = new JsonTask();
			Run.excuteJsonTask(mTask1, new ShopDetailData());

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.add_opinions_comment");
			jb.addParams("member_id", mUserId);
			jb.addParams("opinions_id", mId);
			jb.addParams("content", mEt_comment.getText().toString());
			
			return jb;
		}
	}
	
	/**
	 * 点赞
	 * @author Administrator
	 *
	 */
	private class AddPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;//商品推荐id

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
					Log.i("tentinet:zan:", ""+json_str);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 取消点赞
	 * @author Administrator
	 *
	 */
	private class CalcelPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;//商品推荐id

		public CalcelPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.del_opinions_praise");
//			bean.addParams("member_id", meber_Id);
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext, all)) {
					Log.i("tentinet:quxiaozan:", ""+json_str);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
