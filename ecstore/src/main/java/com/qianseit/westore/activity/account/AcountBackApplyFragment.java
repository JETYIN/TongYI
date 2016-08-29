package com.qianseit.westore.activity.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/27.
 */
public class AcountBackApplyFragment extends BaseDoFragment {

    private VolleyImageLoader mImageLoader;

    private LayoutInflater mLayoutInflater;

    private JSONObject backApplyInfo;

    private String orderID;
    // 1:退货  2:换货
    private String type = "";

    private JSONObject applyInfo = null;
    private List<JSONObject> list = new ArrayList<JSONObject>();
    private List<JSONObject> pList = new ArrayList<JSONObject>();
    private List<Integer> countList = new ArrayList<Integer>();

    private View headView;
    private View footView;
    private ListView mListView;
    private MyAdapter myAdapter;
    private EditText titleEdit,contentEdit;
    private Button commitBtn;

    public AcountBackApplyFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar.setTitle("售后申请");
        mImageLoader = AgentApplication.getApp(mActivity).getImageLoader();
        String dataStr = mActivity.getIntent().getStringExtra("info");
        try {
            backApplyInfo = new JSONObject(dataStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject j1 = backApplyInfo.optJSONObject("goods_items");
        Iterator it = j1.keys();
        while (it.hasNext()){
            String key = it.next().toString();
            list.add(j1.optJSONObject(key).optJSONObject("product"));
        }
        orderID = backApplyInfo.optString("order_id");
        Log.e("order_id", orderID);
       // Run.excuteJsonTask(new JsonTask(), new AddAfterSaleTask((DoActivity) mActivity));
    }

    @Override
    public void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.init(inflater, container, savedInstanceState);
        mLayoutInflater = inflater;
        rootView = inflater.inflate(R.layout.acount_back_apply, null);
        headView = inflater.inflate(R.layout.acount_back_apply_head,null);
        footView = inflater.inflate(R.layout.acount_back_apply_foot,null);
        mListView = (ListView)rootView.findViewById(R.id.listview);
        mListView.addHeaderView(headView);
        mListView.addFooterView(footView);
        initHeadView();
        initFootView();
        myAdapter = new MyAdapter();
        mListView.setAdapter(myAdapter);

    }

