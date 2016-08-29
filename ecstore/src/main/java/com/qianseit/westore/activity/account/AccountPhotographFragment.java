package com.qianseit.westore.activity.account;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.CropperActivity;
import com.qianseit.westore.imageloader.ClipPictureBean;
import com.qianseit.westore.util.ChooseUtils;
import com.zhy.bean.ImageFloder;
import com.zhy.imageloader.ListImageDirPopupWindow;
import com.zhy.imageloader.ListImageDirPopupWindow.OnImageDirSelected;
import com.zhy.imageloader.MyAdapter;
import com.zhy.utils.ViewHolder;

public class AccountPhotographFragment extends BaseDoFragment implements
		OnImageDirSelected {
	private ProgressDialog mProgressDialog;

	private final int PHOTO_GRAPH = 0X100;
//	private final int PHOTO_RESOULT = 0X101;
//	private final String IMAGE_UNSPECIFIED = "image/*";
//	private String time;
	private String mID;
	/**
	 * 存储文件夹中的图片数量
	 */
//	private int mPicsSize;
	/**
	 * 图片数量最多的文件夹
	 */
	private File mImgDir;
	/**
	 * 所有的图片
	 */
	private List<String> mImgs;
	private ChooseUtils chooseInfo;
	private GridView mGirdView;
	private MyAdapter mAdapter;
	private File f;
	public static final String PHOTO_FILE_NAME = "temp_photo.png";
	/**
	 * 临时的辅助类，用于防止同一个文件夹的多次扫描
	 */
	private HashSet<String> mDirPaths = new HashSet<String>();

	/**
	 * 扫描拿到所有的图片文件夹
	 */
	private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();

	private LinearLayout mBottomAlbum;
	private TextView mPhotographText;
	int totalCount = 0;
	int reque;

	private int mScreenHeight;

	private ListImageDirPopupWindow mListImageDirPopupWindow;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mProgressDialog.dismiss();
			// 为View绑定数据
			data2View();
			// 初始化展示文件夹的popupWindw
			initListDirPopupWindw();
		}
	};

	/**
	 * 为View绑定数据
	 */
	private void data2View() {
		if (mImgDir == null) {

			Run.alert(mActivity, "没有扫描到图片。");
			return;
		}

		mImgs = Arrays.asList(mImgDir.list());
		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		initLoader();
		mAdapter = new MyAdapter(mActivity, mImgs, R.layout.grid_item,
				mImgDir.getAbsolutePath());
		mGirdView.setAdapter(mAdapter);
	};
	
	private void initLoader(){
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.default_img_rect)
		.showImageForEmptyUri(R.drawable.default_img_rect)
		.showImageOnFail(R.drawable.default_img_rect).cacheInMemory(true)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300, true, true, true))
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				mActivity).defaultDisplayImageOptions(defaultOptions)
		.memoryCache(new WeakMemoryCache());
		ImageLoaderConfiguration config = builder.build();
		ImageLoader.getInstance().init(config);
	}

	/**
	 * 初始化展示文件夹的popupWindw
	 */
	private void initListDirPopupWindw() {

		View view = LayoutInflater.from(mActivity).inflate(R.layout.list_dir,
				null);
		view.findViewById(R.id.id_list_album).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mListImageDirPopupWindow.dismiss();

					}
				});
		mListImageDirPopupWindow = new ListImageDirPopupWindow(
				LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.5),
				mImageFloders, view);

		mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = mActivity.getWindow()
						.getAttributes();
				lp.alpha = 1.0f;
				mActivity.getWindow().setAttributes(lp);
			}
		});
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setShowTitleBar(false);
		Intent intent = mActivity.getIntent();
		mID = intent.getStringExtra("ID");
		reque = intent.getIntExtra("REQUE", 0);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_photograph_main, null);
		chooseInfo = (ChooseUtils) mActivity.getIntent().getSerializableExtra(
				getString(R.string.intent_key_serializable));
		DisplayMetrics outMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;

		initView();
		getImages();
		initEvent();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Run.alert(mActivity, "暂无外部存储");
			return;
		}
		// 显示进度条
		mProgressDialog = ProgressDialog.show(mActivity, null, "正在加载...");

		new Thread(new Runnable() {
			@Override
			public void run() {

				String firstImage = null;

				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = mActivity
						.getContentResolver();

				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_MODIFIED);

				boolean nFirst = true;//取第一个目录的图片就可以了
				while (mCursor.moveToNext()) {
					// 获取图片的路径
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));

					// 拿到第一张图片的路径
					if (firstImage == null)
						firstImage = path;
					// 获取该图片的父路径名
					File parentFile = new File(path).getParentFile();
					if (parentFile == null)
						continue;
					String dirPath = parentFile.getAbsolutePath();
					ImageFloder imageFloder = null;
					// 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
					if (mDirPaths.contains(dirPath)) {
						continue;
					} else {
						mDirPaths.add(dirPath);
						// 初始化imageFloder
						imageFloder = new ImageFloder();
						imageFloder.setDir(dirPath);
						imageFloder.setFirstImagePath(path);
					}

					String[] strImage = parentFile.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".png")
									|| filename.endsWith(".jpeg"))
								return true;
							return false;
						}
					});
					int picSize = strImage != null ? strImage.length : 0;
					totalCount += picSize;

					imageFloder.setCount(picSize);
					mImageFloders.add(imageFloder);

					if (nFirst){//picSize > mPicsSize) {//取第一个目录的图片就可以了
						//mPicsSize = picSize;
						nFirst = false;
						mImgDir = parentFile;
					}
				}
				mCursor.close();

				// 扫描完成，辅助的HashSet也就可以释放内存了
				mDirPaths = null;

				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(0x110);

			}
		}).start();

	}

	/**
	 * 初始化View
	 */
	private void initView() {
		mGirdView = (GridView) findViewById(R.id.id_gridView);
		mBottomAlbum = (LinearLayout) findViewById(R.id.photo_album_linear);
		mPhotographText = (TextView) findViewById(R.id.photo_camera);
		findViewById(R.id.photo_back).setOnClickListener(this);
		mPhotographText.setOnClickListener(this);
		mGirdView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// Intent mIntent = new Intent();
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				ImageView imageView = (ImageView) viewHolder
						.getView(R.id.id_item_image);
				String imagePath = (String) imageView.getTag();
				//
				// Intent intent = AgentActivity.intentForFragment(
				// mActivity, AgentActivity.FRAGMENT_GOODS_XIUJIAN);
				//
				// startActivity(intent);
				Log.i("tentinet--->", "1111:" + imagePath);
				gotoCrop(imagePath);
				// mIntent.putExtra("imagePath", imagePath);
				// getActivity().setResult(Activity.RESULT_OK, mIntent);
				// getActivity().finish();
			}
		});

	}

	/**
	 * 调用系统截图工具裁剪图片
	 */
	private void gotoCrop(String path) {
		Log.i("tentinet---->", "" + "333333333" + path);
		Bundle bundle = new Bundle();
		ClipPictureBean clipPictureBean = new ClipPictureBean();
		clipPictureBean.setSrcPath(path);
		clipPictureBean.setOutputX(150);
		clipPictureBean.setOutputY(150);
		// clipPictureBean.setOutputX(600);
		// clipPictureBean.setOutputY(600);
		clipPictureBean.setAspectX(1);
		clipPictureBean.setAspectY(1);
		bundle.putSerializable(getString(R.string.intent_key_serializable),
				clipPictureBean);
		bundle.putSerializable(getString(R.string.intent_key_chooses),
				chooseInfo);
		// IntentUtil.gotoActivityForResult(this, ClipPictureActivity.class,
		// bundle, RequestCode.REQUEST_CODE_CROP_ICON);
		// Bundle bundle = new Bundle();
		// bundle.putByteArray("picture", data);
		bundle.putSerializable(getString(R.string.intent_key_serializable),
				clipPictureBean);
		// IntentUtil.gotoActivityForResult(this, ClipPictureActivity.class,
		// bundle, RequestCode.REQUEST_CODE_CROP_ICON);
//		Intent intent = new Intent(mActivity, ClipPictureActivitys.class);
		Intent intent = new Intent(mActivity, CropperActivity.class);
		// Bundle bundle = new Bundle();
		// bundle.putByteArray("picture", data);
		intent.putExtras(bundle);
		// startActivity(intent);

		//
		// Intent intent = AgentActivity.intentForFragment(mActivity,
		// AgentActivity.FRAGMENT_GOODS_XIUJIAN);
		// intent.putExtras(bundle);
		// intent.putExtra("ID",mID);
		// startActivityForResult(intent, reque);
		// getActivity().finish();
		intent.putExtra("ID", mID);
		startActivityForResult(intent, reque);
		if ("IDPHOTE".equals(mID) && "AVATAR".equals(mID)) {
			getActivity().finish();
		}
	}
	
	private void initEvent() {
		/**
		 * 为底部的布局设置点击事件，弹出popupWindow
		 */
		mBottomAlbum.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListImageDirPopupWindow
						.setAnimationStyle(R.style.anim_popup_dir);
				mListImageDirPopupWindow.showAsDropDown(mBottomAlbum, 0, 0);

				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = mActivity.getWindow()
						.getAttributes();
				lp.alpha = .3f;
				mActivity.getWindow().setAttributes(lp);
			}
		});
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.photo_camera:

