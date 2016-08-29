package com.qianseit.westore.imageloader;

import com.qianseit.westore.util.Comm;

import android.os.Environment;

/**
 * 文件属性类
 * 
 * @Description 设置文件目录
 * @author CodeApe
 * @version 1.0
 * @date 2014年4月4日
 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd. Inc.
 *             All rights reserved.
 * 
 */
public class FileConfig {

	// *************************** 应用使用的文件路径 *****************************//
	// 公用文件路径
	/** 应用根目录 */
	public static final String PATH_BASE = Comm.doFolder;
	/** 应用Log日志 */
//	public static final String PATH_LOG = PATH_BASE + "Log/";
	/** 临时文件夹 */
//	public static final String PATH_HTML = PATH_BASE + "Html/";
	/** 临时文件 */
//	public static final String PATH_HTML_TEMP = PATH_BASE + "Html/temp.html";
	/** 下载文件文件夹 */
//	public static final String PATH_DOWNLOAD = PATH_BASE + "Download/";
	/** 拍照文件夹 */
//	public static final String PATH_CAMERA = PATH_BASE + "Camera/";
	/** 应用基本缓存文件图片路径 */
//	public static final String PATH_IMAGES = PATH_BASE + "Image/";
	/** 收藏的图片路径 */
//	public static final String PATH_PHOTOS = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Qing/";
	/** 拍照缓存文件 */
	public static final String PATH_IMAGE_TEMP = Comm.doFolder + "temp.jpg";

	// 用户私有文件路径
	/** 用户私有缓存文件夹 */
	public String PATH_USER_FILE = "";
	/** 用户私有图片缓存文件夹 */
	public static String PATH_USER_IMAGE = "";
	/** 用户私有图片缩略图 缓存文件夹 */
	public static String PATH_USER_THUMBNAIL = "";
	/** 用户私有录音缓存文件夹 */
//	public static String PATH_USER_AUDIO = "";
	/** 用户私有视频缓存文件夹 */
//	public static String PATH_USER_VIDEO = "";
	/** 用户私收藏文件夹 */
//	public static String PATH_USER_FAVORITES = "";
	/** 上传文件名字 */
//	public static String UPLOAD_FILE_NAME = "";
	
	/** 用户私有图片缓存文件夹潜水吧 */
//	public static String PATH_USER_IMAGE_BAR = "";

}
