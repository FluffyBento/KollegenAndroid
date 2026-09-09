package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.MainActivity.touchCharInput;
import static net.kdt.pojavlaunch.Tools.LOCAL_RENDERER;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_MOUSE_GRAB_FORCE;
import static net.kdt.pojavlaunch.utils.MCOptionUtils.getMcScale;
import static org.lwjgl.glfw.CallbackBridge.sendMouseButton;
import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.gamepad.DefaultDataProvider;
import net.kdt.pojavlaunch.customcontrols.gamepad.Gamepad;
import net.kdt.pojavlaunch.customcontrols.gamepad.direct.DirectGamepad;
import net.kdt.pojavlaunch.customcontrols.gamepad.direct.DirectGamepadEnableHandler;
import net.kdt.pojavlaunch.customcontrols.mouse.AbstractTouchpad;
import net.kdt.pojavlaunch.customcontrols.mouse.AndroidPointerCapture;
import net.kdt.pojavlaunch.customcontrols.mouse.InGUIEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.InGameEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.TouchEventProcessor;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.JREUtils;
import net.kdt.pojavlaunch.utils.MCOptionUtils;
import net.kdt.pojavlaunch.utils.TouchControllerUtils;

import org.libsdl.app.SDL;
import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLControllerManager;
import org.libsdl.app.SDLSurface;
import org.lwjgl.glfw.CallbackBridge;


import fr.spse.gamepad_remapper.GamepadHandler;
import fr.spse.gamepad_remapper.RemapperManager;
import fr.spse.gamepad_remapper.RemapperView;


public class MinecraftGLSurface extends View implements GrabListener, DirectGamepadEnableHandler {
    
    private GamepadHandler mGamepadHandler;
    
    private final RemapperManager mInputManager = new RemapperManager(getContext(), new RemapperView.Builder(null)
            .remapA(true)
            .remapB(true)
            .remapX(true)
            .remapY(true)

            .remapLeftJoystick(true)
            .remapRightJoystick(true)
            .remapStart(true)
            .remapSelect(true)
            .remapLeftShoulder(true)
            .remapRightShoulder(true)
            .remapLeftTrigger(true)
            .remapRightTrigger(true)
            .remapDpad(true));

    
    private final double mSensitivityFactor = (1.4 * (1080f/ Tools.getDisplayMetrics((Activity) getContext()).heightPixels));

    
    SurfaceReadyListener mSurfaceReadyListener = null;
    final Object mSurfaceReadyListenerLock = new Object();
    
    View mSurface;
    Surface mNativeSurface;
    String TAG = "MinecraftGLSurface";

    private final InGameEventProcessor mIngameProcessor = new InGameEventProcessor(mSensitivityFactor);
    private final InGUIEventProcessor mInGUIProcessor = new InGUIEventProcessor();
    private TouchEventProcessor mCurrentTouchProcessor = mInGUIProcessor;
    private AndroidPointerCapture mPointerCapture;
    private boolean mLastGrabState = false;
    public static boolean sdlEnabled = false;
    boolean useSurfaceView = LauncherPreferences.PREF_USE_ALTERNATE_SURFACE;

    public MinecraftGLSurface(Context context) {
        this(context, null);
    }

