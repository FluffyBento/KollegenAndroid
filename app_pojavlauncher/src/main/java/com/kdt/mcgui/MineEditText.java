package com.kdt.mcgui;

import android.content.*;
import android.util.*;
import android.graphics.*;
import android.widget.EditText;

public class MineEditText extends androidx.appcompat.widget.AppCompatEditText {
	public MineEditText(Context ctx) {
		super(ctx);
		init();
	}

	public MineEditText(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init();
	}

	public void init() {
		setBackgroundColor(Color.parseColor("#131313"));
		setTextColor(net.kdt.pojavlaunch.KollegenTheme.color(net.kdt.pojavlaunch.KollegenTheme.TEXT));
		setHintTextColor(net.kdt.pojavlaunch.KollegenTheme.color(net.kdt.pojavlaunch.KollegenTheme.MUTED));
		setPadding(5, 5, 5, 5);
	}
}
