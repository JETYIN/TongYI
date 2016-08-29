package com.qianseit.westore.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;

import com.qianseit.westore.Run;

/**
 * 图片缓存工具类，提供图片处理的一些基础功能
 * 
 * 
 */
public class CacheUtils {

	public static String getCacheFile(String url) {
		return Util.buildString(Run.doCacheFolder, Md5.getMD5(url));
	}
	
	public static String getImageCacheFile(String url) {
		return Util.buildString(Run.doImageCacheFolder, Md5.getMD5(url));
	}

	public static Bitmap getImageIntelligent(String url, boolean limit) {
		return getImageIntelligent(url, limit, CompressFormat.PNG);
	}

	/**
	 * 获取缓存的图片，并缩小比例
	 * 
	 * @param cname
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap getCachedBitmap(String cname, float width, float height) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(cname, options);
		int imageHeight = options.outHeight, imageWidth = options.outWidth;
		int inSampleSize = 1;
		if (imageHeight > height || imageWidth > width) {
			int heightRatio = Math.round((float) imageHeight / height);
			int widthRatio = Math.round((float) imageWidth / height);
			inSampleSize = Math.min(heightRatio, widthRatio);
		}
		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;
		return BitmapFactory.decodeFile(cname, options);
	}

	public static Bitmap getImageIntelligent(String url, boolean limit,
			CompressFormat format) {
		String cname = getCacheFile(url);
		File cache = new File(cname);
		Bitmap bitmap = null;
		if (cache.exists()) {
			try {
				bitmap = CacheUtils.readBitmap(cache);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (limit) // 限制50KB
					bitmap = CacheUtils.getMBitmap(url);
				else
					bitmap = CacheUtils.getBitmap(url);
				if (bitmap != null)
					CacheUtils.saveBitmap(cache, bitmap, format);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bitmap;

	}

	/**
	 * 智能选择图片
	 * 
	 * @param url
	 */
	public static Bitmap getImageIntelligent(String url) {
		return getImageIntelligent(url, false);
	}

	/**
	 * 下载图片
	 * 
	 * @param str
	 * @return
	 */
	public final static Bitmap getBitmap(String str) {
		URL url;
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			url = new URL(str);
			is = url.openConnection().getInputStream();
			bitmap = BitmapFactory.decodeStream(is);

			is.close();
		} catch (Exception e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	/**
	 * 下载图片，50KB限制缩放
	 * 
	 * @param str
	 * @return
	 */
	public final static Bitmap getMBitmap(String str) {
		URL url;
		Bitmap bitmap = null;
		InputStream is = null;
		int maxsize = 100 * 1024;
		try {
			url = new URL(str);
			is = url.openConnection().getInputStream();
			byte[] bytes = getBytes(is);
			int count = bytes.length;
			// RI.log("image count:" + count / 1024 + "kb");
			if (count > maxsize) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				int ss = count / maxsize;
				opts.inSampleSize = ss == 1 ? 2 : ss;
				// RI.log("inSampleSize=" + opts.inSampleSize);
				bitmap = BitmapFactory.decodeByteArray(bytes, 0, count, opts);
			} else {
				bitmap = BitmapFactory.decodeByteArray(bytes, 0, count);
			}

			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	/**
	 * 获取流的字节数组
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private final static byte[] getBytes(InputStream is) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int len = 0;

		while ((len = is.read(b, 0, 1024)) != -1) {
			baos.write(b, 0, len);
			baos.flush();
		}
		byte[] bytes = baos.toByteArray();
		return bytes;
	}

	/**
	 * 添加倒影，原理，先翻转图片，由上到下放大透明度
	 * 
	 * @param originalImage
	 * @return
	 */
	public static Bitmap createReflectedImage(Bitmap originalImage) {

		final int reflectionGap = 5;
		final int rp = 8;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width + 2
				* reflectionGap, (height + height / rp) + 2 * reflectionGap,
				Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(originalImage, reflectionGap, reflectionGap, null);
		Paint paint = new Paint();
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap rmap = Bitmap.createBitmap(originalImage, 0, height * (rp - 1)
				/ rp, width, height / rp, matrix, false);
		canvas.drawBitmap(rmap, reflectionGap, height + reflectionGap
				+ reflectionGap, null);

		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
				+ reflectionGap, 0xffffffff, 0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height + reflectionGap + reflectionGap, width
				+ reflectionGap + reflectionGap,
				bitmapWithReflection.getHeight() + reflectionGap, paint);
		// new SoftReference(canvas);
		canvas = null;
		if (rmap.isRecycled() == false) // 如果没有回收
			rmap.recycle();
		return bitmapWithReflection;
	}

	/**
	 * 从文件读出图片
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public final static Bitmap readBitmap(File f) throws IOException {

		FileInputStream fOut = null;
		Bitmap bitmap = null;
		try {
			fOut = new FileInputStream(f);
			bitmap = BitmapFactory.decodeStream(fOut);
		} catch (Exception e) {
		}

		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 将位图保存为图片文件
	 * 
	 * @param f
	 * @param mBitmap
	 * @throws IOException
	 */
	public final static void saveBitmap(File f, Bitmap mBitmap,
			CompressFormat format) throws IOException {

		FileOutputStream fOut = null;
		try {// 20120716 try起来
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();

			f.createNewFile();
			fOut = new FileOutputStream(f);
			if (format == CompressFormat.JPEG) {
				mBitmap.compress(format, 80, fOut);
			} else {
				mBitmap.compress(format, 100, fOut);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (fOut != null) {
			try {
				fOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
