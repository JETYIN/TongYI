package com.qianseit.westore.activity.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.alipay.client.AliPayFragment;
import com.alipay.client.PayResult;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.activity.MainTabFragmentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class AccountOrdersFragment extends AliPayFragment implements
		OnCheckedChangeListener {
	private final int REQUEST_CODE_ORDER_DETAIL = 0x1001;
	public static final String PAY_SUCCEE = "PAY_SUCCEE";

	private ListView mListView;
	private RadioButton mLatestMonthRadio, mEarlierMonthRadio;
	private RadioButton mAllOrder , mNeedPayOrder , mNeedShipingOrder , mHadShipingOrder , mNeedRecomandOrder;
	private ArrayList<JSONObject> mOrdersList = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mOrderGoods = new ArrayList<JSONObject>();

	// private ImageLoader mImageLoader;
	private VolleyImageLoader mVolleyImageLoader;
	private String mFilterCreateTime = "recent";
	private String mPayStatus = "0";
	private String mPayStatusKey = "pay_status";
	private String mPayStatusTitle = Run.EMPTY_STR;
	private boolean mShowAllOrders = false;
	private int pageNum = 0;

	private JSONObject mPayingOrder;
	private boolean mDataLoading = false;
	private boolean mDataLoadEnded = false;

	private int addSucceedCount;
	private int addFailCount;

	public AccountOrdersFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_orders_title);
		// mImageLoader = Run.getDefaultImageLoader(mActivity,
		// mActivity.getResources());

		Intent data = mActivity.getIntent();
		mShowAllOrders = data.getIntExtra(Run.EXTRA_VALUE, 0) == 0;
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_account_orders, null);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(new OrdersAdapter());

		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5 || mDataLoadEnded)
					return;

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(pageNum);
			}
		});

		mLatestMonthRadio = (RadioButton) findViewById(R.id.account_orders_latest_month);
		mEarlierMonthRadio = (RadioButton) findViewById(R.id.account_orders_earlier_month);
		mAllOrder = (RadioButton) findViewById(R.id.account_orders_all);
		mNeedPayOrder = (RadioButton) findViewById(R.id.account_orders_needpay);
		mNeedShipingOrder = (RadioButton) findViewById(R.id.account_orders_needshipping);
		mHadShipingOrder = (RadioButton) findViewById(R.id.account_orders_hadshipping);
		mNeedRecomandOrder = (RadioButton) findViewById(R.id.account_orders_needrecommend);
		mAllOrder.setOnCheckedChangeListener(this);
		mNeedPayOrder.setOnCheckedChangeListener(this);
		mNeedShipingOrder.setOnCheckedChangeListener(this);
		mHadShipingOrder.setOnCheckedChangeListener(this);
		mNeedRecomandOrder.setOnCheckedChangeListener(this);
		mLatestMonthRadio.setOnCheckedChangeListener(this);
		mEarlierMonthRadio.setOnCheckedChangeListener(this);
		// 选中默认的分类
		Intent data = mActivity.getIntent();
		int mStatusBox = data.getIntExtra(Run.EXTRA_VALUE, -1);
		if (mStatusBox == R.id.account_orders_paying) {
//			this.mPayStatus = "0";
//			this.mPayStatusKey = "pay_status";
			mNeedPayOrder.setChecked(true);
		} else if (mStatusBox == R.id.account_orders_shipping) {
//			this.mPayStatus = "0";
//			this.mPayStatusKey = "ship_status";
			mNeedShipingOrder.setChecked(true);
		} else if (mStatusBox == R.id.account_orders_receiving) {
//			this.mPayStatus = "1";
//			this.mPayStatusKey = "ship_status";
			mHadShipingOrder.setChecked(true);
		} else if (mStatusBox == R.id.account_orders_return) {
//			this.mPayStatus = "finish";
//			this.mPayStatusKey = "status";
			mNeedPayOrder.setChecked(true);
		} else {
			mAllOrder.setChecked(true);
		}
