package com.qianseit.westore.activity;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountOrdersFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;

public class ArticleReaderFragment extends BaseDoFragment {
//	public static final String METHOD_ARTICLE_DETAIL = "article_detail";
//	public static final String METHOD_EMAIL_DETAIL = "my_email_detail";
//	public static final String METHOD_CALL_US = "call_us";

	private WebView mWebView;
	private TextView mTitleTV;
	private TextView mDateTV;
	private TextView mSourceTV;

	private String articleid;
	private String articleHtml;
	private String articleUrl;
	private JsonTask mJsonTask;

	public ArticleReaderFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articleid = mActivity.getIntent().getStringExtra(Run.EXTRA_ARTICLE_ID);
//		articleid = "64";
		articleHtml = mActivity.getIntent().getStringExtra(Run.EXTRA_HTML);
		articleUrl = mActivity.getIntent().getStringExtra("com.qianseit.westore.EXTRA_URL");
	}

	@SuppressLint("JavascriptInterface")
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_article_reader, null);
		mTitleTV = (TextView) findViewById(R.id.article_reader_title);
		mDateTV = (TextView) findViewById(R.id.article_reader_date);
		mSourceTV = (TextView) findViewById(R.id.article_reader_source);

		mWebView = (WebView) findViewById(R.id.article_reader_webview);
//		mWebView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);
		mWebView.setBackgroundColor(0xffffffff);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
//	    webSettings.setSupportZoom(true);
	    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	    webSettings.setBuiltInZoomControls(false);//support zoom
//	    webSettings.setUseWideViewPort(true);// 这个很关键
//	    webSettings.setLoadWithOverviewMode(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				view.loadUrl("javascript:window.aliasInHtml.getHTML(document.body.innerHTML);");
			}
		});

		// fragment背景
		View parent = findViewById(R.id.article_reader_parent);
		if (!TextUtils.isEmpty(articleHtml)) {
			parent.setVisibility(View.VISIBLE);
			mWebView.loadDataWithBaseURL(Run.MAIN_URL, articleHtml,
					"text/html", "utf8", null);
		} else if (!TextUtils.isEmpty(articleUrl)) {
			parent.setVisibility(View.VISIBLE);
			mWebView.loadUrl(articleUrl);
		} else {
			this.reloadArticleContent();
		}
		mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "aliasInHtml");
	}

	// 加载文章分类列表
	private void reloadArticleContent() {
		if (mJsonTask != null)
			mJsonTask.cancel(true);

		mJsonTask = new JsonTask();
		Run.excuteJsonTask(mJsonTask, new ArticleListTask());
	}
	
	final class InJavaScriptLocalObj {
		
		@JavascriptInterface
        public void getHTML(String html) {
        	if (html.contains("成功付款")) {
        		Run.sendMyBroadcast(mActivity, AccountOrdersFragment.PAY_SUCCEE);
				mActivity.finish();
			}
        }  
    } 

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	@Override
	public void ui(int what, Message msg) {
	}

	//获取文章详情
	private class ArticleListTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			return new JsonRequestBean(
					"mobileapi.article.get_detail").addParams("article_id",
					articleid);
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					View parent = findViewById(R.id.article_reader_parent);
					parent.setVisibility(View.VISIBLE);

					mTitleTV.setText(all.optString("title"));
					JSONObject data = all.optJSONObject("data");
					JSONObject index = data.optJSONObject("indexs");
					mActionBar.setTitle(index.optString("title"));
					JSONObject body = data.optJSONObject("bodys");
					String content = body.optString("content");
//					content = content.replaceAll("<img", "<img width=\"100%\"");
					mWebView.loadDataWithBaseURL("",
							content, "text/html", "utf-8", "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
