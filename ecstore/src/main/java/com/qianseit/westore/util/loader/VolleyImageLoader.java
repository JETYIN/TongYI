package com.qianseit.westore.util.loader;

import cn.shopex.ecstore.R;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class VolleyImageLoader {
	
	private static VolleyImageLoader mImageLoader;
	private static Object obj = new Object();
	private ImageLoader imageLoader = null;
	private ImageListener listener = null;
	private ImageCache mImageCache = null;
	private RequestQueue mQueue;
    
	private VolleyImageLoader(Context context){
		mQueue = Volley.newRequestQueue(context);
		mImageCache = new ImageCache(context);
		imageLoader = new ImageLoader(mQueue, mImageCache);
	}
	
	public static VolleyImageLoader getImageLoader(Context context){
		synchronized (obj) {
			if (mImageLoader == null) {
				mImageLoader = new VolleyImageLoader(context);
			}
		}
		return mImageLoader;
	}
	
	public ImageLoader getVImageLoader(){
		return imageLoader;
	}
	
	public void showImage(ImageView imageView, String strImgUrl){
		if (TextUtils.isEmpty(strImgUrl) || !strImgUrl.contains("http")) {
			imageView.setImageResource(R.drawable.default_img_rect);
			return;
		}
//		mImageCache.setScaleSize(false, 0, 0);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		listener = ImageLoader.getImageListener(imageView, R.drawable.default_img_rect, 0);
		if(listener!=null){
			imageLoader.get(strImgUrl, listener);
		}
			
	}
	
	public void showImage(ImageView imageView, String strImgUrl ,ScaleType scaleType){
		if (TextUtils.isEmpty(strImgUrl) || !strImgUrl.contains("http")) {
			imageView.setImageResource(R.drawable.default_img_rect);
			return;
		}
//		mImageCache.setScaleSize(false, 0, 0);
		imageView.setScaleType(scaleType);
		listener = ImageLoader.getImageListener(imageView, R.drawable.default_img_rect, 0);
		imageLoader.get(strImgUrl, listener);
	}
//	public void showImage(ImageView imageView, String strImgUrl, ImageListener listener){
//		if (TextUtils.isEmpty(strImgUrl) || !strImgUrl.contains("http")) {
//			return;
//		}
////		mImageCache.setScaleSize(false, 0, 0);
//		imageView.setScaleType(ScaleType.CENTER_CROP);
//		imageLoader.get(strImgUrl, listener);
//	}
//	public void showImage(ImageView imageView, String strImgUrl , int width ,int height){
//		if (TextUtils.isEmpty(strImgUrl)|| !strImgUrl.contains("http")) {
//			return;
//		}
////		mImageCache.setScaleSize(true, width, height);
//		imageView.setScaleType(ScaleType.CENTER_CROP);
//		listener = ImageLoader.getImageListener(imageView, R.drawable.default_img_rect, 0);
//		imageLoader.get(strImgUrl, listener);
//	}
	
	public void showImage(NetworkImageView imageView, String strImgUrl){
		if (TextUtils.isEmpty(strImgUrl) || !strImgUrl.contains("http")) {
			imageView.setImageResource(R.drawable.default_img_rect);
			return;
		}
		imageView.setDefaultImageResId(R.drawable.default_img_rect);  
		imageView.setErrorImageResId(R.drawable.default_img_rect);  
		imageView.setImageUrl(strImgUrl , imageLoader);
	}
	
	public void showImage(NetworkImageView imageView, String strImgUrl ,ScaleType scaleType){
		if (TextUtils.isEmpty(strImgUrl) || !strImgUrl.contains("http")) {
			imageView.setImageResource(R.drawable.default_img_rect);
			return;
		}
		imageView.setScaleType(scaleType);
		imageView.setDefaultImageResId(R.drawable.default_img_rect);  
		imageView.setErrorImageResId(R.drawable.default_img_rect);  
		imageView.setImageUrl(strImgUrl , imageLoader);
	}
	
	
	
	/**
	 * 删除存在 SDCARD 的 ImageCache 文件
	 * @author chesonqin
	 * 2014-12-11
	 */
	public void deleteImageCache(){
		mImageCache.deleteImageCache();
	}
}
