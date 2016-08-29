package com.qianseit.westore.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.Run;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.ui.CircleFlowIndicator;
import com.qianseit.westore.ui.FlowView;
import com.qianseit.westore.ui.LazyScrollView;
import com.qianseit.westore.ui.RushBuyCountDownTimerView;
import com.qianseit.westore.ui.fall.StaggeredGridView;
import com.qianseit.westore.ui.fall.SwipeRefreshAndLoadLayout;
import com.qianseit.westore.util.loader.VolleyImageLoader;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.shopex.ecstore.R;

/**
 * Created by newadmin on 2016/2/2.
 */
public class MainShoppingFragment2 extends Fragment implements SwipeRefreshAndLoadLayout.OnRefreshListener, View.OnClickListener{

    private final int INTERVAL_AUTO_SNAP_FLOWVIEW = 5000;
    private final int TIME_AUTO_INCREASE = 1000;

    private ArrayList<JSONObject> mTopAdsArray = new ArrayList<JSONObject>();
    private ArrayList<JSONObject> mGoodsArray = new ArrayList<JSONObject>();

    private LayoutInflater mLayoutInflater;
    private Point mScreenSize;
    private VolleyImageLoader mVolleyImageLoader;

    //private PullToRefreshListView mListView;
    private LazyScrollView waterfall_scroll;
    private LinearLayout left;
    private LinearLayout right;
    private double leftHeight;
    private double rightHeight;
    private int itemWidth;
    // true表示两列,false表示1列
    private boolean flag = true;


    private FlowView mTopAdsView;

    private int mPageNum = 0;
//	private boolean isScrolling = false;
    // private int lastScrollY;

    private boolean isCalcel = false;

    private GoodtAdapter mGoodsAdapter;
    private View mAdsLayoutView;
    // private CircleFlowIndicator mTopAdsIndicator;
    private View mAdvertisementView;
    private TextView mAdvertisementViewText;
    private ImageView mAdvertisementViewDelect;
    private float width;
    //	private JsonTask mTask;
    private long mNewSysteTime;
    private boolean isLoadEnd;
    private boolean isLoadingData;
    private int totalCount;
    private SwipeRefreshAndLoadLayout swipeLayout;
    StaggeredGridView gridView;
    View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenSize = Run.getScreenSize(getActivity().getWindowManager());
        mLayoutInflater = getActivity().getLayoutInflater();
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        width = Float.valueOf(dm.widthPixels);
        // 自动创建桌面快捷方式
        if (!Run.loadOptionBoolean(getActivity(), Run.pk_shortcut_installed, false)) {
            Run.savePrefs(getActivity(), Run.pk_shortcut_installed, true);
            Run.createShortcut(getActivity());
        }

        mVolleyImageLoader = ((AgentApplication) getActivity().getApplication())
                .getImageLoader();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_shangchegn_main_2, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rootView.findViewById(R.id.fragment_main_category).setOnClickListener(this);
        rootView.findViewById(R.id.fragment_main_goto_top).setOnClickListener(this);
        //mListView = (PullToRefreshListView) findViewById(R.id.goods_main_listview);

