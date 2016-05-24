package com.byteera.bank.activity.chatgroup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.byteera.bank.BankHXSDKModel;
import com.byteera.bank.activity.contact.ContactDetailActivity;
import com.byteera.bank.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.AlertDialog;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.activity.ExitGroupDialog;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.ExpandGridView;
import com.byteera.bank.widget.SwitchView;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.util.NetUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = GroupDetailsActivity.class.getSimpleName();
    private static final int REQUEST_CODE_ADD_USER = 0;
    private static final int REQUEST_CODE_EXIT = 1;
    private static final int REQUEST_CODE_EXIT_DELETE = 2;
    private static final int REQUEST_CODE_CLEAR_ALL_HISTORY = 3;

    private String groupId = "";
    private RelativeLayout rlExit;
    private EMGroup group;
    private GridAdapter adapter;
    private ProgressDialog progressDialog;
    public static GroupDetailsActivity instance;
    private boolean isOwner;


    @ViewInject(R.id.gridview) ExpandGridView userGridview;
    @ViewInject(R.id.tv_groupName) TextView tvGroupName;
    @ViewInject(R.id.tv_max_num) TextView tvManNum;
    @ViewInject(R.id.sv_tishi) SwitchView svTishi;    //是否提示消息
    @ViewInject(R.id.rl_erweima) RelativeLayout rlerWeiMa;
    @ViewInject(R.id.tv_exit) TextView tvExit;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        ViewUtils.inject(this);
        instance = this;
        initGridView();
        rlExit = (RelativeLayout) findViewById(R.id.rl_exit);
        rlExit.setOnClickListener(this);
        rlerWeiMa.setOnClickListener(this);

        List<String> disabledGroups = MyApp.getInstance().getHxSdkHelper().getModel()
                .getDisabledGroups();

        if(disabledGroups != null)
            svTishi.setState(disabledGroups.contains(groupId));

        svTishi.setOnSwitchStateChangedListener(new SwitchView.OnSwitchStateChangedListener() {
            @Override
            public void onStateChanged(int state) {
                List<String> disabledGroups = MyApp.getInstance().getHxSdkHelper().getModel()
                        .getDisabledGroups();

                if(disabledGroups == null)
                    disabledGroups = new ArrayList<>();

                if((state == SwitchView.STATE_SWITCH_ON || state == SwitchView.STATE_SWITCH_ON2)
                        && !disabledGroups.contains(groupId))
                {
                    disabledGroups.add(groupId);
                    MyApp.getInstance().getHxSdkHelper().getModel().setDisabledGroups(disabledGroups);
                }
                else if((state == SwitchView.STATE_SWITCH_OFF || state == SwitchView.STATE_SWITCH_OFF2)
                        && disabledGroups.contains(groupId))
                {
                    disabledGroups.remove(groupId);
                    MyApp.getInstance().getHxSdkHelper().getModel().setDisabledGroups(disabledGroups);
                }
            }
        });

        findViewById(R.id.rl_clear).setOnClickListener(this);
    }

    private void initGridView() {
        // 获取传过来的groupid
        groupId = getIntent().getStringExtra("groupId");
        group = EMGroupManager.getInstance().getGroup(groupId);
        if (group == null) {
            return;
        }

        UserDao userDao = new UserDao(baseContext);
        EMGroup emGroup = group;
        List<User> tempList = new ArrayList<>();
        for (int j = 0; j < emGroup.getMembers().size(); j++) {
            String s = emGroup.getMembers().get(j);
            User user = userDao.selectUser(s);
            tempList.add(user);
        }

        adapter = new GridAdapter(this, R.layout.grid, tempList);
        isOwner = group.getOwner().equals(MyApp.getInstance().getUserName());

        userGridview.setAdapter(adapter);
        tvGroupName.setText(group.getName());
        tvManNum.setText(group.getMembers().size() + "/" + group.getMaxUsers() + "人");
        if(isOwner)
        {
            tvExit.setText("解散该群");
        }
        else
        {
            tvExit.setText("退出该群");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailsActivity.this);
                progressDialog.setMessage("正在添加...");
                progressDialog.setCanceledOnTouchOutside(false);
            }

            switch (requestCode) {
                case REQUEST_CODE_ADD_USER:// 添加群成员
                    final String[] newmembers = data.getStringArrayExtra("newmembers");
                    progressDialog.show();
                    addMembersToGroup(newmembers);
                    break;
                case REQUEST_CODE_EXIT: // 退出群
                    progressDialog.setMessage("正在退出群聊...");
                    progressDialog.show();
                    exitGrop();
                    break;
                case REQUEST_CODE_CLEAR_ALL_HISTORY:
                    // 清空此群聊的聊天记录
                    progressDialog.setMessage("正在清空群消息...");
                    progressDialog.show();
                    clearGroupHistory();
                    break;
            }
        }
    }

    /** 点击解散群组按钮 @param view */
    public void exitDeleteGroup(View view) {
        startActivityForResult(new Intent(this, ExitGroupDialog.class).putExtra("deleteToast", getString(R.string.dissolution_group_hint)), REQUEST_CODE_EXIT_DELETE);

    }

    /** 清空群聊天记录 */
    public void clearGroupHistory() {
        EMChatManager.getInstance().clearConversation(group.getGroupId());
        progressDialog.dismiss();
    }

    /** 退出群组 */
    private void exitGrop() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    if(isOwner)
                        EMGroupManager.getInstance().exitAndDeleteGroup(groupId);
                    else
                        EMGroupManager.getInstance().exitFromGroup(groupId);

                    MyApp.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override public void run() {
                            setResult(ChatActivity.RESULT_CODE_EXIT_GROUP);
                            ActivityUtil.finishActivity(baseContext);
                        }
                    }, 1000);
                } catch (final Exception e) {
                    MyApp.getMainThreadHandler().post(new Runnable() {
                        @Override public void run() {
                            if(progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();
                            if(isOwner)
                                ToastUtil.showToastText("解散群聊失败");
                            else
                                ToastUtil.showToastText("退出群聊失败");
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 增加群成员
     */
    private void addMembersToGroup(final String[] newmembers) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 创建者调用add方法
                    if (EMChatManager.getInstance().getCurrentUser().equals(group.getOwner())) {
                        EMGroupManager.getInstance().addUsersToGroup(groupId, newmembers);
                    } else {
                        // 一般成员调用invite方法
                        EMGroupManager.getInstance().inviteUser(groupId, newmembers, null);
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //需要重新设置界面
                            initGridView();
                            ((TextView) findViewById(R.id.group_name)).setText(group.getGroupName() + "(" + group.getAffiliationsCount() + "人)");
                            progressDialog.dismiss();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            ToastUtil.showToastText("添加群成员失败");
                        }
                    });
                }
            }
        }).start();
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_clear: // 清空聊天记录
                Intent intent = new Intent(GroupDetailsActivity.this, AlertDialog.class);
                intent.putExtra("cancel", true);
                intent.putExtra("titleIsCancel", true);
                intent.putExtra("msg", "确定清空此群的聊天记录吗？");
                startActivityForResult(intent, REQUEST_CODE_CLEAR_ALL_HISTORY);
                break;
            case R.id.rl_exit:
                startActivityForResult(new Intent(this, ExitGroupDialog.class), REQUEST_CODE_EXIT);
                break;
            case R.id.rl_erweima:       //进入群二维码
                Intent groupIntent = new Intent(baseContext, GroupErWeiMaActivity.class);
                groupIntent.putExtra("groupId", groupId);
                ActivityUtil.startActivity(baseContext, groupIntent);
                break;
        }
    }

    private class GridAdapter extends ArrayAdapter<User> {
        private int res;
        public boolean isInDeleteMode;

        public GridAdapter(Context context, int textViewResourceId, List<User> users) {
            super(context, textViewResourceId, users);
            res = textViewResourceId;
            isInDeleteMode = false;
        }

        @Override public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(res, null);
            }

            ImageView ivHead = ViewHolder.get(convertView, R.id.iv_head);
            TextView tvName = ViewHolder.get(convertView, R.id.tv_name);
            RelativeLayout rl = ViewHolder.get(convertView, R.id.rl);
            // 最后一个item，减人按钮
            if (position == getCount() - 1) {
                tvName.setText("");
                // 设置成删除按钮
                ivHead.setImageResource(R.drawable.smiley_minus_btn_nor);
                // 如果不是创建者或者没有相应权限，不提供加减人按钮
                if (!group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
                    // if current user is not group admin, hide add/remove btn
                    convertView.setVisibility(View.GONE);
                } else { // 显示删除按钮
                    if (isInDeleteMode) {
                        // 正处于删除模式下，隐藏删除按钮
                        convertView.setVisibility(View.GONE);
                    } else {
                        // 正常模式
                        convertView.setVisibility(View.VISIBLE);
                        convertView.findViewById(R.id.badge_delete).setVisibility(View.GONE);
                    }

                    rl.setOnClickListener(new OnClickListener() {
                        @Override public void onClick(View v) {
                            LogUtil.d("删除按钮被点击");
                            isInDeleteMode = true;
                            notifyDataSetChanged();
                        }
                    });
                }
            } else if (position == getCount() - 2) { // 添加群组成员按钮
                tvName.setText("");
                ivHead.setImageResource(R.drawable.smiley_add_btn_nor);
                // 如果不是创建者或者没有相应权限
                if (!group.isAllowInvites() && !group.getOwner().equals(EMChatManager.getInstance().getCurrentUser())) {
                    // if current user is not group admin, hide add/remove btn
                    convertView.setVisibility(View.GONE);
                } else {
                    // 正处于删除模式下,隐藏添加按钮
                    if (isInDeleteMode) {
                        convertView.setVisibility(View.GONE);
                    } else {
                        convertView.setVisibility(View.VISIBLE);
                        convertView.findViewById(R.id.badge_delete).setVisibility(View.GONE);
                    }
                    rl.setOnClickListener(new OnClickListener() {
                        @Override public void onClick(View v) {
                            LogUtil.d("添加按钮被点击");
                            // 进入选人页面
                            startActivityForResult((new Intent(GroupDetailsActivity.this, GroupPickContactsActivity.class).putExtra("groupId", groupId)), REQUEST_CODE_ADD_USER);
                        }
                    });
                }
            } else { // 普通item，显示群组成员
                final User user = getItem(position);
                convertView.setVisibility(View.VISIBLE);
                if (user != null) {
                    if (user.getNickName() != null) {
                        tvName.setText(user.getNickName());
                    }

                    String avatar = user.getAvatar();
                    if(!StringUtils.isEmpty(avatar))
                        ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + avatar, ivHead);
                    else
                        ImageLoader.getInstance().displayImage(null, ivHead);

                    ivHead.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(GroupDetailsActivity.this, ContactDetailActivity.class);
                            intent.putExtra("user_id", user.getUserId());
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    });
                } else {
                    ImageLoader.getInstance().displayImage(null, ivHead);
                }

