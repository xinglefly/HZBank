package com.byteera.bank.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import com.byteera.R;
import com.byteera.bank.BankHXSDKHelper;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.MainActivity;
import com.byteera.bank.activity.contact.ContactFragment;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.Department;
import com.byteera.bank.utils.EasemobUtil;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends BaseActivity {
    private LinearLayout rootLayout;
    private static final int sleepTime = 2500;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_splash);
        rootLayout = (LinearLayout) findViewById(R.id.splash_root);
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(2000);
        rootLayout.startAnimation(animation);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (BankHXSDKHelper.getInstance().isLogined()) {
                    long start = System.currentTimeMillis();

                    EasemobUtil.getUserGroup();
                    EasemobUtil.updateUserList();
                    EasemobUtil.getDepartmentList();

                    EMGroupManager.getInstance().loadAllGroups();
                    EMChatManager.getInstance().loadAllConversations();
                    long costTime = System.currentTimeMillis() - start;
                    //等待sleeptime时长
                    if (sleepTime - costTime > 0) {
                        try {
                            Thread.sleep(sleepTime - costTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //进入主页面
                    ActivityUtil.startActivity(baseContext, new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                    ActivityUtil.startActivity(baseContext, new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }


}
