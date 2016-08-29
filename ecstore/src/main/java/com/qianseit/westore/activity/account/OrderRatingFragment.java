package com.qianseit.westore.activity.account;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;

import com.qianseit.westore.AgentApplication;
import com.qianseit.westore.BaseDoFragment;
import com.qianseit.westore.Run;
import com.qianseit.westore.SimpleTextWatcher;
import com.qianseit.westore.http.JsonRequestBean;
import com.qianseit.westore.http.JsonTask;
import com.qianseit.westore.http.JsonTaskHandler;
import com.qianseit.westore.util.ImageLoader;
import com.qianseit.westore.util.loader.VolleyImageLoader;
import cn.shopex.ecstore.R;

public class OrderRatingFragment extends BaseDoFragment {
	private ArrayList<JSONObject> mOrderGoods = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> mCommentsItems = new ArrayList<JSONObject>();

	private ListView mListView;

	private VolleyImageLoader mVolleyImageLoader;
//	private ImageLoader mImageLoader;
	private LayoutInflater mInflater;
	private String keyComment = "comment";

	public OrderRatingFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.setTitle(R.string.order_detail_rating);
//		mImageLoader = Run.getDefaultImageLoader(mActivity,
//				mActivity.getResources());
		mInflater = LayoutInflater.from(mActivity);
		mVolleyImageLoader = ((AgentApplication)mActivity.getApplication()).getImageLoader();
	}

	@Override
	public void init(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_order_rating, null);
		mListView = (ListView) findViewById(android.R.id.list);

		// mFooterView = findViewById(R.id.order_rating_footerView);
		// mCommentsTableLayout = (TableLayout) mFooterView
		// .findViewById(R.id.order_rating_tableLayout);
		// Run.removeFromSuperView(mFooterView);
		// mFooterView.setLayoutParams(new AbsListView.LayoutParams(
		// LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		Run.excuteJsonTask(new JsonTask(), new GetCommentsItemsTask());
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	private class OrdersAdapter extends BaseAdapter implements OnClickListener {
		private final int ID_SUBMIT = R.id.order_rating_item_submit;

		@Override
		public int getCount() {
			return mOrderGoods.size();
		}

		@Override
		public JSONObject getItem(int position) {
			return mOrderGoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.fragment_order_rating_item, null);
				convertView.findViewById(ID_SUBMIT).setOnClickListener(this);
			}

			final JSONObject product = getItem(position);
			try {
				// 同一个商品绑定到相同view，不再重复设置
				if (product == convertView.getTag())
					return convertView;

				convertView.setTag(product);
				convertView.findViewById(ID_SUBMIT).setTag(product);
				((TextView) convertView.findViewById(android.R.id.title))
						.setText(product.optString("goods_name"));
				// 用户评论的文字内容
				EditText commentText = (EditText) convertView
						.findViewById(R.id.order_rating_item_comment);
				commentText.setText(product.optString(keyComment));
				commentText.setTag(product);
				commentText.addTextChangedListener(new SimpleTextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						try {
							product.put(keyComment, s.toString());
						} catch (Exception e) {
						}
					}
				});

				// 评价分数
				TableLayout commentsTableLayout = (TableLayout) convertView
						.findViewById(R.id.order_rating_item_tableLayout);
				commentsTableLayout.removeAllViews();
				for (int i = 0; i < mCommentsItems.size(); i++) {
					JSONObject itemJson = mCommentsItems.get(i);
					final String typeKey = itemJson.optString("type_id");
					// 默认为商品打5分
					if (!product.has(typeKey))
						product.put(typeKey, 5.f);

					View rateItemView = mInflater.inflate(
							R.layout.fragment_order_rating_ratebar_item, null);
					commentsTableLayout.addView(rateItemView);

					((TextView) rateItemView
							.findViewById(R.id.order_rating_ratebar_title))
							.setText(itemJson.optString("name"));
					RatingBar ratingBar = (RatingBar) rateItemView
							.findViewById(R.id.order_rating_ratebar);
					ratingBar.setRating((float) product.optDouble(typeKey, 5));
					ratingBar.setTag(typeKey);
					ratingBar
							.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
								@Override
								public void onRatingChanged(
										RatingBar ratingBar, float rating,
										boolean fromUser) {
									try {
										product.put(typeKey, rating);
									} catch (Exception e) {
									}
								}
							});
				}
				// 缩略图
