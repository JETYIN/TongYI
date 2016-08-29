package com.qianseit.westore.ui;

import java.util.Timer;
import java.util.TimerTask;

import cn.shopex.ecstore.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class RushBuyCountDownTimerView extends LinearLayout {

	private TextView tv_hour_unit;
	private TextView tv_min_unit;
	private TextView tv_sec_unit;
	private Context context;
	// ��ʱ��
	private Timer timer;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			countDown();
		};
	};

	public RushBuyCountDownTimerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_countdowntimer, this);
		tv_hour_unit = (TextView) view.findViewById(R.id.tv_hour_unit);
		tv_min_unit = (TextView) view.findViewById(R.id.tv_min_unit);
		tv_sec_unit = (TextView) view.findViewById(R.id.tv_sec_unit);

	}

	/**
	 * 
	 * @Description: ��ʼ��ʱ
	 * @param
	 * @return void
	 * @throws
	 */
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

	/**
	 * 
	 * @Description: ֹͣ��ʱ
	 * @param
	 * @return void
	 * @throws
	 */
	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	/**
	 * @throws Exception
	 * 
	 * @Description: ���õ���ʱ��ʱ��
	 * @param
	 * @return void
	 * @throws
	 */
	public boolean setTime(int hour, int min, int sec) {

		if (hour >= 60 || min >= 60 || sec >= 60 || hour < 0 || min < 0
				|| sec < 0||(hour==0&&min==0&&sec==0)) {
			return false;
			// throw new RuntimeException(
			// "Time format is error,please check out your code");
		} else {
			tv_hour_unit.setText(hour / 10 == 0 ? ("0" + hour + "")
					: (hour + ""));
			tv_min_unit.setText(min / 10 == 0 ? ("0" + min + "") : (min + ""));
			tv_sec_unit.setText(sec / 10 == 0 ? ("0" + sec + "") : (sec + ""));
			return true;
		}

	}

	/**
	 * 
	 * @Description: ����ʱ
	 * @param
	 * @return boolean
	 * @throws
	 */
	private void countDown() {

		if (isUnit(tv_sec_unit)) {
			if (isUnit(tv_min_unit)) {

				if (isUnit(tv_hour_unit)) {
					tv_hour_unit.setText("00");
					tv_min_unit.setText("00");
					tv_sec_unit.setText("00");
					stop();
				}
			}
		}

	}

	private boolean isUnit(TextView tv) {
		int time = Integer.valueOf(tv.getText().toString());
		time = time - 1;
		if (time < 0) {
			time = 59;
			tv.setText(time + "");
			return true;
		} else {
			tv.setText(time / 10 == 0 ? ("0" + time + "") : (time + ""));
			return false;
		}
	}

}
