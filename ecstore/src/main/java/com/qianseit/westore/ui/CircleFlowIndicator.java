package com.qianseit.westore.ui;

import cn.shopex.ecstore.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;



/**
 * A FlowIndicator which draws circles (one for each view). <br/>
 * Availables attributes are:<br/>
 * <ul>
 * activeColor: Define the color used to draw the active circle (default to
 * white)
 * </ul>
 * <ul>
 * inactiveColor: Define the color used to draw the inactive circles (default to
 * 0x44FFFFFF)
 * </ul>
 * <ul>
 * inactiveType: Define how to draw the inactive circles, either stroke or fill
 * (default to stroke)
 * </ul>
 * <ul>
 * activeType: Define how to draw the active circle, either stroke or fill
 * (default to fill)
 * </ul>
 * <ul>
 * fadeOut: Define the time (in ms) until the indicator will fade out (default
 * to 0 = never fade out)
 * </ul>
 * <ul>
 * radius: Define the circle radius (default to 4.0)
 * </ul>
 */
public class CircleFlowIndicator extends View implements FlowIndicator,
		AnimationListener {
	private final int STYLE_STROKE = 0;
	private final int STYLE_FILL = 1;

	private float radius = 8;
	private int fadeOutTime = 0;
	private final Paint mPaintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintActive = new Paint(Paint.ANTI_ALIAS_FLAG);
	private FlowView viewFlow;
	private int currentScroll = 0;
	private int flowWidth = 0;
	private FadeTimer timer;
	public AnimationListener animationListener = this;
	private Animation animation;

	/**
	 * Default constructor
	 * 
	 * @param context
	 */
	public CircleFlowIndicator(Context context) {
		super(context);

		Resources res = context.getResources();
		initColors(res.getColor(R.color.circle_indicator_active_color),
				res.getColor(R.color.circle_indicator_inactive_color),
				STYLE_FILL, STYLE_FILL);

	}

	/**
	 * The contructor used with an inflater
	 * 
	 * @param context
	 * @param attrs
	 */
	public CircleFlowIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources res = context.getResources();

		// Retrieve the radius
		radius = res.getDimension(R.dimen.PaddingSmall);

		// Gets the inactive circle type, defaulting to "fill"
		int activeType = STYLE_FILL;
		// Get a custom inactive color if there is one
		int activeDefaultColor = res
				.getColor(R.color.circle_indicator_active_color);
		int activeColor = activeDefaultColor;

		// Gets the inactive circle type, defaulting to "stroke"
		int inactiveType = STYLE_STROKE;
		// Get a custom inactive color if there is one
		int inactiveDefaultColor = res
				.getColor(R.color.circle_indicator_inactive_color);
		int inactiveColor = inactiveDefaultColor;

		// Retrieve the radius
		// rectWidth = (int) getContext().getResources().getDimension(
		// R.dimen.food_cate_rect_width);
		// rectHeight = (int) getContext().getResources().getDimension(
		// R.dimen.food_cate_rect_height);
		// rectSeprator = (int) getContext().getResources().getDimension(
		// R.dimen.food_cate_rect_sep);

		initColors(activeColor, inactiveColor, activeType, inactiveType);
	}

	private void initColors(int activeColor, int inactiveColor, int activeType,
			int inactiveType) {
		// Select the paint type given the type attr
		switch (inactiveType) {
		case STYLE_FILL:
			mPaintInactive.setStyle(Style.FILL);
			break;
		default: {
			mPaintInactive.setStyle(Style.FILL);
			mPaintInactive.setStrokeWidth(radius / 3);
		}
			break;
		}
		mPaintInactive.setColor(inactiveColor);

		// Select the paint type given the type attr
		switch (activeType) {
		case STYLE_STROKE:
			mPaintActive.setStyle(Style.FILL);
			mPaintActive.setStrokeWidth(radius / 2);
			break;
		default:
			mPaintActive.setStyle(Style.FILL);
		}
		mPaintActive.setColor(activeColor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int count = 1;
		if (viewFlow != null)
			count = viewFlow.getViewsCount();

		float circleSeparation = 2 * radius + radius;
		// this is the amount the first circle should be offset to make the
		// entire thing centered
		float centeringOffset = 0;

		int leftPadding = getPaddingLeft();

		// Draw stroked circles

		for (int iLoop = 0; iLoop < count; iLoop++) {
			canvas.drawCircle(leftPadding + radius + (iLoop * circleSeparation)
					+ centeringOffset, getPaddingTop() + radius,
					(int) (radius * 0.7), mPaintInactive);
		}
		float cx = 0;
		if (flowWidth != 0) {
			// Draw the filled circle according to the current scroll
			cx = (currentScroll * (2 * radius + radius)) / flowWidth;
		}
		// The flow width has been upadated yet. Draw the default position
		canvas.drawCircle(leftPadding + radius + cx + centeringOffset,
				getPaddingTop() + radius, radius, mPaintActive);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// We were told how big to be
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		// Calculate the width according the views count
		else {
			int count = 1;
			if (viewFlow != null) {
				count = viewFlow.getViewsCount();
			}
			result = (int) (getPaddingLeft() + getPaddingRight()
					+ (count * 2 * radius) + (count - 1) * radius + 1);
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// We were told how big to be
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		// Measure the height
		else {
			result = (int) (2 * radius + getPaddingTop() + getPaddingBottom() + 1);
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Sets the fill color
	 * 
	 * @param color
	 *            ARGB value for the text
	 */
	public void setFillColor(int color) {
		mPaintActive.setColor(color);
		invalidate();
	}

	/**
	 * Sets the stroke color
	 * 
	 * @param color
	 *            ARGB value for the text
	 */
	public void setStrokeColor(int color) {
		mPaintInactive.setColor(color);
		invalidate();
	}

	/**
	 * Resets the fade out timer to 0. Creating a new one if needed
	 */
	private void resetTimer() {
		// Only set the timer if we have a timeout of at least 1 millisecond
		if (fadeOutTime > 0) {
			// Check if we need to create a new timer
			if (timer == null || timer._run == false) {
				// Create and start a new timer
				timer = new FadeTimer();
				timer.execute();
			} else {
				// Reset the current tiemr to 0
				timer.resetTimer();
			}
		}
	}

	/**
	 * Counts from 0 to the fade out time and animates the view away when
	 * reached
	 */
	private class FadeTimer extends AsyncTask<Void, Void, Void> {
		// The current count
		private int timer = 0;
		// If we are inside the timing loop
		private boolean _run = true;

		public void resetTimer() {
			timer = 0;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			while (_run) {
				try {
					// Wait for a millisecond
					Thread.sleep(1);
					// Increment the timer
					timer++;

					// Check if we've reached the fade out time
					if (timer == fadeOutTime) {
						// Stop running
						_run = false;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			animation = AnimationUtils.loadAnimation(getContext(),
					android.R.anim.fade_out);
			animation.setAnimationListener(animationListener);
			startAnimation(animation);
		}
	}

	public void setViewFlow(FlowView view) {
		resetTimer();
		viewFlow = view;
		flowWidth = viewFlow.getWidth();

		invalidate();
	}

	@Override
	public void onSwitched(int position) {
	}

	public void onAnimationEnd(Animation animation) {
		setVisibility(View.INVISIBLE);
	}

	public void onAnimationRepeat(Animation animation) {
	}

	public void onAnimationStart(Animation animation) {
	}

	public void onScrolled(int h, int v, int oldh, int oldv) {
		if (viewFlow.getViewsCount() > 1)
			setVisibility(View.VISIBLE);
		else
			setVisibility(View.INVISIBLE);

		resetTimer();
		currentScroll = h;
		flowWidth = viewFlow.getWidth();
		invalidate();
	}
}