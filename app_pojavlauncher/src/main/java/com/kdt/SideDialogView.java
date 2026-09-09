package com.kdt;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;


public abstract class SideDialogView {

    private final ViewGroup mParent;
    private final @LayoutRes int mLayoutId;
    private ViewGroup mDialogLayout;
    private DefocusableScrollView mScrollView;
    protected View mDialogContent;

    protected final int mMargin;
    private ObjectAnimator mSideDialogAnimator;
    protected boolean mDisplaying = false;
    
    private boolean mIsInstantiated = false;

    
    private Button mStartButton, mEndButton;
    private TextView mTitleTextview;
    private View mTitleDivider;

    
    private @StringRes int mStartButtonStringId, mEndButtonStringId, mTitleStringId;
    private View.OnClickListener mStartButtonListener, mEndButtonListener;


    public SideDialogView(Context context, ViewGroup parent, @LayoutRes int layoutId) {
        mMargin = context.getResources().getDimensionPixelOffset(R.dimen._20sdp);
        mParent = parent;
        mLayoutId = layoutId;
    }

    public void setTitle(@StringRes int textId) {
        mTitleStringId = textId;
        if (mIsInstantiated) {
            mTitleTextview.setText(textId);
            mTitleTextview.setVisibility(View.VISIBLE);
            mTitleDivider.setVisibility(View.VISIBLE);
        }
    }

    public final void setStartButtonListener(@StringRes int textId, @Nullable View.OnClickListener listener) {
        mStartButtonStringId = textId;
        mStartButtonListener = listener;
        if (mIsInstantiated) setButton(mStartButton, textId, listener);
    }

    public final void setEndButtonListener(@StringRes int textId, @Nullable View.OnClickListener listener) {
        mEndButtonStringId = textId;
        mEndButtonListener = listener;
        if (mIsInstantiated) setButton(mEndButton, textId, listener);
    }

    private void setButton(@NonNull Button button, @StringRes int textId, @Nullable View.OnClickListener listener) {
        button.setText(textId);
        button.setOnClickListener(listener);
        button.setVisibility(View.VISIBLE);
    }


    private void inflateLayout() {
        if(mIsInstantiated) {
            Log.w("SideDialogView", "Layout already inflated");
            return;
        }

        
        mDialogLayout = (ViewGroup) LayoutInflater.from(mParent.getContext()).inflate(R.layout.dialog_side_dialog, mParent, false);
        mScrollView = mDialogLayout.findViewById(R.id.side_dialog_scrollview);
        mStartButton = mDialogLayout.findViewById(R.id.side_dialog_start_button);
        mEndButton = mDialogLayout.findViewById(R.id.side_dialog_end_button);
        mTitleTextview = mDialogLayout.findViewById(R.id.side_dialog_title_textview);
        mTitleDivider = mDialogLayout.findViewById(R.id.side_dialog_title_divider);

        LayoutInflater.from(mParent.getContext()).inflate(mLayoutId, mScrollView, true);
        mDialogContent = mScrollView.getChildAt(0);

        
        mParent.addView(mDialogLayout);

        mSideDialogAnimator = ObjectAnimator.ofFloat(mDialogLayout, "x", 0).setDuration(600);
        mSideDialogAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mDialogLayout.setElevation(10);
        mDialogLayout.setTranslationZ(10);

        mDialogLayout.setVisibility(View.VISIBLE);
        mDialogLayout.setBackground(ResourcesCompat.getDrawable(mDialogLayout.getResources(), R.drawable.background_control_editor, null));

        
        mDialogLayout.setX(-mDialogLayout.getResources().getDimensionPixelOffset(R.dimen._280sdp));
        mIsInstantiated = true;

        
        if (mTitleStringId != 0) setTitle(mTitleStringId);
        if (mStartButtonStringId != 0) setStartButtonListener(mStartButtonStringId, mStartButtonListener);
        if (mEndButtonStringId != 0) setEndButtonListener(mEndButtonStringId, mEndButtonListener);
    }

    
    private void deflateLayout() {
        if(!mIsInstantiated) {
            Log.w("SideDialogView", "Layout not inflated");
            return;
        }

        mSideDialogAnimator.removeAllUpdateListeners();
        mSideDialogAnimator.removeAllListeners();

        mParent.removeView(mDialogLayout);
        mIsInstantiated = false;

        mScrollView = null;
        mSideDialogAnimator = null;
        mDialogLayout = null;
        mDialogContent = null;
        mTitleTextview = null;
        mTitleDivider = null;
        mStartButton = null;
        mEndButton = null;
    }


    
    @CallSuper
    public final void appear(boolean fromRight) {
        if (!mIsInstantiated) {
            inflateLayout();
            onInflate();
        }

        
        onAppear();
        Tools.runOnUiThread(() -> {
            if (fromRight) {
                if (!mDisplaying || !isAtRight()) {
                    mSideDialogAnimator.setFloatValues(currentDisplayMetrics.widthPixels, currentDisplayMetrics.widthPixels - mScrollView.getWidth() - mMargin);
                    mSideDialogAnimator.start();
                    mDisplaying = true;
                }
            } else {
                if (!mDisplaying || isAtRight()) {
                    mSideDialogAnimator.setFloatValues(-mDialogLayout.getWidth(), mMargin);
                    mSideDialogAnimator.start();
                    mDisplaying = true;
                }
            }
        });
    }

    protected final boolean isAtRight() {
        return mDialogLayout.getX() > currentDisplayMetrics.widthPixels / 2f;
    }

    
    @CallSuper
    public final void disappear(boolean destroy) {
        if(!mIsInstantiated) {
            Log.w("SideDialogView", "Layout not inflated");
            return;
        }

        if (!mDisplaying) {
            if(destroy) {
                onDisappear();
                onDestroy();
                deflateLayout();
            }
            return;
        }

        mDisplaying = false;
        if (isAtRight())
            mSideDialogAnimator.setFloatValues(currentDisplayMetrics.widthPixels - mDialogLayout.getWidth() - mMargin, currentDisplayMetrics.widthPixels);
        else
            mSideDialogAnimator.setFloatValues(mMargin, -mDialogLayout.getWidth());

        if(destroy) {
            onDisappear();
            onDestroy();
            mSideDialogAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    deflateLayout();
                }
            });
        }

        mSideDialogAnimator.start();
    }

    
    public final boolean isDisplaying(){
        return mDisplaying;
    }

    
    protected void onInflate() {}

    
    protected void onAppear() {}

    
    protected void onDisappear() {}

    
    protected void onDestroy() {}


}
