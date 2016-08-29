package com.qianseit.westore.ui;

import java.text.NumberFormat;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.shopex.ecstore.R;

public class CustomProgrssDialog extends CustomDialog {
	public final int STYLE_SPINNER = 0;
	public final int STYLE_HORIZONTAL = 1;

	private ProgressBar mProgress;
	private TextView mMessageView;

	private int mProgressStyle = STYLE_SPINNER;
	private TextView mProgressNumber;
	private String mProgressNumberFormat;
	private TextView mProgressPercent;
	private NumberFormat mProgressPercentFormat;

	private int mMax;
	private int mProgressVal;
	private int mSecondaryProgressVal;
	private int mIncrementBy;
	private int mIncrementSecondaryBy;
	private Drawable mProgressDrawable;
	private Drawable mIndeterminateDrawable;
	private CharSequence mMessage;
	private boolean mIndeterminate;

	private Handler mViewUpdateHandler;

	public CustomProgrssDialog(Activity aa) {
		super(aa);
	}

	public CustomProgrssDialog(View anchor) {
		super(anchor);
	}

	private void initFormats() {
		mProgressNumberFormat = "%1d/%2d";
		mProgressPercentFormat = NumberFormat.getPercentInstance();
		mProgressPercentFormat.setMaximumFractionDigits(0);
	}

	public static CustomProgrssDialog show(Activity context, String title,
			String message) {
		return show(context, title, message, false);
	}

	public static CustomProgrssDialog show(Activity context, String title,
			String message, boolean indeterminate) {
		return show(context, title, message, indeterminate, false);
	}

