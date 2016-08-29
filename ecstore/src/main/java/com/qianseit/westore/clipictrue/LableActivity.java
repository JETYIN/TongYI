package com.qianseit.westore.clipictrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.activity.AecommendedLanguageActivity;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.imageloader.FileConfig;
import com.qianseit.westore.picturetagview.PictureTagLayout;
import com.qianseit.westore.util.ChooseUtils;

public class LableActivity extends Activity{
	
	private PictureTagLayout sImageView;
	private Bitmap sBitmap;
	private ImageButton sBack;
	private ImageButton sBar;
	private MyClick click ;
	@SuppressWarnings("unused")
	private RelativeLayout picture;
	private ChooseUtils chooseInfo;   
	private String imagePath;
	private LableActivity mContext;
	private Bitmap mBitmap;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fragment_account_label);
		mContext = this;
		click=new MyClick();
		sImageView = (PictureTagLayout) this.findViewById(R.id.imgfilter);
		
		sBack = (ImageButton) this.findViewById(R.id.action_bar_titlebar_lefts);
		sBar = (ImageButton) this.findViewById(R.id.suress);
		chooseInfo = (ChooseUtils)this. getIntent().getSerializableExtra(getString(R.string.intent_key_chooses));
		picture = (RelativeLayout) this.findViewById(R.id.picturess);
		picture.setDrawingCacheEnabled(true);
		sImageView.setText(chooseInfo.getBrand_name());
		//sImageView.setText("蘑ff菇");
		sBack.setOnClickListener(click);
		sBar.setOnClickListener(click);
		
		if (this.getIntent().getExtras() != null) {
			imagePath=this.getIntent().getStringExtra("bitmap");	
			Log.i("tentinet-->", "000:"+imagePath);
			if(imagePath==null){
				finish();
				return;
			}
			File file = new File(imagePath);
			
			if (file.exists()) {  
				mBitmap=BitmapFactory.decodeFile(imagePath);
//			mBitmap = BitmapFactory.decodeResource(
//					mContext.getResources(), R.drawable.image);
				sBitmap =cutBitmap(mBitmap);
				Resources res = getResources(); //resource handle
				Drawable drawable =new BitmapDrawable(sBitmap);
				sImageView.setBackgroundDrawable(drawable);
				WindowManager wm = (WindowManager)this 
	                    .getSystemService(Context.WINDOW_SERVICE); 
	  
	     int width = wm.getDefaultDisplay().getWidth(); 
	     int height=width*sBitmap.getHeight()/sBitmap.getWidth();
	     sImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
	     width = width/2;
	     sImageView.setDefaultPosition(width-100, width-100);
	     sImageView.setTipViewVisable();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	//单击事件
	public class MyClick implements OnClickListener{

		@Override
		public void onClick(View view) {
			switch(view.getId()){
				case R.id.action_bar_titlebar_lefts:
					sImageView.getDirection();
					sImageView.getXposition();
					sImageView.getYposition();
					LableActivity.this.finish();
					break;
				case R.id.suress:
//					Bitmap obmp = Bitmap.createBitmap(picture.getDrawingCache());
					picture.setDrawingCacheEnabled(false);
					int directions=sImageView.getDirection();
					int xposition=sImageView.getXposition();
					int yposition=sImageView.getYposition();
					
					if(mBitmap!=null){
						@SuppressWarnings("static-access")   
						String paths=FileConfig.PATH_BASE+new DateFormat().format("yyyyMMdd_hhmmss",
								Calendar.getInstance(Locale.CHINA))+".jpg";
						saveBitmapToFile(paths,mBitmap);
//						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						Intent intent = AgentActivity.intentForFragment(LableActivity.this, AgentActivity.FRAGMENT_PRAISE_AECOMMEND);
//						intent.setClass(getApplicationContext(), AecommendedLanguageActivity.class);
						bundle.putSerializable(getString(R.string.intent_key_chooses), chooseInfo);
						intent.putExtras(bundle);
						intent.putExtra("bitmap", paths);
						intent.putExtra("imagePath", imagePath);
						intent.putExtra("directions", String.valueOf(directions));
						intent.putExtra("xposition",  String.valueOf(xposition));
						intent.putExtra("yposition",  String.valueOf(yposition));
						startActivity(intent);	
						LableActivity.this.finish();
						
					}else{
						Toast.makeText(LableActivity.this, "失败！", 5000).show();
					}
					break;
			}
		}
		
	}
	/**
	 * 保存图片
	 * @param saveParentPath
	 *            图片保存路径
	 * @param bitmap
	 *            位图对象
	 */
	public boolean saveBitmapToFile(String saveParentPath, Bitmap bitmap) {
		
		try {
			File saveimg = new File(saveParentPath);
			if(!saveimg.exists())
			 saveimg.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveimg));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			return true;
		} catch (IOException e) {
			Log.i("tentinet",e.toString());
			
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 正方形
	 * @param bmp
	 * @return
	 */
	public Bitmap cutBitmap(Bitmap bmp)
    {
	Bitmap result;
	int w = bmp.getWidth();//输入长方形宽
	int h = bmp.getHeight();//输入长方形高
	int nw;//输出正方形宽
	if(w > h)
	{
	    //宽大于高
	    nw = h;
	    result = Bitmap.createBitmap(bmp, (w - nw) / 2, 0, nw, nw);
	}else{
	  //高大于宽
	    nw = w;
	    result = Bitmap.createBitmap(bmp, 0 , (h - nw) / 2, nw, nw);
	}
	return result;
    } 
	
	
}
