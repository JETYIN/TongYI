package com.qianseit.westore.adapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.ui.RoundImage;
import com.qianseit.westore.util.loader.VolleyImageLoader;
/**
 * 评论适配器
 * @author E431
 *
 */
public class CommentAdapter extends BaseAdapter {
	Context context ;
	JSONArray array;
	VolleyImageLoader mVolleyImageLoader;
   public CommentAdapter(Context context ,JSONArray array,VolleyImageLoader volleyImageLoader){
	   this.context = context;
	   this.array =array;
	   this.mVolleyImageLoader = volleyImageLoader;
   }
	@Override
	public int getCount() {
		return array.length();
	}

	@Override
	public Object getItem(int arg0) {
		JSONObject mJSONObject = null;
		try {
			mJSONObject = array.getJSONObject(arg0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mJSONObject;
	}

	@Override
	public long getItemId(int arg0) {
		
		return arg0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		ViewHold viewHold = null;
		if(view==null){
			viewHold = new ViewHold();
			view = LayoutInflater.from(context).inflate(R.layout.comment_list_item, null);
			viewHold.roundImage =(RoundImage)view.findViewById(R.id.iv_head); 
			viewHold.tv_nikeName =(TextView)view.findViewById(R.id.tv_nikename); 
			viewHold.tv_comtent =(TextView)view.findViewById(R.id.tv_comtent); 
			viewHold.tv_time =(TextView)view.findViewById(R.id.tv_time); 
			view.setTag(viewHold);
		}else{
			viewHold = (ViewHold)view.getTag();
		}
		JSONObject obj;
		try {
			obj = array.getJSONObject(position);
			mVolleyImageLoader.showImage(viewHold.roundImage,  obj.getString("avatar"));
			viewHold.tv_nikeName.setText(obj.getString("name")); 
			viewHold.tv_comtent.setText(obj.getString("content"));
			viewHold.tv_time.setText(obj.getString("created"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			return view;
	}
  class ViewHold{
	  RoundImage roundImage;
	  TextView   tv_nikeName;
	  TextView   tv_comtent;
	  TextView   tv_time;
  }
}
