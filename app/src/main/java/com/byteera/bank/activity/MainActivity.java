package com.byteera.bank.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byteera.R;
import com.byteera.bank.BankHXSDKHelper;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.chatgroup.GroupsActivity;
import com.byteera.bank.activity.contact.ContactFragment;
import com.byteera.bank.activity.conversation.ConversationFragment;
import com.byteera.bank.activity.enterprise.MeetingListFragment;
import com.byteera.bank.activity.login.LoginActivity;
import com.byteera.bank.activity.setting.MineFragment;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.db.InviteMessgeDao;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.FriendRelationShip;
import com.byteera.bank.domain.InviteMessage;
import com.byteera.bank.domain.InviteMessage.InviteMesageStatus;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.CommonUtils;
import com.byteera.bank.utils.DownloadManager;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.controller.HXSDKHelper;
import com.byteera.hxlib.utils.Constants;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMNotifier;
import com.easemob.chat.GroupChangeListener;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.easemob.util.NetUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends BaseActivity implements EMEventListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    // 未读消息textview
    private ImageView unreadLabel;
    // 未读通讯录textview
    private ImageView unreadAddressLable;
    private RelativeLayout[] mTabs;
    private ConversationFragment chatHistoryFragment;
    private Fragment[] fragments;
    private int index;
    // 当前fragment的index
    private int currentTabIndex;
    // 账号在别处登录
    public boolean isConflict = false;
    private FriendRequestReceiver friendRequestReceiver;
    private NetworkStateReceiver networkStateReceiver;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!CommonUtils.isNetWorkConnected(this))
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("错误")
                    .setMessage("请检查您的网络链接！")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    }).create().show();
            return;
        }
        EMLog.debugMode = true;
        EMLog.setLogMode(EMLog.ELogMode.KLogConsoleFile);

        checkNewVersion();

        initView();

        if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow) {
            showConflictDialog();
        }

        inviteMessgeDao = new InviteMessgeDao(this);
        userDao = new UserDao(this);

        // setContactListener监听联系人的变化等
        EMContactManager.getInstance().setContactListener(new MyContactListener());
        // 注册一个监听连接状态的listener
        EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
        // 注册群聊相关的listener
        EMGroupManager.getInstance().addGroupChangeListener(new MyGroupChangeListener());
        // 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
        EMChat.getInstance().setAppInited();

        chatHistoryFragment = new ConversationFragment();     // 会话
        ContactFragment contactFragment = new ContactFragment();    //通讯录
        MeetingListFragment enterpriseFragment = new MeetingListFragment(); //会务圈
        MineFragment mineFragment = new MineFragment(); //设置
        fragments = new Fragment[]{chatHistoryFragment, contactFragment, enterpriseFragment,
                mineFragment};

        getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, chatHistoryFragment)
                    .add(R.id.fragment_container, contactFragment)
                    .hide(contactFragment)
                    .show(chatHistoryFragment)
                    .commit();

        if(savedInstanceState != null)
            LogUtil.e("[MainActivity]savedInstanceState is not null");

        friendRequestReceiver = new FriendRequestReceiver();
        IntentFilter cmdFilter = new IntentFilter(Constants.CMD_BROADCAST_ADDFRIEND);
        registerReceiver(friendRequestReceiver, cmdFilter);

        networkStateReceiver = new NetworkStateReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, networkFilter);
    }



    public class FriendRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkExistNewRequest();
        }
    }

    public class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if(info == null || !info.isAvailable()) {
                    LogUtil.d("当前网络已断开");
                    ToastUtil.showToastText("网络链接已断开，请重新登录！");
                    MyApp.getInstance().logout(null);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ShortcutBadger.with(getApplicationContext()).remove();
        MyApp.getInstance().clearBadgerCount();
        checkExistNewRequest();
    }

    private void checkExistNewRequest() {
        List<FriendRelationShip> friendRequests = MyApp.getInstance().getFriendRequests();

        if (friendRequests.size() > 0) {
            unreadAddressLable.setVisibility(View.VISIBLE);
        }
    }

    private void checkNewVersion() {
        RequestParams params = new RequestParams();

        String version_code = getVersion();

        params.addQueryStringParameter("appversion", String.valueOf(version_code));
        params.addQueryStringParameter("type", "android");
        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.GET, Constants.BYTEERA_SERVICE + "checknewversion?", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.d("err-->" + msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        JSONObject data = json.getJSONObject("data");
                        String new_version = data.optString("new_version"); //获得到版本号
                        String download_path = data.optString("download_path");//获取到新地址的下载路径
                        downLoadNewVersion(new_version, download_path);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void downLoadNewVersion(String new_version, final String download_path) {
        String version = getVersion();
        LogUtil.d("version: %s, new_sersion: %s, download_path: %s", version, new_version, download_path);

        double i_new_version, i_cur_version;

        try
        {
            i_new_version = Double.parseDouble(new_version);
        }
        catch (Exception ex)
        {
            i_new_version = 1.0;
        }

        i_cur_version = Double.parseDouble(version);

        if (i_new_version > i_cur_version) {
            //有新的版本
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(baseContext).setMessage("发现新版本,是否需要更新").
                    setTitle("新版本").setPositiveButton("下载", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    downLoadApk(Constants.BYTEERA_SERVICE + download_path);
                }
            }).setNegativeButton("取消", null);
            builder.create().show();
        }
    }

    protected void downLoadApk(final String download_url) {
        final ProgressDialog pd;
        pd = new  ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.setCancelable(false);
        pd.show();
        new Thread(){
            @Override
            public void run() {
                try {
                    File file = DownloadManager.getFileFromServer(download_url, pd);
                    if (file == null) {
                        Toast.makeText(getApplicationContext(), "请插入SD卡后进行更新！", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                    sleep(1000);
                    installApk(file);
                    pd.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }}.start();
    }


    protected void installApk(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file),  "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    private void initView() {
        unreadLabel = (ImageView) findViewById(R.id.unread_msg_number);
        unreadAddressLable = (ImageView) findViewById(R.id.unread_contact_number);
        mTabs = new RelativeLayout[4];
        mTabs[0] = (RelativeLayout) findViewById(R.id.btn_container_conversation);
        // mTabs[1] = (Button) findViewById(R.id.btn_address_list);
        mTabs[1] = (RelativeLayout) findViewById(R.id.rel_contact);
        mTabs[2] = (RelativeLayout) findViewById(R.id.rel_enterprise);
        mTabs[3] = (RelativeLayout) findViewById(R.id.btn_container_setting);
        // 把第一个tab设为选中状态
        mTabs[0].setSelected(true);
        ((TextView) mTabs[0].getChildAt(1)).setTextColor(Color.parseColor("#18a6e6"));
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_container_conversation:
                index = 0;
                break;
            case R.id.rel_contact:
                index = 1;
                unreadAddressLable.setVisibility(View.GONE);
                break;
            case R.id.rel_enterprise:
                index = 2;
                break;
            case R.id.btn_container_setting:
                index = 3;
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        mTabs[currentTabIndex].setSelected(false);
        ((TextView) mTabs[currentTabIndex].getChildAt(1)).setTextColor(Color.parseColor("#969696"));
        // 把当前tab设为选中状态
        mTabs[index].setSelected(true);
        ((TextView) mTabs[index].getChildAt(1)).setTextColor(Color.parseColor("#18a6e6"));
        currentTabIndex = index;
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (conflictBuilder != null) {
            conflictBuilder.create().dismiss();
            conflictBuilder = null;
        }
        if (friendRequestReceiver != null) {
            unregisterReceiver(friendRequestReceiver);
        }

        if (networkStateReceiver != null)
        {
            unregisterReceiver(networkStateReceiver);
        }
    }

    /** 刷新未读消息数 */
    public void updateUnreadLabel() {
        int count = getUnreadMsgCountTotal();
        if (count > 0) {
            // unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }

    /** 刷新申请与通知消息数 */
    public void updateUnreadAddressLable() {
        runOnUiThread(new Runnable() {
            public void run() {
                int count = getUnreadAddressCountTotal();
                List<FriendRelationShip> friendRequests = MyApp.getInstance().getFriendRequests();
                if (count > 0 || friendRequests.size() > 0) {
                    unreadAddressLable.setVisibility(View.VISIBLE);
                } else {
                    unreadAddressLable.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    /** 获取未读申请与通知消息 */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        if (((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList()
                .get(Constants.NEW_FRIENDS_USERNAME) != null)
            unreadAddressCountTotal = ((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(Constants.NEW_FRIENDS_USERNAME)
                    .getUnreadMsgCount();
        return unreadAddressCountTotal;
    }

    /** 获取未读消息数 */
    public int getUnreadMsgCountTotal() {
        int unreadMsgCountTotal = 0;
        int chatroomUnreadMsgCount = 0;
        unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
        for (EMConversation conversation : EMChatManager.getInstance().getAllConversations().values()) {
            if (conversation.getType() == EMConversation.EMConversationType.ChatRoom)
                chatroomUnreadMsgCount = chatroomUnreadMsgCount + conversation.getUnreadMsgCount();
        }
        return unreadMsgCountTotal - chatroomUnreadMsgCount;
    }


    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage: {
                //获取到message
                EMMessage message = (EMMessage) event.getData();


                notifyNewMessage(message);
                HXSDKHelper.getInstance().getNotifier().viberateAndPlayTone(message);
                refreshUI();


                break;
            }
            case EventDeliveryAck: {
                //获取到message
                EMMessage message = (EMMessage) event.getData();
                refreshUI();
                break;
            }
            case EventReadAck: {
                //获取到message
                EMMessage message = (EMMessage) event.getData();
                refreshUI();
                break;
            }
            case EventOfflineMessage: {
                refreshUI();
                break;
            }
            default:
                break;
        }

    }

    private void refreshUI() {
        runOnUiThread(new Runnable() {
            public void run() {
                // 刷新bottom bar消息未读数
                updateUnreadLabel();
                if (currentTabIndex == 0 && chatHistoryFragment != null) {
                    // 当前页面如果为聊天历史页面，刷新此页面
                    chatHistoryFragment.refresh();
                }
            }
        });
    }

    private InviteMessgeDao inviteMessgeDao;
    private UserDao userDao;

    /*** 好友变化listener */
    private class MyContactListener implements EMContactListener {

        @Override
        public void onContactAdded(List<String> usernameList) {
            // 保存增加的联系人
            Map<String, User> localUsers = ((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList();
            Map<String, User> toAddUsers = new HashMap<String, User>();
            for (String username : usernameList) {
                User user = setUserHead(username);
                // 添加好友时可能会回调added方法两次
                if (!localUsers.containsKey(username)) {
                    userDao.saveContact(user);
                }
                toAddUsers.put(username, user);
            }
            localUsers.putAll(toAddUsers);

        }

        @Override public void onContactDeleted(final List<String> usernameList) {
            // 被删除
            Map<String, User> localUsers = ((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList();
            for (String username : usernameList) {
                localUsers.remove(username);
                userDao.deleteContact(username);
                inviteMessgeDao.deleteMessage(username);
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    // 如果正在与此用户的聊天页面
                    if (ChatActivity.activityInstance != null && usernameList.contains(ChatActivity.activityInstance.getToChatUsername())) {
                        ToastUtil.showToastText(ChatActivity.activityInstance.getToChatUsername() + "已把你从他好友列表里移除");
                        ChatActivity.activityInstance.finish();
                    }
                    updateUnreadLabel();
                }
            });

        }

        @Override public void onContactInvited(String username, String reason) {
            // 接到邀请的消息，如果不处理(同意或拒绝)，掉线后，服务器会自动再发过来，所以客户端不需要重复提醒
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getGroupId() == null && inviteMessage.getFrom().equals(username)) {
                    inviteMessgeDao.deleteMessage(username);
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            msg.setReason(reason);
            LogUtil.d( username + "请求加你为好友,reason: " + reason);
            // 设置相应status
            msg.setStatus(InviteMesageStatus.BEINVITEED);
            notifyNewIviteMessage(msg);
        }

        @Override public void onContactAgreed(String username) {
            List<InviteMessage> msgs = inviteMessgeDao.getMessagesList();
            for (InviteMessage inviteMessage : msgs) {
                if (inviteMessage.getFrom().equals(username)) {
                    return;
                }
            }
            // 自己封装的javabean
            InviteMessage msg = new InviteMessage();
            msg.setFrom(username);
            msg.setTime(System.currentTimeMillis());
            LogUtil.d( username + "同意了你的好友请求");
            msg.setStatus(InviteMesageStatus.BEAGREED);
            notifyNewIviteMessage(msg);

        }

        @Override public void onContactRefused(String username) {
            // 参考同意，被邀请实现此功能,demo未实现
            LogUtil.d(username, username + "拒绝了你的好友请求");
        }

    }

    /** 保存提示新消息 */
    private void notifyNewIviteMessage(InviteMessage msg) {
        saveInviteMsg(msg);
        // 提示有新消息
        EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

        // 刷新bottom bar消息未读数
        updateUnreadAddressLable();
    }

    /** 保存邀请等msg */
    private void saveInviteMsg(InviteMessage msg) {
        // 保存msg
        inviteMessgeDao.saveMessage(msg);
        // 未读数加1
        User user = ((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList()
                .get(Constants.NEW_FRIENDS_USERNAME);
        if (user.getUnreadMsgCount() == 0)
            user.setUnreadMsgCount(user.getUnreadMsgCount() + 1);
    }

    private User setUserHead(String username) {
        User user = new User();
        user.setUsername(username);
        String headerName;
        if (!TextUtils.isEmpty(user.getNick())) {
            headerName = user.getNick();
        } else {
            headerName = user.getUsername();
        }
        if (username.equals(Constants.NEW_FRIENDS_USERNAME)) {
            user.setAvatar("");
        } else if (Character.isDigit(headerName.charAt(0))) {
            user.setAvatar("#");
        } else {
            user.setAvatar(HanziToPinyin.getInstance()
                    .get(headerName.substring(0, 1)).get(0).target.substring(0,
                            1).toUpperCase());
            char header = user.getAvatar().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setAvatar("#");
            }
        }
        return user;
    }


    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error == EMError.CONNECTION_CONFLICT)
                    {
                        showConflictDialog();
                    }
                    else
                    {
                        if (NetUtils.hasNetwork(MainActivity.this))
                            ToastUtil.showToastText("正在重新连接聊天服务，请稍后...");
                        else
                            ToastUtil.showToastText("当前网络不可用，请检查网络设置");
                    }
                }

            });
        }
    }

    /** MyGroupChangeListener */
    private class MyGroupChangeListener implements GroupChangeListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
            boolean hasGroup = false;
            for (EMGroup group : EMGroupManager.getInstance().getAllGroups()) {
                if (group.getGroupId().equals(groupId)) {
                    hasGroup = true;
                    break;
                }
            }
            if (!hasGroup)
                return;

            try{
                EMGroup returnGroup = EMGroupManager.getInstance().getGroupFromServer(groupId);
                EMGroupManager.getInstance().createOrUpdateLocalGroup(returnGroup);
            }
            catch (Exception e)
            {
                LogUtil.e(e, "Get group from server exception");
                return;
            }

            // 被邀请
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(inviter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            User user = DBManager.getInstance(getApplicationContext()).getUserByEasemobId(inviter);
            String inviter_name = "管理员";

            if(user != null)
                inviter_name = user.getNickName();

            msg.addBody(new TextMessageBody(inviter_name + "邀请你加入了群聊"));
            // 保存邀请消息
            EMChatManager.getInstance().saveMessage(msg);
            // 提醒新消息
            EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    // 刷新ui
                    if (currentTabIndex == 0 && chatHistoryFragment != null)
                        chatHistoryFragment.refresh();
                    if (CommonUtils.getTopActivity(MainActivity.this).equals(GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            });

        }

        @Override public void onInvitationAccpted(String groupId, String inviter, String reason) {
        }

        @Override public void onInvitationDeclined(String groupId, String invitee, String reason) {
        }

        @Override public void onUserRemoved(String groupId, String groupName) {
            // 提示用户被T了，demo省略此步骤
            // 刷新ui
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        updateUnreadLabel();
                        if (currentTabIndex == 0 && chatHistoryFragment != null)
                            chatHistoryFragment.refresh();
                        if (CommonUtils.getTopActivity(MainActivity.this)
                                .equals(GroupsActivity.class.getName())) {
                            GroupsActivity.instance.onResume();
                        }
                    } catch (Exception e) {
                        LogUtil.e(TAG, "refresh exception " + e.getMessage());
                    }

                }
            });
        }

        @Override
        public void onGroupDestroy(String groupId, String groupName) {
            // 群被解散
            // 提示用户群被解散,demo省略
            // 刷新ui
            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    if (currentTabIndex == 0 && chatHistoryFragment != null)
                        chatHistoryFragment.refresh();
                    if (CommonUtils.getTopActivity(MainActivity.this).equals(
                            GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            });

        }

        @Override
        public void onApplicationReceived(String groupId, String groupName, String applyer, String reason) {
            // 用户申请加入群聊
            InviteMessage msg = new InviteMessage();
            msg.setFrom(applyer);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            LogUtil.d( applyer + " 申请加入群聊：" + groupName);
            msg.setStatus(InviteMesageStatus.BEAPPLYED);
            notifyNewIviteMessage(msg);
        }


        @Override
        public void onApplicationAccept(String groupId, String groupName, String accepter) {
            // 加群申请被同意
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(accepter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new TextMessageBody(accepter + "同意了你的群聊申请"));
            // 保存同意消息
            EMChatManager.getInstance().saveMessage(msg);
            // 提醒新消息
            EMNotifier.getInstance(getApplicationContext()).notifyOnNewMsg();

            runOnUiThread(new Runnable() {
                public void run() {
                    updateUnreadLabel();
                    // 刷新ui
                    if (currentTabIndex == 0 && chatHistoryFragment != null)
                        chatHistoryFragment.refresh();
                    if (CommonUtils.getTopActivity(MainActivity.this).equals(
                            GroupsActivity.class.getName())) {
                        GroupsActivity.instance.onResume();
                    }
                }
            });
        }

        @Override
        public void onApplicationDeclined(String groupId, String groupName, String decliner, String reason) {
            // 加群申请被拒绝，demo未实现
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ShortcutBadger.with(getApplicationContext()).remove();
        MyApp.getInstance().clearBadgerCount();

        if(!CommonUtils.isNetWorkConnected(this))
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("错误")
                    .setMessage("当前网络不可用")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    }).create().show();
            return;
        }

        if (!isConflict) {
            updateUnreadLabel();
            updateUnreadAddressLable();
            EMChatManager.getInstance().activityResumed();
        }

        BankHXSDKHelper sdkHelper = (BankHXSDKHelper) BankHXSDKHelper.getInstance();
        sdkHelper.pushActivity(this);
        EMChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage, EMNotifierEvent.Event.EventOfflineMessage,
                        EMNotifierEvent.Event.EventDeliveryAck, EMNotifierEvent.Event.EventReadAck});

        // Check user list, if user list is empty then force to login activity
        int totalUserCount = DBManager.getInstance(this).getAllUserCount();
        if (totalUserCount < 1)
        {
            MyApp.getInstance().logout(null);
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EMChatManager.getInstance().unregisterEventListener(this);
        BankHXSDKHelper sdkHelper = (BankHXSDKHelper) BankHXSDKHelper.getInstance();
        sdkHelper.popActivity(this);
    }

    private android.app.AlertDialog.Builder conflictBuilder;
    private boolean isConflictDialogShow;

    /** 显示帐号在别处登录dialog */
    private void showConflictDialog() {
        isConflictDialogShow = true;
//        BankHXSDKHelper.getInstance().logout(true, null);
        MyApp.getInstance().logout(null);

        if (!MainActivity.this.isFinishing()) {
            // clear up global variables
            try {
                if (conflictBuilder == null)
                    conflictBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
                conflictBuilder.setTitle("下线通知");
                conflictBuilder.setMessage(R.string.connect_conflict);
                conflictBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        conflictBuilder = null;
                        finish();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                });
                conflictBuilder.setCancelable(false);
                conflictBuilder.create().show();
                isConflict = true;
            } catch (Exception e) {
                LogUtil.e(TAG, "---------color conflictBuilder error" + e.getMessage());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getIntent().getBooleanExtra("conflict", false) && !isConflictDialogShow)
            showConflictDialog();
    }

}
