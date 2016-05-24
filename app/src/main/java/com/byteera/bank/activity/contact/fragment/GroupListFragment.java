package com.byteera.bank.activity.contact.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.adapter.GroupAdapter2;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.EasemobUtil;
import com.byteera.bank.utils.LogUtil;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends Fragment implements GroupChangeListener {

    public GroupAdapter2 groupAdapter;
    protected ListView groupListView;
    protected List<EMGroup> grouplist;
    protected GroupListFragmentListener listener;
    public static GroupChangeListener changeListener;

    /** 整个标题栏，需要显示标题栏时可以设为visibility */
    protected RelativeLayout titleLayout;
    private SwipeRefreshLayout mSwpeRefreshLayout;

    /** 标题内容 */
    protected TextView title;

    public GroupListFragment() {

    }

    public GroupListFragment(List<EMGroup> grouplist, GroupListFragmentListener listener) {
        this.grouplist = grouplist;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.oldfragment_groups, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        changeListener = this;
        titleLayout = (RelativeLayout) getView().findViewById(R.id.title_container);
        title = (TextView) getView().findViewById(R.id.title);
        groupListView = (ListView) getView().findViewById(R.id.list);
        groupListView.setOnItemClickListener(new OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onListItemClickListener(position);
                }
            }
        });
        mSwpeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_layout);
        mSwpeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }


    // 刷新ui
    public void refresh() {
        MyApp.getInstance().executorService.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    final List<EMGroup> emGroupList = EasemobUtil
                            .getWorkingGroupWithFullInfo(true, true);

                    MyApp.getMainThreadHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.d("[GroupListFragment]Group list count: %d", emGroupList.size());
                            groupChanged(emGroupList);
                            mSwpeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
                catch (Exception e)
                {
                    LogUtil.e(e, "Refresh group list exception");
                }

            }
        });
    }


    public interface GroupListFragmentListener {
        void onListItemClickListener(int position);
    }

    @Override public void groupChanged(List<EMGroup> grouplist) {
        List<List<User>> groups = new ArrayList<>();
        UserDao userDao = new UserDao(getActivity());
        for (int i = 0; i < grouplist.size(); i++) {
            EMGroup emGroup = grouplist.get(i);
            List<User> tempList = new ArrayList<>();
            for (int j = 0; j < emGroup.getMembers().size(); j++) {
                String s = emGroup.getMembers().get(j);
                User user = userDao.selectUser(s);
                tempList.add(user);
            }
            groups.add(tempList);
        }
        groupAdapter = new GroupAdapter2(getActivity(), 1, groups,grouplist);
        groupListView.setAdapter(groupAdapter);
    }

    @Override public void onResume() {
        super.onResume();
        List<EMGroup> groupList = EasemobUtil.getWorkingGroupWithFullInfo(false, true);
        groupChanged(groupList);
    }
}


