package com.byteera.bank.activity.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.utils.AdjustReadLogsPermission;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.easemob.chat.EMChatConfig;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class SelectAddressActivity extends BaseActivity {

    @ViewInject(R.id.id_1) private EditText id1;
    @ViewInject(R.id.id_2) private EditText id2;
    @ViewInject(R.id.id_5) private EditText id5;
    @ViewInject(R.id.id_6) private EditText id6;
    @ViewInject(R.id.id_7) private EditText id7;
    @ViewInject(R.id.id_8) private EditText id8;
    @ViewInject(R.id.btn_confirm) private Button btn_confirm;
    @ViewInject(R.id.btn_setdefault) private Button btn_setdefault;
    private boolean siyou = false;
    private boolean isDebug = false;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(R.layout.select_address_activity);
        ViewUtils.inject(this);

        if(isDebug)
        {
            saveLogcat();
        }

        setLocalDevConfig();
//        setPublicConfig();
        confrim();
    }

    private void saveLogcat()
    {
        AdjustReadLogsPermission.adjustIfNeeded(this);

        if(getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS,
                getPackageName()) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            LogUtil.d("Got ReadLog Permission");

            try
            {
                @SuppressLint("SdCardPath")
                String logFilePath = "/sdcard/hzbank_log.txt";

                LogUtil.d("LogFilePath: %s", logFilePath);

                Runtime.getRuntime().exec("logcat -c");

                String[] commands = new String[]{"logcat", "-v", "time", "-f", logFilePath};

                LogUtil.d("Command: %s", StringUtils.join(commands, " "));

                Runtime.getRuntime().exec(commands);
            }
            catch (Exception ex)
            {
                LogUtil.e(ex, "SaveLogcat Exception");
            }

        }
        else
        {
            LogUtil.d("ReadLog Permission Deny");
        }
    }

    @OnClick({R.id.btn_confirm, R.id.btn_setdefault, R.id.btn_setdefault2})
    public void onClickListener(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                confrim();
                break;
            case R.id.btn_setdefault:
                setHZBankConfig();
                break;
            case R.id.btn_setdefault2:
                setPublicConfig();
                break;
        }
    }

    // 杭州银行
    private void setHZBankConfig() {
        siyou = true;
        //私有配置
        id1.setText("http://60.191.59.19");
        id2.setText("6521");
        id5.setText("60.191.59.19:5296");
        id6.setText("60.191.59.19");
        id7.setText("5222");
        id8.setText("easemob-demo#easemobchat");
    }

    // 公有云
    private void setPublicConfig() {
        siyou = false;
        id1.setText("http://119.254.108.108");
        id2.setText("5000");
        id5.setText("a1.easemob.com");
        id6.setText("im1.easemob.com");
        id7.setText("5222");
        id8.setText("13811964921#hzbank");
    }

    // 本地开发测试
    private void setLocalDevConfig() {
        siyou = false;
        id1.setText("http://dev.zijieshidai.com");
        id2.setText("8881");
        id5.setText("dev.zijieshidai.com:8880");
        id6.setText("dev.zijieshidai.com");
        id7.setText("5222");
        id8.setText("zijieshidai#hzbank");
    }

    /** 确认 **/
    private void confrim() {
        if (TextUtils.isEmpty(id1.getText().toString().trim())
                || TextUtils.isEmpty(id2.getText().toString().trim())
                || TextUtils.isEmpty(id5.getText().toString().trim())
                || TextUtils.isEmpty(id6.getText().toString().trim())
                || TextUtils.isEmpty(id7.getText().toString().trim())
                || TextUtils.isEmpty(id8.getText().toString().trim())
                ) {
            ToastUtil.showToastText("请输入完整！！");
        } else {
            String str1 = id1.getText().toString().trim();
            String str2 = id2.getText().toString().trim();
            String str5 = id5.getText().toString().trim();
            String str6 = id6.getText().toString().trim();
            String str7 = id7.getText().toString().trim();
            String str8 = id8.getText().toString().trim();

            Map<String, String> mapParams = new HashMap<String, String>();

            //私有部署参数
            mapParams.put("EASEMOB_API_URL", str5);//这里写usergrid的ip地址，例如：192.168.30.25:8081
            mapParams.put("EASEMOB_CHAT_ADDRESS", str6);//这里写ejabberd 的地址，例如：192.168.30.25
            mapParams.put("EASEMOB_CHAT_PORT", str7);//这里写聊天用的端口号，默认为5222，可以不写
            mapParams.put("EASEMOB_APPKEY", str8);//这里写appkey
            if (siyou) {
                //注册私有服务器
                LogUtil.d("EASEMOB_CHAT_ADDRESS:%s", str6);
                EMChatConfig.getInstance().registerPrivateServer(mapParams);
            }

            MyApp.getInstance().getHxSdkHelper().onInit(MyApp.getInstance());
            ActivityUtil.startActivity(baseContext, new Intent(this, SplashActivity.class));
            finish();
            //保存服务器地址
            HXPreferenceUtils.getInstance().setServer1(str1);
            HXPreferenceUtils.getInstance().setServer2(str2);
        }

    }
}
