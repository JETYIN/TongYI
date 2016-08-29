package com.qianseit.westore.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json {
	// http错误码
//	public static final String JSON_ERROR_NETWORK = "500";
//	public static final String JSON_ERROR_SERVER = "404";
//	public static final String JSON_ERROR_NULL = "400";

	/**
	 * 安全读取json Int
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static int getJsonInt(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getInt(key);
		}
		return 0;
	}

	/**
	 * 安全读取json Int
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @param defValue
	 *            默认值
	 * @return
	 * @throws JSONException
	 */
	public static int getJsonInt(JSONObject all, String key, int defValue)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getInt(key);
		}
		return defValue;
	}

	/**
	 * 安全读取json Double
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static double getJsonDouble(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getDouble(key);
		}
		return 0;
	}

	/**
	 * 安全读取json Double
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @param defValue
	 *            默认值
	 * @return
	 * @throws JSONException
	 */
	public static double getJsonDouble(JSONObject all, String key,
			double defValue) throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getDouble(key);
		}
		return defValue;
	}

	/**
	 * 安全读取json Long
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static long getJsonLong(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getLong(key);
		}
		return 0;
	}

	/**
	 * 安全读取json Long
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @param defValue
	 *            默认值
	 * @return
	 * @throws JSONException
	 */
	public static long getJsonLong(JSONObject all, String key, long defValue)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getLong(key);
		}
		return defValue;
	}

	/**
	 * 安全读取json Boolean
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static boolean getJsonBool(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getBoolean(key);
		}
		return false;
	}

	/**
	 * 安全读取json Boolean
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @param defValue
	 *            默认值
	 * @return
	 * @throws JSONException
	 */
	public static boolean getJsonBool(JSONObject all, String key,
			boolean defValue) throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getBoolean(key);
		}
		return defValue;
	}

	/**
	 * 安全读取json String
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static String getJsonString(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getString(key);
		}
		return null;
	}

	/**
	 * 安全读取json String
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @param defValue
	 *            默认值
	 * @return
	 * @throws JSONException
	 */
	public static String getJsonString(JSONObject all, String key,
			String defValue) throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getString(key);
		}
		return defValue;
	}

	/**
	 * 安全读取json Object
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject getJsonObject(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getJSONObject(key);
		}
		return null;
	}

	/**
	 * 
	 * @param all
	 *            json对象
	 * @param key
	 *            键
	 * @return
	 * @throws JSONException
	 */
	public static JSONArray getJsonArray(JSONObject all, String key)
			throws JSONException {
		if (all.has(key) && !all.isNull(key)) {
			return all.getJSONArray(key);
		}
		return null;
	}
}
