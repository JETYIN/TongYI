package com.qianseit.westore.http;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;


import android.text.TextUtils;

import com.qianseit.westore.Run;

import com.qianseit.westore.util.Util;

import com.qianseit.westore.util.Md5;


public class JsonRequestBean {
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";

	public String url; // 请求接口url
	public String charset; // 接口字符编码，默认为utf-8
	public String method = METHOD_POST; // 请求的方法GET/POST
	public ArrayList<NameValuePair> params; // 参数列表

	// 上传文件时候使用，请看接口文档
	public File[] files;
	public byte[][] bytess;

	/**
	 * 新建Request Bean，并设置请求url
	 *
	 * @param new_url
	 */
	public JsonRequestBean(String method) {
		this.params = new ArrayList<NameValuePair>();
		this.url = Run.API_URL;

		// 默认必须添加的参数
		if (!TextUtils.isEmpty(method)) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
			this.addParams("date", df.format(System.currentTimeMillis()));
			this.addParams("method", method);
			this.addParams("direct", "true");
		}
	}

	/**
	 * 新建Request Bean，并设置请求url
	 *
	 * @param new_url
	 * @param srcCharset
	 *            接口编码类型
	 */
	public JsonRequestBean(String method, String srcCharset) {
		this(method);
		this.charset = srcCharset;
	}

	/**
	 * 添加请求参数
	 *
	 * @param key
	 * @param value
	 */
	public JsonRequestBean addParams(String key, String value) {
		params.add(new BasicNameValuePair(key, value));
		return this;
	}

	/**
	 * 签名表单数据
	 *
	 * @return
	 */
	public String signatureParams() {
		Collections.sort(params, new Comparator<NameValuePair>() {
			@Override
			public int compare(NameValuePair a, NameValuePair b) {
				return a.getName().compareTo(b.getName());
			}
		});

		String result = "";
		String lastKey = "";
		for (NameValuePair param : params) {
			String paramName = param.getName();
			int start = paramName.indexOf("[");
			if (start != -1) {
				String theKey = paramName.substring(0, start);
				if (TextUtils.equals(lastKey, theKey))
					paramName = paramName.substring(start);
				lastKey = theKey;
			}

			result = Util.buildString(result, paramName.replaceAll("\\[", "").replace("]", ""), param.getValue());
		}

		Util.log("result", result);
		return Md5.getMD5(Util.buildString(Md5.getMD5(result).toUpperCase(), Run.TOKEN)).toUpperCase();
	}

	@Override
	public String toString() {
		String result = url;
		for (int i = 0; i < params.size(); i++) {
			result = result + "&" + params.get(i).getName() + "=" + params.get(i).getValue();
		}
		return result;
	}

	public interface JsonRequestCallback {
		public void task_response(String jsonStr);
	}
}
