package net.kdt.pojavlaunch.customcontrols.buttons;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.PREF_BUTTONSIZE;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import net.kdt.pojavlaunch.GrabListener;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlSideDialog;

import org.lwjgl.glfw.CallbackBridge;


public interface ControlInterface extends View.OnLongClickListener, GrabListener {
    View getControlView();

    ControlData getProperties();

    default void setProperties(ControlData properties) {
        setProperties(properties, true);
    }

    
    void removeButton();

    
    void cloneButton();

    default void setVisible(boolean isVisible) {
        if(getProperties().isHideable)
            getControlView().setVisibility(isVisible ? VISIBLE : GONE);
    }

    void sendKeyPresses(boolean isDown);

    
    void loadEditValues(EditControlSideDialog editControlDialog);

    @Override
    default void onGrabState(boolean isGrabbing) {
        if (getControlLayoutParent() != null && getControlLayoutParent().getModifiable()) return; 
        setVisible(((getProperties().displayInGame && isGrabbing) || (getProperties().displayInMenu && !isGrabbing)) && getControlLayoutParent().areControlVisible());
    }

    default ControlLayout getControlLayoutParent() {
        return (ControlLayout) getControlView().getParent();
    }

    
    default ControlData preProcessProperties(ControlData properties, ControlLayout layout) {
        
        properties.setWidth(properties.getWidth() / layout.getLayoutScale() * PREF_BUTTONSIZE);
        properties.setHeight(properties.getHeight() / layout.getLayoutScale() * PREF_BUTTONSIZE);

        
        properties.isHideable = !properties.containsKeycode(ControlData.SPECIALBTN_TOGGLECTRL) && !properties.containsKeycode(ControlData.SPECIALBTN_VIRTUALMOUSE);

        return properties;
    }

