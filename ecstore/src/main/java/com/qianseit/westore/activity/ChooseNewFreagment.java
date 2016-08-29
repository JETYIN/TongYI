package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.ChooseUtil;
import com.qianseit.westore.util.ChooseUtils;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import com.tencent.android.tpush.horse.o;

public class ChooseNewFreagment extends BaseDoFragment {
	private PullToRefreshListView sentimeListView;
	private VolleyImageLoader mVolleyImageLoader;
	private SelectsAdapter selectsAdapter;
	private JsonTask mTask;
	private int mPageNum;
	private LoginedUser mLoginedUser;
	private String mUserId;
	private ArrayList<ChooseUtil> mGoodsArray = new ArrayList<ChooseUtil>();
	private FragmentActivity context;
    private int imageWidth;
    private View mEmptyView;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.goods_shooseg);
		mActionBar.setShowHomeView(false);
		context = getActivity();
		mLoginedUser = AgentApplication.getLoginedUser(mActivity);
		WindowManager wm= (WindowManager) mActivity
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		float width = Float.valueOf(dm.widthPixels);
		imageWidth=(int) ((width-20)/2);

	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.gridview_new_fragment, null);
		sentimeListView = (PullToRefreshListView) rootView
				.findViewById(R.id.flash_sentiment_listviews);
		sentimeListView.setVisibility(View.GONE);
		sentimeListView.setEmptyView(mEmptyView);
		mVolleyImageLoader = ((AgentApplication) getActivity().getApplication())
				.getImageLoader();
		mEmptyView = rootView.findViewById(R.id.empty_view);
		sentimeListView.setEmptyView(mEmptyView);
		mEmptyView.findViewById(R.id.empty_view_goto_shop).setOnClickListener(this);

		selectsAdapter = new SelectsAdapter();
		sentimeListView.getRefreshableView().setAdapter(selectsAdapter);

		sentimeListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount < 5)
					return;
				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					inte1(mPageNum);
			}
		});
		sentimeListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				inte1(0);
			}

			@Override
			public void onRefreshMore() {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!mLoginedUser.isLogined()){
			startActivityForResult(AgentActivity.intentForFragment(mActivity,
					AgentActivity.FRAGMENT_ACCOUNT_LOGIN),
					REQUEST_CODE_USER_LOGIN);
			getActivity().finish();
		}
		else
			inte1(0);
	}

	private void inte1(int dum) {
		this.mPageNum = dum + 1;
		if (this.mPageNum == 1) {
			mGoodsArray.clear();
			selectsAdapter.notifyDataSetChanged();
			sentimeListView.setRefreshing();
		} else {
			if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
				return;
		}

		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new SelectsListData());
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.empty_view_goto_shop) {
			MainTabFragmentActivity.mTabActivity.setCurrentTabByIndex(0);
			mActivity.finish();
		} else {
			super.onClick(v);
		}
	}

	public class SelectsAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			return mGoodsArray.size();
		}

		@Override
		public Object getItem(int position) {
			return mGoodsArray.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		class ViewHolder{
			ImageView imageview;
			TextView texttime;
			TextView textview;
			Button imageButton;
			Button imageCalcelButton;
			Button imageCalcelButton1;
			Button imageButton1;
			ImageView imageview1;
			TextView texttime1;
			TextView textview1;
		}
		@Override
		public View getView(final int position, View converView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if(converView==null){
				converView = LayoutInflater.from(getActivity()).inflate(
						R.layout.gridview_item_pull, null);
				holder=new ViewHolder();
				holder.imageview = (ImageView) converView
						.findViewById(R.id.goods_detail_images);
				holder.texttime = (TextView) converView
						.findViewById(R.id.textview_times);
				holder.textview = (TextView) converView
						.findViewById(R.id.textview_titles);
				holder.imageButton = (Button) converView
						.findViewById(R.id.button_related);
				holder.imageCalcelButton = (Button) converView
						.findViewById(R.id.button_calcel_related);
				holder.imageview1 = (ImageView) converView
						.findViewById(R.id.goods_detail_images1);
				holder.texttime1 = (TextView) converView
						.findViewById(R.id.textview_times1);
				holder.textview1 = (TextView) converView
						.findViewById(R.id.textview_titles1);
				holder.imageButton1 = (Button) converView
						.findViewById(R.id.button_related1);
				holder.imageCalcelButton1 = (Button) converView
						.findViewById(R.id.button_calcel_related1);
				
				LayoutParams params=new LinearLayout.LayoutParams(imageWidth,imageWidth);
				holder.imageview.setLayoutParams(params);
				holder.imageview1.setLayoutParams(params);
				converView.setTag(holder);
			}else{
				holder=(ViewHolder) converView.getTag();
			}
	
			final LinearLayout mLinearLayout1 = (LinearLayout) converView
					.findViewById(R.id.ll_left);
			final LinearLayout mLinearLayout2 = (LinearLayout) converView
					.findViewById(R.id.ll_left1);
			ChooseUtil chooseUtils = mGoodsArray.get(position);
			ChooseUtils chooseInfo = chooseUtils.getChooseUtil1();
			mVolleyImageLoader.showImage(holder.imageview, chooseInfo.getImagePath());
			holder.textview.setText(chooseInfo.getGoods_name());
			holder.texttime.setText(chooseInfo.getSelectsTime());
			if (chooseInfo.getIs_opinions().equals("0")) {
				holder.imageButton.setVisibility(View.VISIBLE);
				holder.imageCalcelButton.setVisibility(View.GONE);
				mLinearLayout1.setTag(chooseInfo);
				holder.imageButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Bundle bundle = new Bundle();
						bundle.putSerializable(context
								.getString(R.string.intent_key_serializable),
								(ChooseUtils) mLinearLayout1.getTag());
						Intent intent = AgentActivity.intentForFragment(
								context, AgentActivity.FRAGMENT_GOODS_SHOOSEG);
						intent.putExtras(bundle);
						context.startActivity(intent);

					}
				});
			} else {
				holder.imageButton.setVisibility(View.GONE);
				holder.imageCalcelButton.setVisibility(View.VISIBLE);
			}

			final ChooseUtils chooseInfo2 = chooseUtils.getChooseUtil2();
			if (chooseInfo2 != null) {
				mVolleyImageLoader.showImage(holder.imageview1,
						chooseInfo2.getImagePath());
				holder.textview1.setText(chooseInfo2.getGoods_name());
				holder.texttime1.setText(chooseInfo2.getSelectsTime());
				if (chooseInfo2.getIs_opinions().equals("0")) {
					holder.imageButton1.setVisibility(View.VISIBLE);
					holder.imageCalcelButton1.setVisibility(View.GONE);
					mLinearLayout2.setTag(chooseInfo2);
					holder.	imageButton1.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							Bundle bundle = new Bundle();
							bundle.putSerializable(
									context.getString(R.string.intent_key_serializable),
									(ChooseUtils) mLinearLayout2.getTag());
							Intent intent = AgentActivity.intentForFragment(
									context,
									AgentActivity.FRAGMENT_GOODS_SHOOSEG);
							intent.putExtras(bundle);
							context.startActivity(intent);

						}
					});
				} else {
					holder.imageCalcelButton1.setVisibility(View.VISIBLE);
					holder.imageButton1.setVisibility(View.GONE);
				}

			}else{
				mLinearLayout2.setVisibility(View.INVISIBLE);
			}

			return converView;
		}
		
		
	}

	private class SelectsListData implements JsonTaskHandler {
		private JSONObject data;
		private int newQuantity = 1;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.qianseit.westore.http.JsonTaskHandler#task_response(java.lang
		 * .String)
		 */
		@Override
		public void task_response(String json_str) {
			sentimeListView.onRefreshComplete();
			Log.i("Shoosejson:", "" + json_str);

			JSONObject dataJson;
			try {
				dataJson = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, dataJson , false) ) {
					JSONArray dataJsonArray = dataJson.getJSONArray("data");
					int length = (int) (dataJsonArray.length() / 2);
					
					for (int i = 0; i < length; i++) {
						ChooseUtil chooseUtils = new ChooseUtil();
						ChooseUtils chooseUtil1 = new ChooseUtils();
						ChooseUtils chooseUtil2 = new ChooseUtils();
						int posiont = i + 1;
						posiont = posiont * 2;
						JSONObject selectsInfoAObject = dataJsonArray
								.getJSONObject(posiont - 2);
						String order_ids = selectsInfoAObject.getString("order_id");
						chooseUtil1.setOrder_id(order_ids);
						String goods_id = selectsInfoAObject.getString("goods_id");
						chooseUtil1.setGoods_id(goods_id);
						String goods_name = selectsInfoAObject
								.getString("goods_name");
						chooseUtil1.setGoods_name(goods_name);
						String brand_name = selectsInfoAObject
								.getString("brand_name");
						chooseUtil1.setBrand_name(brand_name);
						String createtime = selectsInfoAObject
								.getString("createtime");
						chooseUtil1.setSelectsTime(createtime);
						String image = selectsInfoAObject.getString("image");
						chooseUtil1.setImagePath(image);
						String is_opinions = selectsInfoAObject
								.getString("is_opinions");
						chooseUtil1.setIs_opinions(is_opinions);
						String is_comment = selectsInfoAObject
								.getString("is_comment");
						chooseUtil1.setIs_comment(is_comment);
						JSONObject selectsInfoAObject2 = dataJsonArray
								.getJSONObject(posiont - 1);
						String order_ids1 = selectsInfoAObject2
								.getString("order_id");
						chooseUtil2.setOrder_id(order_ids1);
						String goods_id1 = selectsInfoAObject2
								.getString("goods_id");
						chooseUtil2.setGoods_id(goods_id1);
						String goods_name1 = selectsInfoAObject2
								.getString("goods_name");
						chooseUtil2.setGoods_name(goods_name1);
						String brand_name1 = selectsInfoAObject2
								.getString("brand_name");
						chooseUtil2.setBrand_name(brand_name1);
						String createtime1 = selectsInfoAObject2
								.getString("createtime");
						chooseUtil2.setSelectsTime(createtime1);
						String image1 = selectsInfoAObject2.getString("image");
						chooseUtil2.setImagePath(image1);
						String is_opinions1 = selectsInfoAObject2
								.getString("is_opinions");
						chooseUtil2.setIs_opinions(is_opinions1);
						String is_comment1 = selectsInfoAObject2
								.getString("is_comment");
						chooseUtil2.setIs_comment(is_comment1);
						
						chooseUtils.setChooseUtil1(chooseUtil1);
						chooseUtils.setChooseUtil2(chooseUtil2);
						mGoodsArray.add(chooseUtils);
					}
					
					length = (int) (dataJsonArray.length() % 2);
					if (length == 1) {
						
						ChooseUtil chooseUtils = new ChooseUtil();
						
						ChooseUtils chooseUtil1 = new ChooseUtils();
						
						JSONObject selectsInfoAObject = dataJsonArray
								.getJSONObject(dataJsonArray.length() - 1);
						String order_ids = selectsInfoAObject.getString("order_id");
						chooseUtil1.setOrder_id(order_ids);
						String goods_id = selectsInfoAObject.getString("goods_id");
						chooseUtil1.setGoods_id(goods_id);
						String goods_name = selectsInfoAObject
								.getString("goods_name");
						chooseUtil1.setGoods_name(goods_name);
						String brand_name = selectsInfoAObject
								.getString("brand_name");
						chooseUtil1.setBrand_name(brand_name);
						String createtime = selectsInfoAObject
								.getString("createtime");
						chooseUtil1.setSelectsTime(createtime);
						String image = selectsInfoAObject.getString("image");
						chooseUtil1.setImagePath(image);
						String is_opinions = selectsInfoAObject
								.getString("is_opinions");
						chooseUtil1.setIs_opinions(is_opinions);
						String is_comment = selectsInfoAObject
								.getString("is_comment");
						chooseUtil1.setIs_comment(is_comment);
						
						chooseUtils.setChooseUtil1(chooseUtil1);
						
						mGoodsArray.add(chooseUtils);
					}
					
					sentimeListView.onRefreshComplete();
					selectsAdapter.notifyDataSetChanged();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				sentimeListView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean jrbean = new JsonRequestBean(
					"mobileapi.goods.get_goods_for_order");
			jrbean.addParams("goods_id", "unopinions");
			jrbean.addParams("page", String.valueOf(mPageNum));
			return jrbean;
		}
	}
}
