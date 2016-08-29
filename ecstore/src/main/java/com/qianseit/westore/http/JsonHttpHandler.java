package com.qianseit.westore.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.qianseit.westore.util.Util;

/**
 * 连接控制类 <br/>
 * 完成具体的网络交互功能
 *
 */
public class JsonHttpHandler {

	public static final String JSON_ERROR_NETWORK = "500";
	public static final String JSON_ERROR_SERVER = "404";

	public static String session_id = null;

	static final int ok = 200;

	public static CookieStore mCookieStore;

	public static final String senddata(JsonRequestBean data) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		try {
			if (mCookieStore != null)
				httpclient.setCookieStore(mCookieStore);

			if (JsonRequestBean.METHOD_GET.equals(data.method)) {
				HttpGet httpRequest = new HttpGet(data.url);
				httpResponse = httpclient.execute(httpRequest);
			} else {
				HttpPost httpPost = new HttpPost(data.url);
				if (data.params != null) {
					httpPost.setEntity(new UrlEncodedFormEntity(data.params,
							HTTP.UTF_8));
				}
				httpResponse = httpclient.execute(httpPost);
			}

			mCookieStore = httpclient.getCookieStore();
			if (httpResponse.getStatusLine().getStatusCode() == ok) {

				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					String str = EntityUtils.toString(entity);
					// 字符编码不为空，则进行转码
					if (!TextUtils.isEmpty(data.charset))
						str = new String(str.getBytes(data.charset), "UTF-8");
					return str;
				}
			} else {
				return JSON_ERROR_SERVER;
			}
		} catch (Exception e) {
			return JSON_ERROR_NETWORK;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return JSON_ERROR_NETWORK;
	}

	/**
	 * 上传方法<br/>
	 * 上传时的RequestBean的数据组装<br/>
	 *
	 * <pre>
	 *
	 * HashMap&lt;String, String&gt; params = new HashMap&lt;String, String&gt;();
	 * params.put(&quot;order&quot;, &quot;901&quot;);// 操作命令 必须有
	 * params.put(&quot;name&quot;, ab.label);// 普通参数
	 * params.put(&quot;package&quot;, ab.pname);
	 * params.put(&quot;version&quot;, ab.versionName);
	 * params.put(&quot;vcode&quot;, String.valueOf(ab.versionCode));
	 * params.put(&quot;size&quot;, Formatter.formatFileSize(a, new File(ab.other).length()));
	 *
	 * File[] files = new File[] { new File(ab.other) };// 需要上传的文件，文件类
	 *
	 * BitmapDrawable bd = (BitmapDrawable) ab.icon;
	 * Bitmap bm = bd.getBitmap();
	 * ByteArrayOutputStream dw = new ByteArrayOutputStream();
	 * bm.compress(Bitmap.CompressFormat.PNG, 100, dw);
	 *
	 * byte[][] bytess = new byte[][] { dw.toByteArray() };// 需要上传的文件，字节形式
	 *
	 * RequestBean dd = new RequestBean(params, files, bytess);// 组装发送数据
	 * new NetTask(DataReceiver).execute(dd);// 是DataReceiver接口
	 * </pre>
	 *
	 * <br/>
	 * <br/>
	 * <br/>
	 * <br/>
	 * <br/>
	 * <br/>
	 *
	 */
	public final static String senddata_upload(JsonRequestBean dd) {
		return senddata_upload(dd, dd.params, dd.files, dd.bytess, dd.url);
	}

	public final static String senddata_upload(JsonRequestBean data,
											   List<NameValuePair> params, File[] files, byte[][] bytess,
											   String theurl) {
		DataOutputStream os = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			String BOUNDARY = "--------------Upload"; // 数据分隔线
			String MULTIPART_FORM_DATA = "Multipart/form-data";

			URL url = new URL(theurl);
			conn = (HttpURLConnection) url.openConnection();

			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false);// 不使用Cache
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA
					+ ";boundary=" + BOUNDARY);
			// String usernamePassword = username + ":" + password;
			// conn.setRequestProperty("Authorization", "Basic "+ new
			// String(SecBase64.encode(usernamePassword.getBytes())));

			StringBuilder sb = new StringBuilder();

			// 上传的表单参数部分，格式请参考文章
			for (NameValuePair entry : params) {// 构建表单字段内容
				sb.append("--");
				sb.append(BOUNDARY);
				sb.append("\r\n");
				sb.append("Content-Disposition: form-data; name=\""
						+ entry.getName() + "\"\r\n\r\n");
				sb.append(entry.getValue());
				sb.append("\r\n");
			}
			// System.out.println(sb.toString());
			os = new DataOutputStream(conn.getOutputStream());
			os.write(sb.toString().getBytes());// 发送表单字段数据

			if (files != null && files.length > 0) {
				// byte[] content = readFileImage(filename);
				// System.out.println("content:"+content.toString());
				File file;
				for (int i = 0; i < files.length; i++) {
					file = files[i];
					byte[] content = readFileImage(file);
					StringBuilder split = new StringBuilder();
					split.append("--");
					split.append(BOUNDARY);
					split.append("\r\n");
					split.append("Content-Disposition: form-data;name=\""
							+ file.getName() + "\";filename=\""
							+ file.getName() + ".jpg\"\r\n");
					split.append("Content-Type: application/octet-stream; charset=UTF-8\r\n\r\n");
					Util.log("upload:" + split.toString());
					os.write(split.toString().getBytes());
					os.write(content, 0, content.length);
					os.write("\r\n".getBytes());
				}
			}

			if (bytess != null && bytess.length > 0) {
				// byte[] content = readFileImage(filename);
				// System.out.println("content:"+content.toString());
				byte[] content;
				for (int i = 0; i < bytess.length; i++) {
					content = bytess[i];
					StringBuilder split = new StringBuilder();
					split.append("--");
					split.append(BOUNDARY);
					split.append("\r\n");
					split.append("Content-Disposition: form-data;name=\"file\";filename=\"file\"\r\n");
					split.append("Content-Type: application/octet-stream; charset=UTF-8\r\n\r\n");
					os.write(split.toString().getBytes());
					os.write(content, 0, content.length);
					os.write("\r\n".getBytes());
				}
			}

			byte[] end_data = ("--" + BOUNDARY + "--\r\n").getBytes();// 数据结束标志
			os.write(end_data);
			os.flush();
			if (conn.getResponseCode() == HttpStatus.SC_OK)// 如果发布成功则提示成功
			{
				is = conn.getInputStream();
				String str = getStreamString(is);
				// 字符编码不为空，则进行转码
				if (!TextUtils.isEmpty(data.charset))
					str = new String(str.getBytes(data.charset), "UTF-8");
				return str;
			} else {
				return JSON_ERROR_SERVER;
			}

		} catch (Exception e) {
			Util.log(e);
		} finally {
			try {
				if (os != null)
					os.close();
				if (is != null)
					is.close();
				if (conn != null)
					conn.disconnect();
			} catch (Exception _ex) {
			}
		}
		return JSON_ERROR_NETWORK;
	}

	/**
	 * 将输入流转换为String
	 *
	 * @param tInputStream
	 * @return
	 */
	public static String getStreamString(InputStream tInputStream) {
		if (tInputStream != null) {
			try {
				BufferedReader tBufferedReader = new BufferedReader(
						new InputStreamReader(tInputStream));
				StringBuffer tStringBuffer = new StringBuffer();

				String sTempOneLine;
				while ((sTempOneLine = tBufferedReader.readLine()) != null) {
					tStringBuffer.append(sTempOneLine);
				}

				return tStringBuffer.toString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return "";
	}

	@SuppressWarnings("resource")
	public final static byte[] readFileImage(File file) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				new FileInputStream(file));
		int len = bufferedInputStream.available();
		byte[] bytes = new byte[len];
		int r = bufferedInputStream.read(bytes);
		if (len != r) {
			bytes = null;
			throw new IOException("读取文件不正确");
		}
		bufferedInputStream.close();
		return bytes;
	}

}