//		loadNextPage(pageNum);

//		IntentFilter intentFilter = new IntentFilter(PAY_SUCCEE);
//		intentFilter.setPriority(3);
//		getActivity().registerReceiver(paySucceed, intentFilter);
	};

	@Override
	public void onResume() {
		super.onResume();
		// 刷新ListView显示
		if (mListView.getAdapter() != null)
			((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void ui(int what, Message msg) {

		switch (msg.what) {
		case SDK_PAY_FLAG: {// 支付宝支付结果
			PayResult payResult = new PayResult((String) msg.obj);

			// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
			String resultInfo = payResult.getResult();

			String resultStatus = payResult.getResultStatus();

			// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
			if (TextUtils.equals(resultStatus, "9000")) {
				loadNextPage(0);
				Toast.makeText(mActivity, "支付成功", Toast.LENGTH_SHORT).show();
			} else {
				// 判断resultStatus 为非“9000”则代表可能支付失败
				// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
				if (TextUtils.equals(resultStatus, "8000")) {
					Toast.makeText(mActivity, "支付结果确认中", Toast.LENGTH_SHORT)
							.show();

				} else {
					// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
					Toast.makeText(mActivity, "支付失败", Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;

		}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
//			getActivity().unregisterReceiver(paySucceed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		if (!isChecked)
			return;

		this.updateOrdersFilter(v);
//		this.loadNextPage(0);
	}

	// 更新订单过滤属性
	private void updateOrdersFilter(CompoundButton v) {
		this.mDataLoadEnded = false;
//		if (mLatestMonthRadio == v)
//			mFilterCreateTime = "recent";
//		else if (mEarlierMonthRadio == v)
//			mFilterCreateTime = "prior_to";
		if (mLatestMonthRadio == v) {
			mFilterCreateTime = "recent";
		} else if (mEarlierMonthRadio == v) {
			mFilterCreateTime = "prior_to";
		} else if (mAllOrder == v) {
			mShowAllOrders = true;
		} else if (mNeedPayOrder == v) {
			mShowAllOrders = false;
			this.mPayStatus = "0";
			this.mPayStatusKey = "pay_status";
		} else if (mNeedShipingOrder == v) {
			mShowAllOrders = false;
			this.mPayStatus = "0";
			this.mPayStatusKey = "ship_status";
		} else if (mHadShipingOrder == v) {
			mShowAllOrders = false;
			this.mPayStatus = "1";
			this.mPayStatusKey = "ship_status";
		} else if (mNeedRecomandOrder == v) {
			mShowAllOrders = false;
			this.mPayStatus = "finish";
			this.mPayStatusKey = "status";
			return;
		}
		loadNextPage(0);
		

		// this.mPayStatusTitle = v.getText().toString();
		// if (v.getId() == R.id.account_orders_latest_month) {
		// this.mPayStatus = "0";
		// this.mPayStatusKey = "pay_status";
		// } else if (v.getId() == R.id.account_orders_earlier_month) {
		// this.mPayStatus = "0";
		// this.mPayStatusKey = "ship_status";
		// }
	}

	// 加载下一页
	private void loadNextPage(int pagenum) {
		if (mDataLoading || mDataLoadEnded)
			return;

		this.pageNum = pagenum + 1;
		if (this.pageNum == 1) {
			mOrdersList.clear();
			ListAdapter adapter = mListView.getAdapter();
			((BaseAdapter) adapter).notifyDataSetChanged();
		}

		Run.excuteJsonTask(new JsonTask(), new GetOrdersTask());
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
		} else {
			super.onClick(v);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_USER_LOGIN
				&& resultCode != Activity.RESULT_OK) {
			mActivity.finish();
		} else if (requestCode == REQUEST_CODE_ORDER_DETAIL
				&& resultCode == Activity.RESULT_OK) {
			mOrdersList.remove(AgentApplication.getApp(mActivity).mOrderDetail);
			((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private class GetOrdersTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			if (pageNum == 1)
				showCancelableLoadingDialog();
			mDataLoading = true;

			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.orders");
			if (!mShowAllOrders && !TextUtils.isEmpty(mPayStatus)
					&& !TextUtils.isEmpty(mPayStatusKey)) {
				bean.addParams(mPayStatusKey, mPayStatus);
				// if ("ship_status".equals(mPayStatusKey))
				// bean.addParams("pay_status", "1");
				if (!TextUtils.equals(mPayStatusKey, "status")) {
					bean.addParams("status", "active");
				}
			}
			bean.addParams("n_page", String.valueOf(pageNum));
			bean.addParams("createtime_status", mFilterCreateTime);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			mDataLoading = false;

			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray array = all.optJSONArray("data");
					// 已加载到最后一页
					if (array.length() == 0)
						mDataLoadEnded = true;
					// 添加数据并刷新列表
					for (int i = 0, count = array.length(); i < count; i++)
						mOrdersList.add(array.getJSONObject(i));
					((BaseAdapter) mListView.getAdapter())
							.notifyDataSetChanged();
				}
			} catch (Exception e) {
			}
		}
	}

	private class OrdersAdapter extends BaseAdapter implements OnClickListener {
		private Resources res = null;

		public OrdersAdapter() {
			res = mActivity.getResources();
		}

		@Override
		public int getCount() {
			return mOrdersList.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mOrdersList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_account_orders_item, null);
				convertView.setOnClickListener(this);
				convertView.findViewById(R.id.account_orders_gallery)
						.setVisibility(View.VISIBLE);
			}

			JSONObject all = getItem(position);
			((TextView) convertView.findViewById(R.id.account_orders_item_no))
					.setText(all.optString("order_id"));
			((TextView) convertView
					.findViewById(R.id.account_orders_item_state))
					.setText(mPayStatusTitle);
			convertView.setTag(all);

			// 订单状态
			TextView textState = (TextView) convertView
					.findViewById(R.id.account_orders_item_state);
			TextView sendType = (TextView) convertView
					.findViewById(R.id.account_orders_send_type);
			TextView orderId = (TextView) convertView
					.findViewById(R.id.account_orders_id);
			orderId.setText(getString(R.string.account_orders_order_number)
					+ all.optString("order_id"));
			JSONObject expressInfo = all.optJSONObject("shipping");
			sendType.setText(mActivity.getString(
					R.string.account_orders_send_type,
					expressInfo.optString("shipping_name")));
			View buttonPay = convertView
					.findViewById(R.id.account_orders_item_pay);
			View buttonDelete = convertView
					.findViewById(R.id.account_orders_item_delete);
			View buttonCancel = convertView
					.findViewById(R.id.account_orders_item_cancel);
			View buttonRateAgain = convertView
					.findViewById(R.id.account_orders_item_rate_again);
			// View buttonBuyAgain = convertView
			// .findViewById(R.id.order_list_buy_again);
			buttonPay.setOnClickListener(this);
			buttonDelete.setOnClickListener(this);
			buttonCancel.setOnClickListener(this);
			buttonRateAgain.setOnClickListener(this);
			// buttonBuyAgain.setOnClickListener(this);
			buttonPay.setTag(all);
			buttonCancel.setTag(all);
			buttonDelete.setTag(all);
			buttonRateAgain.setTag(all);
			// buttonBuyAgain.setTag(all);
			if ("dead".equalsIgnoreCase(all.optString("status"))) {
				textState.setText(R.string.account_orders_state_cancel);
				buttonDelete.setVisibility(View.GONE);
				buttonRateAgain.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else if ("finish".equalsIgnoreCase(all.optString("status"))) {
				textState.setText(R.string.account_orders_state_complete);
				buttonRateAgain.setVisibility(View.VISIBLE);// 根本没有追加评论的接口
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else if ("1".equalsIgnoreCase(all
					.optString("delivery_sign_status"))) {
				textState.setText(R.string.account_orders_state_tuotou);
				buttonRateAgain.setVisibility(View.VISIBLE);// 根本没有追加评论的接口
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else if (all.optInt("ship_status") == 1) {
				textState.setText(R.string.account_orders_state_receive);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else if (all.optInt("pay_status") == 0) {
				textState.setText(R.string.account_orders_state_paying);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.GONE);
				JSONObject payinfo = all.optJSONObject("payinfo");
				if ("offlinecard".equals(payinfo.opt("pay_app_id"))) {
					buttonPay.setVisibility(View.GONE);
				} else {
					buttonPay.setVisibility(View.VISIBLE);
				}
			} else if (all.optInt("ship_status") == 0) {
				textState.setText(R.string.account_orders_state_shipping);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.VISIBLE);
				buttonPay.setVisibility(View.GONE);
			} else if (all.optInt("ship_status") == 2) {
				textState.setText(R.string.account_orders_state_part_shipping);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else if (all.optInt("ship_status") == 3) {
				textState.setText(R.string.account_orders_state_part_refund);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else if (all.optInt("ship_status") == 4) {
				textState.setText(R.string.account_orders_state_refund);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.GONE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			} else {
				textState.setText(R.string.account_orders_state_cancel);
				buttonRateAgain.setVisibility(View.GONE);
				buttonDelete.setVisibility(View.VISIBLE);
				buttonCancel.setVisibility(View.GONE);
				buttonPay.setVisibility(View.GONE);
			}

			// JSONObject payinfo = all.optJSONObject("payinfo");
			// if (payinfo != null && Run.isOfflinePayType(payinfo)) {
			// buttonCancel.setVisibility(View.GONE);
			// buttonPay.setVisibility(View.GONE);
			// } else {
			// if (all.optInt("pay_status") != 0) {
			// buttonCancel.setVisibility(View.GONE);
			// } else {
			// buttonCancel.setVisibility(View.VISIBLE);
			// }
			// }
			//
			// LinearLayout content = (LinearLayout) convertView
			// .findViewById(R.id.account_orders_content);
			// content.setTag(all);
			// content.setOnClickListener(this);
			// List<String> uriList = new ArrayList<String>();
			// JSONArray goods = all.optJSONArray("goods_items");

			// for (int i = 0; i < goods.length(); i++) {
			// try {
			// JSONObject prouct = goods.getJSONObject(i);
			// JSONObject product = prouct.optJSONObject("product");
			// uriList.add(product.optString("thumbnail_pic_src"));
			// } catch (JSONException e) {
			// e.printStackTrace();
			// }
			// }
			// GalleryAdapter adapter = new GalleryAdapter(uriList);
			// content.removeAllViews();
			// for (int i = 0; i < uriList.size(); i++) {
			// content.addView(adapter.getView(i, null, null));
			// }

			List<String> uriList = new ArrayList<String>();
			JSONArray goods = all.optJSONArray("goods_items");
			GalleryAdapter adapter = new GalleryAdapter(uriList);
			if (goods != null && goods.length() > 1) {
				convertView.findViewById(R.id.account_orders_layout_onegood)
						.setVisibility(View.GONE);
				convertView.findViewById(R.id.account_orders_gallery)
						.setVisibility(View.VISIBLE);
				LinearLayout content = (LinearLayout) convertView
						.findViewById(R.id.account_orders_content);
				content.setTag(all); // ListView Item 嵌套 HorizontalScrollView
										// 导致ListView点击事件被屏蔽，故加此点击事件
				content.setOnClickListener(this);
				for (int i = 0; i < goods.length(); i++) {
					try {
						JSONObject prouct = goods.getJSONObject(i);
						JSONObject product = prouct.optJSONObject("product");
						uriList.add(product.optString("thumbnail_pic_src"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				content.removeAllViews();
				for (int i = 0; i < uriList.size(); i++) {
					content.addView(adapter.getView(i, null, null));
				}
			} else {
				convertView.findViewById(R.id.account_orders_gallery)
						.setVisibility(View.GONE);
				convertView.findViewById(R.id.account_orders_layout_onegood)
						.setVisibility(View.VISIBLE);
				try {
					JSONObject item = goods.getJSONObject(0);
					JSONObject product = item.optJSONObject("product");
					((TextView) convertView
							.findViewById(R.id.account_orders_item_title))
							.setText(product.optString("name"));
					((TextView) convertView
							.findViewById(R.id.account_orders_item_summary))
							.setText(product.optString("attr"));
					((TextView) convertView
							.findViewById(R.id.account_orders_item_quantity))
							.setText(Run.buildString("x",
									product.optString("quantity")));

					// 缩略图
					String imageUri = product.optString("thumbnail_pic_src");
					ImageView thumbView = (ImageView) convertView
							.findViewById(R.id.account_orders_item_thumb);
					mVolleyImageLoader.showImage(thumbView, imageUri);
				} catch (Exception e) {

				}
			}

			((TextView) convertView
					.findViewById(R.id.account_orders_item_sum_quantity))
					.setText(mActivity.getString(
							R.string.account_orders_order_sum_quantity,
							goods.length()));

			((TextView) convertView.findViewById(R.id.account_orders_item_sum))
					.setText(Run.buildString("￥", all.optString("total_amount")));
			((TextView) convertView
					.findViewById(R.id.account_orders_item_price)).setText(Run
					.buildString("￥", all.optString("total_amount")));

			return convertView;
		}

		@Override
		public void onClick(View v) {
			try {
				final JSONObject order = (JSONObject) v.getTag();
				if (v.getId() == R.id.account_orders_item_pay) {
					mPayingOrder = (JSONObject) v.getTag();
					JSONObject payinfo = mPayingOrder.optJSONObject("payinfo");
					JsonRequestBean bean = new JsonRequestBean(
							"mobileapi.paycenter.dopayment")
							.addParams("payment_order_id",
									mPayingOrder.optString("order_id"))
							.addParams("payment_cur_money",
									mPayingOrder.optString("total_amount"))
							.addParams("payment_pay_app_id",
									payinfo.optString("pay_app_id"));
					Run.excuteJsonTask(new JsonTask(), new BalancePayTask(bean));
				} else if (v.getId() == R.id.account_orders_item_delete) {
					CustomDialog dialog = new CustomDialog(mActivity);
					dialog.setMessage(R.string.account_orders_delete_order_confirm);
					dialog.setNegativeButton(R.string.cancel, null);
					dialog.setPositiveButton(R.string.ok,
							new OnClickListener() {
								@Override
								public void onClick(View v) {
								}
							}).setCancelable(true).show();
				} else if (v.getId() == R.id.account_orders_item_cancel) {
					CustomDialog dialog = new CustomDialog(mActivity);
					dialog.setMessage(R.string.account_orders_cancel_order_confirm);
					dialog.setNegativeButton(R.string.cancel, null);
					dialog.setPositiveButton(R.string.ok,
							new OnClickListener() {
								@Override
								public void onClick(View v) {
									String id = order.optString("order_id");
									new JsonTask().execute(new CancelOrderTask(
											id, order));
								}
							}).setCancelable(true).show();
				} else if (v.getId() == R.id.order_list_buy_again) {
					JSONObject all = (JSONObject) v.getTag();
					JSONArray goods = all.optJSONArray("goods_items");
					for (int i = 0, c = goods.length(); i < c; i++) {
						JSONObject item = goods.getJSONObject(i);
						mOrderGoods.add(item.optJSONObject("product"));
					}
					addRebuy2ShoppingCar(0);
					showLoadingDialog();
				} else if (v.getId() == R.id.account_orders_item_rate_again) {
					startActivity(AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_ORDER_RATING));
				} else {
					AgentApplication.getApp(mActivity).setOrderDetail(order);
					startActivityForResult(AgentActivity.intentForFragment(
							mActivity, AgentActivity.FRAGMENT_ORDER_DETAIL),
							REQUEST_CODE_ORDER_DETAIL);
				}
			} catch (Exception e) {
			}
		}

	}

	private class GalleryAdapter extends BaseAdapter {

		private List<String> mImageUris;

		public GalleryAdapter(List<String> imageUris) {
			mImageUris = imageUris;
		}

		@Override
		public int getCount() {
			if (mImageUris != null) {
				return mImageUris.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = null;
			if (convertView != null && convertView instanceof ImageView) {
				imageView = (ImageView) convertView;
			} else {
				imageView = new ImageView(mActivity);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						Util.dip2px(mActivity, 65), Util.dip2px(mActivity, 65));
				if (position != 0) {
					params.leftMargin = Util.dip2px(mActivity, 10);
				}
				imageView.setLayoutParams(params);
			}
			// Uri imageUri = Uri.parse(mImageUris.get(position));
			// imageView.setTag(imageUri);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			// mImageLoader.showImage(imageView, imageUri);
			mVolleyImageLoader.showImage(imageView, mImageUris.get(position));
			return imageView;
		}

	}

	private class CancelOrderTask implements JsonTaskHandler {
		private String orderId;
		private JSONObject data;

		public CancelOrderTask(String orderID, JSONObject order) {
			orderId = orderID;
			this.data = order;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alertL(mActivity,
							R.string.account_orders_canceled_order_ok);
					this.data.put("status", "dead");
					((BaseAdapter) mListView.getAdapter())
							.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.member.cancel");
			req.addParams("order_id", orderId);
			return req;
		}

	}

	// 余额支付
	private class BalancePayTask implements JsonTaskHandler {
		private JsonRequestBean bean;

		public BalancePayTask(JsonRequestBean bean) {
			this.bean = bean;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					
					if (all.optJSONObject("data").optString("pay_app_id").equals("malipay")) {
						callAliPay(all.optJSONObject("data"));
					} else if (Run.checkPaymentStatus(mActivity, all)) {
						mOrdersList.remove(mPayingOrder);
						((BaseAdapter) mListView.getAdapter())
								.notifyDataSetChanged();
					}
				} else {
					Run.startThirdPartyPayment(mActivity, all);
				}
			} catch (Exception e) {
			}
		}
	}

	private void addRebuy2ShoppingCar(int index) {
		if (index > mOrderGoods.size() - 1) {
			hideLoadingDialog();
			if (addFailCount == 0) {
				Toast.makeText(mActivity,
						getString(R.string.account_orders_rebuy_succeed),
						Toast.LENGTH_SHORT).show();
			} else if (addSucceedCount == 0) {
				Toast.makeText(mActivity,
						getString(R.string.account_orders_rebuy_fail),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						mActivity,
						getString(R.string.account_orders_rebuy_part,
								addSucceedCount), Toast.LENGTH_SHORT).show();
			}
			Intent intent = new Intent(getActivity(),
					MainTabFragmentActivity.class);
			MainTabFragmentActivity.mTabActivity.mSelectIndex = 3;
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Run.EXTRA_TAB_POSITION, 3);
			startActivity(intent);
		} else {
			new JsonTask().execute(new RebuyTask(index));
		}
	}

	private class RebuyTask implements JsonTaskHandler {

		private int index;

		public RebuyTask(int index) {
			this.index = index;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject resp = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, resp)) {
					addSucceedCount += 1;
				} else {
					addFailCount += 1;
				}
				addRebuy2ShoppingCar(index + 1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.cart.add");
			String product_id = mOrderGoods.get(index).optString("goods_id");
			req.addParams("product_id", product_id);
			return req;
		}
	}

	/**
	 * 支付成功之后更新订单列表
	 */
	private BroadcastReceiver paySucceed = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(PAY_SUCCEE)) {
				loadNextPage(0);
			}
		}
	};

}
