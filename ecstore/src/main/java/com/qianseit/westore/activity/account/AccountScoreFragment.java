package com.qianseit.westore.activity.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.qianseit.westore.BaseDoFragment;
import cn.shopex.ecstore.R;

public class AccountScoreFragment extends BaseDoFragment {

	public AccountScoreFragment() {
		super();
	}

	@Override
	public void init(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActionBar.setTitle(R.string.meitong_card_charge_title);
		rootView = inflater.inflate(R.layout.fragment_charge_card, null);
		findViewById(R.id.charge_card_submit).setOnClickListener(this);
	}
}
