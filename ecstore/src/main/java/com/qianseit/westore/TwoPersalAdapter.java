package com.qianseit.westore;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;
import com.qianseit.westore.util.Util;
import com.qianseit.westore.util.loader.VolleyImageLoader;

public abstract class TwoPersalAdapter extends BaseAdapter implements
		OnClickListener {
	private final int[] ITEM_IDS = { R.id.account_personal_list_item_one,
			R.id.account_personal_list_item_two };

	private ArrayList<JSONObject> mGoodsList;
	private LayoutInflater inflater;
	private Activity mActivity;
	private String userId;
	private boolean isFans = true;
	private VolleyImageLoader mIImageLoader;

	public TwoPersalAdapter(Activity activity, VolleyImageLoader imageLoader,
			ArrayList<JSONObject> items, String userId, boolean isfans) {
		this.inflater = activity.getLayoutInflater();
		this.mIImageLoader = imageLoader;
		this.mActivity = activity;
		this.userId = userId;
		this.isFans = isfans;
		if (items != null)
			this.mGoodsList = items;
		else
			this.mGoodsList = new ArrayList<JSONObject>();
	}

	@Override
	public int getCount() {
		return (int) Math.ceil(mGoodsList.size() / 2.0);
	}

	@Override
	public JSONObject getItem(int position) {
		return position >= mGoodsList.size() ? null : mGoodsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			int layout = R.layout.fragment_personal_list_item;
			convertView = inflater.inflate(layout, null);
			for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
				View childView = convertView.findViewById(ITEM_IDS[i]);
			}

		}

		for (int i = 0, c = ITEM_IDS.length; i < c; i++) {
			JSONObject all = getItem(position * c + i);
			View childView = convertView.findViewById(ITEM_IDS[i]);
			if (all != null) {
				childView.setTag(all);
				childView.setVisibility(View.VISIBLE);
				fillupItemView(childView, all);
			} else {
				childView.setVisibility(View.INVISIBLE);
			}
		}

		return convertView;
	}

	/**
	 * convertView初始化
	 * 
	 * @param convertView
	 */
	public void initConvertView(View convertView) {
	}

	/**
	 * 填充列表信息
	 * 
	 * @param convertView
	 * @param all
	 * @param key
	 */
	public void fillupItemView(View convertView, JSONObject all) {
		ImageView avdImage = (ImageView) convertView
				.findViewById(R.id.attention_item_avd);
		Button clickBut = (Button) convertView
				.findViewById(R.id.account_click_but);
		View attentionAddBut = convertView
				.findViewById(R.id.account_attention_linear);
		TextView nameText = (TextView) convertView
				.findViewById(R.id.account_user_name);
		TextView recommendText = (TextView) convertView
				.findViewById(R.id.account_user_recommend);
		TextView fansText = (TextView) convertView
				.findViewById(R.id.account_user_fans);
		nameText.setText(("null".equals(all.optString("name"))) ? "" : all
				.optString("name"));
		String opinions = all.optString("opinions_num");
		recommendText.setText(("null".equals(opinions) ? "0" : opinions)
				+ "个推荐");
		String fans = all.optString("fans_num");
		fansText.setText(("null".equals(fans) ? "0" : fans) + "个粉丝");
		avdImage.setTag(all);
		avdImage.setOnClickListener(this);
		clickBut.setOnClickListener(this);
		attentionAddBut.setOnClickListener(this);
		clickBut.setTag(all);
		attentionAddBut.setTag(all);
		avdImage.setImageResource(R.drawable.account_avatar);
		mIImageLoader.showImage(avdImage, all.optString("avatar"));
		String otherId;
		if (isFans) {
			otherId = all.optString("fans_id");
		} else {
			otherId = all.optString("member_id");
		}
		if (otherId.equals(userId)) {
			attentionAddBut.setVisibility(View.GONE);
			clickBut.setVisibility(View.VISIBLE);
			clickBut.setEnabled(false);
		} else {
			clickBut.setEnabled(true);
			if ("0".equals(all.optString("is_attention"))) {
				attentionAddBut.setVisibility(View.VISIBLE);
				clickBut.setVisibility(View.GONE);
			} else {
				attentionAddBut.setVisibility(View.GONE);
				clickBut.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private SpannableString getSpanString(){
		String str = "+关注";
		SpannableString sp = new SpannableString(str);
		ForegroundColorSpan fp = new ForegroundColorSpan(Color.parseColor(mActivity.getString(R.color.text_textcolor_gray2)));
		AbsoluteSizeSpan ap = new AbsoluteSizeSpan(Util.sp2px(mActivity, 20));
		sp.setSpan(ap, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(fp, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sp;
	}
}