//			f = new File(Environment.getExternalStorageDirectory()
//					+ "/DCIM/Camera/" + UUID.randomUUID().toString() + ".png");
//			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
//			// Log.e("------------------------------",
//			// ""+Environment.getExternalStorageDirectory()
//			// + "/DCIM/Camera/"+ UUID.randomUUID().toString()+".png");
//			startActivityForResult(intent, PHOTO_GRAPH);

			camera();

			break;
		case R.id.photo_back:
			getActivity().finish();
			break;

		default:
			break;
		}
	}

	/*
	 * 从相机获取
	 */
	public void camera() {
		if(hasSdcard()) {
			f = new File(Environment.getExternalStorageDirectory(),
					PHOTO_FILE_NAME);
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			// 判断存储卡是否可以用，可用进行存储
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), PHOTO_FILE_NAME)));
			startActivityForResult(intent, PHOTO_GRAPH);
		}else {
			Toast.makeText(getActivity(),"SD卡不存在，无法拍照！",Toast.LENGTH_SHORT).show();
		}
	}

	public boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void selected(ImageFloder floder) {

		mImgDir = new File(floder.getDir());
		mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".jpg") || filename.endsWith(".png")
						|| filename.endsWith(".jpeg"))
					return true;
				return false;
			}
		}));
		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		mAdapter = new MyAdapter(mActivity, mImgs, R.layout.grid_item,
				mImgDir.getAbsolutePath());
		mGirdView.setAdapter(mAdapter);
		mListImageDirPopupWindow.dismiss();

	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		// 拍照
		if (requestCode == PHOTO_GRAPH) {

			String sdStatus = Environment.getExternalStorageState();
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
				Log.i("TestFile",
						"SD card is not avaiable/writeable right now.");
				return;
			}
			if (!f.exists())
				return;
