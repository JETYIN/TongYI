package com.qianseit.westore;

import java.io.File;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.activity.LauncherActivity;
import com.qianseit.westore.util.CacheUtils;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.ImageLoader.DecodeImageCallback;
import com.qianseit.westore.util.ImageLoader.DisplayImageCallback;
import com.qianseit.westore.util.Util;
import cn.shopex.ecstore.R;

public class Run extends Util {
	// public static final String REQUEST_SUCCESS_CODE = "0";

	public static long countdown_time = 0;
	public static int goodsCounts = 0;
	// public static final String ACTION_SMS_RECEIVED =
	// "android.provider.Telephony.SMS_RECEIVED";
	// public static final String ACTION_SMS_DELIVER =
	// "android.provider.Telephony.SMS_DELIVER";

	public static final String EXTRA_DETAIL_TYPE = "com.qianseit.westore.EXTRA_DETAIL_TYPE";
	public static final String EXTRA_TAB_POSITION = "com.qianseit.westore.EXTRA_TAB_POSITION";
	// public static final String EXTRA_FILE_NAME =
	// "com.qianseit.westore.EXTRA_FILE_NAME";
	public static final String EXTRA_CLASS_ID = "com.qianseit.westore.EXTRA_CLASS_ID";
	// public static final String EXTRA_BUY_CODE =
	// "com.qianseit.westore.EXTRA_BUY_CODE";
	public static final String EXTRA_ARTICLE_ID = "com.qianseit.westore.EXTRA_ARTICLE_ID";
	public static final String EXTRA_DATA = "com.qianseit.westore.EXTRA_DATA";
	public static final String EXTRA_VALUE = "com.qianseit.westore.EXTRA_VALUE";
	public static final String EXTRA_TITLE = "com.qianseit.westore.EXTRA_TITLE";
	// public static final String EXTRA_METHOD =
	// "com.qianseit.westore.EXTRA_METHOD";
	public static final String EXTRA_KEYWORDS = "com.qianseit.westore.EXTRA_KEYWORDS";
	public static final String EXTRA_HTML = "com.qianseit.westore.EXTRA_HTML";
	// public static final String EXTRA_URL = "com.qianseit.westore.EXTRA_URL";
	public static final String EXTRA_VITUAL_CATE = "com.qianseit.westore.EXTRA_VITUAL_CATE";
	public static final String EXTRA_ADDR = "com.qianseit.westore.EXTRA_ADDR";
	// public static final String EXTRA_AREA_ID =
	// "com.qianseit.westore.AREA_ID";
	public static final String EXTRA_PRODUCT_ID = "com.qianseit.westore.PRODUCT_ID";
	public static final String EXTRA_FROM_EXTRACT = "com.qianseit.westore.FROM_EXTRACT";
	public static final String EXTRA_COUPON_DATA = "com.qianseit.westore.COUPON_DATA";
	public static final String EXTRA_SCAN_REZULT = "com.qianseit.westore.SCAN_REZULT";
	public static final String EXTRA_GOODS_DETAIL_BRAND = "com.qianseit.westore.DETAIL_BRAND";
	public static final String EXTRA_STROE_DELETE_GOODS = "com.qianseit.westore.STROE_DELETE_GOODS";

	// 用户信息
	public static final String pk_logined_username = "logined_username";
	public static final String pk_logined_user_password = "logined_user_password";
	public static final String pk_shortcut_installed = "shortcut_installed";
	// public static final String pk_newest_version_code =
	// "newest_version_code";

	public static final String DOMAIN = "http://www.ty16.cn";
	public static final String TOKEN = "a1df65b565a7bb32bce4ef5518cf501e1b3c9f0bd97e1225ee23b7f40f1238cf";



//	 public static final String DOMAIN = "http://admin.yingtaoshe.com";
//	 public static final String TOKEN =
//	 "5cf3365a5d783482c44b4a4b721bca1021c83a5f78ccebfb87c57dc8da5c9ec6";


