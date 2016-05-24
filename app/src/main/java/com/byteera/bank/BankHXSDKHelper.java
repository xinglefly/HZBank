/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.byteera.bank;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.Toast;

import com.byteera.R;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.activity.MainActivity;
import com.byteera.bank.activity.voice.VideoCallActivity;
import com.byteera.bank.activity.voice.VoiceCallActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.db.FriendshipDao;
import com.byteera.bank.domain.FriendRelationShip;
import com.byteera.bank.domain.RobotUser;
import com.byteera.bank.domain.User;
import com.byteera.bank.receiver.CallReceiver;
import com.byteera.bank.utils.CommonUtils;
import com.byteera.bank.utils.LogUtil;
import com.byteera.hxlib.controller.HXSDKHelper;
import com.byteera.hxlib.model.HXNotifier;
import com.byteera.hxlib.model.HXNotifier.HXNotificationInfoProvider;
import com.byteera.hxlib.model.HXSDKModel;
import com.byteera.hxlib.utils.Constants;
import com.easemob.EMCallBack;
import com.easemob.EMChatRoomChangeListener;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.util.EasyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Demo UI HX SDK helper class which subclass HXSDKHelper
 * @author easemob
 *
 */
public class BankHXSDKHelper extends HXSDKHelper {

    private static final String TAG = "DemoHXSDKHelper";

    /**
     * EMEventListener
     */
    protected EMEventListener eventListener = null;

    /**
     * contact list in cache
     */
    private Map<String, User> contactList;

    /**
     * robot list in cache
     */
    private Map<String, RobotUser> robotList;
    private CallReceiver callReceiver;

    /**
     * 用来记录foreground Activity
     */
    private List<Activity> activityList = new ArrayList<Activity>();

