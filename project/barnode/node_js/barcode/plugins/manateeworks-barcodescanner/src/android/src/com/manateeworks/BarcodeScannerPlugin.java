package com.manateeworks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;

import com.manateeworks.BarcodeScanner.MWResult;
import com.manateeworks.BarcodeScanner.MWResults;
import com.manateeworks.ScannerActivity.State;
import com.manateeworks.camera.CameraManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BarcodeScannerPlugin extends CordovaPlugin implements SurfaceHolder.Callback {

    private boolean hasSurface;

    public static class ImageInfo {
        byte[] pixels;
        int width;
        int height;

        ImageInfo(int width, int height) {
            this.width = width;
            this.height = height;
            pixels = new byte[width * height];
        }
    }

    ArrayList<RectF> rects;

    SurfaceView surfaceView;
    RelativeLayout rlSurfaceContainer;
    RelativeLayout rlFullScreen;
    ScrollView scrollView;
    ImageView overlayImage;
    ProgressBar pBar;
    public static boolean useAutoRect = true;
    public static boolean useFrontCamera = false;
    // !!! Rects are in format: x, y, width, height !!!
    public static final Rect RECT_LANDSCAPE_1D = new Rect(2, 20, 96, 60);
    public static final Rect RECT_LANDSCAPE_2D = new Rect(20, 2, 60, 96);
    public static final Rect RECT_PORTRAIT_1D = new Rect(20, 2, 60, 96);
    public static final Rect RECT_PORTRAIT_2D = new Rect(20, 2, 60, 96);
    public static final Rect RECT_FULL_1D = new Rect(2, 2, 96, 96);
    public static final Rect RECT_FULL_2D = new Rect(20, 2, 60, 96);
    public static final Rect RECT_DOTCODE = new Rect(30, 20, 40, 60);
    private static CallbackContext cbc;
    private static String lastType;

    public static double widthP;
    public static double heightP;
    public static double xP;
    public static double yP;
    private ImageButton flashButton;
    private ImageButton zoomButton;


    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);

        ScannerActivity.flashOn = false;
        updateFlash();

        if (rlFullScreen != null) {
            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("code", "");
                jsonResult.put("type", "Cancel");
                jsonResult.put("bytes", "");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            cbc.success(jsonResult);
            // updateFlash();
            CameraManager.get().stopPreview();
            ScannerActivity.handler = null;

            CameraManager.get().closeDriver();
            ScannerActivity.state = State.STOPPED;
            stopScanner();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (rlFullScreen != null && CameraManager.get().camera != null) {

            Display display = ((WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            final Point size = new Point();
            display.getSize(size);

            int w = size.x;
            int h = size.y;

            final float AR = (float) size.y / (float) size.x;

            final double x = xP / 100 * w;
            final double y = yP / 100 * h;
            final double width = widthP / 100 * w;
            final double height = heightP / 100 * h;

            LayoutParams lps = (LayoutParams) scrollView.getLayoutParams();

            lps.width = (int) Math.round(width);
            lps.height = (int) Math.round(height);

            lps.leftMargin = (int) Math.round(x);
            lps.topMargin = (int) Math.round(y);
            int heightTmp = 0;
            int widthTmp = 0;

            if (width * AR >= height) {
                heightTmp = (int) Math.round(width * AR);
                widthTmp = (int) Math.round(width);
            } else {
                widthTmp = (int) Math.round(height / AR);
                heightTmp = (int) Math.round(height);
            }
            final float heightTmpRunnable = heightTmp;
            final float widthTmpRunnable = widthTmp;

            scrollView.setLayoutParams(lps);

            android.view.ViewGroup.LayoutParams surfaceLPS = rlSurfaceContainer.getLayoutParams();
            surfaceLPS.width = widthTmp;
            surfaceLPS.height = heightTmp;
            rlSurfaceContainer.setLayoutParams(surfaceLPS);

            android.view.ViewGroup.LayoutParams surfaceViewLPS = surfaceView.getLayoutParams();
            surfaceViewLPS.width = widthTmp;
            surfaceViewLPS.height = heightTmp;

            surfaceView.setLayoutParams(surfaceViewLPS);

            if (flashButton != null) {
                LayoutParams flashParams = (LayoutParams) flashButton.getLayoutParams();
                int marginDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, cordova.getActivity().getResources().getDisplayMetrics());

                flashParams.topMargin = (int) ((heightTmp - height) / 2) + marginDP;
                flashParams.rightMargin = (int) ((widthTmp - width) / 2) + marginDP;
                flashButton.setLayoutParams(flashParams);
            }
            if (zoomButton != null) {
                LayoutParams zoomParams = (LayoutParams) zoomButton.getLayoutParams();
                int marginDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, cordova.getActivity().getResources().getDisplayMetrics());

                zoomParams.topMargin = (int) ((heightTmp - height) / 2) + marginDP;
                zoomParams.leftMargin = (int) ((widthTmp - width) / 2) + marginDP;
                zoomButton.setLayoutParams(zoomParams);
            }


            if (ScannerActivity.param_OverlayMode == 1) {
                MWOverlay.removeOverlay();
            } else if (ScannerActivity.param_OverlayMode == 2) {
                LayoutParams overlayLps = (LayoutParams) overlayImage.getLayoutParams();
                overlayLps.width = (int) Math.round(width);
                overlayLps.height = (int) Math.round(height);
                overlayLps.topMargin = (int) Math.round(heightTmpRunnable / 2 - height / 2);

                overlayImage.setLayoutParams(overlayLps);

            }

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            setAutoRect();
                            if (ScannerActivity.param_OverlayMode == 1) {
                                MWOverlay.addOverlay(cordova.getActivity(), surfaceView);
                            }
                            scrollView.scrollTo((int) Math.round(widthTmpRunnable / 2 - width / 2),
                                    (int) Math.round(heightTmpRunnable / 2 - height / 2));
                        }
                    });
                }
            }, 300);
            CameraManager.get().setCameraDisplayOrientation(0, CameraManager.get().camera,
                    (cordova.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT));
        }

    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {

        if ("initDecoder".equals(action)) {

            initDecoder();
            callbackContext.success();
            return true;

        } else if ("startScanner".equals(action)) {

            stopScanner();
            cbc = callbackContext;
            ScannerActivity.cbc = cbc;

            if(cordova.hasPermission(Manifest.permission.CAMERA)){
                Context context = this.cordova.getActivity().getApplicationContext();
                Intent intent = new Intent(context, com.manateeworks.ScannerActivity.class);
                this.cordova.startActivityForResult(this, intent, 1);
            }else{
                cordova.requestPermission(this,234,Manifest.permission.CAMERA);
            }
            return true;

        } else if ("startScannerView".equals(action)) {
            if (rlFullScreen == null) {
                cbc = callbackContext;
                xP = args.getDouble(0);
                yP = args.getDouble(1);
                widthP = args.getDouble(2);
                heightP = args.getDouble(3);

                startScannerView();

            } else {
                setAutoRect();
            }
            return true;

        } else if ("getLastType".equals(action)) {

            callbackContext.success(lastType);
            return true;
        } else if ("togglePauseResume".equals(action)) {

            if (rlFullScreen != null) {
                if (ScannerActivity.state != State.STOPPED) {
                    ScannerActivity.state = State.STOPPED;
                    if (ScannerActivity.param_OverlayMode == 1) {
                        MWOverlay.setPaused(true);
                    }
                } else {

                    ScannerActivity.state = State.PREVIEW;
                    if (ScannerActivity.param_OverlayMode == 1) {
                        MWOverlay.setPaused(false);
                    }
                }
            }
            return true;

        } else if ("setLevel".equals(action)) {

            BarcodeScanner.MWBsetLevel(args.getInt(0));
            return true;

        } else if ("setActiveCodes".equals(action)) {

            BarcodeScanner.MWBsetActiveCodes(args.getInt(0));
            return true;

        } else if ("setActiveSubcodes".equals(action)) {

            BarcodeScanner.MWBsetActiveSubcodes(args.getInt(0), args.getInt(1));
            return true;

        } else if ("setUseAutorect".equals(action)) {
            useAutoRect = args.getBoolean(0);
            return true;

        } else if ("setFlags".equals(action)) {

            callbackContext.success(BarcodeScanner.MWBsetFlags(args.getInt(0), args.getInt(1)));
            return true;

        } else if ("setMinLength".equals(action)) {

            callbackContext.success(BarcodeScanner.MWBsetMinLength(args.getInt(0), args.getInt(1)));
            return true;

        } else if ("setDirection".equals(action)) {

            BarcodeScanner.MWBsetDirection(args.getInt(0));
            return true;

        } else if ("setScanningRect".equals(action)) {

            BarcodeScanner.MWBsetScanningRect(args.getInt(0), args.getInt(1), args.getInt(2), args.getInt(3), args.getInt(4));
            return true;

        } else if ("registerCode".equals(action)) {

            BarcodeScanner.MWBregisterCode(args.getInt(0), args.getString(1), args.getString(2));
            return true;

        } else if ("setInterfaceOrientation".equals(action)) {

            String orientation = args.getString(0);
            if (orientation.equalsIgnoreCase("Portrait")) {
                ScannerActivity.param_Orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientation.equalsIgnoreCase("LandscapeLeft"))

            {
                ScannerActivity.param_Orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else if (orientation.equalsIgnoreCase("LandscapeRight"))

            {
                ScannerActivity.param_Orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else if (orientation.equalsIgnoreCase("All")) {
                ScannerActivity.param_Orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            }

            return true;

        } else if ("setOverlayMode".equals(action)) {

            ScannerActivity.param_OverlayMode = args.getInt(0);
            return true;

        } else if ("enableHiRes".equals(action)) {

            ScannerActivity.param_EnableHiRes = args.getBoolean(0);
            return true;

        } else if ("enableFlash".equals(action)) {
            ScannerActivity.param_EnableFlash = args.getBoolean(0);
            return true;

        } else if ("turnFlashOn".equals(action)) {
            ScannerActivity.param_DefaultFlashOn = args.getBoolean(0);
            return true;

        } else if ("toggleFlash".equals(action)) {
            if (rlFullScreen != null && CameraManager.get().isTorchAvailable()) {
                ScannerActivity.flashOn = !ScannerActivity.flashOn;
                CameraManager.get().setTorch(ScannerActivity.flashOn);

                if (flashButton != null) {
                    if (ScannerActivity.flashOn) {
                        flashButton.setImageResource(cordova.getActivity().getResources().getIdentifier("flashbuttonon", "drawable", cordova.getActivity().getApplication().getPackageName()));
                    } else {
                        flashButton.setImageResource(cordova.getActivity().getResources().getIdentifier("flashbuttonoff", "drawable", cordova.getActivity().getApplication().getPackageName()));
                    }
                    flashButton.postInvalidate();
                }


            }
            return true;

        } else if ("enableZoom".equals(action)) {
            ScannerActivity.param_EnableZoom = args.getBoolean(0);
            return true;

        } else if ("toggleZoom".equals(action)) {
            if (rlFullScreen != null) {
                int maxZoom = CameraManager.get().getMaxZoom();
                if (maxZoom > 100) {
                    cordova.getActivity().runOnUiThread(new Runnable() {

                        public void run() {
                            ScannerActivity.toggleZoom();
                        }
                    });
                }

            }
            return true;
        } else if ("setMaxThreads".equals(action)) {
            ScannerActivity.param_maxThreads = args.getInt(0);
            return true;

        } else if ("setZoomLevels".equals(action)) {

            ScannerActivity.param_ZoomLevel1 = args.getInt(0);
            ScannerActivity.param_ZoomLevel2 = args.getInt(1);
            ScannerActivity.zoomLevel = args.getInt(2);
            if (ScannerActivity.zoomLevel > 2) {
                ScannerActivity.zoomLevel = 2;
            }
            if (ScannerActivity.zoomLevel < 0) {
                ScannerActivity.zoomLevel = 0;
            }
            return true;

        } else if ("setCustomParam".equals(action)) {

            if (ScannerActivity.customParams == null) {
                ScannerActivity.customParams = new HashMap<String, Object>();
            }

            ScannerActivity.customParams.put((String) args.get(0), args.get(1));
            return true;

        } else if ("setParam".equals(action)) {

            BarcodeScanner.MWBsetParam(args.getInt(0), args.getInt(1), args.getInt(2));
            return true;

        } else if ("resumeScanning".equals(action)) {

            ScannerActivity.state = State.PREVIEW;
            if (ScannerActivity.param_OverlayMode == 1) {
                MWOverlay.setPaused(false);
            }
            return true;

        } else if ("closeScannerOnDecode".equals(action)) {
            ScannerActivity.param_closeOnSuccess = args.getBoolean(0);
            return true;

        } else if ("closeScanner".equals(action)) {
            stopScanner();

            if (ScannerActivity.activity != null) {
                ScannerActivity.activity.finish();
            }
            return true;

        } else if ("duplicateCodeDelay".equals(action)) {
            BarcodeScanner.MWBsetDuplicatesTimeout(args.getInt(0));
            return true;

        } else if ("useFrontCamera".equals(action)) {
            useFrontCamera = args.getBoolean(0);
            return true;

        } else if ("scanImage".equals(action)) {

            String imageURI = args.getString(0);
            if (imageURI.startsWith("file://"))

            {
                imageURI = imageURI.substring(7);
            }

            ImageInfo imageInfo = bitmapToGrayscale(imageURI);

            if (imageInfo != null)

            {
                // initDecoder();
                byte[] result = BarcodeScanner.MWBscanGrayscaleImage(imageInfo.pixels, imageInfo.width, imageInfo.height);

                if (result != null) {
                    MWResults mwResults = new MWResults(result);
                    if (mwResults != null && mwResults.count > 0) {
                        MWResult mwResult = mwResults.getResult(0);

                        String typeName = "";
                        switch (mwResult.type) {
                            case BarcodeScanner.FOUND_25_INTERLEAVED:
                                typeName = "Code 25";
                                break;
                            case BarcodeScanner.FOUND_25_STANDARD:
                                typeName = "Code 25 Standard";
                                break;
                            case BarcodeScanner.FOUND_128:
                                typeName = "Code 128";
                                break;
                            case BarcodeScanner.FOUND_39:
                                typeName = "Code 39";
                                break;
                            case BarcodeScanner.FOUND_93:
                                typeName = "Code 93";
                                break;
                            case BarcodeScanner.FOUND_AZTEC:
                                typeName = "AZTEC";
                                break;
                            case BarcodeScanner.FOUND_DM:
                                typeName = "Datamatrix";
                                break;
                            case BarcodeScanner.FOUND_EAN_13:
                                typeName = "EAN 13";
                                break;
                            case BarcodeScanner.FOUND_EAN_8:
                                typeName = "EAN 8";
                                break;
                            case BarcodeScanner.FOUND_NONE:
                                typeName = "None";
                                break;
                            case BarcodeScanner.FOUND_RSS_14:
                                typeName = "Databar 14";
                                break;
                            case BarcodeScanner.FOUND_RSS_14_STACK:
                                typeName = "Databar 14 Stacked";
                                break;
                            case BarcodeScanner.FOUND_RSS_EXP:
                                typeName = "Databar Expanded";
                                break;
                            case BarcodeScanner.FOUND_RSS_LIM:
                                typeName = "Databar Limited";
                                break;
                            case BarcodeScanner.FOUND_UPC_A:
                                typeName = "UPC A";
                                break;
                            case BarcodeScanner.FOUND_UPC_E:
                                typeName = "UPC E";
                                break;
                            case BarcodeScanner.FOUND_PDF:
                                typeName = "PDF417";
                                break;
                            case BarcodeScanner.FOUND_QR:
                                typeName = "QR";
                                break;
                            case BarcodeScanner.FOUND_CODABAR:
                                typeName = "Codabar";
                                break;
                            case BarcodeScanner.FOUND_128_GS1:
                                typeName = "Code 128 GS1";
                                break;
                            case BarcodeScanner.FOUND_ITF14:
                                typeName = "ITF 14";
                                break;
                            case BarcodeScanner.FOUND_11:
                                typeName = "Code 11";
                                break;
                            case BarcodeScanner.FOUND_MSI:
                                typeName = "MSI Plessey";
                                break;
                            case BarcodeScanner.FOUND_25_IATA:
                                typeName = "IATA Code 25";
                                break;
                        }

                        JSONObject jsonResult = new JSONObject();
                        try {
                            jsonResult.put("code", mwResult.text);
                            jsonResult.put("type", typeName);
                            jsonResult.put("isGS1", mwResult.isGS1);
                            jsonResult.put("imageWidth", mwResult.imageWidth);
                            jsonResult.put("imageHeight", mwResult.imageHeight);

                            if (mwResult.locationPoints != null) {
                                jsonResult.put("location",
                                        new JSONObject()
                                                .put("p1",
                                                        new JSONObject().put("x", mwResult.locationPoints.p1.x).put("y",
                                                                mwResult.locationPoints.p1.y))
                                                .put("p2", new JSONObject().put("x", mwResult.locationPoints.p2.x).put("y", mwResult.locationPoints.p2.y))
                                                .put("p3", new JSONObject().put("x", mwResult.locationPoints.p3.x).put("y", mwResult.locationPoints.p3.y))
                                                .put("p4", new JSONObject().put("x", mwResult.locationPoints.p4.x).put("y", mwResult.locationPoints.p4.y)));
                            } else {
                                jsonResult.put("location", false);
                            }

                            JSONArray rawArray = new JSONArray();

                            for (int i = 0; i < mwResult.bytesLength; i++) {
                                rawArray.put(0xff & mwResult.bytes[i]);
                            }

                            jsonResult.put("bytes", rawArray);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        PluginResult pr = new PluginResult(PluginResult.Status.OK, jsonResult);

                        callbackContext.sendPluginResult(pr);

                    } else {
                        callbackContext.error(-1);
                    }
                } else {
                    callbackContext.error(-1);
                }

            } else

            {
                callbackContext.error(-1);
            }

            return true;

        }
        return false;
    }

    private void updateFlash() {

        if (flashButton != null) {
            if (!CameraManager.get().isTorchAvailable()) {
                flashButton.setVisibility(View.GONE);
                return;

            }


            if (ScannerActivity.flashOn) {
                flashButton.setImageResource(cordova.getActivity().getResources().getIdentifier("flashbuttonon", "drawable", cordova.getActivity().getApplication().getPackageName()));
            } else {
                flashButton.setImageResource(cordova.getActivity().getResources().getIdentifier("flashbuttonoff", "drawable", cordova.getActivity().getApplication().getPackageName()));
            }

            CameraManager.get().setTorch(ScannerActivity.flashOn);

            flashButton.postInvalidate();
        }

    }

    public void setAutoRect() {

        if (rlFullScreen != null) {

            float p1x;
            float p1y;

            float p2x;
            float p2y;

            p1x = (float) (surfaceView.getWidth() - scrollView.getWidth()) / 2 / surfaceView.getWidth();
            p1y = (float) (surfaceView.getHeight() - scrollView.getHeight()) / 2 / surfaceView.getHeight();

            p2x = (float) scrollView.getWidth() / surfaceView.getWidth();
            p2y = (float) scrollView.getHeight() / surfaceView.getHeight();

            if (surfaceView.getWidth() < surfaceView.getHeight()) {
                float tmp = p1x;
                p1x = p1y;
                p1y = tmp;
                tmp = p2x;
                p2x = p2y;
                p2y = tmp;
            }

            if (useAutoRect) {

                p1x += 0.02f;
                p1y += 0.02f;
                p2x -= 0.04f;
                p2y -= 0.04f;

                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI, p1x * 100, p1y * 100, (p2x) * 100, (p2y) * 100);

            } else {

                if (rects == null) {
                    rects = new ArrayList<RectF>();
                    rects.add(0, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_128));
                    rects.add(1, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_25));
                    rects.add(2, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_39));
                    rects.add(3, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_93));
                    rects.add(4, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC));
                    rects.add(5, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM));
                    rects.add(6, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC));
                    rects.add(7, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF));
                    rects.add(8, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR));
                    rects.add(9, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS));
                    rects.add(10, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR));
                    rects.add(11, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE));
                    rects.add(12, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_11));
                    rects.add(13, BarcodeScanner.MWBgetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI));

                } else {
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128, rects.get(0).left, rects.get(0).top, rects.get(0).right,
                            rects.get(0).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25, rects.get(1).left, rects.get(1).top, rects.get(1).right,
                            rects.get(1).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39, rects.get(2).left, rects.get(2).top, rects.get(2).right,
                            rects.get(2).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93, rects.get(3).left, rects.get(3).top, rects.get(3).right,
                            rects.get(3).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC, rects.get(4).left, rects.get(4).top, rects.get(4).right,
                            rects.get(4).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM, rects.get(5).left, rects.get(5).top, rects.get(5).right,
                            rects.get(5).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, rects.get(6).left, rects.get(6).top, rects.get(6).right,
                            rects.get(6).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF, rects.get(7).left, rects.get(7).top, rects.get(7).right,
                            rects.get(7).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR, rects.get(8).left, rects.get(8).top, rects.get(8).right,
                            rects.get(8).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS, rects.get(9).left, rects.get(9).top, rects.get(9).right,
                            rects.get(9).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR, rects.get(10).left, rects.get(10).top,
                            rects.get(10).right, rects.get(10).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE, rects.get(11).left, rects.get(11).top,
                            rects.get(11).right, rects.get(11).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11, rects.get(12).left, rects.get(12).top, rects.get(12).right,
                            rects.get(12).bottom);
                    BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI, rects.get(13).left, rects.get(13).top, rects.get(13).right,
                            rects.get(13).bottom);
                }

                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_128)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_128)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_128)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_128)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_25)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_25)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_25)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_25)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_39)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_39)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_39)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_39)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_93)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_93)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_93)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_93)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC,
                        (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_AZTEC)[0] / 100)
                                * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                        (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_AZTEC)[1] / 100)
                                * (surfaceView.getHeight() * p2y)) / surfaceView.getHeight()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_AZTEC)[2] / 100) * (surfaceView.getWidth() * p2x))
                                / surfaceView.getWidth()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_AZTEC)[3] / 100) * (surfaceView.getWidth() * p2y))
                                / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DM)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DM)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DM)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DM)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC,
                        (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_EANUPC)[0] / 100)
                                * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                        (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_EANUPC)[1] / 100)
                                * (surfaceView.getHeight() * p2y)) / surfaceView.getHeight()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_EANUPC)[2] / 100) * (surfaceView.getWidth() * p2x))
                                / surfaceView.getWidth()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_EANUPC)[3] / 100) * (surfaceView.getWidth() * p2y))
                                / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_PDF)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_PDF)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_PDF)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_PDF)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_QR)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_QR)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_QR)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_QR)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_RSS)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_RSS)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_RSS)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_RSS)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR,
                        (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_CODABAR)[0] / 100)
                                * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                        (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_CODABAR)[1] / 100)
                                * (surfaceView.getHeight() * p2y)) / surfaceView.getHeight()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_CODABAR)[2] / 100) * (surfaceView.getWidth() * p2x))
                                / surfaceView.getWidth()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_CODABAR)[3] / 100) * (surfaceView.getWidth() * p2y))
                                / surfaceView.getWidth()) * 100);
                BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE,
                        (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DOTCODE)[0] / 100)
                                * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                        (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DOTCODE)[1] / 100)
                                * (surfaceView.getHeight() * p2y)) / surfaceView.getHeight()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DOTCODE)[2] / 100) * (surfaceView.getWidth() * p2x))
                                / surfaceView.getWidth()) * 100,
                        (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_DOTCODE)[3] / 100) * (surfaceView.getWidth() * p2y))
                                / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_11)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_11)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_11)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_11)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);
                BarcodeScanner
                        .MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI,
                                (p1x + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_MSI)[0] / 100)
                                        * (surfaceView.getWidth() * p2x)) / surfaceView.getWidth()) * 100,
                                (p1y + ((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_MSI)[1] / 100) * (surfaceView.getHeight() * p2y))
                                        / surfaceView.getHeight()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_MSI)[2] / 100) * (surfaceView.getWidth() * p2x))
                                        / surfaceView.getWidth()) * 100,
                                (((BarcodeScanner.MWBgetScanningRectArray(BarcodeScanner.MWB_CODE_MASK_MSI)[3] / 100) * (surfaceView.getWidth() * p2y))
                                        / surfaceView.getWidth()) * 100);

            }

        }
    }

    public static int MAX_IMAGE_SIZE = 1280;

    public static ImageInfo bitmapToGrayscale(String imageUri) {

        File image = new File(imageUri);
        if (image == null) {
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

        if (bmOptions.outHeight <= 0 || bmOptions.outWidth <= 0) {
            return null;
        }

        int height = bmOptions.outHeight;
        int width = bmOptions.outWidth;
        int inSampleSize = 1;

        while (height > MAX_IMAGE_SIZE || width > MAX_IMAGE_SIZE) {

            height = height / 2;
            width = width / 2;
            inSampleSize *= 2;
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = inSampleSize;
        bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

        if (bitmap == null) {
            return null;
        }
        // convert bitmap to ARGB8888 format for any case
        Bitmap argbBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(argbBitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, 0, 0, paint);

        int[] pixels = new int[width * height];

        argbBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        ImageInfo imageInfo = new ImageInfo(width, height);

        for (int i = 0; i < width * height; i++) {
            int color = pixels[i];
            int B = (int) (color & 0xff);
            int G = (int) ((color >> 8) & 0xff);
            int R = (int) ((color >> 16) & 0xff);

            int fgray = (int) (0.299 * R + 0.587 * G + 0.114 * B);
            if (fgray < 0) {
                fgray = 0;
            }
            if (fgray > 255) {
                fgray = 255;
            }

            imageInfo.pixels[i] = (byte) (fgray);
        }
        argbBitmap.recycle();
        bitmap.recycle();
        bitmap = null;
        argbBitmap = null;
        canvas = null;
        paint = null;

        return imageInfo;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {

            if (resultCode == 1 && ScannerActivity.param_closeOnSuccess) {
                JSONObject jsonResult = new JSONObject();
                try {
                    jsonResult.put("code", intent.getStringExtra("code"));
                    jsonResult.put("type", intent.getStringExtra("type"));
                    jsonResult.put("isGS1", (BarcodeScanner.MWBisLastGS1() == 1));

                    JSONArray rawArray = new JSONArray();
                    byte[] bytes = intent.getByteArrayExtra("bytes");
                    if (bytes != null) {
                        for (int i = 0; i < bytes.length; i++) {
                            rawArray.put((int) (0xff & bytes[i]));
                        }
                    }

                    jsonResult.put("bytes", rawArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cbc.success(jsonResult);

            } else if (resultCode == 0) {
                JSONObject jsonResult = new JSONObject();
                try {
                    jsonResult.put("code", "");
                    jsonResult.put("type", "Cancel");
                    jsonResult.put("bytes", "");

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                cbc.success(jsonResult);

            }

        }
    }

    public static void initDecoder() {

        // //You should perform registering calls from MWBScanner.js!

		/*
         * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_25,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_39,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_93,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_128,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_AZTEC,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_DM,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_EANUPC,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_PDF,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_QR,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_RSS,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_CODABAR
		 * ,"username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_DOTCODE
		 * ,"username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_11,
		 * "username", "key");
		 * BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_MSI,
		 * "username", "key");
		 */
        // choose code type or types you want to search for

        // Our sample app is configured by default to search all supported
        // barcodes...
        BarcodeScanner.MWBsetActiveCodes(
                BarcodeScanner.MWB_CODE_MASK_25 | BarcodeScanner.MWB_CODE_MASK_39 | BarcodeScanner.MWB_CODE_MASK_93 | BarcodeScanner.MWB_CODE_MASK_128
                        | BarcodeScanner.MWB_CODE_MASK_AZTEC | BarcodeScanner.MWB_CODE_MASK_DM | BarcodeScanner.MWB_CODE_MASK_EANUPC
                        | BarcodeScanner.MWB_CODE_MASK_PDF | BarcodeScanner.MWB_CODE_MASK_QR | BarcodeScanner.MWB_CODE_MASK_CODABAR
                        | BarcodeScanner.MWB_CODE_MASK_11 | BarcodeScanner.MWB_CODE_MASK_MSI | BarcodeScanner.MWB_CODE_MASK_RSS);

        // But for better performance, only activate the symbologies your
        // application requires...
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_25 );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_39 );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_93 );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_128 );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_AZTEC
        // );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_DM );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_EANUPC
        // );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_PDF );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_QR );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_RSS );
        // BarcodeScanner.MWBsetActiveCodes(
        // BarcodeScanner.MWB_CODE_MASK_CODABAR );
        // BarcodeScanner.MWBsetActiveCodes(
        // BarcodeScanner.MWB_CODE_MASK_DOTCODE );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_11 );
        // BarcodeScanner.MWBsetActiveCodes( BarcodeScanner.MWB_CODE_MASK_MSI );

        // Our sample app is configured by default to search both directions...
        BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL | BarcodeScanner.MWB_SCANDIRECTION_VERTICAL);
        // set the scanning rectangle based on scan direction(format in pct: x,
        // y, width, height)
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC, RECT_FULL_2D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM, RECT_FULL_2D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR, RECT_FULL_2D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE, RECT_DOTCODE);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11, RECT_FULL_1D);
        BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI, RECT_FULL_1D);

        // But for better performance, set like this for PORTRAIT scanning...
        // BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_VERTICAL);
        // set the scanning rectangle based on scan direction(format in pct: x,
        // y, width, height)
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC,
        // RECT_PORTRAIT_2D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM,
        // RECT_PORTRAIT_2D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR,
        // RECT_PORTRAIT_2D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR,RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE,RECT_DOTCODE);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11,
        // RECT_PORTRAIT_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI,
        // RECT_PORTRAIT_1D);

        // or like this for LANDSCAPE scanning - Preferred for dense or wide
        // codes...
        // BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL);
        // set the scanning rectangle based on scan direction(format in pct: x,
        // y, width, height)
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_25,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_39,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_93,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_128,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_AZTEC,
        // RECT_LANDSCAPE_2D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DM,
        // RECT_LANDSCAPE_2D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_EANUPC,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_QR,
        // RECT_LANDSCAPE_2D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_RSS,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_CODABAR,RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_DOTCODE,RECT_DOTCODE);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_11,
        // RECT_LANDSCAPE_1D);
        // BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_MSI,
        // RECT_LANDSCAPE_1D);

        // Set minimum result length for low-protected barcode types

        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_25, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_MSI, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_39, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_CODABAR, 5);
        BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_11, 5);

        // set decoder effort level (1 - 5)
        // for live scanning scenarios, a setting between 1 to 3 will suffice
        // levels 4 and 5 are typically reserved for batch scanning
        BarcodeScanner.MWBsetLevel(2);

        BarcodeScanner.MWBsetResultType(BarcodeScanner.MWB_RESULT_TYPE_MW);

    }

    private ViewGroup getMainViewGroup() {
        if (webView instanceof WebView) {
            return (ViewGroup) webView;
        } else {
            try {
                java.lang.reflect.Method getView = webView.getClass().getMethod("getView");
                Object viewObject = getView.invoke(webView);
                if (viewObject instanceof View) {
                    View view = (View) viewObject;
                    ViewParent parentView = view.getParent();
                    if (parentView instanceof ViewGroup) {
                        return (ViewGroup) parentView;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isFullscreen(View topLeftView) {
        int location[] = new int[2];
        topLeftView.getLocationOnScreen(location);
        return location[0] == 0 && location[1] == 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (!hasSurface) {

                            hasSurface = true;
                            initCamera(holder);
                        }
                    }
                });
            }
        }, 1);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }


    public void initCamera(SurfaceHolder surfaceHolder) {
        try {
            // Select desired camera resoloution. Not all devices supports all
            // resolutions, closest available will be chosen
            // If not selected, closest match to screen resolution will be
            // chosen
            // High resolutions will slow down scanning proccess on slower
            // devices

            if (ScannerActivity.param_EnableHiRes) {
                CameraManager.setDesiredPreviewSize(1280, 720);
            } else {
                CameraManager.setDesiredPreviewSize(800, 480);
            }
            CameraManager.get().openDriver(surfaceHolder,
                    (cordova.getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT));
            int maxZoom = CameraManager.get().getMaxZoom();
            if (maxZoom <= 100) {
                if (zoomButton != null) {
                    zoomButton.setVisibility(View.GONE);
                }
            } else {
                if (ScannerActivity.param_EnableZoom) {
                    if (zoomButton != null) {
                        zoomButton.setVisibility(View.VISIBLE);
                    }
                }
                ScannerActivity.updateZoom();
            }
        } catch (IOException ioe) {
            // displayFrameworkBugMessageAndExit();
            return;
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            // displayFrameworkBugMessageAndExit();
            return;
        }
        if (ScannerActivity.handler == null) {
            ScannerActivity.handler = new Handler(new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {

                    switch (msg.what) {
                        case ScannerActivity.MSG_AUTOFOCUS:
                            if (ScannerActivity.state == State.PREVIEW || ScannerActivity.state == State.DECODING) {
                                CameraManager.get().requestAutoFocus(ScannerActivity.handler, ScannerActivity.MSG_AUTOFOCUS);
                            }
                            break;
                        case ScannerActivity.MSG_DECODE:
                            ScannerActivity.decode((byte[]) msg.obj, msg.arg1, msg.arg2);
                            break;
                        case ScannerActivity.MSG_DECODE_FAILED:
                            // CameraManager.get().requestPreviewFrame(handler,
                            // MSG_DECODE);
                            break;
                        case ScannerActivity.MSG_DECODE_SUCCESS:
                            ScannerActivity.state = State.STOPPED;
                            handleDecode((MWResult) msg.obj);
                            break;

                        default:
                            break;
                    }

                    return false;
                }
            });
        }

        CameraManager.get().startPreview();
        ScannerActivity.state = State.PREVIEW;
        CameraManager.get().requestPreviewFrame(ScannerActivity.handler, ScannerActivity.MSG_DECODE);
        CameraManager.get().requestAutoFocus(ScannerActivity.handler, ScannerActivity.MSG_AUTOFOCUS);
        scrollView.setVisibility(View.VISIBLE);
        pBar.setVisibility(View.GONE);
        // flashOn = false;
        // updateFlash();


    }

    public void handleDecode(MWResult result) {

        byte[] rawResult = null;

        if (result != null && result.bytes != null) {
            rawResult = result.bytes;
        }

        String s = "";

        try {
            s = new String(rawResult, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            s = "";
            for (byte aRawResult : rawResult) s = s + (char) aRawResult;
            e.printStackTrace();
        }

        int bcType = result.type;
        String typeName = "";
        switch (bcType) {
            case BarcodeScanner.FOUND_25_INTERLEAVED:
                typeName = "Code 25";
                break;
            case BarcodeScanner.FOUND_25_STANDARD:
                typeName = "Code 25 Standard";
                break;
            case BarcodeScanner.FOUND_128:
                typeName = "Code 128";
                break;
            case BarcodeScanner.FOUND_39:
                typeName = "Code 39";
                break;
            case BarcodeScanner.FOUND_93:
                typeName = "Code 93";
                break;
            case BarcodeScanner.FOUND_AZTEC:
                typeName = "AZTEC";
                break;
            case BarcodeScanner.FOUND_DM:
                typeName = "Datamatrix";
                break;
            case BarcodeScanner.FOUND_EAN_13:
                typeName = "EAN 13";
                break;
            case BarcodeScanner.FOUND_EAN_8:
                typeName = "EAN 8";
                break;
            case BarcodeScanner.FOUND_NONE:
                typeName = "None";
                break;
            case BarcodeScanner.FOUND_RSS_14:
                typeName = "Databar 14";
                break;
            case BarcodeScanner.FOUND_RSS_14_STACK:
                typeName = "Databar 14 Stacked";
                break;
            case BarcodeScanner.FOUND_RSS_EXP:
                typeName = "Databar Expanded";
                break;
            case BarcodeScanner.FOUND_RSS_LIM:
                typeName = "Databar Limited";
                break;
            case BarcodeScanner.FOUND_UPC_A:
                typeName = "UPC A";
                break;
            case BarcodeScanner.FOUND_UPC_E:
                typeName = "UPC E";
                break;
            case BarcodeScanner.FOUND_PDF:
                typeName = "PDF417";
                break;
            case BarcodeScanner.FOUND_QR:
                typeName = "QR";
                break;
            case BarcodeScanner.FOUND_CODABAR:
                typeName = "Codabar";
                break;
            case BarcodeScanner.FOUND_128_GS1:
                typeName = "Code 128 GS1";
                break;
            case BarcodeScanner.FOUND_ITF14:
                typeName = "ITF 14";
                break;
            case BarcodeScanner.FOUND_11:
                typeName = "Code 11";
                break;
            case BarcodeScanner.FOUND_MSI:
                typeName = "MSI Plessey";
                break;
            case BarcodeScanner.FOUND_25_IATA:
                typeName = "IATA Code 25";
                break;
        }

        if (result.locationPoints != null && CameraManager.get().getCurrentResolution() != null && ScannerActivity.param_OverlayMode == 1) {
            MWOverlay.showLocation(result.locationPoints.points, result.imageWidth, result.imageHeight);
        }

        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("code", s);
            jsonResult.put("type", typeName);
            jsonResult.put("isGS1", result.isGS1);
            jsonResult.put("imageWidth", result.imageWidth);
            jsonResult.put("imageHeight", result.imageHeight);

            if (result.locationPoints != null) {
                jsonResult.put("location",
                        new JSONObject().put("p1", new JSONObject().put("x", result.locationPoints.p1.x).put("y", result.locationPoints.p1.y))
                                .put("p2", new JSONObject().put("x", result.locationPoints.p2.x).put("y", result.locationPoints.p2.y))
                                .put("p3", new JSONObject().put("x", result.locationPoints.p3.x).put("y", result.locationPoints.p3.y))
                                .put("p4", new JSONObject().put("x", result.locationPoints.p4.x).put("y", result.locationPoints.p4.y)));
            } else {
                jsonResult.put("location", false);
            }

            JSONArray rawArray = new JSONArray();
            if (rawResult != null) {
                for (byte aRawResult : rawResult) {
                    rawArray.put((int) (0xff & aRawResult));
                }
            }

            jsonResult.put("bytes", rawArray);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PluginResult pr = new PluginResult(PluginResult.Status.OK, jsonResult);

        if (!ScannerActivity.param_closeOnSuccess) {

            pr.setKeepCallback(true);

        }
        cbc.sendPluginResult(pr);
        if (ScannerActivity.param_closeOnSuccess) {
            stopScanner();
        }
    }

    private void stopScanner() {
        if (rlFullScreen != null) {
            cordova.getActivity().runOnUiThread(new Runnable() {

                public void run() {
                    if (ScannerActivity.param_OverlayMode == 1) {
                        MWOverlay.removeOverlay();
                    } else if (ScannerActivity.param_OverlayMode == 2) {
                        rlSurfaceContainer.removeView(overlayImage);
                    }
                    CameraManager.get().stopPreview();
                    CameraManager.get().closeDriver();
                    getMainViewGroup().removeView(rlFullScreen);
                    rlFullScreen = null;
                    rlSurfaceContainer = null;
                    surfaceView = null;
                    scrollView = null;
                    flashButton = null;
                }
            });

        }
    }

    private void toggleFlash() {
        ScannerActivity.flashOn = !ScannerActivity.flashOn;
        updateFlash();
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                stopScanner();
                if (ScannerActivity.activity != null) {
                    ScannerActivity.activity.finish();
                }
                return;
            }
            if (r == PackageManager.PERMISSION_GRANTED) {
                if (requestCode==123)
                    startScannerView();
                else if (requestCode == 234){
                    Context context = this.cordova.getActivity().getApplicationContext();
                    Intent intent = new Intent(context, com.manateeworks.ScannerActivity.class);
                    this.cordova.startActivityForResult(this, intent, 1);
                }
            }
        }

    }


    @SuppressLint("NewApi")
    private void startScannerView() {
        if (cordova.hasPermission(Manifest.permission.CAMERA)) {
            if (rlFullScreen == null) {
                rects = null;
                final ViewGroup viewGroupToAddTo = getMainViewGroup();
                int w = cordova.getActivity().findViewById(android.R.id.content).getWidth();
                int h = cordova.getActivity().findViewById(android.R.id.content).getHeight();

                WindowManager wm = (WindowManager) cordova.getActivity().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();

                final Point size = new Point();
                display.getSize(size);

                final float AR = (float) size.y / (float) size.x;

                final double x = xP / 100 * w;
                final double y = yP / 100 * h;
                final double width = widthP / 100 * w;
                final double height = heightP / 100 * h;

                cordova.getActivity().runOnUiThread(new Runnable() {

                    public void run() {
                        CameraManager.init(cordova.getActivity());

                        rlFullScreen = new RelativeLayout(cordova.getActivity());
                        rlSurfaceContainer = new RelativeLayout(cordova.getActivity());
                        scrollView = new ScrollView(cordova.getActivity());
                        scrollView.setVerticalScrollBarEnabled(false);
                        scrollView.setOnTouchListener(new OnTouchListener() {

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });
                        scrollView.setVisibility(View.INVISIBLE);
                        surfaceView = new SurfaceView(cordova.getActivity());
                        pBar = new ProgressBar(cordova.getActivity());
                        RelativeLayout.LayoutParams pBarParams = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        pBarParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        pBar.setLayoutParams(pBarParams);
                        pBar.setVisibility(View.VISIBLE);
                        rlFullScreen.setLayoutParams(
                                new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

                        LayoutParams lps = new LayoutParams((int) Math.round(width), (int) Math.round(height));
                        lps.leftMargin = (int) Math.round(x);
                        lps.topMargin = (int) Math.round(y);
                        int heightTmp = 0;
                        int widthTmp = 0;

                        if (width * AR >= height) {
                            heightTmp = (int) Math.round(width * AR);
                            widthTmp = (int) Math.round(width);
                        } else {
                            widthTmp = (int) Math.round(height / AR);
                            heightTmp = (int) Math.round(height);
                        }
                        final float heightTmpRunnable = heightTmp;
                        rlSurfaceContainer.setLayoutParams(new LayoutParams(Math.round(widthTmp), Math.round(heightTmp)));
                        surfaceView.setLayoutParams(new LayoutParams(Math.round(widthTmp), Math.round(heightTmp)));

                        rlSurfaceContainer.addView(surfaceView);

                        if (ScannerActivity.param_EnableFlash) {
                            int widthHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, cordova.getActivity().getResources().getDisplayMetrics());
                            int marginDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, cordova.getActivity().getResources().getDisplayMetrics());
                            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, cordova.getActivity().getResources().getDisplayMetrics());

                            flashButton = new ImageButton(cordova.getActivity());
                            LayoutParams flashParams = new LayoutParams(widthHeight, widthHeight);
                            flashParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            flashParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            flashParams.topMargin = (int) ((heightTmp - height) / 2) + marginDP;
                            flashParams.rightMargin = (int) ((widthTmp - width) / 2) + marginDP;
                            flashButton.setImageResource(cordova.getActivity().getResources().getIdentifier("flashbuttonoff", "drawable", cordova.getActivity().getApplication().getPackageName()));
                            flashButton.setScaleType(ScaleType.FIT_XY);
                            flashButton.setPadding(padding, padding, padding, padding);
                            flashButton.setBackground(null);
                            if (flashButton != null) {
                                flashButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        toggleFlash();

                                    }
                                });
                            }

                            rlSurfaceContainer.addView(flashButton, flashParams);
                            rlSurfaceContainer.bringChildToFront(flashButton);
                        }

                        if (ScannerActivity.param_EnableZoom) {
                            int widthHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, cordova.getActivity().getResources().getDisplayMetrics());
                            int marginDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, cordova.getActivity().getResources().getDisplayMetrics());
                            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, cordova.getActivity().getResources().getDisplayMetrics());

                            zoomButton = new ImageButton(cordova.getActivity());
                            LayoutParams zoomParams = new LayoutParams(widthHeight, widthHeight);
                            zoomParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            zoomParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            zoomParams.topMargin = (int) ((heightTmp - height) / 2) + marginDP;
                            zoomButton.setPadding(padding, padding, padding, padding);
                            zoomParams.leftMargin = (int) ((widthTmp - width) / 2) + marginDP;
                            zoomButton.setImageResource(cordova.getActivity().getResources().getIdentifier("zoom", "drawable", cordova.getActivity().getApplication().getPackageName()));

                            zoomButton.setScaleType(ScaleType.FIT_XY);
                            zoomButton.setBackground(null);
                            if (zoomButton != null) {
                                zoomButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ScannerActivity.toggleZoom();

                                    }
                                });
                            }

                            rlSurfaceContainer.addView(zoomButton, zoomParams);
                            rlSurfaceContainer.bringChildToFront(zoomButton);
                        }

                        scrollView.addView(rlSurfaceContainer);

                        scrollView.setClipToPadding(true);
                        scrollView.setLayoutParams(lps);
                        rlFullScreen.addView(scrollView);
                        viewGroupToAddTo.addView(rlFullScreen);

                        if (xP == 0 && yP == 0 && widthP == 1 && heightP == 1) {
                            rlFullScreen.setVisibility(View.INVISIBLE);
                        }

                        rlFullScreen.addView(pBar);

                        new Timer().schedule(new TimerTask() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                cordova.getActivity().runOnUiThread(new Runnable() {
                                    public void run() {

                                        setAutoRect();
                                        scrollView.scrollTo(0, (int) (heightTmpRunnable / 2 - height / 2));
                                        if (ScannerActivity.param_OverlayMode == 1) {
                                            MWOverlay.addOverlay(cordova.getActivity(), surfaceView);
                                            MWOverlay.setPaused(false);
                                        } else if (ScannerActivity.param_OverlayMode == 2) {
                                            overlayImage = new ImageView(cordova.getActivity());
                                            overlayImage.setScaleType(ScaleType.FIT_XY);
                                            overlayImage.setImageResource(cordova.getActivity().getResources().getIdentifier("overlay_mw", "drawable",
                                                    cordova.getActivity().getPackageName()));
                                            LayoutParams lps = new LayoutParams((int) Math.round(width), (int) Math.round(height));
                                            lps.topMargin = (int) Math.round(heightTmpRunnable / 2 - height / 2);
                                            rlSurfaceContainer.addView(overlayImage, lps);
                                        }

                                    }
                                });
                            }
                        }, 500);

                        SurfaceHolder surfaceHolder = surfaceView.getHolder();

                        surfaceHolder.addCallback(BarcodeScannerPlugin.this);
                        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                    }
                });

            }
        } else {
            cordova.requestPermission(this, 123, android.Manifest.permission.CAMERA);

        }

    }

}