    public MinecraftGLSurface(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
        CallbackBridge.setDirectGamepadEnableHandler(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpPointerCapture(AbstractTouchpad touchpad) {
        if(mPointerCapture != null) mPointerCapture.detach();
        mPointerCapture = new AndroidPointerCapture(touchpad, this);
    }
    protected static View.OnGenericMotionListener motionListener = (v, event) -> false;
    private static void setupSDL(Context ctx, Surface nativeSurface, ViewGroup layout){
        SDLSurface surface = new SDLSurface(ctx);
        motionListener = SDLActivity.getMotionListener();
        
        
        org.libsdl.app.SDL.initialize();
        SDL.setContext((MainActivity) ctx);
        SDLActivity.externalInitialize(surface, layout, nativeSurface);
    }

    
    public void start(boolean isAlreadyRunning, AbstractTouchpad touchpad){
        if(Tools.isAndroid8OrHigher()) setUpPointerCapture(touchpad);
        mInGUIProcessor.setAbstractTouchpad(touchpad);
        
        
        
        
        try {
            useSurfaceView = useSurfaceView && !LOCAL_RENDERER.equals("opengles3_desktopgl_zink_kopper") &&
                    !Tools.hasMods("angelica") &&
                    !Tools.hasMods("lwjgl3ify-3");
        } catch (NullPointerException ignored){}
        if(useSurfaceView){
            SurfaceView surfaceView = new SurfaceView(getContext());
            mSurface = surfaceView;
            mNativeSurface = surfaceView.getHolder().getSurface();
            setupSDL(getContext(), mNativeSurface, (ViewGroup) getParent());
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                private boolean isCalled = isAlreadyRunning;
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    if(isCalled) {
                        JREUtils.setupBridgeWindow(mNativeSurface);
                        if (sdlEnabled) SDLSurface.setNativeSurface(mNativeSurface);
                        refreshSize(true);
                        return;
                    }
                    isCalled = true;

                    realStart(mNativeSurface);
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    
                    
                    if (sdlEnabled) SDLActivity.getSDLSurface().surfaceChanged(holder, format, Tools.currentDisplayMetrics.widthPixels, Tools.currentDisplayMetrics.heightPixels);
                    refreshSize();
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    
                    if (sdlEnabled) SDLActivity.getSDLSurface().surfaceDestroyed(holder);
                }
            });

            ((ViewGroup)getParent()).addView(surfaceView);
        }else{
            TextureView textureView = new TextureView(getContext());
            textureView.setOpaque(true);
            textureView.setAlpha(1.0f);
            mSurface = textureView;

            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                private boolean isCalled = isAlreadyRunning;
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    mNativeSurface = new Surface(surface);
                    setupSDL(getContext(), mNativeSurface, (ViewGroup) getParent());
                    if(isCalled) {
                        JREUtils.setupBridgeWindow(mNativeSurface);
                        if (sdlEnabled) SDLSurface.setNativeSurface(mNativeSurface);
                        return;
                    }
                    isCalled = true;

                    realStart(mNativeSurface);
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                    
                    
                    if (sdlEnabled) SDLActivity.getSDLSurface().surfaceChanged(null, 0, Tools.currentDisplayMetrics.widthPixels, Tools.currentDisplayMetrics.heightPixels);
                    refreshSize();
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    
                    if (sdlEnabled) SDLActivity.getSDLSurface().surfaceDestroyed(null);
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                    
                }
            });

            ((ViewGroup)getParent()).addView(textureView);
        }


    }

    
    @Override
    @SuppressWarnings("accessibility")
    public boolean onTouchEvent(MotionEvent e) {
        
        if(((ControlLayout)getParent()).getModifiable()) return false;

        
        for (int i = 0; i < e.getPointerCount(); i++) {
            int toolType = e.getToolType(i);
            if(toolType == MotionEvent.TOOL_TYPE_MOUSE) {
                if(Tools.isAndroid8OrHigher() &&
                        mPointerCapture != null) {
                    
                    if (!CallbackBridge.isGrabbing() 
                            && !PREF_MOUSE_GRAB_FORCE) {
                        
                        
                        return !dispatchGenericMotionEvent(e);
                    }
                    mPointerCapture.handleAutomaticCapture();
                    return true;
                }
            }else if(toolType != MotionEvent.TOOL_TYPE_STYLUS) continue;

            
            if(CallbackBridge.isGrabbing()) return false;
            CallbackBridge.sendCursorPos(   e.getX(i) * LauncherPreferences.PREF_SCALE_FACTOR, e.getY(i) * LauncherPreferences.PREF_SCALE_FACTOR);
            return true; 
        }
        TouchControllerUtils.processTouchEvent(e, this);
        if (mIngameProcessor == null || mInGUIProcessor == null) return true;
        return mCurrentTouchProcessor.processTouchEvent(e);
    }

    private void createGamepad(View contextView, InputDevice inputDevice) {
        if (CallbackBridge.sGamepadDirectInput) {
            mGamepadHandler = new DirectGamepad();
        } else if (!sdlEnabled) {
            mGamepadHandler = new Gamepad(contextView, inputDevice, DefaultDataProvider.INSTANCE, true);
        }
    }

    
    @SuppressLint("NewApi")
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if(sdlEnabled && Gamepad.isGamepadEvent(event)) {
            final MotionEvent copy = MotionEvent.obtain(event);
            PojavApplication.sExecutorService.execute(()->{
                try {
                    motionListener.onGenericMotion(this, copy);
                    copy.recycle();
                } catch (Throwable ignored) {
                    Log.e(TAG, "SDL failed to send motionevent!");
                }
            });
        }
        super.dispatchGenericMotionEvent(event);
        int mouseCursorIndex = -1;

        if(Gamepad.isGamepadEvent(event)){
            if(mGamepadHandler == null) createGamepad(this, event.getDevice());

            mInputManager.handleMotionEventInput(getContext(), event, mGamepadHandler);
            return true;
        }

        for(int i = 0; i < event.getPointerCount(); i++) {
            if(event.getToolType(i) != MotionEvent.TOOL_TYPE_MOUSE && event.getToolType(i) != MotionEvent.TOOL_TYPE_STYLUS ) continue;
            
            mouseCursorIndex = i;
            break;
        }
        if(mouseCursorIndex == -1) return false; 

        
        updateGrabState(CallbackBridge.isGrabbing());
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_MOVE:
                CallbackBridge.mouseX = (event.getX(mouseCursorIndex) * LauncherPreferences.PREF_SCALE_FACTOR);
                CallbackBridge.mouseY = (event.getY(mouseCursorIndex) * LauncherPreferences.PREF_SCALE_FACTOR);
                CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);
                return true;
            case MotionEvent.ACTION_SCROLL:
                CallbackBridge.sendScroll(event.getAxisValue(MotionEvent.AXIS_HSCROLL), event.getAxisValue(MotionEvent.AXIS_VSCROLL));
                return true;
            case MotionEvent.ACTION_BUTTON_PRESS:
                return sendMouseButtonUnconverted(event.getActionButton(),true);
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return sendMouseButtonUnconverted(event.getActionButton(),false);
            default:
                return false;
        }
    }

    
    public boolean processKeyEvent(KeyEvent event) {
        

        
        int eventKeycode = event.getKeyCode();
        if(eventKeycode == KeyEvent.KEYCODE_UNKNOWN) return true;
        if(eventKeycode == KeyEvent.KEYCODE_VOLUME_DOWN) return false;
        if(eventKeycode == KeyEvent.KEYCODE_VOLUME_UP) return false;
        if(event.getRepeatCount() != 0) return true;
        int action = event.getAction();
        if(action == KeyEvent.ACTION_MULTIPLE) return true;
        
        
        if(action == KeyEvent.ACTION_UP &&
                (event.getFlags() & KeyEvent.FLAG_CANCELED) != 0) return true;

        
        
        if((event.getFlags() & KeyEvent.FLAG_SOFT_KEYBOARD) == KeyEvent.FLAG_SOFT_KEYBOARD){
            if(eventKeycode == KeyEvent.KEYCODE_ENTER) return true; 
            touchCharInput.dispatchKeyEvent(event);
            return true;
        }

        
        if(event.getDevice() != null
                && ( (event.getSource() & InputDevice.SOURCE_MOUSE_RELATIVE) == InputDevice.SOURCE_MOUSE_RELATIVE
                ||   (event.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE)  ){
            if(eventKeycode == KeyEvent.KEYCODE_BACK){
                sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_4, event.getAction() == KeyEvent.ACTION_DOWN);
                return true;
            }else if(eventKeycode == KeyEvent.KEYCODE_FORWARD){
                sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_5, event.getAction() == KeyEvent.ACTION_DOWN);
                return true;
            }
        }
        
        
        boolean isGamepadEvent = Gamepad.isGamepadEvent(event);
        if (sdlEnabled && isGamepadEvent) {
            final KeyEvent copy = new KeyEvent(event);
            PojavApplication.sExecutorService.execute(() -> {
                try {
                    SDLActivity.handleKeyEvent(this, eventKeycode, copy, null);
                } catch (Throwable ignored) {
                    Log.e(TAG, "SDL failed to send keyevent!");
                }
            });
        }
        if(isGamepadEvent){
            if(mGamepadHandler == null) createGamepad(this, event.getDevice());

            mInputManager.handleKeyEventInput(getContext(), event, mGamepadHandler);
            return true;
        }

        int index = EfficientAndroidLWJGLKeycode.getIndexByKey(eventKeycode);
        if(EfficientAndroidLWJGLKeycode.containsIndex(index)) {
            EfficientAndroidLWJGLKeycode.execKey(event, index);
            return true;
        }

        
        return (event.getFlags() & KeyEvent.FLAG_FALLBACK) == KeyEvent.FLAG_FALLBACK;
    }

    
    public static boolean sendMouseButtonUnconverted(int button, boolean status) {
        int glfwButton = -256;
        switch (button) {
            case MotionEvent.BUTTON_PRIMARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT;
                break;
            case MotionEvent.BUTTON_TERTIARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE;
                break;
            case MotionEvent.BUTTON_SECONDARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT;
                break;
            case MotionEvent.BUTTON_BACK:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_4;
                break;
            case MotionEvent.BUTTON_FORWARD:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_5;
                break;
        }
        if(glfwButton == -256) return false;
        sendMouseButton(glfwButton, status);
        return true;
    }

    
    public void refreshSize(){
        refreshSize(false);
    }

    
    public void refreshSize(boolean immediate) {
        if(isInLayout() && !immediate) {
            post(this::refreshSize);
            return;
        }
        int newWidth;
        int newHeight;
        
        
        
        newWidth = Tools.getDisplayFriendlyRes(getWidth(), LauncherPreferences.PREF_SCALE_FACTOR);
        newHeight = Tools.getDisplayFriendlyRes(getHeight(), LauncherPreferences.PREF_SCALE_FACTOR);
        if (newHeight < 1 || newWidth < 1) {
            Log.e("MGLSurface", String.format("Impossible resolution : %dx%d", newWidth, newHeight));
            return;
        }
        windowWidth = newWidth;
        windowHeight = newHeight;
        if(mSurface == null){
            Log.w("MGLSurface", "Attempt to refresh size on null surface");
            return;
        }
        if(useSurfaceView){
            SurfaceView view = (SurfaceView) mSurface;
            if(view.getHolder() != null){
                view.getHolder().setFixedSize(windowWidth, windowHeight);
            }
        }else{
            TextureView view = (TextureView)mSurface;
            if(view.getSurfaceTexture() != null){
                view.getSurfaceTexture().setDefaultBufferSize(windowWidth, windowHeight);
            }
        }
        if (sdlEnabled) SDLActivity.getSDLSurface().nativeResize(windowWidth, windowHeight);

        CallbackBridge.sendUpdateWindowSize(windowWidth, windowHeight);
    }

    private void realStart(Surface surface){
        
        
        refreshSize(true);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            float maxHz = 120f; 
            for (float altHz : getDisplay().getMode().getAlternativeRefreshRates()) {
                maxHz = Math.max(maxHz, altHz);
            }
            surface.setFrameRate(maxHz, Surface.FRAME_RATE_COMPATIBILITY_DEFAULT, Surface.CHANGE_FRAME_RATE_ONLY_IF_SEAMLESS);
        }

        
        MCOptionUtils.set("fullscreen", "off");
        MCOptionUtils.set("overrideWidth", String.valueOf(windowWidth));
        MCOptionUtils.set("overrideHeight", String.valueOf(windowHeight));
        MCOptionUtils.save();
        getMcScale();

        JREUtils.setupBridgeWindow(surface);

        new Thread(() -> {
            try {
                
                synchronized(mSurfaceReadyListenerLock) {
                    if(mSurfaceReadyListener == null) mSurfaceReadyListenerLock.wait();
                }

                mSurfaceReadyListener.isReady();
            } catch (Throwable e) {
                Tools.showError(getContext(), e, true);
            }
        }, "JVM Main thread").start();
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        post(()->updateGrabState(isGrabbing));
    }

    private TouchEventProcessor pickEventProcessor(boolean isGrabbing) {
        return isGrabbing ? mIngameProcessor : mInGUIProcessor;
    }

    private void updateGrabState(boolean isGrabbing) {
        TouchEventProcessor desiredProcessor = pickEventProcessor(isGrabbing);
        if (mLastGrabState != isGrabbing || mCurrentTouchProcessor != desiredProcessor) {
            mCurrentTouchProcessor.cancelPendingActions();
            mCurrentTouchProcessor = desiredProcessor;
            mLastGrabState = isGrabbing;
        }
    }

    @Override
    public void onDirectGamepadEnabled() {
        post(()->{
            if(mGamepadHandler != null && mGamepadHandler instanceof Gamepad) {
                ((Gamepad)mGamepadHandler).removeSelf();
            }
            
            mGamepadHandler = null;
        });
    }

    
    public interface SurfaceReadyListener {
        void isReady();
    }

    public void setSurfaceReadyListener(SurfaceReadyListener listener){
        synchronized (mSurfaceReadyListenerLock) {
            mSurfaceReadyListener = listener;
            mSurfaceReadyListenerLock.notifyAll();
        }
    }
}
