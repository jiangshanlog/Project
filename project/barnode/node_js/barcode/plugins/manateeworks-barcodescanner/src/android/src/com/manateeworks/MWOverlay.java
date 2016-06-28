/*
 
 Version 1.0
 
 The MWOverlay class serves to greatly simplify the addition of a dynamic viewfinder (similar to the one implemented 
 in Manatee Works Barcode Scanners application) to your own application.
 
 Minimum setup assumes:
 1. Add MWOverlay.java to your project;
 2. Put MWOverlay.addOverlay(this, surfaceView); after initialization of surface view;
 3. Put MWOverlay.removeOverlay(); on closing the activity;
 
 If all three steps are done correctly, you should be able to see a default red viewfinder with a blinking line, 
 capable of updating itself automatically after changing any of the scanning parameters (scanning direction, scanning
 rectangles and active barcode symbologies).
  
 The appearance of the viewfinder and the blinking line can be further customized by changing colors, line width, 
 transparencies and similar, by setting the following properties:
 
 	MWOverlay.isViewportVisible;
	MWOverlay.isBlinkingLineVisible;
	MWOverlay.viewportLineWidth;
	MWOverlay.blinkingLineWidth;
	MWOverlay.viewportAlpha;
	MWOverlay.viewportLineAlpha;
	MWOverlay.blinkingLineAlpha;
	MWOverlay.blinkingSpeed;
	MWOverlay.viewportLineColor;
	MWOverlay.blinkingLineColor;

 */