    public void pushActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(0, activity);
        }
    }

    public void popActivity(Activity activity) {
        activityList.remove(activity);
    }


    @Override
    protected void initHXOptions() {
        super.initHXOptions();

        EMChatOptions options = EMChatManager.getInstance().getChatOptions();
        options.allowChatroomOwnerLeave(getModel().isChatroomOwnerLeaveAllowed());
    }

    @Override
    protected void initListener() {
        super.initListener();
        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }

        //注册通话广播接收者
        appContext.registerReceiver(callReceiver, callFilter);
        //注册消息事件监听
        initEventListener();
    }

    /**
     * 全局事件监听
     * 因为可能会有UI页面先处理到这个消息，所以一般如果UI页面已经处理，这里就不需要再次处理
     * activityList.size() <= 0 意味着所有页面都已经在后台运行，或者已经离开Activity Stack
     */
    protected void initEventListener() {
        eventListener = new EMEventListener() {
//            private BroadcastReceiver FriendRequestReceiver = null;

            @Override
            public void onEvent(EMNotifierEvent event) {
                EMMessage message = null;
                if (event.getData() instanceof EMMessage) {
                    message = (EMMessage) event.getData();
                    LogUtil.d("receive the event : " + event.getEvent() + ",id : " + message.getMsgId());
                }

                switch (event.getEvent()) {
                    case EventNewMessage:
                        //应用在后台，不需要刷新UI,通知栏提示新消息
                        if (activityList.size() <= 0) {
                            HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                        }
                        break;
                    case EventOfflineMessage:
                        if (activityList.size() <= 0) {
                            LogUtil.d("received offline messages");
                            List<EMMessage> messages = (List<EMMessage>) event.getData();
                            HXSDKHelper.getInstance().getNotifier().onNewMesg(messages);
                        }
                        break;
                    // below is just giving a example to show a cmd toast, the app should not follow this
                    // so be careful of this
                    case EventNewCMDMessage: {

                        LogUtil.i("收到透传消息");
                        //获取消息body
                        CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
                        final String action = cmdMsgBody.action;//获取自定义action

                        //获取扩展属性 此处省略
                        LogUtil.i(String.format("透传消息：action:%s", action));
                        LogUtil.Object(message);
                        final String str = appContext.getString(R.string.receive_the_passthrough);

                        //公告
                        String content = message.getStringAttribute("content", "");
                        final String title = message.getStringAttribute("title", "");
                        final long time = message.getIntAttribute("time", 0);

                        //加好友请求+ 删除好友关系
                        final String srcuser = message.getStringAttribute("srcuser", "");

                        //请求回执
                        final String dstuser = message.getStringAttribute("dstuser", "");
                        final int result = message.getIntAttribute("result", 0);


                        Intent broadcastIntent = null;
                        switch (action) {
                            case "bulletin"://公告
                                broadcastIntent = new Intent(Constants.CMD_BROADCAST);
                                broadcastIntent.putExtra("action", "bulletin");
                                broadcastIntent.putExtra("cmd_value", str + action);
                                broadcastIntent.putExtra("content", content);
                                broadcastIntent.putExtra("title", title);
                                broadcastIntent.putExtra("time", time);
                                DBManager.getInstance(appContext).addAnnouncement(title, content, time);
                                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                                break;
                            case "addfriendrequest":
                                broadcastIntent = new Intent(Constants.CMD_BROADCAST_ADDFRIEND);
                                broadcastIntent.putExtra("srcuser", srcuser);
                                broadcastIntent.putExtra("mode", FriendshipDao.MODE_ADD);

                                FriendRelationShip friendRelationShip =
                                        new FriendRelationShip(srcuser, FriendshipDao.MODE_ADD,
                                                FriendshipDao.RESULT_REQUEST);
                                MyApp.getInstance().addFriendRequests(friendRelationShip);

                                FriendRelationShip friendship = DBManager.getInstance(appContext).getFriendShipBySrcUser(srcuser);
                                if(friendship != null)
                                {
                                    DBManager.getInstance(appContext).UpdateFriendship(srcuser, FriendshipDao.RESULT_REQUEST);
                                }
                                else
                                {
                                    DBManager.getInstance(appContext).AddFriendship(srcuser, FriendshipDao.MODE_ADD, FriendshipDao.RESULT_REQUEST);
                                }
                                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                                break;
                            case "addfriendresult"://action 为 "addfriendresult" 扩展字段为 {dstuser:  "afewagf32233wegqwg", result: 0}
                                broadcastIntent = new Intent(Constants.CMD_BROADCAST_ADDFRIEND);
                                broadcastIntent.putExtra("srcuser", dstuser);
                                broadcastIntent.putExtra("result", result);
                                HXSDKHelper.getInstance().getNotifier().onNewMsg(message);
                                break;
                            case "deletefriend"://action 为 "deletefriend" 扩展字段为 { srcuser:  'w7202323u308f2u0723032t'}
                                broadcastIntent = new Intent(Constants.CMD_BROADCAST_ADDFRIEND);
                                broadcastIntent.putExtra("srcuser", srcuser);
                                broadcastIntent.putExtra("mode", FriendshipDao.MODE_DEL);
                                break;
                        }

                        appContext.sendBroadcast(broadcastIntent, null);
                        break;
                    }
                    case EventDeliveryAck:
                        message.setDelivered(true);
                        break;
                    case EventReadAck:
                        message.setAcked(true);
                        break;
                    // add other events in case you are interested in
                    default:
                        break;
                }

            }
        };

        EMChatManager.getInstance().registerEventListener(eventListener);

        EMChatManager.getInstance().addChatRoomChangeListener(new EMChatRoomChangeListener() {
            private final static String ROOM_CHANGE_BROADCAST = "easemob.demo.chatroom.changeevent.toast";
            private final IntentFilter filter = new IntentFilter(ROOM_CHANGE_BROADCAST);
            private boolean registered = false;

            private void showToast(String value) {
                if (!registered) {
                    //注册广播接收者
                    appContext.registerReceiver(new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Toast.makeText(appContext, intent.getStringExtra("value"), Toast.LENGTH_SHORT).show();
                        }

                    }, filter);

                    registered = true;
                }

                Intent broadcastIntent = new Intent(ROOM_CHANGE_BROADCAST);
                broadcastIntent.putExtra("value", value);
                appContext.sendBroadcast(broadcastIntent, null);
            }

            @Override
            public void onChatRoomDestroyed(String roomId, String roomName) {
                showToast(" room : " + roomId + " with room name : " + roomName + " was destroyed");
                LogUtil.i("info", "onChatRoomDestroyed=" + roomName);
            }

            @Override
            public void onMemberJoined(String roomId, String participant) {
                showToast("member : " + participant + " join the room : " + roomId);
                LogUtil.i("info", "onmemberjoined=" + participant);

            }

            @Override
            public void onMemberExited(String roomId, String roomName,
                                       String participant) {
                showToast("member : " + participant + " leave the room : " + roomId + " room name : " + roomName);
                LogUtil.i("info", "onMemberExited=" + participant);

            }

            @Override
            public void onMemberKicked(String roomId, String roomName,
                                       String participant) {
                showToast("member : " + participant + " was kicked from the room : " + roomId + " room name : " + roomName);
                LogUtil.i("info", "onMemberKicked=" + participant);

            }

        });
    }

    /**
     * 自定义通知栏提示内容
     * @return
     */
    @Override
    protected HXNotificationInfoProvider getNotificationListener() {
        //可以覆盖默认的设置
        return new HXNotificationInfoProvider() {

            @Override
            public String getTitle(EMMessage message) {
                //修改标题,这里使用默认
                return null;
            }

            @Override
            public int getSmallIcon(EMMessage message) {
                //设置小图标，这里为默认
                return 0;
            }

            @Override
            public String getDisplayedText(EMMessage message) {
                // 设置状态栏的消息提示，可以根据message的类型做相应提示
                String ticker = CommonUtils.getMessageDigest(message, appContext);
                if (message.getType() == Type.TXT) {
                    ticker = ticker.replaceAll("\\[.{2,3}\\]", "[表情]");
                }
                Map<String, RobotUser> robotMap = ((BankHXSDKHelper) HXSDKHelper.getInstance()).getRobotList();
                if (robotMap != null && robotMap.containsKey(message.getFrom())) {
                    String nick = robotMap.get(message.getFrom()).getNick();
                    if (!TextUtils.isEmpty(nick)) {
                        return nick + ": " + ticker;
                    } else {
                        return message.getFrom() + ": " + ticker;
                    }
                } else {
                    User user = DBManager.getInstance(appContext).getUserByEasemobId(message.getFrom());
                    return user.getNickName() + ": " + ticker;
                }
            }

            @Override
            public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                return null;
                // return fromUsersNum + "个基友，发来了" + messageNum + "条消息";
            }

            @Override
            public Intent getLaunchIntent(EMMessage message) {
                //设置点击通知栏跳转事件
                Intent intent = new Intent(appContext, ChatActivity.class);
                //有电话时优先跳转到通话页面
                if (isVideoCalling) {
                    intent = new Intent(appContext, VideoCallActivity.class);
                } else if (isVoiceCalling) {
                    intent = new Intent(appContext, VoiceCallActivity.class);
                } else {
                    if(message.getType() == Type.CMD)
                    {
                        intent = new Intent(appContext, MainActivity.class);
                    }
                    else
                    {
                        ChatType chatType = message.getChatType();
                        if (chatType == ChatType.Chat) { // 单聊信息
                            intent.putExtra("userId", message.getFrom());
                            intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
                        } else if(chatType == ChatType.GroupChat) { // 群聊信息
                            // message.getTo()为群聊id
                            intent.putExtra("groupId", message.getTo());
                            if (chatType == ChatType.GroupChat) {
                                intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                            }
                        }
                    }
                }
                return intent;
            }
        };
    }


    @Override
    protected void onConnectionConflict() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("conflict", true);
        appContext.startActivity(intent);
    }

    @Override
    protected void onCurrentAccountRemoved() {
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.ACCOUNT_REMOVED, true);
        appContext.startActivity(intent);
    }


    @Override
    protected HXSDKModel createModel() {
        return new BankHXSDKModel(appContext);
    }

    @Override
    public HXNotifier createNotifier() {
        return new HXNotifier() {
            public synchronized void onNewMsg(final EMMessage message) {
                if (EMChatManager.getInstance().isSlientMessage(message)) {
                    return;
                }

                // 获取设置的不提示新消息的用户或者群组ids
                if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                    String groupId = message.getTo();
                    List<String> disabledGroups = MyApp.getInstance().getHxSdkHelper().getModel().getDisabledGroups();
                    if (disabledGroups != null && disabledGroups.contains(groupId))
                        return;
                } else {
                    String messageFrom = message.getFrom();
                    List<String> disabledIds = MyApp.getInstance().getHxSdkHelper().getModel().getDisabledIds();
                    if (disabledIds != null && disabledIds.contains(messageFrom))
                        return;
                }

                if (!EasyUtils.isAppRunningForeground(appContext)) {
                    int badgerCount = MyApp.getInstance().increaseBadgerCount();
                    LogUtil.d("app is running in backgroud, badger count: %d", badgerCount);
                    ShortcutBadger.with(appContext).count(badgerCount);
                    sendNotification(message, false);
                } else {
                    sendNotification(message, true);

                }

                viberateAndPlayTone(message);

            }
        };
    }

    /**
     * get demo HX SDK Model
     */
    public BankHXSDKModel getModel() {
        return (BankHXSDKModel) hxModel;
    }

    /**
     * 获取内存中好友user list
     *
     * @return
     */
    public Map<String, User> getContactList() {
        if (getHXId() != null && contactList == null) {
            contactList = ((BankHXSDKModel) getModel()).getContactList();
        }

        return contactList;
    }

    public Map<String, RobotUser> getRobotList() {
        if (getHXId() != null && robotList == null) {
            robotList = ((BankHXSDKModel) getModel()).getRobotList();
        }
        return robotList;
    }


    /**
     * 设置好友user list到内存中
     *
     * @param contactList
     */
    public void setContactList(Map<String, User> contactList) {
        this.contactList = contactList;
    }


    @Override
    public void logout(final EMCallBack callback) {
        endCall();
        EMChatManager.getInstance().logout();
        super.logout(new EMCallBack() {
            @Override
            public void onSuccess() {
                setContactList(null);
                getModel().closeDB();
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

        });
    }

    void endCall(){
        try {
            EMChatManager.getInstance().endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
