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
public class RushBuyCountOrdersDownTimerView extends LinearLayout {

	private TextView tv_min_unit;
	private TextView tv_sec_unit;
	private Context context;
	private Timer timer;
	private boolean isTimeEnd = false;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			countDown();
		};
	};

	public RushBuyCountOrdersDownTimerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.fragment_orders_countdowntimer,
				this);
		tv_min_unit = (TextView) view.findViewById(R.id.orders_min_unit);
		tv_sec_unit = (TextView) view.findViewById(R.id.orders_sec_unit);

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
	 * @Description:
	 * @param
	 * @return void
	 * @throws
	 */
	public void setTime(int min, int sec) {

		if (min >= 60 || sec >= 60 || min < 0 || sec < 0) {
			throw new RuntimeException(
					"Time format is error,please check out your code");
		}
		tv_min_unit.setText(min / 10 == 0 ? ("0" + min + "") : (min + ""));
		tv_sec_unit.setText(sec / 10 == 0 ? ("0" + sec + "") : (sec + ""));

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
				tv_min_unit.setText("00");
				tv_sec_unit.setText("00");
				stop();
				if (!isTimeEnd) {
					Toast.makeText(context, "订单已过时", Toast.LENGTH_SHORT).show();
				}
				isTimeEnd = true;
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
