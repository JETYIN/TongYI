package com.qianseit.westore.picturetagview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.shopex.ecstore.R;
import com.qianseit.westore.picturetagview.PictureTagView.Direction;
import com.qianseit.westore.picturetagview.PictureTagView.Status;
@SuppressLint("NewApi")
public class PictureTagLayout extends RelativeLayout implements OnTouchListener{
	int startX = 0;
	boolean blan=false;
	int startY = 0;
	int startTouchViewLeft = 0;
	int startTouchViewTop = 0;
	private PictureTagView mPictureTagView;
	/**
	 * 控制屏幕是否滑动
	 */
	private boolean mIsToch = false;
	/**
	 * 触摸开始时间
	 */
	private long startTochLong = 0;
//	private ImageView mTipImageView;
	private View mTipView;

	public PictureTagLayout(Context context) {
		super(context, null);
		//mPictureTagView = new PictureTagView(context,PictureTagView.Direction.Right);
	}
	public PictureTagLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		//mPictureTagView = new PictureTagView(context,PictureTagView.Direction.Right);
		init();
	}
	private void init(){
		this.setOnTouchListener(this);
	} 
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(mIsToch)return true;
		if(mPictureTagView!=null)mPictureTagView.setImageGone();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startTochLong = System.currentTimeMillis();
			
			
			startX = (int) event.getX();
			startY = (int) event.getY();
			if(!blan){
				if(hasView(startX,startY)){
					startTouchViewLeft = mPictureTagView.getLeft();
					startTouchViewTop = mPictureTagView.getTop();
				}else{				
					setDefaultPosition(startX,startY);
					blan=true;
				}
			}
			
			if(hasView(startX,startY)){
				startTouchViewLeft = mPictureTagView.getLeft();
				startTouchViewTop = mPictureTagView.getTop();
			}
			else{				
//				addItem(startX,startY);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			moveView((int) event.getX(),
					(int) event.getY());
			break;
		case MotionEvent.ACTION_UP:
			int endX = (int) event.getX();
			int endY = (int) event.getY();
			//如果挪动的范围很小，则判定为单击
//			if(mPictureTagView!=null&&Math.abs(endX - startX)<CLICKRANGE&&Math.abs(endY - startY)<CLICKRANGE){
//				//当前点击的view进入编辑状态
//				mPictureTagView.setStatus(Status.Edit);
//			}
			
			 long times = System.currentTimeMillis()-startTochLong;
			  float offsetX = Math.abs(endX - startX);  
			  float offsetY = Math.abs(endY - startY);

			 if((0<times&&times<1500)&&offsetX<=50&&offsetY<=50){
				 if(mPictureTagView.getDirection()==PictureTagView.Direction.Left){
					 moveView(((int) event.getX())-mPictureTagView.getWidth()+10,
								(int) event.getY());
					mPictureTagView.setDirection(PictureTagView.Direction.Right);
					mPictureTagView.directionChange();
				 }else{
					 moveView(((int) event.getX())+(mPictureTagView.getWidth()-10),
								(int) event.getY());
					 mPictureTagView.setDirection(PictureTagView.Direction.Left);
					 mPictureTagView.directionChange(); 
				 }
			 }
			
			break;
		}
		return true;
	}

	public void setTipPosition(int x,int y){
		mTipView = LayoutInflater.from(getContext()).inflate(R.layout.tipview, this,
				true);
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.topMargin = y;
		params.leftMargin = x;
		this.addView(mTipView, params);
	}
	
	/**
	 * 初始化位置
	 * @param x
	 * @param y
	 */
	public void setDefaultPosition(int x,int y){
		 if(mPictureTagView != null)return;
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		if(x>getWidth()*0.5){
			params.leftMargin = x - PictureTagView.getViewWidth();
			mPictureTagView = new PictureTagView(getContext(),Direction.Right);
		}
		else{
			params.leftMargin = x;
			mPictureTagView = new PictureTagView(getContext(),Direction.Left);
		}
		params.topMargin = y;
		//上下位置在视图内
		this.addView(mPictureTagView, params);
		mPictureTagView.setText(title);
	}
	private void moveView(int x,int y){
		if(mPictureTagView == null) return;
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.leftMargin = x - startX + startTouchViewLeft;
		params.topMargin = y - startY + startTouchViewTop;
		//限制子控件移动必须在视图范围内
		if(params.leftMargin<0||(params.leftMargin+mPictureTagView.getWidth())>getWidth())
			params.leftMargin = mPictureTagView.getLeft();
		if(params.topMargin<0||(params.topMargin+mPictureTagView.getHeight())>getHeight())
			params.topMargin = mPictureTagView.getTop();
		mPictureTagView.setLayoutParams(params);
	}
	private boolean hasView(int x,int y){
		//循环获取子view，判断xy是否在子view上，即判断是否按住了子view
		for(int index = 0; index < this.getChildCount(); index ++){
			View view = this.getChildAt(index);
			int left = (int) view.getX();
			int top = (int) view.getY();
			int right = view.getRight();
			int bottom = view.getBottom();
			Rect rect = new Rect(left, top, right, bottom);
			boolean contains = rect.contains(x, y);
			//如果是与子view重叠则返回真,表示已经有了view不需要添加新view了
			if(contains){
//				mPictureTagView = view;
//				touchView.bringToFront();
				return true;
			}
		}
	
		return false;
	}
	/**
	 * 获取图标方向  1为向右，2为向左
	 * @return
	 */
	public int getDirection(){
		if(mPictureTagView.getDirection()==PictureTagView.Direction.Left){
			return 2;
		}else{
			return 1;
		}
	}
	/**
	 * 获取图片顶点X坐标。
	 * @return
	 */
	public int getXposition(){
//		if(mPictureTagView.getDirection()==PictureTagView.Direction.Left){
//			return (mPictureTagView.getLeft()*100)/getWidth();
//		}else{
//			return (mPictureTagView.getRight()*100)/getWidth();
//		}
		return (mPictureTagView.getLeft()*100)/getWidth();
		
	}
	/**
	 * 获取图片顶点Y坐标。
	 * @return
	 */
	public int getYposition(){
//		return ((mPictureTagView.getTop()+(mPictureTagView.getHeight()/2))*100)/getHeight();
		return (mPictureTagView.getTop()*100)/getHeight();
	}
	/**
	 * 设置标签文本
	 * @param title
	 */
	public void setText(String title){
		if(title==null){
			return;
		}
		this.title = title;
		
	}
	private String title="";
	/**
	 * 设置提示
	 */
	public void setTipViewVisable(){
		if(mPictureTagView==null){
			return;
		}
		this.mPictureTagView.setImageVisiable();
		
	}
	 /**
	  * 
	  * @param x  
	  * @param y      
	  * @param dirction   1为向右，2为向左
	  * @param isTouch  屏幕是否滑动
	  */
	public void setPosition(int x,int y,int dirction,boolean isTouch){
		 if(mPictureTagView != null)return;
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		if(x>getWidth()*0.5){
			params.leftMargin = x - PictureTagView.getViewWidth(); 
			mPictureTagView = new PictureTagView(getContext(),Direction.Right);
		}
		else{
			params.leftMargin = x;
			mPictureTagView = new PictureTagView(getContext(),Direction.Left);
		}
		params.topMargin = y;
		//上下位置在视图内
		this.addView(mPictureTagView, params);
		mPictureTagView.setText(title);
	}
}
