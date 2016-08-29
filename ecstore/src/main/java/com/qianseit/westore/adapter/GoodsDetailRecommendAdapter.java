package com.qianseit.westore.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.DoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.clipictrue.CommentAtivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.ShareView.ShareViewDataSource;
import com.qianseit.westore.ui.SharedPopupWindow;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

/**
 * 商品详情推荐adapter
 *
 */
@SuppressLint("SimpleDateFormat")
public class GoodsDetailRecommendAdapter extends BaseAdapter implements View.OnClickListener{

	private DoFragment mContext;
	private ArrayList<JSONObject> datas = new ArrayList<JSONObject>();
	private VolleyImageLoader mImageLoader;
	
	public GoodsDetailRecommendAdapter(DoFragment context ,VolleyImageLoader imageLoader) {
		mContext = context;
		mImageLoader = imageLoader;
	}

	@Override
	public int getCount() {
		return datas == null ? 0 : datas.size();
	}

	@Override
	public JSONObject getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	int width;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		JSONObject all = getItem(position);
		if (convertView == null) {
			holder=new ViewHolder();
			convertView = LayoutInflater.from(mContext.mActivity).inflate(R.layout.item_new_listview, null);
			holder.textview_commentss=(TextView) convertView.findViewById(R.id.textview_commentss);
			holder.textview_likes_layout=(LinearLayout) convertView.findViewById(R.id.textview_likes_layout);
			holder.img_brand_logos=(CircleImageView) convertView.findViewById(R.id.img_brand_logos);
			holder.button_relateds=(TextView) convertView.findViewById(R.id.button_relateds);
			holder.textview_shareads=(TextView) convertView.findViewById(R.id.textview_shareads);
			holder.textview_likes=(TextView)convertView.findViewById(R.id.textview_likes);
			holder.textview_likes_image= (ImageView) convertView.findViewById(R.id.textview_likes_image);
			holder.textview_names=(TextView)convertView.findViewById(R.id.textview_names);
			holder.textview_levels=(TextView)convertView.findViewById(R.id.textview_levels);
			holder.textview_titles=(TextView)convertView.findViewById(R.id.textview_titles);
			holder.textview_contents=(TextView)convertView.findViewById(R.id.textview_contents);
			holder.textview_times=(TextView)convertView.findViewById(R.id.textview_times);
			holder.imgfilter=(ImageView) convertView.findViewById(R.id.imgfilter);
			holder.textview_commentss.setOnClickListener(this);
			holder.textview_likes_layout.setOnClickListener(this);
			holder.img_brand_logos.setOnClickListener(this);
			holder.button_relateds.setOnClickListener(this);
			holder.textview_shareads.setOnClickListener(this);
			holder.textview_levels.setOnClickListener(this);
			holder.textview_names.setOnClickListener(this);
			convertView.findViewById(R.id.ll_position).setOnClickListener(this);
			convertView.setTag(holder);
			holder.img_brand_logos.setTag(all);
			holder.textview_names.setTag(all);
			holder.button_relateds.setTag(all);
			holder.textview_commentss.setTag(all);
			holder.textview_likes.setTag(all);
			holder.textview_likes_layout.setTag(all);
			holder.textview_shareads.setTag(all);
			holder.textview_levels.setTag(all);
			
			WindowManager wm = (WindowManager) mContext.getActivity()
					.getSystemService(Context.WINDOW_SERVICE);
			width = wm.getDefaultDisplay().getWidth() - Util.dip2px(mContext.getActivity(), 10);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		mImageLoader.showImage(holder.img_brand_logos, all.optString("avatar"));
		holder.textview_names.setText(all.optString("name"));
		holder.textview_levels.setText(Run.buildString("LV",all.optString("member_lv_id")));
		holder.textview_contents.setText(all.optString("content"));
		holder.textview_times.setText(getDateFomat(all.optString("created")));
		if (all.optInt("is_attention") == 1) {
			holder.button_relateds.setBackgroundResource(R.drawable.bg_semicircle_selector);
			holder.button_relateds.setTextColor(Color.WHITE);
			holder.button_relateds.setText("已关注");
		} else {
//			holder.button_relateds.setText("+关注");
			holder.button_relateds.setBackgroundResource(R.drawable.icon_add_guanzhu);
		}
		if (TextUtils.isEmpty(all.optString("image"))) {
			convertView.findViewById(R.id.picturess).setVisibility(View.GONE);
			convertView.findViewById(R.id.comment_fans_share_layout).setVisibility(View.GONE);
			holder.textview_titles.setVisibility(View.GONE);
		} else {
			JSONObject obj = all.optJSONObject("tag");
			holder.textview_titles.setText(all.optString("goods_name"));
			Iterator<String> keys = obj.keys();
			JSONObject tag = null;
			while(keys.hasNext()){
				String key = keys.next();
				tag = obj.optJSONObject(key);
			}
			mImageLoader.showImage(holder.imgfilter, all.optString("image"));
			RelativeLayout imageLayout = (RelativeLayout) convertView.findViewById(R.id.ll_position);
			if(imageLayout.getChildCount() >= 2)
				imageLayout.removeViewAt(1);
			LayoutParams p = (LayoutParams) imageLayout.getLayoutParams();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//			WindowManager wm = (WindowManager) mContext.mActivity.getSystemService(Context.WINDOW_SERVICE);
//			float width = Float.valueOf(wm.getDefaultDisplay().getWidth());
			p.width =  (int) (width - Util.dip2px(mContext.mActivity, 10));
			p.height = (int) (width - Util.dip2px(mContext.mActivity, 10));
			imageLayout.setLayoutParams(p);
			float x = (float) (Float.valueOf(tag.optInt("x")) / 100.0);
			float y = (float) (Float.valueOf(tag.optInt("y")) / 100.0);
			int xx = (int)((width - Util.dip2px(mContext.mActivity, 10)) * x);
			int yy = (int)((Util.dip2px(mContext.mActivity, 320) - Util.dip2px(mContext.mActivity, 10)) * y);
			params.topMargin = yy;
			params.leftMargin = xx;
			View view =LayoutInflater.from(mContext.mActivity).inflate(R.layout.picturetagview, null,
					true);
			TextView tvPictureTagLabel =(TextView)view.findViewById(R.id.tvPictureTagLabel);
			RelativeLayout rrTag =(RelativeLayout)view.findViewById(R.id.loTag);
			tvPictureTagLabel.setText(tag.optString("image_tag"));
			
			if(!tag.optString("image_type").equals("1")){
				rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_right);
			}else{
				rrTag.setBackgroundResource(R.drawable.bg_picturetagview_tagview_left);
			}
			imageLayout.setTag(all);
			imageLayout.addView(view, params);
			holder.textview_commentss.setText("评论("+all.optString("c_num")+")");
			holder.textview_likes.setText(all.optString("p_num"));
			
			if (all.optString("is_praise").equals("0")) {
				holder.textview_likes_image.setImageResource(R.drawable.my_msg_praise);
			} else {
				holder.textview_likes_image.setImageResource(R.drawable.my_new_fans);
			}
			
			convertView.findViewById(R.id.picturess).setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.comment_fans_share_layout).setVisibility(View.VISIBLE);
		}
		return convertView;
	}
	class ViewHolder{
		TextView textview_commentss;
		LinearLayout textview_likes_layout;
		CircleImageView img_brand_logos;
		TextView button_relateds;
		TextView textview_shareads;
		TextView textview_names;
		TextView textview_levels;
		TextView textview_titles;
		TextView textview_contents;
		TextView textview_times;
		TextView textview_likes;
		ImageView textview_likes_image;
		ImageView imgfilter;
	}
	@Override
	public void onClick(View v) {
		if (v.getTag() != null) {
			final JSONObject all = (JSONObject) v.getTag();
			if (v.getId() == R.id.img_brand_logos || v.getId() == R.id.textview_names) {
				mContext.startActivity(AgentActivity
						.intentForFragment(mContext.mActivity,
								AgentActivity.FRAGMENT_PERSONAL_HOME)
						.putExtra("userId",
								all.optString("member_id")));
			} else if(v.getId() == R.id.textview_commentss || v.getId() == R.id.ll_position){
				Intent intent = new Intent(mContext.mActivity, CommentAtivity.class);
				intent.putExtra("id", all.optString("id"));
				mContext.startActivity(intent);
			} else if(v.getId() == R.id.textview_likes_layout){
				if (all.optInt("is_praise") == 0) {//未点赞
					new JsonTask().execute(new AddPraiseTask(all));
				} else {
					new JsonTask().execute(new CancelPraiseTask(all));
				}
			} else if(v.getId() == R.id.button_relateds){
				if (all.optInt("is_attention") == 0 ) {//未关注
					new JsonTask().execute(new AddAttentionTask(all));
				} else {
					new JsonTask().execute(new CancelAttentionTaskTask(all));
				}
			} else if(v.getId() == R.id.textview_shareads){
				SharedPopupWindow morePopWindow = new SharedPopupWindow(
						mContext.mActivity);
				ShareViewDataSource dataSource = new ShareViewDataSource() {

					@Override
					public String getShareUrl() {
						return String.format(Run.RECOMMEND_URL,all.optString("id"));
					}

					@Override
					public String getShareText() {
						return all.optString("goods_name");
					}

					@Override
					public String getShareImageUrl() {
						return all.optString("image");
					}

					@Override
					public String getShareImageFile() {
						return CacheUtils.getImageCacheFile(all.optString("image"));
					}
					
				};

				morePopWindow.setDataSource(dataSource);
				morePopWindow.showPopupWindow(v);
			} else if(v.getId() == R.id.textview_levels){
				mContext.startActivity(AgentActivity.intentForFragment(mContext.mActivity,
						AgentActivity.FRAGMENT_HELP_ARTICLE).putExtra("title", "商派等级规则")
						.putExtra("url", Run.buildString(Run.DOMAIN,"/wap/statics-pointLv.html?from=app")));
			}
		}
	};
	
	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private String getDateFomat(String createDate){
		String dateString = "";
		try {
			Date date = sdf1.parse(createDate);
			dateString = sdf2.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateString;
	}
	
	public void loadData(String goodsId){
		new JsonTask().execute(new GetRecommend(goodsId));
	}
	
	private class GetRecommend implements JsonTaskHandler{

		private String iid;
		
		public GetRecommend(String iid){
			this.iid = iid;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext.mActivity, all, false)) {
					JSONArray list = all.optJSONArray("data");
					int count = list == null ? 0 : list.length();
					for (int i = 0; i < count && i < 5; i++) {
						datas.add(list.optJSONObject(i));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				notifyDataSetChanged();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean requestBean = new JsonRequestBean( "mobileapi.goods.getcomment");
			requestBean.addParams("goods_id", iid);
			return requestBean;
		}
		
	}
	
	/**
	 * 点赞
	 * @author Administrator
	 *
	 */
	class AddPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;//商品推荐id
		private JSONObject temp;

		public AddPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}
		
		public AddPraiseTask(JSONObject all){
			this.meber_Id = AgentApplication.getLoginedUser(mContext.mActivity).getMemberId();
			this.opinions_Id = all.optString("id");
			temp = all;
		}

		@Override
		public JsonRequestBean task_request() {
			mContext.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.add_opinions_praise");
			bean.addParams("member_id", meber_Id);
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			mContext.hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext.mActivity, all)) {
					if (all.optBoolean("data")) {
						int index = datas.indexOf(temp);
						temp.put("is_praise", 1);
						temp.put("p_num", temp.optInt("p_num") + 1);
						datas.set(index, temp);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * 取消点赞
	 * @author Administrator
	 *
	 */
	class CancelPraiseTask implements JsonTaskHandler {
		private String meber_Id;
		private String opinions_Id;//商品推荐id
		private JSONObject temp;

		public CancelPraiseTask(String meberId, String opinions_Id) {
			this.meber_Id = meberId;
			this.opinions_Id = opinions_Id;
		}
		
		public CancelPraiseTask(JSONObject all){
			this.meber_Id = AgentApplication.getLoginedUser(mContext.mActivity).getMemberId();
			this.opinions_Id = all.optString("id");
			temp = all;
		}

		@Override
		public JsonRequestBean task_request() {
			mContext.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.goods.del_opinions_praise");
//			bean.addParams("member_id", meber_Id);
			bean.addParams("opinions_id", opinions_Id);
			return bean;
		}  

		@Override
		public void task_response(String json_str) {
			mContext.hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext.mActivity, all)) {
					if (all.optBoolean("data")){
						int index = datas.indexOf(temp);
						temp.put("is_praise", 0);
						temp.put("p_num", temp.optInt("p_num") - 1);
						datas.set(index, temp);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				notifyDataSetChanged();
			}
		}
	}
	
	
	/**
	 * 取消关注
	 * @author Administrator
	 *
	 */
	private class CancelAttentionTaskTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;
		private JSONObject temp;

		public CancelAttentionTaskTask(String meberId, String fansId) {
			this.meberId = meberId;
			this.fansId = fansId;
		}
		
		public CancelAttentionTaskTask(JSONObject temp){
			this.temp = temp;
			this.meberId = temp.optString("member_id");
			this.fansId = AgentApplication.getLoginedUser(mContext.mActivity).getMemberId();
		}

		@Override
		public JsonRequestBean task_request() {
			mContext.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.un_attention");
			bean.addParams("member_id", meberId);
			bean.addParams("fans_id", fansId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			mContext.hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext.mActivity, all)) {
					String data=all.getString("data");
					if(data.equals("请重新登录")){
						Toast.makeText(mContext.mActivity, "请重新登录", 5000).show();
					}else{
						if(data.equals("请重新登录")){
							Toast.makeText(mContext.mActivity, "请重新登录", 5000).show();
						}else{
							int index = datas.indexOf(temp);
							temp.put("is_attention",0);
							datas.set(index, temp);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * 添加关注
	 * @author Administrator
	 *
	 */
	private class AddAttentionTask implements JsonTaskHandler {
		private String meberId;
		private String fansId;
		private JSONObject temp;

		public AddAttentionTask(String meberId, String fansId) {
			this.meberId = meberId;
			this.fansId = fansId;
		}
		
		public AddAttentionTask(JSONObject temp){
			this.temp = temp;
			this.meberId = temp.optString("member_id");
			this.fansId = AgentApplication.getLoginedUser(mContext.mActivity).getMemberId();
		}

		@Override
		public JsonRequestBean task_request() {
			mContext.showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.attention");
			bean.addParams("member_id", meberId);
			bean.addParams("fans_id", fansId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			mContext.hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mContext.mActivity, all)) {
					String data=all.getString("data");
					if(data.equals("请重新登录")){
						Toast.makeText(mContext.mActivity, "请重新登录", 5000).show();
					}else{
						int index = datas.indexOf(temp);
						temp.put("is_attention", 1);
						datas.set(index, temp);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				notifyDataSetChanged();
			}
		}
	}
	
}
