package com.ysshopex.wxapi;

import java.io.StringReader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayV3Fragment extends BaseDoFragment {

//	private static final String TAG = "MicroMsg.SDKSample.PayActivity";

	IWXAPI msgApi;
	PayReq req;
//	TextView show;
	Map<String, String> resultunifiedorder;
	StringBuffer sb;
	private JSONObject data;

	public WXPayV3Fragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		msgApi = WXAPIFactory.createWXAPI(mActivity, null);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
//		rootView = inflater.inflate(R.layout.test_wx_pay, null);
//		show = (TextView) findViewById(R.id.editText_prepay_id);
		req = new PayReq();
		sb = new StringBuffer();

		msgApi.registerApp(Constants.APP_ID);
		 //生成prepay_id
//		Button payBtn = (Button) findViewById(R.id.unifiedorder_btn);
//		payBtn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
//				getPrepayId.execute();
//			}
//		});
//		Button appayBtn = (Button) findViewById(R.id.appay_btn);
//		appayBtn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				sendPayReq();
//			}
//		});
//
//		// 生成签名参数
//		Button appay_pre_btn = (Button) findViewById(R.id.appay_pre_btn);
//		appay_pre_btn.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				genPayReq();
//			}
//		});
//		callWXPay(null);
	}
	
	public void callWXPay(JSONObject data){
		this.data = data;
//		GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
//		getPrepayId.execute();
		genPayReq();
	}

	/**
	 生成签名
	 */

	private String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		//sb.append(Constants.API_KEY);
		sb.append(data.optString("paysignkey"));
		

		String packageSign = getMessageDigest(sb.toString().getBytes()).toUpperCase();
		Log.e("orion",packageSign);
		return packageSign;
	}
	private String genAppSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		//sb.append(Constants.API_KEY);
		sb.append(data.optString("paysignkey"));

       this.sb.append("sign str\n"+sb.toString()+"\n\n");
		String appSign = getMessageDigest(sb.toString().getBytes()).toUpperCase();
		Log.e("orion",appSign);
		return appSign;
	}
	private String toXml(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (int i = 0; i < params.size(); i++) {
			sb.append("<"+params.get(i).getName()+">");


			sb.append(params.get(i).getValue());
			sb.append("</"+params.get(i).getName()+">");
		}
		sb.append("</xml>");

		Log.e("orion",sb.toString());
		return sb.toString();
	}

	private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String,String>> {

//		private ProgressDialog dialog;


		@Override
		protected void onPreExecute() {
//			dialog = ProgressDialog.show(mActivity, getString(R.string.app_tip), getString(R.string.getting_prepayid));
		}

		@Override
		protected void onPostExecute(Map<String,String> result) {
//			if (dialog != null) {
//				dialog.dismiss();
//			}
			sb.append("prepay_id\n"+result.get("prepay_id")+"\n\n");
//			show.setText(sb.toString());

			resultunifiedorder=result;
			genPayReq();

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Map<String,String>  doInBackground(Void... params) {

			String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
			String entity = genProductArgs();

			Log.e("orion",entity);

			byte[] buf = Util.httpPost(url, entity);

			String content = new String(buf);
			Log.e("orion", content);
			Map<String,String> xml=decodeXml(content);

			return xml;
		}
	}



	public Map<String,String> decodeXml(String content) {

		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {

				String nodeName=parser.getName();
				switch (event) {
					case XmlPullParser.START_DOCUMENT:

						break;
					case XmlPullParser.START_TAG:

						if("xml".equals(nodeName)==false){
							//实例化student对象
							xml.put(nodeName,parser.nextText());
						}
						break;
					case XmlPullParser.END_TAG:
						break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion",e.toString());
		}
		return null;

	}


	private String genNonceStr() {
		Random random = new Random();
		return getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}
	
	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}
	


	private String genOutTradNo() {
		Random random = new Random();
		return getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}
	

  //
	private String genProductArgs() {
		StringBuffer xml = new StringBuffer();

		try {
			String	nonceStr = genNonceStr();

			JSONObject temp = data.optJSONObject("package");

			xml.append("</xml>");
           List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
//			packageParams.add(new BasicNameValuePair("body", "weixin"));
			packageParams.add(new BasicNameValuePair("body", data.optString("body")));
			packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));//body为中文设置成utf-8 最后转为ISO8859-1
//			packageParams.add(new BasicNameValuePair("mch_id", Constants.PARTNER_ID));
			packageParams.add(new BasicNameValuePair("mch_id", data.optString("partnerid")));
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
//			packageParams.add(new BasicNameValuePair("notify_url", "http://121.40.35.3/test"));
			packageParams.add(new BasicNameValuePair("notify_url", temp.optString("notify_url")));
//			packageParams.add(new BasicNameValuePair("out_trade_no",genOutTradNo()));
			packageParams.add(new BasicNameValuePair("out_trade_no",data.optString("payment_id")));
//			packageParams.add(new BasicNameValuePair("spbill_create_ip","127.0.0.1"));
			packageParams.add(new BasicNameValuePair("spbill_create_ip",temp.optString("spbill_create_ip")));
//			packageParams.add(new BasicNameValuePair("total_fee", "1"));
			int count = (int)(data.optDouble("total_amount") * 100);
			packageParams.add(new BasicNameValuePair("total_fee", String.valueOf(count)));
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));


			String sign = genPackageSign(packageParams);
			packageParams.add(new BasicNameValuePair("sign", sign));


		   String xmlstring =toXml(packageParams);

			return new String(xmlstring.toString().getBytes(), "ISO8859-1");

		} catch (Exception e) {
//			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}
		

	}
	private void genPayReq() {

//		req.appId = Constants.APP_ID;
//		req.partnerId = data.optString("partnerid");
//		req.prepayId = resultunifiedorder.get("prepay_id");
//		req.packageValue = "Sign=WXPay";
//		req.nonceStr = genNonceStr();
//		req.timeStamp = String.valueOf(genTimeStamp());
//		FragmentActivity activity=getActivity();
//		SharedPreferences sharedPre=activity.getSharedPreferences("orderPay",activity.MODE_PRIVATE);
//		SharedPreferences.Editor editor=sharedPre.edit();
		
		JSONObject returnData = data.optJSONObject("return");
//		editor.putString("orderId",data.optString("order_id"));
//		editor.putString("sing",returnData.optString("sign"));
//		editor.commit();
		Run.savePrefs(mActivity, "orderId", data.optString("order_id"));
		Run.savePrefs(mActivity, "sign", returnData.optString("sign"));
		req.appId = returnData.optString("appid");
		req.partnerId = returnData.optString("partnerid");
		req.prepayId = returnData.optString("prepayid");
		req.packageValue = returnData.optString("package");
		req.nonceStr = returnData.optString("noncestr");
		req.timeStamp = returnData.optString("timestamp");


		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

		req.sign = genAppSign(signParams);

		sb.append("sign\n"+req.sign+"\n\n");

//		show.setText(sb.toString());

		Log.e("orion", signParams.toString());
		sendPayReq();

	}
	private void sendPayReq() {
		

		msgApi.registerApp(Constants.APP_ID);
		msgApi.sendReq(req);
	}

	private final String getMessageDigest(byte[] buffer) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(buffer);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

}
