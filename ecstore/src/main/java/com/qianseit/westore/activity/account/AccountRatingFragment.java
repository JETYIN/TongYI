package com.qianseit.westore.activity.account;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountRatingFragment extends BaseDoFragment {

	private EditText mEditText;
	private String mOrderId;
	private String mMemberId;
	private String mGoodsId;
	private LoginedUser mLoginedUser;
	private JSONObject goodsJson;
	private TextView mTextViewNum;
	private int contentNum = 140;
	private VolleyImageLoader mVolleyImageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.orders_goods_rating);
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		Intent intent = mActivity.getIntent();
		String data = intent.getStringExtra(Run.EXTRA_DATA);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		try {
			goodsJson = new JSONObject(data);
			if (goodsJson != null) {
				mGoodsId = goodsJson.optString("goods_id");
			}
		} catch (JSONException e) {

			e.printStackTrace();
		}
		mMemberId = mLoginedUser.getMemberId();
		mOrderId = intent.getStringExtra(Run.EXTRA_ADDR);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_orders_rating_main, null);
		findViewById(R.id.account_rating_submit).setOnClickListener(this);
		mEditText = (EditText) findViewById(R.id.account_rating_content);
		ImageView imageViewIcon = (ImageView) findViewById(R.id.account_rating_goods_icon);
		mVolleyImageLoader.showImage(imageViewIcon,
				goodsJson.optString("thumbnail_pic_src"));
		((TextView) findViewById(R.id.account_rating_goods_title))
				.setText(goodsJson.optString("name"));
		((TextView) findViewById(R.id.account_rating_goods_price)).setText("￥"
				+ goodsJson.optString("price"));
		mTextViewNum = (TextView) findViewById(R.id.account_rating_num);
		// TextView
		// markText=(TextView)findViewById(R.id.account_rating_goods_market);
		// markText.setText(goodsJson.optString("price"));
		// markText.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				    String content = mEditText.getText().toString();  
			        int ab=140;
			        int num=ab-content.length();
			        if(num<0){
			        	num=0;
			        	mEditText.setText(content.subSequence(0, ab));
			        }
			        
			        mTextViewNum.setText(String.valueOf(num)); 

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.account_rating_submit:
			String strContent = mEditText.getText().toString().trim();
			if (!TextUtils.isEmpty(strContent)) {
				Run.excuteJsonTask(new JsonTask(), new SubMitRatingTask(
						strContent));
			}
			break;
		default:
			break;
		}
		super.onClick(v);
	}

	private class SubMitRatingTask implements JsonTaskHandler {
		private String content;

		public SubMitRatingTask(String content) {
			this.content = content;
		}

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					if (all.optBoolean("data")) {
						Run.alert(mActivity, "评论成功");
						mActivity.setResult(Activity.RESULT_OK);
						mActivity.finish();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.goods.add_comment");
			req.addParams("member_id", mMemberId);
			req.addParams("goods_id", mGoodsId);
			req.addParams("content", content);
			req.addParams("order_id", mOrderId);
			return req;
		}
	}
}
