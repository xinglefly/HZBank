package com.byteera.bank.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.byteera.bank.utils.LogUtil;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.chatgroup.GroupPickContactsActivity;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.bank.widget.SwitchView;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.io.Serializable;
import java.util.List;

public class NewMeetingGroupActivity extends BaseActivity {
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.edit_group_name) private EditText groupNameEditText;
    @ViewInject(R.id.cb_public) private SwitchView checkBox;
    @ViewInject(R.id.cb_member_inviter) private SwitchView memberCheckbox;
    @ViewInject(R.id.ll_open_invite) private LinearLayout openInviteContainer;
    @ViewInject(R.id.tv_quanxian) private TextView tvQuanXian;
    @ViewInject(R.id.tv_yaoqing) private TextView tvYaoqing;

    private ProgressDialog progressDialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting_group);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });
        //公开群和允许其他人员加入
        checkBox.setState(true);
        memberCheckbox.setState(true);
        if (checkBox.getState() == SwitchView.STATE_SWITCH_ON) {
            tvQuanXian.setText("公开群");
            openInviteContainer.setVisibility(View.VISIBLE);
        } else {
            tvQuanXian.setText("私有群");
            openInviteContainer.setVisibility(View.GONE);
        }
        checkBox.setOnSwitchStateChangedListener(new SwitchView.OnSwitchStateChangedListener() {
            @Override public void onStateChanged(int state) {
                if (checkBox.getState() == SwitchView.STATE_SWITCH_ON) {
                    tvQuanXian.setText("公开群");
                    openInviteContainer.setVisibility(View.VISIBLE);
                } else {
                    tvQuanXian.setText("私有群");
                    openInviteContainer.setVisibility(View.GONE);
                }
            }
        });
        memberCheckbox.setOnSwitchStateChangedListener(new SwitchView.OnSwitchStateChangedListener() {
            @Override public void onStateChanged(int state) {
                if (memberCheckbox.getState() == SwitchView.STATE_SWITCH_ON) {
                    tvYaoqing.setText("允许群成员邀请其他人");

                } else {
                    tvYaoqing.setText("不允许群成员邀请其他人");
                }
            }
        });
    }


    @Override protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //新建群组
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在创建群聊...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            new Thread(new Runnable() {
                @Override public void run() {
                    // 调用sdk创建群组方法
                    String groupName = groupNameEditText.getText().toString().trim();
                    String desc = "";
                    // String desc = introductionEditText.getText().toString();
                    if (getIntent().getBooleanExtra("description", false)) {    //如果创建的是一般的群而不是会议群的话,就要将这个标示符给带上
                        desc = "ZJSD_WORK_GROUP";
                    }
                    String[] members = data.getStringArrayExtra("newmembers");

                    try {
                        //只创建公开群 不创建私有群
                        EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, false);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                setResult(RESULT_OK);
                                ActivityUtil.finishActivity(baseContext);
                            }
                        });

                    } catch (final EaseMobException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                ToastUtil.showLongToastText("创建群组失败:" + e.getLocalizedMessage());
                            }
                        });
                    }
                }
            }).start();
        }
    }


    @OnClick(R.id.tv_next)
    public void onClickListener(View v) {
        switch (v.getId()) {
            case R.id.tv_next:
                save();
                break;
        }
    }

    public void save() {
        String name = groupNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Intent intent = new Intent(this, AlertDialog.class);
            intent.putExtra("msg", "群组名称不能为空");
            ActivityUtil.startActivity(baseContext, intent);
        } else {
            // 进通讯录选人
            List<String> huanxins = (List<String>) getIntent().getSerializableExtra("huanxin");
            if (huanxins == null) {
                ActivityUtil.startActivityForResult(baseContext, new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), 0);
            } else {
                ActivityUtil.startActivityForResult(baseContext, new Intent(this, GroupPickContactsActivity.class).putExtra("huanxin", (Serializable) huanxins), 0);
            }

        }
    }
}
