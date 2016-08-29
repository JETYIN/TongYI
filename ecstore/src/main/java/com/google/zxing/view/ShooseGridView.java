package com.google.zxing.view;



import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
/**
 * 
 * 自定义的“九宫格”——用在显示帖子详情的图片集合  
 * 解决的问题：GridView显示不全，只显示了一行的图片，比较奇怪，尝试重写GridView来解决 
 * @author LiGang
 * @date 2015-6-12
 * @Copyright: Copyright (c) 2015 Shenzhen Tentinet Technology Co., Ltd. Inc. All rights reserved.
 */
public class ShooseGridView extends GridView {

    public ShooseGridView(Context context) {  
        super(context);  
        // TODO Auto-generated constructor stub  
    }  
  
    public ShooseGridView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
    }  
  
    public ShooseGridView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        // TODO Auto-generated constructor stub  
    }  
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        // TODO Auto-generated method stub  
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);  
        super.onMeasure(widthMeasureSpec, expandSpec);  
    }  
  
}  