//				Uri imageUri = Uri.parse(product.optString("default_img_url"));
				ImageView thumbView = (ImageView) convertView
						.findViewById(android.R.id.icon);
//				thumbView.setTag(imageUri);
//				mImageLoader.showImage(thumbView, imageUri);
				mVolleyImageLoader.showImage(thumbView, product.optString("default_img_url"));
			} catch (Exception e) {
			}

			return convertView;
		}

		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag != null && tag instanceof JSONObject) {
				JsonTaskHandler handler = null;
				handler = new SubmitCommentsTask((JSONObject) tag);
				Run.excuteJsonTask(new JsonTask(), handler);
			}
		}
	}

	/* 获取未评价列表 */
	private class GetCommentsItemsTask implements JsonTaskHandler {
		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();
			return new JsonRequestBean(
					"mobileapi.member.nodiscuss");
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					JSONObject data = all.optJSONObject("data");
					JSONArray goods = data.optJSONArray("list");
					int length = (goods != null) ? goods.length() : 0;
					for (int i = 0; i < length; i++)
						mOrderGoods.add(goods.getJSONObject(i));

					JSONArray items = data.optJSONArray("comment_goods_type");
					int itemsCount = (items != null) ? items.length() : 0;
					for (int i = 0; i < itemsCount; i++) {
						JSONObject itemJson = items.getJSONObject(i);
						mCommentsItems.add(itemJson);
						// View rateItemView = mInflater.inflate(
						// R.layout.fragment_order_rating_ratebar_item,
						// null);
						// ((TextView) rateItemView
						// .findViewById(R.id.order_rating_ratebar_title))
						// .setText(itemJson.optString("name"));
						// rateItemView.setTag(itemJson);
						// mCommentsTableLayout.addView(rateItemView);
					}

					// mListView.addFooterView(mFooterView);
					mListView.setAdapter(new OrdersAdapter());
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (mOrderGoods.isEmpty())
					mListView.setEmptyView(findViewById(android.R.id.empty));
			}
		}
	}

	/* 获取未评价列表 */
	private class SubmitCommentsTask implements JsonTaskHandler {
		private JSONObject goods;

		public SubmitCommentsTask(JSONObject item) {
			this.goods = item;
		}

		@Override
		public JsonRequestBean task_request() {
			showCancelableLoadingDialog();

			JSONObject point_json = new JSONObject();
			for (JSONObject itemJson : mCommentsItems) {
				try {
					String key = itemJson.optString("type_id");
					point_json.put(key, goods.optDouble(key));
				} catch (Exception e) {
				}
			}

			return new JsonRequestBean(
					"mobileapi.comment.toComment")
					.addParams("goods_id", goods.optString("goods_id"))
					.addParams("product_id", goods.optString("product_id"))
					.addParams("order_id", goods.optString("order_id"))
					.addParams("point_json", point_json.toString())
					.addParams(keyComment, goods.optString(keyComment));
		}

		@Override
		public void task_response(String json_str) {
			hideLoadingDialog_mt();
			try {
				JSONObject all = new JSONObject(json_str);
				if (Run.checkRequestJson(mActivity, all)) {
					Run.alert(mActivity, all.optString("data"));
					mOrderGoods.remove(goods);
					((BaseAdapter) mListView.getAdapter())
							.notifyDataSetChanged();
				}
			} catch (Exception e) {
			}
		}
	}
}
