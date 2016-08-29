package com.qianseit.westore.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import cn.shopex.ecstore.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.ui.CustomProgrssDialog;

public class Util extends Comm {

	// 应用市场链接
	public static final String PLAY_STORE = "https://play.google.com/store/apps/details?id=";

	// 打开应用详情所需数据
	public static final String pkg = "pkg";
	public static final String pkgo = "com.android.settings.ApplicationPkgName";
	public static final String settingspkg = "com.android.settings";
	public static final String appdetails = "com.android.settings.InstalledAppDetails";
	public static final String appdetailsnew = "android.settings.APPLICATION_DETAILS_SETTINGS";

	// 桌面快捷方式
	public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
	public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
	public static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
	public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";

	// 屏幕方向发生改变
	public static final String EXTRA_ORIENTATION = "com.mxzrct.frame.EXTRA_ORIENTATION";
	public static final String ACTION_SCREEN_ORINET_CHANGED = "com.mxzrct.frame.action.ACTION_SCREEN_ORINET_CHANGED";

	public static final String EMPTY_STR = "";

	/**
	 * 保存配置文件
	 * 
	 * @param ctx
	 * @param key
	 * @param value
	 */
	public static void savePrefs(Context ctx, String key, Object value) {
		Editor prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
				.edit();
		if (value instanceof Boolean) {
			prefs.putBoolean(key, (Boolean) value);
		} else if (value instanceof Integer) {
			prefs.putInt(key, (Integer) value);
		} else if (value instanceof String) {
			prefs.putString(key, (String) value);
		} else if (value instanceof Long) {
			prefs.putLong(key, (Long) value);
		} else if (value instanceof Float) {
			prefs.putFloat(key, (Float) value);
		} else {
			throw new UnsupportedOperationException();
		}

		// else if (value instanceof Set) {
		// try {
		// Set<String> set = (Set<String>) value;
		// prefs.putStringSet(key, set);
		// } catch (Exception e) {
		// throw new UnsupportedOperationException();
		// }
		// }

		prefs.commit();
	}