//			Bitmap bitmap1 = BitmapFactory.decodeFile(f.getAbsolutePath());
			gotoCrop(f.getAbsolutePath());

//			if (!"IDPHOTE".equals(mID)) {
//				getActivity().finish();
//			}

			// Bundle bundle = data.getExtras();
			// Bitmap bitmap = (Bitmap) bundle.get("data");//
			// 获取相机返回的数据，并转换为Bitmap图片格式
			//
			// FileOutputStream b = null;
			// String abPath = Environment.getExternalStorageDirectory()
			// .getAbsolutePath();
			// File file = new File(abPath + "/myImage/");
			// file.mkdirs();// 创建文件夹
			// String fileName = abPath + "/myImage/" ;

			// try {
			// b = new FileOutputStream(fileName);
			// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
			// } catch (Exception e) {
			// e.printStackTrace();
			// } finally {
			// try {
			// b.flush();
			// b.close();
			// Intent mIntent = new Intent();
			// Log.i("tentinet--->", "2222:" + fileName);
			// // mIntent.putExtra("imagePath", f.getAbsolutePath());
			// getActivity().setResult(Activity.RESULT_OK, mIntent);
			// gotoCrop(f.getAbsolutePath());
			// getActivity().finish();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
		} else if (requestCode == UploadingIDImageFragement.REQUEST_CODE_FRONT) {
			mActivity.setResult(resultCode, data);
			getActivity().finish();
		} else if (requestCode == UploadingIDImageFragement.REQUEST_CODE_CONTRARY) {
			mActivity.setResult(resultCode, data);
			getActivity().finish();
		} else {//上传头像
			mActivity.setResult(resultCode, data);
			getActivity().finish();
		}

	}

}
