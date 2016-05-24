package com.byteera.bank.activity.conversation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;

import com.byteera.bank.activity.NewWorkingGroupActivity;
import com.byteera.bank.activity.VerticalCaptureActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.utils.LogUtil;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.activity.MainActivity;
import com.byteera.bank.activity.contact.ContactDetailActivity;
import com.byteera.bank.activity.setting.ConversationPop;
import com.byteera.bank.activity.setting.MessageNotifyActivity;
import com.byteera.bank.adapter.ChatAllHistoryAdapter;
import com.byteera.bank.db.InviteMessgeDao;
import com.byteera.bank.domain.Announcement;
import com.byteera.bank.utils.GetDataUtils;
import com.byteera.bank.utils.StringUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.widget.MySwipeRefreshLayout;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.exceptions.EaseMobException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class ConversationFragment extends Fragment {

    @ViewInject(R.id.list) private ListView listView;
    @ViewInject(R.id.swipe_layout) private MySwipeRefreshLayout mySwipeRefreshLayout;
    @ViewInject(R.id.unread_msg_number) private TextView unread_msg_number;
    @ViewInject(R.id.time) private TextView tv_time;
    @ViewInject(R.id.tv_message) private TextView tv_message;
    @ViewInject(R.id.iv_jiahao) private ImageView ivJiahao;
    private ChatAllHistoryAdapter adapter;

    private boolean hidden;
    private List<EMGroup> groups;
    private List<EMConversation> conversationList;

    private BroadcastReceiver broadCastReceiver = null;

    List<Announcement> msgNotifyLists = new ArrayList<>();
    List<Announcement> msgShowLists = new ArrayList<>();

    private String username = "";
    private ConversationPop conversationPop;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.inject(this, getView());

        initListView();
        setEvent();
        registerForContextMenu(listView);
    }


    public void updateUI() {
        msgShowLists.clear();
        EMConversation conversation = EMChatManager.getInstance().getConversation(username);
        conversation.resetUnreadMsgCount();
        unread_msg_number.setVisibility(View.INVISIBLE);
    }

    @OnClick({R.id.rel_gonggao, R.id.iv_jiahao, R.id.iv_search})
    public void clickListener(View view) {
        switch (view.getId()) {
            case R.id.rel_gonggao:
                final Intent intent = new Intent(getActivity(), MessageNotifyActivity.class);
                ActivityUtil.startActivity(getActivity(), intent);
                break;
            case R.id.iv_search:
                break;
            case R.id.iv_jiahao:
                if (conversationPop == null) {
                    conversationPop = new ConversationPop(getActivity());
                }
                conversationPop.showAtLocation(ivJiahao, getActivity());
                conversationPop.setSaoyisaoListener(new ConversationPop.SaoyisaoListener() {
                    @Override public void saoyisao() {
                        scanQRCode();
                    }
                });
                conversationPop.setGroupChatListener(new ConversationPop.GroupChatListener() {
                    @Override public void groupChat() {
                        //创建群聊
                        Intent intent = new Intent(getActivity(), NewWorkingGroupActivity.class);
                        intent.putExtra("description", true);
                        ActivityUtil.startActivity(getActivity(), intent);
                    }
                });
                break;
        }
    }

    private void scanQRCode()
    {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setBeepEnabled(true);
        integrator.setPrompt("请扫描好友或者群的二维码图片");
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(VerticalCaptureActivity.class);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private void setEvent() {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMConversation conversation = adapter.getItem(position);
                String username = conversation.getUserName();
                if (username.equals(MyApp.getInstance().getUserName()))
                    ToastUtil.showToastText("不能和自己聊天");
                else {
                    // 进入聊天页面
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    EMContact emContact = null;
                    groups = EMGroupManager.getInstance().getAllGroups();
                    for (EMGroup group : groups) {
                        if (group.getGroupId().equals(username)) {
                            emContact = group;
                            break;
                        }

                    }
                    if (emContact != null) {
                        intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                        intent.putExtra("groupId", ((EMGroup) emContact).getGroupId());
                    } else {
                        // it is single chat
                        intent.putExtra("userId", username);
                    }
                    startActivity(intent);
                }
            }
        });
        mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            Thread.sleep(1000);
                            MyApp.getHandler().post(new Runnable() {
                                @Override public void run() {
                                    initListView();
                                    mySwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

    }

    private void initListView() {
        conversationList = GetDataUtils.loadConversationsWithRecentChat();
        adapter = new ChatAllHistoryAdapter(getActivity(), 1, conversationList);
        // 设置adapter
        listView.setAdapter(adapter);
        groups = EMGroupManager.getInstance().getAllGroups();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.delete_message, menu);
    }

    @Override public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_message) {
            EMConversation tobeDeleteCons = adapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
            // 删除此会话
            EMChatManager.getInstance().deleteConversation(tobeDeleteCons.getUserName(), tobeDeleteCons.isGroup());
            InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(getActivity());
            inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
            adapter.remove(tobeDeleteCons);
            adapter.notifyDataSetChanged();

            // 更新消息未读数
            ((MainActivity) getActivity()).updateUnreadLabel();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        if (Constants.isRecieveMsgNotify) {
            updateUI();
            Constants.isRecieveMsgNotify = false;
        }

        if(adapter != null)
        {
            adapter.refreshList();
        }

        refreshAnnouncement();
    }

    public void refreshAnnouncement() {
        Announcement lastAnnouncement = DBManager.getInstance(getActivity()).getLastAnnouncement();
        if(lastAnnouncement != null && tv_time != null && tv_message != null)
        {
            tv_time.setText(StringUtil.checkTime(lastAnnouncement.getTime() * 1000));
            tv_message.setText(lastAnnouncement.getTitle());
        }
    }

    /**
     * 获取所有会话
     */
    private List<EMConversation> loadConversationsWithRecentChat() {
        // 获取所有会话，包括陌生人
        Hashtable<String, EMConversation> conversations = EMChatManager.getInstance().getAllConversations();
        List<EMConversation> list = new ArrayList<EMConversation>();
        // 过滤掉messages seize为0的conversation
        for (EMConversation conversation : conversations.values()) {
            if (conversation.getAllMessages().size() != 0)
                list.add(conversation);
        }
        // 排序
        sortConversationByLastChatTime(list);
        return list;
    }

    /**
     * 根据最后一条消息的时间排序
     */
    private void sortConversationByLastChatTime(List<EMConversation> conversationList) {
        Collections.sort(conversationList, new Comparator<EMConversation>() {
            @Override
            public int compare(final EMConversation con1, final EMConversation con2) {
                EMMessage con2LastMessage = con2.getLastMessage();
                EMMessage con1LastMessage = con1.getLastMessage();
                if (con2LastMessage.getMsgTime() == con1LastMessage.getMsgTime()) {
                    return 0;
                } else if (con2LastMessage.getMsgTime() > con1LastMessage.getMsgTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (!hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden && !((MainActivity) getActivity()).isConflict) {
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播
        if (broadCastReceiver != null) {
            getActivity().unregisterReceiver(broadCastReceiver);
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            if(result.getContents() == null) {
                ToastUtil.showToastText("扫描已取消！");
            } else {
                String scanResult = result.getContents();
                LogUtil.d("scanResult: %s", scanResult);
                if (scanResult.contains("ZJSD_USER@")) {
                    scanResult = scanResult.substring("ZJSD_USER@".length());
                    LogUtil.e("userId", scanResult);
                    Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                    intent.putExtra("user_id", scanResult);
                    ActivityUtil.startActivity(getActivity(), intent);
                } else if (scanResult.contains("ZJSD_GROUP@")) {
                    //根据id获取到群组id
                    scanResult = scanResult.substring("ZJSD_GROUP@".length());
                    final String finalScanResult = scanResult;
                    MyApp.getInstance().executorService.execute(new Runnable() {
                        @Override public void run() {
                            try {
                                EMGroupManager.getInstance().joinGroup(finalScanResult);
                                ToastUtil.showToastText("加群成功");
                            } catch (EaseMobException e) {
                                ToastUtil.showToastText("加群失败" + e.toString());
                                LogUtil.e(e, "Join group failed");
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter cmdFilter = new IntentFilter(Constants.CMD_BROADCAST);
        if (broadCastReceiver == null) {
            broadCastReceiver = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    String content = intent.getStringExtra("content");
                    final String title = intent.getStringExtra("title");
                    final long time = intent.getLongExtra("time", 0);

                    msgNotifyLists.add(new Announcement(title, time, content));
                    msgShowLists.add(new Announcement(title, time, content));

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            unread_msg_number.setVisibility(View.VISIBLE);
                            unread_msg_number.setText(msgShowLists.size() + "");
                            refreshAnnouncement();
                        }
                    });
                }
            };

            //注册广播接收者
            getActivity().registerReceiver(broadCastReceiver, cmdFilter);
        }
    }

}
