package com.byteera.bank.widget;

import android.content.Context;
import android.util.AttributeSet;
import com.byteera.bank.utils.LogUtil;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/*
 * auto scroll to rightmost on new view addition
 */
public class AutoHorizontalScrollView extends HorizontalScrollView {
	
	public AutoHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		fullScroll(HorizontalScrollView.FOCUS_RIGHT);
	}
	
	
}