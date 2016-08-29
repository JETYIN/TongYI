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
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.LoginedUser;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.adapter.BackOrderInnerAdapter;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.HorizontalListView;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/27.
 */
public class AcountBackOrderFragment extends BaseDoFragment {
    private VolleyImageLoader mImageLoader;
    private LoginedUser mLoginedUser;
    private ArrayList<JSONObject> returnList = new ArrayList<JSONObject>();

    private LayoutInflater mLayoutInflater;

    private ReturnGoodsAdapter mAdapter;

    public AcountBackOrderFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar.setTitle("退换换订单");
        mLoginedUser = AgentApplication.getLoginedUser(mActivity);
        mImageLoader = AgentApplication.getApp(mActivity).getImageLoader();
        Run.excuteJsonTask(new JsonTask(), new GetReturnListTask((DoActivity) mActivity));
    }

    @Override
    public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        rootView = inflater.inflate(R.layout.acount_back_fragment, null);
        ListView mListView = (ListView)rootView.findViewById(R.id.account_back_list);
        mAdapter = new ReturnGoodsAdapter();
        mListView.setAdapter(mAdapter);
    }

    /**
     * 获取退换货订单列表
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
                    "mobileapi.member.afterrec");
            req.addParams("member_id", mLoginedUser.getMemberId());
            req.addParams("nPage", "1");
            return req;
        }

    }


    private class ReturnGoodsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return returnList.size();
        }

        @Override
        public JSONObject getItem(int i) {
            return returnList.get(i);
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
                view = mLayoutInflater.inflate(R.layout.back_order_item,null);
                holder.orderID = (TextView)view.findViewById(R.id.order_num);
                holder.date = (TextView)view.findViewById(R.id.apply_date);
                holder.status = (TextView)view.findViewById(R.id.status);
                holder.look = (LinearLayout)view.findViewById(R.id.look);
                holder.addPicll = (LinearLayout)view.findViewById(R.id.add_pic);
                holder.listview = (ListView)view.findViewById(R.id.horizontal_listview);
                view.setTag(holder);
            }
            else{
                holder = (ViewHolder)view.getTag();
            }
            final JSONObject jsonObject = getItem(i);
            holder.orderID.setText(jsonObject.optString("order_id"));
            holder.date.setText(jsonObject.optString("add_time"));
            String status = jsonObject.optString("status");
            if ("1".equals(status)){
                holder.status.setText("申请中");
            }else if ("2".equals(status)){
                holder.status.setText("审核中");
            }else if ("3".equals(status)){
                holder.status.setText("审核成功");
            }else if ("4".equals(status)){
                holder.status.setText("完成");
            }else if ("5".equals(status)){
                holder.status.setText("审核未通过");
            }else{
                holder.status.setText("未知状态");
            }

            JSONArray productData = jsonObject.optJSONArray("product_data");
//            for (int j=0;j<productData.length();j++){
//                JSONObject jb = productData.optJSONObject(j);
//                View ll = (View)mLayoutInflater.inflate(R.layout.imageview, null);
//                String url = jb.optString("thumbnail_pic");
//                mImageLoader.showImage((ImageView)ll.findViewById(R.id.imgview_item), url);
//                ((TextView)ll.findViewById(R.id.textview_item)).setText(jb.optString("bn"));
//                holder.addPicll.addView(ll);
//            }

            BackOrderInnerAdapter adapter = new BackOrderInnerAdapter(mActivity, productData, mImageLoader);
            Util.setListViewHeight(holder.listview);
            holder.listview.setAdapter(adapter);

            holder.look.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(AgentActivity.intentForFragment(mActivity,
                            AgentActivity.FRAGMENT_ACCOUNT_BACK_ORDER_DETAIL)
                            .putExtra("return_id", jsonObject.optString("return_id"))
                            .putExtra("member_id", jsonObject.optString("member_id")));
                }
            });

            return view;
        }
    }

    class ViewHolder{
        TextView orderID;
        TextView date;
        TextView status;
        LinearLayout look;
        LinearLayout addPicll;
        ListView listview;
    }

}
