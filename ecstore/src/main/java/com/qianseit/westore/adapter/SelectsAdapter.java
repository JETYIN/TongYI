package com.qianseit.westore.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.clipictrue.CommentAtivity;
import com.qianseit.westore.ui.CircleImageView;
import com.qianseit.westore.ui.CommonTextView;
import com.qianseit.westore.util.SelectsUtils;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class SelectsAdapter extends BaseAdapter{
	
	private Context context;
	private VolleyImageLoader mVolleyImageLoader;
	private List<SelectsUtils> selectslistdata;
	private int itemKoubeiRecommentHead;
	private LayoutInflater iLayoutInflater;//动态布局加载器
	
	public SelectsAdapter(Context context,List<SelectsUtils> selectslistdata, int itemKoubeiRecommentHead,VolleyImageLoader mVolleyImageLoader) {
		this.context=context;
		this.selectslistdata=selectslistdata;
		this.mVolleyImageLoader=mVolleyImageLoader;
		this.itemKoubeiRecommentHead=itemKoubeiRecommentHead;
		iLayoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return selectslistdata.size();
	}

	@Override
	public Object getItem(int position) {
		return selectslistdata.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View converView, ViewGroup parent) {
		CircleImageView img_brand;	//头像
		CommonTextView textview_sharead;	//分享
		CommonTextView textview_like;	//喜欢
		CommonTextView textview_comments;	//评论
		CommonTextView textview_time;	//日期
		CommonTextView textview_content;	//详细内容
		CommonTextView textview_title;	//内容标题
		ImageView goods_detail_images;	//详情图片
		CommonTextView button_related;	//关注图标
//		CommonTextView textview_sTime=null;	//发布日期
		CommonTextView textview_name;	//用户名
		CommonTextView textview_level;	//等级
		if(converView==null){
			converView=iLayoutInflater.inflate(itemKoubeiRecommentHead, null, false);
			//初始化listview的每一项的布局文件中的组件
			goods_detail_images=(ImageView)converView.findViewById(R.id.imgfilter);	//详情图片
			img_brand=(CircleImageView)converView.findViewById(R.id.img_brand_logos);	//头像
			textview_name=(CommonTextView)converView.findViewById(R.id.textview_names);	//用户名
			textview_level=(CommonTextView)converView.findViewById(R.id.textview_levels);	//等级
			button_related=(CommonTextView)converView.findViewById(R.id.button_relateds);	//关注图标
			textview_title=(CommonTextView)converView.findViewById(R.id.textview_titles);	//内容标题
			textview_content=(CommonTextView)converView.findViewById(R.id.textview_contents);	//详情内容
			textview_time=(CommonTextView)converView.findViewById(R.id.textview_times);	//日期
			textview_comments=(CommonTextView)converView.findViewById(R.id.textview_commentss);	//评论
			textview_like=(CommonTextView)converView.findViewById(R.id.textview_likes);	//喜欢
			textview_sharead=(CommonTextView)converView.findViewById(R.id.textview_shareads);	//分享	
			textview_sharead=(CommonTextView)converView.findViewById(R.id.textview_shareads);
//			relaylayout=(RelativeLayout)converView.findViewById(R.id.photo_topss);	
			//封装listview的每一项的布局文件中的组件
			converView.setTag(new SelectsWrapper(img_brand,textview_sharead,textview_like,textview_comments,textview_time,
					textview_content,textview_title,goods_detail_images,button_related,textview_name,textview_level
					));
		}else{
			SelectsWrapper dataWrapper=(SelectsWrapper)converView.getTag();
			goods_detail_images=dataWrapper.goods_detail_images;
			img_brand=dataWrapper.img_brand;
			textview_level=dataWrapper.textview_level;	
			button_related=dataWrapper.button_related;
			textview_title=dataWrapper.textview_title;
			textview_content=dataWrapper.textview_content;	
			textview_time=dataWrapper.textview_time;
			textview_comments=dataWrapper.textview_comments;
			textview_like=dataWrapper.textview_like;	
			textview_sharead=dataWrapper.textview_sharead;
			textview_name=dataWrapper.textview_name;	
		} 
		final SelectsUtils selectInfo= selectslistdata.get(position);
		
		mVolleyImageLoader.showImage(img_brand, selectInfo.getImg_brand());
		mVolleyImageLoader.showImage(goods_detail_images, selectInfo.getGoods_detail_images());
		textview_name.setText(selectInfo.getTextview_name());
		textview_content.setText(selectInfo.getTextview_content());
		textview_title.setText(selectInfo.getTextview_title());

  
		Log.i("tentinet-->", ""+selectInfo.getButton_related());
		if(selectInfo.getButton_related().equals("0")){
			String timase=selectInfo.getsTime();
			String time[]=timase.split(" ");
			button_related.setText(time[0]);
			button_related.setBackgroundResource(R.drawable.bais);
			button_related.setTextColor(Color.BLACK);
		}else{
			button_related.setBackgroundResource(R.drawable.icon_red_background);
			button_related.setTextColor(Color.WHITE);
			button_related.setText("已关注");
			
		}
		textview_content.setText(Html
				.fromHtml("<font size=\"4\" color=\"red\">[好物推荐]</font><font size=\"4\" color=\"#9b9b9b\">阿凡达蘑菇灯，是设计师根据电影《阿凡达》蘑菇灯场景，设计的一款家居装饰品。</font>"));
		textview_time.setText(selectInfo.getsTime());
		String pinglun="评论("+selectInfo.getTextview_comments()+")";
		textview_comments.setText(pinglun);
		textview_like.setText(selectInfo.getP_num());			
		textview_level.setText("LV"+selectInfo.getTextview_level()); 

		goods_detail_images.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context,CommentAtivity.class);
				intent.putExtra("id", selectInfo.getId());
				context.startActivity(intent);
			}
		});
		textview_comments.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context,CommentAtivity.class);
						intent.putExtra("id", selectInfo.getId());
						context.startActivity(intent);
					}
				});
		return converView;
	}
	
	public static int getWidth(View view) 
	{ 
	int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED); 
	int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED); 
	view.measure(w, h); 
	return (view.getMeasuredWidth()); 
	} 
	
	
	
	
	
	public class SelectsWrapper{
		CircleImageView img_brand;	//头像
		CommonTextView textview_sharead;	//分享
		CommonTextView textview_like;	//喜欢
		CommonTextView textview_comments;	//评论
		CommonTextView textview_time;	//日期
		CommonTextView textview_content;	//详细内容
		CommonTextView textview_title;	//内容标题
		ImageView goods_detail_images;	//详情图片
		CommonTextView button_related;	//关注图标
		CommonTextView textview_name;	//用户名
		CommonTextView textview_level;	//等级
		public SelectsWrapper(CircleImageView img_brand,CommonTextView textview_sharead,CommonTextView textview_like,
				CommonTextView textview_comments,CommonTextView textview_time,CommonTextView textview_content,
				CommonTextView textview_title,ImageView goods_detail_images,CommonTextView button_related
				,CommonTextView textview_name,CommonTextView textview_level){
			this.img_brand=img_brand;
			this.textview_sharead=textview_sharead;
			this.textview_like=textview_like;
			this.textview_comments=textview_comments;
			this.textview_time=textview_time;
			this.textview_content=textview_content;
			this.textview_title=textview_title;
			this.goods_detail_images=goods_detail_images;
			this.button_related=button_related;
			this.textview_name=textview_name;
			this.textview_level=textview_level;
		}
	}

}









