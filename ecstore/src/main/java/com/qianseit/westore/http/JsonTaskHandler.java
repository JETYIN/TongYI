package com.qianseit.westore.http;

public interface JsonTaskHandler {

	/**
	 * 接收保存反馈数据<br/>
	 * 此方法由任务类调用，将获取的结果数据传送给任务控制类<br/>
	 * 任务完成后的操作<br/>
	 * 主要指涉及UI层的操作，此方法在UI主线程执行<br/>
	 * 
	 * @param json_str
	 *            反馈的json数据，字符串
	 */
	public abstract void task_response(String json_str);// 接收，保存结果数据,结束操作，对接收数据进行实际的UI线程操作

	/**
	 * 提供请求数据<br/>
	 * 生成请求数据类返回<br/>
	 * 
	 * @return 请求数据类
	 */
	public abstract JsonRequestBean task_request();// 请求数据

}