	public static CustomProgrssDialog show(Activity context, String title,
			String message, boolean indeterminate, boolean cancelable) {
		CustomProgrssDialog dialog = new CustomProgrssDialog(context);
		dialog.setTitle(title).setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable).show();
		return dialog;
	}

	@Override
	public void initCustomDialog() {
		super.initCustomDialog();
		this.initFormats();

		LayoutInflater inflater = LayoutInflater.from(getContext());
		if (mProgressStyle == STYLE_HORIZONTAL) {
			mViewUpdateHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);

					/* Update the number and percent */
					int progress = mProgress.getProgress();
					int max = mProgress.getMax();
					if (mProgressNumberFormat != null) {
						String format = mProgressNumberFormat;
						mProgressNumber.setText(String.format(format, progress,
								max));
					} else {
						mProgressNumber.setText("");
					}
					if (mProgressPercentFormat != null) {
						double percent = (double) progress / (double) max;
						SpannableString tmp = new SpannableString(
								mProgressPercentFormat.format(percent));
						tmp.setSpan(new StyleSpan(
								android.graphics.Typeface.BOLD), 0, tmp
								.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						mProgressPercent.setText(tmp);
					} else {
						mProgressPercent.setText("");
					}
				}
			};

			int viewLayout = R.layout.progress_dialog_horizontal;
			View view = inflater.inflate(viewLayout, null);
			mProgress = (ProgressBar) view.findViewById(R.id.progress);
			mProgressNumber = (TextView) view
					.findViewById(R.id.progress_number);
			mProgressPercent = (TextView) view
					.findViewById(R.id.progress_percent);
			setCustomView(view);
		} else {
			int viewLayout = R.layout.progress_dialog_spinner;
			View view = inflater.inflate(viewLayout, null);
			mProgress = (ProgressBar) view.findViewById(R.id.progress);
			mMessageView = (TextView) view.findViewById(R.id.message);
			setCustomView(view);
		}

		if (mMax > 0) {
			setMax(mMax);
		}
		if (mProgressVal > 0) {
			setProgress(mProgressVal);
		}
		if (mSecondaryProgressVal > 0) {
			setSecondaryProgress(mSecondaryProgressVal);
		}
		if (mIncrementBy > 0) {
			incrementProgressBy(mIncrementBy);
		}
		if (mIncrementSecondaryBy > 0) {
			incrementSecondaryProgressBy(mIncrementSecondaryBy);
		}
		if (mProgressDrawable != null) {
			setProgressDrawable(mProgressDrawable);
		}
		if (mIndeterminateDrawable != null) {
			setIndeterminateDrawable(mIndeterminateDrawable);
		}
		if (mMessage != null) {
			setMessage(mMessage);
		}
		setIndeterminate(mIndeterminate);
		onProgressChanged();
	}

	public void setProgress(int value) {
		if (isShowing()) {
			mProgress.setProgress(value);
			onProgressChanged();
		} else {
			mProgressVal = value;
		}
	}

	public void setSecondaryProgress(int secondaryProgress) {
		if (mProgress != null) {
			mProgress.setSecondaryProgress(secondaryProgress);
			onProgressChanged();
		} else {
			mSecondaryProgressVal = secondaryProgress;
		}
	}

	public int getProgress() {
		if (mProgress != null) {
			return mProgress.getProgress();
		}
		return mProgressVal;
	}

	public int getSecondaryProgress() {
		if (mProgress != null) {
			return mProgress.getSecondaryProgress();
		}
		return mSecondaryProgressVal;
	}

	public int getMax() {
		if (mProgress != null) {
			return mProgress.getMax();
		}
		return mMax;
	}

	public void setMax(int max) {
		if (mProgress != null) {
			mProgress.setMax(max);
			onProgressChanged();
		} else {
			mMax = max;
		}
	}

	public void incrementProgressBy(int diff) {
		if (mProgress != null) {
			mProgress.incrementProgressBy(diff);
			onProgressChanged();
		} else {
			mIncrementBy += diff;
		}
	}

	public void incrementSecondaryProgressBy(int diff) {
		if (mProgress != null) {
			mProgress.incrementSecondaryProgressBy(diff);
			onProgressChanged();
		} else {
			mIncrementSecondaryBy += diff;
		}
	}

	public void setProgressDrawable(Drawable d) {
		if (mProgress != null) {
			mProgress.setProgressDrawable(d);
		} else {
			mProgressDrawable = d;
		}
	}

	public void setIndeterminateDrawable(Drawable d) {
		if (mProgress != null) {
			mProgress.setIndeterminateDrawable(d);
		} else {
			mIndeterminateDrawable = d;
		}
	}

	public void setIndeterminate(boolean indeterminate) {
		if (mProgress != null) {
			mProgress.setIndeterminate(indeterminate);
		} else {
			mIndeterminate = indeterminate;
		}
	}

	public boolean isIndeterminate() {
		if (mProgress != null) {
			return mProgress.isIndeterminate();
		}
		return mIndeterminate;
	}

	@Override
	public CustomProgrssDialog setMessage(CharSequence message) {
		if (mProgress != null) {
			if (mProgressStyle == STYLE_HORIZONTAL) {
				super.setMessage(message);
			} else {
				mMessageView.setText(message);
			}
		} else {
			mMessage = message;
		}
		return this;
	}

	public void setProgressStyle(int style) {
		mProgressStyle = style;
	}

	/**
	 * Change the format of the small text showing current and maximum units of
	 * progress. The default is "%1d/%2d". Should not be called during the
	 * number is progressing.
	 * 
	 * @param format
	 *            A string passed to {@link String#format String.format()}; use
	 *            "%1d" for the current number and "%2d" for the maximum. If
	 *            null, nothing will be shown.
	 */
	public void setProgressNumberFormat(String format) {
		mProgressNumberFormat = format;
		onProgressChanged();
	}

	/**
	 * Change the format of the small text showing the percentage of progress.
	 * The default is {@link NumberFormat#getPercentInstance()
	 * NumberFormat.getPercentageInstnace().} Should not be called during the
	 * number is progressing.
	 * 
	 * @param format
	 *            An instance of a {@link NumberFormat} to generate the
	 *            percentage text. If null, nothing will be shown.
	 */
	public void setProgressPercentFormat(NumberFormat format) {
		mProgressPercentFormat = format;
		onProgressChanged();
	}

	private void onProgressChanged() {
		if (mProgressStyle == STYLE_HORIZONTAL) {
			if (mViewUpdateHandler != null
					&& !mViewUpdateHandler.hasMessages(0)) {
				mViewUpdateHandler.sendEmptyMessage(0);
			}
		}
	}
}
