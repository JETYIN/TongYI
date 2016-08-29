package com.qianseit.westore.util;

import java.io.Serializable;

public class ChooseUtils implements Serializable{
	/** 订单ID　　*/
	private String order_id ; 
	private String goods_id; //商品ID
	private String goods_name ; //商品名称
	private String brand_name ; //品牌名称（就是标签的内容）
	private String is_opinions ; //是否推荐，1=已推荐
	private String is_comment ; //是否评论，1=已评论
	private String imagePath;	//图片路径
	private String selectsTime;	//图片路径
	
	
	public String getSelectsTime() {
		return selectsTime;
	}
	public void setSelectsTime(String selectsTime) {
		this.selectsTime = selectsTime;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getGoods_id() {
		return goods_id;
	}
	public void setGoods_id(String goods_id) {
		this.goods_id = goods_id;
	}
	public String getGoods_name() {
		return goods_name;
	}
	public void setGoods_name(String goods_name) {
		this.goods_name = goods_name;
	}
	public String getBrand_name() {
		return brand_name;
	}
	public void setBrand_name(String brand_name) {
		this.brand_name = brand_name;
	}
	public String getIs_opinions() {
		return is_opinions;
	}
	public void setIs_opinions(String is_opinions) {
		this.is_opinions = is_opinions;
	}
	public String getIs_comment() {
		return is_comment;
	}
	public void setIs_comment(String is_comment) {
		this.is_comment = is_comment;
	}
	
	
}
