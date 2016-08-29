package com.qianseit.westore.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.model.SmalGood;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import java.util.List;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/29.
 */
public class HorListViewAdapter extends BaseAdapter{
    Context context;
    List<SmalGood> list;
    private VolleyImageLoader mImageLoader;
    public HorListViewAdapter(Context context, List<SmalGood> list,VolleyImageLoader mImageLoader){
        this.context = context;
        this.list = list;
        this.mImageLoader = mImageLoader;
    }

    @Override
    public int getCount() {
        return null==list?0:list.size();
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (null == view){
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.imageview,null);
            holder.img = (ImageView)view.findViewById(R.id.imgview_item);
            holder.count = (TextView)view.findViewById(R.id.count);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view.getTag();
        }
        mImageLoader.showImage(holder.img, list.get(i).getUrl());
        holder.count.setText(list.get(i).getCount());
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(AgentActivity.intentForFragment(context,
                        AgentActivity.FRAGMENT_ACCOUNT_BACK_WEBVIEW_FRAGEMNT)
                        .putExtra("link_url", "http://www.ty16.cn"+list.get(i).getLinkUrl()));
            }
        });
        return view;
    }

    class ViewHolder {
        ImageView img;
        TextView count;
    }

}
