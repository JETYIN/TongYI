package com.qianseit.westore.imageloader;

import java.io.Serializable;

import android.graphics.Bitmap;


/**
 * 裁剪图片属性类
 * 
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2014年6月10日
 * @Copyright: Copyright (c) 2014 Shenzhen Utoow Technology Co., Ltd. All rights
 *             reserved.
 * 
 */
public class ClipPictureBean implements Serializable{

	/**
	 * 
	 * @author CodeApe
	 * @version 1.0
	 * @date 2014年6月10日
	 */
	private final long serialVersionUID = 9000422348759231089L;
	/** 输出图片宽度 */
	private int outputX = 1;
	/** 输出图片高度 */
	private int outputY = 1;

	/** 图片裁剪宽度比例 */
	private int aspectX = 200;
	/** 图片裁剪高度比例 */
	private int aspectY = 200;
	/** 裁剪后的图片 */
	private Bitmap aspectPhotos;
	/** 原图路径 */
	private String srcPath;
	/** 图片保存路路径 */
	private String savePath = FileConfig.PATH_IMAGE_TEMP;

	
	
	public Bitmap getAspectPhotos() {
		return aspectPhotos;
	}

	public void setAspectPhotos(Bitmap aspectPhotos) {
		this.aspectPhotos = aspectPhotos;
	}

	public int getOutputX() {
		return outputX;
	}

	public void setOutputX(int outputX) {
		this.outputX = outputX;
	}

	public int getOutputY() {
		return outputY;
	}

	public void setOutputY(int outputY) {
		this.outputY = outputY;
	}

	public int getAspectX() {
		return aspectX;
	}

	public void setAspectX(int aspectX) {
		this.aspectX = aspectX;
	}

	public int getAspectY() {
		return aspectY;
	}

	public void setAspectY(int aspectY) {
		this.aspectY = aspectY;
	}

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	@Override
	public String toString() {
		return "ClipPictureBean [outputX=" + outputX + ", outputY=" + outputY + ", aspectX=" + aspectX + ", aspectY=" + aspectY + ", srcPath="
				+ srcPath + ", savePath=" + savePath + "]";
	}

}