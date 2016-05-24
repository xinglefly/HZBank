package com.byteera.bank.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.byteera.bank.MyApp;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ActivityController;
import com.byteera.bank.utils.CommonUtils;
import com.byteera.bank.utils.LoadingDialogShow;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.NotificationCompat;
import com.easemob.util.EasyUtils;

import java.util.List;

public class BaseActivity extends AppCompatActivity {
    private static final int notifiId = 11;
    protected NotificationManager notificationManager;
    protected LoadingDialogShow dialog = null;
    protected BaseActivity baseContext = BaseActivity.this;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActivityController.add(this);
        dialog = new LoadingDialogShow(baseContext);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示
        EMChatManager.getInstance().activityResumed();
    }

    @Override protected void onStart() {
        super.onStart();
    }

    /**
     * 当应用在前台时，如果当前消息不是属于当前会话，在状态栏提示一下
     * 如果不需要，注释掉即可
     */
    protected void notifyNewMessage(EMMessage message) {
        //如果是设置了不提醒只显示数目的群组(这个是app里保存这个数据的，demo里不做判断)
        //以及设置了setShowNotificationInbackgroup:false(设为false后，后台时sdk也发送广播)
        if (!EasyUtils.isAppRunningForeground(this)) {
            return;
        }

        if(! MyApp.getInstance().getHxSdkHelper().getModel().getSettingMsgNotification())
        {
            return;
        }

        // 获取设置的不提示新消息的用户或者群组ids
        if (message.getChatType() == EMMessage.ChatType.GroupChat) {
            String groupId = message.getTo();
            List<String> disabledGroups = MyApp.getInstance().getHxSdkHelper().getModel().getDisabledGroups();
            if(disabledGroups != null && disabledGroups.contains(groupId))
                return;
        } else {
            String messageFrom = message.getFrom();
            List<String> disabledIds = MyApp.getInstance().getHxSdkHelper().getModel().getDisabledIds();
            if(disabledIds != null && disabledIds.contains(messageFrom))
                return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true);

        String ticker = CommonUtils.getMessageDigest(message, this);
        if (message.getType() == EMMessage.Type.TXT)
            ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
        //设置状态栏提示

        User user = DBManager.getInstance(baseContext).getUserByEasemobId(message.getFrom());

        mBuilder.setTicker(user.getNickName() + ": " + ticker);

        Notification notification = mBuilder.build();
        notificationManager.notify(notifiId, notification);
        notificationManager.cancel(notifiId);
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
        ActivityController.remove(this);
    }


    @Override public void onBackPressed() {
        super.onBackPressed();
        ActivityUtil.finishActivity(this);
    }
}
