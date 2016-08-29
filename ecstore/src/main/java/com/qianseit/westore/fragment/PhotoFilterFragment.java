package com.qianseit.westore.fragment;

import java.io.File;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.imageloader.ClipPictureBean;
import com.zhy.bean.ImageFloder;
import com.zhy.imageloader.ListImageDirPopupWindow.OnImageDirSelected;

public class PhotoFilterFragment extends BaseDoFragment implements OnImageDirSelected{
	
	/** 图片裁剪属性 */
	private ClipPictureBean clipPictureBean;
	private ImageView imageview;
	public PhotoFilterFragment() {
		super();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.tabbar_filter);
	}



	@SuppressLint("NewApi")
	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowHomeView(false);
//		()getActivity().getIntent().getStringArrayExtra("");
		rootView = inflater.inflate(R.layout.fragment_accounr_filter, null);
		imageview=(ImageView)rootView.findViewById(R.id.goods_detail_images);
		if (getActivity().getIntent().getExtras() != null) {
//		BitMapBean mpben=(BitMapBean)getActivity().getIntent().getParcelableExtra("bitmap");
//		Bitmap bt=mpben.getDw();
			String path=getActivity().getIntent().getStringExtra("path");	
			Log.i("tentinet-->", "000:"+path);
			File file = new File(path);
			
		    if (file.exists()) {
		    Bitmap bm = BitmapFactory.decodeFile(path);
		    //将图片显示到ImageView中
		    imageview.setImageBitmap(bm);
		    }
			
//		if (mpben.getDw()!=null) {
//				imageview.setImageBitmap(mpben.getDw());
//				
//			}else{
//				Toast.makeText(getActivity(), " 图片为空", 5000).show();
//			}
	}	
}
	
	@Override
	public void onResume() {
		super.onResume();
//		mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
	}

	@Override
	public void onPause() {
		super.onPause();
//		mHandler.removeMessages(0);
	}
	@Override
	public void selected(ImageFloder floder) {
		// TODO Auto-generated method stub
		
	}
}
