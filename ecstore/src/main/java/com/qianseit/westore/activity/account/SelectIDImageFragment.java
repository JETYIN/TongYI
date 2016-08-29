package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.AgentActivity;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public class SelectIDImageFragment extends BaseDoFragment {

	public final int REQUEST_ADD_IDCARD = 0x14;
	private ArrayList<JSONObject> mIdData = new ArrayList<JSONObject>();
	private BaseAdapter mSelectIdAdapter;
	private LayoutInflater mLayoutInflater;
	private PullToRefreshListView mListView;
	private Button mIdAddBut;
	private LinearLayout mIdAddLinear;
	// private int mSelectPosit = -1;
	private int mPageNum;
	private JsonTask mTask;
	private JSONObject delectDate = null;
	private VolleyImageLoader mVolleyImageLoader;
	private boolean isScrolling = false;
	private String selectID;

	private String addId;

	private Dialog mDialog;
	private String idCar;
	private boolean isFirst =true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.select_id_photo);
		mVolleyImageLoader = ((AgentApplication) mActivity.getApplication())
				.getImageLoader();
		Bundle b = getArguments();
		if (b != null) {
			addId = b.getString(Run.EXTRA_DATA);
			idCar = b.getString("idcardId");
		}
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_account_select_id_main,
				null);
		rootView.setVisibility(View.GONE);
		mListView = (PullToRefreshListView) findViewById(android.R.id.list);
		mIdAddBut = (Button) findViewById(R.id.account_add_id_but);
		mIdAddLinear = (LinearLayout) findViewById(R.id.account_add_id_linear);
		mIdAddBut.setOnClickListener(this);
		mSelectIdAdapter = new IdAdapter();
		mListView.getRefreshableView().setAdapter(mSelectIdAdapter);
		Run.removeFromSuperView(mIdAddLinear);
		mListView.getRefreshableView().addFooterView(mIdAddLinear);
