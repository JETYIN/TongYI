package com.qianseit.westore.activity.account;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.RoundAngleImageView;
import com.qianseit.westore.util.loader.FileUtils;

public class UploadingIDImageFragement extends BaseDoFragment {
	public static final int REQUEST_CODE_FRONT = 100;
	public static final int REQUEST_CODE_CONTRARY = 101;
	private final int REQUEST_CODE_CAPTURE_CAMEIA_FRONT = 102;
	private final int REQUEST_CODE_CAPTURE_CAMEIA_CONTRARY = 103;
	private RoundAngleImageView mFrontImage;
	private RoundAngleImageView mContraryImage;
	private Boolean mFront = false;
	private Boolean mContrary = false;
	private String mAddId;
	private Dialog dialog;
	private Dialog mPhoeDialog;
	private TextView mTiteText;
	private LayoutInflater mInflater;
	private File[] files = new File[2];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_uploading_title);
		Intent intent = getActivity().getIntent();
		mAddId = intent.getStringExtra(Run.EXTRA_DATA);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_uploading_image_main,
				null);
		mTiteText = (TextView) findViewById(R.id.fragment_uploading_title);
		mFrontImage = (RoundAngleImageView) findViewById(R.id.fragment_uploading_front);
		mContraryImage = (RoundAngleImageView) findViewById(R.id.fragment_uploading_contrary);
		findViewById(R.id.uploading_image_submit).setOnClickListener(this);
		mFrontImage.setOnClickListener(this);
		mContraryImage.setOnClickListener(this);
		String str = getResources().getString(
				R.string.account_uploading_require);
		SpannableStringBuilder style = new SpannableStringBuilder(str);
		style.setSpan(new ForegroundColorSpan(Color.parseColor("#f04641")), 0,
				1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTiteText.setText(style);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.fragment_uploading_front:
			startActivityForResult(
					AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_PHOTO)
							.putExtra("ID", "IDPHOTE")
							.putExtra("REQUE", REQUEST_CODE_FRONT),
					REQUEST_CODE_FRONT);
			break;
		case R.id.fragment_uploading_contrary:
			startActivityForResult(
					AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_PHOTO)
							.putExtra("ID", "IDPHOTE")
							.putExtra("REQUE", REQUEST_CODE_CONTRARY),
					REQUEST_CODE_CONTRARY);
			break;
		case R.id.uploading_image_submit:
			if (!mFront) {
				dialog = AccountLoginFragment.showAlertDialog(mActivity,
						"没有正面身份照片", "", "OK", new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}, null, false, null);
				return;
			}
			if (!mContrary) {
				dialog = AccountLoginFragment.showAlertDialog(mActivity,
						"没有反面身份照片", "", "OK", new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						}, null, false, null);
				return;
			}
			JsonTask task = new JsonTask();
			Run.excuteJsonTask(task, new UploadIdTask());
			break;

		default:
			break;
		}
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		FileOutputStream fos = null;
		switch (requestCode) {
		case REQUEST_CODE_FRONT:
			String path = data.getStringExtra("imagePath");
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File file = new File(path);
				if (file.exists()) {
					if (!file.exists())
						return;
					// 图尺寸大小限制
					double size = file.length() / 1024.0 / 1024.0;
//					if (size > 2) {
//						Run.alert(mActivity, R.string.shop_thumb_large_size);
//						return;
//					}
					Bitmap bitmap=FileUtils.getSmallBitmap(file.getAbsolutePath());
//					Bitmap bitmap = BitmapFactory.decodeFile(file
//							.getAbsolutePath());
					
					File pathfile = new File(Run.doCacheFolder, "file0");
					if (!pathfile.getParentFile().exists())
						pathfile.getParentFile().mkdirs();
					try {
						fos = new FileOutputStream(pathfile);
						bitmap.compress(CompressFormat.JPEG, 60, fos);
						fos.flush();
						files[0] = pathfile;
						mFront = true;
						mFrontImage.setImageBitmap(bitmap);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			findViewById(R.id.tv_uploading_front).setVisibility(View.VISIBLE);
			break;
		case REQUEST_CODE_CONTRARY:
			String contraryPath = data.getStringExtra("imagePath");
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File file = new File(contraryPath);
				if (file.exists()) {
					if (!file.exists())
						return;
					// 图尺寸大小限制
					double size = file.length() / 1024.0 / 1024.0;
//					if (size > 1) {
//						Run.alert(mActivity, R.string.shop_thumb_large_size);
//						return;
//					}
					Bitmap bitmap=FileUtils.getSmallBitmap(file.getAbsolutePath());
//					Bitmap bitmap = BitmapFactory.decodeFile(file
//							.getAbsolutePath());
					
					File pathfile = new File(Run.doCacheFolder, "file1");
					if (!pathfile.getParentFile().exists())
						pathfile.getParentFile().mkdirs();
					try {
						fos = new FileOutputStream(pathfile);
						bitmap.compress(CompressFormat.JPEG, 60, fos);
						fos.flush();
						files[1] = pathfile;
						mContrary = true;
						mContraryImage.setImageBitmap(bitmap);
					} catch (Exception e) {
						Log.v("TAG", e.toString());
						e.printStackTrace();
						StackTraceElement[] st = e.getStackTrace();
						for (StackTraceElement s : st) {
							Log.v("TAG", s.toString());
						}
					}
				}

			}
			findViewById(R.id.tv_uploading_contrary).setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	private class UploadIdTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.save_idcard");
			if (!TextUtils.isEmpty(mAddId)) {
				 bean.addParams("addr_id", mAddId);
			}
			if (files.length >= 2) {
				bean.addParams("type", "avatar");
				bean.files = files;
			}
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
//					startActivity(AgentActivity.intentForFragment(mActivity,
//							AgentActivity.FRAGMENT_SELECT_ID));
//					getActivity().finish();
					mActivity.setResult(Activity.RESULT_OK, new Intent().putExtra(Run.EXTRA_VALUE, all.optJSONObject("data").optString("id")));
					getActivity().finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

	}

	private Bitmap getimage(String srcPath) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// float hh
		// =mActivity.getWindowManager().getDefaultDisplay().getWidth(); //
		// 屏幕宽（像素，如：480px）
		// float ww
		// =mActivity.getWindowManager().getDefaultDisplay().getHeight(); //
		// 屏幕高（像素，如：800p）

		float hh = 150f;
		float ww = 300f;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	private Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 1000) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;// 每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

}
