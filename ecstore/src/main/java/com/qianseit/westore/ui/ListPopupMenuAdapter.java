package com.qianseit.westore.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.shopex.ecstore.R;

public class ListPopupMenuAdapter extends BaseAdapter {
	Context context;
	ActionMenu items;

	public ListPopupMenuAdapter(Context context, ActionMenu items) {
		this.items = items;
		this.context = context;
	}

	@Override
	public int getCount() {
		return items == null ? 0 : items.size();
	}

	@Override
	public ActionMenuItem getItem(int position) {
		return (items == null) ? null : items.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.list_popup_window_item, null);
		}

		ActionMenuItem item = getItem(position);
		ImageView iconView = (ImageView) convertView
				.findViewById(android.R.id.icon);
		iconView.setImageDrawable(item.getIcon());

		((TextView) convertView.findViewById(R.id.list_popup_window_item_text))
				.setText(item.getTitle());

		return convertView;
	}
}