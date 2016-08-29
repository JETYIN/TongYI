package com.qianseit.westore.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.shopex.ecstore.R;

public class NewCountDownView extends LinearLayout {

	private TextView mHourTextView;
	private TextView mMinuteTextView;
	private TextView mSecondTextView;
	private TextView mDayCountTv;
	private TextView mDayUnitTv;

	// 计时器
	private Timer timer;
	private long times;
	private TimeEndListener mTimeEndListener;

	public NewCountDownView(Context context) {
		super(context);
		init(context);
	}

	public NewCountDownView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			countDown();
		};
	};

	private void init(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.item_new_countdown_view, this);
		mHourTextView = (TextView) view
				.findViewById(R.id.item_new_countdown_view_hour);
		mMinuteTextView = (TextView) view
				.findViewById(R.id.item_new_countdown_view_minute);
		mSecondTextView = (TextView) view
				.findViewById(R.id.item_new_countdown_view_second);
		mDayCountTv = (TextView) view.findViewById(R.id.item_new_countdown_view_day);
		mDayUnitTv = (TextView) view.findViewById(R.id.item_new_countdown_view_dayname);
	}

	public void start() {
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					handler.sendEmptyMessage(0);
				}
			}, 0, 1000);
		}
	}

	public void setTimeNew(long remainTimes) {
		this.times = remainTimes;
		if (times <= 0) {
			return;
		}
		long hours = remainTimes / (60 * 60) % 24;
		long minutes = remainTimes / 60 % 60;
		long seconds = remainTimes % 60;
		if (remainTimes > 86400) {
			mDayCountTv.setVisibility(View.VISIBLE);
			mDayUnitTv.setVisibility(View.VISIBLE);
			mDayCountTv.setText(""+(remainTimes / (60 * 60 * 24)));
		} else {
			mDayCountTv.setVisibility(View.GONE);
			mDayUnitTv.setVisibility(View.GONE);
		}
		if (hours < 10) {
			mHourTextView.setText("0" + hours);
		} else {
			mHourTextView.setText("" + hours);
		}
		if (minutes < 10) {
			mMinuteTextView.setText("0" + minutes);
		} else {
			mMinuteTextView.setText("" + minutes);
		}
		if (seconds < 10) {
			mSecondTextView.setText("0" + seconds);
		} else {
			mSecondTextView.setText("" + seconds);
		}
	}
	
	private void countDown() {
		times -= 1;
		if (times <= 0) {
			if (mTimeEndListener != null) {
				mTimeEndListener.isTimeEnd();
			}
			stop();
			return;
		}
		setTimeNew(times);
	}

	private void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			mHourTextView.setText("00");
			mMinuteTextView.setText("00");
			mSecondTextView.setText("00");
		}
	}

	public void setTimeEndListener(TimeEndListener listener) {
		mTimeEndListener = listener;
	}

	public interface TimeEndListener {
		public void isTimeEnd();
	}

}
