package com.qianseit.westore.clipictrue;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.activity.CropperActivity;
import com.qianseit.westore.imageloader.ClipPictureBean;
import com.qianseit.westore.imageloader.FileConfig;
import com.qianseit.westore.util.ChooseUtils;
import com.qianseit.westore.util.loader.FileUtils;


/**
 *@图片的放缩
 *@移动、截图
 *  
 */
public class ClipPictureActivitys extends Activity implements OnTouchListener,
		OnClickListener
{
	ImageView srcPic;
	ImageButton sure;
	ClipViews clipview;
	private ChooseUtils chooseInfo; 
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	final int NONE = 0;
	final int DRAG = 1;
	final int ZOOM = 2;
	private final String TAG = "11";
	int mode = NONE;
	ImageButton titlebar;
	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	/** 图片裁剪属性 */
	private ClipPictureBean clipPictureBean;
	private String mID;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent intent=this.getIntent();
		mID=intent.getStringExtra("ID");
		clipPictureBean = (ClipPictureBean)this. getIntent().getSerializableExtra(getString(R.string.intent_key_serializable));
		Bitmap bm =FileUtils.getBitMap(clipPictureBean.getSrcPath()) ;//getBitmap(clipPictureBean.getSrcPath());
	
		chooseInfo = (ChooseUtils)this. getIntent().getSerializableExtra(getString(R.string.intent_key_chooses));
		srcPic = (ImageView) this.findViewById(R.id.src_pic);
		titlebar = (ImageButton) this.findViewById(R.id.action_bar_titlebar_lefts);
		srcPic.setOnTouchListener(this);
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		
		bm = zoomImg(bm, wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
		if(bm != null  ){
			srcPic.setImageBitmap(bm);
		}
		
		sure = (ImageButton) this.findViewById(R.id.sure);
		sure.setOnClickListener(this);
		titlebar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ClipPictureActivitys.this.finish();
			}
		});
		
	}

	
	public static Bitmap zoomImg(Bitmap bm, int newWidth ,int newHeight){   
	    // 获得图片的宽高   
	    int width = bm.getWidth();   
	    int height = bm.getHeight();   
	    // 计算缩放比例   
	    float scaleWidth = ((float) newWidth) / width;   
	    float scaleHeight = ((float) newHeight) / height;   
	    // 取得想要缩放的matrix参数   
	    Matrix matrix = new Matrix();   
	    matrix.postScale(scaleWidth, scaleHeight);   
	    // 得到新的图片   www.2cto.com
	    Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);   
	    return newbm;   
	}  

	 
	private Bitmap getBitmap(String path){

		BitmapFactory.Options bfOptions=new BitmapFactory.Options();
        bfOptions.inDither=false;                    
        bfOptions.inPurgeable=true;              

        bfOptions.inInputShareable = true;

//        bfOptions.inJustDecodeBounds = true;
        File file = new File(path);
        if(!file.exists())return null;
        FileInputStream fs=null;
        try {
           fs = new FileInputStream(file);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
        Bitmap bmp = null;
        if(fs != null)
           try {
               bmp = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
           } catch (IOException e) {
               e.printStackTrace();
           }finally{ 
               if(fs!=null) {
                   try {
                       fs.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
		return bmp;
	}

	private Bitmap getBitmaps(String path){
//		Bitmap btp;
//		btp=BitmapFactory.decodeFile(clipPictureBean.getSrcPath());
		BitmapFactory.Options bfOptions=new BitmapFactory.Options();
        bfOptions.inDither=false;                    
        bfOptions.inPurgeable=true;              
   //     bfOptions.inTempStorage=new byte[12 * 1024]; 
        bfOptions.inSampleSize = 2;
        bfOptions.inJustDecodeBounds = true;
        File file = new File(path);
        if(!file.exists())return null;
        FileInputStream fs=null;
        try {
           fs = new FileInputStream(file);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
        Bitmap bmp = null;
        if(fs != null)
           try {
               bmp = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
           } catch (IOException e) {
               e.printStackTrace();
           }finally{ 
               if(fs!=null) {
                   try {
                       fs.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
		return bmp;
	}

	
	/*这里实现了多点触摸放大缩小，和单点移动图片的功能，参考了论坛的代码*/
	public boolean onTouch(View v, MotionEvent event)
	{
		ImageView view = (ImageView) v;
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK)
			{
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				// 設置初始點位置
				start.set(event.getX(), event.getY());
				Log.d(TAG, "mode=DRAG");
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				oldDist = spacing(event);
				Log.d(TAG, "oldDist=" + oldDist);
				if (oldDist > 10f)
				{
					savedMatrix.set(matrix);
					midPoint(mid, event);
					mode = ZOOM;
					Log.d(TAG, "mode=ZOOM");
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				Log.d(TAG, "mode=NONE");
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG)
				{
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);
				} else if (mode == ZOOM)
				{
					float newDist = spacing(event);
					Log.d(TAG, "newDist=" + newDist);
					if (newDist > 10f)
					{
						matrix.set(savedMatrix);
						float scale = newDist / oldDist;
						matrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
				break;
			}

		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	private String imgPaths;
	
	/*点击进入预览*/
	@SuppressWarnings("static-access")
	public void onClick(View v)
	{
		
		 Bitmap bitmap1 = getBitmap();
		 @SuppressWarnings("static-access")
		 String paths=FileConfig.PATH_BASE+new DateFormat().format("yyyyMMdd_hhmmss",
					Calendar.getInstance(Locale.CHINA))+".jpg";
		 boolean b=saveBitmapToFile(paths,bitmap1);
		 imgPaths=paths;
		 if(b){
			if("IDPHOTE".equals(mID)){
				Intent mIntent = new Intent();
				mIntent.putExtra("imagePath",paths);
				this.setResult(this.RESULT_OK,mIntent);
				finish();
			}
			else{
//			Intent intent = AgentActivity.intentForFragment(this, AgentActivity.FRAGMENT_ACCORDING);
////			intent.putExtras(bundle);
//			intent.putExtra("path", paths);
//			startActivity(intent);
//				Bitmap fianBitmap = getBitmap();
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				fianBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//				byte[] bitmapByte = baos.toByteArray();
				
				@SuppressWarnings("static-access")
		 			String outpath = FileConfig.PATH_BASE
						+ new DateFormat().format("yyyyMMdd_hhmmss",
								Calendar.getInstance(Locale.CHINA))
								+ ".jpg";
				// PGEditSDK.instance().startEdit(ClipPictureActivitys.this, PGEditActivity.class, paths, outpath);



				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				intent.setClass(getApplicationContext(),
						LableActivity.class);
				bundle.putSerializable(
						getString(R.string.intent_key_chooses), chooseInfo);
				intent.putExtra("bitmap", paths);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
				
//				Intent intent = new Intent();
//				Bundle bundle = new Bundle();
//				bundle.putSerializable(getString(R.string.intent_key_chooses), chooseInfo);
//				intent.setClass(getApplicationContext(), PreviewActivitys.class);
//				intent.putExtra("bitmap", paths);
//				intent.putExtras(bundle);
//				startActivity(intent);	
//				this.finish();
			}
		 }
	}
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// 获取编辑后的大图路径
//		String resultPhotoPath=null;
//		if (requestCode == PGEditSDK.PG_EDIT_SDK_REQUEST_CODE
//				&& resultCode == Activity.RESULT_OK) {
//
//				PGEditResult editResult = PGEditSDK.instance().handleEditResult(data);
//
//					// 获取编辑后的缩略图
//					Bitmap thumbNail = editResult.getThumbNail();
//
//					resultPhotoPath= editResult.getReturnPhotoPath();
//					Intent intent = new Intent();
//					Bundle bundle = new Bundle();
//					intent.setClass(getApplicationContext(),
//							LableActivity.class);
//					bundle.putSerializable(
//							getString(R.string.intent_key_chooses), chooseInfo);
//					intent.putExtra("bitmap", resultPhotoPath);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					finish();
//
//				}
//
//				if (requestCode == PGEditSDK.PG_EDIT_SDK_REQUEST_CODE&& resultCode == PGEditSDK.PG_EDIT_SDK_RESULT_CODE_CANCEL) {
//					//用户取消编辑
//					resultPhotoPath=imgPaths;
//					finish();
//				}
//
//				if (requestCode == PGEditSDK.PG_EDIT_SDK_REQUEST_CODE&& resultCode == PGEditSDK.PG_EDIT_SDK_RESULT_CODE_NOT_CHANGED) {
//					// 照片没有修改
//					resultPhotoPath=imgPaths;
//					Intent intent = new Intent();
//					Bundle bundle = new Bundle();
//					intent.setClass(getApplicationContext(),
//							LableActivity.class);
//					bundle.putSerializable(
//							getString(R.string.intent_key_chooses), chooseInfo);
//					intent.putExtra("bitmap", resultPhotoPath);
//					intent.putExtras(bundle);
//					startActivity(intent);
//					finish();
//
//				}
//				if(resultCode==0001){
//					finish();
//				}
//
//		super.onActivityResult(requestCode, resultCode, data);
//
//	}
	/*获取矩形区域内的截图*/
	private Bitmap getBitmap()
	{
		getBarHeight();
		Bitmap screenShoot = takeScreenShot();
	
		clipview = (ClipViews)this.findViewById(R.id.clipview);
		int width = clipview.getWidth();
		int height = clipview.getHeight();
		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot,
				(width - height / 2) / 2, height / 4 + titleBarHeight + statusBarHeight, height / 2, height / 2);
		return finalBitmap;
	}

	int statusBarHeight = 0;
	int titleBarHeight = 0;

	private void getBarHeight()
	{
		// 获取状态栏高度
		Rect frame = new Rect();
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
		
		int contenttop = this.getWindow()
				.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// statusBarHeight是上面所求的状态栏的高度
		titleBarHeight = contenttop - statusBarHeight;
		
		Log.v(TAG, "statusBarHeight = " + statusBarHeight
				+ ", titleBarHeight = " + titleBarHeight);
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
	// 获取Activity的截屏
	private Bitmap takeScreenShot()
	{
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

}