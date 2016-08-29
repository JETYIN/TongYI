package com.ysshopex.wxapi;

/**
 * 微信支付V3版本只需要 app_id 
 * @author chanson
 * @CreatTime 2015-8-2 上午9:36:58
 *
 */
public class Constants {
	// APP_ID 替换为你的应用从官方网站申请到的合法appId
	public static final String APP_ID = "wx058a5abb7720ea82";
	/**
	 * 微信开放平台和商户约定的密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genSign这个过程由服务器端完成
	 */
//	public static final String APP_SECRET = "d0b5b0c980fce630db44b9db52f78065"; // wx058a5abb7720ea82 对应的密钥

	/**
	 * 微信开放平台和商户约定的支付密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genSign这个过程由服务器端完成
	 */
//	public static final String APP_KEY = "b28be4b8a68f4ff8abdcacbb3bdc042d"; // wxd930ea5d5a258f4f
																																												// 对应的支付密钥

	/**
	 * 微信公众平台商户模块和商户约定的密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genPackage这个过程由服务器端完成
	 */
//	public static final String PARTNER_KEY = "b28be4b8a68f4ff8abdcacbb3bdc042d";
//	public static final String API_KEY = "b28be4b8a68f4ff8abdcacbb3bdc042d";

	/** 商家向财付通申请的商家id */
//	public static final String PARTNER_ID = "1253359601";

//	public static class ShowMsgActivity {
//		public static final String STitle = "showmsg_title";
//		public static final String SMessage = "showmsg_message";
//		public static final String BAThumbData = "showmsg_thumb_data";
//	}
}