        swipeLayout = (SwipeRefreshAndLoadLayout) rootView.findViewById(R.id.swipe_refresh);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setmMode(SwipeRefreshAndLoadLayout.Mode.BOTH);
        // 顶部刷新的样式
        swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright, android.R.color.holo_orange_light);

        gridView = (StaggeredGridView) rootView.findViewById(R.id.staggeredGridView1);
        gridView.setFastScrollEnabled(true);
        mGoodsAdapter = new GoodtAdapter();
        gridView.setAdapter(mGoodsAdapter);
        gridView.setVerticalScrollBarEnabled(false);
        mGoodsAdapter.notifyDataSetChanged();
        gridView.setOnItemClickListener(new StaggeredGridView.OnItemClickListener() {
            @Override
            public void onItemClick(StaggeredGridView parent, View view, int position, long id) {
                String goodsIID = mGoodsArray.get(position).optString("iid");
                Intent intent = AgentActivity.intentForFragment(
                        getActivity(), AgentActivity.FRAGMENT_GOODS_DETAIL)
                        .putExtra(Run.EXTRA_CLASS_ID, goodsIID);
                startActivity(intent);
            }
        });
        mAdsLayoutView = rootView.findViewById(R.id.fragment_main_content_container);
        mAdvertisementView = rootView.findViewById(R.id.fragment_main_advertisement);
        mAdvertisementViewText = (TextView) rootView.findViewById(R.id.fragment_main_advertisement_content);
        mAdvertisementViewDelect = (ImageView) rootView.findViewById(R.id.fragment_main_advertisement_delect);
        mAdvertisementViewDelect.setOnClickListener(this);
        mAdvertisementViewText.setFocusable(true);
        mAdvertisementViewText.requestFocus();
        rootView.findViewById(R.id.main_top_adsview_foot_flash_Sale).setOnClickListener(
                this);
        rootView.findViewById(R.id.main_top_adsview_foot_season)
                .setOnClickListener(this);
        rootView.findViewById(R.id.main_top_adsview_foot_new_product)
                .setOnClickListener(this);

        //Run.removeFromSuperView(mAdsLayoutView);
        mTopAdsView = (FlowView) rootView.findViewById(R.id.main_top_adsview);
        // mTopAdsIndicator = (CircleFlowIndicator) rootView
        // .findViewById(R.id.main_top_adsview_indicator);

//        mAdsLayoutView.setLayoutParams(new AbsListView.LayoutParams(
//                mAdsLayoutView.getLayoutParams()));

        gridView.setOnScrollListener(new StaggeredGridView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(StaggeredGridView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//					isScrolling = false;
                    mGoodsAdapter.notifyDataSetChanged();
                } else {
//					isScrolling = true;
                }
            }

            @Override
            public void onScroll(StaggeredGridView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem >= 2) {
                    if (!isCalcel) {
                        mAdvertisementView.setVisibility(View.VISIBLE);
                    }
                    rootView.findViewById(R.id.fragment_main_goto_top)
                            .setVisibility(View.VISIBLE);
                } else {
                    mAdvertisementView.setVisibility(View.GONE);
                    rootView.findViewById(R.id.fragment_main_goto_top)
                            .setVisibility(View.GONE);
                }
                if (totalItemCount < 3 || isLoadingData || isLoadEnd)
                    return;
                if (totalItemCount - (firstVisibleItem + visibleItemCount) <= 5)
                    loadNextPage(mPageNum);

            }
        });

        //rootView.findViewById(R.id.main_top_adsview_foot).setVisibility(View.GONE);
        Run.excuteJsonTask(new JsonTask(), new LoadHomeDetailsTask(true));
        Run.excuteJsonTask(new JsonTask(), new getAdvTextHandler());
