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

public class CountDownView extends LinearLayout {

	private Context mContext;

	private TimeEndListener mTimeEndListener;
	
	// 天数
	private TextView tv_day_count;
	private TextView tv_day;
	// 小时，十位
	private TextView tv_hour_decade;
	// 小时，个位
	private TextView tv_hour_unit;
	// 分钟，十位
	private TextView tv_min_decade;
	// 分钟，个位
	private TextView tv_min_unit;
	// 秒，十位
	private TextView tv_sec_decade;
	// 秒，个位
	private TextView tv_sec_unit;
	
	private TextView tv_hour_spit;
	private TextView tv_minute_spit;
	private TextView tv_text_util;
	private TextView tv_text2;
	private TextView tv_text3;
	
	// 计时器
	private Timer timer;
	private long times;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			countDown();
		};
	};

	public CountDownView(Context context) {
		super(context);
		init(context);
	}

	public CountDownView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.item_countdown_view, this);
		tv_day_count = (TextView) view.findViewById(R.id.tv_day_count);
		tv_day = (TextView) view.findViewById(R.id.tv_day_decade);
		tv_hour_decade = (TextView) view.findViewById(R.id.tv_hour_decade);
		tv_hour_unit = (TextView) view.findViewById(R.id.tv_hour_unit);
		tv_min_decade = (TextView) view.findViewById(R.id.tv_min_decade);
		tv_min_unit = (TextView) view.findViewById(R.id.tv_min_unit);
		tv_sec_decade = (TextView) view.findViewById(R.id.tv_sec_decade);
		tv_sec_unit = (TextView) view.findViewById(R.id.tv_sec_unit);
		
		tv_hour_spit  = (TextView) view.findViewById(R.id.tv_hour_spit);
		tv_minute_spit  = (TextView) view.findViewById(R.id.tv_minute_spit);
		tv_text_util  = (TextView) view.findViewById(R.id.tv_text_util);
//		tv_text1  = (TextView) view.findViewById(R.id.tv_text1);
		tv_text2  = (TextView) view.findViewById(R.id.tv_text2);
		tv_text3  = (TextView) view.findViewById(R.id.tv_text3);
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

	public void stop() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			tv_hour_decade.setText(0 + "");
			tv_hour_unit.setText(0 + "");
			tv_min_decade.setText(0 + "");
			tv_min_unit.setText(0 + "");
			tv_sec_decade.setText(0 + "");
			tv_sec_unit.setText(0 + "");
		}
	}

	public void setTime(long secondss) {
		if (secondss <= 0) {
			return;
		}
		times = secondss;
		long seconds = secondss % 60;
		long minutes = secondss / 60 % 60;
		long hours = secondss / (60 * 60) % 24;
		long day = secondss / (60 * 60 * 24);
		long hour_decade = hours / 10;
		long hour_unit = hours % 10;

		long min_decade = minutes / 10;
		long min_unit = minutes % 10;

		long sec_decade = seconds / 10;
		long sec_unit = seconds % 10;
		if (day > 0) {
			tv_day_count.setText(day + "");
			tv_day_count.setVisibility(VISIBLE);
			tv_day.setVisibility(VISIBLE);
		} else {
			tv_day_count.setVisibility(GONE);
			tv_day.setVisibility(GONE);
		}

		tv_hour_decade.setText(hour_decade + "");
		tv_hour_unit.setText(hour_unit + "");
		tv_min_decade.setText(min_decade + "");
		tv_min_unit.setText(min_unit + "");
		tv_sec_decade.setText(sec_decade + "");
		tv_sec_unit.setText(sec_unit + "");
	}
	
	/**
	 * 
	 * @author chesonqin
	 * 2014-12-11
	 * @param isStart 是否已经开始
	 * @param seconds 距离活动时间
	 */
	public void setTime(boolean isStart , long seconds){
		if (isStart) {
			tv_text2.setText("结");
			tv_text3.setText("束");
		} else {
			tv_text2.setText("开");
			tv_text3.setText("始");
		}
		setTimeNew(seconds);
	}
	
	public void setTimeNew(long remainSeconds){
		times = remainSeconds;
		if (remainSeconds <= 0) {
			return;
		}
		long day = remainSeconds / (60 * 60 * 24);
		long hours = remainSeconds / (60 * 60) % 24;
		if (day > 0) {
			long hour_decade = hours / 10;
			long hour_unit = hours % 10;
			tv_day_count.setText(day + "");
			tv_hour_decade.setText(hour_decade + "");
			tv_hour_unit.setText(hour_unit + "");
			tv_text_util.setText("时");
			tv_day_count.setVisibility(VISIBLE);
			tv_day.setVisibility(VISIBLE);
			if (hour_decade > 0) {
				tv_hour_decade.setVisibility(VISIBLE);
			} else {
				tv_hour_decade.setVisibility(GONE);
			}
			tv_hour_unit.setVisibility(VISIBLE);
			tv_min_decade.setVisibility(GONE);
			tv_min_unit.setVisibility(GONE);
			tv_sec_decade.setVisibility(GONE);
			tv_sec_unit.setVisibility(GONE);
			
			tv_hour_spit.setVisibility(GONE);
			tv_minute_spit.setVisibility(GONE);
			tv_text_util.setVisibility(VISIBLE);
			
		} else if( hours > 0) {
			long minutes = remainSeconds / 60 % 60;
			long hour_decade = hours / 10;
			long hour_unit = hours % 10;
			
			long min_decade = minutes / 10;
			long min_unit = minutes % 10;
			tv_hour_decade.setText(hour_decade + "");
			tv_hour_unit.setText(hour_unit + "");
			tv_min_decade.setText(min_decade + "");
			tv_min_unit.setText(min_unit + "");
			tv_text_util.setText("分");

			tv_day_count.setVisibility(GONE);
			tv_day.setVisibility(GONE);
			tv_hour_decade.setVisibility(VISIBLE);
			tv_hour_unit.setVisibility(VISIBLE);
			tv_min_decade.setVisibility(VISIBLE);
			tv_min_unit.setVisibility(VISIBLE);
			tv_sec_decade.setVisibility(GONE);
			tv_sec_unit.setVisibility(GONE);
			tv_hour_spit.setVisibility(VISIBLE);
			tv_minute_spit.setVisibility(GONE);
			tv_text_util.setVisibility(VISIBLE);
		} else {
			long minutes = remainSeconds / 60 % 60;
			long min_decade = minutes / 10;
			long min_unit = minutes % 10;
			long seconds = remainSeconds % 60;
			long sec_decade = seconds / 10;
			long sec_unit = seconds % 10;
			tv_min_decade.setText(min_decade + "");
			tv_min_unit.setText(min_unit + "");
			tv_sec_decade.setText(sec_decade + "");
			tv_sec_unit.setText(sec_unit + "");
			
			tv_day_count.setVisibility(GONE);
			tv_day.setVisibility(GONE);
			tv_hour_decade.setVisibility(GONE);
			tv_hour_unit.setVisibility(GONE);
			tv_min_decade.setVisibility(VISIBLE);
			tv_min_unit.setVisibility(VISIBLE);
			tv_sec_decade.setVisibility(VISIBLE);
			tv_sec_unit.setVisibility(VISIBLE);
			
			tv_hour_spit.setVisibility(GONE);
			tv_minute_spit.setVisibility(VISIBLE);
			tv_text_util.setVisibility(GONE);
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
	
	public void setTimeEndListener(TimeEndListener listener){
		mTimeEndListener = listener;
	}
	
	public interface TimeEndListener{
		void isTimeEnd();
	}
	
}