	// //////////////////////////////加载配置文件中的信息////////////////////////////////
	public static String loadOptionString(Context context, String name,
			String defValue) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getString(name, defValue);
	}

	public static int loadOptionInt(Context context, String name, int defValue) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getInt(name, defValue);
	}

	public static boolean loadOptionBoolean(Context context, String name,
			boolean defValue) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getBoolean(name, defValue);
	}

	public static long loadOptionLong(Context context, String name,
			long defValue) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getLong(name, defValue);
	}

	public static float loadOptionFloat(Context context, String name,
			float defValue) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getFloat(name, defValue);
	}

	// public static Set<String> loadOptionStringSet(Context context, String
	// name) {
	// SharedPreferences prefs = PreferenceManager
	// .getDefaultSharedPreferences(context);
	//
	// return prefs.getStringSet(name, null);
	// }

	public static void removeOption(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		prefs.edit().remove(key).commit();
	}

	// 是否包含key
	public static boolean containsOption(Context context, String key) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.contains(key);
	}

	// //////////////////////////////加载配置文件中的信息////////////////////////////////

	/**
	 * 发送广播
	 * 
	 * @param intent
	 */
	public static void sendMyBroadcast(Context a, Intent intent) {
		intent.setPackage(a.getPackageName());
		a.sendBroadcast(intent);
	}

	/**
	 * 发送广播
	 * 
	 * @param action
	 */
	public static void sendMyBroadcast(Context a, String action) {
		Intent intent = new Intent(action);
		sendMyBroadcast(a, intent);
	}

	/**
	 * 去除字符串中的空格,回车,换行符,制表符
	 * */
	public static String trim(String str) {
		str = str.trim();
		return str.replaceAll("\\s*|\t|\r|\n", "");
	}

	/**
	 * 复制文件
	 * 
	 * @param from
	 * @param to
	 * 
	 * @return false 失败
	 */
	public static final boolean copyAsset2File(Context context,
			String assetName, String to) {
		try {
			AssetManager am = context.getAssets();
			InputStream from = am.open(assetName);
			return copyStream2File(from, to);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 复制文件
	 * 
	 * @param from
	 * @param to
	 * 
	 * @return false 失败
	 */
	public static final boolean copyStream2File(InputStream from, String to) {
		File dest = new File(to);
		BufferedOutputStream bos = null;
		try {
			// 新建目标文件
			if (!dest.exists()) {
				dest.getParentFile().mkdirs();
				dest.createNewFile();
			}

			bos = new BufferedOutputStream(new FileOutputStream(dest));
			byte[] buf = new byte[512];
			int size = from.read(buf);
			while (size != -1) {
				bos.write(buf, 0, size);
				size = from.read(buf);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
					bos = null;
				}
				if (from != null) {
					from.close();
					from = null;
				}
			} catch (Exception e) {
			}
		}

		return false;
	}

	/**
	 * 复制String到文件
	 * 
	 * @param from
	 * @param to
	 */
	public static final void copyString2File(String from, String to) {
		BufferedWriter writer = null;
		try {
			File dest = new File(to);
			if (!dest.exists()) {
				dest.getParentFile().mkdirs();
				dest.createNewFile();
			}
			// 复制
			writer = new BufferedWriter(new FileWriter(to));
			writer.write(from);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 添加String到文件
	 * 
	 * @param from
	 * @param to
	 */
	public static final void appendString2File(String from, String to) {
		FileWriter writer = null;
		try {
			File dest = new File(to);
			if (!dest.exists()) {
				dest.getParentFile().mkdirs();
				dest.createNewFile();
			}

			writer = new FileWriter(dest, true);
			writer.append(from);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 复制文件到另一个路径
	 * 
	 * @param from
	 * @param to
	 * 
	 * @return false 失败
	 */
	public static final boolean copyFile2File(String from, String to) {
		try {
			return copyStream2File(new FileInputStream(from), to);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 是否eMail
	 * 
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmailAddr(String strEmail) {
		if (TextUtils.isEmpty(strEmail)) {
			return false;
		}

		Matcher match = Patterns.EMAIL_ADDRESS.matcher(strEmail);
		return match.matches();
	}

	/**
	 * 判断时候为手机号
	 * 
	 * @param number
	 *            the input number to be tested
	 * @return 返回true则是手机号
	 */
	public static boolean isPhoneNumber(String number) {
		if (TextUtils.isEmpty(number)) {
			return false;
		}

		Pattern PHONE = Pattern.compile("(\\+[0-9]+[\\- \\.]*)?"
				+ "(\\([0-9]+\\)[\\- \\.]*)?"
				+ "([0-9][0-9\\- \\.][0-9\\- \\.]+[0-9])");
		Matcher match = PHONE.matcher(number);
		return match.matches();
	}

	// 格式化后的时间
	public static String getFormatedCurrenTime() {
		String time = android.text.format.DateFormat.format("yyyyMMddkkmmss",
				System.currentTimeMillis()).toString();
		return time;
	}

	// 时间转long
	public static long getTimeFromString(String timeStr) {
		try {
			return (new SimpleDateFormat("yyyyMMddkkmmss")).parse(timeStr)
					.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}

	/**
	 * 检查是否有可用的网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetwork(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}

	/**
	 * 获取软件版本号
	 * 
	 * @param a
	 * @return
	 */
	public static int getVersionCode(Context a) {
		int ver = 0;
		try {
			String pkgName = a.getPackageName();
			PackageManager pm = a.getPackageManager();
			ver = pm.getPackageInfo(pkgName, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return ver;
	}

	/**
	 * 获取软件版本号
	 * 
	 * @param a
	 * @return
	 */
	public static String getVersionName(Context a) {
		String version = "1.0";
		try {
			String pkgName = a.getPackageName();
			PackageManager pm = a.getPackageManager();
			version = pm.getPackageInfo(pkgName, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}

	// 显示dialog
	public static CustomProgrssDialog showLoadingDialog(Activity a,
			String title, String msg) {
		CustomProgrssDialog mDialog = null;
		try {
			if (TextUtils.isEmpty(msg)) {
				msg = a.getString(R.string.loading_text);
			}
			mDialog = CustomProgrssDialog.show(a, title, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return mDialog;
	}

	// 隐藏dialog
	public static void hideLoading(CustomDialog dialog) {
		try {
			if (dialog != null)
				dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 让手机震动指定毫秒数
	 * 
	 * @param ctx
	 */
	public static void vibrate(Context ctx, long milliseconds) {
		Vibrator vibrate = (Vibrator) ctx
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibrate.vibrate(milliseconds);
	}

	/**
	 * 分享文字内容
	 * 
	 * @param context
	 * @param subject
	 * @param msg
	 */
	public static void shareText(Context context, String subject, String msg) {
		shareText(context, subject, msg, null);
	}

	/**
	 * 分享文字与图片内容
	 * 
	 * @param context
	 * @param siteUrl
	 */
	public static void shareText(Context context, String subject, String msg,
			String path) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.setType("text/plain");

		// 有图片则分享图片
		if (!TextUtils.isEmpty(path)) {
			intent.setType("image/*");
			Uri uri = Uri.parse(buildString("file://", path));
			intent.putExtra(Intent.EXTRA_STREAM, uri);
		}

		Intent sendIntent = Intent.createChooser(intent,
				context.getString(R.string.share));
		sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(sendIntent);
		} catch (Exception e) {
		}
	}

	/**
	 * 通过浏览器打开指定Url
	 * 
	 * @param a
	 * @param url
	 */
	public static void openBrowser(Context a, String url) {
		try {
			// 打开网页进行密码找回
			Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			urlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			a.startActivity(urlIntent);
		} catch (Exception e) {
		}
	}

	public static void openAppInPlayStore(Context a, String pkgname) {
		if (TextUtils.isEmpty(pkgname))
			pkgname = a.getPackageName();

		try {
			// 打开市场
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=" + pkgname));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			a.startActivity(intent);
		} catch (Exception e) {
			try {
				// 打开浏览器
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(PLAY_STORE + pkgname));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				a.startActivity(intent);
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * 显示通知栏
	 * 
	 * @param context
	 * @param notification
	 */
	public static void showNotify(Context context, int notifyId,
			Notification notification) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(notifyId, notification);
	}

	/**
	 * 评价应用
	 * 
	 * @param a
	 */
	public static void evaluateApp(Activity a) {
		openAppInPlayStore(a, null);
	}

	/**
	 * 读取asset的文件
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String readAssetStr(Context context, String fileName) {
		String result = "";
		try {
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					context.getAssets().open(fileName)));
			String str = null;
			while ((str = bis.readLine()) != null) {
				result += str;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 返回含有颜色值的html片段
	 * 
	 * @param content
	 * @param color
	 */
	public static String setColorHtml(String content, String color) {
		return buildString("<font color=", color, ">", content, "</font>");
	}

	/**
	 * 获取TextView或其子类的String值
	 * 
	 * @param v
	 * @return
	 */
	public static String getValue(TextView v) {
		return v.getText().toString();
	}

	/**
	 * 判断View是否为可见的
	 * 
	 * @param view
	 * @return
	 */
	public static boolean isVisible(View view) {
		return view.getVisibility() == View.VISIBLE;
	}

	/**
	 * 将dip转换为px
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dipValue, context.getResources().getDisplayMetrics());
	}

	/**
	 * 将px转换为dip
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	/**
	 * sp 转 px
	 * @param context
	 * @param spValue
	 * @return
	 */
	public static int sp2px(Context context, float spValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (spValue * fontScale + 0.5f); 
    } 
	
	/**
     * 将px值转换为sp值
     * 
     * @param pxValue
     * @param fontScale
     *     
     * @return
     */ 
    public static int px2sp(Context context, float pxValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (pxValue / fontScale + 0.5f); 
    } 

	/**
	 * 打开软键盘
	 * 
	 * @param mContext
	 * @param view
	 *            当前焦点
	 */
	public static void openSoftInputMethod(Context mContext, View view) {
		try {
			((InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.showSoftInput(view, 0);// 打开软键盘
		} catch (Exception e) {
		}
	}

	/**
	 * 关闭软键盘
	 * 
	 * @param mContext
	 * @param view
	 *            当前焦点
	 */
	public static void hideSoftInputMethod(Context mContext, View view) {
		try {
			// 隐藏软键盘
			((InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);
		} catch (Exception e) {
		}
	}

	/**
	 * 关闭软键盘
	 * 
	 * @param mContext
	 * @param view
	 *            当前焦点
	 */
	public static void showSoftInputMethod(Context mContext, View view) {
		try {
			// 隐藏软键盘
			((InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE))
					.showSoftInputFromInputMethod(view.getWindowToken(), 0);
		} catch (Exception e) {
		}
	}

	/**
	 * 调用系统卸载方法
	 * 
	 * @param context
	 * @param pkgName
	 */
	public static void uninstallApp(Context context, String pkgName) {
		if (context == null || TextUtils.isEmpty(pkgName))
			return;

		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
				Uri.fromParts("package", pkgName, null));
		uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
	}

	/**
	 * 查看应用详情
	 * 
	 * @param context
	 * @param pkgName
	 */
	public static void showAppDetail(Context context, String pkgName) {
		if (context == null || TextUtils.isEmpty(pkgName))
			return;

		String EXTRA_PKGNAME = Build.VERSION.SDK_INT < 8 ? pkgo : pkg;

		if (Build.VERSION.SDK_INT < 9) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(settingspkg, appdetails);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(EXTRA_PKGNAME, pkgName);
			context.startActivity(intent);
		} else {
			Uri uri = Uri.fromParts("package", pkgName, null);
			Intent intent = new Intent(appdetailsnew, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	/**
	 * 获得bitmap点击效果的StateListDrawable <br/>
	 * 获得半透明的点击效果(点击状态为半透明)
	 * 
	 * @param ctx
	 * @param resid
	 * @param bmTransate
	 *            0.0-1.0之间的值
	 * @return
	 */
	public static Drawable getDrawableList(Context ctx, int resid,
			float bmTransate) {
		return getDrawableList(
				BitmapFactory.decodeResource(ctx.getResources(), resid),
				bmTransate);
	}

	/**
	 * 获得bitmap点击效果的StateListDrawable <br/>
	 * 获得半透明的点击效果(点击状态为半透明)
	 * 
	 * @param drawable
	 * @param bmTransate
	 *            0.0-1.0之间的值
	 * @return
	 */
	public static Drawable getDrawableList(Drawable drawable, float bmTransate) {
		return getDrawableList(((BitmapDrawable) drawable).getBitmap(),
				bmTransate);
	}

	/**
	 * 获得bitmap点击效果的StateListDrawable <br/>
	 * 获得半透明的点击效果(点击状态为半透明)
	 * 
	 * @param bm
	 * @param bmTransate
	 *            0.0-1.0之间的值
	 * @return
	 */
	public static Drawable getDrawableList(Bitmap bm, float bmTransate) {
		Bitmap pressed_bm = getTranslucentBitmap(bm, bmTransate);
		StateListDrawable mDrawable = new StateListDrawable();
		mDrawable.addState(new int[] { android.R.attr.state_pressed },
				new BitmapDrawable(Resources.getSystem(), pressed_bm));
		mDrawable.addState(new int[] { android.R.attr.state_focused },
				new BitmapDrawable(Resources.getSystem(), pressed_bm));
		mDrawable.addState(StateSet.WILD_CARD,
				new BitmapDrawable(Resources.getSystem(), bm));
		return mDrawable;
	}

	/**
	 * 获得bitmap点击效果的StateListDrawable <br/>
	 * 获得半透明的点击效果(点击状态为半透明)
	 * 
	 * @param bm
	 * @param bmTransate
	 *            0.0-1.0之间的值
	 * @return
	 */
	public static Drawable getDrawableList(Drawable src) {
		Drawable pressDrawable = src.mutate();
		pressDrawable.setColorFilter(Color.parseColor("#66000000"),
				PorterDuff.Mode.SRC_ATOP);
		StateListDrawable mDrawable = new StateListDrawable();
		mDrawable.addState(new int[] { android.R.attr.state_pressed },
				pressDrawable);
		mDrawable.addState(new int[] { android.R.attr.state_focused },
				pressDrawable);
		mDrawable.addState(StateSet.WILD_CARD, src);
		return mDrawable;
	}

	/**
	 * 获得bitmap点击效果的StateListDrawable <br/>
	 * 获得相反的半透明点击效果(普通状态为半透明)
	 * 
	 * @param bm
	 * @param bmTransate
	 *            0.0-1.0之间的值O
	 * @return
	 */
	public static Drawable getDrawableListReverse(Bitmap bm, float bmTransate) {
		Bitmap pressed_bm = getTranslucentBitmap(bm, bmTransate);

		StateListDrawable mDrawable = new StateListDrawable();
		mDrawable.addState(new int[] { android.R.attr.state_pressed },
				new BitmapDrawable(Resources.getSystem(), bm));
		mDrawable.addState(new int[] { android.R.attr.state_focused },
				new BitmapDrawable(Resources.getSystem(), bm));
		mDrawable.addState(StateSet.WILD_CARD,
				new BitmapDrawable(Resources.getSystem(), pressed_bm));

		return mDrawable;
	}

	/**
	 * 返回半透明的bitmap
	 * 
	 * @param map
	 * @return
	 */
	private static Bitmap getTranslucentBitmap(Bitmap map, float translate) {
		Bitmap pressed_map = Bitmap.createBitmap(map.getWidth(),
				map.getHeight(), Config.ARGB_8888);
		Canvas pressed_canvas = new Canvas(pressed_map);
		Paint p = new Paint();
		p.setColorFilter(new ColorMatrixColorFilter(
				getContrastTranslateOnly(translate)));
		pressed_canvas.drawBitmap(map, 0, 0, p);
		return pressed_map;
	}

	/**
	 * 改变ColorMatrix透明度值
	 * 
	 * @param map
	 * @return
	 */
	private static ColorMatrix getContrastTranslateOnly(float contrast) {
		ColorMatrix cm = new ColorMatrix();
		float scale = contrast + 1.f;
		float translate = (-.5f * scale + .5f) * 255.f;
		cm.set(new float[]{1, 0, 0, 0, translate, 0, 1, 0, 0, translate, 0,
				0, 1, 0, translate, 0, 0, 0, 1, 0});
		return cm;
	}

	/**
	 * 生成圆角矩形图片,角度为宽度一半<br />
	 * 相当于生成圆形图片
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createCornerRadiusBitmap(Bitmap bitmap) {
		int imgWidth = bitmap.getWidth();
		int radius = (imgWidth - 2) / 2;
		return createCornerRadiusBitmap(bitmap, radius);
	}

	/**
	 * 生成圆角矩形图片
	 * 
	 * @param bitmap
	 * @param cornerRadius
	 *            角度
	 * @return
	 */
	public static Bitmap createCornerRadiusBitmap(Bitmap bitmap,
			int cornerRadius) {
		try {
			int radius = cornerRadius;
			int imgWidth = bitmap.getWidth();
			int imgHeight = bitmap.getHeight();
			Rect rect = new Rect(0, 0, imgWidth - 2, imgHeight - 2);

			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);

			Bitmap output = Bitmap.createBitmap(imgWidth, imgHeight,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			canvas.drawARGB(0, 0, 0, 0);

			RectF rectF = new RectF(rect);
			canvas.drawRoundRect(rectF, radius, radius, paint);

			Rect src = new Rect(rect);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, src, rect, paint);

			// Path path = new Path();
			// path.addCircle(imgWidth / 2, imgHeight / 2, imgWidth / 2,
			// Path.Direction.CCW);
			// paint.setColor(Color.RED);
			// paint.setStrokeWidth(2);
			// canvas.drawPath(path, paint);

			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

	/**
	 * 设置阴影Bitmap
	 * 
	 * @param bitmap
	 */
	public static Bitmap createShadowBitmap(Bitmap bitmap) {
		BlurMaskFilter blurFilter = new BlurMaskFilter(3,
				BlurMaskFilter.Blur.OUTER);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);
		int[] offsetXY = new int[2];
		Bitmap shadowBm1 = bitmap.extractAlpha(shadowPaint, offsetXY);
		Bitmap shadowBm2 = shadowBm1.copy(Config.ARGB_8888, true);

		Canvas c = new Canvas(shadowBm2);
		c.drawBitmap(bitmap, 0, 0, null);
		bitmap.recycle();
		shadowBm1.recycle();
		return shadowBm2;
	}

	/**
	 * 生成平铺的背景图
	 * 
	 * @param context
	 * @param resid
	 * @return
	 */
	public static Drawable makeRepeatBitmapDrawable(Context context, int resid) {
		Resources res = context.getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res, resid);
		return makeRepeatBitmapDrawable(context, bitmap);
	}

	/**
	 * 生成平铺的背景图
	 * 
	 * @param context
	 * @param bitmap
	 * @return
	 */
	public static Drawable makeRepeatBitmapDrawable(Context ctx, Bitmap bitmap) {
		Resources res = ctx.getResources();
		BitmapDrawable bd = new BitmapDrawable(res, bitmap);
		bd.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		return bd;
	}

	/**
	 * 获得渐变显示的Drawable
	 * 
	 * @param d
	 * @return
	 */
	public static TransitionDrawable makeTransitionDrawable(Drawable d) {
		ColorDrawable cd = new ColorDrawable(Color.TRANSPARENT);
		Drawable[] ds = new Drawable[] { cd, d };
		TransitionDrawable td = new TransitionDrawable(ds);
		td.startTransition(350);
		return td;
	}

	/**
	 * 打开图片选择
	 * 
	 * @return
	 */
	public static Intent pickerPhotoIntent(int width, int heigth) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", width);
		intent.putExtra("aspectY", heigth);
		intent.putExtra("outputX", width);
		intent.putExtra("outputY", heigth);
		intent.putExtra("return-data", true);
		return intent;
	}

	/**
	 * 安全取消注册的Receiver
	 * 
	 * @param context
	 * @param receiver
	 */
	public static void unregistReceiverSafety(Context context,
			BroadcastReceiver receiver) {
		try {
			context.unregisterReceiver(receiver);
		} catch (Exception e) {
		}
	}

	/**
	 * 获得屏幕相对的宽度和高度<br />
	 * 横屏时候宽度大于高度，竖屏时候高度大于宽度
	 * 
	 * @param windowManager
	 * @return <br />
	 *         point.x：宽度，point.y：高度
	 */
	public static Point getScreenSize(WindowManager windowManager) {
		Point point = new Point(0, 0);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			point.x = windowManager.getDefaultDisplay().getWidth();
			point.y = windowManager.getDefaultDisplay().getHeight();
		} else {
			windowManager.getDefaultDisplay().getSize(point);
		}

		return point;
	}

	/**
	 * 取消通知栏
	 * 
	 * @param context
	 * @param notification
	 */
	public final static void cancelNotify(Context context, int notifyId) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(notifyId);
	}

	/**
	 * 评价应用
	 * 
	 * @param a
	 */
	public static void evaluateApp(Context a) {
		openAppInPlayStore(a, null);
	}

	/**
	 * 显示退出App提示框
	 * 
	 * @param a
	 */
	public static CustomDialog showExitAppDialog(final Activity a) {
		CustomDialog mDialog = new CustomDialog(a);
		mDialog.setShowBottomDivider(true);
		mDialog.setShowTitleDivider(false);
		mDialog.setCancelable(true);
		mDialog.setNegativeButton(android.R.string.cancel, null);
		mDialog.setPositiveButton(android.R.string.ok,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						a.finish();
					}
				}).show();
		return mDialog;
	}

	/**
	 * 返回设备IMEI，空则返回null
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getDeviceIMEI(Context ctx) {
		try {
			TelephonyManager tm = (TelephonyManager) ctx
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getDeviceId();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取指定范围的随机正整数，包含范围
	 * 
	 * @param min
	 *            最小
	 * @param max
	 *            最大
	 * @return
	 */
	public static int getRandomInt(int min, int max) {
		int minValue = Math.min(min, max);
		int maxValue = Math.max(min, max);
		return minValue + (int) (Math.random() * (maxValue - minValue + 1));
	}

	/**
	 * 执行异步任务
	 * 
	 * @param task
	 * @param params
	 */
	public static void excuteAsyncTask(AsyncTask task, Object... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		else
			task.execute(params);
	}

	/**
	 * 执行异步任务
	 * 
	 * @param task
	 * @param params
	 */
	public static void excuteJsonTask(JsonTask task, JsonTaskHandler... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		else
			task.execute(params);
	}

	/**
	 * unicode转换为中文
	 * 
	 * @param s
	 * @return
	 */
	public static String decodeUnicode(String s) {
		Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
		Matcher matcher = reUnicode.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (matcher.find()) {
			matcher.appendReplacement(sb, Character.toString((char) Integer
					.parseInt(matcher.group(1), 16)));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	public static String calculateRemainTime(long remainTime){
		long seconds = remainTime % 60;
		long minutes = remainTime / 60 % 60;
		long hours = remainTime / (60 * 60) % 24;
		long day = remainTime / (60 * 60 * 24);
		long hour_decade = hours / 10;
		long hour_unit = hours % 10;

		long min_decade = minutes / 10;
		long min_unit = minutes % 10;

		long sec_decade = seconds / 10;
		long sec_unit = seconds % 10;
		
		String time = "";
		if (day > 0) {
			time = day+"天"+hour_decade+hour_unit+":"+min_decade+min_unit+":"+
					sec_decade+sec_unit;
		} else {
			if (hour_decade == 0 && hour_unit == 0) {
				time = ""+min_decade+min_unit+":"+
						sec_decade+sec_unit;
			} else {
				time = ""+hour_decade+hour_unit+":"+min_decade+min_unit+":"+
						sec_decade+sec_unit;
			}
		}
		return time;
	}
	
	/**
	 * 用于将给定的内容生成成一维码 注：目前生成内容为中文的话将直接报错
	 * @param context
	 * @param content 将要生成一维码的内容
	 * @return
	 * @throws WriterException
	 */
	public static Bitmap CreateOneDCode(Context context, String content){
		BitMatrix matrix = null;
		Bitmap bitmap = null;
		if (TextUtils.isEmpty(content)) {
			return null;
		}
		try {
			matrix = new MultiFormatWriter().encode(content,
					BarcodeFormat.CODE_128, dip2px(context, 250), dip2px(context, 50));
			int width = matrix.getWidth(); //条码的宽度
			int height = matrix.getHeight(); //条码的高度
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (matrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					}
				}
			}
			bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 将指定的内容生成成二维码 * 
	 * @param content 将要生成二维码的内容  
	 * @return 返回生成好的二维码事件
	 * @throws
	 * WriterException WriterException异常
	 */
	public static Bitmap CreateTwoDCode(Context context ,String content ,int dipWidth ,int dipHeight){
		try{
			if (dipWidth < 10) {
				dipWidth = 50;
			}
			if(dipHeight < 10){
				dipHeight = 50;
			}
			Hashtable hints = new Hashtable();
			hints.put(EncodeHintType.MARGIN, 0);
			hints.put(EncodeHintType.CHARACTER_SET,"utf-8");
			BitMatrix matrix = new MultiFormatWriter().encode(content,
					BarcodeFormat.QR_CODE, dip2px(context, dipWidth), dip2px(context, dipWidth),hints);
			int width = matrix.getWidth();
			int height = matrix.getHeight(); // 二维矩阵转为一维像素数组,也就是一直横着排了
			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (matrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888); // 通过像素数组生成bitmap,具体参考api
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch(WriterException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static int getViewHeight(View view){
	    view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    return view.getMeasuredHeight();
	}

	/**
	 * 动态设置listview的高
	 *
	 * */
	public static void setListViewHeight(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

}
