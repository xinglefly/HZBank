package com.byteera.bank.activity.contact.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.byteera.bank.activity.contact.ContactDetailActivity;
import com.byteera.bank.activity.contact.FriendNewRequest;
import com.byteera.bank.adapter.ContactAdapter;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.FriendRelationShip;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.Sidebar;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** 联系人列表页 */
public class FriendListFragment extends Fragment {

    protected static final String TAG = FriendListFragment.class.getSimpleName();
    @ViewInject(R.id.rel_addfriend) private RelativeLayout rel_addfriend;
    @ViewInject(R.id.unread_number) private TextView unread_number;

    private ContactAdapter adapter;
    private ListView listView;
    private Sidebar sidebar;

    private SwipeRefreshLayout mSwpeRefreshLayout;

    //透传消息
    private FriendRequestReceiver friendRequestReceiver = null;
    List<FriendRelationShip> msgShowLists = new ArrayList<>();


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.inject(this, getActivity());

        initView();

        logicListener();

        friendRequestReceiver = new FriendRequestReceiver();
        IntentFilter cmdFilter = new IntentFilter(Constants.CMD_BROADCAST_ADDFRIEND);
        getActivity().registerReceiver(friendRequestReceiver, cmdFilter);
    }

    public class FriendRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkNewRequest();
        }
    }

    public void updateUI() {
        msgShowLists.clear();
        unread_number.setVisibility(View.INVISIBLE);
    }

    private void initView() {
        listView = (ListView) getView().findViewById(R.id.list);
        mSwpeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_layout);
        sidebar = (Sidebar) getView().findViewById(R.id.sidebar);
        sidebar.setListView(listView);
    }

    @OnClick({R.id.rel_addfriend})
    public void onClickListener(View v) {
        switch (v.getId()) {
            case R.id.rel_addfriend:
                updateUI();
                ActivityUtil.startActivity(getActivity(), new Intent(getActivity(), FriendNewRequest.class));
                break;
        }
    }

    private void logicListener() {
        mSwpeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                refresh();
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView user_id = ViewHolder.get(view, R.id.tv_userid);
                Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                intent.putExtra("user_id", user_id.getText().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                getActivity().startActivity(intent);
            }
        });
    }

    // 刷新ui
    public void refresh() {
        getContactList();
    }

    /** 获取联系人列表，并过滤掉黑名单和排序 */
    private void getContactList() {
        MyApp.getInstance().executorService.execute(new Runnable() {
            @Override
            public void run() {
                getFriends();
            }
        });
    }

    private void getFriends() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", MyApp.getInstance().getToken());
        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.GET, Constants.BYTEERA_SERVICE + "friends?", params, new RequestCallBack<String>() {
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
                    String error_description = json.optString("error_description");
                    if ("0".equals(error)) {
                        //{"data": {"friends": ["55dd2ce81acfdc47280eb2fb", "55dd2cf91acfdc47280eb2fd"]}
                        JSONObject data = json.getJSONObject("data");
                        JSONArray friends = data.optJSONArray("friends");
                        final List<User> contactList = new ArrayList<>();
                        Constants.friendList.clear();
                        for (int i = 0; i < friends.length(); i++) {
                            String friend_userid = (String) friends.get(i);
                            User userFriend = DBManager.getInstance(getActivity()).getUserByUserId(friend_userid);
                            if (userFriend != null) {
                                contactList.add(userFriend);
                                if (!Constants.friendList.contains(userFriend.getUserId())) {
                                    Constants.friendList.add(friend_userid);
                                }
                            }
                        }
                        LogUtil.d("friends-->" + contactList.size());
                        // 排序
                        Collections.sort(contactList, new Comparator<User>() {
                            @Override
                            public int compare(User lhs, User rhs) {
                                return lhs.getPinYin().compareTo(rhs.getPinYin());
                            }
                        });


                        if (getActivity() != null) {
                            MyApp.getHandler().post(new Runnable() {
                                @Override
                                public void run() {

                                    if (adapter == null) {
                                        adapter = new ContactAdapter(getActivity(), R.layout.row_contact, contactList);
                                        listView.setAdapter(adapter);
                                    } else {
                                        adapter.clear();
                                        adapter.addAll(contactList);
                                    }

                                    adapter.notifyDataSetChanged();
                                    mSwpeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    LogUtil.e(e, "Parse get friends failed.");
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播
        if (friendRequestReceiver != null) {
            getActivity().unregisterReceiver(friendRequestReceiver);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }


    @Override
    public void onStart() {
        super.onStart();
        checkNewRequest();
    }

    private void checkNewRequest(){
        List<FriendRelationShip> friendRequests = MyApp.getInstance().getFriendRequests();
        if (friendRequests.size() > 0) {
            msgShowLists.addAll(friendRequests);

            unread_number.setVisibility(View.VISIBLE);
            unread_number.setText(friendRequests.size() + "");
        }

        MyApp.getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });

        MyApp.getInstance().clearFriendRequest();
    }
}
