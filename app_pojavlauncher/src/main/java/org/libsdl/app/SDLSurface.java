

package org.libsdl.app;


import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Insets;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import android.view.ScaleGestureDetector;

import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;


public class SDLSurface extends SurfaceView implements SurfaceHolder.Callback,
    View.OnApplyWindowInsetsListener, View.OnKeyListener, View.OnTouchListener,
    SensorEventListener, ScaleGestureDetector.OnScaleGestureListener {

    
    protected SensorManager mSensorManager;
    protected Display mDisplay;

    
    protected float mWidth, mHeight;

    
    protected boolean mIsSurfaceReady;

    
    protected boolean mKeyboardVisible;

    
    private final ScaleGestureDetector scaleGestureDetector;
    static Surface mNativeSurface;

    
    public SDLSurface(Context context) {
        super(context);
        getHolder().addCallback(this);

        scaleGestureDetector = new ScaleGestureDetector(context, this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnApplyWindowInsetsListener(this);
        setOnKeyListener(this);
        setOnTouchListener(this);

        mDisplay = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        setOnGenericMotionListener(SDLActivity.getMotionListener());




        
        
        
        mWidth = Tools.currentDisplayMetrics.widthPixels;
        mHeight = Tools.currentDisplayMetrics.heightPixels;

        mIsSurfaceReady = false;
    }

    protected void handlePause() {
        enableSensor(Sensor.TYPE_ACCELEROMETER, false);
    }

    protected void handleResume() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnApplyWindowInsetsListener(this);
        setOnKeyListener(this);
        setOnTouchListener(this);
        enableSensor(Sensor.TYPE_ACCELEROMETER, true);
    }

    public static Surface getNativeSurface() {
        return mNativeSurface;
    }

    public static void setNativeSurface(Surface nativeSurface) {
        mNativeSurface = nativeSurface;
        SDLActivity.getSDLSurface().surfaceCreated(null);
    }

    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!MinecraftGLSurface.sdlEnabled) return;
        Log.v("SDL", "surfaceCreated()");
        SDLActivity.onNativeSurfaceCreated();
    }

    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!MinecraftGLSurface.sdlEnabled) return;
        Log.v("SDL", "surfaceDestroyed()");

        
        SDLActivity.mNextNativeState = SDLActivity.NativeState.PAUSED;
        SDLActivity.handleNativeState();

        mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();
    }

    public void surfaceChanged(){
        
        surfaceChanged(null, 0, Tools.currentDisplayMetrics.widthPixels, Tools.currentDisplayMetrics.heightPixels);
    }

    
    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        if (!MinecraftGLSurface.sdlEnabled) return;
        Log.v("SDL", "surfaceChanged()");

        if (SDLActivity.mSingleton == null) {
            return;
        }

        mWidth = width;
        mHeight = height;
        int nDeviceWidth = width;
        int nDeviceHeight = height;
        float density = 1.0f;
        try
        {
            DisplayMetrics realMetrics = new DisplayMetrics();
            mDisplay.getRealMetrics( realMetrics );
            nDeviceWidth = realMetrics.widthPixels;
            nDeviceHeight = realMetrics.heightPixels;
            
            density = (float)realMetrics.densityDpi / 160.0f;
        } catch(Exception ignored) {
        }

        synchronized(SDLActivity.getContext()) {
            
            SDLActivity.getContext().notifyAll();
        }
        Log.v("SDL", "Window size: " + width + "x" + height);
        Log.v("SDL", "Device size: " + nDeviceWidth + "x" + nDeviceHeight);
        
        
        boolean skip = false;
        int requestedOrientation = SDLActivity.mSingleton.getRequestedOrientation();

        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
            if (mWidth > mHeight) {
               skip = true;
            }
        } else if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            if (mWidth < mHeight) {
               skip = true;
            }
        }

        
        if (skip) {
           double min = Math.min(mWidth, mHeight);
           double max = Math.max(mWidth, mHeight);

           if (max / min < 1.20) {
              Log.v("SDL", "Don't skip on such aspect-ratio. Could be a square resolution.");
              skip = false;
           }
        }

        
        if (skip) {
            if (Build.VERSION.SDK_INT >= 24 ) {
                skip = false;
            }
        }

        if (skip) {
           Log.v("SDL", "Skip .. Surface is not ready.");
           mIsSurfaceReady = false;
           return;
        }

        
        SDLActivity.onNativeSurfaceChanged();

        
        mIsSurfaceReady = true;

        SDLActivity.mNextNativeState = SDLActivity.NativeState.RESUMED;
        SDLActivity.handleNativeState();
    }

    public void nativeResize(int w, int h){
        DisplayMetrics realMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics( realMetrics );
        SDLActivity.nativeSetScreenResolution(w, h, w, h, (float)realMetrics.densityDpi / 160.0f, mDisplay.getRefreshRate());
        SDLActivity.onNativeResize();
    }

    
    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= 30 ) {
            Insets combined = insets.getInsets(WindowInsets.Type.systemBars() |
                                               WindowInsets.Type.systemGestures() |
                                               WindowInsets.Type.mandatorySystemGestures() |
                                               WindowInsets.Type.tappableElement() |
                                               WindowInsets.Type.displayCutout());

            SDLActivity.onNativeInsetsChanged(combined.left, combined.right, combined.top, combined.bottom);

            if (insets.isVisible(WindowInsets.Type.ime())) {
                if (!mKeyboardVisible) {
                    mKeyboardVisible = true;
                    SDLActivity.onNativeScreenKeyboardShown();
                }
            } else {
                if (mKeyboardVisible) {
                    mKeyboardVisible = false;
                    SDLActivity.onNativeScreenKeyboardHidden();
                }
            }
        }

        
        return insets;
    }

    
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return SDLActivity.handleKeyEvent(v, keyCode, event, null);
    }

    private float getNormalizedX(float x)
    {
        if (mWidth <= 1) {
            return 0.5f;
        } else {
            return (x / (mWidth - 1));
        }
    }

    private float getNormalizedY(float y)
    {
        if (mHeight <= 1) {
            return 0.5f;
        } else {
            return (y / (mHeight - 1));
        }
    }

    


    @Override
    public boolean onTouch(View v, MotionEvent event) {


































































        return false;
    }

    
    protected void enableSensor(int sensortype, boolean enabled) {
        
        if (enabled) {
            SDLSensorManager.registerListener(mSensorManager, this,
                            mSensorManager.getDefaultSensor(sensortype),
                            SensorManager.SENSOR_DELAY_GAME);
        } else {
            SDLSensorManager.unregisterListener(mSensorManager, this,
                            mSensorManager.getDefaultSensor(sensortype));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            
            
            int newRotation;

            float x, y;
            switch (mDisplay.getRotation()) {
                case Surface.ROTATION_0:
                default:
                    x = event.values[0];
                    y = event.values[1];
                    newRotation = 0;
                    break;
                case Surface.ROTATION_90:
                    x = -event.values[1];
                    y = event.values[0];
                    newRotation = 90;
                    break;
                case Surface.ROTATION_180:
                    x = -event.values[0];
                    y = -event.values[1];
                    newRotation = 180;
                    break;
                case Surface.ROTATION_270:
                    x = event.values[1];
                    y = -event.values[0];
                    newRotation = 270;
                    break;
            }

            if (newRotation != SDLActivity.mCurrentRotation) {
                SDLActivity.mCurrentRotation = newRotation;
                SDLActivity.onNativeRotationChanged(newRotation);
            }

            SDLActivity.onNativeAccel(-x / SensorManager.GRAVITY_EARTH,
                                      y / SensorManager.GRAVITY_EARTH,
                                      event.values[2] / SensorManager.GRAVITY_EARTH);


        }
    }

    
    @Override
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        try {
            return super.onResolvePointerIcon(event, pointerIndex);
        } catch (NullPointerException e) {
            return null;
        }
    }

    
    @Override
    public boolean onCapturedPointerEvent(MotionEvent event)
    {
        int action = event.getActionMasked();
        int pointerCount = event.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            float x, y;
            switch (action) {
                case MotionEvent.ACTION_SCROLL:
                    x = event.getAxisValue(MotionEvent.AXIS_HSCROLL, i);
                    y = event.getAxisValue(MotionEvent.AXIS_VSCROLL, i);
                    SDLActivity.onNativeMouse(0, action, x, y, false);
                    return true;

                case MotionEvent.ACTION_HOVER_MOVE:
                case MotionEvent.ACTION_MOVE:
                    x = event.getX(i);
                    y = event.getY(i);
                    SDLActivity.onNativeMouse(0, action, x, y, true);
                    return true;

                case MotionEvent.ACTION_BUTTON_PRESS:
                case MotionEvent.ACTION_BUTTON_RELEASE:

                    
                    if (action == MotionEvent.ACTION_BUTTON_PRESS) {
                        action = MotionEvent.ACTION_DOWN;
                    } else { 
                        action = MotionEvent.ACTION_UP;
                    }

                    x = event.getX(i);
                    y = event.getY(i);
                    int button = event.getButtonState();

                    SDLActivity.onNativeMouse(button, action, x, y, true);
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor();
        SDLActivity.onNativePinchUpdate(scale);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        SDLActivity.onNativePinchStart();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        SDLActivity.onNativePinchEnd();
    }

}
