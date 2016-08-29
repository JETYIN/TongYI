package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleTextWatcher;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.MyGroupContainer;
import cn.shopex.ecstore.R;

public class SearchFragment extends BaseDoFragment implements
		OnCheckedChangeListener {
	private final String KEY_GOODS_SERCH_HISTORY = "goods_serach_history";
	private final String KEYWORDS_SEPARATOR = ",";

	private EditText mKeywordsText;
	private MyGroupContainer mContentViews;
	private ListView mAssociateListView;

	private JsonTask mAssociateTask;
	private boolean isFromGoodsList;
	private SharedPreferences sp;
	
	private int index = 0;

	private ArrayList<String> mGoodsKeywords = new ArrayList<String>();
	private ArrayList<String> mHotKeywords = new ArrayList<String>();
	private ArrayList<String> mAssociateKeywords = new ArrayList<String>();

	public SearchFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setShowTitleBar(false);

		rootView = inflater.inflate(R.layout.fragment_search, null);
		mAssociateListView = (ListView) findViewById(android.R.id.list);
		mAssociateListView.setAdapter(new AssKeywordsAdapter());

		mContentViews = (MyGroupContainer) findViewById(android.R.id.content);
		mKeywordsText = (EditText) findViewById(android.R.id.edit);
		Bundle b = getArguments();
		if (b != null) {
			String keyword = b.getString(Run.EXTRA_KEYWORDS);
			isFromGoodsList = b.getBoolean("com.qianseit.westore.EXTRA_METHOD");
			if (!TextUtils.isEmpty(keyword)) {
				mKeywordsText.setText(keyword);
				mKeywordsText.setSelection(keyword.length());
			}
		}
		findViewById(R.id.search_history_clear).setOnClickListener(this);
		findViewById(R.id.search_hot_goods_refresh).setOnClickListener(this);

		((RadioButton) findViewById(R.id.search_history))
				.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.search_hot_goods))
				.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.search_history)).setChecked(true);

		mKeywordsText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					startSearchAction(mKeywordsText.getText().toString());
					return true;
				}
				return false;
			}
		});

		mAssociateListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startSearchAction(mAssociateKeywords.get(position));
			}
		});
		mKeywordsText.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.isEmpty(s.toString())) {
					mAssociateListView.setVisibility(View.INVISIBLE);
					return;
				}

				// 取消前一个任务
				if (mAssociateTask != null && mAssociateTask.isExcuting)
					mAssociateTask.cancel(true);
				mAssociateTask = new JsonTask();
				Run.excuteJsonTask(mAssociateTask,
						new SearchAssociateTask(s.toString()));
				mAssociateListView.setVisibility(View.VISIBLE);
			}
		});

		findViewById(R.id.fragment_search_cancel).setOnClickListener(this);
		findViewById(R.id.fragment_search_search).setOnClickListener(this);

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			findViewById(R.id.search_history_clear).setVisibility(View.GONE);
			findViewById(R.id.search_history_closely_label).setVisibility(
					View.GONE);
			findViewById(R.id.search_hot_goods_refresh)
					.setVisibility(View.GONE);
			if (buttonView.getId() == R.id.search_history) {
				findViewById(R.id.search_history_clear).setVisibility(
						View.VISIBLE);
				findViewById(R.id.search_history_closely_label).setVisibility(
						View.VISIBLE);
				loadKeywordsHistory();
			} else if (buttonView.getId() == R.id.search_hot_goods) {
				findViewById(R.id.search_hot_goods_refresh).setVisibility(
						View.VISIBLE);
				// 加载热门关键词       一次访问取回所有的热门关键字
				mContentViews.removeAllViews();
				if (mHotKeywords.size() > 0) {
					addKeywordView(mHotKeywords,0);
				}else{
					Run.excuteJsonTask(new JsonTask(), new HotSearchTask());
				}
			}
		}
	}

	// 开始搜索
	private void startSearchAction(String keywords) {
		if (!TextUtils.isEmpty(keywords)) {
			if (mGoodsKeywords.contains(keywords)) {
				mGoodsKeywords.remove(keywords);
			}
			mGoodsKeywords.add(0, keywords);
//			mActivity.startActivity(AgentActivity
//					.intentForFragment(mActivity,
//							AgentActivity.FRAGMENT_GOODS_LIST)
//					.putExtra(Run.EXTRA_KEYWORDS, keywords)
//					.putExtra(Run.EXTRA_TITLE, keywords));
//			mActivity.finish();
			saveSearchHistory(KEY_GOODS_SERCH_HISTORY, mGoodsKeywords);
			if (isFromGoodsList) {
				Intent data = new Intent();
				data.putExtra(Run.EXTRA_KEYWORDS, keywords);
				mActivity.setResult(Activity.RESULT_OK, data);
			}else{
				mActivity.startActivity(AgentActivity
						.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_GOODS_LIST)
								.putExtra(Run.EXTRA_KEYWORDS, keywords)
								.putExtra(Run.EXTRA_TITLE, keywords));
			}
			mActivity.finish();
		}
	}

	/* 加载搜索历史记录 */
	private void loadKeywordsHistory() {
		// 商品搜索历史
		mGoodsKeywords.clear();
		String keywords = Run.loadOptionString(mActivity,
				KEY_GOODS_SERCH_HISTORY, "");
		if (!TextUtils.isEmpty(keywords))
			mGoodsKeywords.addAll(Arrays.asList(keywords
					.split(KEYWORDS_SEPARATOR)));

		addKeywordView(mGoodsKeywords,1);
	}

	/**
	 * 
	 * @param keywords
	 * @param type 0: 热门关键字每次只显示10    1:搜索历史全部显示
	 */
	private void addKeywordView(ArrayList<String> keywords , int type) {
		index = 0;
		if (type == 1) {
			mContentViews.removeAllViews();
			LayoutInflater inf = mActivity.getLayoutInflater();
			for (String text : keywords) {
				View view = inf.inflate(R.layout.fragment_search_item, null);
				((TextView) view.findViewById(android.R.id.text1)).setText(text);
				view.findViewById(android.R.id.text1).setOnClickListener(
						mItemClickListener);
				mContentViews.addView(view);
			}
		} else {
			setShowTextList();
		}
	}
	
	/**
	 * 获取接下来的要显示的热门关键字
	 * @author chesonqin
	 * 2014-11-25
	 */
	private void setShowTextList(){
		mContentViews.removeAllViews();
		int row = mHotKeywords.size() / 10 + (mHotKeywords.size() % 10 == 0 ? 0 : 1);
		if (row == 0) {
			return;
		}
		ArrayList<String> showList = new ArrayList<String>(); 
		index = index % row;
		if (index == row - 1) {   
			if (mHotKeywords.size() >= 10 ) {
				for (int i = mHotKeywords.size() - 10; i < mHotKeywords.size(); i ++) {
					System.out.print(mHotKeywords.get(i) + "   ");
					showList.add(mHotKeywords.get(i));
				}
			}else{
				for (int i = index * 10; i < mHotKeywords.size(); i ++) {
					showList.add(mHotKeywords.get(i));
				}
			}
		} else {
			for (int i = index * 10; i < (index + 1) * 10; i++) {
				showList.add(mHotKeywords.get(i));
			}
		}
		
		LayoutInflater inf = mActivity.getLayoutInflater();
		for (String text : showList) {
			View view = inf.inflate(R.layout.fragment_search_item, null);
			((TextView) view.findViewById(android.R.id.text1)).setText(text);
			view.findViewById(android.R.id.text1).setOnClickListener(
					mItemClickListener);
			mContentViews.addView(view);
		}
		index++;
	}

	private OnClickListener mItemClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String keyword = ((TextView) v).getText().toString();
			if (mGoodsKeywords.contains(keyword)) {
				mGoodsKeywords.remove(keyword);
			}
			mGoodsKeywords.add(0, keyword);
			saveSearchHistory(KEY_GOODS_SERCH_HISTORY, mGoodsKeywords);
			if (isFromGoodsList) {
				Intent data = new Intent();
				data.putExtra(Run.EXTRA_KEYWORDS, keyword);
				mActivity.setResult(Activity.RESULT_OK, data);
			}else{
				mActivity.startActivity(AgentActivity
						.intentForFragment(mActivity,
								AgentActivity.FRAGMENT_GOODS_LIST)
								.putExtra(Run.EXTRA_KEYWORDS, keyword)
								.putExtra(Run.EXTRA_TITLE, keyword));
			}
			mActivity.finish();
		}
	};

	@Override
	public void onStop() {
		super.onStop();

//		saveSearchHistory(KEY_GOODS_SERCH_HISTORY, mGoodsKeywords);
	}

	// 保存搜索历史
	private void saveSearchHistory(String key, ArrayList<String> keywords) {
		String savedKeywords = Run.EMPTY_STR;
//		for (int i = 0, c = keywords.size(); i < 10 && i < c; i++) {
//			savedKeywords = Run.buildString(savedKeywords,
//					(i == 0) ? Run.EMPTY_STR : KEYWORDS_SEPARATOR, keywords
//							.get(i).replaceAll(KEYWORDS_SEPARATOR, ""));
//		}
		for (int i = 0, c = keywords.size(); i < c; i++) {
			savedKeywords = Run.buildString(savedKeywords,
					(i == 0) ? Run.EMPTY_STR : KEYWORDS_SEPARATOR, keywords
							.get(i).replaceAll(KEYWORDS_SEPARATOR, ""));
		}
		Run.savePrefs(mActivity, key, savedKeywords);

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fragment_search_cancel) {
			mActivity.finish();
		} else if (v.getId() == R.id.fragment_search_search) {
			startSearchAction(mKeywordsText.getText().toString());
		} else if (v.getId() == R.id.search_hot_goods_refresh) {
			// 加载热门关键词    刚进来时候已经把所有关键字加载进来了
			//Run.excuteJsonTask(new JsonTask(), new HotSearchTask());
			setShowTextList();
		} else if (v.getId() == R.id.search_history_clear) {
			mGoodsKeywords.clear();
			mContentViews.removeAllViews();
			saveSearchHistory(KEY_GOODS_SERCH_HISTORY, mGoodsKeywords);
		} else {
			super.onClick(v);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Run.showSoftInputMethod(mActivity, mKeywordsText);
			}
		}, 500);
	}

	private class HotSearchTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.keywords.get_all_list");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				JSONObject data = all.optJSONObject("data");
				JSONArray array = data.optJSONArray("items");
				mHotKeywords.clear();
				for (int i = 0, len = array.length(); i < len; i++)
					mHotKeywords.add(array.getJSONObject(i)
							.optString("kw_name"));
				addKeywordView(mHotKeywords,0);
			} catch (Exception e) {
			}
		}
	}

	private class SearchAssociateTask implements JsonTaskHandler {
		private String words = Run.EMPTY_STR;

		public SearchAssociateTask(String words) {
			this.words = words;
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean(
					"mobileapi.keywords.associate").addParams("words",
					this.words);
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				JSONArray array = all.optJSONArray("data");
				mAssociateKeywords.clear();
				for (int i = 0, len = array.length(); i < len; i++)
					mAssociateKeywords.add(array.optString(i));
				((BaseAdapter) mAssociateListView.getAdapter())
						.notifyDataSetChanged();
			} catch (Exception e) {
			}
		}
	}

	public class AssKeywordsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mAssociateKeywords.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(
						R.layout.item_help_centre, null);
				convertView.findViewById(R.id.item_help_centre_arrow)
						.setVisibility(View.INVISIBLE);
			}

			TextView tv = (TextView) convertView
					.findViewById(R.id.item_help_centre_title);
			tv.setText(mAssociateKeywords.get(position));
			convertView.findViewById(R.id.account_home_item_divider_martop)
					.setVisibility(View.GONE);
			convertView.findViewById(R.id.account_home_item_divider_b2)
					.setVisibility(View.GONE);
			convertView.findViewById(R.id.account_home_item_divider_t)
					.setVisibility(View.GONE);
			if (position == 0) {
				convertView.findViewById(R.id.account_home_item_divider_t)
						.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.account_home_item_divider_b1)
						.setVisibility(View.VISIBLE);
				if (getCount() == 1) {
					convertView.findViewById(R.id.account_home_item_divider_b1)
							.setVisibility(View.GONE);
					convertView.findViewById(R.id.account_home_item_divider_b2)
							.setVisibility(View.VISIBLE);
				}
			} else if (position == getCount() - 1) {
				convertView.findViewById(R.id.account_home_item_divider_b1)
						.setVisibility(View.GONE);
				convertView.findViewById(R.id.account_home_item_divider_b2)
						.setVisibility(View.VISIBLE);
			}

			return convertView;
		}

	}

}
