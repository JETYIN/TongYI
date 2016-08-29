package com.qianseit.westore.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

/**
 * 网络图片缓存 TODO 查看官方文档关于显示大图片的解决方案
 * 
 */
public class ImageLoader {
	private static final String KEY_VIEW = "view";
	private static final String KEY_BEAN = "bean";
	private static final String KEY_BITMAP = "bitmap";
	private static final String KEY_LAYOUT = "layout";
	// private static final int FADE_IN_TIME = 300;

	private Map<Object, SoftReference<Drawable>> imageCache = new HashMap<Object, SoftReference<Drawable>>();
	private ArrayList<Map<String, Object>> imageQueue = new ArrayList<Map<String, Object>>();

	private DelayHandlerCallback mDelayHandlerCallback;
	private DecodeImageCallback mDecodeImageCallback;
	private DisplayImageCallback mDisplayImageCallback;
	private BaseImageDownloader mImageDownloader;
	private Drawable def_image = null;
	private int mMaxTaskNum = 3;
	private int mNumOfTask = 0;
	private Context mContext;

	// 有序的显示图片，先load的先显示
	private boolean showImagesOrderly = false;
	// getParant为空的View不加载
	private boolean neverDecodeRemovedView = false;

	public static ImageLoader getInstance(Context context) {
		return new ImageLoader(context);
	}

	private ImageLoader(Context context) {
		this.mContext = context;
	}

	/**
	 * 设置默认的图片
	 * 
	 * @param defImage
	 */
	public void setDefautImage(Drawable defImage) {
		this.def_image = defImage;
	}

	/**
	 * 设置默认的图片
	 * 
	 * @param defImage
	 */
	public void setDefautImage(int defImage) {
		this.def_image = mContext.getResources().getDrawable(defImage);
	}

	public Drawable getDefautImage() {
		return this.def_image;
	}

	/**
	 * 同时进行的最大线程数<br />
	 * 建议小于mMaxTaskNum<=5
	 * 
	 * @param mMaxTaskNum
	 */
	public ImageLoader setMaxTaskNum(int mMaxTaskNum) {
		this.mMaxTaskNum = mMaxTaskNum;
		return this;
	}

	/**
	 * 有序的显示图片
	 * 
	 * @param showImagesOrderly
	 */
	public void setShowImagesOrderly(boolean showImagesOrderly) {
		this.showImagesOrderly = showImagesOrderly;
	}

	/**
	 * 不加载getParent为空的ImageView对应的图片
	 * 
	 * @param neverDecodeRemovedView
	 */
	public void setNeverDecodeRemovedView(boolean neverDecodeRemovedView) {
		this.neverDecodeRemovedView = neverDecodeRemovedView;
	}

	/**
	 * 设置图片解析的回调
	 * 
	 * @param mDecodeImageCallback
	 */
	public void setDecodeImageCallback(DecodeImageCallback mDecodeImageCallback) {
		this.mDecodeImageCallback = mDecodeImageCallback;
	}

	/**
	 * 设置延迟加载的回调
	 * 
	 * @param mDelayHandlerCallback
	 */
	public void setDelayHandlerCallback(
			DelayHandlerCallback mDelayHandlerCallback) {
		this.mDelayHandlerCallback = mDelayHandlerCallback;
	}

	/**
	 * 设置图片显示方式的回调
	 * 
	 * @param mDisplayImageCallback
	 */
	public void setDisplayImageCallback(
			DisplayImageCallback mDisplayImageCallback) {
		this.mDisplayImageCallback = mDisplayImageCallback;
	}

