package com.qianseit.westore.imageloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.util.Comm;

public class ClipPictureActivity extends BaseDoFragment implements OnTouchListener, OnClickListener {
	/** 加载图片成功 */
	public final int LOAD_IMG_SUCCESS = 0;
	/** 加载图片失败 */
	public final int LOAD_IMG_FAIL = 1;
	/** 原图图片控件 */
	private ImageView img_SrcPic;
	/** 裁剪区域视图 */
	private ClipView clipview;
	private final int VIDEO_WIDTH = 160;     
	private final int VIDEO_HEIGHT = 200; 
	/** 确定按钮 */
	private Button btn_Enter;
	/** 确定按钮 */
	private Button btn_rotate;
	/** 取消按钮 */
	private Button btn_Canel;

	/** 图片显示区域视图是否加载完毕 */
	private boolean hasMeasured = false;

	/** 图片裁剪属性 */
	private ClipPictureBean clipPictureBean;
	/** 工具栏高度 */
	private int statusBarHeight = 0;
	private String mID;

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	Matrix savedrotate = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	private static final String TAG = "11";
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	/** 图片的最小缩放比例 */
	private float minScaleR = 1f;
	// 位图
	private Bitmap bitmap;
	/** 显示图片区域高度 */
	private int screenHeight = 0;
	/** 显示图片区域宽度 */
	private int screenWidth = 0;
	private String type = "";
	private String name = "";
	private int num = 1;
	
	
	public ClipPictureActivity(){
		super();	
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.goods_ptotoss);
		Intent intent=mActivity.getIntent();
		mID=intent.getStringExtra("ID");
	}
	
	  
	@SuppressLint("NewApi")
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowHomeView(false);
//		()getActivity().getIntent().getStringArrayExtra("");
		rootView = inflater.inflate(R.layout.photos, null);
		
		img_SrcPic = (ImageView) this.findViewById(R.id.src_pic);
		btn_Enter = (Button) this.findViewById(R.id.btn_enter);
		btn_rotate = (Button) this.findViewById(R.id.btn_rotate);
		btn_Canel = (Button) this.findViewById(R.id.btn_cancel);
		clipview = (ClipView) this.findViewById(R.id.clipview);

		
		initGetData();
		widgetListener();
		init();
	}
	
	protected void init() {
		savedrotate.preRotate(90);// matrix对旋转有两种方法，一个是preRotate(floag
		// angle)，此方法是默认旋转给定角度
		getScreenSize();

	}

	protected void widgetListener() {
		img_SrcPic.setOnTouchListener(this);
		btn_Enter.setOnClickListener(this);
		btn_Canel.setOnClickListener(this);
		btn_rotate.setOnClickListener(this);
	}

	protected void initGetData() {
		if (getActivity().getIntent().getExtras() != null) {
			clipPictureBean = (ClipPictureBean)getActivity(). getIntent().getSerializableExtra(getString(R.string.intent_key_serializable));
			type = getActivity().getIntent().getExtras().getString(getString(R.string.intent_key_type));
			name = getActivity().getIntent().getExtras().getString(getString(R.string.intent_key_image_name));
			if (TextUtils.isEmpty(type)) {
				type = "";
			}
			if (TextUtils.isEmpty(name)) {
				name = "";
			}
		}
//		super.initGetData();
	}

	/**
	 * 获取显示区域大小
	 * 
	 * @version 1.0
	 * @createTime 2013-11-6,下午3:11:57
	 * @updateTime 2013-11-6,下午3:11:57
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 */
	private void getScreenSize() {

		ViewTreeObserver vto = clipview.getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {

				if (!hasMeasured) {
					screenHeight = clipview.getMeasuredHeight();
					screenWidth = clipview.getMeasuredWidth();

					if (null != clipPictureBean) {
						clipview.setClipPictureBean(clipPictureBean);
						getLocalBitmap();
					}

					hasMeasured = true;
				}
				return true;
			}
		});
	}

	/**
	 * 最小缩放比例，最大为100%
	 * 
	 * @version 1.0
	 * @createTime 2013-11-6,上午11:37:35
	 * @updateTime 2013-11-6,上午11:37:35
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 */
	private void minZoom() {
		minScaleR = Math.min((float) screenWidth / (float) bitmap.getWidth(), (float) screenHeight / (float) bitmap.getHeight());
		matrix.setScale(minScaleR, minScaleR);
	}

	/* 这里实现了多点触摸放大缩小，和单点移动图片的功能，参考了论坛的代码 */
	public boolean onTouch(View v, MotionEvent event) {
		if (null == bitmap) {
			return true;
		}
		ImageView view = (ImageView) v;
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			matrix.set(view.getImageMatrix());
			savedMatrix.set(matrix);
			// 設置初始點位置
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				// ...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
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
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/**
	 * 横向、纵向居中
	 * 
	 * @version 1.0
	 * @createTime 2013-11-6,上午11:33:38
	 * @updateTime 2013-11-6,上午11:33:38
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param horizontal
	 *            是否横向居中
	 * @param vertical
	 *            是否垂直居中
	 */
	private void center(boolean horizontal, boolean vertical) {

		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		if (vertical) {

			if (height < screenHeight) {
				deltaY = (screenHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < screenHeight) {
				deltaY = img_SrcPic.getHeight() - rect.bottom;
			}
		}

		if (horizontal) {
			if (width < screenWidth) {
				deltaX = (screenWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < screenWidth) {
				deltaX = screenWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);
		img_SrcPic.setImageMatrix(matrix);

	}

	/**
	 * 点击事件监听
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午5:24:40
	 * @updateTime 2014年6月10日,下午5:24:40
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param v
	 */
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_enter:
			
			
		 Bitmap bitmap1 = getBitmap();
		 String paths=Comm.doFolder+new DateFormat().format("yyyyMMdd_hhmmss",
					Calendar.getInstance(Locale.CHINA))+".jpg";
		 boolean b=saveBitmapToFile(paths,bitmap1);
		 if(b){
			if("IDPHOTE".equals(mID)){
				Intent mIntent = new Intent();
				mIntent.putExtra("imagePath",paths);
				mActivity.setResult(Activity.RESULT_OK,mIntent);
				mActivity.finish();
			}
			else{
			Toast.makeText(getActivity(), "上传成功！", 5000).show();
			Intent intent = AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_ACCORDING);
//			intent.putExtras(bundle);
			intent.putExtra("path", paths);
			startActivity(intent);
			mActivity.finish();
			}
		 }
//			
			break;
		case R.id.btn_cancel:
			getActivity().finish();
			break;
		case R.id.btn_rotate:
			if (bitmap != null) {
				if (num % 2 == 0) {
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), savedrotate, true);
				} else {
					bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), savedrotate, true);
				}
				num++;
				img_SrcPic.setImageBitmap(bitmap);
			}
			break;
		}
	}

	/**
	 * 保存图片
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午4:52:43
	 * @updateTime 2014年6月10日,下午4:52:43
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
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
			e.printStackTrace();
			Log.e("TAG", e.toString());
			return false;
		}
	}

	/* 获取矩形区域内的截图 */
	private Bitmap getBitmap() {
		getBarHeight();
		Bitmap finalBitmap = takeScreenShot();
		finalBitmap = Bitmap.createBitmap(finalBitmap, (clipview.getScreenWidth() - clipview.getClipWidth()) / 2, (clipview.getScreenHeight() - clipview.getClipHeight()) / 2
				+ statusBarHeight, clipview.getClipWidth(), clipview.getClipHeight());
		finalBitmap = Bitmap.createScaledBitmap(finalBitmap, clipPictureBean.getOutputX(), clipPictureBean.getOutputY(), true);

		return finalBitmap;
	}

	/**
	 * 获取工具栏的高度
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午4:55:06
	 * @updateTime 2014年6月10日,下午4:55:06
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 */
	private void getBarHeight() {
		// 获取状态栏高度
		Rect frame = new Rect();
		getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
	}

	/**
	 * 获取屏幕截图
	 * 
	 * @version 1.0
	 * @createTime 2014年6月10日,下午4:54:42
	 * @updateTime 2014年6月10日,下午4:54:42
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @return 当前屏幕的截图
	 */
	private Bitmap takeScreenShot() {
		View view = getActivity().getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

	/**
	 * 载本地图片
	 * 
	 * @version 1.0
	 * @createTime 2015-3-6,下午5:14:20
	 * @updateTime 2015-3-6,下午5:14:20
	 * @createAuthor yeqing
	 * @updateAuthor yeqing
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 */
	protected void getLocalBitmap() {
//		ProcessDialogUtil.showDialog(ClipPictureActivity.this, getString(R.string.auth_process_login_wait), true);
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = new Message();
				String path = "";
				if (type != null && type.equals("1")) {
					path = new ImageUtil().compressImage(clipPictureBean.getSrcPath(), name, 100);
				} else {
					path = new ImageUtil().compressImage(clipPictureBean.getSrcPath(), System.currentTimeMillis() + "temp.jpg", 100);
				}
//				Bitmap bitmap = BitmapFactory.decodeFile(path);
				Bitmap bitmap = getImageThumbnail(clipPictureBean.getSrcPath()) ;
				
				if (null != bitmap) {
					Log.i("tentinet-->", "8989:"+path);
					msg.what = LOAD_IMG_SUCCESS;
					msg.obj = bitmap;
					handler.sendMessage(msg);
				} else {
					Log.i("tentinet-->", "meiyou::"+path);
					msg.what = LOAD_IMG_FAIL;
					msg.obj = "load image error";
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	private Bitmap getImageThumbnail(String imagePath) {  
		System.out.println("getImageThumbnail - imagePath: " + imagePath);     
		Bitmap bitmap = null;    
		BitmapFactory.Options options = new BitmapFactory.Options();    
		options.inJustDecodeBounds = true;  //不申请内存 计算图片比例               
		// 获取这个图片的宽和高，注意此处的bitmap为null       
		bitmap = BitmapFactory.decodeFile(imagePath, options);    
		options.inJustDecodeBounds = false; //设为 false  申请内存    
		// 计算缩放比       
		int h = options.outHeight;     
		int w = options.outWidth;     
		int beWidth  = w / VIDEO_WIDTH;  
		int beHeight = h / VIDEO_HEIGHT;    
		int be = 4;         
		if (beWidth < beHeight && beHeight >= 1) {    
			be = beHeight;     
			}      
		if (beHeight< beWidth  && beWidth >= 1) {   
			be = beWidth;   
			}              
		if (be <= 0) {      
			be = 1;        
			} else if (be > 3) { 
				be = 3;      
				}               
		options.inSampleSize = be;   
		options.inPreferredConfig = Bitmap.Config.ARGB_4444;   
		options.inPurgeable = true;   
		options.inInputShareable = true;   
		try {      
			// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
			bitmap = BitmapFactory.decodeFile(imagePath, options);   
			// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象             
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, VIDEO_WIDTH, VIDEO_HEIGHT, 
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);     
			} catch (OutOfMemoryError e) {       
				System.gc();           
				bitmap = null;      
				}               
		return bitmap;  
	}

  

	
	
	
	/**
	 * 异步消息处理对象
	 */
	protected Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

//			ProcessDialogUtil.dismissDialog();

			switch (msg.what) {

			case LOAD_IMG_SUCCESS:
				Bitmap bm = (Bitmap) msg.obj;
				bitmap = bm;
				img_SrcPic.setImageBitmap(bm);
				matrix.set(img_SrcPic.getImageMatrix());
				minZoom();
				center(true, true);
				break;

			case LOAD_IMG_FAIL:
				String string = (String) msg.obj;
//				ToastUtil.showToast(ClipPictureActivity.this, string);
				break;

			default:
				break;
			}
		}

	};

}