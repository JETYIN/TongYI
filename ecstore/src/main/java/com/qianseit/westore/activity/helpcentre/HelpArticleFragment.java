package com.qianseit.westore.activity.helpcentre;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import cn.shopex.ecstore.R;

public class HelpArticleFragment extends BaseDoFragment {
	
	private String articleId;
	private String title;
	private String mContent;
	private String mUrl;
//	private TextView mTextView;
	private WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		if (b != null) {
			title = b.getString("title");
			articleId = b.getString("article_id");
			mContent = b.getString(Run.EXTRA_DATA);
			mUrl = b.getString("url");
		}
		mActionBar.setTitle(title);
	}
	
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_help_article, null);
//		mTextView = (TextView) rootView.findViewById(R.id.fragment_help_article_content);
		mWebView = (WebView) rootView.findViewById(R.id.fragment_help_article_content01);
//		mWebView.getSettings().setUseWideViewPort(true);
//		mWebView.getSettings().setLoadWithOverviewMode(true);
		if (title.equals("商派等级规则")) {
			new JsonTask().execute(new GetLVDetail());
		}else if (title.equals("商派网络服务协议")) {
			new JsonTask().execute(new GetLicense());
		}
		else if (!TextUtils.isEmpty(mContent)) {
			mWebView.setBackgroundColor(0x00000000);
//			mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			mWebView.loadDataWithBaseURL(null, mContent, "text/html", "utf8", null);
		}  else if(!TextUtils.isEmpty(mUrl)){
			mWebView.setBackgroundColor(0x00000000);
			mWebView.loadUrl(mUrl);
		}
		else new JsonTask().execute(new GetArticleDetail());
	}
	
	private class GetArticleDetail implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if(Run.checkRequestJson(mActivity, all)){
					JSONObject data = all.getJSONObject("data");    
					JSONObject body = data.getJSONObject("bodys");
					mContent = body.optString("content");
//					mContent = "<div style=\"background-color: rgb(255, 255, 255); padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; cursor: text; width: 100%; height: 100%; overflow-x: auto; overflow-y: auto; font-family: Arial, Verdana, sans-serif; font-size: 12px; \"><div class=\"pageWrap\"><h2>体贴的售后服务<\/h2>本网站所售产品均实行三包政策，请顾客保存好有效凭证，以确保我们为您更好服务。本公司的客户除享受国家规定“三包”。您可以更放心地在这里购物。<br\/><h3>保修细则<\/h3><h4>一、在本网站购买的商品，自购买日起(以到货登记为准)7日内出现性能故障，您可以选择退货、换货或修理。<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h4>二、在本公司购买的商品，自购日起(以到货登记为准)15日内出现性能故障，您可以选择换货或修理。(享受15天退换货无需理由的商品，按《15天退换货无需理由细则》办理)<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h4>三、在本公司购买的商品，自购日起(以到货登记为准)一年之内出现非人为损坏的质量问题，本公司承诺免费保修。<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h3>收费维修：<\/h3><h4>一、对于人为造成的故障，本公司将采取收费维修，包括：<\/h4><ol>\n\t<li>产品内部被私自拆开或其中任何部分被更替；<\/li>\n\t<li>商品里面的条码不清楚，无法成功判断；<\/li>\n\t<li>有入水、碎裂、损毁或有腐蚀等现象；<\/li>\n\t<li>过了保修期的商品。<\/li>\n<\/ol>\n<h4>二、符合以下条件，可以要求换货：<\/h4><ol>\n\t<li>客户在收到货物时当面在送货员面前拆包检查，发现货物有质量问题的<\/li>\n\t<li>实际收到货物与网站上描述的有很大的出入的<\/li>\n\t<li>换货流程：客户当面要求送货人员退回货物，然后与我们联系。我们会在一个工作日内为您重新发货，货物到达时间顺延。<\/li>\n<\/ol>\n<h4>三、符合以下条件，可以要求退货：<\/h4>客户收到货物后两天之内，<ol>\n\t<li>发现商品有明显的制造缺陷的<\/li>\n\t<li>货物经过一次换货但仍然存在质量问题的<\/li>\n\t<li>由于人为原因造成超过我们承诺到货之日三天还没收到货物的<\/li>\n<\/ol>\n退货流程：客户在收到货物后两天内与我们联系，我们会在两个工作日内通过银行汇款把您的货款退回。<h4>在以下情况我们有权拒绝客户的退换货要求：<\/h4><ol>\n\t<li>货物出现破损，但没有在收货时当场要求送货人员换货的<\/li>\n\t<li>超过退换货期限的退换货要求<\/li>\n\t<li>退换货物不全或者外观受损<\/li>\n\t<li>客户发货单据丢失或者不全<\/li>\n\t<li>产品并非我们提供<\/li>\n\t<li>货物本身不存在质量问题的<\/li>\n<\/ol>\n<\/div><\/div>"; 
					mWebView.setBackgroundColor(0x00000000);
//					mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
					mWebView.loadDataWithBaseURL(null, mContent, "text/html", "UTF-8", null);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.article.get_detail");
			req.addParams("article_id", articleId);
			return req;
		}
		
	}

	private class GetLVDetail implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if(Run.checkRequestJson(mActivity, all)){
					JSONObject data = all.getJSONObject("data");    
					JSONObject body = data.getJSONObject("bodys");
					mContent = body.optString("content");
//					mContent = "<div style=\"background-color: rgb(255, 255, 255); padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; cursor: text; width: 100%; height: 100%; overflow-x: auto; overflow-y: auto; font-family: Arial, Verdana, sans-serif; font-size: 12px; \"><div class=\"pageWrap\"><h2>体贴的售后服务<\/h2>本网站所售产品均实行三包政策，请顾客保存好有效凭证，以确保我们为您更好服务。本公司的客户除享受国家规定“三包”。您可以更放心地在这里购物。<br\/><h3>保修细则<\/h3><h4>一、在本网站购买的商品，自购买日起(以到货登记为准)7日内出现性能故障，您可以选择退货、换货或修理。<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h4>二、在本公司购买的商品，自购日起(以到货登记为准)15日内出现性能故障，您可以选择换货或修理。(享受15天退换货无需理由的商品，按《15天退换货无需理由细则》办理)<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h4>三、在本公司购买的商品，自购日起(以到货登记为准)一年之内出现非人为损坏的质量问题，本公司承诺免费保修。<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h3>收费维修：<\/h3><h4>一、对于人为造成的故障，本公司将采取收费维修，包括：<\/h4><ol>\n\t<li>产品内部被私自拆开或其中任何部分被更替；<\/li>\n\t<li>商品里面的条码不清楚，无法成功判断；<\/li>\n\t<li>有入水、碎裂、损毁或有腐蚀等现象；<\/li>\n\t<li>过了保修期的商品。<\/li>\n<\/ol>\n<h4>二、符合以下条件，可以要求换货：<\/h4><ol>\n\t<li>客户在收到货物时当面在送货员面前拆包检查，发现货物有质量问题的<\/li>\n\t<li>实际收到货物与网站上描述的有很大的出入的<\/li>\n\t<li>换货流程：客户当面要求送货人员退回货物，然后与我们联系。我们会在一个工作日内为您重新发货，货物到达时间顺延。<\/li>\n<\/ol>\n<h4>三、符合以下条件，可以要求退货：<\/h4>客户收到货物后两天之内，<ol>\n\t<li>发现商品有明显的制造缺陷的<\/li>\n\t<li>货物经过一次换货但仍然存在质量问题的<\/li>\n\t<li>由于人为原因造成超过我们承诺到货之日三天还没收到货物的<\/li>\n<\/ol>\n退货流程：客户在收到货物后两天内与我们联系，我们会在两个工作日内通过银行汇款把您的货款退回。<h4>在以下情况我们有权拒绝客户的退换货要求：<\/h4><ol>\n\t<li>货物出现破损，但没有在收货时当场要求送货人员换货的<\/li>\n\t<li>超过退换货期限的退换货要求<\/li>\n\t<li>退换货物不全或者外观受损<\/li>\n\t<li>客户发货单据丢失或者不全<\/li>\n\t<li>产品并非我们提供<\/li>\n\t<li>货物本身不存在质量问题的<\/li>\n<\/ol>\n<\/div><\/div>"; 
					mWebView.setBackgroundColor(0x00000000);
//					mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
					mWebView.loadDataWithBaseURL(null, mContent, "text/html", "UTF-8", null);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.info.lv_description");
//			req.addParams("article_id", articleId);
			return req;
		}
		
	}

	private class GetLicense implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if(Run.checkRequestJson(mActivity, all)){
					JSONObject data = all.getJSONObject("data");    
					JSONObject body = data.getJSONObject("bodys");
					mContent = body.optString("content");
//					mContent = "<div style=\"background-color: rgb(255, 255, 255); padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; cursor: text; width: 100%; height: 100%; overflow-x: auto; overflow-y: auto; font-family: Arial, Verdana, sans-serif; font-size: 12px; \"><div class=\"pageWrap\"><h2>体贴的售后服务<\/h2>本网站所售产品均实行三包政策，请顾客保存好有效凭证，以确保我们为您更好服务。本公司的客户除享受国家规定“三包”。您可以更放心地在这里购物。<br\/><h3>保修细则<\/h3><h4>一、在本网站购买的商品，自购买日起(以到货登记为准)7日内出现性能故障，您可以选择退货、换货或修理。<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h4>二、在本公司购买的商品，自购日起(以到货登记为准)15日内出现性能故障，您可以选择换货或修理。(享受15天退换货无需理由的商品，按《15天退换货无需理由细则》办理)<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h4>三、在本公司购买的商品，自购日起(以到货登记为准)一年之内出现非人为损坏的质量问题，本公司承诺免费保修。<\/h4><ol>\n\t<li>在接到您的产品后，我公司将问题商品送厂商特约维修中心检测；<\/li>\n\t<li>检测报出来后，如非人为损坏的，是产品本身质量问题，我公司会及时按您的要求予以退款、换可或维修。<\/li>\n\t<li>如果检测结果是无故障或是人为因素造成的故障，我公司会及时通知您，并咨询您的处理意见。<\/li>\n<\/ol>\n<h3>收费维修：<\/h3><h4>一、对于人为造成的故障，本公司将采取收费维修，包括：<\/h4><ol>\n\t<li>产品内部被私自拆开或其中任何部分被更替；<\/li>\n\t<li>商品里面的条码不清楚，无法成功判断；<\/li>\n\t<li>有入水、碎裂、损毁或有腐蚀等现象；<\/li>\n\t<li>过了保修期的商品。<\/li>\n<\/ol>\n<h4>二、符合以下条件，可以要求换货：<\/h4><ol>\n\t<li>客户在收到货物时当面在送货员面前拆包检查，发现货物有质量问题的<\/li>\n\t<li>实际收到货物与网站上描述的有很大的出入的<\/li>\n\t<li>换货流程：客户当面要求送货人员退回货物，然后与我们联系。我们会在一个工作日内为您重新发货，货物到达时间顺延。<\/li>\n<\/ol>\n<h4>三、符合以下条件，可以要求退货：<\/h4>客户收到货物后两天之内，<ol>\n\t<li>发现商品有明显的制造缺陷的<\/li>\n\t<li>货物经过一次换货但仍然存在质量问题的<\/li>\n\t<li>由于人为原因造成超过我们承诺到货之日三天还没收到货物的<\/li>\n<\/ol>\n退货流程：客户在收到货物后两天内与我们联系，我们会在两个工作日内通过银行汇款把您的货款退回。<h4>在以下情况我们有权拒绝客户的退换货要求：<\/h4><ol>\n\t<li>货物出现破损，但没有在收货时当场要求送货人员换货的<\/li>\n\t<li>超过退换货期限的退换货要求<\/li>\n\t<li>退换货物不全或者外观受损<\/li>\n\t<li>客户发货单据丢失或者不全<\/li>\n\t<li>产品并非我们提供<\/li>\n\t<li>货物本身不存在质量问题的<\/li>\n<\/ol>\n<\/div><\/div>"; 
					mWebView.setBackgroundColor(0x00000000);
//					mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
					mWebView.loadDataWithBaseURL(null, mContent, "text/html", "UTF-8", null);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.info.license_content");
//			req.addParams("article_id", articleId);
			return req;
		}
		
	}
}