	public static final String MAIN_URL = DOMAIN + "/index.php/";
	// public static final String PRODUCT_URL = MAIN_URL +
	// "wap/product-%s.html";
	public static final String GOODS_URL = DOMAIN + "/wap/agoods-info.html?goods_id=%s";
	public static final String RECOMMEND_URL = DOMAIN + "/wap/opinions-info.html?opinions_id=%s";
	public static final String VCODE_URL = MAIN_URL + "index-gen_vcode-b2c-4.html?";
	public static final String API_URL = MAIN_URL + "api";
	// public static final String VERSION_URL = DOMAIN
	// + "/app_version/version_info.php";

	public static final String FILE_HOME_ADS_JSON = "home_ads_json.cache";

	// public static final Pattern sDrawableRegex = Pattern
	// .compile(" *@(drawable/[a-z0-9_]+) *");
	// public static final Pattern sStringRegex = Pattern
	// .compile(" *@(string/[a-z0-9_]+) *");

	/**
	 * 移除2端的引号
	 * 
	 * @param source
	 * @return
	 */
	public static String removeQuotes(String source) {
		if (source.startsWith("\""))
			source = source.substring(1);
		if (source.endsWith("\""))
			source = source.substring(0, source.length());
		return source;
	}

	/**
	 * 打开图片选择
	 * 
	 * @return
	 */
	public static Intent pickerPhotoIntent(int width, int heigth) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "false");
		intent.putExtra("aspectX", width);
		intent.putExtra("aspectY", heigth);
		intent.putExtra("outputX", width);
		intent.putExtra("outputY", heigth);
		intent.putExtra("return-data", false);
		return intent;
	}

	/**
	 * 判断是否为手机号
	 * 
	 * @param number
	 *            the input number to be tested
	 * @return 返回true则是手机号
	 */
	public static boolean isChinesePhoneNumber(String number) {
		if (TextUtils.isEmpty(number))
			return false;
		if (number.length() != 11 || !number.startsWith("1"))
			return false;

		if (isPhoneNumber(number)) {
			int secDigit = Integer.parseInt(number.substring(1, 2));
			return secDigit >= 3 && secDigit <= 9;
		}
		return false;
	}

	public static boolean checkRequestJson(Context ctx, JSONObject all) {
		return checkRequestJson(ctx, all, true);
	}

	/**
	 * 检测请求的状态
	 * 
	 * @param ctx
	 * @param all
	 * @return
	 */
	public static boolean checkRequestJson(Context ctx, JSONObject all, boolean alert) {
		if (all == null)
			return false;

		if ("succ".equals(all.optString("rsp")))
			return true;

		if (TextUtils.equals(all.optString("res"), "need_login")) {
			ctx.startActivity(AgentActivity.intentForFragment(ctx, AgentActivity.FRAGMENT_ACCOUNT_LOGIN)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			AgentApplication.getLoginedUser(ctx).setIsLogined(false);
			AgentApplication.getLoginedUser(ctx).setUserInfo(null);
			return false;
		}

		// 提示语不为空则提示用户
		String dataStr = all.isNull("data") ? EMPTY_STR : all.optString("data");
		if (!TextUtils.isEmpty(dataStr) && alert) {
			try {
				JSONObject dataJson = new JSONObject(dataStr);
				alert(ctx, decodeUnicode(dataJson.optString("msg")));
			} catch (Exception e) {
				alert(ctx, decodeUnicode(dataStr));
			}
		}
		return false;
	}

	/**
	 * 获取默认的图片缓存类
	 * 
	 * @param context
	 * @param resources
	 * @return
	 */
	public static ImageLoader getDefaultImageLoader(final Context context, final Resources resources) {
		return getDefaultImageLoader(context, resources, true);
	}

	/**
	 * 获取默认的图片缓存类
	 * 
	 * @param context
	 * @param resources
	 * @return
	 */
	public static ImageLoader getDefaultImageLoader(final Context context, final Resources resources,
			final boolean scaleLimit) {
		ImageLoader mImageLoader = ImageLoader.getInstance(context);
		mImageLoader.setDisplayImageCallback(new DisplayImageCallback() {
			@Override
			public boolean displayImage(View v, Drawable drawable) {
				((ImageView) v).setImageDrawable(drawable);
				return true;
			}
		});
		mImageLoader.setDecodeImageCallback(new DecodeImageCallback() {
			@Override
			public Drawable decodeImage(Object object) {
				if (object != null && object instanceof Uri) {
					Uri imageUri = (Uri) object;
					Bitmap bitmap = CacheUtils.getImageIntelligent(imageUri.toString(), scaleLimit);
					if (bitmap != null)
						return new BitmapDrawable(resources, bitmap);
				}
				return null;
			}
		});

		return mImageLoader;
	}

	/**
	 * 改变Resources的语言
	 * 
	 * @param res
	 * @param locale
	 */
	@SuppressLint("NewApi")
	public static void changeResourceLocale(Resources res, Locale locale) {
		Configuration config = res.getConfiguration();
		DisplayMetrics dm = res.getDisplayMetrics();
		try {
			config.setLocale(locale);
		} catch (NoSuchMethodError e) {
			config.locale = locale;
		}
		res.updateConfiguration(config, dm);
	}

	/**
	 * 移除View
	 * 
	 * @param view
	 */
	public static void removeFromSuperView(View view) {
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null)
			parent.removeView(view);
	}

	/**
	 * 解析地址area_id
	 * 
	 * @param address
	 * @return
	 */
	public static String parseAddressId(JSONObject address) {
		String area = address.optString("area");
		int index = area.lastIndexOf(":");
		if (index == -1)
			return EMPTY_STR;

		return area.substring(index + 1);
	}

	/**
	 * 计算文件夹大小
	 * 
	 * @param file
	 * @return
	 */
	public static long countFileSize(File file) {
		long size = 0;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (int i = 0, c = files.length; i < c; i++)
					size += countFileSize(files[i]);
			}
		} else {
			size += file.length();
		}

		return size;
	}

	/**
	 * 删除所有文件
	 * 
	 * @param file
	 * @return
	 */
	public static void deleteAllFiles(File file) {
		if (file != null && file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (int i = 0, c = files.length; i < c; i++)
					deleteAllFiles(files[i]);
			}
		} else {
			file.delete();
		}
	}

	/**
	 * 是否为线下支付
	 * 
	 * @param payinfo
	 * @return
	 */
	public static boolean isOfflinePayment(JSONObject payinfo) {
		return TextUtils.equals("offline", payinfo.optString("pay_app_id"))
				|| TextUtils.equals("offline", payinfo.optString("app_id"));
	}

	/**
	 * 是否App内支付
	 * 
	 * @param payinfo
	 * @return
	 */
	public static boolean isOfflinePayType(JSONObject payinfo) {
		return !payinfo.optBoolean("app_pay_type", false);
	}

	/**
	 * 绘制圆形的avatar
	 * 
	 * @param avatar
	 *            源图片
	 * @param mask1
	 *            源图会按照mask1的形状切掉透明区域
	 * @param mask2
	 *            mask2会盖在被切掉的图片上 <br>
	 * 
	 * @return 新的图片
	 */
	public static Bitmap placeImage(Bitmap avatar, Bitmap mask1, Bitmap mask2) {
		final Paint p = new Paint();
		p.setAntiAlias(true);
		p.setFilterBitmap(true);
		int width = mask1.getWidth(), height = mask1.getHeight();
		RectF dest = new RectF(0, 0, width, height);
		final Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(b);

		// draw the whole canvas as transparent
		p.setColor(Color.TRANSPARENT);
		c.drawPaint(p);
		// draw the mask normally
		p.setColor(0xFFFFFFFF);
		c.drawBitmap(mask1, null, dest, p);

		Paint pdpaint = new Paint();
		pdpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		pdpaint.setStyle(Paint.Style.STROKE);
		c.drawRect(0, 0, width, height, pdpaint);
		c.drawBitmap(avatar, null, dest, pdpaint);
		c.drawBitmap(mask2, null, dest, p);
		avatar.recycle();
		return b;
	}

	/**
	 * 获取默认的头像缓存类
	 * 
	 * @param context
	 * @param resources
	 * @return
	 */
	public static ImageLoader getDefaultAvatarLoader(final Context context, final Resources resources) {
		ImageLoader mImageLoader = ImageLoader.getInstance(context);
		mImageLoader.setDefautImage(R.drawable.account_avatar);
		mImageLoader.setDisplayImageCallback(new DisplayImageCallback() {
			@Override
			public boolean displayImage(View v, Drawable drawable) {
				((ImageView) v).setImageDrawable(drawable);
				return true;
			}
		});
		mImageLoader.setDecodeImageCallback(new DecodeImageCallback() {
			@Override
			public Drawable decodeImage(Object object) {
				if (object != null && object instanceof Uri) {
					Uri imageUri = (Uri) object;
					Bitmap bitmap = CacheUtils.getImageIntelligent(imageUri.toString(), true);
					if (bitmap != null) {
						AgentApplication app = AgentApplication.getApp(context);
						return new BitmapDrawable(resources, placeImage(bitmap, app.mAvatarMask, app.mAvatarCover));
					}
				}
				return null;
			}
		});

		return mImageLoader;
	}

	/**
	 * 检测并打开第三方支付
	 * 
	 * @param mActivity
	 * @param all
	 */
	public static boolean startThirdPartyPayment(Activity mActivity, JSONObject all) {
		if (!TextUtils.isEmpty(all.optString("res"))) {
			mActivity.startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ARTICLE_READER)
					.putExtra(Run.EXTRA_HTML, all.optString("res")));
			return true;
		}
		return false;
	}

	/**
	 * 检测订单是否付款成功
	 * 
	 * @param all
	 */
	public static boolean checkPaymentStatus(Activity activity, JSONObject all) {
		try {
			JSONObject data = all.optJSONObject("data");
			JSONObject order = data.optJSONObject("order");
			Run.alert(activity, data.optString("msg"));
			return (order.optInt("pay_status") == 1);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 加密字符串，将中间的字符转换成“***”
	 * 
	 * @param source
	 *            源字符串
	 * @param secretLen
	 *            加密的位数
	 * @return <br />
	 *         加密后的字符串
	 */
	public static String makeSecretString(String source, int secretLen) {
		if (TextUtils.isEmpty(source) || source.length() < secretLen + 2)
			return source;

		// 替换的字符串
		StringBuilder secStr = new StringBuilder();
		for (int i = 0; i < secretLen; i++)
			secStr.append("*");

		int start = (source.length() - secretLen) / 2;
		int end = start + secretLen;
		return source.replaceFirst(source.substring(start, end), secStr.toString());
	}

	/**
	 * 生成桌面快捷方式
	 * 
	 * @param context
	 */
	public static void createShortcut(Context context) {
		Intent main = new Intent();
		main.setComponent(new ComponentName(context, LauncherActivity.class));
		Intent shortcutIntent = new Intent(ACTION_INSTALL_SHORTCUT);
		shortcutIntent.putExtra(EXTRA_SHORTCUT_DUPLICATE, true);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, main);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				ShortcutIconResource.fromContext(context, R.drawable.ic_launcher));
		context.sendBroadcast(shortcutIntent);
	}

	/**
	 * 解析短信
	 * 
	 * @param intent
	 * @return
	 */
	// @SuppressLint("NewApi")
	// public static String handleSmsReceived(Intent intent) {
	// SmsMessage[] msgs = Sms.Intents.getMessagesFromIntent(intent);
	//
	// // 读取长信息内容
	// StringBuilder body = new StringBuilder();
	// int len = (msgs != null) ? msgs.length : 0;
	// for (int i = 0; i < len; i++)
	// body.append(msgs[i].getDisplayMessageBody());
	// return body.toString();
	// }

}