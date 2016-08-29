package com.alipay.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONObject;

import android.os.Message;

import cn.shopex.ecstore.R;
import com.ysshopex.wxapi.WXPayV3Fragment;
import com.alipay.sdk.app.PayTask;

public class AliPayFragment extends WXPayV3Fragment {

	//商户PID
	private String PARTNER = "2088611974864105";
	
	//商户收款账号
	private String SELLER = "qiansekj@qq.com";
	
	//商户私钥，pkcs8格式
	private String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOIWfoAEi4NC7RQ4jdPj+u5BUc1AkTX/iBgP3Y9uQebG6cBiKx52E45Jxw7XrxVk4B6LrOpS9r6cvyws9ARUrsQV/skPeCVWO8DC7KmqAXXRQufIhD2559O9qwXHIoJuEWGgixx+sHYbI5pluZJ6CSN7N+zsC9MjDwkFy+OSPx3BAgMBAAECgYB3grJ902k4CqGt5bM7BbE4TnkCSZY4+AmlxoU083CMoCsiEgJ/CKWPunop42NeqMM8AlN2TkK2Qb9gh6a5v1XdvLl/UOywNqbp635AxzGLlGtYbWSrrI6JXZ9lBoiq6PN6sQFJoxOZ7+GcYp6Sqb16OkdtM6fin95uU0ww8KjNPQJBAPsz07No1MZEShUYN19NbMP8K/jTKE7OZxazJZYXRHV9Rv6Cv5XMz/doGFBy6ACcJVR036lzq9SQvxiOD44mTtMCQQDmZ+CodiQLZxC+OAcK2MkrI3L7EX0Z2xfcsV+kPlxHYQWe3NcpXL5FSOwOeHDJzzdkvdaqKsydeT1970RXBoybAkEAkoYJfwWvzrI4kBNfAQz1W1/0+h+YzFbilMAMNX7+5JUdWwlC3QrpZ4NlY0+S+gAaWigN6hZZbRS9EoKSmjTIFwJAV9a4XmET4sj7KBnCDY+SOgD8v9zt/aJ6WWfB16LymT/S3brByIIUA+QGIoomLbnzG1QId71zXoGvQMisHCViCwJAShwCZYPXZozJclUXdpugOYK2R4Ihm6axgKO5ErhdLh+J853D6VSUrWpkCHdZUNlg5ldD/mglalHzeBc4GM+Ybg==";
	
	//支付结果通知
	private String NOTIFY_URL;
	
	private JSONObject paymentInfo;
	
	public static final int SDK_PAY_FLAG = 1;

//	public static final int SDK_CHECK_FLAG = 2;
	
	public AliPayFragment() {
	}
	
	public void callAliPay(JSONObject data){
		paymentInfo = data;
		PARTNER = paymentInfo.optString("mer_id");
		SELLER = paymentInfo.optString("seller_account_name");
		RSA_PRIVATE = paymentInfo.optString("key");
		NOTIFY_URL = paymentInfo.optString("callback_url");
		pay();
	}
	
	/**
	 * call alipay sdk pay. 调用SDK支付
	 * 
	 */
	private void pay() {
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.app_name));
		sb.append(paymentInfo.optString("payment_id"));
		// 订单
		String orderInfo = getOrderInfo(sb.toString(), sb.toString(), paymentInfo.optString("total_amount") ,paymentInfo.optString("payment_id"));

		// 对订单做RSA 签名
		String sign = sign(orderInfo);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
				+ getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(mActivity);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * create the order info. 创建订单信息
	 * 
	 */
	public String getOrderInfo(String subject, String body, String price , String onderid) {
		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
//		orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";
		orderInfo += "&out_trade_no=" + "\"" + onderid + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
//		orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm"
//				+ "\"";
		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + NOTIFY_URL	+ "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}
	
	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param content
	 * 待签名订单信息
	 */
	public String sign(String content) {
		return SignUtils.sign(content, RSA_PRIVATE);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}
}
