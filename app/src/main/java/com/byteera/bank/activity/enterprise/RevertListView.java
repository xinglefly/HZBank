package com.byteera.bank.activity.enterprise;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class RevertListView extends ListView {

	public RevertListView(Context context) {
		super(context);
	}

	public RevertListView(Context context, AttributeSet att) {
		super(context, att);
	}

	public RevertListView(Context context, AttributeSet att, int defStyle) {
		super(context, att, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
