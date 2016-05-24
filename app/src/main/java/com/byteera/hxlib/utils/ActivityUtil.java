package com.byteera.hxlib.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import com.byteera.R;
import com.byteera.bank.activity.login.LoginActivity;


public class ActivityUtil {
    public static void startActivity(Activity activity, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_in_right, R.anim.push_out_left);
    }

    public static void finishActivity(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.push_in_left, R.anim.push_out_right);
    }

    public static void startActivityForResult(Activity activity, Intent intent, int flag) {
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivityForResult(intent, flag);
        activity.overridePendingTransition(R.anim.push_in_right, R.anim.push_out_left);
    }

    public static void startActivityForResult(Activity activity, Fragment fragment, Intent intent, int flag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        fragment.startActivityForResult(intent, flag);
        activity.overridePendingTransition(R.anim.push_in_right, R.anim.push_out_left);
    }

    public static void startLoginActivityForResult(Activity activity, int flag) {
        Intent intent = new Intent(activity, LoginActivity.class);
        startActivityForResult(activity, intent, flag);
    }

    public static void showLoginDialog(final Activity mContext, final int requestFlag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("您还没有登录，是否登录？").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                startLoginActivityForResult(mContext, requestFlag);
            }
        }).create().show();
    }
}
