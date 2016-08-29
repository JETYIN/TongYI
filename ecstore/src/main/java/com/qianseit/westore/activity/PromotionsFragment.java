package com.qianseit.westore.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.fragment.RecommenFragment;
import com.qianseit.westore.fragment.RelatedFragment;
import com.qianseit.westore.fragment.SentimentFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonRequestBean.JsonRequestCallback;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.SelectsUtils;


@SuppressLint("NewApi")
public class PromotionsFragment extends BaseDoFragment{

//    private  ArrayList<String> mIds = new ArrayList<String>();
//	private LinearLayout llChildCheckView;
	
//	private ImageView bottom_view_child1;
//	private ImageView bottom_view_child2;
//	private ImageView bottom_view_child3;
//	private FrameLayout mListView;

//	private int PromotStatue=0; //0:精选 1：人气  2：关注
	private ArrayList<SelectsUtils> mGoodsArray = new ArrayList<SelectsUtils>();
	
	private RelativeLayout mSelectView;
	private RelativeLayout mRecommend;
	private RelativeLayout mRenqi;
	private RelativeLayout mGuanzhu;
	private int mPageNum;
//	private JsonTask mTask;
//	private VolleyImageLoader mVolleyImageLoader;
//	private SelectsAdapter selectsAdapter;
	private TextView imageView;
	private int potion=0;
	
	//定义3个Fragment
	private  SentimentFragment sentimeFragment=null;
	private  RecommenFragment recmmendFragment=null;
	private  RelatedFragment relatedFragment=null;
	
	private FragmentManager fragmentManager=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mActionBar.setTitle(R.string.tabbar_title2);
		mActionBar.setShowTitleBar(false);
		mActionBar.setShowHomeView(false);
//		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
//				.getImageLoader();
//
		mActionBar.setShowHomeView(false);
	}
	public void onResume() {
		super.onResume();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
//		fragmentManager=getActivity().getFragmentManager();
		fragmentManager = getChildFragmentManager();
		rootView = inflater.inflate(R.layout.fragment_goods_recommended_view_bottom, null); 
		imageView = (TextView) rootView.findViewById(R.id.fragment_main_category);
		mGuanzhu=(RelativeLayout)rootView.findViewById(R.id.recommend_guanzhu_rel);
//		mListView = (FrameLayout) rootView.findViewById(R.id.flash_sale_listviews);
		mRecommend=(RelativeLayout)rootView.findViewById(R.id.recommend_recommend_rel);
		mRenqi=(RelativeLayout)rootView.findViewById(R.id.recommend_renqi_rel);
		mGuanzhu=(RelativeLayout)rootView.findViewById(R.id.recommend_guanzhu_rel);
		
		mSelectView = mRecommend;
		mSelectView.setSelected(true);
		mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
//		selectsAdapter=new SelectsAdapter(mActivity, mGoodsArray, R.layout.item_new_listview,mVolleyImageLoader);
//		mListView.getRefreshableView().setAdapter(selectsAdapter);
		
//		selectsAdapter.notifyDataSetChanged();
		mRecommend.setOnClickListener(mSaleClickListener);
		mRenqi.setOnClickListener(mSaleClickListener);
		mGuanzhu.setOnClickListener(mSaleClickListener);
		setTabSelection(0);
//		mListView.getRefreshableView().setOnItemClickListener(
//				new OnItemClickListener() {   
//					@Override
//					public void onItemClick(AdapterView<?> parent, View view,
//							int position, long id) {
//						
//					}
//
//				});
		
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Intent intent = AgentActivity.intentForFragment(mActivity, AgentActivity.FRAGMENT_GOODS_CHOOSE);
				startActivity(intent);
			}
		});
		


	}

	private OnClickListener mSaleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mSelectView.setSelected(false);
			mSelectView.getChildAt(1).setVisibility(View.GONE);
			if (v == mRecommend) {
				mSelectView = mRecommend;
//				PromotStatue=0;
				potion=0;

			} else if (v == mRenqi) {
				mSelectView = mRenqi;
//				PromotStatue=1;
				potion=1;

			}else if (v == mGuanzhu) {
				mSelectView = mGuanzhu;
//				PromotStatue=2;
				potion=2;

			}
			setTabSelection(potion);
			mSelectView.setSelected(true);
			mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
//			loadNextPage(0);
		}
	};
	
	
	public void setTabSelection(int i) {
		//开启Fragment事物
		FragmentTransaction transaction=fragmentManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		switch(i){
		case 0:
			if(sentimeFragment==null){
				// 如果HomeFragment为空，则创建一个并添加到界面上
				sentimeFragment = new SentimentFragment();
				transaction.add(R.id.flash_sale_listviews, sentimeFragment , "sentimeFragment");
			}else{//如果不为空直接显示在控件上
				transaction.show(sentimeFragment);
			}
			break;
		case 1:
			if (recmmendFragment == null) {
				// 如果discoverFragment为空，则创建一个并添加到界面上
				recmmendFragment = new RecommenFragment();
				transaction.add(R.id.flash_sale_listviews,recmmendFragment);
			} else {
//				// 如果discoverFragment不为空，则直接将它显示出来
				transaction.show(recmmendFragment);
			}
			break;
		case 2:
			if (relatedFragment == null) {
				// 如果messageFragment为空，则创建一个并添加到界面上
				relatedFragment = new RelatedFragment();
				transaction.add(R.id.flash_sale_listviews, relatedFragment);
			} else {
//				// 如果messageFragment不为空，则直接将它显示出来
				transaction.show(relatedFragment);
			}
			break;
		}
		transaction.commitAllowingStateLoss();
	}
	
	//隐藏组件
	private void hideFragments(FragmentTransaction transaction) {
			if(sentimeFragment!=null){
				transaction.hide(sentimeFragment);
			}
			if (recmmendFragment != null) {
				transaction.hide(recmmendFragment);
			}
			if (relatedFragment != null) {
				transaction.hide(relatedFragment);
			}
	}
	
	/**
	 * 添加、取消关注，点赞和取消点赞
	 * @author chanson
	 * @CreatTime 2015-8-13 下午4:50:35
	 *
	 */
	public static class AttendPraiseTask implements JsonTaskHandler{
		
		private JsonRequestBean req;
		private JsonRequestCallback callBack;

		public AttendPraiseTask(JsonRequestBean req, JsonRequestCallback callBack){
			this.req = req;
			this.callBack = callBack;
		}
		
		@Override
		public void task_response(String json_str) {
			if (callBack != null) {
				callBack.task_response(json_str);
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return req;
		}
		
	}

	
}
