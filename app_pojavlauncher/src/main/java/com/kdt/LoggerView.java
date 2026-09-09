package com.kdt;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.kdt.pojavlaunch.Logger;
import net.kdt.pojavlaunch.R;


public class LoggerView extends ConstraintLayout {
    private Logger.eventLogListener mLogListener;
    private ToggleButton mLogToggle;
    private DefocusableScrollView mScrollView;
    private TextView mLogTextView;


    public LoggerView(@NonNull Context context) {
        this(context, null);
    }

    public LoggerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        
        mLogToggle.setChecked(visibility == VISIBLE);
    }

    
    private void init(){
        inflate(getContext(), R.layout.view_logger, this);
        mLogTextView = findViewById(R.id.content_log_view);
        mLogTextView.setTypeface(Typeface.MONOSPACE);
        
        mLogTextView.setMaxLines(Integer.MAX_VALUE);
        mLogTextView.setEllipsize(null);
        mLogTextView.setVisibility(GONE);

        
        mLogToggle = findViewById(R.id.content_log_toggle_log);
        mLogToggle.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> {
                    mLogTextView.setVisibility(isChecked ? VISIBLE : GONE);
                    if(isChecked) {
                        Logger.addLogListener(mLogListener);
                    }else{
                        mLogTextView.setText("");
                        Logger.removeLogListener(mLogListener);
                    }
                });
        mLogToggle.setChecked(false);

        
        ImageButton cancelButton = findViewById(R.id.log_view_cancel);
        cancelButton.setOnClickListener(view -> LoggerView.this.setVisibility(GONE));

        
        mScrollView = findViewById(R.id.content_log_scroll);
        mScrollView.setKeepFocusing(true);

        
        ToggleButton autoscrollToggle = findViewById(R.id.content_log_toggle_autoscroll);
        autoscrollToggle.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> {
                    if(isChecked) mScrollView.fullScroll(View.FOCUS_DOWN);
                    mScrollView.setKeepFocusing(isChecked);
                }
        );
        autoscrollToggle.setChecked(true);

        
        mLogListener = text -> {
            if(mLogTextView.getVisibility() != VISIBLE) return;
            post(() -> {
                mLogTextView.append(text + '\n');
                if(mScrollView.isKeepFocusing()) mScrollView.fullScroll(View.FOCUS_DOWN);
            });

        };
    }

}
