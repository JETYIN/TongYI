package com.qianseit.westore.activity.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleAnimListener;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.wheelview.WheelAdapter;
import com.qianseit.westore.ui.wheelview.WheelView;
import cn.shopex.ecstore.R;

public class ApplyforChargeBackFragment extends BaseDoFragment {

	private TextView mChargeBackReason;
	private TextView mChargeBackMoney;
	private TextView mCompleteBtn;
	private TextView mCancelBtn;
	private TextView mReasonTitleView;
	private TextView mMoneyTitleView;

	private EditText mChargeBackExplain;

	private Button mSubmitBtn;
	private WheelView mWheelView;
	private View mSelectView;
	private List<String> mReasonList;
	private JSONArray mItemsList;
	private String mType;

	private View viewTemp;

	private List<String> mList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("退款申请");
		Bundle b = getArguments();
		if (b != null) {
			String mReasons = b.getString(Run.EXTRA_DATA);
			String mItems = b.getString(Run.EXTRA_VALUE);
			mType = b.getString(Run.EXTRA_VITUAL_CATE);
			try {
				JSONArray all = new JSONArray(mReasons);
				for (int i = 0; i < all.length(); i++) {
					mList.add(all.getString(i));
				}
				mItemsList = new JSONArray(mItems);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragmet_chargeback, null);
		mChargeBackReason = (TextView) findViewById(R.id.fragment_changeback_reason_content);
		mChargeBackMoney = (TextView) findViewById(R.id.fragment_changeback_money_content);
		mCompleteBtn = (TextView) findViewById(R.id.fragment_changeback_comfirm);
		mCancelBtn = (TextView) findViewById(R.id.fragment_changeback_cancel);
		mReasonTitleView = (TextView) findViewById(R.id.fragment_changeback_reason_title);
		mReasonTitleView.setText(Html
				.fromHtml(getString(R.string.exchange_reason)));
		mMoneyTitleView = (TextView) findViewById(R.id.fragment_changeback_money_title);
		mMoneyTitleView.setText(Html
				.fromHtml(getString(R.string.exchange_money)));

		mChargeBackExplain = (EditText) findViewById(R.id.fragment_changeback_explain_content);
		mSubmitBtn = (Button) findViewById(R.id.fragment_changeback_submit);
		mSelectView = findViewById(R.id.fragment_changeback_select_layout);

		mChargeBackExplain.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mSelectView.getVisibility() == View.VISIBLE) {
					dimissSelectView();
				}
				return false;
			}
		});
		mChargeBackReason.setOnClickListener(this);
		mChargeBackMoney.setOnClickListener(this);
		mCompleteBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		mSubmitBtn.setOnClickListener(this);

		mWheelView = (WheelView) findViewById(R.id.fragment_changeback_wheelview);

		mWheelView.setAdapter(mAdapter);
		// mWheelView.setCyclic(true);
		mWheelView.setVisibleItems(7);
	}

	@Override
	public void onClick(View v) {
		if (v == mChargeBackReason) {
			viewTemp = mChargeBackReason;
			Run.hideSoftInputMethod(mActivity, v);
			setShowSelectView();
		} else if (v == mChargeBackMoney) {
			viewTemp = mChargeBackMoney;
			Run.hideSoftInputMethod(mActivity, v);
			setShowSelectView();
		} else if (v == mCompleteBtn) {
			if (viewTemp == mChargeBackReason) {
				mChargeBackReason
						.setText(mList.get(mWheelView.getCurrentItem()));
			} else if (viewTemp == mChargeBackMoney) {
				mChargeBackMoney
						.setText(mList.get(mWheelView.getCurrentItem()));
			}
			dimissSelectView();
		} else if (v == mSubmitBtn) {
			if (!TextUtils.isEmpty(mChargeBackReason.getText().toString())) {
				new JsonTask().execute(new SaveRequest());
			} else {
				Run.alert(mActivity, mActivity
						.getString(R.string.exchange_please_select_reason));
			}
		} else if(v == mCancelBtn){
			dimissSelectView();
		} else {
			super.onClick(v);
		}
	}

	private void setShowSelectView() {
		if (mSelectView.getVisibility() == View.VISIBLE) {
			return;
		}
		mSelectView.setVisibility(View.VISIBLE);
		mSelectView.startAnimation(AnimationUtils.loadAnimation(mActivity,
				R.anim.push_up_in));
	}

	private void dimissSelectView() {
		Animation animOut = AnimationUtils.loadAnimation(mActivity,
				R.anim.push_down_out);
		animOut.setAnimationListener(new SimpleAnimListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mSelectView.setVisibility(View.GONE);
			}
		});
		mSelectView.startAnimation(animOut);
	}

	private WheelAdapter mAdapter = new WheelAdapter() {

		@Override
		public int getMaximumLength() {
			return mList.size();
		}

		@Override
		public int getItemsCount() {
			return mList.size();
		}

		@Override
		public String getItem(int index) {
			return mList.get(index);
		}
	};

	private class SaveRequest implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					setState(true);
				} else {
					setState(false);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.aftersales.return_save");
			String order_id = "";
			try {
				order_id = mItemsList.getJSONObject(0).optString("order_id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			req.addParams("order_id", order_id); // 订单号 String
			for (int i = 0; i < mItemsList.length(); i++) {
				JSONObject obj = mItemsList.optJSONObject(i);
				String product_id = obj.optJSONObject("products").optString(
						"product_id");
				req.addParams("product_bn[" + product_id + "]",
						obj.optString("bn")); // 货品号 Aarry
				req.addParams("product_nums[" + product_id + "]",
						obj.optString("quantity")); // 发货数量
				// Aarry
				req.addParams("product_name[" + product_id + "]",
						obj.optString("name")); // 货品名称 Aarry
				req.addParams("product_price[" + product_id + "]",
						obj.optString("price")); // 货品价格
			}
			// req.addParams("product_bn", getArrayString("bn")); // 货品号 Aarry
			// req.addParams("product_nums", getArrayString("quantity")); //
			// 发货数量
			// Aarry
			// req.addParams("product_name", getArrayString("name")); // 货品名称
			// Aarry
			// req.addParams("product_price", getArrayString("price")); // 货品价格
			// Aarry
			req.addParams("type", mType); // 售后服务类型 1:退货;2:换货; int
			req.addParams("title", mChargeBackReason.getText().toString()); // 退换理由
																			// String
			req.addParams("content", mChargeBackExplain.getText().toString()); // 详细描述
																				// String
			req.addParams("agree", "true"); // (是否同意售后服务 agree=on) boolean
			return req;
		}

	}

	private String getArrayString(String keyword) {
		if (mItemsList == null) {
			return "";
		}
		JSONArray list = new JSONArray();
		for (int i = 0; i < mItemsList.length(); i++) {
			try {
				String value = mItemsList.getJSONObject(i).optString(keyword);
				list.put(value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list.toString();
	}

	private void setState(boolean isSucceed) {
		mActivity.setResult(Activity.RESULT_OK, null);
		findViewById(R.id.fragment_changeback_state)
				.setVisibility(View.VISIBLE);
		if (isSucceed) {
			((ImageView) findViewById(R.id.fragment_changeback_state_icon))
					.setImageResource(R.drawable.pay_success_face);
			if (mType.equals("1")) {
				((TextView) findViewById(R.id.fragment_changeback_state_text))
						.setText("退货申请已提交！");
				findViewById(R.id.fragment_changeback_tips).setVisibility(
						View.INVISIBLE);
			} else {
				((TextView) findViewById(R.id.fragment_changeback_state_text))
						.setText("换货申请已提交！");
				((TextView) findViewById(R.id.fragment_changeback_tips1))
						.setVisibility(View.INVISIBLE);
				((TextView) findViewById(R.id.fragment_changeback_tips2))
						.setVisibility(View.INVISIBLE);
			}
		} else {
			((ImageView) findViewById(R.id.fragment_changeback_state_icon))
					.setImageResource(R.drawable.pay_failed_face);
			if (mType.equals("1")) {
				((TextView) findViewById(R.id.fragment_changeback_state_text))
						.setText("退货申请失败！");
				findViewById(R.id.fragment_changeback_tips).setVisibility(
						View.INVISIBLE);
			} else {
				((TextView) findViewById(R.id.fragment_changeback_state_text))
						.setText("换货申请已提交！");
				((TextView) findViewById(R.id.fragment_changeback_tips1))
						.setVisibility(View.INVISIBLE);
				((TextView) findViewById(R.id.fragment_changeback_tips2))
						.setVisibility(View.INVISIBLE);
			}
		}
	}
}
