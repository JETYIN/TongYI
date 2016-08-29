package com.qianseit.westore.activity.account;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/3/1.
 */
public class AccountBackWebViewFragment extends BaseDoFragment {

    private WebView webview;
    public AccountBackWebViewFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar.setTitle("详情");
    }


    @Override
    public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.webview, null);
        webview = (WebView)rootView.findViewById(R.id.webview);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        Log.e("图片链接","ldy"+mActivity.getIntent().getStringExtra("link_url"));
        webview.loadUrl(mActivity.getIntent().getStringExtra("link_url"));
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
//        webview.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                // TODO Auto-generated method stub
//                if (newProgress == 100) {
//                    // 网页加载完成
//                    ((DoActivity) mActivity).hideLoadingDialog_mt();
//                } else {
//                    // 加载中
//                    ((DoActivity) mActivity).showCancelableLoadingDialog();
//                }
//
//            }
//        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode== KeyEvent.KEYCODE_BACK)
        {
            if(webview.canGoBack())
            {
                webview.goBack();//返回上一页面
                return true;
            }
            else
            {
                mActivity.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
