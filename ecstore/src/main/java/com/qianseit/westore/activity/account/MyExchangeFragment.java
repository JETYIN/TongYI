package com.qianseit.westore.activity.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class MyExchangeFragment extends BaseDoFragment {

//	private ImageLoader mImageLoader;
//	private Resources mResources;
	private String orderId;
	private VolleyImageLoader mVolleyImageLoader;
	
	private TextView mSubTitle;
	private ListView mListView;
	private Button mSelectAllBtn;
	private Button mSubmitBtn;

	private MyExchangeAdapter mAdapter;
	private JSONArray mReasonsList;
	
	private String mSubTitleString;
	private String mStringType;

	private List<JSONObject> mAllGoods = new ArrayList<JSONObject>();
	private List<JSONObject> mSelectedGoods = new ArrayList<JSONObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle("选择商品");
//		mResources = mActivity.getResources();
//		mImageLoader = Run.getDefaultImageLoader(mActivity, mResources);
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
		Bundle b = getArguments();
		if (b != null) {
			orderId = b.getString(Run.EXTRA_DATA);
			mStringType = b.getString(Run.EXTRA_VITUAL_CATE);
			if("1".endsWith(b.getString(Run.EXTRA_VITUAL_CATE))){
				mSubTitleString = getString(R.string.please_select_change);
			} else {
				mSubTitleString = getString(R.string.please_select_return);
			}
		}
		
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_exchange_list, null);
		mSubTitle = (TextView) findViewById(R.id.fragment_exchange_subtitle);
		mSubTitle.setText(mSubTitleString);
		mListView = (ListView) findViewById(R.id.fragment_exchange_listview);
		mSelectAllBtn = (Button) findViewById(R.id.fragment_exchange_select_all);
		mSubmitBtn = (Button) findViewById(R.id.fragment_exchange_submit);

		mSelectAllBtn.setOnClickListener(this);
		mSubmitBtn.setOnClickListener(this);
		mAdapter = new MyExchangeAdapter();
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		new JsonTask().execute(new GetChargeBackList());
	}

	private class MyExchangeAdapter extends BaseAdapter implements
			OnClickListener {

		private final int ITEM_SELECT_ID = R.id.item_exchange_list_selected;

		@Override
		public int getCount() {
			return mAllGoods.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(
						R.layout.item_exchange_list, null);
				convertView.findViewById(ITEM_SELECT_ID).setOnClickListener(
						this);
				convertView.setOnClickListener(this);
			}
			JSONObject data = mAllGoods.get(position);
			convertView.setTag(data);
			TextView name = (TextView) convertView.findViewById(R.id.item_exchange_list_title);
			name.setText(data.optString("name"));
			//TextView guige = (TextView) convertView.findViewById(R.id.item_exchange_list_info1);
			TextView price = (TextView) convertView.findViewById(R.id.item_exchange_list_price);
			price.setText("￥"+data.optString("price"));
			TextView quantity = (TextView) convertView.findViewById(R.id.item_exchange_list_count);
			quantity.setText("x"+data.optString("quantity"));
			
			ImageView thumbView = ((ImageView) convertView.findViewById(R.id.item_exchange_list_thumb));
//			Uri imageUri = Uri.parse(data.optString("thumbnail_pic_scr"));
//			thumbView.setTag(imageUri);
//			mImageLoader.showImage(thumbView, imageUri);
			mVolleyImageLoader.showImage(thumbView, data.optString("thumbnail_pic_scr"));
			
			convertView.findViewById(ITEM_SELECT_ID).setTag(data);
			boolean isSelected = mSelectedGoods.contains(data);
			((ImageButton) convertView.findViewById(ITEM_SELECT_ID))
					.setImageResource(isSelected ? R.drawable.shopping_car_selected
							: R.drawable.shopping_car_unselected);
			return convertView;
		}

		@Override
		public void onClick(View view) {
			Object obj = view.getTag();
			if (obj == null) {
				return;
			}

			JSONObject data = (JSONObject) obj;
			if (view.getId() == ITEM_SELECT_ID) {
				if (mSelectedGoods.contains(data)) {
					mSelectedGoods.remove(data);
					((ImageButton) view)
							.setImageResource(R.drawable.shopping_car_unselected);
				} else {
					mSelectedGoods.add(data);
					((ImageButton) view)
							.setImageResource(R.drawable.shopping_car_selected);
				}
			} else {
				if (mSelectedGoods.contains(data)) {
					mSelectedGoods.remove(data);
					((ImageButton)view.findViewById(ITEM_SELECT_ID))
							.setImageResource(R.drawable.shopping_car_unselected);
				} else {
					mSelectedGoods.add(data);
					((ImageButton)view.findViewById(ITEM_SELECT_ID))
							.setImageResource(R.drawable.shopping_car_selected);
				}
			}
			if (mSelectedGoods.size() == mAllGoods.size()) {
				setSelectedAllState(true);
			} else {
				setSelectedAllState(false);
			}
		}

	}

	@Override
	public void onClick(View v) {
		if (v == mSelectAllBtn) {
			boolean isSelectedAll = mSelectAllBtn.isSelected();
			mSelectedGoods.clear();
			if (!isSelectedAll) {
				mSelectedGoods.addAll(mAllGoods);
			}
			setSelectedAllState(!isSelectedAll);
			mAdapter.notifyDataSetChanged();
		} else if (v == mSubmitBtn) {
			if (mSelectedGoods == null || mSelectedGoods.size() < 1) {
				Run.alert(mActivity, "请选择要退换的商品");
				return;
			}
			JSONArray list = new JSONArray();
			for (int i = 0; i < mSelectedGoods.size(); i++) {
				list.put(mSelectedGoods.get(i));
			}
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_EXCHAGNE_REASON)
					.putExtra(Run.EXTRA_DATA, mReasonsList.toString())
					.putExtra(Run.EXTRA_VALUE, list.toString())
					.putExtra(Run.EXTRA_VITUAL_CATE, mStringType),9999);
		} else {
			super.onClick(v);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			getActivity().finish();
		}
	}

	private void setSelectedAllState(boolean isSelectedAll) {
		mSelectAllBtn.setSelected(isSelectedAll);
		mSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(
				isSelectedAll ? R.drawable.shopping_car_selected
						: R.drawable.shopping_car_unselected, 0, 0, 0);
	}
	
	private class GetChargeBackList implements JsonTaskHandler{

		@Override
		public void task_response(String json_str) {
			mAllGoods.clear();
			mSelectedGoods.clear();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.getJSONObject("data");
					JSONObject content = data.getJSONObject("content");
					mReasonsList = content.getJSONArray("reason");
					JSONObject order = data.getJSONObject("order");
					JSONArray items = order.getJSONArray("items");
					int size = (items == null)? 0 : items.length();
					for (int i = 0; i < size; i++) {
						mAllGoods.add(items.getJSONObject(i));
					}
					mSelectedGoods.addAll(mAllGoods);
					mAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.aftersales.add");
			req.addParams("order_id", orderId);
			return req;
		}
		
	}

}
