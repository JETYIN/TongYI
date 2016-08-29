package com.qianseit.westore.activity.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/27.
 */
public class AcountBackOrderDetailFragment  extends BaseDoFragment{
    private VolleyImageLoader mImageLoader;

    private JSONObject info;
    private ArrayList<JSONObject> productList = new ArrayList<JSONObject>();

    private LayoutInflater mLayoutInflater;

    private ProductAdapter mAdapter;
    private ListView mListView;

    public AcountBackOrderDetailFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar.setTitle("售后申请详情");
        mImageLoader = AgentApplication.getApp(mActivity).getImageLoader();
        Run.excuteJsonTask(new JsonTask(), new GetProductListTask((DoActivity) mActivity));
    }

    @Override
    public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        rootView = inflater.inflate(R.layout.acount_back_fragment, null);
        mListView = (ListView)rootView.findViewById(R.id.account_back_list);
        mAdapter = new ProductAdapter();
        mListView.setAdapter(mAdapter);
    }

    private void initView(){
        View headView = mLayoutInflater.inflate(R.layout.back_order_detial_headview,null);
        TextView statusText = (TextView)headView.findViewById(R.id.status);
        String status = info.optString("status");
        if ("1".equals(status)){
            statusText.setText("售后状态："+"申请中");
        }else if ("2".equals(status)){
            statusText.setText("售后状态："+"审核中");
        }else if ("3".equals(status)){
            statusText.setText("售后状态："+"审核成功");
        }else if ("4".equals(status)){
            statusText.setText("售后状态："+"完成");
        }else if ("5".equals(status)){
            statusText.setText("售后状态："+"审核未通过");
        }else{
            statusText.setText("售后状态："+"未知状态");
        }
        ((TextView)headView.findViewById(R.id.time)).setText(info.optString("add_time"));
        ((TextView)headView.findViewById(R.id.title)).setText(info.optString("title"));
        ((TextView)headView.findViewById(R.id.content)).setText(info.optString("content"));
        mListView.addHeaderView(headView);

    }

    /**
     * 获取退换货订单列表
     */
    class GetProductListTask implements JsonTaskHandler {

        private DoActivity  mActivity;

        public GetProductListTask(DoActivity  mActivity){
            this.mActivity = mActivity;
        }

        @Override
        public void task_response(String json_str) {
            mActivity.hideLoadingDialog_mt();
            try {
                JSONObject childs = new JSONObject(json_str);
                if (Run.checkRequestJson(mActivity, childs)) {
                    if (childs != null) {
                        info =  childs.optJSONObject("data");
                        initView();
                        JSONArray items = info.optJSONArray("product_data");
                        if (items != null && items.length() > 0) {
                            Log.e("item.length()", items.length() + "");
                            for (int i = 0; i < items.length(); i++) {
                                productList.add(items.optJSONObject(i));
                            }
                        }

                        mAdapter.notifyDataSetChanged();
                    }

                }
            } catch (Exception e) {
                System.out.println("---->>---e");
                e.printStackTrace();
            }

        }

        @Override
        public JsonRequestBean task_request() {
            mActivity.showCancelableLoadingDialog();
            JsonRequestBean req = new JsonRequestBean(
                    "mobileapi.member.afterrec_info");
            req.addParams("member_id", mActivity.getIntent().getStringExtra("member_id"));
            req.addParams("return_id", mActivity.getIntent().getStringExtra("return_id"));
            return req;
        }

    }


    private class ProductAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return productList.size();
        }

        @Override
        public JSONObject getItem(int i) {
            return productList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(null == view){
                holder = new ViewHolder();
                view = mLayoutInflater.inflate(R.layout.back_order_detail_item,null);
                holder.name = (TextView)view.findViewById(R.id.product_name);
                holder.img = (ImageView)view.findViewById(R.id.product_img);
                view.setTag(holder);
            }
            else{
                holder = (ViewHolder)view.getTag();
            }
            final JSONObject jsonObject = getItem(i);
            mImageLoader.showImage(holder.img, jsonObject.optString("thumbnail_pic"));
            holder.name.setText(jsonObject.optString("name"));
            return view;
        }
    }

    class ViewHolder{
        TextView name;
        ImageView img;
    }
}
