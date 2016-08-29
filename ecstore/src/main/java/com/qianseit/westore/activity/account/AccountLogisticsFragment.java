package com.qianseit.westore.activity.account;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.MyListView;


@SuppressLint("ResourceAsColor")
public class AccountLogisticsFragment extends BaseDoFragment {
	private final int WHAT=0X100;
	private TextView mLogisticsName;
	private TextView mLogisticsId;
	private MyListView mListView;
	private MyListView top_list;
	private BaseAdapter mRouteAdapter;
	private BaseAdapter mDeliveryAdapter;
	private LayoutInflater mInflater;
	private String strId;
	private String delivery_id;
	private JSONArray  msgArray;
	int check = 0;
	private List<JSONObject> infoArray = new ArrayList<JSONObject>();
	private List<JSONObject> deliveryArray = new ArrayList<JSONObject>();
	Handler handler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(WHAT==msg.what){
				msgArray=(JSONArray)msg.obj;
//				mLogisticsName.setText("快递公司："+data.optString("logi_name"));
//				mLogisticsId.setText("运单编号："+data.optString("logi_no"));
//				delivery_id = data.optString("delivery_id");
				if(msgArray!=null&&msgArray.length()>0){
					for(int i=0;i<msgArray.length();i++)
						deliveryArray.add(msgArray.optJSONObject(i));
					mDeliveryAdapter.notifyDataSetChanged();
				}
				Run.excuteJsonTask(new JsonTask(), new GetLogisticsDetail(msgArray.optJSONObject(check).optString("delivery_id")));
			}

		}

	};
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.account_logitics_info_title);
		Intent intent=mActivity.getIntent();
		strId=intent.getStringExtra("orderId");
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
					 Bundle savedInstanceState) {
		mInflater=inflater;
		rootView=inflater.inflate(R.layout.fragment_logistics_main, null);
		rootView.setVisibility(View.GONE);
		mListView=(MyListView)findViewById(android.R.id.list);
		top_list=(MyListView)findViewById(R.id.top_list);
		mLogisticsName=(TextView)findViewById(R.id.account_logistics_name);
		mLogisticsId=(TextView)findViewById(R.id.account_logistics_id);
		LinearLayout topView=(LinearLayout)findViewById(R.id.account_logistics_top);
		Run.removeFromSuperView(topView);
		topView.setLayoutParams(new AbsListView.LayoutParams(topView
				.getLayoutParams()));
		mListView.addHeaderView(topView, null, false);
		mRouteAdapter=new RouteItemAdapter();
		mDeliveryAdapter=new DeliveryAdapter();
		mListView.setAdapter(mRouteAdapter);
		top_list.setAdapter(mDeliveryAdapter);
		Run.excuteJsonTask(new JsonTask(), new GetLogisticsTask(strId));

	}

	@Override
	public void onClick(View v) {

		super.onClick(v);
	}

	@SuppressLint("ResourceAsColor")
	public class DeliveryAdapter extends BaseAdapter {

		public DeliveryAdapter(){
			super();
		}
		@Override
		public int getCount() {
			return deliveryArray.size();
		}
		@Override
		public JSONObject getItem(int position) {
			return deliveryArray.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView =  mInflater.inflate(R.layout.logistics_item, null);
				holder = new ViewHolder();
				holder.item_selected =  (ImageButton) convertView.findViewById(R.id.item_selected );
				holder.account_logistics_name =  (TextView) convertView.findViewById(R.id.account_logistics_name );
				holder.account_logistics_id =  (TextView) convertView.findViewById(R.id.account_logistics_id);
				holder.main_top =  (RelativeLayout) convertView.findViewById(R.id.main_top);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			JSONObject data=getItem(position);
			if(check==position){
				holder.item_selected.setBackgroundResource(R.drawable.order_detail_status4_ok);
			}else {
				holder.item_selected.setBackgroundResource(R.drawable.shopping_car_unselected);
			}
			holder.account_logistics_name.setText("快递公司:"+data.optString("logi_name"));
			holder.account_logistics_id.setText("运单编号:"+data.optString("logi_no"));
			holder.main_top.setOnClickListener(new ontopCheck(position));
			return convertView;
		}

		private class ViewHolder{
			private ImageButton item_selected;
			private TextView account_logistics_name;
			private TextView account_logistics_id;
			private RelativeLayout main_top;
		}

		class ontopCheck implements View.OnClickListener{

			int position;

			public ontopCheck(int position){
				this.position = position;
			}

			@Override
			public void onClick(View v) {
				check = position;
				mDeliveryAdapter.notifyDataSetChanged();
				infoArray.clear();
				Run.excuteJsonTask(new JsonTask(), new GetLogisticsDetail(msgArray.optJSONObject(check).optString("delivery_id")));
			}
		}

	}


	@SuppressLint("ResourceAsColor")
	public class RouteItemAdapter extends BaseAdapter {

		public RouteItemAdapter(){
			super();
		}
		@Override
		public int getCount() {
			return infoArray.size();
		}
		@Override
		public JSONObject getItem(int position) {
			return infoArray.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ResourceAsColor")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView =  mInflater.inflate(R.layout.view_route_item, null);
				holder = new ViewHolder();
				holder.icon =  (ImageView) convertView.findViewById(R.id. iv_route_icon );
				holder.icon_top_line =  convertView.findViewById(R.id.icon_top_line);
				holder.icon_bottom_line =  convertView.findViewById(R.id.icon_bottom_line );
				holder.ll_bottom_line =  (LinearLayout) convertView.findViewById(R.id.ll_bottom_line );
				holder.time =  (TextView) convertView.findViewById(R.id.tv_route_time );
				holder.address =  (TextView) convertView.findViewById(R.id.tv_route_address);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			JSONObject data=getItem(position);
			holder.time.setTextColor(Color.parseColor("#999999"));
			holder.address.setTextColor(Color.parseColor("#999999"));
			if (position == 0) {
				holder.icon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.logistics_track_arrive));
				holder.icon_top_line.setVisibility(View.INVISIBLE);
				holder.icon_bottom_line.setVisibility(View.VISIBLE);
				holder.ll_bottom_line.setVisibility(View.VISIBLE);
				holder.time.setTextColor(Color.parseColor("#f04641"));
				holder.address.setTextColor(Color.parseColor("#f04641"));
			}else if (position == infoArray.size()-1) {
				holder.icon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.logistics_track_point));
				holder.icon_bottom_line.setVisibility(View.INVISIBLE);
				holder.ll_bottom_line.setVisibility(View.INVISIBLE);
				holder.icon_top_line.setVisibility(View.VISIBLE);
			}else {
				holder.icon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.logistics_track_point));
				holder.icon_top_line.setVisibility(View.VISIBLE);
				holder.icon_bottom_line.setVisibility(View.VISIBLE);
				holder.ll_bottom_line.setVisibility(View.VISIBLE);
			}

			holder.time.setText(data.optString("AcceptTime"));
			holder.address.setText(data.optString("AcceptStation"));
			return convertView;
		}

		private class ViewHolder{
			private ImageView icon;
			private View icon_top_line;
			private View icon_bottom_line;
			private LinearLayout ll_bottom_line;
			private TextView time;
			private TextView address;
		}
	}

	private class GetLogisticsTask implements JsonTaskHandler {
		private String orderId;

		public GetLogisticsTask(String orderID) {
			orderId = orderID;
		}


		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray data = all.getJSONArray("data");
					if (data != null && data.length()>0) {
						rootView.setVisibility(View.VISIBLE);
							Message message = new Message();
							message.what = WHAT;
							message.obj = data;
							handler.sendMessage(message);
//					  JSONArray msgArray=data.optJSONArray("msg");
//					  if(msgArray!=null&&msgArray.length()>0){
//						  for(int i=0;i<msgArray.length();i++)
//							  infoArray.add(msgArray.optJSONObject(i));
//						  mRouteAdapter.notifyDataSetChanged();
//					  }
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.express.order_delivery");
			req.addParams("order_id", "151214183022926");
			return req;
		}
	}

	private class GetLogisticsDetail implements JsonTaskHandler {
		private String delivery_id;

		public GetLogisticsDetail(String delivery_id) {
			this.delivery_id = delivery_id;
		}


		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			JSONObject all = null;
			try {
				infoArray.clear();
				all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray data = all.getJSONArray("data");
					JSONArray msgArray=all.optJSONArray("data");
					if(msgArray!=null&&msgArray.length()>0){
						for(int i=0;i<msgArray.length();i++)
							infoArray.add(msgArray.optJSONObject(i));
						mRouteAdapter.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
				Toast.makeText(getActivity(),all.optString("data"),Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean req = new JsonRequestBean(
					"mobileapi.express.order_delivery_logisticstrack");
			req.addParams("delivery_id", delivery_id);
			return req;
		}
	}

}
