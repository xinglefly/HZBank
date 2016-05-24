package com.byteera.bank.activity.enterprise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;

import com.byteera.bank.domain.MeetingContent;
import com.byteera.bank.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.utils.LoadingDialogShow;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeetingListFragment extends Fragment {

    protected static final String TAG = MeetingListFragment.class.getSimpleName();
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.chat_list) private ListView mListView;
    @ViewInject(R.id.ll_empty) private LinearLayout llEmpty;
    @ViewInject(R.id.swipe_layout) private SwipeRefreshLayout mSwipeLayout;

    private List<MeetingContent> meetingContentList;
    private ChatListAdapter adapter;
    private LoadingDialogShow dialog;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.metting_list, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.inject(this, getView());
        initView();
        initEvent();
    }

    protected void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MeetingContent meetingContent = meetingContentList.get(position);
                Intent intent = new Intent(getActivity(), MeetingCircleActivity.class);
                intent.putExtra("chat_group_name", meetingContent.getChat_group_name());
                intent.putExtra("meet_name", meetingContent.getMeet_name());
                intent.putExtra("huanxin", (Serializable) adapter.getItem(position).getList());
                intent.putExtra("meet_des", meetingContent.getMeet_desc());
                intent.putExtra("file_path", meetingContent.getFilePath());

                HXPreferenceUtils.getInstance().setTag(meetingContent.getMeet_circle_tag());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //toast消息提示
                Boolean isStart = true;
                //获取会议人员列表
                List<String> list = meetingContent.getList();
                for (int i = 0; i < list.size(); i++) {
                    String huanxinaccount = list.get(i);
                    if (huanxinaccount.equals(MyApp.getInstance().getUserName())) {
                        startActivity(intent);
                        isStart = false;
                        break;
                    }
                }
                if (isStart)
                    ToastUtil.showToastText("您没有权限查看此次会务");
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                String token = HXPreferenceUtils.getInstance().getToken();
                getDocumentList(token);
            }
        });
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(getActivity());
            }
        });
    }

    public void initView() {
        dialog = new LoadingDialogShow(getActivity());
        ViewUtils.inject(this, getView());
        String token = HXPreferenceUtils.getInstance().getToken();
        getDocumentList(token);
    }

    private void getDocumentList(final String token) {
        dialog.show();
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", token);
        MyhttpUtils.getInstance().sendAsync(HttpMethod.GET, Constants.BYTEERA_SERVICE + "meeting?", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                MyApp.getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToastText("获取数据失败，请稍后重试！");
                        mSwipeLayout.setRefreshing(false);
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    LogUtil.d("onSucc-->" + result);
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");

                    if ("0".equals(error)) {
                        JSONArray data = json.optJSONArray("data");
                        meetingContentList = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = (JSONObject) data.get(i);
                            String meet_name = obj.optString("meet_name");
                            String meet_datetime = obj.optString("meet_datetime");
                            String meet_desc = obj.optString("meet_desc");
                            String meet_id = obj.optString("meet_id");
                            String chat_group_name = obj.optString("chat_group_name");
                            String meet_circle_tag = obj.optString("meet_circle_tag");
                            String filePath = obj.optString("file_path");
                            int expired = obj.optInt("expired");

                            JSONArray members = obj.optJSONArray("members");
                            ArrayList<String> list = new ArrayList<>();
                            for (int j = 0; j < members.length(); j++) {
                                JSONObject user = (JSONObject) members.get(j);
                                String easemob_id = user.optString("huanxin");
                                String hxid = easemob_id.substring(0, easemob_id.lastIndexOf(":"));
                                list.add(hxid);
                            }
                            meetingContentList.add(new MeetingContent(meet_name, meet_datetime, meet_desc,
                                    meet_id, chat_group_name, list.size(), list, meet_circle_tag,
                                    filePath, expired));
                        }

                        MyApp.getMainThreadHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeLayout.setRefreshing(false);
                                dialog.dismiss();
                                adapter = new ChatListAdapter(meetingContentList);
                                mListView.setAdapter(adapter);
                                if (adapter.getCount() > 0) {
                                    llEmpty.setVisibility(View.GONE);
                                } else {
                                    llEmpty.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                    } else {
                        MyApp.getMainThreadHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToastText("获取数据失败，请稍后重试！");
                                mSwipeLayout.setRefreshing(false);
                                dialog.dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MyApp.getMainThreadHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToastText("获取数据失败，请稍后重试！");
                            mSwipeLayout.setRefreshing(false);
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private class ChatListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private List<MeetingContent> mettInfos;

        private ChatListAdapter(List<MeetingContent> mettInfos) {
            mInflater = LayoutInflater.from(getActivity());
            this.mettInfos = mettInfos;
        }

        @Override public int getCount() {
            return mettInfos.size();
        }

        @Override public MeetingContent getItem(int position) {
            return mettInfos.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.chatfriend_item, null);
            }
            RelativeLayout rel_chat = ViewHolder.get(convertView, R.id.rel_chat);
            ImageView img_chat = ViewHolder.get(convertView, R.id.img_chat);
            TextView tv_chattitle = ViewHolder.get(convertView, R.id.tv_chattitle);
            TextView tv_personsize = ViewHolder.get(convertView, R.id.tv_personsize);
            TextView tv_chattime = ViewHolder.get(convertView, R.id.tv_chattime);
            ImageView img_end = ViewHolder.get(convertView, R.id.img_end);
            MeetingContent mettInfo = mettInfos.get(position);
            tv_chattitle.setText(mettInfo.getMeet_name());
            tv_chattime.setText(mettInfo.getMeet_datetime());
            tv_personsize.setText(mettInfo.getMembers() + "人");
            if (mettInfo.getExpired() == 1) {
                img_end.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
}