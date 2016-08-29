package com.qianseit.westore.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.util.ChooseUtils;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class GridViewItemAdapter extends BaseAdapter{
//	private String impath [];
	private VolleyImageLoader mVolleyImageLoader;
    private int gridviewItem;
    private DisplayImageOptions options;
    private  ImageLoader imageloader;
    private  LayoutInflater  layoutInflater;
    private List<ChooseUtils> mGoodsArray ;
    private Context context;
	public GridViewItemAdapter(Context context, List<ChooseUtils> mGoodsArray,
			int gridviewItem, DisplayImageOptions options,
			ImageLoader imageloader,VolleyImageLoader mVolleyImageLoader) {
		this.mGoodsArray=mGoodsArray;
		this.gridviewItem=gridviewItem;
		this.options=options;
		this.context=context;
		this.imageloader=imageloader;
		this.mVolleyImageLoader=mVolleyImageLoader;
		//得到一个布局填充服务
		layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
//    
//    	public GridViewItemAdapter(Context context, List<ChooseUtils> mGoodsArray,
//			int gridviewItem, DisplayImageOptions options,
//			ImageLoader imageloader,VolleyImageLoader mVolleyImageLoader) {
//		this.mGoodsArray=mGoodsArray;
//		this.gridviewItem=gridviewItem;
//		this.options=options;
//		this.context=context;
//		this.imageloader=imageloader;
//		this.mVolleyImageLoader=mVolleyImageLoader;
//		//得到一个布局填充服务
//		layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	} 
	@Override
	public int getCount() { 
		return mGoodsArray.size();
	}
	@Override
	public Object getItem(int  position) {
		return mGoodsArray.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}  
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageview;
		TextView textview;
		TextView imageButton;
		TextView texttime;
		if(convertView==null){
			convertView=layoutInflater.inflate(gridviewItem, null, false);
			imageview=(ImageView)convertView.findViewById(R.id.goods_detail_images);
			texttime=(TextView)convertView.findViewById(R.id.textview_times);
			textview=(TextView)convertView.findViewById(R.id.textview_titles);
			imageButton=(TextView)convertView.findViewById(R.id.button_related);
			//封装listview的每一项的布局文件中的组件
			convertView.setTag(new ChooseWrapper(imageview,textview,imageButton,texttime));
		}else{
			ChooseWrapper dataWrapper=(ChooseWrapper)convertView.getTag();
			imageview=dataWrapper.imageview;
			textview=dataWrapper.textview;
			imageButton=dataWrapper.imageButton;	
			texttime=dataWrapper.texttime;	
		} 
		final ChooseUtils chooseInfo= mGoodsArray.get(position);
//		mVolleyImageLoader.showImage(imageview, chooseInfo.getImagePath());
//		imageview.setImageResource(R.drawable.shop_service_topimage);
//		imageButton.setImageResource(R.drawable.but_tuijian);
//		imageloader.displayImage(imagePaht[position], imageview, options);
		mVolleyImageLoader.showImage(imageview,chooseInfo.getImagePath());
		textview.setText(chooseInfo.getGoods_name());  
//		String times=getTime(chooseInfo.getSelectsTime());
//		Log.i("tentinet-->", "time:"+times);
		texttime.setText(chooseInfo.getSelectsTime());
		if(chooseInfo.getIs_opinions().equals("0")){
//			imageButton.setImageResource(R.drawable.but_tuijian);
			imageButton.setBackgroundResource(R.drawable.but_tuijian);
			imageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Bundle bundle = new Bundle();
					bundle.putSerializable(context.getString(R.string.intent_key_serializable), chooseInfo);
					Intent intent = AgentActivity.intentForFragment(
							context, AgentActivity.FRAGMENT_GOODS_SHOOSEG);
					intent.putExtras(bundle);
					context.startActivity(intent);
					
				}
			});
		}else{
//			imageButton.setImageResource(R.drawable.weituijain);
			imageButton.setBackgroundResource(R.drawable.weituijain);
		}
		return convertView;
	}
	private String getTime(String selectsTime) {
		String temes="";
		String ts[]=selectsTime.split(" ");
		String nian[]=ts[1].split(":");
		String yue[]=ts[0].split("-");
		temes=nian[0]+"-"+yue[1]+"-"+yue[2];
		return temes;
	}
	public class ChooseWrapper{
		ImageView imageview;
		TextView textview;
		TextView imageButton;
		TextView texttime;
		public ChooseWrapper(ImageView imageview,TextView textview,TextView imageButton,TextView texttime){
			this.imageview=imageview;
			this.textview=textview;
			this.imageButton=imageButton;
			this.texttime=texttime;
		}
	}
	
	/**
	 * 正方形
	 * @param bmp
	 * @return
	 */
	public Bitmap cutBitmap(Bitmap bmp)
    {
	Bitmap result;
	int w = bmp.getWidth();//输入长方形宽
	int h = bmp.getHeight();//输入长方形高
	int nw;//输出正方形宽
	if(w > h)
	{
	    //宽大于高
	    nw = h;
	    result = Bitmap.createBitmap(bmp, (w - nw) / 2, 0, nw, nw);
	}else{
	  //高大于宽
	    nw = w;
	    result = Bitmap.createBitmap(bmp, 0 , (h - nw) / 2, nw, nw);
	}
	return result;
    }
	}