//		Run.excuteJsonTask(new JsonTask(), new GetGoodsTask());
        loadNextPage(0);
    }



    private void loadNextPage(int oldPageNum) {
        if (isLoadingData || isLoadEnd) {
            swipeLayout.setRefreshing(false);
            return;
        }
        this.mPageNum = oldPageNum + 1;
        if (this.mPageNum == 1) {
            isLoadEnd = false;
            mGoodsArray.clear();
            Log.e("mGoodsArray.clear();", mGoodsArray.size() + "");
            swipeLayout.setRefreshing(false);
            mGoodsAdapter.notifyDataSetChanged();
//			mListView.setRefreshing();
        }
//		if (mTask != null && mTask.isExcuting)
//				return;
//		mTask = new JsonTask();

//		Run.excuteJsonTask(mTask, new GetGoodsTask());
        new JsonTask().execute(new GetGoodsTask());
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(1);
    }

    private void reloadMainView(boolean isOutDate) {
        if (mTopAdsArray != null && mTopAdsArray.size() > 0 && !isOutDate) {
            CircleFlowIndicator mTopAdsIndicator = (CircleFlowIndicator) rootView
                    .findViewById(R.id.main_top_adsview_indicator);
            mTopAdsView.setAdapter(new FlowAdapter());
            mTopAdsView.setFlowIndicator(mTopAdsIndicator);
            mTopAdsIndicator.setViewFlow(mTopAdsView);

            try {
                JSONObject topAdsObject = mTopAdsArray.get(0);

                // 根据屏幕和图片大小调整显示尺寸
                int width = topAdsObject.optInt("ad_img_w");
                int height = topAdsObject.optInt("ad_img_h");
                Log.e("height","----"+height);
                int viewHeight = mScreenSize.x * height / width;
                ViewGroup.LayoutParams params = mTopAdsView.getLayoutParams();
                //params.height = viewHeight;
                Log.e("viewHeight","----"+viewHeight);
                params.height = 500;
                mTopAdsView.setLayoutParams(params);

            } catch (Exception e) {
                System.out.println("---->>---e ban");
                e.printStackTrace();
            }
        } else {
            mTopAdsView.setVisibility(View.GONE);
            rootView.findViewById(R.id.main_top_adsview_indicator)
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_main_feed_back) {
            startActivity(AgentActivity.intentForFragment(getActivity(),
                    AgentActivity.FRAGMENT_FEEDBACK));
        } else if (v.getId() == R.id.fragment_main_goto_top) {
            //mListView.getRefreshableView().setSelection(0);
            gridView.setSelectionToTop();
        } else if (v.getId() == R.id.fragment_main_category) {
            startActivity(AgentActivity.intentForFragment(getActivity(),
                    AgentActivity.FRAGMENT_CATEGORY_YING));
        } else if (v.getId() == R.id.main_top_adsview_foot_season) {

            startActivity(AgentActivity.intentForFragment(getActivity(),
                    AgentActivity.FRAGMENT_SEASON_SPECIAL));
        } else if (v.getId() == R.id.main_top_adsview_foot_flash_Sale) {

            startActivity(AgentActivity.intentForFragment(getActivity(),
                    AgentActivity.FRAGMENT_FLASH_SALE));
        } else if (v.getId() == R.id.main_top_adsview_foot_new_product) {

            startActivity(AgentActivity.intentForFragment(getActivity(),
                    AgentActivity.FRAGMENT_NEW_PRODUCT));

        } else if (v.getId() == R.id.fragment_main_advertisement_delect) {
            mAdvertisementView.setVisibility(View.GONE);
            isCalcel = true;
        } else {
            //super.onClick(v);
        }
    }

    @Override
    public void onRefresh() {
        //Toast.makeText(getActivity(), "refresh", Toast.LENGTH_SHORT).show();
        loadNextPage(0);
        Run.excuteJsonTask(new JsonTask(),
                new LoadHomeDetailsTask(true));

		new Handler().postDelayed(new Runnable() {
			public void run() {
				swipeLayout.setRefreshing(false);

			}
		}, 1000);
    }

    @Override
    public void onLoadMore() {

//		Toast.makeText(getActivity(), "load", Toast.LENGTH_SHORT).show();
//		loadNextPage(mPageNum);
//		new Handler().postDelayed(new Runnable() {
//			public void run() {
//				swipeLayout.setRefreshing(false);
//
//			}
//		}, 500);

    }

    /**
     * 获取banner
     */
    public class LoadHomeDetailsTask implements JsonTaskHandler {

        private boolean noShowLoading = false;

        private LoadHomeDetailsTask() {
        }

        private LoadHomeDetailsTask(boolean noShowLoading) {
            this.noShowLoading = noShowLoading;
        }

        @Override
        public JsonRequestBean task_request() {
            if (!noShowLoading) {
                //showCancelableLoadingDialog();
            }
            JsonRequestBean req = new JsonRequestBean(
                    "mobileapi.indexad.get_ad");
            req.addParams("app_ad_key", "1");
            return req;
        }

        @Override
        public void task_response(String json_str) {
            //hideLoadingDialog_mt();
            //rootView.findViewById(R.id.main_top_adsview_foot).setVisibility(View.GONE);
            parseHomeAdsJson(json_str, true);
        }
    }

    /**
     * 解析主屏幕广告json
     *
     * @param json_str
     * @param needSave
     * @throws Exception
     */
    private void parseHomeAdsJson(String json_str, boolean needSave) {
        try {
            JSONObject all = new JSONObject(json_str);
            if (Run.checkRequestJson(getActivity(), all)) {
                JSONArray child = all.optJSONArray("data");
                if (child != null && child.length() > 0) {
                    // 顶部广告
                    mTopAdsArray.clear();
                    for (int i = 0; i < child.length(); i++) {
                        JSONObject topJson = child.getJSONObject(i);
                        mTopAdsArray.add(topJson);
                    }
                    if (mTopAdsArray.size() > 0) {
                        reloadMainView(false);
                    } else {
                        reloadMainView(true);
                    }
                    if (needSave) {
                        File cacheFile = new File(getActivity().getFilesDir(),
                                Run.FILE_HOME_ADS_JSON);
                        Run.copyString2File(json_str,
                                cacheFile.getAbsolutePath());
                    }

                } else {
                    if (needSave) // 无需缓存则不重复加载
                        loadLocalAdJson();
                }

            } else {
                if (needSave) // 无需缓存则不重复加载
                    loadLocalAdJson();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (needSave) // 无需缓存则不重复加载
                loadLocalAdJson();
        }
    }

    // 读取缓存的json
    private void loadLocalAdJson() {
        try {
            File file = new File(getActivity().getFilesDir(),
                    Run.FILE_HOME_ADS_JSON);
            String jsonStr = FileUtils.readFileToString(file);
            parseHomeAdsJson(jsonStr, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mAdViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag(R.id.tag_object) != null) {
                JSONObject data = (JSONObject) v.getTag(R.id.tag_object);
                String urlType = data.optString("url_type");

                if ("goods".equals(urlType)) {
                    if (v.getTag(R.id.tag_first) != null) {
                        startActivity(AgentActivity.intentForFragment(
                                getActivity(), AgentActivity.FRAGMENT_GOODS_DETAIL)
                                .putExtra(Run.EXTRA_PRODUCT_ID,
                                        data.optString("ad_url")));
                    } else {
                        startActivity(AgentActivity.intentForFragment(
                                getActivity(), AgentActivity.FRAGMENT_GOODS_DETAIL)
                                .putExtra(Run.EXTRA_CLASS_ID,
                                        data.optString("ad_url")));
                    }
                } else if ("article".equals(urlType)) {
                    startActivity(AgentActivity.intentForFragment(getActivity(),
                            AgentActivity.FRAGMENT_ARTICLE_READER).putExtra(
                            Run.EXTRA_ARTICLE_ID, data.optString("ad_url")));
                } else if ("virtual_cat".equals(urlType)) {
                    startActivity(AgentActivity
                            .intentForFragment(getActivity(),
                                    AgentActivity.FRAGMENT_GOODS_LIST)
                            .putExtra(Run.EXTRA_VITUAL_CATE,
                                    data.optString("ad_url"))
                            .putExtra(Run.EXTRA_TITLE,
                                    data.optString("ad_name")));
                } else if ("cat".equals(urlType)) {
                    startActivity(AgentActivity
                            .intentForFragment(getActivity(),
                                    AgentActivity.FRAGMENT_GOODS_LIST)
                            .putExtra(Run.EXTRA_CLASS_ID,
                                    data.optString("ad_url"))
                            .putExtra(Run.EXTRA_TITLE,
                                    data.optString("ad_name")));
                }
            }
        }
    };

    private class FlowAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTopAdsArray.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return mTopAdsArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                ImageView view = new ImageView(getActivity());
                view.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                view.setOnClickListener(mAdViewClickListener);
                convertView = view;
            }

            JSONObject topAdsObject = getItem(position);
            Uri imageUri = Uri.parse(topAdsObject.optString("ad_img"));
            convertView.setTag(R.id.tag_object, topAdsObject);
            convertView.setTag(imageUri);
            mVolleyImageLoader.showImage((ImageView) convertView,
                    topAdsObject.optString("ad_img"));

            return convertView;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (mTopAdsView.getViewsCount() > 1) {
                    int count = mTopAdsView.getViewsCount();
                    int curScreen = mTopAdsView.getSelectedItemPosition();
                    if (curScreen >= (count - 1))
                        mTopAdsView.smoothScrollToScreen(0);
                    else
                        mTopAdsView.smoothScrollToScreen(curScreen + 1);
                }
                mHandler.sendEmptyMessageDelayed(0, INTERVAL_AUTO_SNAP_FLOWVIEW);
            } else if(msg.what == 1){
                mNewSysteTime += 1;
                mHandler.sendEmptyMessageDelayed(1, TIME_AUTO_INCREASE);
            }
        };
    };

    public class GoodtAdapter extends BaseAdapter {

//		private ArrayList<JSONObject> mGoodsArray;
//
//		public GoodtAdapter()
//		{
//			mGoodsArray = new ArrayList<JSONObject>();
//		}

        public void addItemLast(List<JSONObject> datas) {
            mGoodsArray.addAll(datas);
        }

        @Override
        public int getCount() {
            return mGoodsArray.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return mGoodsArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
            //return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.e("---getView---", "getView");
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater
                        .inflate(R.layout.goods_item, null);
                viewHolder.rl = (RelativeLayout)convertView.findViewById(R.id.rl);
                viewHolder.imageFragme = (FrameLayout) convertView
                        .findViewById(R.id.fragment_goods_item_image);
                viewHolder.imageFragme
                        .setLayoutParams(new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, (int) (width / 2)));
                Log.e("width","width----"+width);
                viewHolder.iconImage = (NetworkImageView) convertView
                        .findViewById(R.id.fragment_goods_item_icon);
                viewHolder.titleTextView = (TextView) convertView
                        .findViewById(R.id.fragment_goods_item_title);
                viewHolder.priceTextView = (TextView) convertView
                        .findViewById(R.id.fragment_goods_item_price);
                viewHolder.statusTextView = (TextView) convertView
                        .findViewById(R.id.fragment_goods_item_status);
                viewHolder.timeTextView = (RushBuyCountDownTimerView) convertView
                        .findViewById(R.id.fragment_goods_item_time_buy);
                viewHolder.timeView = convertView
                        .findViewById(R.id.fragment_goods_item_time);
                viewHolder.soldImage = (ImageView) convertView
                        .findViewById(R.id.fragment_goods_item_sold);
                viewHolder.markPriceTextView = (TextView) convertView
                        .findViewById(R.id.fragment_goods_item_mark_price);
                viewHolder.timeTitleTextView = (TextView) convertView
                        .findViewById(R.id.fragment_goods_item_time_title);
                viewHolder.status2TextView = (TextView) convertView
                        .findViewById(R.id.fragment_goods_item_status2);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ViewGroup.LayoutParams lp = viewHolder.rl.getLayoutParams();

            if (position%2==0){
                lp.height = 700;
            }
//			else if (position == 3)
//			{
//				lp.height = 780;
//			}
            else{
                lp.height = 725;
            }
            viewHolder.rl.setLayoutParams(lp);

            JSONObject goodsObject = getItem(position);
            convertView.setTag(R.id.tag_object, goodsObject);
            viewHolder.titleTextView.setText(goodsObject.optString("title"));
            viewHolder.priceTextView.setText("￥"
                    + goodsObject.optString("price"));

//			viewHolder.iconImage.setDefaultImageResId(R.drawable.default_img_rect);
//			viewHolder.iconImage.setErrorImageResId(R.drawable.default_img_rect);
//			viewHolder.iconImage.setImageUrl(goodsObject.optString("ipad_image_url") , mVolleyImageLoader.getVImageLoader());

            mVolleyImageLoader.showImage(viewHolder.iconImage,
                    goodsObject.optString("ipad_image_url"));
            JSONArray SkusStatue = goodsObject.optJSONArray("skus");
            String strPmt = goodsObject.optString("pmt_text");
            viewHolder.markPriceTextView.setText("￥"
                    + goodsObject.optString("market_price"));
            viewHolder.markPriceTextView.getPaint().setFlags(
                    Paint.STRIKE_THRU_TEXT_FLAG);
            if (!TextUtils.isEmpty(strPmt) && !"null".equals(strPmt)) {
                String strStatue = goodsObject.optString("pmt_text").trim();
                if ("NEW".equals(strStatue)) {
                    viewHolder.status2TextView.setVisibility(View.VISIBLE);
                    viewHolder.statusTextView.setVisibility(View.GONE);
                    viewHolder.status2TextView.setText(strStatue);
                } else {
                    viewHolder.status2TextView.setVisibility(View.GONE);
                    viewHolder.statusTextView.setVisibility(View.VISIBLE);
                    viewHolder.statusTextView.setText(strStatue);
                }
            } else {
                viewHolder.statusTextView.setVisibility(View.GONE);
            }
            int store = goodsObject.optInt("store");
            if (store <= 0) {
                viewHolder.soldImage.setVisibility(View.VISIBLE);
            } else {
                viewHolder.soldImage.setVisibility(View.GONE);
            }
            if (SkusStatue != null && SkusStatue.length() > 0) {
                JSONObject statueJSON = SkusStatue.optJSONObject(0);
                if (statueJSON != null) {
                    JSONObject infoJSON = statueJSON
                            .optJSONObject("starbuy_info");
                    if (infoJSON != null) {
                        if (infoJSON.optInt("type_id") == 2
                                && statueJSON.optBoolean("is_starbuy")) {
                            String endTime = infoJSON.optString("end_time");
                            int min = 0;
                            int hour = 0;
                            int sec = 0;
                            long time = Long.parseLong(endTime) - mNewSysteTime;
                            sec = (int) time;
                            if (sec > 60) {
                                min = sec / 60;
                                sec = sec % 60;
                            }
                            if (time > 60) {
                                hour = min / 60;
                                min = min % 60;
                            }
                            if (viewHolder.timeTextView.setTime(hour, min, sec)) {
                                viewHolder.timeView.setVisibility(View.VISIBLE);
                                viewHolder.markPriceTextView
                                        .setVisibility(View.VISIBLE);
                                viewHolder.timeTitleTextView
                                        .setText(getResources().getString(
                                                R.string.goods_item_time_end));
                                viewHolder.timeTextView.start();
                            } else {
                                viewHolder.timeView.setVisibility(View.GONE);
                                viewHolder.markPriceTextView
                                        .setVisibility(View.GONE);
                            }
                        } else {
                            viewHolder.timeView.setVisibility(View.GONE);
                            viewHolder.markPriceTextView
                                    .setVisibility(View.GONE);
                        }
                    } else {
                        viewHolder.timeView.setVisibility(View.GONE);
                        viewHolder.markPriceTextView.setVisibility(View.GONE);
                    }
                } else {
                    viewHolder.timeView.setVisibility(View.GONE);
                    viewHolder.markPriceTextView.setVisibility(View.GONE);
                }
            } else {
                viewHolder.timeView.setVisibility(View.GONE);
                viewHolder.markPriceTextView.setVisibility(View.GONE);
            }
            return convertView;
        }

