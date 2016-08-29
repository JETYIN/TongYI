package com.qianseit.westore.http;

import org.apache.http.NameValuePair;


import com.qianseit.westore.util.Util;

import android.os.AsyncTask;

/**
 * 通用网络交互任务<br/>
 * 请求接口，返回请求参数<br/>
 * 回调接口，反馈调用结果<br/>
 * <br/>
 * <br/>
 */
public class JsonTask extends
        AsyncTask<JsonTaskHandler, Integer, JsonTaskHandler> {
    public boolean isExcuting = false;
    public boolean isFinished = false;

    static final int ok = 200;
    String info = "";

    /**
     * 任务开始
     */
    @Override
    protected void onPreExecute() {
        isExcuting = true;
        isFinished = false;
    }

    /**
     * 任务执行操作，publishProgress可提示任务进程
     */
    @Override
    protected JsonTaskHandler doInBackground(JsonTaskHandler... handlers) {
        JsonTaskHandler handler = handlers[0];
        JsonRequestBean data = handler.task_request();
        if (data.params.size() > 0) // 签名参数
            data.addParams("sign", data.signatureParams());

        String args = "";
        for (NameValuePair arg : data.params)
            args = Util.buildString(args, "&", arg.getName(), "=",
                    arg.getValue());
        Util.log(data.url, args);

        // 请求数据
        // if (data.files != null || data.bytess != null)
        // else
        if (JsonRequestBean.METHOD_GET.equals(data.method))
            info = JsonHttpHandler.senddata(data);
        else
            info = JsonHttpHandler.senddata_upload(data);

        return handler;
    }

    /**
     * 进度提示
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        // 更新进度
        System.out.println("" + values[0]);
    }

    /**
     * 任务结束操作
     */
    @Override
    protected void onPostExecute(JsonTaskHandler handler) {
        Util.log(info);
        handler.task_response(info);
        isExcuting = false;
        isFinished = true;
    }

    /**
     * 任务终止
     */
    @Override
    public void onCancelled() {
        super.onCancelled();
    }

}