package com.manateeworks;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class MWOverlay extends View {

	public enum LayerType {
		LT_VIEWPORT, LT_LINE, LT_LOCATION
	}

	private static boolean isAttached = false;
	public static boolean isViewportVisible = true;
	public static boolean isBlinkingLineVisible = true;

	public static float viewportLineWidth = 3.0f;
	public static float blinkingLineWidth = 1.0f;
	public static float locationLineWidth = 4.0f;
	public static float viewportAlpha = 0.5f;
	public static float viewportLineAlpha = 0.5f;
	public static float blinkingLineAlpha = 1.0f;
	public static float blinkingSpeed = 0.25f;
	public static int viewportLineColor = 0xff0000;
	public static int blinkingLineColor = 0xff0000;
	public static int locationLineColor = 0x00ff00;

	private static int lastOrientation = -1;
	private static int lastMask = -1;
	private static float lastLeft = -1;
	private static float lastTop = -1;
	private static float lastWidth = -1;
	private static float lastHeight = -1;
	private static float lastBLinkingSpeed = -1;

	private static float dpiCorrection = 1;

	private static MWOverlay viewportLayer;
	private static MWOverlay lineLayer;
	private static MWOverlay locationLayer;

	private PointF location[] = null;
	private int imageWidth = 0;
	private int imageHeight = 0;

	private LayerType layerType;

	private static Timer checkChangeTimer;
	private static Context mainContext = null;

	private static boolean isPaused = false;

	public static MWOverlay addOverlay(Context context, View previewLayer) {

		if (isAttached) {
			return null;
		}
		isAttached = true;
		mainContext = context;

		ViewGroup parent = (ViewGroup) previewLayer.getParent();

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		dpiCorrection = metrics.density;

		viewportLayer = new MWOverlay(context);
		viewportLayer.layerType = LayerType.LT_VIEWPORT;

		lineLayer = new MWOverlay(context);
		lineLayer.layerType = LayerType.LT_LINE;

		locationLayer = new MWOverlay(context);
		locationLayer.layerType = LayerType.LT_LOCATION;

		viewportLayer.setDrawingCacheEnabled(true);
		lineLayer.setDrawingCacheEnabled(true);
		locationLayer.setDrawingCacheEnabled(true);

		ViewGroup.LayoutParams rl;
		if (parent.getWidth() + parent.getHeight() > 0) {
			rl = new ViewGroup.LayoutParams(parent.getWidth(), parent.getHeight());
		} else {
			rl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}

		parent.addView(viewportLayer, rl);
		parent.addView(lineLayer, rl);
		parent.addView(locationLayer, rl);
		locationLayer.setVisibility(View.INVISIBLE);

		int totalCount = parent.getChildCount();

		for (int i = 0; i < totalCount; i++) {

			View child = parent.getChildAt(i);
			if (child.equals(previewLayer) || child.equals(viewportLayer) || child.equals(lineLayer) || child.equals(locationLayer)) {

			} else {
				child.bringToFront();
				i--;
				totalCount--;
			}

		}

		checkChangeTimer = new Timer();
		checkChangeTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				checkChange();
			}
		}, 200, 200);
		viewportLayer.postInvalidate();
		lineLayer.postInvalidate();

		updateAnimation();

		return viewportLayer;

	}

	public static void removeOverlay() {

		if (!isAttached)
			return;

		if (lineLayer == null || viewportLayer == null)
			return;

		isAttached = false;
		checkChangeTimer.cancel();
		Animation animation = lineLayer.getAnimation();
		if (animation != null) {
			animation.cancel();
			animation.reset();
		}

		/*
		 * animation = locationLayer.getAnimation(); if (animation != null){
		 * animation.cancel(); animation.reset(); }
		 */

		ViewGroup viewParent = (ViewGroup) lineLayer.getParent();

		if (viewParent != null) {
			viewParent.removeView(lineLayer);
			viewParent.removeView(viewportLayer);
			viewParent.removeView(locationLayer);
		}

	}

	public static void setPaused(boolean paused) {
		isPaused = paused;
		if (viewportLayer != null && lineLayer != null) {
			lineLayer.postInvalidate();
		}
	}

	private static void checkChange() {

		RectF frame = BarcodeScanner.MWBgetScanningRect(0);
		int orientation = BarcodeScanner.MWBgetDirection();

		if (orientation != lastOrientation || frame.left != lastLeft || frame.top != lastTop || frame.right != lastWidth
				|| frame.bottom != lastHeight) {

			viewportLayer.postInvalidate();
			lineLayer.postInvalidate();
		}

		if (lastBLinkingSpeed != blinkingSpeed) {
			updateAnimation();
		}

		if (isBlinkingLineVisible != (lineLayer.getVisibility() == View.VISIBLE)) {

			lineLayer.postInvalidate();
		}

		if (isViewportVisible != (viewportLayer.getVisibility() == View.VISIBLE)) {
			viewportLayer.postInvalidate();
		}

	}

	private static void updateAnimation() {

		AlphaAnimation animation = new AlphaAnimation(0, 1);
		animation.setRepeatMode(Animation.REVERSE);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setDuration((long) (blinkingSpeed * 1000));

		lineLayer.startAnimation(animation);

		lastBLinkingSpeed = blinkingSpeed;
	}

	public MWOverlay(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public static void showLocation(PointF[] points, int imageWidth, int imageHeight) {

		locationLayer.imageWidth = imageWidth;
		locationLayer.imageHeight = imageHeight;

		locationLayer.setVisibility(View.VISIBLE);

		int rotation = ((Activity) mainContext).getWindowManager().getDefaultDisplay().getRotation();

		locationLayer.location = points;

		if (rotation == Surface.ROTATION_0) {

		} else if (rotation == Surface.ROTATION_90) {

		} else if (rotation == Surface.ROTATION_180) {
			for (int i = 0; i < 4; i++) {
				locationLayer.location[i].x = imageWidth - points[i].x;
				locationLayer.location[i].y = imageHeight - points[i].y;
			}
		} else if (rotation == Surface.ROTATION_270) {

			for (int i = 0; i < 4; i++) {
				locationLayer.location[i].x = imageWidth - points[i].x;
				locationLayer.location[i].y = imageHeight - points[i].y;
			}

		}

		Animation animationOld = locationLayer.getAnimation();
		if (animationOld != null) {
			animationOld.cancel();
			animationOld.reset();
		}

		AlphaAnimation animation = new AlphaAnimation(1, 0);
		animation.setDuration((long) (0.5 * 1000));
		animation.setFillAfter(true);

		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				locationLayer.setVisibility(View.INVISIBLE);

			}
		});

		locationLayer.startAnimation(animation);

		locationLayer.postInvalidate();

	}

	@Override
	public void onDraw(Canvas canvas) {

		RectF frame = BarcodeScanner.MWBgetScanningRect(0);

		int rotation = ((Activity) mainContext).getWindowManager().getDefaultDisplay().getRotation();

		if (rotation == Surface.ROTATION_0) {
			frame = new RectF(100 - frame.top - frame.bottom, frame.left, frame.bottom, frame.right);
		} else if (rotation == Surface.ROTATION_90) {

		} else if (rotation == Surface.ROTATION_180) {
			frame = new RectF(frame.left, 100 - frame.top - frame.bottom, frame.right, frame.bottom);
		} else {
			frame = new RectF(100 - frame.left - frame.right, 100 - frame.top - frame.bottom, frame.right, frame.bottom);
		}

		/*
		 * if (getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_PORTRAIT){ frame = new RectF(frame.top,
		 * frame.left, frame.bottom, frame.right); }
		 */

		lastLeft = frame.left;
		lastTop = frame.top;
		lastWidth = frame.right;
		lastHeight = frame.bottom;

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		float rectLeft = frame.left * width / 100.0f;
		float rectTop = frame.top * height / 100.0f;
		float rectWidth = frame.right * width / 100.0f;
		float rectHeight = frame.bottom * height / 100.0f;

		frame = new RectF(rectLeft, rectTop, rectWidth + rectLeft, rectHeight + rectTop);

		Paint paint = new Paint();

		if (layerType == LayerType.LT_LOCATION && location != null
				&& locationLayer.location != null/*
													 * && locationLayer.
													 * getVisibility() ==
													 * View.VISIBLE
													 */) {
			paint.setColor(locationLineColor);
			paint.setAlpha(255);
			paint.setStrokeWidth(locationLineWidth * dpiCorrection);

			PointF correctedLocation[] = new PointF[4];

			float xScale = (float) width / imageWidth;
			float yScale = (float) height / imageHeight;

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				xScale = (float) width / imageHeight;
				yScale = (float) height / imageWidth;
			}

			for (int i = 0; i < 4; i++) {
				correctedLocation[i] = new PointF();

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					correctedLocation[i].x = width - locationLayer.location[i].y * yScale;
					correctedLocation[i].y = locationLayer.location[i].x * xScale;
				} else {
					correctedLocation[i].x = locationLayer.location[i].x * xScale;
					correctedLocation[i].y = locationLayer.location[i].y * yScale;
				}

			}

			canvas.drawLine(correctedLocation[0].x, correctedLocation[0].y, correctedLocation[1].x, correctedLocation[1].y, paint);
			canvas.drawLine(correctedLocation[1].x, correctedLocation[1].y, correctedLocation[2].x, correctedLocation[2].y, paint);
			canvas.drawLine(correctedLocation[2].x, correctedLocation[2].y, correctedLocation[3].x, correctedLocation[3].y, paint);
			canvas.drawLine(correctedLocation[3].x, correctedLocation[3].y, correctedLocation[0].x, correctedLocation[0].y, paint);

		} else

		if (layerType == LayerType.LT_VIEWPORT) {

			if (isViewportVisible != (viewportLayer.getVisibility() == View.VISIBLE)) {
				if (isViewportVisible) {
					viewportLayer.setVisibility(View.VISIBLE);
				} else {
					viewportLayer.setVisibility(View.INVISIBLE);
				}
			}

			paint.setColor(0xff000000);
			paint.setAlpha((int) (viewportAlpha * 255));

			canvas.drawRect(0, 0, width, frame.top, paint);
			canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
			canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
			canvas.drawRect(0, frame.bottom + 1, width, height, paint);

			paint.setColor(viewportLineColor);
			paint.setAlpha((int) (viewportLineAlpha * 255));
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(viewportLineWidth * dpiCorrection);

			canvas.drawRect(frame.left, frame.top, frame.right, frame.bottom, paint);

		} else {

			if (isBlinkingLineVisible != (lineLayer.getVisibility() == View.VISIBLE)) {
				if (isBlinkingLineVisible) {
					lineLayer.setVisibility(View.VISIBLE);
					updateAnimation();
				} else {

					Animation animation = lineLayer.getAnimation();
					if (animation != null) {
						animation.cancel();
						animation.reset();
					}
					lineLayer.setVisibility(View.INVISIBLE);
				}
			}

			paint.setColor(blinkingLineColor);
			paint.setStrokeWidth(blinkingLineWidth * dpiCorrection);

			long curTime = System.currentTimeMillis() % 10000000;
			double position = ((double) curTime) / 1000.0d * 3.14d / blinkingSpeed;

			float lineAlpha = (float) (Math.abs(Math.sin(position)));

			paint.setAlpha((int) (blinkingLineAlpha * lineAlpha * 255));
			paint.setAlpha((int) (blinkingLineAlpha * 255));

			int orientation = BarcodeScanner.MWBgetDirection();

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				double pos1f = Math.log(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL) / Math.log(2);
				double pos2f = Math.log(BarcodeScanner.MWB_SCANDIRECTION_VERTICAL) / Math.log(2);

				int pos1 = (int) (pos1f + 0.01);
				int pos2 = (int) (pos2f + 0.01);

				int bit1 = (orientation >> pos1) & 1;// bit at pos1
				int bit2 = (orientation >> pos2) & 1;// bit at pos2
				int mask = (bit2 << pos1) | (bit1 << pos2);
				orientation = orientation & 0xc;
				orientation = orientation | mask;

			}

			lastOrientation = orientation;

			if (isPaused) {

				float size = Math.min(height, width) / 10;

				canvas.drawRect(frame.left + frame.width() / 2 - size / 2, frame.top + frame.height() / 2 - size / 2,
						frame.left + frame.width() / 2 - size / 2 + size / 3, frame.top + frame.height() / 2 - size / 2 + size, paint);
				canvas.drawRect(frame.left + frame.width() / 2 + size / 6, frame.top + frame.height() / 2 - size / 2,
						frame.left + frame.width() / 2 + size / 6 + size / 3, frame.top + frame.height() / 2 - size / 2 + size, paint);

				// CGContextFillRect(context, CGRectMake(rect.origin.x +
				// rect.size.width / 2 - size / 2, rect.origin.y +
				// rect.size.height / 2 - size / 2, size / 3, size));

				// CGContextFillRect(context, CGRectMake(rect.origin.x +
				// rect.size.width / 2 + size / 6, rect.origin.y +
				// rect.size.height / 2 - size / 2, size / 3, size));

			} else {

				if ((orientation & BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL) > 0 || (orientation & BarcodeScanner.MWB_SCANDIRECTION_OMNI) > 0) {

					float middle = frame.height() / 2 + frame.top;
					canvas.drawLine(frame.left, middle, frame.right, middle, paint);
				}
				if ((orientation & BarcodeScanner.MWB_SCANDIRECTION_VERTICAL) > 0 || (orientation & BarcodeScanner.MWB_SCANDIRECTION_OMNI) > 0) {

					float middle = frame.width() / 2 + frame.left;
					canvas.drawLine(middle, frame.top, middle, frame.bottom - 1, paint);
				}

				if ((orientation & BarcodeScanner.MWB_SCANDIRECTION_OMNI) > 0) {

					canvas.drawLine(frame.left + 2, frame.top + 2, frame.right - 2, frame.bottom - 2, paint);
					canvas.drawLine(frame.right - 2, frame.top + 2, frame.left + 2, frame.bottom - 2, paint);

				}
			}
		}

	}

}
