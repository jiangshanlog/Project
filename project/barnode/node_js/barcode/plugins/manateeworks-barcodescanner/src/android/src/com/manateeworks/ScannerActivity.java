package com.manateeworks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.manateeworks.BarcodeScanner.MWResult;
import com.manateeworks.camera.CameraManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ScannerActivity extends Activity implements SurfaceHolder.Callback {

    public static final int OM_MW = 1;
    public static final int OM_IMAGE = 2;

    public static Handler handler;
    public static final int MSG_DECODE = 1;
    public static final int MSG_AUTOFOCUS = 2;
    public static final int MSG_DECODE_SUCCESS = 3;
    public static final int MSG_DECODE_FAILED = 4;
    public static int param_delayOnDuplicateScan = 0;

    // private byte[] lastResult;
    private boolean hasSurface;
    public static CallbackContext cbc;

    public static int param_Orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    public static boolean param_EnableHiRes = true;
    public static boolean param_EnableFlash = true;
    public static boolean param_EnableZoom = true;
    public static boolean param_DefaultFlashOn = false;
    public static boolean param_closeOnSuccess = true;
    public static boolean param_showLocation = true;
    public static int param_OverlayMode = OM_MW;

    public static int param_ZoomLevel1 = 0;
    public static int param_ZoomLevel2 = 0;
    public static int zoomLevel = 0;
    public static int firstZoom = 150;
    public static int secondZoom = 300;
    public static int param_maxThreads = 4;

    private ImageView overlayImage;
    private ImageButton buttonFlash;
    private ImageButton buttonZoom;

    private String package_name;
    private Resources resources;

    static boolean flashOn = false;

    public static HashMap<String, Object> customParams;

    private static int activeThreads = 0;
    public static int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    public static Activity activity = null;

    public enum State {
        STOPPED, PREVIEW, DECODING
    }

    public static State state = State.STOPPED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (param_Orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            setRequestedOrientation(param_Orientation);
        }


        state = State.STOPPED;
        package_name = getApplication().getPackageName();
        resources = getApplication().getResources();

        setContentView(resources.getIdentifier("scanner", "layout", package_name));
        activity = this;

        overlayImage = (ImageView) findViewById(resources.getIdentifier("overlayImage", "id", package_name));

        buttonFlash = (ImageButton) findViewById(resources.getIdentifier("flashButton", "id", package_name));
        if (buttonFlash != null) {
            buttonFlash.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFlash();

                }
            });
        }

        buttonZoom = (ImageButton) findViewById(resources.getIdentifier("zoomButton", "id", package_name));
        if (buttonZoom != null) {
            buttonZoom.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleZoom();

                }
            });
        }

        CameraManager.init(getApplication());

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (buttonZoom != null) {
            buttonZoom.setVisibility(View.GONE);
        }

        SurfaceView surfaceView = (SurfaceView) findViewById(resources.getIdentifier("preview_view", "id", package_name));
        SurfaceHolder surfaceHolder = surfaceView.getHolder();

        if ((param_OverlayMode & OM_MW) > 0) {
            MWOverlay.addOverlay(this, surfaceView);
        }

        if ((param_OverlayMode & OM_IMAGE) > 0) {
            if (overlayImage != null) {
                overlayImage.setVisibility(View.VISIBLE);
            }
        } else {
            if (overlayImage != null) {
                overlayImage.setVisibility(View.GONE);
            }
        }

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        int ver = BarcodeScanner.MWBgetLibVersion();
        int v1 = (ver >> 16);
        int v2 = (ver >> 8) & 0xff;
        int v3 = (ver & 0xff);
        String libVersion = "Lib version: " + String.valueOf(v1) + "." + String.valueOf(v2) + "." + String.valueOf(v3);
        // Toast.makeText(this, libVersion, Toast.LENGTH_LONG).show();
        Log.i("Lib version", libVersion);

        if (param_DefaultFlashOn) {

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    flashOn = true;
                    updateFlash();
                }
            }, 1000);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        flashOn = false;
        updateFlash();
        if ((param_OverlayMode & OM_MW) > 0) {
            MWOverlay.removeOverlay();
        }
        if (handler != null) {
            CameraManager.get().stopPreview();
            handler = null;
        }
        CameraManager.get().closeDriver();
        state = State.STOPPED;

    }

    private void toggleFlash() {
        flashOn = !flashOn;
        updateFlash();
    }

    public static void toggleZoom() {

        zoomLevel++;
        if (zoomLevel > 2) {
            zoomLevel = 0;
        }

        updateZoom();
    }

    public static void updateZoom() {

        if (param_ZoomLevel1 == 0 || param_ZoomLevel2 == 0) {
            firstZoom = 150;
            secondZoom = 300;
        } else {
            firstZoom = param_ZoomLevel1;
            secondZoom = param_ZoomLevel2;

            int maxZoom = CameraManager.get().getMaxZoom();

            if (maxZoom < secondZoom) {
                secondZoom = maxZoom;
            }
            if (maxZoom < firstZoom) {
                firstZoom = maxZoom;
            }

        }

        switch (zoomLevel) {
            case 0:
                CameraManager.get().setZoom(100);
                break;
            case 1:
                CameraManager.get().setZoom(firstZoom);
                break;
            case 2:
                CameraManager.get().setZoom(secondZoom);
                break;

            default:
                break;
        }
    }

    private void updateFlash() {

        if (buttonFlash != null) {
            if (!CameraManager.get().isTorchAvailable() || !param_EnableFlash) {
                buttonFlash.setVisibility(View.GONE);
                return;

            } else {
                buttonFlash.setVisibility(View.VISIBLE);
            }

            if (flashOn) {
                buttonFlash.setImageResource(resources.getIdentifier("flashbuttonon", "drawable", package_name));
            } else {
                buttonFlash.setImageResource(resources.getIdentifier("flashbuttonoff", "drawable", package_name));
            }

            CameraManager.get().setTorch(flashOn);

            buttonFlash.postInvalidate();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

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

            if (param_EnableHiRes) {
                CameraManager.setDesiredPreviewSize(1280, 720);
            } else {
                CameraManager.setDesiredPreviewSize(800, 480);
            }

            CameraManager.get().openDriver(surfaceHolder, (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT));

            int maxZoom = CameraManager.get().getMaxZoom();
            if (maxZoom <= 100) {
                if (buttonZoom != null) {
                    buttonZoom.setVisibility(View.GONE);
                }
            } else {
                if (param_EnableZoom) {
                    if (buttonZoom != null) {
                        buttonZoom.setVisibility(View.VISIBLE);
                    }
                }
                updateZoom();
            }
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
            return;
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            displayFrameworkBugMessageAndExit();
            return;
        }
        if (handler == null) {
            handler = new Handler(new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {

                    switch (msg.what) {
                        case MSG_AUTOFOCUS:
                            if (state == State.PREVIEW || state == State.DECODING) {
                                CameraManager.get().requestAutoFocus(handler, MSG_AUTOFOCUS);
                            }
                            break;
                        case MSG_DECODE:
                            decode((byte[]) msg.obj, msg.arg1, msg.arg2);
                            break;
                        case MSG_DECODE_FAILED:
                            // CameraManager.get().requestPreviewFrame(handler,
                            // MSG_DECODE);
                            break;
                        case MSG_DECODE_SUCCESS:
                            state = State.STOPPED;
                            handleDecode((MWResult) msg.obj);
                            break;

                        default:
                            break;
                    }

                    return false;
                }
            });
        }

        startScanning();

        flashOn = false;
        updateFlash();

    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(resources.getIdentifier("app_name", "string", package_name)));
        builder.setMessage("Camera error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.show();
    }

    public static void startScanning() {
        CameraManager.get().startPreview();
        state = State.PREVIEW;
        CameraManager.get().requestPreviewFrame(handler, MSG_DECODE);
        CameraManager.get().requestAutoFocus(handler, MSG_AUTOFOCUS);
    }

    public static void decode(final byte[] data, final int width, final int height) {
        if (param_maxThreads > MAX_THREADS) {
            param_maxThreads = MAX_THREADS;
        }

        if (activeThreads >= param_maxThreads || state == State.STOPPED) {
            return;
        }

        new Thread(new Runnable() {
            public void run() {
                activeThreads++;
                // Log.i("Active threads", String.valueOf(activeThreads));
                long start = System.currentTimeMillis();

                // byte[] source =
                // CameraManager.get().buildLuminanceSource(data, width,
                // height);

                byte[] rawResult = null;
                /*
				 * if (Global.mode_39) rawResult =
				 * BarcodeScanner.decode39(source, w, h); else rawResult =
				 * BarcodeScanner.decodeDM(source, w, h);
				 */

                rawResult = BarcodeScanner.MWBscanGrayscaleImage(data, width, height);

                if (state == State.STOPPED) {
                    activeThreads--;
                    return;
                }

                MWResult mwResult = null;

                if (rawResult != null && BarcodeScanner.MWBgetResultType() == BarcodeScanner.MWB_RESULT_TYPE_MW) {

                    BarcodeScanner.MWResults results = new BarcodeScanner.MWResults(rawResult);

                    if (results.count > 0) {
                        mwResult = results.getResult(0);
                        rawResult = mwResult.bytes;
                    }

                }

                if (rawResult != null) {

                    state = State.STOPPED;
                    BarcodeScanner.MWBsetDuplicate(mwResult.bytes, mwResult.bytesLength);
                    MWOverlay.setPaused(true);

                    long end = System.currentTimeMillis();

                    String s = "";

                    for (int i = 0; i < rawResult.length; i++)
                        s = s + (char) rawResult[i];

                    Message message = Message.obtain(handler, MSG_DECODE_SUCCESS, mwResult);

					/*
					 * Bundle bundle = new Bundle();
					 * bundle.putParcelable(DecodeThread.BARCODE_BITMAP,
					 * CameraManager.get().renderCroppedGreyscaleBitmap(data, w,
					 * h)); message.setData(bundle);
					 */
                    message.arg1 = mwResult.type;

                    message.sendToTarget();

                } else if (handler != null) {
                    Message message = Message.obtain(handler, MSG_DECODE_FAILED);
                    message.sendToTarget();
                }

                activeThreads--;
            }
        }).start();

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
            for (int i = 0; i < rawResult.length; i++)
                s = s + (char) rawResult[i];
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

        if (result.locationPoints != null && CameraManager.get().getCurrentResolution() != null) {

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
                    rawArray.put(0xff & aRawResult);
                }
            }

            jsonResult.put("bytes", rawArray);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PluginResult pr = new PluginResult(PluginResult.Status.OK, jsonResult);

        if (param_closeOnSuccess) {

            activity.finish();
        } else {
            pr.setKeepCallback(true);

        }
        ScannerActivity.cbc.sendPluginResult(pr);

    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
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
        finish();
    }

}
