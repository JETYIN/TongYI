package com.qianseit.westore.activity;

import cn.shopex.ecstore.R;
import com.qianseit.westore.fragment.CollectionFragment;
import com.qianseit.westore.fragment.RecommendationFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MeActivity extends FragmentActivity {

	
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
	 private RecommendationFragment  recommendFragment;
	 



	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_me);
		initViews();
		initData();
		this.initListener();
		super.onCreate(savedInstanceState);
	}
	

	private void initViews() {
		headPhoto =  (ImageView) findViewById(R.id.fragment_me_image);
		headPhoto =  (ImageView) findViewById(R.id.fragment_me_image);
		name =   (TextView) findViewById(R.id.fragment_me_name);
		sex =   (TextView) findViewById(R.id.fragment_me_sex);
		rank =   (TextView) findViewById(R.id.fragment_me_rank);
		notice =   (TextView) findViewById(R.id.fragment_me_notice);
		recommendNumber =   (TextView) findViewById(R.id.fragment_me_recommend_number);
		praiseNumber =   (TextView) findViewById(R.id.fragment_me_praise_number);
		attentionNumber =   (TextView) findViewById(R.id.fragment_me_attention_number);
		fansNumber =   (TextView) findViewById(R.id.fragment_me_fans_number);
		rg =   (RadioGroup) findViewById(R.id.fragment_me_rg);
		
	}



	private void initData() {
		collectionFragment  = new CollectionFragment();
		recommendFragment =  new RecommendationFragment();
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
		FragmentTransaction  Transaction   =  this.getSupportFragmentManager().beginTransaction();
		Transaction.replace(R.id.fragment_me_fl, fragment);
		Transaction.commit();
	}
	
	
	

}
