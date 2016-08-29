package com.qianseit.westore.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.activity.MainTabFragmentActivity;


public class MeFragment extends Fragment{
     
	
	 private View  rootView;
	 private MainTabFragmentActivity  activity;
	 private ImageView  headPhoto;
	 private TextView   name;
	 private TextView   sex;
	 private TextView   rank;
	 private TextView   notice;
	 private TextView   recommendNumber;
	 private TextView praiseNumber;
	 private TextView attentionNumber;
	 private TextView fansNumber;
	 private RadioGroup  rg;
	
	 private CollectionFragment  collectionFragment;
	 private CollectionFragment  recommendFragment;
	 
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    activity  =  (MainTabFragmentActivity) this.getActivity();
		initData();
		initListener();
		super.onActivityCreated(savedInstanceState);
	}

	


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		     rootView  =   inflater.inflate(R.layout.fragment_me, null);
	    	 initViews();
		return   rootView;
	}



	private void initViews() {
		headPhoto =  (ImageView) rootView.findViewById(R.id.fragment_me_image);
		headPhoto =  (ImageView) rootView.findViewById(R.id.fragment_me_image);
		name =   (TextView) rootView.findViewById(R.id.fragment_me_name);
		sex =   (TextView) rootView.findViewById(R.id.fragment_me_sex);
		rank =   (TextView) rootView.findViewById(R.id.fragment_me_rank);
		notice =   (TextView) rootView.findViewById(R.id.fragment_me_notice);
		recommendNumber =   (TextView) rootView.findViewById(R.id.fragment_me_recommend_number);
		praiseNumber =   (TextView) rootView.findViewById(R.id.fragment_me_praise_number);
		attentionNumber =   (TextView) rootView.findViewById(R.id.fragment_me_attention_number);
		fansNumber =   (TextView) rootView.findViewById(R.id.fragment_me_fans_number);
		rg =   (RadioGroup) rootView.findViewById(R.id.fragment_me_rg);
		
	}



	private void initData() {
		collectionFragment  = new CollectionFragment();
		recommendFragment =  new CollectionFragment();
	}




	private void initListener() {
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				 
				if(checkedId ==  R.id.fragment_me_rb1){
					switchFragment(collectionFragment);
				}else{
					switchFragment(recommendFragment);
				}
				
				
			}
		});
		
	}
	
	
	private void switchFragment(Fragment  fragment){
		FragmentTransaction  Transaction   =  activity.getSupportFragmentManager().beginTransaction();
		Transaction.replace(R.id.fragment_me_fl, fragment);
		Transaction.commit();
	}
	
	
	
	

}
