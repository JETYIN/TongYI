package com.qianseit.westore.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;

public class GirdViewAdapter<T> extends BaseAdapter{
	
	private List<T>list;
	private Context  mContext;

	public GirdViewAdapter(Context  context ,List<T>list){
		mContext = context;
		this.list  = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold hold = null;
		if(convertView == null){
			hold = new ViewHold();
			convertView =  View.inflate(mContext, R.layout.item_fragment_collection, null);
			hold.image =  (ImageView) convertView.findViewById(R.id.item_fragment_collection_image);
			hold.title =  (TextView) convertView.findViewById(R.id.item_fragment_collection_image_title);
			hold.money =  (TextView) convertView.findViewById(R.id.item_fragment_collection_money);
			hold.time =  (TextView) convertView.findViewById(R.id.item_fragment_collection_time);
			convertView.setTag(hold);
		}else {
			hold =  (ViewHold) convertView.getTag();
		}
		
		return convertView;
	}
	
	
	public class ViewHold{
	   public ImageView  image;
	   public TextView   title;
	   public TextView   money;
	   public TextView   time;
}

}