    default void updateProperties() {
        setProperties(getProperties());
    }

    
    @CallSuper
    default void setProperties(ControlData properties, boolean changePos) {
        if (changePos) {
            getControlView().setX(properties.insertDynamicPos(getProperties().dynamicX));
            getControlView().setY(properties.insertDynamicPos(getProperties().dynamicY));
        }

        
        ViewGroup.LayoutParams params = getControlView().getLayoutParams();
        if (params == null)
            params = new FrameLayout.LayoutParams((int) properties.getWidth(), (int) properties.getHeight());
        params.width = (int) properties.getWidth();
        params.height = (int) properties.getHeight();
        getControlView().setLayoutParams(params);
    }

    
    default void setBackground() {
        GradientDrawable gd = getControlView().getBackground() instanceof GradientDrawable
                ? (GradientDrawable) getControlView().getBackground()
                : new GradientDrawable();
        gd.setColor(getProperties().bgColor);
        gd.setStroke((int) Tools.dpToPx(getProperties().strokeWidth * (getControlLayoutParent().getLayoutScale()/100f)), getProperties().strokeColor);
        gd.setCornerRadius(computeCornerRadius(getProperties().cornerRadius));

        getControlView().setBackground(gd);
    }

    
    default void setDynamicX(String dynamicX) {
        getProperties().dynamicX = dynamicX;
        getControlView().setX(getProperties().insertDynamicPos(dynamicX));
    }

    
    default void setDynamicY(String dynamicY) {
        getProperties().dynamicY = dynamicY;
        getControlView().setY(getProperties().insertDynamicPos(dynamicY));
    }

    
    default String generateDynamicX(float x) {
        if (x + (getProperties().getWidth() / 2f) > CallbackBridge.physicalWidth / 2f) {
            return (x + getProperties().getWidth()) / CallbackBridge.physicalWidth + " * ${screen_width} - ${width}";
        } else {
            return x / CallbackBridge.physicalWidth + " * ${screen_width}";
        }
    }

    
    default String generateDynamicY(float y) {
        if (y + (getProperties().getHeight() / 2f) > CallbackBridge.physicalHeight / 2f) {
            return (y + getProperties().getHeight()) / CallbackBridge.physicalHeight + " * ${screen_height} - ${height}";
        } else {
            return y / CallbackBridge.physicalHeight + " * ${screen_height}";
        }
    }

    
    default void regenerateDynamicCoordinates() {
        getProperties().dynamicX = generateDynamicX(getControlView().getX());
        getProperties().dynamicY = generateDynamicY(getControlView().getY());
        updateProperties();
    }

    
    default String applySize(String equation, ControlInterface button) {
        return equation
                .replace("${right}", "(${screen_width} - ${width})")
                .replace("${bottom}", "(${screen_height} - ${height})")
                .replace("${height}", "(px(" + Tools.pxToDp(button.getProperties().getHeight()) + ") /" + PREF_BUTTONSIZE + " * ${preferred_scale})")
                .replace("${width}", "(px(" + Tools.pxToDp(button.getProperties().getWidth()) + ") / " + PREF_BUTTONSIZE + " * ${preferred_scale})");
    }


    
    default float computeCornerRadius(float radiusInPercent) {
        float minSize = Math.min(getProperties().getWidth(), getProperties().getHeight());
        return (minSize / 2) * (radiusInPercent / 100);
    }

    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean canSnap(ControlInterface button) {
        float MIN_DISTANCE = getSnapDistance();

        if (button == this) return false;
        return !(net.kdt.pojavlaunch.utils.MathUtils.dist(
                button.getControlView().getX() + button.getControlView().getWidth() / 2f,
                button.getControlView().getY() + button.getControlView().getHeight() / 2f,
                getControlView().getX() + getControlView().getWidth() / 2f,
                getControlView().getY() + getControlView().getHeight() / 2f)
                > Math.max(button.getControlView().getWidth() / 2f + getControlView().getWidth() / 2f,
                button.getControlView().getHeight() / 2f + getControlView().getHeight() / 2f) + MIN_DISTANCE);
    }

    
    default void snapAndAlign(float x, float y) {
        final float MIN_DISTANCE = getSnapDistance();
        String dynamicX = generateDynamicX(x);
        String dynamicY = generateDynamicY(y);

        getControlView().setX(x);
        getControlView().setY(y);

        for (ControlInterface button : ((ControlLayout) getControlView().getParent()).getButtonChildren()) {
            
            if (!canSnap(button)) continue;

            
            float button_top = button.getControlView().getY();
            float button_bottom = button_top + button.getControlView().getHeight();
            float button_left = button.getControlView().getX();
            float button_right = button_left + button.getControlView().getWidth();

            float top = getControlView().getY();
            float bottom = getControlView().getY() + getControlView().getHeight();
            float left = getControlView().getX();
            float right = getControlView().getX() + getControlView().getWidth();

            
            if (Math.abs(top - button_bottom) < MIN_DISTANCE) { 
                dynamicY = applySize(button.getProperties().dynamicY, button) + applySize(" + ${height}", button) + " + ${margin}";
            } else if (Math.abs(button_top - bottom) < MIN_DISTANCE) { 
                dynamicY = applySize(button.getProperties().dynamicY, button) + " - ${height} - ${margin}";
            }
            if (!dynamicY.equals(generateDynamicY(getControlView().getY()))) { 
                if (Math.abs(button_left - left) < MIN_DISTANCE) { 
                    dynamicX = applySize(button.getProperties().dynamicX, button);
                } else if (Math.abs(button_right - right) < MIN_DISTANCE) { 
                    dynamicX = applySize(button.getProperties().dynamicX, button) + applySize(" + ${width}", button) + " - ${width}";
                }
            }

            if (Math.abs(button_left - right) < MIN_DISTANCE) { 
                dynamicX = applySize(button.getProperties().dynamicX, button) + " - ${width} - ${margin}";
            } else if (Math.abs(left - button_right) < MIN_DISTANCE) { 
                dynamicX = applySize(button.getProperties().dynamicX, button) + applySize(" + ${width}", button) + " + ${margin}";
            }
            if (!dynamicX.equals(generateDynamicX(getControlView().getX()))) { 
                if (Math.abs(button_top - top) < MIN_DISTANCE) { 
                    dynamicY = applySize(button.getProperties().dynamicY, button);
                } else if (Math.abs(button_bottom - bottom) < MIN_DISTANCE) { 
                    dynamicY = applySize(button.getProperties().dynamicY, button) + applySize(" + ${height}", button) + " - ${height}";
                }
            }

        }

        setDynamicX(dynamicX);
        setDynamicY(dynamicY);
    }

    
    default void injectBehaviors() {
        injectProperties();
        injectTouchEventBehavior();
        injectLayoutParamBehavior();
        injectGrabListenerBehavior();
    }

    
    default void injectGrabListenerBehavior() {
        if (getControlView() == null) {
            Log.e(ControlInterface.class.toString(), "Failed to inject grab listener behavior !");
            return;
        }


        getControlView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                CallbackBridge.addGrabListener(ControlInterface.this);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                getControlView().removeOnAttachStateChangeListener(this);
                CallbackBridge.removeGrabListener(ControlInterface.this);
            }
        });


    }

    default void injectProperties() {
        getControlView().post(() -> getControlView().setTranslationZ(10));
    }

    
    default void injectTouchEventBehavior() {
        getControlView().setOnTouchListener(new View.OnTouchListener() {
            private boolean mCanTriggerLongClick = true;
            private float downX, downY;
            private float downRawX, downRawY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!getControlLayoutParent().getModifiable()) {
                    
                    view.onTouchEvent(event);
                    return true;
                }

                
                

                if (event.getActionMasked() == MotionEvent.ACTION_UP && mCanTriggerLongClick) {
                    
                    onLongClick(view);
                }

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mCanTriggerLongClick = true;
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        downX = downRawX - view.getX();
                        downY = downRawY - view.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - downRawX) > 8 || Math.abs(event.getRawY() - downRawY) > 8)
                            mCanTriggerLongClick = false;
                        getControlLayoutParent().adaptPanelPosition();
                        snapAndAlign(
                                MathUtils.clamp(event.getRawX() - downX, 0, CallbackBridge.physicalWidth - view.getWidth()),
                                MathUtils.clamp(event.getRawY() - downY, 0, CallbackBridge.physicalHeight - view.getHeight())
                        );
                        break;
                }

                return true;
            }
        });
    }

    default void injectLayoutParamBehavior() {
        getControlView().addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            getProperties().setWidth(right - left);
            getProperties().setHeight(bottom - top);
            setBackground();

            
            getControlView().setX(getControlView().getX());
            getControlView().setY(getControlView().getY());
        });
    }

    @Override
    default boolean onLongClick(View v) {
        if (getControlLayoutParent().getModifiable()) {
            getControlLayoutParent().editControlButton(this);
            getControlLayoutParent().mActionRow.setFollowedButton(this);
        }

        return true;
    }

    static float getSnapDistance() {
        return Tools.dpToPx(6);
    }

    static float getMarginDistance() {
        return Tools.dpToPx(2);
    }
}
