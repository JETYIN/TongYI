package com.qianseit.westore.activity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshExpandableListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class TestShoppingCarFragment extends BaseDoFragment {

	private PullToRefreshExpandableListView listView;
	private Map<String, ArrayList<JSONObject>> groupProduct = new HashMap<String, ArrayList<JSONObject>>();
	private ArrayList<String> title = new ArrayList<String>();
	private MyAdapter mAdapter;
	private VolleyImageLoader mVolleyImageLoader;

	private ArrayList<JSONObject> allGoods = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> selectedGoods = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> unselectedGoods = new ArrayList<JSONObject>();
	private ArrayList<Boolean> selectedIndex = new ArrayList<Boolean>();
	private ArrayList<Integer> acountMax = new ArrayList<Integer>();
	private String unSelectedString;
	private RelativeLayout relativeLayout;
	private boolean isFirstIn = true;
	private Button buttGo;
	
	private Dialog mDeleteDialog;
	
	private JSONArray mCoupon;
	
	private DecimalFormat df = new DecimalFormat("0.00");

	public TestShoppingCarFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.shopping_car);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.init(inflater, container, savedInstanceState);
		rootView = inflater.inflate(R.layout.test_expandable, null);
		listView = (PullToRefreshExpandableListView) findViewById(R.id.expandable);
		relativeLayout=(RelativeLayout)findViewById(R.id.shopping_rel);
		mAdapter = new MyAdapter();
		relativeLayout.setVisibility(View.GONE);
		listView.getRefreshableView().setEmptyView(relativeLayout);
		listView.getRefreshableView().setVisibility(View.GONE);
		listView.getRefreshableView().setAdapter(mAdapter);
		buttGo=(Button)findViewById(R.id.shopping_go);
		buttGo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mActivity instanceof MainTabFragmentActivity){
					MainTabFragmentActivity.mTabActivity.setCurrentTabByIndex(0);
				} else {
					Intent intent = new Intent(mActivity,MainTabFragmentActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					MainTabFragmentActivity.mTabActivity.setCurrentTabByIndex(0);
					mActivity.startActivity(intent);
				}
				
			}
		});
		listView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new JsonTask().execute(new GetCarListData());
			}

			@Override
			public void onRefreshMore() {

			}
		});

		listView.getRefreshableView().setOnGroupClickListener(
				new OnGroupClickListener() {
					@Override
					public boolean onGroupClick(ExpandableListView p, View v,
							int pos, long id) {
						return true;
					}
				});

	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (AgentApplication.getLoginedUser(mActivity).isLogined()) {
			String storeGoods = Run.loadOptionString(mActivity, Run.EXTRA_STROE_DELETE_GOODS, null);
			if (!TextUtils.isEmpty(storeGoods)) {
				if(storeGoods.indexOf("=") != -1){
					allGoods.clear();
					selectedGoods.clear();
					selectedIndex.clear();
					groupProduct.clear();
					acountMax.clear();
					title.clear();
					mAdapter.notifyDataSetChanged();
					String[] temp = storeGoods.split("=");
					Run.savePrefs(mActivity, Run.EXTRA_STROE_DELETE_GOODS,"");
					if (TextUtils.equals(temp[0], AgentApplication.getLoginedUser(mActivity).getMemberId())){
						showCancelableLoadingDialog();
						new JsonTask().execute(new ReAdd2ShoppingCar(temp[1]));
					} else {
						showCancelableLoadingDialog();
						new JsonTask().execute(new GetCarListData());
					}
				};
			} else {
				showCancelableLoadingDialog();
				new JsonTask().execute(new GetCarListData());
			}
		} else {
			if (isFirstIn) {
				isFirstIn = false;
				showCancelableLoadingDialog();
				new JsonTask().execute(new GetCarListData());
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		relativeLayout.setVisibility(View.GONE);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	private class MyAdapter extends BaseExpandableListAdapter implements
			View.OnClickListener {

		final int ID_SELECTED = R.id.shopping_car_item_selected;
		final int ID_REMOVE = R.id.shopping_car_item_remove;
		private boolean isAllSelected;

		@Override
		public int getGroupCount() {
			return title.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return groupProduct.get(title.get(groupPosition)).size();
		}

		@Override
		public String getGroup(int groupPosition) {
			return title.get(groupPosition);
		}

		@Override
		public JSONObject getChild(int groupPosition, int childPosition) {
			return groupProduct.get(title.get(groupPosition))
					.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(
						R.layout.item_shopping_car_title, null);
			}
			((TextView) convertView.findViewById(R.id.tv_goods_title))
					.setText(title.get(groupPosition));
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				int layout = R.layout.fragment_shopping_car_item_new;
				convertView = LayoutInflater.from(mActivity).inflate(layout,
						null);
				convertView.findViewById(ID_SELECTED).setOnClickListener(this);
				convertView.findViewById(ID_REMOVE).setOnClickListener(this);
				convertView.findViewById(R.id.shopping_car_item_minus).setOnClickListener(this);
				convertView.findViewById(R.id.shopping_car_item_plus)
						.setOnClickListener(this);
				convertView.findViewById(R.id.shopping_car_checkout)
						.setOnClickListener(this);
//				convertView.findViewById(R.id.shopping_car_item_itemview)
//						.setOnClickListener(this);
				convertView.findViewById(R.id.shopping_car_item_thumb)
						.setOnClickListener(this);
				convertView.findViewById(R.id.shopping_car_select_all)
						.setOnClickListener(this);
			}

			JSONObject all = getChild(groupPosition, childPosition);
			if (all == null)
				return convertView;

			int index = allGoods.indexOf(all);
			if (childPosition == 0) {
				isAllSelected = true;
			}
			boolean select = selectedIndex.get(index);
			if (!select) {
				isAllSelected = false;
			}
			if (childPosition == getChildrenCount(groupPosition) - 1) {
				JSONObject temp = new JSONObject();
				String key = title.get(groupPosition);
				int start = allGoods.indexOf(groupProduct.get(key).get(0));
				try {
					temp.put("start",
							allGoods.indexOf(groupProduct.get(key).get(0)));
					temp.put("end", start + groupProduct.get(key).size());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				convertView.findViewById(R.id.item_shoppingcar_bottom)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.shopping_car_select_all).setTag(
						temp);
				convertView.findViewById(R.id.shopping_car_select_all)
						.setSelected(isAllSelected);
				convertView.findViewById(R.id.shopping_car_checkout).setTag(
						groupPosition);
				((Button) convertView
						.findViewById(R.id.shopping_car_select_all))
						.setCompoundDrawablesWithIntrinsicBounds(
								isAllSelected ? R.drawable.order_detail_status4_ok
										: R.drawable.shopping_car_unselected,
								0, 0, 0);
				double totalPrice = 0.0d;
				for (int i = start; i < start + groupProduct.get(key).size(); i++) {
					if (selectedIndex.get(i)) {
						totalPrice += allGoods.get(i)
								.optInt("quantity")
								* allGoods.get(i)
										.optJSONObject("obj_items")
										.optJSONArray("products")
										.optJSONObject(0)
										.optJSONObject("price")
										.optDouble("buy_price");
					}
					convertView.findViewById(R.id.shopping_car_checkout).setTag(R.id.tag_object, totalPrice);
				}
				((TextView) convertView
						.findViewById(R.id.shopping_car_total_price))
						.setText("￥"+df.format(totalPrice));
			} else {
				convertView.findViewById(R.id.item_shoppingcar_bottom)
						.setVisibility(View.GONE);
			}

			convertView.setTag(all);
			boolean isSelected = selectedIndex.get(index);
			convertView.findViewById(R.id.shopping_car_item_itemview).setTag(
					all);
			fillupItemView(convertView, all);
			// 选中与否
			convertView.findViewById(ID_SELECTED).setTag(all);
			convertView.findViewById(R.id.shopping_car_item_thumb).setTag(all);
			convertView.findViewById(ID_SELECTED).setTag(R.id.tag_first,
					groupPosition);
			convertView.findViewById(ID_REMOVE).setTag(all);
			convertView.findViewById(R.id.shopping_car_item_plus).setTag(all);
			convertView.findViewById(R.id.shopping_car_item_minus).setTag(all);
			((ImageButton) convertView.findViewById(ID_SELECTED))
					.setImageResource(isSelected ? R.drawable.order_detail_status4_ok
							: R.drawable.shopping_car_unselected);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() == null)
				return;
			if (v.getId() == R.id.shopping_car_checkout) {
				int positionGroup = (Integer) v.getTag();
//				String groupParent = title.get(positionGroup);
				if (v.getTag(R.id.tag_object) == null) {
					return ;
				} else {
					double ss = (Double) v.getTag(R.id.tag_object);
					if (ss > acountMax.get(positionGroup)) {
						AccountLoginFragment.showAlertDialog(mActivity, Run.buildString("根据海关要求,一次性购买多件商品不能超过",acountMax.get(positionGroup),"元哦,请分多次购买."), "", "OK", null, null, false, null);
						return;
					}
				}
				int tempIndex = allGoods.indexOf(groupProduct.get(title.get(positionGroup)).get(0));
				int size = groupProduct.get(title.get(positionGroup)).size();
				selectedGoods.clear();
				unselectedGoods.clear();
				unSelectedString = "";
				for (int i = 0; i < allGoods.size(); i++) {
					if (i >= tempIndex && i < tempIndex + size && selectedIndex.get(i)) {
						selectedGoods.add(allGoods.get(i));
					} else {
						unselectedGoods.add(allGoods.get(i));
					}
				}
				if (selectedGoods.size() < 1) {
					Run.alert(mActivity, "请选择要算的商品");
					return ;
				}
				if (selectedGoods.size() == allGoods.size()) {
					showCancelableLoadingDialog();
					new JsonTask().execute(new SubmitCarTask());
					return;
				}
				JSONArray removeList1 = new JSONArray();
				JSONArray removeList2 = new JSONArray();
				JSONObject obj1 ;
				JSONObject obj2 ;
				for (int i = 0; i < unselectedGoods.size(); i++) {
					obj1 = new JSONObject();
					obj2 = new JSONObject();
					try {
						obj1.put("obj_type", "goods");
						obj1.put("obj_ident", unselectedGoods.get(i).optString("obj_ident"));
						removeList1.put(obj1);
						obj2.put("product_id", unselectedGoods.get(i).optJSONObject("params").getInt("product_id"));
						obj2.put("num", unselectedGoods.get(i).optInt("quantity"));
						removeList2.put(obj2);
					} catch (JSONException e) {
						e.printStackTrace();
					} finally {
						unSelectedString = removeList2.toString();
					}
				}
				new JsonTask().execute(new DeleteGoods(removeList1.toString()));
				return;
			}
			final JSONObject data = (JSONObject) v.getTag();
			if (v.getId() == R.id.shopping_car_item_itemview || R.id.shopping_car_item_thumb == v.getId()) {
				try {
					JSONObject product = data.optJSONObject("obj_items").optJSONArray("products").getJSONObject(0);
					startActivity(AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL).putExtra(
							Run.EXTRA_CLASS_ID, product.optString("goods_id")));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (v.getId() == R.id.shopping_car_item_plus) {
				Run.excuteJsonTask(new JsonTask(), new UpdateCartTask(data, data.optInt("quantity") + 1));
			} else if (v.getId() == R.id.shopping_car_item_minus) {
				int quantity = data.optInt("quantity") - 1;
				if (quantity <= 0) {
//					askRemoveGoods(data);
					return;
				}
				Run.excuteJsonTask(new JsonTask(), new UpdateCartTask(data, quantity));
			} else if (v.getId() == R.id.shopping_car_item_selected) {
				// 需要改变的价格
				int index = allGoods.indexOf(data);
				boolean select = !selectedIndex.get(index);
				selectedIndex.set(index, select);
				int groupParent = (Integer) v.getTag(R.id.tag_first);
				int tempIndex = allGoods.indexOf(groupProduct.get(title.get(groupParent)).get(0));
				int size = groupProduct.get(title.get(groupParent)).size();
				boolean isAll = true;
				for (int i = tempIndex ; i < tempIndex + size ; i++) {
					if (!selectedIndex.get(i)) {
						isAll = false;
					}
				}
				isAllSelected = isAll;
				mAdapter.notifyDataSetChanged();
			} else if (v.getId() == R.id.shopping_car_item_remove) {
				askRemoveGoods(data);
			} else if(v.getId() == R.id.shopping_car_select_all){
				boolean select = !v.isSelected();
				for (int i = data.optInt("start"); i < data.optInt("end"); i++) {
					selectedIndex.set(i, select);
				}
				if (select) {
					isAllSelected = select;
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	// 询问删除商品
	private void askRemoveGoods(final JSONObject data) {
//		CustomDialog dialog = new CustomDialog(mActivity)
//				.setMessage(R.string.shopping_car_delete);
//		dialog.setNegativeButton(R.string.cancel, null);
//		dialog.setPositiveButton(R.string.ok, new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				try {
//					// 删除购物车中商品
//					JsonRequestBean bean = new JsonRequestBean(
//							"mobileapi.cart.remove").addParams("obj_type",
//							data.optString("obj_type")).addParams("obj_ident",
//							data.optString("obj_ident"));
//					Run.excuteJsonTask(new JsonTask(), new RemoveCartTask(bean));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}).setCancelable(true).show();
		relativeLayout.setVisibility(View.GONE);
		mDeleteDialog = AccountLoginFragment.showAlertDialog(mActivity, "确定删除此商品？", "取消", "确定", null, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDeleteDialog.dismiss();
				mDeleteDialog = null;
				relativeLayout.setVisibility(View.GONE);
				try {
					// 删除购物车中商品
					JsonRequestBean bean = new JsonRequestBean(
							"mobileapi.cart.remove").addParams("obj_type",
							data.optString("obj_type")).addParams("obj_ident",
							data.optString("obj_ident"));
					Run.excuteJsonTask(new JsonTask(), new RemoveCartTask(bean));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, false, null);
	}

	private void fillupItemView(View view, JSONObject all) {
		try {
			((TextView) view.findViewById(R.id.shopping_car_item_quantity))
					.setText(all.optString("quantity"));
			// 商品信息
			JSONObject product = all.optJSONObject("obj_items")
					.optJSONArray("products").getJSONObject(0);
			JSONObject prices = product.optJSONObject("price");
			((TextView) view.findViewById(R.id.shopping_car_item_price))
					.setText(mActivity.getString(R.string.shopping_car_price,
							prices.optString("buy_price")));
			// 原价
			TextView oldPriceTV = (TextView) view
					.findViewById(R.id.shopping_car_item_oldprice);
			oldPriceTV.setVisibility(View.INVISIBLE);
			oldPriceTV.setText(mActivity.getString(R.string.shopping_car_price,
					prices.optString("buy_price")));
			oldPriceTV.getPaint().setFlags(
					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

			((TextView) view.findViewById(R.id.shopping_car_item_title))
					.setText(product.optString("name"));
			if (!product.isNull("spec_info"))
				((TextView) view.findViewById(R.id.shopping_car_item_info1))
						.setText(product.optString("spec_info"));
			// 缩略图
			ImageView thumbView = (ImageView) view
					.findViewById(R.id.shopping_car_item_thumb);
			mVolleyImageLoader.showImage(thumbView,
					product.optString("thumbnail_url"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取购物车所有商品
	 */
	private class GetCarListData implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					allGoods.clear();
					selectedGoods.clear();
					selectedIndex.clear();
					groupProduct.clear();
					acountMax.clear();
					title.clear();
					mAdapter.notifyDataSetChanged();
					JSONArray jsonList = data.optJSONArray("list_group_by_tip");
					int count = 0;
					if (jsonList != null && jsonList.length() > 0) {
						ArrayList<JSONObject> list = null;
						for (int i = 0; i < jsonList.length(); i++) {
							JSONObject obj = jsonList.optJSONObject(i);
							if (obj != null) {
								JSONArray listGoods = obj.optJSONArray("goods");
								if (listGoods != null && listGoods.length() > 0) {
									list = new ArrayList<JSONObject>();
									title.add(obj.optString("tip_name"));
									acountMax.add(obj.optInt("amount_max"));
									for (int j = 0; j < listGoods.length(); j++) {
										list.add(listGoods.optJSONObject(j));
										count += listGoods.getJSONObject(j).optInt("quantity");
									}
									groupProduct.put(obj.optString("tip_name"),
											list);
									allGoods.addAll(list);
								}
								mCoupon = obj.optJSONArray("coupon");
								if (mCoupon != null) {
									removeCoupon(0);
								}
							}
						}
					}
					Run.goodsCounts = count;
					if(MainTabFragmentActivity.mTabActivity!=null){
						MainTabFragmentActivity.mTabActivity.setShoppingCarCount(count);
					}
					for (int i = 0; i < allGoods.size(); i++) {
						selectedIndex.add(true);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				hideLoadingDialog_mt();
				listView.onRefreshComplete();
				mAdapter.notifyDataSetChanged();
				for (int i = 0; i < title.size(); i++) {
					listView.getRefreshableView().expandGroup(i);
				}
				if (title.size() > 0) {
					relativeLayout.setVisibility(View.GONE);
				} else 
					relativeLayout.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean(
					"mobileapi.cart.get_list_group_by_tip");
		}

	}

	// 更新购物车的商品
	private class UpdateCartTask implements JsonTaskHandler {
		private JSONObject data;
		private int newQuantity;

		public UpdateCartTask(JSONObject data, int newQuantity) {
			this.data = data;
			this.newQuantity = newQuantity;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean( "mobileapi.cart.update")
					.addParams("obj_type", data.optString("obj_type"))
					.addParams("obj_ident", data.optString("obj_ident"))
					.addParams("quantity", String.valueOf(newQuantity));
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
//					for (int i = 0; i < title.size(); i++) {
//						ArrayList<JSONObject> list = groupProduct.get(title.get(0));
//						for (int j = 0; j < array.length; j++) {
//							
//						}
//					}
					Run.excuteJsonTask(new JsonTask(), new GetCarListData());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 删除商品
	private class RemoveCartTask implements JsonTaskHandler {
		private JsonRequestBean bean;

		public RemoveCartTask(JsonRequestBean bean) {
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
					allGoods.clear();
					selectedGoods.clear();
					selectedIndex.clear();
					groupProduct.clear();
					acountMax.clear();
					title.clear();
//					relativeLayout.setVisibility(View.INVISIBLE);
//					mAdapter.notifyDataSetChanged();
					new JsonTask().execute(new GetCarListData());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 结算移除没选中的商品
	private class DeleteGoods implements JsonTaskHandler {

		String deleteItems;

		public DeleteGoods(String deleteItems) {
			this.deleteItems = deleteItems;
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject resp = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, resp)) {
					Run.savePrefs(mActivity, Run.EXTRA_STROE_DELETE_GOODS, 
							AgentApplication.getLoginedUser(mActivity).getMemberId()+"="+unSelectedString);
					Run.excuteJsonTask(new JsonTask(), new SubmitCarTask());
				} else {
					hideLoadingDialog_mt();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.cart.batch_remove");
			req.addParams("items", deleteItems);
			return req;
		}

	}

	/**
	 * 提交生成订单
	 */
	private class SubmitCarTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.cart.checkout")
					.addParams("isfastbuy", "false");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if ("succ".equals(all.optString("rsp")) && !all.isNull("data")) {
					JSONObject data = all.optJSONObject("data");
					String coupon_lists = "";
					if (data.optJSONArray("coupon_lists") != null) {
						coupon_lists = data.optJSONArray("coupon_lists").toString();
					}
					startActivity(AgentActivity.intentForFragment(mActivity,AgentActivity.FRAGMENT_SUBMIT_SHOPPING_CAR)
							.putExtra(Run.EXTRA_DATA,data.toString()).putExtra(Run.EXTRA_COUPON_DATA,coupon_lists));
				} else {
					String dataStr = all.isNull("data") ? "" : all.optString("data");
					AccountLoginFragment.showAlertDialog(mActivity, dataStr, "", "OK", null, null, false, null);
					
					String storeGoods = Run.loadOptionString(mActivity, Run.EXTRA_STROE_DELETE_GOODS, null);
					if (!TextUtils.isEmpty(storeGoods)) {
						if(storeGoods.indexOf("=") != -1){
							allGoods.clear();
							selectedGoods.clear();
							selectedIndex.clear();
							groupProduct.clear();
							acountMax.clear();
							title.clear();
//							mAdapter.notifyDataSetChanged();
							String[] temp = storeGoods.split("=");
							Run.savePrefs(mActivity, Run.EXTRA_STROE_DELETE_GOODS,"");
							if (TextUtils.equals(temp[0], AgentApplication.getLoginedUser(mActivity).getMemberId())){
								showCancelableLoadingDialog();
								new JsonTask().execute(new ReAdd2ShoppingCar(temp[1]));
							} else {
								showCancelableLoadingDialog();
								new JsonTask().execute(new GetCarListData());
							}
						};
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 把商品重新加入购物车
	 */
	private class ReAdd2ShoppingCar implements JsonTaskHandler{

		private String params;
		
		public ReAdd2ShoppingCar(String params){
			this.params = params;
		}
		
		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all) && !all.isNull("data")){
					new JsonTask().execute(new GetCarListData());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean( "mobileapi.cart.batch_add_cart");
			req.addParams("goods", params);
			return req;
		}
		
	}
	
	private class RemoveCoupon implements JsonTaskHandler {

		private String obj_ident;
		private int index;

		public RemoveCoupon(int index, String obj_ident) {
			this.obj_ident = obj_ident;
			this.index = index;
		}

		@Override
		public void task_response(String json_str) {
			removeCoupon(index + 1);
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(

			"mobileapi.cart.remove");
			req.addParams("obj_type", "coupon");
			req.addParams("obj_ident", obj_ident);

			return req;
		}

	}
	
	private void removeCoupon(int index) {
		if (index < mCoupon.length()) {
			new JsonTask().execute(new RemoveCoupon(index, mCoupon.optJSONObject(index).optString("obj_ident")));
		}
	}

}
