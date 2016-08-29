package com.qianseit.westore;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;

import org.json.JSONObject;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.TextUtils;

import cn.sharesdk.framework.ShareSDK;

import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.activity.account.AccountSettingFragment;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import com.tencent.android.tpush.XGNotifaction;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushNotifactionCallback;
import com.tencent.android.tpush.service.XGPushService;
import com.tencent.bugly.crashreport.CrashReport;

import cn.shopex.ecstore.R;

public class AgentApplication extends Application {
    private LoginedUser mLoginedUser;
    private ImageLoader mAvatarLoader;
    private VolleyImageLoader mImageLoader;

    public Bitmap mAvatarMask, mAvatarCover;

    private ArrayList<Activity> mRecentActivies = new ArrayList<Activity>();

    public JSONObject mOrderDetail;
    public boolean gotoMyFavorite = false;
    private Typeface typeface;

    @Override
    public void onCreate() {
        super.onCreate();
        // bugly
        CrashReport.initCrashReport(this, "900015437", false);


        // ShareSDK
        ShareSDK.initSDK(this, "8f5830089d1f");
        // 信鸽推送
        XGPushManager.registerPush(this);
        Intent service = new Intent(this, XGPushService.class);
        this.startService(service);


        XGPushManager.setNotifactionCallback(new XGPushNotifactionCallback() {

            @Override
            public void handleNotify(XGNotifaction xGNotifaction) {
                // 获取标签、内容、自定义内容
//				String title = xGNotifaction.getTitle();
//				String content = xGNotifaction.getContent();
//				String customContent = xGNotifaction.getCustomContent();
                // 其它的处理
                // 根据设置里面的勿扰模式决定要不要显示推送
                if (!Run.loadOptionBoolean(AgentApplication.this, AccountSettingFragment.WURAOMODE, false)) {
                    xGNotifaction.doNotify();
                }
            }
        });

        // 头像
        Resources resources = getResources();
        mAvatarMask = BitmapFactory.decodeResource(resources,
                R.drawable.account_avatar);
        mAvatarCover = BitmapFactory.decodeResource(resources,
                R.drawable.westore_avatar_hole);

        // 错误信息收集
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        // cookie管理
        CookieManager cookieManager = null;
        cookieManager = new CookieManager(null, null);
        CookieHandler.setDefault(cookieManager);

        mLoginedUser = LoginedUser.getInstance();
        mAvatarLoader = Run.getDefaultAvatarLoader(this, resources);
        mImageLoader = VolleyImageLoader.getImageLoader(this);

        this.userAutoLogin();
        //typeface = Typeface.createFromAsset(this.getAssets(), "fonts/huawenblackfont.ttf");

    }

    public Typeface getTypeface() {
        return typeface;
    }

    // 自动登录
    public void userAutoLogin() {
        if (!TextUtils.isEmpty(Run.loadOptionString(this,
                Run.pk_logined_username, Run.EMPTY_STR))
                && !TextUtils.isEmpty(Run.loadOptionString(this,
                Run.pk_logined_user_password, Run.EMPTY_STR))) {
            Run.excuteJsonTask(
                    new JsonTask(),
                    new AccountLoginFragment.UserLoginTask(null, Run
                            .loadOptionString(this, Run.pk_logined_username,
                                    Run.EMPTY_STR), Run.loadOptionString(this,
                            Run.pk_logined_user_password, Run.EMPTY_STR), null,
                            new JsonRequestCallback() {
                                @Override
                                public void task_response(String jsonStr) {
                                    userLoginCallback(jsonStr);
                                }
                            }));
        }
    }

    /**
     * 用户登录返回
     *
     * @param json_str
     */
    private void userLoginCallback(String json_str) {
        try {
            JSONObject all = new JSONObject(json_str);
            if (Run.checkRequestJson(this, all, false)) {
                LoginedUser user = AgentApplication.getLoginedUser(this);
                user.setIsLogined(true);
                user.setUserInfo(all.getJSONObject("data"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onTerminate() {
        ShareSDK.stopSDK(this);
        mLoginedUser.clearLoginedStatus();
        super.onTerminate();
    }

    public static ImageLoader getAvatarLoader(Context context) {
        return getApp(context).mAvatarLoader;
    }

    public VolleyImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * 正在查看的订单
     *
     * @param mOrderDetail
     */
    public void setOrderDetail(JSONObject mOrderDetail) {
        this.mOrderDetail = mOrderDetail;
    }

    /**
     * 打开的Activity历史
     *
     * @return
     */
    public ArrayList<Activity> getRecentActivies() {
        return mRecentActivies;
    }

    /**
     * 获取App代理
     *
     * @param context
     * @return
     */
    public static AgentApplication getApp(Context context) {
        return (AgentApplication) context.getApplicationContext();
    }

    /**
     * 登录的用户
     *
     * @return
     */
    public LoginedUser getLoginedUser() {
        return mLoginedUser;
    }

    /**
     * 登录的用户
     *
     * @return
     */
    public static LoginedUser getLoginedUser(Context context) {
        return AgentApplication.getApp(context).getLoginedUser();
    }

}
