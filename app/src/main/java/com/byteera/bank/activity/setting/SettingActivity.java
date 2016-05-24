package com.byteera.bank.activity.setting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.byteera.R;
import com.byteera.bank.BankHXSDKModel;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.login.LoginActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.utils.ActivityController;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.bank.widget.SwitchView;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class SettingActivity extends BaseActivity implements OnClickListener {
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.rl_clear_liaotian) private RelativeLayout rlClearLiaotian;
    @ViewInject(R.id.rl_clear_cache) private RelativeLayout rlClearCache;

    private SwitchView iv_switch_notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        ViewUtils.inject(this);

        RelativeLayout change_password = (RelativeLayout) findViewById(R.id.change_password);

        iv_switch_notification = (SwitchView) findViewById(R.id.iv_switch_notification);

        rlClearLiaotian.setOnClickListener(this);
        rlClearCache.setOnClickListener(this);
        change_password.setOnClickListener(this);

        findViewById(R.id.btn_logout).setOnClickListener(this);
        final BankHXSDKModel bankHXSDKModel = MyApp.getInstance().getHxSdkHelper().getModel();
        if (bankHXSDKModel.getSettingMsgNotification()) {
            iv_switch_notification.setState(false);
        } else {
            iv_switch_notification.setState(true);
        }

        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });

        iv_switch_notification.setOnSwitchStateChangedListener(new SwitchView.OnSwitchStateChangedListener() {
            @Override
            public void onStateChanged(int state) {
                if (state == SwitchView.STATE_SWITCH_ON) {    //打开
                    bankHXSDKModel.setSettingMsgNotification(false);
                } else {  //关闭
                    bankHXSDKModel.setSettingMsgNotification(true);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password:
                ActivityUtil.startActivity(baseContext, new Intent(baseContext, UpdatePwdActivity.class));
                break;
            case R.id.btn_logout: //退出登陆
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(baseContext).setMessage("您确定要退出登录吗?").
                        setTitle("退出登录").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                }).setNegativeButton("取消", null);
                builder.create().show();
                break;
            case R.id.rl_clear_liaotian: //清空聊天记录
                android.app.AlertDialog.Builder builder2 = new AlertDialog.Builder(baseContext).setMessage("是否确认清空?").
                        setTitle("清空聊天记录").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        //更新新版本
                        EMChatManager.getInstance().deleteAllConversation();
                        ToastUtil.showToastText("清空成功");
                    }
                }).setNegativeButton("取消", null);
                builder2.create().show();
                break;
            case R.id.rl_clear_cache:
                android.app.AlertDialog.Builder builder3 = new AlertDialog.Builder(baseContext).setMessage("是否确认清空?").
                        setTitle("清空缓存").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        //更新新版本
                        ToastUtil.showToastText("清空成功");
                    }
                }).setNegativeButton("取消", null);
                builder3.create().show();
                break;

        }

    }

    public void logout() {
        final ProgressDialog pd = new ProgressDialog(baseContext);
        String st = getResources().getString(R.string.Are_logged_out);
        pd.setMessage(st);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        MyApp.getInstance().logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        // 重新显示登陆页面
                        ActivityController.finishAllActivitys();

                        HXPreferenceUtils.getInstance().setUserInfo(null);

                        HXPreferenceUtils.getInstance().setPassword(null);
                        HXPreferenceUtils.getInstance().setUsername(null);

                        ActivityUtil.startActivity(baseContext, new Intent(baseContext, LoginActivity.class));
                        try {
                            DBManager.getInstance(baseContext).deleteAllFriendShip();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override public void onProgress(int progress, String status) {
            }

            @Override public void onError(int code, String message) {
            }
        });
    }
}