//                ivHead.setImageResource(R.drawable.default_avatar);
                // demo群组成员的头像都用默认头像，需由开发者自己去设置头像
                if (isInDeleteMode) {
                    // 如果是删除模式下，显示减人图标
                    convertView.findViewById(R.id.badge_delete).setVisibility(View.VISIBLE);
                } else {
                    convertView.findViewById(R.id.badge_delete).setVisibility(View.GONE);
                }
                rl.setOnClickListener(new OnClickListener() {
                    @Override public void onClick(View v) {
                        if (isInDeleteMode) {
                            // 如果是删除自己，return
                            if (EMChatManager.getInstance().getCurrentUser().equals(user.getEasemobId())) {
                                startActivity(new Intent(GroupDetailsActivity.this, AlertDialog.class).putExtra("msg", "不能删除自己"));
                                return;
                            }

                            if (!NetUtils.hasNetwork(getApplicationContext())) {
                                ToastUtil.showToastText(getString(R.string.network_unavailable));
                                return;
                            }
                            deleteMembersFromGroup(user.getEasemobId());
                        }
                    }

                    protected void deleteMembersFromGroup(final String username) {
                        final ProgressDialog deleteDialog = new ProgressDialog(GroupDetailsActivity.this);
                        deleteDialog.setMessage("正在移除...");
                        deleteDialog.setCanceledOnTouchOutside(false);
                        deleteDialog.show();
                        new Thread(new Runnable() {
                            @Override public void run() {
                                try {
                                    // 删除被选中的成员
                                    EMGroupManager.getInstance().removeUserFromGroup(groupId, username);
                                    isInDeleteMode = false;
                                    MyApp.getMainThreadHandler().postDelayed(new Runnable() {
                                        @Override public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override public void run() {
                                                    deleteDialog.dismiss();
                                                    initGridView();
                                                    ((TextView) findViewById(R.id.group_name)).setText(group.getGroupName() + "(" + group.getAffiliationsCount() + "人)");
                                                }
                                            });
                                        }
                                    }, 1000);
                                } catch (final Exception e) {
                                    deleteDialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ToastUtil.showToastText("删除失败");
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
            }
            return convertView;
        }

        @Override public int getCount() {
            return super.getCount() + 2;
        }
    }

    protected void updateGroup() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMGroup returnGroup = EMGroupManager.getInstance().getGroupFromServer(groupId);
                    EMGroupManager.getInstance().createOrUpdateLocalGroup(returnGroup);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public void back(View view) {
        setResult(RESULT_OK);
        ActivityUtil.finishActivity(baseContext);
    }

    @Override public void onBackPressed() {
        setResult(RESULT_OK);
        ActivityUtil.finishActivity(baseContext);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        instance = null;
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