	/**
	 * 设置图片方法，只能从主线程调用此方法
	 * 
	 * @param bundle
	 */
	@SuppressWarnings("unchecked")
	private void setImage(Map<String, Object> bundle) {
		ImageView v = (ImageView) bundle.get(KEY_VIEW);
		Object bean = bundle.get(KEY_BEAN);

		// ImageView的tag与图片url不一致，说明ImageView已经被重用了
		if (v.getTag() == null || !v.getTag().equals(bean))
			return;

		// 缓存中存在，则直接设置图片背景
		Object object = null;
		if (bundle.containsKey(KEY_BITMAP)
				&& (object = bundle.get(KEY_BITMAP)) != null) {
			SoftReference<Drawable> sf = (SoftReference<Drawable>) object;
			Drawable drawable = null;
			if (sf != null && (drawable = sf.get()) != null) {
				setImageDrawable(v, drawable);
				return;
			} else {
				showImage(v, bean);
			}
		}

		// 缓存中不存在，则设置默认图标
		setImageDrawable(v, def_image);
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param v
	 * @param drawable
	 */
	private void setImageDrawable(ImageView v, Drawable drawable) {
		// 如果添加了DisplayImageCallback且被消费，则不使用默认的设置
		if (mDisplayImageCallback == null
				|| !mDisplayImageCallback.displayImage(v, drawable))
			v.setImageDrawable(drawable);
	}

	private void justShowImage(ImageView v, Object bean) {
		// 缓存中已经存在，则直接设置图片背景
		if (imageCache.containsKey(bean)) {
			SoftReference<Drawable> softReference = imageCache.get(bean);
			if (softReference != null && softReference.get() != null) {
				setImageDrawable(v, softReference.get());
				return;
			}
		}
		// 缓存中不存在，则设置默认图标
		setImageDrawable(v, def_image);
	}

	/**
	 * 延迟指定的delayTime后执行操作<br />
	 * 需要设置mDelayHandlerCallback来回调
	 * 
	 * @param v
	 * @param bean
	 * @param delayTime
	 */
	public void showImageDelayed(ImageView v, Object bean, int delayTime) {
		if (delayTime > 0) {
			mDelayHandler.removeMessages(0);
			this.justShowImage(v, bean);
			mDelayHandler.sendEmptyMessageDelayed(0, delayTime);
		} else {
			showImageDelayed(v, bean, delayTime);
		}
	}

	/**
	 * 显示网络图片,入口方法
	 * 
	 * @param v
	 *            显示图片的ImageView,需要实现setTag将url植入
	 * @param url
	 *            图片网络地址
	 * @param turl
	 *            图片备用网络地址，比如缩略图地址
	 * @param did
	 *            自带的默认图片id
	 */
	public void showImage(ImageView v, Object bean) {
		showImage(v, bean, null);
	}

	/**
	 * 显示网络图片,入口方法
	 * 
	 * @param v
	 *            显示图片的ImageView,需要实现setTag将url植入
	 * @param url
	 *            图片网络地址
	 * @param layout
	 *            当重用的View从父View移除后，也不解析图片
	 */
	public void showImage(ImageView v, Object bean, View layout) {
		// 缓存中已经存在，则直接设置图片背景
		if (!showImagesOrderly && imageCache.containsKey(bean)) {
			SoftReference<Drawable> softReference = imageCache.get(bean);
				if (softReference != null && softReference.get() != null) {
				// 通知主线程刷新ImageView
				setImageDrawable(v, softReference.get());
				return;
			}
		} else {
			// 缓存中不存在，则设置默认图标
			setImageDrawable(v, def_image);
		}

		// RI.log("get "+bean.appName);
		Map<String, Object> bundle = new HashMap<String, Object>();
		bundle.put(KEY_BEAN, bean);
		bundle.put(KEY_VIEW, v);
		bundle.put(KEY_LAYOUT, layout);

		// 将图片缓存放入队列，等待下次执行
		if (showImagesOrderly)
			imageQueue.add(bundle);
		else
			imageQueue.add(0, bundle);
		excuteNextTask();
	}

	/**
	 * 获取图片并将图片放入缓存
	 * 
	 * @param bundle
	 */
	private void getImage(Map<String, Object> bundle) {
		ImageView v = (ImageView) bundle.get(KEY_VIEW);
		View layout = (View) bundle.get(KEY_LAYOUT);
		Object bean = bundle.get(KEY_BEAN);
		Drawable icon = null;

		// ImageView的tag与图片url不一致，说明ImageView已经被重用了,无需加载图片
		if (v.getTag() == null || !v.getTag().equals(bean))
			return;

		// 当重用的View从父View移除后，也不解析图片
		if (neverDecodeRemovedView && layout != null
				&& layout.getParent() == null) {
			return;
		}

		try {
			// 指定了如何解码图片，则交给Callback处理
			if (mDecodeImageCallback != null) {
				icon = mDecodeImageCallback.decodeImage(bean);
			} else if (bean instanceof Uri) {
				icon = decodeUriImage((Uri) bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 已经读取到图片，则缓存图片到内存
		if (icon != null) {
			SoftReference<Drawable> sb = null;
			sb = new SoftReference<Drawable>(icon);
			imageCache.put(bean, sb);
			bundle.put(KEY_BITMAP, sb);
		}
	}

	/**
	 * 解析Uri图片
	 * 
	 * @param imageUri
	 * @return
	 * @throws Exception
	 */
	public Drawable decodeUriImage(Uri imageUri) throws Exception {
		if (mImageDownloader == null)
			mImageDownloader = new BaseImageDownloader(mContext);
		Options options = createNativeAllocOptions();
		InputStream stream = mImageDownloader.getStream(imageUri);
		if (stream != null) {
			return new BitmapDrawable(mContext.getResources(),
					BitmapFactory.decodeStream(stream, null, options));
		}
		return null;
	}

	/* 开始下一个任务 */
	private void excuteNextTask() {
		if (mNumOfTask < mMaxTaskNum && imageQueue.size() > 0) {
			Map<String, Object> bundle = imageQueue.get(0);
			new DownloadFilesTask().execute(bundle);
			imageQueue.remove(bundle);
		}
	}

	// Returns Options that set the puregeable flag for Bitmap decode.
	public static BitmapFactory.Options createNativeAllocOptions() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		try {
			options.getClass().getField("inNativeAlloc")
					.setBoolean(options, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return options;
	}

	/* 延迟加载的消息处理 */
	private Handler mDelayHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (mDelayHandlerCallback != null)
				mDelayHandlerCallback.handlerDelayMessage();
		};
	};

	/**
	 * 下载图片任务，下载完成后通知更新
	 * 
	 */
	private class DownloadFilesTask extends
			AsyncTask<Map<String, Object>, Integer, Map<String, Object>> {
		protected Map<String, Object> doInBackground(
				Map<String, Object>... bundles) {
			Map<String, Object> bundle = bundles[0];
			mNumOfTask += 1;
			getImage(bundle);
			return bundle;
		}

		protected void onPostExecute(Map<String, Object> bundle) {
			mNumOfTask -= 1;
			setImage(bundle);
			excuteNextTask();
		}
	}

	/**
	 * 清除缓存
	 */
	public void clearCache() {
		imageQueue.clear();
		imageCache.clear();
		System.gc();
		System.runFinalization();
	}

	public static class BaseImageDownloader {
		public final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5000;
		public final int DEFAULT_HTTP_READ_TIMEOUT = 20000;
		protected final int BUFFER_SIZE = 8192;
		protected final Context context;
		protected final int connectTimeout;
		protected final int readTimeout;

		public BaseImageDownloader(Context context) {
			this.context = context.getApplicationContext();
			this.connectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT;
			this.readTimeout = DEFAULT_HTTP_READ_TIMEOUT;
		}

		public BaseImageDownloader(Context context, int connectTimeout,
				int readTimeout) {
			this.context = context.getApplicationContext();
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
		}

		public InputStream getStream(Uri imageUri) throws IOException {
			return this.getStream(imageUri.toString());
		}

		public InputStream getStream(String imageUri) throws IOException {
			switch (Scheme.ofUri(imageUri)) {
			case HTTP:
			case HTTPS:
				return getStreamFromNetwork(imageUri);
			case FILE:
				return getStreamFromFile(imageUri);
			case CONTENT:
				return getStreamFromContent(imageUri);
			case ASSETS:
				return getStreamFromAssets(imageUri);
			case DRAWABLE:
				return getStreamFromDrawable(imageUri);
			}
			return null;
		}

		protected InputStream getStreamFromNetwork(String imageUri)
				throws IOException {
			HttpURLConnection conn = connectTo(imageUri);

			int redirectCount = 0;
			while ((conn.getResponseCode() / 100 == 3) && (redirectCount < 5)) {
				conn = connectTo(conn.getHeaderField("Location"));
				redirectCount++;
			}

			return new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
		}

		private HttpURLConnection connectTo(String url) throws IOException {
			String encodedUrl = Uri.encode(url, "@#&=*+-_.,:!?()/~'%");
			HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl)
					.openConnection();
			conn.setConnectTimeout(this.connectTimeout);
			conn.setReadTimeout(this.readTimeout);
			conn.connect();
			return conn;
		}

		protected InputStream getStreamFromFile(String imageUri)
				throws IOException {
			String filePath = Scheme.FILE.crop(imageUri);
			return new BufferedInputStream(new FileInputStream(filePath),
					BUFFER_SIZE);
		}

		protected InputStream getStreamFromContent(String imageUri)
				throws FileNotFoundException {
			ContentResolver res = this.context.getContentResolver();
			Uri uri = Uri.parse(imageUri);
			return res.openInputStream(uri);
		}

		protected InputStream getStreamFromAssets(String imageUri)
				throws IOException {
			String filePath = Scheme.ASSETS.crop(imageUri);
			return this.context.getAssets().open(filePath);
		}

		protected InputStream getStreamFromDrawable(String imageUri) {
			String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
			int drawableId = Integer.parseInt(drawableIdString);
			BitmapDrawable drawable = (BitmapDrawable) context.getResources()
					.getDrawable(drawableId);
			Bitmap bitmap = drawable.getBitmap();

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 0, os);
			return new ByteArrayInputStream(os.toByteArray());
		}
	}

	public static enum Scheme {
		HTTP("http"), HTTPS("https"), FILE("file"), CONTENT("content"), ASSETS(
				"assets"), DRAWABLE("drawable"), UNKNOWN("");

		private String scheme;
		private String uriPrefix;

		private Scheme(String scheme) {
			this.scheme = scheme;
			this.uriPrefix = (scheme + "://");
		}

		public static Scheme ofUri(String uri) {
			if (uri != null) {
				for (Scheme s : values()) {
					if (s.belongsTo(uri)) {
						return s;
					}
				}
			}
			return UNKNOWN;
		}

		private boolean belongsTo(String uri) {
			return uri.startsWith(this.uriPrefix);
		}

		public String wrap(String path) {
			return this.uriPrefix + path;
		}

		public String crop(String uri) {
			if (!belongsTo(uri)) {
				throw new IllegalArgumentException(String.format(
						"URI [%1$s] doesn't have expected scheme [%2$s]",
						new Object[] { uri, this.scheme }));
			}
			return uri.substring(this.uriPrefix.length());
		}
	}

	public interface DecodeImageCallback {
		/**
		 * 解析图片，Object为ImageView中Tag值
		 * 
		 * @param object
		 * @return
		 */
		public Drawable decodeImage(Object object);
	}

	public interface DisplayImageCallback {
		/**
		 * 设置如何显示图片
		 * 
		 * @param drawable
		 * @return
		 */
		public boolean displayImage(View v, Drawable drawable);
	}

	public interface DelayHandlerCallback {
		/**
		 * 解析图片，Object为ImageView中Tag值
		 * 
		 * @param object
		 * @return
		 */
		public void handlerDelayMessage();
	}

}
