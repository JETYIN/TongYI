package com.qianseit.westore.activity;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshBase.OnRefreshListener;
import com.qianseit.westore.ui.pulltorefresh.lib.PullToRefreshListView;

public class RecommendFragment extends BaseDoFragment {
	private PullToRefreshListView mRefreshListView;
	private BaseAdapter mRecommendListAdapter;
	private RelativeLayout mSelectView;
	private RelativeLayout mRecommendSelection;
	private RelativeLayout mRecommendPopularity;
	private RelativeLayout mRecommendAttention;
	private JsonTask mTask;
	private LayoutInflater mInflater;

	private boolean isScrolling = false;
	private int mPageNum;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_recommend_main, null);
		mRefreshListView = (PullToRefreshListView) findViewById(R.id.recommend_listview);
		mRecommendListAdapter = new RecommendLIstAdapter();
		mRefreshListView.getRefreshableView().setAdapter(mRecommendListAdapter);

		mRecommendSelection = (RelativeLayout) findViewById(R.id.recommend_selection);
		mRecommendSelection.setOnClickListener(mSaleClickListener);
		mRecommendPopularity = (RelativeLayout) findViewById(R.id.recommend_popularity);
		mRecommendPopularity.setOnClickListener(mSaleClickListener);
		mRecommendAttention = (RelativeLayout) findViewById(R.id.recommend_attention);
		mRecommendAttention.setOnClickListener(mSaleClickListener);

		mSelectView = mRecommendSelection;
		mSelectView.setSelected(true);
		mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
		// mListView.getRefreshableView().setOnItemClickListener(
		// new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// JSONObject json = (JSONObject) view
		// .getTag(R.id.tag_object);
		// String goodsIID = json.optString("goods_id");
		// Intent intent = AgentActivity.intentForFragment(
		// mActivity, AgentActivity.FRAGMENT_GOODS_DETAIL)
		// .putExtra(Run.EXTRA_CLASS_ID, goodsIID);
		// startActivity(intent);
		//
		// }
		//
		// });
		mRefreshListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					isScrolling = false;
					mRecommendListAdapter.notifyDataSetChanged();
				} else {
					isScrolling = true;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount < 5)
					return;

				// 滚动到倒数第五个时，自动加载下一页
				if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
					loadNextPage(mPageNum);
			}
		});
		mRefreshListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadNextPage(0);
			}

			@Override
			public void onRefreshMore() {
			}
		});
		loadNextPage(mPageNum);

	}

	private void loadNextPage(int oldPageNum) {
		this.mPageNum = oldPageNum + 1;
		if (this.mPageNum == 1) {

			mRecommendListAdapter.notifyDataSetChanged();
			mRefreshListView.setRefreshing();
		}
		if (mTask != null && mTask.getStatus() == JsonTask.Status.RUNNING)
			return;
		mTask = new JsonTask();
		Run.excuteJsonTask(mTask, new GetRecommendTask());
	}

	private OnClickListener mSaleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mSelectView.setSelected(false);
			mSelectView.getChildAt(1).setVisibility(View.GONE);
			if (v == mRecommendSelection) {
				mSelectView = mRecommendSelection;
			} else if (v == mRecommendPopularity) {
				mSelectView = mRecommendPopularity;

			} else if (v == mRecommendAttention) {
				mSelectView = mRecommendAttention;

			}
			mSelectView.setSelected(true);
			mSelectView.getChildAt(1).setVisibility(View.VISIBLE);
			loadNextPage(0);
		}
	};

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private class RecommendLIstAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
			if(convertView==null){
				viewHolder=new ViewHolder();
			//	mInflater.inflate(R.id., root)
			}else{
				viewHolder=(ViewHolder)convertView.getTag();
			}			
			if(isScrolling)
				return convertView;
			
			
			return null;
		}
	}

	private class ViewHolder {
		private ImageView radioImage;
		private TextView tickerTypeTextView;
		private TextView tickerValueTextView;
		private TextView tickerNametView;
		private TextView tickerExplainView;
		private TextView tickerTimeView;

	}

	private class GetRecommendTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				hideLoadingDialog_mt();
				mRefreshListView.onRefreshComplete();
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject childs = all.optJSONObject("data");

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean req = new JsonRequestBean(
					"starbuy.index.getGroup");
			req.addParams("page_no", String.valueOf(mPageNum));
			return req;
		}
	}

}
