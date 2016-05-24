package com.byteera.bank.activity.contact;

import android.content.Intent;
import android.os.Bundle;
import com.byteera.bank.utils.LogUtil;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Created by lieeber on 15/8/29. */
public class HuiYiQunActivity extends BaseActivity {
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.list_view) private ListView mListView;
    private Executor executorService = Executors.newSingleThreadExecutor();
    private ArrayList<EMGroup> allGroups;
    private HuiYiQunAdapter huiYiQunAdapter;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.huiyiqun_activity);
        ViewUtils.inject(this);
        dialog.show();
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });
        initListView();
    }

    private void initListView() {
        huiYiQunAdapter = new HuiYiQunAdapter(this);
        mListView.setAdapter(huiYiQunAdapter);
        //工作组
        allGroups = new ArrayList<>();
        executorService.execute(new Runnable() {
            @Override public void run() {
                try {
                    List<EMGroup> groupsFromServer = EMGroupManager.getInstance().getGroupsFromServer();//需异步处理
                    for (int i = 0; i < groupsFromServer.size(); i++) {
                        EMGroup group = EMGroupManager.getInstance().getGroupFromServer(groupsFromServer.get(i).getGroupId());
                        // 更新本地数据
                        if (group == null) {
                            group = EMGroupManager.getInstance().getGroup(groupsFromServer.get(i).getGroupId());
                        } else {
                            EMGroupManager.getInstance().createOrUpdateLocalGroup(group);
                        }

                        if (group != null) {
                            allGroups.add(group);
                        }
                    }
                    final List<EMGroup> tempGroup = new ArrayList<>();
                    //需要剔除掉会议群
                    for (int i = 0; i < allGroups.size(); i++) {
                        LogUtil.e("descriptaion", allGroups.get(i).getDescription());
                        if (!allGroups.get(i).getDescription().equals("ZJSD_WORK_GROUP")) {
                            tempGroup.add(allGroups.get(i));
                        }
                    }

                    final List<List<User>> groups = new ArrayList<>();
                    UserDao userDao = new UserDao(baseContext);
                    for (int i = 0; i < tempGroup.size(); i++) {
                        EMGroup emGroup = tempGroup.get(i);
                        List<User> tempList = new ArrayList<>();
                        for (int j = 0; j < emGroup.getMembers().size(); j++) {
                            String s = emGroup.getMembers().get(j);
                            User user = userDao.selectUser(s);
                            tempList.add(user);
                        }
                        groups.add(tempList);
                    }
                    MyApp.getMainThreadHandler().post(new Runnable() {
                        @Override public void run() {
                            setView(groups, tempGroup);
                            dialog.dismiss();
                        }
                    });
                } catch (EaseMobException e) {
                    LogUtil.e(e, "getGroupsFromServer Exception");
                    //ToastUtil.showToastText("获取群组信息错误，请稍后重试！");
                    dialog.dismiss();
                }
            }
        });
    }

    private void setView(List<List<User>> groups, List<EMGroup> tempGroup) {
        huiYiQunAdapter.setData(groups, tempGroup);
        huiYiQunAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳转到群聊天界面
                Intent intent = new Intent(baseContext, ChatActivity.class);
                intent.putExtra("isChat", false);
                intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                intent.putExtra("position", position - 1);
                intent.putExtra("groupId", huiYiQunAdapter.getGroupItem(position).getGroupId());
                startActivity(intent);
            }
        });
    }
}