//		mIdAddLinear.setVisibility(View.GONE);
		mIdAddLinear.setLayoutParams(new AbsListView.LayoutParams(mIdAddLinear
				.getLayoutParams()));
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0, false);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					isScrolling = false;
					mSelectIdAdapter.notifyDataSetChanged();
				} else {
					isScrolling = true;
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {// 身份证信息不多不用分页，接口也没有分页功能
				// if (firstVisibleItem > visibleItemCount) {
				// rootView.findViewById(R.id.fragment_select_id_goto_top)
				// .setVisibility(View.VISIBLE);
				// } else {
				// rootView.findViewById(R.id.fragment_select_id_goto_top)
				// .setVisibility(View.GONE);
				// }
				// if (totalItemCount < 5)
				// return;
				// // 滚动到倒数第五个时，自动加载下一页
				// if (totalItemCount - (firstVisibleItem + visibleItemCount) <=
				// 5)
				// loadNextPage(mPageNum);
			}
		});
		loadNextPage(0, true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK
				&& REQUEST_ADD_IDCARD == requestCode) {
			selectID = data.getStringExtra(Run.EXTRA_VALUE);
			// if (!TextUtils.isEmpty(ss)) {
			// try {
			// JSONObject re = new JSONObject(ss);
			// selectID = re.optJSONObject("data").optString("id");
			// mActivity.setResult(Activity.RESULT_OK, new Intent());
			// mActivity.finish();
			// } catch (JSONException e) {
			// e.printStackTrace();
			// }
			// }
			// loadNextPage(0);
			mActivity.setResult(Activity.RESULT_OK,
					new Intent().putExtra("idcardId", selectID));
			mActivity.finish();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// mActivity.setResult(Activity.RESULT_OK);
	}

	// 加载下一页
	private void loadNextPage(int oldPageNum, boolean isShow) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {
			mIdData.clear();
			mSelectIdAdapter.notifyDataSetChanged();
			if (!isShow)
				mListView.setRefreshing();

		}
		if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
			return;

		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetIdTask(isShow));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.account_add_id_but:
			startActivityForResult(
					AgentActivity.intentForFragment(mActivity,
							AgentActivity.FRAGMENT_UPLOADING_ID).putExtra(
							Run.EXTRA_DATA, addId), REQUEST_ADD_IDCARD);
			break;

		default:
			break;
		}
	}

	private class IdAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {

			return mIdData.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mIdData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int mposition = position;
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mLayoutInflater.inflate(
						R.layout.fragment_account_id_item, null);
				viewHolder.radioImage = (ImageView) convertView
						.findViewById(R.id.my_id_item_default);
				viewHolder.frontImage = (ImageView) convertView
						.findViewById(R.id.my_id_item_front);
				viewHolder.contraryImage = (ImageView) convertView
						.findViewById(R.id.my_id_item_contrary);
				viewHolder.delectIdImage = (ImageView) convertView
						.findViewById(R.id.my_id_item_delect);
				viewHolder.idNumView = (TextView) convertView
						.findViewById(R.id.my_id_item_num);
				viewHolder.idNametView = (TextView) convertView
						.findViewById(R.id.my_id_item_name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final JSONObject data = getItem(position);
			// if (isScrolling)
			// return convertView;
			// if (position == mSelectPosit) {
			// viewHolder.radioImage
			// .setImageResource(R.drawable.my_address_book_default);
			// } else {
			// viewHolder.radioImage
			// .setImageResource(R.drawable.my_address_book_not_default);
			// }
			if (TextUtils.equals(idCar, data.optString("id")) && isFirst) {
				selectID = data.optString("id");
				isFirst = false;
			}
			if (TextUtils.equals(selectID, data.optString("id"))) {
				viewHolder.radioImage
						.setImageResource(R.drawable.my_address_book_default);
			} else {
				viewHolder.radioImage
						.setImageResource(R.drawable.my_address_book_not_default);
			}
			mVolleyImageLoader.showImage(viewHolder.frontImage,
					data.optString("forward_url"));
			mVolleyImageLoader.showImage(viewHolder.contraryImage,
					data.optString("back_url"));
			viewHolder.radioImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// mSelectPosit=mposition;
					selectID = data.optString("id");
					new JsonTask().execute(new UploadIdTask(selectID));
					notifyDataSetChanged();
				}
			});
			viewHolder.delectIdImage.setOnClickListener(this);
			viewHolder.delectIdImage.setTag(data);
			viewHolder.contraryImage.setOnClickListener(this);
			viewHolder.idNametView.setText("姓名：" + data.optString("real_name"));
			viewHolder.idNumView.setText("身份证号码："
					+ (("null".equals(data.optString("card_num"))) ? "" : data
							.optString("card_num")));
			return convertView;
		}

		@Override
		public void onClick(View v) {
			delectDate = (JSONObject) v.getTag();
			mDialog = AccountLoginFragment.showAlertDialog(mActivity,
					"确定删除此身份证信息？", "取消", "确定", null, new OnClickListener() {

						@Override
						public void onClick(View v) {
							Run.excuteJsonTask(new JsonTask(),
									new DelectIdTask());
							mDialog.dismiss();
						}
					}, false, null);

		}
	}

	private class GetIdTask implements JsonTaskHandler {
		boolean isShow;

		public GetIdTask(boolean isShow) {
			this.isShow = isShow;
		}

		@Override
		public JsonRequestBean task_request() {
			if (isShow)
				showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.idcards").addParams("n_page",
					String.valueOf(mPageNum));
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			rootView.setVisibility(View.VISIBLE);
			mListView.onRefreshComplete();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONArray child = all.optJSONArray("data");
					if (child != null && child.length() > 0) {
						for (int i = 0, c = child.length(); i < c; i++)
							mIdData.add(child.getJSONObject(i));
						mSelectIdAdapter.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
//				mIdAddLinear.setVisibility(View.VISIBLE);
			}
		}

	}

	private class DelectIdTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.del_idcard");
			if (delectDate != null) {
				bean.addParams("id", delectDate.optString("id"));
			}
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			mListView.onRefreshComplete();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					loadNextPage(0, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

	}

	private class UploadIdTask implements JsonTaskHandler {

		private String idcardId;

		public UploadIdTask(String idcardId) {

			this.idcardId = idcardId;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			JsonRequestBean bean = new JsonRequestBean(
					"mobileapi.member.save_idcard");
			bean.addParams("addr_id", addId);
			bean.addParams("id", idcardId);
			return bean;
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Intent intent = new Intent();
					intent.putExtra("idcardId", idcardId);
					mActivity.setResult(Activity.RESULT_OK, intent);
					mActivity.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

	}

	private class ViewHolder {
		private ImageView radioImage;
		private TextView idNametView;
		private TextView idNumView;
		private ImageView frontImage;
		private ImageView delectIdImage;
		private ImageView contraryImage;

	}
}
