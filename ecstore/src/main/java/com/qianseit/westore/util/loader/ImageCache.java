package com.qianseit.westore.util.loader;

import java.io.IOException;

import com.qianseit.westore.Run;
import com.qianseit.westore.util.Md5;
import com.qianseit.westore.util.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class ImageCache implements
		com.android.volley.toolbox.ImageLoader.ImageCache {

	private final String TAG = "ImageCache";
	private boolean isScaleImage;
	// private int scaleWidth , scaleHeight;
	// private Context mContext;

	/**
	 * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
	 */
	private LruCache<String, Bitmap> mMemoryCache;
	/**
	 * 操作文件相关类对象的引用
	 */
	private FileUtils fileUtils;

	@SuppressLint("NewApi")
	public ImageCache(Context context) {
		// int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// Log.i("", "--->>-->-" + (maxMemory / 1024 / 1024));
		// int cacheMemory = maxMemory / 10;
		final int cacheMemory = 1024 * 1024 * 5;

		mMemoryCache = new LruCache<String, Bitmap>(cacheMemory) {

			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getHeight() * bitmap.getWidth();
			}

		};
		fileUtils = new FileUtils(context);
		// mContext = context;
	}

	@Override
	public Bitmap getBitmap(String url) {
		if (url.indexOf("http") >= 0) {
			url = url.substring(url.indexOf("http"));
		}
		Log.i(TAG, "get----->>>" + url);
		url = Md5.getMD5(url);
		Bitmap bitmap = mMemoryCache.get(url);
//		Bitmap bitmap = null;
		if (bitmap == null) {
			// Bitmap bitmap = fileUtils.getBitmap(url);
			bitmap = fileUtils.getBitmap(url);
			if (bitmap != null) {
				mMemoryCache.put(url, bitmap);
			}
		}
		return bitmap;
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		// url = url.replaceAll("/", "");
		if (url.indexOf("http") >= 0) {
			url = url.substring(url.indexOf("http"));
		}
		url = Md5.getMD5(url);
		if (mMemoryCache.get(url) == null && bitmap != null) {
			Log.i(TAG, url);
			if (bitmap != null) {
				mMemoryCache.put(url, bitmap);
				try {
					fileUtils.savaBitmap(url, bitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
//		try {
//			fileUtils.savaBitmap(url, bitmap);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void deleteImageCache() {
		if (fileUtils != null) {
			fileUtils.deleteFile();
		}
	}
	
}
