package com.qianseit.westore.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.DoActivity;
import com.qianseit.westore.Run;
import com.qianseit.westore.activity.account.AccountHomeFragment;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.MyAutoUpdate;
import com.qianseit.westore.util.Util;
import cn.shopex.ecstore.R;

public class MainTabFragmentActivity extends DoActivity implements
		OnCheckedChangeListener {
//	public static final int REQUEST_CODE_FUND_FILTER = 0x1001;

//	public static final int INDEX_MAIN = 0;
//	public static final int INDEX_CATEGOTY = 1;
//	public static final int INDEX_SHOPPING_CAR = 2;
//	public static final int INDEX_PROMT = 3;
//	public static final int INDEX_ACCOUNT = 4;

	public static MainTabFragmentActivity mTabActivity;
	public int mSelectIndex = 0;

	private TabHost mTabHost;
	private RadioGroup mRadioGroup;
	private TabManager mTabManager;
	public boolean isRightMenuOpened = false;
	private ArrayList<FragmentBean> classes = new ArrayList<FragmentBean>();
	private TextView mGoodsCountTV;
	private RadioButton mRadioButton;

	// private Handler mHandler = new Handler();
	private long mLastBackDownTime = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab);
		mTabActivity = this;
		mGoodsCountTV = (TextView) findViewById(R.id.maintab_count);
		mRadioButton = (RadioButton) findViewById(R.id.tabbar3);
		this.initFragments();

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTabManager = new TabManager(this, mTabHost, android.R.id.tabcontent);
		for (int i = 0, c = classes.size(); i < c; i++) {
			FragmentBean fb = classes.get(i);
			mTabManager.addTab(
					mTabHost.newTabSpec(fb.orderTag).setIndicator(fb.orderTag),
					fb.fragment, fb.bundle);
		}

		int defaultIndex = getIntent().getIntExtra(Run.EXTRA_TAB_POSITION, 0);
		defaultIndex = (defaultIndex >= classes.size()) ? 0 : defaultIndex;
		mRadioGroup = (RadioGroup) findViewById(R.id.maintab_tab_radiogroup);
		for (int i = 0, c = mRadioGroup.getChildCount(); i < c; i++) {
			((RadioButton) mRadioGroup.getChildAt(i))
					.setChecked(defaultIndex == i);
			((RadioButton) mRadioGroup.getChildAt(i))
					.setOnCheckedChangeListener(this);
		}
		mRadioGroup.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){

			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				Rect viewRect = new Rect();
				mRadioButton.getGlobalVisibleRect(viewRect);
//				int r = viewRect.right - Util.dip2px(mTabActivity,38);
//				int t = viewRect.top - Util.dip2px(mTabActivity, 30);
				boolean isShow = false;
				if (mGoodsCountTV.getVisibility() == View.VISIBLE) {
					isShow = true;
				}
//				mGoodsCountTV.setX(r);
//				mGoodsCountTV.setY(t);
				if (!isShow) {
					mGoodsCountTV.setVisibility(View.INVISIBLE);
				}
				mRadioGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}});
		
		new JsonTask().execute(new UpdateTask());//检测更新

		// 检测第一个可以初始化打开的Fragment，并选中
		mTabHost.setCurrentTabByTag(classes.get(defaultIndex).orderTag);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mSelectIndex != 0) {
			((RadioButton) mRadioGroup.getChildAt(mSelectIndex)).setChecked(true);
			mTabHost.setCurrentTabByTag(classes.get(mSelectIndex).orderTag);
			mSelectIndex = 0;
		}
		if (AgentApplication.getLoginedUser(this).isLogined()){
			Run.excuteJsonTask(new JsonTask(), new GetCarCountTask());
		}else{
			mGoodsCountTV.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mTabActivity = null;
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.custom_dialog_disappear);
	}

	/**
	 * 选中tab
	 * 
	 * @param index
	 */
	public void setCurrentTabByIndex(int index) {
		mRadioGroup.check(mRadioGroup.getChildAt(index).getId());
	}

	@Override
	protected void onActivityResult(int req, int res, Intent data) {
		super.onActivityResult(req, res, data);
	}

	private void initFragments() {
		classes.add(new FragmentBean(MainShoppingFragment.class,
				R.string.tabbar_title1, R.drawable.tabbar_logo1, "tabbar1",
				null));
		classes.add(new FragmentBean(PromotionsFragment.class,
				R.string.tabbar_title2, R.drawable.tabbar_logo2, "tabbar2",
				null));
		classes.add(new FragmentBean(TestShoppingCarFragment.class,
				R.string.tabbar_title3, R.drawable.tabbar_logo3, "tabbar3",
				null));
		classes.add(new FragmentBean(AccountHomeFragment.class,
				R.string.tabbar_title4, R.drawable.tabbar_logo4, "tabbar4",
				null));	
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			int resid = buttonView.getId();
			if (resid == R.id.tabbar1) {
				mTabHost.setCurrentTabByTag(classes.get(0).orderTag);
			} else if (resid == R.id.tabbar2) {
				mTabHost.setCurrentTabByTag(classes.get(1).orderTag);
			} else if (resid == R.id.tabbar3) {
				mTabHost.setCurrentTabByTag(classes.get(2).orderTag);
			} else if (resid == R.id.tabbar4) {
				mTabHost.setCurrentTabByTag(classes.get(3).orderTag);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int key, KeyEvent e) {
		Run.log("repeat count:", e.getRepeatCount());
		if (key == KeyEvent.KEYCODE_BACK && e.getRepeatCount() == 0) {
			long now = System.currentTimeMillis();
			// 点击Back键提示退出程序
			if (now - mLastBackDownTime > 3000) {
				mLastBackDownTime = now;
				Run.alert(this, R.string.exit_message);
			} else {
				this.finish();
			}
			return true;
		}

		return super.onKeyDown(key, e);
	}

	public static class FragmentBean {
		// SlidingMenu主内容
		private Class fragment;
		// 标题
		private int title;
		// 图标
		private int icon;
		// 用于标识排序位置的tag
		private String orderTag;
		// fragment参数列表
		private Bundle bundle;

		public FragmentBean(Class fragment, int tip, int title, int icon,
				String orderTag) {
			this(fragment, title, icon, orderTag, null);
		}

		public FragmentBean(Class fragment, int title, int icon,
				String orderTag, Bundle args) {
			this.fragment = fragment;
			this.orderTag = orderTag;
			this.title = title;
			this.icon = icon;
			this.bundle = args;
		}

		public String getOrderTag() {
			return orderTag;
		}

		public int getTitle() {
			return title;
		}

		public int getIcon() {
			return icon;
		}
	}

	/**
	 * This is a helper class that implements a generic mechanism for
	 * associating fragments with the tabs in a tab host. It relies on a trick.
	 * Normally a tab host has a simple API for supplying a View or Intent that
	 * each tab will show. This is not sufficient for switching between
	 * fragments. So instead we make the content part of the tab host 0dp high
	 * (it is not shown) and the TabManager supplies its own dummy view to show
	 * as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct fragment shown in a separate content area whenever
	 * the selected tab changes.
	 */
	public static class TabManager implements TabHost.OnTabChangeListener {
		private final FragmentActivity mActivity;
		private final TabHost mTabHost;
		private final int mContainerId;
		private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();

		private OnTabChangeListener mTabChangeListener;
		private TabInfo mLastTab;

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private Fragment fragment;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(FragmentActivity activity, TabHost tabHost,
				int containerId) {
			mActivity = activity;
			mTabHost = tabHost;
			mContainerId = containerId;
			mTabHost.setOnTabChangedListener(this);
		}

		/**
		 * TabHost切换监听器
		 * 
		 * @param mTabChangeListener
		 */
		public void setOnTabChangeListener(
				OnTabChangeListener mTabChangeListener) {
			this.mTabChangeListener = mTabChangeListener;
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mActivity));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			info.fragment = mActivity.getSupportFragmentManager()
					.findFragmentByTag(tag);
			if (info.fragment != null && !info.fragment.isDetached()) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager()
						.beginTransaction();
				ft.detach(info.fragment);
				ft.commitAllowingStateLoss();
			}

			mTabs.put(tag, info);
			mTabHost.addTab(tabSpec);
		}

		@Override
		public void onTabChanged(String tabId) {
			// 自定义Tab切换事件
			if (mTabChangeListener != null)
				mTabChangeListener.onTabChanged(tabId);

			TabInfo newTab = mTabs.get(tabId);
			if (mLastTab != newTab) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager()
						.beginTransaction();
				if (mLastTab != null) {
					if (mLastTab.fragment != null) {
						ft.detach(mLastTab.fragment);
					}
				}
				if (newTab != null) {
					if (newTab.fragment == null) {
						newTab.fragment = Fragment.instantiate(mActivity,
								newTab.clss.getName(), newTab.args);
						ft.add(mContainerId, newTab.fragment, newTab.tag);
					} else {
						ft.attach(newTab.fragment);
					}
				}

				mLastTab = newTab;
				ft.commitAllowingStateLoss();
				mActivity.getSupportFragmentManager()
						.executePendingTransactions();
			}
		}
	}
	
	private class GetCarCountTask implements JsonTaskHandler {

		@Override
		public void task_response(String json_str) {
			try {
				if(json_str.contains("need_login")){
					return;
				}
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mTabActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					JSONObject object = data.optJSONObject("object");
					JSONArray goods = object.optJSONArray("goods");
//					if (goods != null && goods.length() > 0) {
//						mGoodsCountTV.setVisibility(View.VISIBLE);
//						mGoodsCountTV.setText(String.valueOf(goods.length()));
//					}else{
//						mGoodsCountTV.setVisibility(View.INVISIBLE);
//					}
					int size = (goods == null) ? 0 : goods.length();
					int count = 0;
					for (int i = 0; i < size; i++) {
						count += goods.getJSONObject(i).optInt("quantity");
					}
					Run.goodsCounts = count;
					setShoppingCarCount(count);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public JsonRequestBean task_request() {
			return new JsonRequestBean( "mobileapi.cart.get_list");
		}
	}
	
	public void setShoppingCarCount(int count){
		if (count > 0) {
			mGoodsCountTV.setVisibility(View.VISIBLE);
			mGoodsCountTV.setText(String.valueOf(count));
		}else{
			mGoodsCountTV.setVisibility(View.INVISIBLE);
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};
	
	private class UpdateTask implements JsonTaskHandler {

		@Override
		public JsonRequestBean task_request() {
			JsonRequestBean jrb = new JsonRequestBean(
					"mobileapi.info.get_version");
			return jrb.addParams("os", "android");
		}

		@Override
		public void task_response(String json_str) {
			try {
				JSONObject all = new JSONObject(json_str);
				JSONObject data = all.optJSONObject("data");
				if (data == null) {
					return;
				}
				String version = data.optString("ver");
				if (TextUtils.isEmpty(version)) {
					return;
				}
				version = version.replaceAll("\\.", "");
				String oldVer = MainTabFragmentActivity.this.getString(R.string.app_version_name).replaceAll("\\.", "");
				if (version.length() > oldVer.length()) {
					for (int i = oldVer.length(); i < version.length(); i++) {
						oldVer += "0"; 
					}
				} else if (version.length() < oldVer.length()) {
					for (int i = version.length(); i < oldVer.length(); i++)
						version += "0";
				}
				int ver = Integer.parseInt(version);
				int old = Integer.parseInt(oldVer);
				if (ver > old) {
					MyAutoUpdate autoUpdate = new MyAutoUpdate(MainTabFragmentActivity.this);
					autoUpdate.checkUpdateInfo(data.optString("down"),data.optInt("ismust"),data.optString("info"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