//		public void addItemTop(ArrayList<JSONObject> datas) {
//			for (JSONObject info : datas) {
//				mGoodsArray.addFirst(info);
//			}
//		}

    }

    private class ViewHolder {
        private RelativeLayout rl;
        private FrameLayout imageFragme;
        private NetworkImageView iconImage;
        private ImageView soldImage;
        private RushBuyCountDownTimerView timeTextView;
        private TextView titleTextView;
        private TextView priceTextView;
        private TextView markPriceTextView;
        private TextView statusTextView;
        private TextView status2TextView;
        private View timeView;
        private TextView timeTitleTextView;

    }


    /**
     * 获取商品列表
     */
    class GetGoodsTask implements JsonTaskHandler {

        @Override
        public void task_response(String json_str) {
            isLoadingData = false;
            try {
//				hideLoadingDialog_mt();
                JSONObject all = new JSONObject(json_str);
                if (Run.checkRequestJson(getActivity(), all)) {
                    JSONObject childs = all.optJSONObject("data");
                    totalCount = childs.optInt("total_results");
                    if (mGoodsArray.size() >= totalCount) {
                        isLoadEnd = true;
                    }
                    if (mPageNum == 1) {
                        mNewSysteTime = childs.optLong("system_time");
                        mHandler.sendEmptyMessageDelayed(1, TIME_AUTO_INCREASE);
                    }
                    if (childs != null) {
                        JSONObject items = childs.optJSONObject("items");
                        if (items != null) {
//							loadLocalGoods(items);
                            JSONArray item = items.optJSONArray("item");
                            if (item != null && item.length() > 0) {
                                Log.e("item.length()", item.length() + "");
                                for (int i = 0; i < item.length(); i++) {
                                    mGoodsArray.add(item.optJSONObject(i));
                                }
                                Log.e("mGoodsArray.length()", mGoodsArray.size() + "");
                                //swipeLayout.setRefreshing(false);
                                gridView.setSelection(mGoodsAdapter.getCount()-1);
                                //gridView.setVerticalScrollbarPosition(mGoodsArray.size() - 1);
                                //mGoodsAdapter.addItemLast(mGoodsArray);
                                mGoodsAdapter.notifyDataSetChanged();
                                //addView();
                            }
                        }
                    }

                }
            } catch (Exception e) {
                System.out.println("---->>---e");
                swipeLayout.setRefreshing(false);
                e.printStackTrace();
            }

        }

        @Override
        public JsonRequestBean task_request() {
            isLoadingData = true;
            JsonRequestBean req = new JsonRequestBean(
                    "mobileapi.goods.get_all_list");
            req.addParams("son_object", "json");
            req.addParams("page_no", String.valueOf(mPageNum));
            req.addParams("page_size", "20");
            return req;
        }

    }

    private void loadLocalGoods(JSONObject json) {
        JSONArray item = json.optJSONArray("item");
        if (item != null && item.length() > 0) {
            for (int i = 0; i < item.length(); i++) {
                mGoodsArray.add(item.optJSONObject(i));
            }
            //mGoodsAdapter.notifyDataSetChanged();
            //addView();
        }

    }

    class getAdvTextHandler implements JsonTaskHandler {
        public void task_response(String json_str) {
            try {
                //hideLoadingDialog_mt();
                JSONObject all = new JSONObject(json_str);
                if (Run.checkRequestJson(getActivity(), all)) {
                    JSONArray childs = all.optJSONArray("data");
                    if (childs != null && childs.length() > 0) {
                        JSONObject items = childs.optJSONObject(0);
                        if (items != null) {
                            String str = "<u>" + items.optString("ad_name")
                                    + "</u>";
                            mAdvertisementViewText.setText(Html.fromHtml(str));
                        }
                    }

                }
            } catch (Exception e) {
                System.out.println("---->>---e ads");
                e.printStackTrace();
            }

        }

        @Override
        public JsonRequestBean task_request() {
            JsonRequestBean req = new JsonRequestBean(
                    "mobileapi.indexad.get_ad");
            req.addParams("app_ad_key", "2");
            return req;
        }

    }
}
