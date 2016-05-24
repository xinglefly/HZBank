package com.byteera.bank.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.byteera.bank.utils.ActivityController;
import com.byteera.hxlib.utils.ActivityUtil;

public class BaseFragmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 竖屏显示
        ActivityController.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.remove(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActivityUtil.finishActivity(this);
    }
}
