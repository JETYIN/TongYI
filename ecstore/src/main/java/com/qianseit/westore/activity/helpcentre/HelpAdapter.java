package com.qianseit.westore.activity.helpcentre;

import java.util.List;

import cn.shopex.ecstore.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HelpAdapter extends BaseAdapter {
	
	private List<String> mNameList;
	private Context mContext;
	private boolean isShowTopMargin = true;
	
	public HelpAdapter(Context context,List<String> list){
		mNameList = list;
		mContext = context;
	}
	
	public HelpAdapter(Context context,List<String> list , boolean isShowTopMargin){
		mNameList = list;
		mContext = context;
		this.isShowTopMargin = isShowTopMargin;
	}
	
	@Override
	public int getCount() {
		if (mNameList != null) {
			return mNameList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_help_centre, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.item_help_centre_title);
		tv.setText(mNameList.get(position));
		convertView.findViewById(R.id.account_home_item_divider_martop).setVisibility(View.GONE);
		convertView.findViewById(R.id.account_home_item_divider_b2).setVisibility(View.GONE);
		convertView.findViewById(R.id.account_home_item_divider_t).setVisibility(View.GONE);
		if (position == 0) {
			if (isShowTopMargin) {
				convertView.findViewById(R.id.account_home_item_divider_martop).setVisibility(View.VISIBLE);
			}
			convertView.findViewById(R.id.account_home_item_divider_t).setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.account_home_item_divider_b1).setVisibility(View.VISIBLE);
			if (mNameList.size() == 1) {
				convertView.findViewById(R.id.account_home_item_divider_b1).setVisibility(View.GONE);
				convertView.findViewById(R.id.account_home_item_divider_b2).setVisibility(View.VISIBLE);
			}
		}else if(position == mNameList.size() - 1){
			convertView.findViewById(R.id.account_home_item_divider_b1).setVisibility(View.GONE);
			convertView.findViewById(R.id.account_home_item_divider_b2).setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}

}
