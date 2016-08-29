package com.qianseit.westore.clipictrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.qianseit.westore.imageloader.FileConfig;
import com.qianseit.westore.textures.BlackWhiteFilter;
import com.qianseit.westore.textures.BrightContrastFilter;
import com.qianseit.westore.textures.ColorToneFilter;
import com.qianseit.westore.textures.HslModifyFilter;
import com.qianseit.westore.textures.IImageFilter;
import com.qianseit.westore.textures.Image;
import com.qianseit.westore.textures.LensFlareFilter;
import com.qianseit.westore.textures.PosterizeFilter;
import com.qianseit.westore.textures.SepiaFilter;
import com.qianseit.westore.textures.VintageFilter;
import com.qianseit.westore.util.ChooseUtils;
import android.graphics.PorterDuff.Mode;

public class PreviewActivitys extends Activity {
	/** 滤镜图片 */
	private ImageView mImageView;
	/** 需要处理的bitmap */
	private Bitmap mBitmap;
	/**
	 * 图片加载器
	 */
	private ImageLoader imageloader; // 使用imageLoader加载图片
	/**
	 * DisplayImageOptions设置图片属性
	 */
	private DisplayImageOptions options; //

	private MyClick click;
	private ImageButton sImagebt;
	private ImageButton sBack;
	private Bitmap bits;
	private File mFile;
	private ChooseUtils chooseInfo;
	/**
	 * 上下文
	 */
	private PreviewActivitys mContext;
	/**
	 * 滤化的提示
	 */
	private TextView mTextView;
	/**
	 * 滤镜效果容器
	 */
	private LinearLayout mLinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.preview);
		mContext = this;
		click = new MyClick();
		imageloader = ImageLoader.getInstance();
		imageloader.init(ImageLoaderConfiguration.createDefault(this));
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.ic_launcher)
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565) // 设置图片的解码类型
				.build();
		chooseInfo = (ChooseUtils) this.getIntent().getSerializableExtra(
				getString(R.string.intent_key_chooses));
		sImagebt = (ImageButton) this.findViewById(R.id.sure);
		sBack = (ImageButton) this.findViewById(R.id.action_bar_titlebar_lefts);
		mImageView = (ImageView) this.findViewById(R.id.imgfilter);
		mTextView = (TextView) findViewById(R.id.runtime);
		sBack.setOnClickListener(click);
		sImagebt.setOnClickListener(click);
		if (this.getIntent().getExtras() != null) {
			initFiter();
			String path = this.getIntent().getStringExtra("bitmap");
			mFile = new File(path);
			if (mFile.exists()) {
				// 将图片显示到ImageView中
				BitmapFactory.Options bfOptions = new BitmapFactory.Options();
				bfOptions.inDither = false; // Disable Dithering mode
				bfOptions.inPurgeable = true; // Tell to gc that whether it
												// needs free memory, the Bitmap
												// can be cleared
				bfOptions.inInputShareable = true; // Which kind of reference
													// will be used to recover
													// the Bitmap data after
													// being clear, when it will
													// be used in the future
				bfOptions.inTempStorage = new byte[512];

				File file = new File(path);
				if (!file.exists())
					return;
				FileInputStream fs = null;
				try {
					fs = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				Bitmap bmp = null;
				if (fs != null)
					try {
						mBitmap = BitmapFactory.decodeFileDescriptor(
								fs.getFD(), null, bfOptions);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (fs != null) {
							try {
								fs.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

				mImageView.setImageBitmap(mBitmap);
				bits = mBitmap;
				initFilterView();
			} else {
				Toast.makeText(mContext, "获取图片失败", 1).show();
			}
		}

	}

	/**
	 * 初始化底部滤镜效果图
	 */
	private void initFilterView() {
		mLinearLayout = (LinearLayout) findViewById(R.id.main_ll);

		for (int i = 0; i < filterArray.size(); i++) {
			final FilterInfo filterInfo = filterArray.get(i);
			final View convertView = LayoutInflater.from(this).inflate(
					R.layout.g_item, null);
			ImageView iv = (ImageView) convertView
					.findViewById(R.id.g_imageview);
			TextView tv = (TextView) convertView.findViewById(R.id.g_textview);
			tv.setText(filterInfo.name);
			convertView.setTag(filterInfo);
			new processImageTask(mContext, (IImageFilter) filterInfo.filter,
					iv, true).execute();

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					FilterInfo filterInfo = (FilterInfo) convertView.getTag();
					new processImageTask(mContext,
							(IImageFilter) filterInfo.filter, mImageView, false)
							.execute();
				}
			});
			mLinearLayout.addView(convertView);
		}

	}

	public class MyClick implements OnClickListener {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.sure:
				if (bits != null) {
					@SuppressWarnings("static-access")
					String paths = FileConfig.PATH_BASE
							+ new DateFormat().format("yyyyMMdd_hhmmss",
									Calendar.getInstance(Locale.CHINA))
							+ ".jpg";
					boolean b = saveBitmapToFile(paths, bits);
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					intent.setClass(getApplicationContext(),
							LableActivity.class);
					bundle.putSerializable(
							getString(R.string.intent_key_chooses), chooseInfo);
					intent.putExtra("bitmap", paths);
					intent.putExtras(bundle);
					startActivity(intent);
					PreviewActivitys.this.finish();

				}
				break;
			case R.id.action_bar_titlebar_lefts:
				PreviewActivitys.this.finish();
				break;
			}
		}
	}

	/**
	 * 保存图片
	 * 
	 * @param saveParentPath
	 *            图片保存路径
	 * @param bitmap
	 *            位图对象
	 */
	public boolean saveBitmapToFile(String saveParentPath, Bitmap bitmap) {

		try {
			File saveimg = new File(saveParentPath);
			if (!saveimg.exists())
				saveimg.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(saveimg));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			return true;
		} catch (IOException e) {
			Log.i("tentinet", e.toString());

			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取圆角位图的方法
	 * 
	 * @param bitmap
	 *            需要转化成圆角的位图
	 * @param pixels
	 *            圆角的度数，数值越大，圆角越大
	 * @return 处理后的圆角位图
	 */
	public Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 正方形
	 * 
	 * @param bmp
	 * @return
	 */
	public Bitmap cutBitmap(Bitmap bmp) {
		Bitmap result;
		int w = bmp.getWidth();// 输入长方形宽
		int h = bmp.getHeight();// 输入长方形高
		int nw;// 输出正方形宽
		if (w > h) {
			// 宽大于高
			nw = h;
			result = Bitmap.createBitmap(bmp, (w - nw) / 2, 0, nw, nw);
		} else {
			// 高大于宽
			nw = w;
			result = Bitmap.createBitmap(bmp, 0, (h - nw) / 2, nw, nw);
		}
		return result;
	}

	/***
	 * 添加底部样式
	 */
	private void initFiter() {
		filterArray.add(new FilterInfo("LOVE", new BrightContrastFilter()));

		filterArray.add(new FilterInfo("光晕", new HslModifyFilter(20f)));
		filterArray.add(new FilterInfo("夜色", new HslModifyFilter(40f)));
		filterArray.add(new FilterInfo("夜色", new HslModifyFilter(60f)));
		filterArray.add(new FilterInfo("梦幻", new HslModifyFilter(250f)));
		filterArray.add(new FilterInfo("酒红", new HslModifyFilter(300f)));
		filterArray.add(new FilterInfo("青柠", new HslModifyFilter(100f)));
		filterArray.add(new FilterInfo("蓝调", new ColorToneFilter(0x00FFFF, 192)));
		filterArray.add(new FilterInfo("浪漫", new LensFlareFilter()));
		filterArray.add(new FilterInfo("瑞色", new PosterizeFilter(2)));
		filterArray.add(new FilterInfo("哥特", new VintageFilter()));
		filterArray.add(new FilterInfo("黑白", new BlackWhiteFilter()));
		filterArray.add(new FilterInfo("复古", new SepiaFilter()));
	}

	/**
	 * 保存滤镜样式集合
	 * 
	 */
	private List<FilterInfo> filterArray = new ArrayList<FilterInfo>();

	/**
	 * 
	 * 单个的滤镜样式对象
	 * 
	 */
	private class FilterInfo {
		/**
		 * 样式名称
		 */
		public String name;
		/**
		 * 滤镜对象
		 */
		public IImageFilter filter;

		public FilterInfo(String name, IImageFilter filter) {
			this.name = name;
			this.filter = filter;
		}
	}

	/**
	 * 加载滤镜效果类
	 * 
	 * 
	 */
	public class processImageTask extends AsyncTask<Void, Void, Bitmap> {
		private IImageFilter filter;
		private Activity activity = null;
		private boolean isRound = false;
		ImageView iv;

		/**
		 * 
		 * @param activity
		 *            上下文
		 * @param imageFilter
		 *            过滤器
		 * @param iv
		 *            展示的视图
		 * @param isRound
		 *            是否显示圆形
		 */
		public processImageTask(Activity activity, IImageFilter imageFilter,
				ImageView iv, boolean isRound) {
			this.filter = imageFilter;
			this.activity = activity;
			this.iv = iv;
			this.isRound = isRound;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			mTextView.setVisibility(View.VISIBLE);
		}

		public Bitmap doInBackground(Void... params) {
			Image img = null;
			try {

				img = new Image(mBitmap);
				if (filter != null) {
					img = filter.process(img);
					img.copyPixelsFromBuffer();
				}
				return img.getImage();
			} catch (Exception e) {
				if (img != null && img.destImage.isRecycled()) {
					img.destImage.recycle();
					img.destImage = null;
					System.gc(); // 提醒系统及时回收
				}
			} finally {
				if (img != null && img.image.isRecycled()) {
					img.image.recycle();
					img.image = null;
					System.gc(); // 提醒系统及时回收
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				super.onPostExecute(result);
				if (isRound) {
					result = toRoundCorner(result, 20);
				} else {
					bits = result;
				}
				iv.setImageBitmap(result);
			}
			mTextView.setVisibility(View.GONE);
		}
	}
}
