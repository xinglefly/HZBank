package com.byteera.bank.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;


public class LoadingDialogShow {
	Context context;
	Dialog dialog;
	TextView tv;
	ImageView ivResult;
	ProgressBar pb;
	public LoadingDialogShow(Context context){
		this.context = context;
		
		dialog = new Dialog(context);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//		dialog.setContentView(R.layout.loading_dialog);
		View view=View.inflate(context, R.layout.loading_dialog, null);
		tv=(TextView) view.findViewById(R.id.dialog_submit_text);
		ivResult=(ImageView) view.findViewById(R.id.dialog_iv);
		pb=(ProgressBar) view.findViewById(R.id.dialog_pb);
		dialog.setContentView(view);
	}
	public void setMessage(String hint){
//		tv.setVisibility(View.VISIBLE);
		tv.setText(hint);
	}

	public void setResultStatusDrawable(final boolean resultStatus, final String hint) {
		MyApp.getMainThreadHandler().post(new Runnable() {
			@Override
			public void run() {
				tv.setText(hint);
				if (resultStatus) {
					ivResult.setBackgroundResource(R.drawable.progerss_ok);
				} else {
					tv.setTextColor(context.getResources().getColor(R.color.orange_st));
					ivResult.setBackgroundResource(R.drawable.progerss_error);
				}
				ivResult.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}
		});
		new Thread(new Runnable() {
			@Override public void run() {
				SystemClock.sleep(1000);
				dialog.dismiss();
			}
		}).start();
	}
	public void show(){
		if(dialog.isShowing()) {
			dialog.dismiss();
		}
		tv.setTextColor(context.getResources().getColor(R.color.babybull));
		pb.setVisibility(View.VISIBLE);
		ivResult.setVisibility(View.GONE);
		dialog.show();
	}
	public void dismiss(){
		dialog.dismiss();
	}
}
