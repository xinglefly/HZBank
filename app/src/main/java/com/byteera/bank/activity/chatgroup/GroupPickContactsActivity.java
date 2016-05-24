
package com.byteera.bank.activity.chatgroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.adapter.ContactAdapter;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.widget.Sidebar;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupPickContactsActivity extends BaseActivity {
    private ListView listView;
    private Sidebar sidebar;
    /** 是否为一个新建的群组 */
    protected boolean isCreatingNewGroup;
    /** 是否为单选 */
    private boolean isSignleChecked;
    private PickContactAdapter contactAdapter;
    /** group中一开始就有的成员 */
    private List<String> exitingMembers;
    private ExecutorService executorService;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_pick_contacts);
        sidebar = (Sidebar) findViewById(R.id.sidebar);
        dialog.show();
        listView = (ListView) findViewById(R.id.list);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override public void run() {
                String groupId = getIntent().getStringExtra("groupId");
                List<String> huanxins = (List<String>) getIntent().getSerializableExtra("huanxin");
                if (groupId == null) {// 创建群组
                    isCreatingNewGroup = true;
                } else {
                    // 获取此群组的成员列表
                    EMGroup group = EMGroupManager.getInstance().getGroup(groupId);
                    exitingMembers = group.getMembers();
                }
                if (exitingMembers == null)
                    exitingMembers = new ArrayList<>();
                // 获取好友列表,从本地数据库检索
                List<User> alluserList = new ArrayList<>();

                if (huanxins == null) {
                    try {
                        List<User> allUser = DBManager.getInstance(baseContext).getAllUserList();
                        if (allUser != null) {
                            alluserList = allUser;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    UserDao dao = new UserDao(baseContext);
                    for (int i = 0; i < huanxins.size(); i++) {
                        User user = dao.selectUser(huanxins.get(i));
                        alluserList.add(user);
                    }
                }

                final List<User> finalAlluserList = alluserList;
                LogUtil.d("selectContact-->", "contact-->" + finalAlluserList.size());
                MyApp.getHandler().post(new Runnable() {
                    @Override public void run() {
                        contactAdapter = new PickContactAdapter(baseContext,
                                R.layout.row_contact_with_checkbox, finalAlluserList);
                        listView.setAdapter(contactAdapter);
                        sidebar.setListView(listView);
                        dialog.dismiss();
                    }
                });
            }
        });

        ((Sidebar) findViewById(R.id.sidebar)).setListView(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                checkBox.toggle();
            }
        });
    }


    /** 确认选择的members @param v */
    public void save(View v) {
        List<String> toBeAddMembers = getToBeAddMembers();
        setResult(RESULT_OK, new Intent().putExtra("newmembers", toBeAddMembers.toArray(new String[toBeAddMembers.size()])));
        ActivityUtil.finishActivity(baseContext);
    }

    /** 获取要被添加的成员 @return */
    private List<String> getToBeAddMembers() {
        List<String> members = new ArrayList<>();
        int length = contactAdapter.isCheckedArray.length;
        for (int i = 0; i < length; i++) {
            String username = contactAdapter.getItem(i).getEasemobId();   //获得环信id
            if (contactAdapter.isCheckedArray[i] && !exitingMembers.contains(username)) {
                members.add(username);
            }
        }
        return members;
    }

    private class PickContactAdapter extends ContactAdapter {
        private boolean[] isCheckedArray;

        public PickContactAdapter(Context context, int resource, List<User> users) {
            super(context, resource, users);
            isCheckedArray = new boolean[users.size()];
        }

        @Override public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final String easemobId = getItem(position).getEasemobId();
            // 选择框checkbox
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            if (exitingMembers != null && exitingMembers.contains(easemobId)) {
                checkBox.setButtonDrawable(R.drawable.checkbox_bg_gray_selector);
            } else {
                checkBox.setButtonDrawable(R.drawable.checkbox_bg_selector);
            }
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // 群组中原来的成员一直设为选中状态
                    if (exitingMembers.contains(easemobId)) {
                        isChecked = true;
                        checkBox.setChecked(true);
                    }
                    isCheckedArray[position] = isChecked;
                    //如果是单选模式
                    if (isSignleChecked && isChecked) {
                        for (int i = 0; i < isCheckedArray.length; i++) {
                            if (i != position) {
                                isCheckedArray[i] = false;
                            }
                        }
                        contactAdapter.notifyDataSetChanged();
                    }

                }
            });
            // 群组中原来的成员一直设为选中状态
            if (exitingMembers.contains(easemobId)) {
                checkBox.setChecked(true);
                isCheckedArray[position] = true;
            } else {
                checkBox.setChecked(isCheckedArray[position]);
            }

            return view;
        }
    }

    public void back(View view) {
        ActivityUtil.finishActivity(baseContext);
    }
}
