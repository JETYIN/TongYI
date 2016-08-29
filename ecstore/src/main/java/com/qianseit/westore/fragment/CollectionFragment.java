package com.qianseit.westore.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import cn.shopex.ecstore.R;
import com.qianseit.westore.activity.MeActivity;


public class CollectionFragment extends Fragment{
     
	  private MeActivity  activity;




	 
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		activity = (MeActivity) this.getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View  view  =    inflater.inflate(R.layout.fragment_collection, null);
		GridView  girdview =   (GridView) view 
				.findViewById(R.id.fragment_collection_girdview);
		/***
		 * 在这里添加代码      girdview写了
		 */
		///girdview.setAdapter(new GirdViewAdapter(activity.this)  );
		return   view;
	}



	private void initViews() {
		
	}



	private void initData() {
		
		
	}




	
	
	
	
	

}
