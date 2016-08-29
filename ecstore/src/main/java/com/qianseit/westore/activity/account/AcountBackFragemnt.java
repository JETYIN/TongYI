package com.qianseit.westore.activity.account;

import android.content.Intent;
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
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.adapter.GirdViewAdapter;
import com.qianseit.westore.adapter.HorListViewAdapter;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.model.SmalGood;
import com.qianseit.westore.ui.HorizontalListView;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/26.
 */
public class AcountBackFragemnt extends BaseDoFragment {

    private VolleyImageLoader mImageLoader;
    private LoginedUser mLoginedUser;
    private ArrayList<JSONObject> returnList = new ArrayList<JSONObject>();
    private ReturnGoodsAdapter returnAdapter;

    private LayoutInflater mLayoutInflater;

    public AcountBackFragemnt(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar.setTitle("申请退换货");
        mImageLoader = AgentApplication.getApp(mActivity).getImageLoader();
        mLoginedUser = AgentApplication.getLoginedUser(mActivity);
//        mImageLoader = ((AgentApplication) mActivity.getApplication())
//                .getImageLoader();
        Run.excuteJsonTask(new JsonTask(), new GetReturnListTask((DoActivity)mActivity));
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(null != returnList && returnList.size()>0){
//            returnList.clear();;
//            returnAdapter.notifyDataSetChanged();
//        }
//        Run.excuteJsonTask(new JsonTask(), new GetReturnListTask((DoActivity) mActivity));
    }

    @Override
    public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        rootView = inflater.inflate(R.layout.acount_back_fragment, null);
        ListView mListView = (ListView)rootView.findViewById(R.id.account_back_list);
        returnAdapter = new ReturnGoodsAdapter();
        mListView.setAdapter(returnAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ldy","-----ldy");
        if (null != data){
            if (0 == resultCode){
                if(null != returnList && returnList.size()>0){
                    returnList.clear();;
                    returnAdapter.notifyDataSetChanged();
                }
                Run.excuteJsonTask(new JsonTask(), new GetReturnListTask((DoActivity) mActivity));
            }
        }
    }

    /**
     * 获取退换货列表
     */
    class GetReturnListTask implements JsonTaskHandler {

        private DoActivity  mActivity;

        public GetReturnListTask(DoActivity  mActivity){
            this.mActivity = mActivity;
        }

        @Override
        public void task_response(String json_str) {
            mActivity.hideLoadingDialog_mt();
            try {
                JSONObject childs = new JSONObject(json_str);
                if (Run.checkRequestJson(mActivity, childs)) {
                    if (childs != null) {
                        JSONArray items = childs.optJSONArray("data");

                        if (items != null && items.length() > 0) {
                            Log.e("item.length()", items.length() + "");
                            for (int i = 0; i < items.length(); i++) {
                                returnList.add(items.optJSONObject(i));
                            }
                            returnAdapter.notifyDataSetChanged();
                        }
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
                    "mobileapi.member.afterlist");
            req.addParams("member_id", mLoginedUser.getMemberId());
            req.addParams("nPage", "1");
            return req;
        }

    }


    private class ReturnGoodsAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return returnList.size();
        }

        @Override
        public JSONObject getItem(int i) {
            return null;
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
                view = mLayoutInflater.inflate(R.layout.acount_back_fragment_item,null);
                holder.orderID = (TextView)view.findViewById(R.id.order_num);
                holder.payAcount = (TextView)view.findViewById(R.id.amount_pay);
                holder.payType = (TextView)view.findViewById(R.id.pay_type);
                holder.date = (TextView)view.findViewById(R.id.pay_date);
                holder.apply = (TextView)view.findViewById(R.id.apply);
                holder.img = (ImageView)view.findViewById(R.id.product_img);
                holder.applyLl = (LinearLayout)view.findViewById(R.id.for_customer_service);
                holder.listview = (HorizontalListView)view.findViewById(R.id.horizontal_listview);
                view.setTag(holder);
            }
            else{
                holder = (ViewHolder)view.getTag();
            }
            final JSONObject jsonObject = returnList.get(i);
            holder.orderID.setText(jsonObject.optString("order_id"));
            holder.payAcount.setText("￥" + jsonObject.optString("cur_amount"));
            String payAppID = jsonObject.optJSONObject("payinfo").optString("pay_app_id");
            if ("deposit".equals(payAppID)){
                holder.payType.setText("预存款");
            } else if ("wxpayjsapi".equals(payAppID)){
                holder.payType.setText("微信支付");
            }else if ("malipay".equals(payAppID)){
                holder.payType.setText("支付宝支付");
            }else if ("wapupacp".equals(payAppID)){
                holder.payType.setText("银联支付");
            }
            holder.date.setText(jsonObject.optString("createtime"));
            final boolean isAfterRec = jsonObject.optBoolean("is_afterrec");
            if (!isAfterRec){
                holder.apply.setText("已申请售后");
            }else{
                holder.apply.setText("申请售后");
            }
            holder.applyLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isAfterRec)
                        return;
                    startActivityForResult(AgentActivity.intentForFragment(mActivity,
                            AgentActivity.ACCOUNT_BACK_APPLY).putExtra("info", jsonObject.toString()),0);
                }
            });

            JSONObject j1 = jsonObject.optJSONObject("goods_items");
            Log.e("ldy","j1:"+j1.toString());
            Iterator it = j1.keys();
            String key = "";
            List<SmalGood> imgUrlList = new ArrayList<SmalGood>();
            while (it.hasNext()){
                key = it.next().toString();
                Log.e("ldy", "key:" + key + "---position:" + i);
                JSONObject j2 = j1.optJSONObject(key).optJSONObject("product");
                SmalGood sg = new SmalGood();
                sg.setUrl(j2.optString("thumbnail_pic_src"));
                sg.setCount(String.valueOf(j2.optInt("quantity")));
                sg.setLinkUrl(j2.optString("link_url"));
                imgUrlList.add(sg);
            }
            HorListViewAdapter adapter = new HorListViewAdapter(mActivity,imgUrlList,mImageLoader);
            holder.listview.setAdapter(adapter);
            //mImageLoader.showImage(holder.img, url);


            return view;
        }
    }

    class ViewHolder{
        TextView orderID;
        TextView payAcount;
        TextView payType;
        TextView date;
        TextView apply;
        ImageView img;
        LinearLayout applyLl;
        HorizontalListView listview;
    }

}
