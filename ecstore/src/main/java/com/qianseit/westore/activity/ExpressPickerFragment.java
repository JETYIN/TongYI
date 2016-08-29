package com.qianseit.westore.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CustomDialog;
import cn.shopex.ecstore.R;

public class ExpressPickerFragment extends BaseDoFragment implements
		OnItemClickListener {
	private ListView mListView;

	private ArrayList<JSONObject> mSources = new ArrayList<JSONObject>();

	public ExpressPickerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent data = mActivity.getIntent();
		try {
			String jsonStr = data.getStringExtra(Run.EXTRA_VALUE);
			JSONArray child = new JSONArray(jsonStr);
			if (child != null && child.length() > 0) {
				for (int i = 0, c = child.length(); i < c; i++)
					mSources.add(child.getJSONObject(i));
			}
		} catch (Exception e) {
			mActivity.finish();
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.confirm_order_express);

		mListView = new ListView(mActivity);
		mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mListView.setAdapter(new ExpressAdapter());
		mListView.setOnItemClickListener(this);
		rootView = mListView;
	}

	@Override
	public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
		Intent data = new Intent();
		data.putExtra(Run.EXTRA_DATA, mSources.get(pos).toString());
		mActivity.setResult(Activity.RESULT_OK, data);
		mActivity.finish();
	}

	private class ExpressAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mSources.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mSources.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.simple_list_item2, null);
				((TextView) convertView.findViewById(android.R.id.text1))
						.setTextSize(18);
			}

			JSONObject data = getItem(position);
			((TextView) convertView.findViewById(android.R.id.text1))
					.setText(Run.buildString(data.optString("dt_name"), "(￥",
							data.optString("money"), ")"));
			((TextView) convertView.findViewById(android.R.id.text2))
					.setText(Html.fromHtml(data.optString("detail")));

			return convertView;
		}
	}
}
// public class ExpressPickerFragment extends BaseDoFragment {
//
// private final int REQUEST_ADDRESS = 0x1000;
// private final int REQUEST_STORES = 0x1001;
//
// private RadioGroup mRadioGroup;
// private TextView mShopValueTV;
// private TextView mTimeValueTV;
// private String mFromExtract;
//
// private ArrayList<String> selfTimeArray = new ArrayList<String>();
// private ArrayList<String> deliveryTimeArray = new ArrayList<String>();
// private ArrayList<String> shopArray = new ArrayList<String>();
// private ArrayList<JSONObject> mSources = new ArrayList<JSONObject>();
// private ArrayList<JSONObject> mSelftObj = new ArrayList<JSONObject>();
//
// private JSONArray listArea = new JSONArray();
//
// private JSONObject selfShop;
//
// private String mDefAddress;
// private JSONObject mExpressJson;
// private String deliveryTime;
// private String selfTime;
// private String addrId;
// private boolean isChangedAddr;
// private String mShopValue;
//
// public ExpressPickerFragment() {
// }
//
// @Override
// public void onCreate(Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
//
// Bundle b = getArguments();
// if (b != null) {
// this.addrId = b.getString(Run.EXTRA_AREA_ID);
// mDefAddress = b.getString(Run.EXTRA_ADDR);
// mFromExtract = b.getString(Run.EXTRA_FROM_EXTRACT);
// String jsonStr = b.getString(Run.EXTRA_DATA);
// if (!TextUtils.isEmpty(jsonStr)) {
// try {
// mExpressJson = new JSONObject(jsonStr);
// } catch (JSONException e) {
// e.printStackTrace();
// }
// }
// }
// this.resetTimeArray();
//
// // 门店
// // shopArray.add("惠新店");
// // shopArray.add("田村店");
// }
//
// // 重置时间数组
// private void resetTimeArray() {
// if (mFromExtract != null) {
// try {
// JSONArray list = new JSONArray(mFromExtract);
// int size = list == null ? 0 : list.length();
// deliveryTimeArray.clear();
// selfTimeArray.clear();
// for (int i = 0; i < size; i++) {
// String dateStr = list.getString(i).replaceAll("\n", "");
// SimpleDateFormat sdf = null;
// if (dateStr.length() > 15) {
// sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
// } else if(dateStr.length() > 9){
// sdf = new SimpleDateFormat("yyyy-MM-dd");
// }
// Date date = null;
// try {
// date = sdf.parse(dateStr);
// } catch (ParseException e) {
// e.printStackTrace();
// }
// Calendar c = Calendar.getInstance();
// c.setTime(date);
// if (date != null && (Calendar.getInstance().getTime().before(date)
// && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) !=
// c.get(Calendar.DAY_OF_MONTH))) {
// deliveryTimeArray.add(list.getString(i).replaceAll("\n", ""));
// selfTimeArray.add(list.getString(i).replaceAll("\n", ""));
// }
// }
// deliveryTime = deliveryTimeArray.get(0);
// Collections.sort(deliveryTimeArray);
// selfTime = selfTimeArray.get(0);
// } catch (JSONException e) {
// e.printStackTrace();
// }
// } else {
// // SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
// long now = System.currentTimeMillis();
// Calendar cal = Calendar.getInstance();
// cal.setTimeInMillis(now);
// int year = cal.get(Calendar.YEAR);
// int month = cal.get(Calendar.MONTH) + 1;
// int day = cal.get(Calendar.DAY_OF_MONTH);
// int hour = cal.get(Calendar.HOUR_OF_DAY);
// // int start = (int) Math.floor(hour / 3) + 1;
// // for (int i = 0; i < 3; i++) {
// // String date = format.format(now + i * AlarmManager.INTERVAL_DAY);
// // for (int j = (i == 0 ? start : 0); j < 8; j++) {
// // timeArray.add(String.format("%s %02d:00-%02d:00", date, j * 3,
// // (j + 1) * 3));
// // }
// // }
//
// deliveryTimeArray.clear();
// selfTimeArray.clear();
//
// // 送货时间
// String today = String.format("%d-%02d-%02d", year, month, day);
// // 明天
// cal.setTimeInMillis(now + AlarmManager.INTERVAL_DAY);
// String tomorrow = String.format("%d-%02d-%02d",
// cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
// cal.get(Calendar.DAY_OF_MONTH));
// deliveryTimeArray.add(((hour < 9) ? today : tomorrow)
// + " 08:00-10:00");
// deliveryTimeArray.add(((hour < 11) ? today : tomorrow)
// + " 10:00-12:00");
// deliveryTimeArray.add(((hour < 14) ? today : tomorrow)
// + " 12:00-15:00");
// deliveryTimeArray.add(((hour < 17) ? today : tomorrow)
// + " 15:00-18:00");
// deliveryTimeArray.add(((hour < 20) ? today : tomorrow)
// + " 18:00-21:00");
// Collections.sort(deliveryTimeArray);
// if (hour >= 7 && 8 <= hour && hour < 19)
// deliveryTimeArray.add("立即送（1小时送货）");
// if (TextUtils.isEmpty(deliveryTime))
// deliveryTime = deliveryTimeArray.get(0);
// // 自提时间
// selfTimeArray.add(((hour < 9) ? today : tomorrow) + " 08:00-10:00");
// selfTimeArray
// .add(((hour < 11) ? today : tomorrow) + " 10:00-12:00");
// selfTimeArray
// .add(((hour < 14) ? today : tomorrow) + " 12:00-15:00");
// selfTimeArray
// .add(((hour < 17) ? today : tomorrow) + " 15:00-18:00");
// selfTimeArray
// .add(((hour < 20) ? today : tomorrow) + " 18:00-21:00");
// Collections.sort(selfTimeArray);
// if (TextUtils.isEmpty(selfTime))
// selfTime = selfTimeArray.get(0);
// }
// }
//
// @Override
// public void init(LayoutInflater inflater, ViewGroup container,
// Bundle savedInstanceState) {
// mActionBar.setTitle(R.string.confirm_order_express);
//
// // 配送方式列表
// Intent data = mActivity.getIntent();
// try {
// String jsonStr = data.getStringExtra(Run.EXTRA_VALUE);
// JSONArray child = new JSONArray(jsonStr);
// if (child != null && child.length() > 0) {
// for (int i = 0, c = child.length(); i < c; i++)
// mSources.add(child.getJSONObject(i));
// }
// } catch (Exception e) {
// mActivity.finish();
// return;
// }
//
// rootView = inflater.inflate(R.layout.fragment_express_picker, null);
// findViewById(R.id.express_picker_shop_item).setOnClickListener(this);
// findViewById(R.id.express_picker_time_item).setOnClickListener(this);
// findViewById(R.id.express_picker_submit).setOnClickListener(this);
// mShopValueTV = (TextView) findViewById(R.id.express_picker_shop);
// mTimeValueTV = (TextView) findViewById(R.id.express_picker_time);
//
// mRadioGroup = (RadioGroup) findViewById(R.id.express_picker_radios);
// mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
// @Override
// public void onCheckedChanged(RadioGroup group, int checkedId) {
// TextView shopTV = (TextView) findViewById(R.id.express_picker_shop_title);
// TextView timeTV = (TextView) findViewById(R.id.express_picker_time_title);
// TextView timeValueTV = (TextView) findViewById(R.id.express_picker_time);
// if (checkedId == R.id.express_picker_delvery) {
// mExpressJson = mSources.get(1);
// timeValueTV.setText(deliveryTime);
// timeTV.setText(R.string.confirm_order_express_delivery_time);
// ((View) shopTV.getParent()).setVisibility(View.GONE);
// } else if (checkedId == R.id.express_picker_self) {
// mExpressJson = mSources.get(0);
// timeValueTV.setText(selfTime);
// ((View) shopTV.getParent()).setVisibility(View.VISIBLE);
// timeTV.setText(R.string.confirm_order_express_self_time);
// }
// }
// });
//
// try {
// // 恢复用户已经选择的配送方式
// // String jsonStr = data.getStringExtra(Run.EXTRA_DATA);
// // JSONObject child = new JSONObject(jsonStr);
// // mTimeValueTV.setText(child.optString("time"));
// // if (!TextUtils.isEmpty(child.optString("shop"))) {
// // selfTime = child.optString("time");
// // mShopValueTV.setText(child.optString("shop"));
// // mRadioGroup.check(R.id.express_picker_self);
// // } else {
// // deliveryTime = child.optString("time");
// // mRadioGroup.check(R.id.express_picker_delvery);
// // }
// mTimeValueTV.setText(mExpressJson.optString("time"));
// if (!TextUtils.isEmpty(mExpressJson.optString("shop"))) {
// selfShop = new JSONObject();
// selfShop.put("name", mExpressJson.optString("shop"));
// selfShop.put("branch_id", mExpressJson.optString("branch_id"));
// selfShop.put("address", mExpressJson.optString("address"));
// selfTime = mExpressJson.optString("time");
// mShopValueTV.setText(mExpressJson.optString("shop"));
// ((TextView)rootView.findViewById(R.id.express_picker_shop_addr)).setText(mExpressJson.optString("address"));
// mRadioGroup.check(R.id.express_picker_self);
// } else {
// deliveryTime = mExpressJson.optString("time");
// mRadioGroup.check(R.id.express_picker_delvery);
// }
// } catch (Exception e) {
// mRadioGroup.check(R.id.express_picker_delvery);
// }
//
// //new JsonTask().execute(new GetStoreList());
// //new JsonTask().execute(new GetRegionsTask());
// }
//
// @Override
// public void onClick(View v) {
// if (v.getId() == R.id.express_picker_time_item) {
// this.resetTimeArray();
// int checkedRadio = mRadioGroup.getCheckedRadioButtonId();
// boolean isDelivery = (checkedRadio != R.id.express_picker_self);
// final ArrayList<String> timeArray = isDelivery ? deliveryTimeArray
// : selfTimeArray;
//
// final TextView timeValueTV = (TextView)
// findViewById(R.id.express_picker_time);
// TextView timeTV = (TextView) findViewById(R.id.express_picker_time_title);
// final CustomDialog mDialog = new CustomDialog(mActivity);
// mDialog.setTitle(timeTV.getText().toString());
// mDialog.setSingleChoiceItems(new TimeAdapter(timeArray), -1,
// new OnItemClickListener() {
// @Override
// public void onItemClick(AdapterView<?> parent,
// View view, int pos, long id) {
// mDialog.dismiss();
// timeValueTV.setText(timeArray.get(pos));
// }
// }).setCancelable(true).show();
// } else if (v.getId() == R.id.express_picker_shop_item) {
// //showPickerShopDialog();
// //showPickerAreaDialog();
// startActivityForResult(AgentActivity.intentForFragment(
// getActivity(), AgentActivity.FRAGMENT_PICKER_STRORE), REQUEST_STORES);
// } else if (v.getId() == R.id.express_picker_submit) {
// Intent data = new Intent();
// if (mSources.indexOf(mExpressJson) == 1) {
// try {
// JSONObject mSendAddress = new JSONObject(mDefAddress);
// if (mSendAddress.optString("area").contains("其它区域")) {
// final CustomDialog mDialog = new CustomDialog(mActivity);
// mDialog.setTitle(R.string.express_tips);
// mDialog.setMessage(R.string.express_tips_content);
// mDialog.setNegativeButton(R.string.express_change_addr,
// new OnClickListener() {
//
// @Override
// public void onClick(View arg0) {
// mDialog.dismiss();
// startActivityForResult(
// AgentActivity
// .intentForFragment(
// mActivity,
// AgentActivity.FRAGMENT_ADDRESS_BOOK)
// .putExtra(
// Run.EXTRA_VALUE,
// true),
// REQUEST_ADDRESS);
// }
// });
// mDialog.setPositiveButton(R.string.express_picker_self,
// new OnClickListener() {
//
// @Override
// public void onClick(View arg0) {
// mDialog.dismiss();
// mRadioGroup
// .check(R.id.express_picker_self);
// startActivityForResult(AgentActivity.intentForFragment(
// getActivity(), AgentActivity.FRAGMENT_PICKER_STRORE), REQUEST_STORES);
// //showPickerShopDialog();
// //showPickerAreaDialog();
// }
// });
// mDialog.setCancelable(true);
// mDialog.show();
// return;
// }
// } catch (JSONException e1) {
// e1.printStackTrace();
// }
// }
// try {
// TextView timeValueTV = (TextView) findViewById(R.id.express_picker_time);
// TextView shopValueTV = (TextView) findViewById(R.id.express_picker_shop);
// String shopValue = shopValueTV.getText().toString();
//
// String detailStr = "";
// String branchId = "";
// if (shopValueTV.isShown()) {
// if (TextUtils.isEmpty(shopValue)) {
// onClick(findViewById(R.id.express_picker_shop_item));
// return;
// }
// String branch_id = "";
// // detailStr = Run.buildString(shopValue, "\n");
// // for (int i = 0; i < mSelftObj.size(); i++) {
// // if (mSelftObj.get(i).optString("name")
// // .equals(shopValue)) {
// // branch_id = mSelftObj.get(i).optString("branch_id");
// // mExpressJson.put("branch_id", branch_id);
// // break;
// // }
// //
// // }
// detailStr = Run.buildString(shopValue, "\n");
// mExpressJson.put("shop", shopValue);
// if (selfShop != null) {
// mExpressJson.put("address", selfShop.optString("address"));
// branch_id = selfShop.optString("branch_id");
// mExpressJson.put("branch_id", branch_id);
// }
// branchId = Run.buildString(branch_id, "\n");
// }
//
// String timeValue = timeValueTV.getText().toString();
// detailStr = Run.buildString(detailStr, timeValue);
// mExpressJson.put("detail", detailStr);
// mExpressJson.put("time", timeValue);
// data.putExtra(Run.EXTRA_DATA, mExpressJson.toString());
// data.putExtra(Run.EXTRA_VALUE, isChangedAddr);
// data.putExtra(Run.EXTRA_ADDR, mDefAddress);
// mActivity.setResult(Activity.RESULT_OK, data);
// mActivity.finish();
// } catch (Exception e) {
// }
// } else {
// super.onClick(v);
// }
// }
//
// private void showPickerShopDialog() {
// TextView shopTV = (TextView) findViewById(R.id.express_picker_shop_title);
// final CustomDialog mDialog = new CustomDialog(mActivity);
// mDialog.setTitle(shopTV.getText().toString());
// mDialog.setSingleChoiceItems(new TimeAdapter(shopArray), -1,
// new OnItemClickListener() {
// @Override
// public void onItemClick(AdapterView<?> parent, View view,
// int pos, long id) {
// mDialog.dismiss();
// ((TextView) findViewById(R.id.express_picker_shop))
// .setText(shopArray.get(pos));
// }
// }).setCancelable(true).show();
// }
//
// private void showPickerAreaDialog(){
// if (listArea.length() > 0) {
// final ArrayList<String> list = new ArrayList<String>();
// for (int i = 0; i < listArea.length(); i++) {
// list.add(listArea.optString(i).split(":")[0]);
// }
// final CustomDialog mDialog = new CustomDialog(mActivity);
// mDialog.setTitle("区域选择");
// mDialog.setSingleChoiceItems(new TimeAdapter(list), -1,
// new OnItemClickListener() {
// @Override
// public void onItemClick(AdapterView<?> parent, View view,
// int pos, long id) {
// mDialog.dismiss();
// Toast.makeText(mActivity, "选择了:"+listArea.optString(pos),
// Toast.LENGTH_SHORT).show();
// showPickerShopDialog();
// }
// }).setCancelable(true).setCanceledOnTouchOutside(true).show();
// }
// }
//
// @Override
// public void onActivityResult(int requestCode, int resultCode, Intent data) {
// super.onActivityResult(requestCode, resultCode, data);
// if (resultCode != Activity.RESULT_OK || data == null)
// return;
// if (requestCode == REQUEST_ADDRESS) {
// isChangedAddr = true;
// mDefAddress = data.getStringExtra(Run.EXTRA_DATA);
// // try {
// // JSONObject addrObj = new JSONObject(mDefAddress);
// // if (!addrObj.optString("addr_id").equals(addrId)) {
// // new JsonTask().execute(new GetStoreList());
// // }
// // } catch (JSONException e) {
// // e.printStackTrace();
// // }
// }else if(requestCode == REQUEST_STORES){
// String resultStores = data.getStringExtra(Run.EXTRA_DATA);
// if (!TextUtils.isEmpty(resultStores)) {
// try {
// selfShop = new JSONObject(resultStores);
// } catch (JSONException e) {
// e.printStackTrace();
// }
// String name = selfShop.optString("name");
// ((TextView)findViewById(R.id.express_picker_shop_addr)).setText("("+selfShop.optString("address")+")");
// mShopValueTV.setText(name);
// }
// }
// }
//
// private class GetStoreList implements JsonTaskHandler {
//
// @Override
// public void task_response(String json_str) {
// mSelftObj.clear();
// shopArray.clear();
// try {
// JSONObject all = new JSONObject(json_str);
// if (Run.checkRequestJson(mActivity, all)) {
// JSONArray list = all.getJSONArray("data");
// int length = list == null ? 0 : list.length();
// for (int i = 0; i < length; i++) {
// mSelftObj.add(list.getJSONObject(i));
// shopArray.add(list.getJSONObject(i).optString("name"));
// }
// }
// } catch (JSONException e) {
// e.printStackTrace();
// }
//
// }
//
// @Override
// public JsonRequestBean task_request() {
// JsonRequestBean req = new JsonRequestBean(
// "mobileapi.cart.get_stores_list");
// req.addParams("addr_id", addrId);
// return req;
// }
//
// }
//
// private class GetRegionsTask implements JsonTaskHandler{
//
// @Override
// public void task_response(String json_str) {
// try {
// JSONObject all = new JSONObject(json_str);
// if (Run.checkRequestJson(mActivity, all)) {
// JSONArray child = all.optJSONArray("data");
// if (child != null && child.length() > 0) {
// listArea = child.optJSONArray(2).optJSONArray(0);
// }
// }
// } catch (Exception e) {
// e.printStackTrace();
// } finally{
// hideLoadingDialog_mt();
// }
// }
//
// @Override
// public JsonRequestBean task_request() {
// showCancelableLoadingDialog();
// return new JsonRequestBean(
// "mobileapi.member.get_regions");
// }
//
// }
//
// private class TimeAdapter extends BaseAdapter {
// private ArrayList<String> mTimeStrArray;
//
// public TimeAdapter(ArrayList<String> times) {
// mTimeStrArray = times;
// }
//
// @Override
// public int getCount() {
// return mTimeStrArray.size();
// }
//
// @Override
// public Object getItem(int position) {
// return mTimeStrArray.get(position);
// }
//
// @Override
// public long getItemId(int position) {
// return 0;
// }
//
// @Override
// public View getView(int position, View convertView, ViewGroup parent) {
// if (convertView == null) {
// convertView = mActivity.getLayoutInflater().inflate(
// android.R.layout.simple_list_item_1, null);
// TextView tv = ((TextView) convertView
// .findViewById(android.R.id.text1));
// tv.setTextColor(mActivity.getResources().getColor(
// R.color.westore_primary_textcolor));
// tv.setTextSize(16);
// }
//
// ((TextView) convertView.findViewById(android.R.id.text1))
// .setText(getItem(position).toString());
// return convertView;
// }
// }
// }