    private void initHeadView(){
        //通过findViewById获得RadioGroup对象
        RadioGroup raGroup1=(RadioGroup)headView.findViewById(R.id.radioGroup1);
        //添加事件监听器
        raGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(checkedId==R.id.radioBtn1){
                    type = "1";
                }
                else if(checkedId==R.id.radioBtn2){
                    type = "2";
                }
            }
        });

    }

    private void initFootView(){
        commitBtn = (Button)footView.findViewById(R.id.commit);
        CheckBox check = (CheckBox)footView.findViewById(R.id.agree);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    commitBtn.setClickable(true);
                else
                    commitBtn.setClickable(false);
            }
        });
        titleEdit = (EditText) footView.findViewById(R.id.title);
        contentEdit = (EditText) footView.findViewById(R.id.content);
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if ("".equals(type)){
                   Run.alert(mActivity,"请选择售后方式");
                   return;
               }
                if (titleEdit.getText().toString().equals("")){
                    Run.alert(mActivity,"请填写申请理由");
                    return;
                }
                for (int i=0;i<mListView.getCount();i++){
                    View view2 = mListView.getChildAt(i);
                    ViewHolder holder = (ViewHolder)view2.getTag();
                    CheckBox cb = (CheckBox)view2.findViewById(R.id.chekbox);
                    EditText edit = (EditText)view2.findViewById(R.id.count);
                    Log.e("ldy","cb:"+holder.checkbox+",edit:"+edit+",view"+view2);
                    if (holder.checkbox.isChecked()){
                        pList.add(list.get(i));
                        if (edit.getText().toString().equals("")){
                            Run.alert(mActivity, "请输入退货数量");
                            return;
                        }
                        countList.add(Integer.parseInt(edit.getText().toString()));
                    }
                }
                if (pList.size() == countList.size()){
                    Run.excuteJsonTask(new JsonTask(), new ReturnSaveTask((DoActivity) mActivity,pList,countList));
                }
            }
        });

    }

    /**
     * 提交申请
     */
    class ReturnSaveTask implements JsonTaskHandler {

        private DoActivity mActivity;
        private List<JSONObject> list;
        private List<Integer> count;

        public ReturnSaveTask(DoActivity  mActivity, List<JSONObject> list, List<Integer> count){
            this.mActivity = mActivity;
            this.count = count;
            this.list = list;
        }

        @Override
        public void task_response(String json_str) {
            try {
                hideLoadingDialog_mt();
                JSONObject childs = new JSONObject(json_str);
                if (Run.checkRequestJson(mActivity, childs)) {
                   Run.alert(mActivity, "恭喜申请成功");

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
                    "mobileapi.member.return_save");
            req.addParams("order_id", orderID);
            // 1:退货  2:换货
            req.addParams("type", type);
            req.addParams("title", titleEdit.getText().toString().trim());
            req.addParams("content", contentEdit.getText().toString().trim());
            if (null != list && list.size()>0){
                for (int i=0;i<list.size();i++){

                    req.addParams("product_nums["+i+"]", count.get(i)+"");
                    req.addParams("product_name["+i+"]", list.get(i).optString("name"));
                    req.addParams("product_price["+i+"]", list.get(i).optString("price"));
                    //商品ID
                    req.addParams("product_bn["+i+"]", list.get(i).optString("product_id"));
                }
            }


            return req;
        }

    }

    /**
     * 申请售后服务
     */
    class AddAfterSaleTask implements JsonTaskHandler {

        private DoActivity  mActivity;

        public AddAfterSaleTask(DoActivity  mActivity){
            this.mActivity = mActivity;
        }

        @Override
        public void task_response(String json_str) {
            try {
                hideLoadingDialog_mt();
                JSONObject childs = new JSONObject(json_str);
                if (Run.checkRequestJson(mActivity, childs)) {
                    if (childs != null) {
                        applyInfo = childs.optJSONObject("data");
                        //initView();

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
                    "mobileapi.member.add_aftersales");
            req.addParams("order_id", orderID);
            return req;
        }

    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public JSONObject getItem(int i) {
            return list.get(i);
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
                view = mLayoutInflater.inflate(R.layout.acount_back_apply_item,null);
                holder.checkbox = (CheckBox)view.findViewById(R.id.chekbox);
                holder.pImg = (ImageView)view.findViewById(R.id.product_img);
                holder.pName = (TextView)view.findViewById(R.id.product_name);
                holder.plus = (Button)view.findViewById(R.id.plus);
                holder.minus = (Button)view.findViewById(R.id.minus);
                holder.count = (EditText)view.findViewById(R.id.count);
                holder.maxCount = (TextView)view.findViewById(R.id.maxCount);
                view.setTag(holder);
            }
            else{
                holder = (ViewHolder)view.getTag();
            }
            final EditText countEdit = holder.count;
            final JSONObject jsonObject = getItem(i);
            holder.pName.setText(jsonObject.optString("name"));
            int quantity = jsonObject.optInt("quantity");
            holder.maxCount.setText("最大" + quantity + "件");
            holder.count.setText(quantity+"");
            mImageLoader.showImage(holder.pImg, jsonObject.optString("thumbnail_pic_src"));

            holder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (countEdit.getText().toString().equals("")) {
                        countEdit.setText("1");
                    } else
                    {
                        int count = Integer.parseInt(countEdit.getText().toString());
                        if (count < jsonObject.optInt("quantity")) {
                            countEdit.setText((count++) + "");
                        }
                    }
                }
            });
            holder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!countEdit.getText().toString().equals("")){
                        int count = Integer.parseInt(countEdit.getText().toString());
                        if (count>0) {
                            countEdit.setText((count--)+"");
                        }
                    }

                }
            });

//            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    if (b) {
//                        pList.add(jsonObject);
//                    } else {
//                        pList.remove(jsonObject);
//                    }
//                }
//            });

            return view;
        }
    }
    class ViewHolder{
        CheckBox checkbox;
        ImageView pImg;
        TextView pName;
        Button minus,plus;
        EditText count;
        TextView maxCount;

    }

}
