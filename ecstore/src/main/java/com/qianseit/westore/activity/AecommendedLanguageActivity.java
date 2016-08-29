package com.qianseit.westore.activity;

import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.wechat.moments.WechatMoments;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.RoundAngleImageView;
import com.qianseit.westore.util.ChooseUtils;


/**
 * 商品推荐发布界面
 */
public class AecommendedLanguageActivity extends BaseDoFragment {
	private RoundAngleImageView aecomend;
	private Bitmap sBitmaps;
	private EditText text_edit;
	private TextView textview_show;
	private Button button_show;
	private ImageButton imageButton;
	private String numInfo;
	private ImageView selectsweiboButton;
	private ImageView selectsweixingButton;
	private boolean flag; // true 微博 false 微信
	private MyClick click;
	private JsonTask mTask;
	private String info;
	private String meMberid; // 用户ID、
	private ShareViewDataSource mDataSource;
	private ChooseUtils chooseInfo;
	File file = null;
	private String path;
	private LoginedUser mLoginedUser;
	private String datas;
	private String res;
	private String clicks = "2";
	private String weixinclicks = "2";
	private String imagePath;
	private File files[] = new File[1];
	private String directions;
	private String xposition;
	private String yposition;
	private JSONObject dataJsonArray;
	private String flagsas; // true 请求成功 false失败

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.tabbar_aecommend);
	}

	public void onResume() {
		super.onResume();
		;
	}
	
	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.aecommended_activity, null);
		chooseInfo = (ChooseUtils) mActivity.getIntent().getSerializableExtra(
				getString(R.string.intent_key_chooses));

		directions = mActivity.getIntent().getStringExtra("directions");
		xposition = mActivity.getIntent().getStringExtra("xposition");
		yposition = mActivity.getIntent().getStringExtra("yposition");

		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		meMberid = mLoginedUser.getMemberId();
		click = new MyClick();
		aecomend = (RoundAngleImageView) rootView
				.findViewById(R.id.fragment_uploading_front);
		text_edit = (EditText) rootView.findViewById(R.id.text_edit);
		textview_show = (TextView) rootView.findViewById(R.id.textview_show);
		button_show = (Button) rootView.findViewById(R.id.aecomend_sures);
		// imageButton = (ImageButton)
		// rootView.findViewById(R.id.action_bar_leftss);
		selectsweiboButton = (ImageView) rootView
				.findViewById(R.id.main_top_adsview_foot_season);
		selectsweixingButton = (ImageView) rootView
				.findViewById(R.id.main_top_adsview_foot_flash_Sale);
		button_show.setOnClickListener(click);
		// imageButton.setOnClickListener(click);
		selectsweiboButton.setOnClickListener(click);
		selectsweixingButton.setOnClickListener(click);

		if (mActivity.getIntent().getExtras() != null) {
			path = mActivity.getIntent().getStringExtra("bitmap");
			imagePath = mActivity.getIntent().getStringExtra("imagePath");
			File imageFile = new File(imagePath);

			Bitmap bitmap = BitmapFactory.decodeFile(imageFile
					.getAbsolutePath());

			File pathfile = new File(Run.doCacheFolder, "file");
			if (!pathfile.getParentFile().exists())
				pathfile.getParentFile().mkdirs();
			try {
				FileOutputStream fos = new FileOutputStream(pathfile);
				bitmap.compress(CompressFormat.JPEG, 60, fos);
				fos.flush();
				fos.close();
				files[0] = pathfile;
			} catch (Exception e) {
				e.printStackTrace();
			}
			File file = new File(path);
			if (file.exists()) {
				// 将图片显示到ImageView中
				sBitmaps = BitmapFactory.decodeFile(path);
				// sBitmaps =cutBitmap(BitmapFactory.decodeFile(path));
				aecomend.setImageBitmap(sBitmaps);
			}
		}
		// 焦点监听事件
		text_edit.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				// EditText et=(EditText)v;
				// if (!hasFocus) {// 失去焦点
				// et.setHint(et.getTag().toString());
				// } else {
				// String hint=et.getHint().toString();
				// et.setTag(hint);//保存预设字
				// et.setHint(null);
				// }
			}
		});

		text_edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String content = text_edit.getText().toString();
				int ab = 140;
				int num = ab - content.length();
				if (num < 0) {
					num = 0;
					text_edit.setText(content.subSequence(0, ab));
				}
				textview_show.setText(String.valueOf(num));
			}

		});


	}

	public class MyClick implements OnClickListener {

		@Override
		public void onClick(View view) {
			// 暂时移到这里，开放分享图片后移到最前面

			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;

			mTask = new JsonTask();
			switch (view.getId()) {
			case R.id.aecomend_sures:

				numInfo = text_edit.getText().toString();
				if (TextUtils.isEmpty(numInfo.trim()) || numInfo.length() < 1) {
					AccountLoginFragment.showAlertDialog(mActivity, "请填写推荐语", "", "确定", null, null, false, null);
					return ;
				}
				Run.excuteJsonTask(mTask, new UploadListData());
				// Platform platform = null;
				// platform = ShareSDK.getPlatform(mActivity,
				// WechatMoments.NAME);
				//
				// if(flagsas.equals("1")){
				// hideLoadingDialog();
				// Toast.makeText(mActivity, "发布成功", 5000).show();
				// mActivity.finish();
				// if(clicks.equals("2")){
				// platform = ShareSDK.getPlatform(mActivity, SinaWeibo.NAME);
				// platform.SSOSetting(true);
				// SinaWeibo.ShareParams paramss = new SinaWeibo.ShareParams();
				// if (file != null)
				// paramss.setImagePath(path);
				// paramss.setText("");
				// platform.share(paramss);
				// }
				// if(weixinclicks.equals("2")){
				// platform = ShareSDK.getPlatform(mActivity,
				// WechatMoments.NAME);
				// WechatMoments.ShareParams params = new
				// WechatMoments.ShareParams();
				// params.setImagePath(path);
				// params.setTitle("哈哈");
				// params.setText(numInfo);
				// platform.share(params);
				// }

				// }else{
				// hideLoadingDialog();
				// Toast.makeText(mActivity, "文件获取失败", 5000).show();
				// }
				//
				break;
			// case R.id.action_bar_leftss:
			// mActivity.finish();
			// break;
			case R.id.main_top_adsview_foot_season: // 微博
				if ("1".equals(clicks)) {
					selectsweiboButton.setImageResource(R.drawable.showweibos);
					clicks = "2";
				} else {
					selectsweiboButton
							.setImageResource(R.drawable.about_weinos);
					clicks = "1";
				}
				flag = true;
				break;
			case R.id.main_top_adsview_foot_flash_Sale: // 微信
				if ("1".equals(weixinclicks)) {
					selectsweixingButton
							.setImageResource(R.drawable.show_weixings);
					weixinclicks = "2";
				} else {
					selectsweixingButton
							.setImageResource(R.drawable.about_weobo);
					weixinclicks = "1";
				}
				flag = false;
				break;
			}
		}

	}

	//发布推荐商品
	private class UploadListData implements JsonTaskHandler {
		// private JSONObject data;
		// private int newQuantity = 1;

		@Override
		public void task_response(String json_str) {
			// JSONObject dataJson;
			hideLoadingDialog_mt();
			json_str.length();
			Platform platform = null;
			numInfo = text_edit.getText().toString();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {

					platform = ShareSDK.getPlatform(mActivity,
							WechatMoments.NAME);
					if (json_str.length() < 80) {
						Toast.makeText(mActivity, "文件获取失败", 5000).show();
					} else {
						Toast.makeText(mActivity, "发布成功", 5000).show();
						String url = all.optJSONObject("data").optString("fx");
						String content = all.optJSONObject("data").optString("content");
//						String goodsName = all.optJSONObject("data").optString("goods_name");
//						String brand_name = all.optJSONObject("data").optString("brand_name");
						if (clicks.equals("2")) {
							platform = ShareSDK.getPlatform(mActivity,
									SinaWeibo.NAME);
							platform.SSOSetting(true);
							SinaWeibo.ShareParams paramss = new SinaWeibo.ShareParams();
							if (file != null)
								paramss.setImagePath(path);
							paramss.setText(content+"@樱淘社"+url);
							paramss.setUrl(url);
							platform.share(paramss);
						}
						if (weixinclicks.equals("2")) {
							platform = ShareSDK.getPlatform(mActivity,
									WechatMoments.NAME);
							WechatMoments.ShareParams params = new WechatMoments.ShareParams();
							params.setShareType(WechatMoments.SHARE_WEBPAGE);
							params.setImagePath(path);
							// params.setTitle("哈哈");
							params.setTitle(content);
							params.setUrl(url);
							platform.share(params);
						}
					}
					startActivity(AgentActivity
							.intentForFragment(mActivity,
									AgentActivity.FRAGMENT_PERSONAL_HOME)
							.putExtra(Run.EXTRA_DATA, false)
							.putExtra("userId", mLoginedUser.getMemberId()));
					getActivity().finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean jb = new JsonRequestBean(
					"mobileapi.goods.add_opinions");
			jb.addParams("member_id", meMberid); // 用户id-
			jb.addParams("goods_id", chooseInfo.getGoods_id()); // 商品id
			jb.addParams("content", numInfo); // 推荐内容
			jb.addParams("tag-1-x", xposition); // 图片标签x坐标
			jb.addParams("tag-1-y", yposition); // 图片标签y坐标
			jb.addParams("tag-1-image_type", directions); // 图片标签方向:1.标签向右,2.标签向左
			jb.addParams("tag-1-image_tag", chooseInfo.getBrand_name()); // 标签名
			jb.addParams("order_id", chooseInfo.getOrder_id()); // 订单id
			// jb.addParams("type", "avatar");
			jb.files = files; // 图片文件
			Log.i("tentinet",
					"shuju:" + meMberid + "\n" + chooseInfo.getGoods_id()
							+ "\n" + numInfo + "\n" + xposition + "\n"
							+ yposition + "\n" + directions + "\n"
							+ chooseInfo.getBrand_name() + "\n"
							+ chooseInfo.getOrder_id());
			return jb;
		}
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

	public interface ShareViewDataSource {
		public String getShareText();

		public String getShareImageFile();

		public String getShareImageUrl();

		public String getShareUrl();
	}

}
