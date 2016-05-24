package com.byteera.bank.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityController {
    private static final String TAG = ActivityController.class.getSimpleName();

    private static List<Activity> activitys = new ArrayList<Activity>();


    public static void add(Activity activity) {
        activitys.add(activity);
    }

    public static void remove(Activity activity) {
        if (activity != null) {
            activitys.remove(activity);
            activity = null;
        }
    }

    public static void exitApp() {
        for (Activity activity : activitys) {
            if (null != activity) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void finishAllActivitys() {
        for (Activity activity : activitys) {
            if (null != activity) {
                activity.finish();
            }
        }
    }
}
