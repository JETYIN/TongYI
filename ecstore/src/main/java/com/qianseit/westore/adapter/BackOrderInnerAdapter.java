package com.qianseit.westore.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.json.JSONArray;

import java.util.List;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/29.
 */
public class BackOrderInnerAdapter extends BaseAdapter {
    Context context;
    JSONArray list;
    private VolleyImageLoader mImageLoader;
    public BackOrderInnerAdapter(Context context, JSONArray list,VolleyImageLoader mImageLoader){
        this.context = context;
        this.list = list;
        this.mImageLoader = mImageLoader;
    }

    @Override
    public int getCount() {
        return null==list?0:list.length();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (null == view){
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.imageview,null);
            holder.img = (ImageView)view.findViewById(R.id.imgview_item);
            holder.text = (TextView)view.findViewById(R.id.textview_item);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        mImageLoader.showImage(holder.img, list.optJSONObject(i).optString("thumbnail_pic"));
        holder.text.setText(list.optJSONObject(i).optString("bn"));
        return view;
    }

    class ViewHolder {
        ImageView img;
        TextView text;
    }
}
