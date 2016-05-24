package com.byteera.bank.utils;

import android.content.Context;
import android.widget.Toast;

import com.byteera.bank.MyApp;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;

public class ToastUtil {
	private static Toast toast = null;
	private static String oldmsg;
	private static long oneTime = 0;
	private static Context context = MyApp.getInstance();

	public static void showToastText(String text) {
		showToastText(text, Toast.LENGTH_SHORT);
	}

	public static void showLongToastText(String text){
		showToastText(text, Toast.LENGTH_LONG);
	}
	public static void showToastText(int resId) {
		showToastText(context.getString(resId), Toast.LENGTH_SHORT);
	}

	private static void showToastText(final String text, final int duration) {
		if (UIUtils.isRunInMainThread()) {
			showToast(text,duration);
		} else {
			post(new Runnable() {
				@Override
				public void run() {
					showToast(text,duration);
				}
			});
		}
	}
	public static boolean post(Runnable runnable) {
		return UIUtils.getHandler().post(runnable);
	}


	private static void showToast(String text, int duration) {
		if (toast == null) {
			toast = Toast.makeText(context, text, duration);
			toast.show();
			oneTime = System.currentTimeMillis();
		} else {
			long twoTime = System.currentTimeMillis();
			if (text.equals(oldmsg)) {
				if (twoTime - oneTime > duration) {
					toast.show();
				}
			} else {
				oldmsg = text;
				toast.setText(text);
				toast.setDuration(duration);
				toast.show();
			}
		}
	}
}
