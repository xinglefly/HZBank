package com.byteera.bank.activity.enterprise;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.activity.NewMeetingGroupActivity;
import com.byteera.bank.activity.business_circle.activity.activity.ImagePagerActivity;
import com.byteera.bank.activity.business_circle.activity.activity.PublishDynamicActivity;
import com.byteera.bank.activity.business_circle.activity.showCircle.view.DynamicItemGridAdapter;
import com.byteera.bank.activity.business_circle.activity.util.ParseJsonStr;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;
import com.byteera.bank.activity.business_circle.activity.widget.NoScrollGridView;
import com.byteera.bank.activity.business_circle.activity.widget.ZhiCaiLRefreshListView;
import com.byteera.bank.activity.contact.ContactDetailActivity;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.ChatContent;
import com.byteera.bank.domain.User;
import com.byteera.bank.domain.VisitorContent;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.StringUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.CommentView;
import com.byteera.bank.widget.ExpandGridView;
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
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeetingCircleActivity extends BaseActivity {

    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.ll_view) private LinearLayout ll_view;
    @ViewInject(R.id.comment_view) private CommentView mCommentView;
    @ViewInject(R.id.lv_chat) private ZhiCaiLRefreshListView mListView;
    @ViewInject(R.id.ll_empty) private LinearLayout llEmpty;

    private ChatAdapter mAdapter;
    private String lastChatId;
    private float downY;
    private String meet_name;
    private String meet_des;
    private String meetingFilePath;
    private List<String> huanxins;
    private PopupWindow popupWindow;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.metting_circle);
        ViewUtils.inject(this);
        initView();
        getChatContent(true);
    }

    private void initView() {
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });
        meet_name = getIntent().getStringExtra("meet_name");
        meet_des = getIntent().getStringExtra("meet_des");
        meetingFilePath = getIntent().getStringExtra("file_path");
        huanxins = (List<String>) getIntent().getSerializableExtra("huanxin");
        mHeadView.setTitleName(meet_name);
        mAdapter = new ChatAdapter();
        mListView.getListView().setAdapter(mAdapter);

        mListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChatContent(true);
            }
        });

        mListView.setLoadMoreDataListener(new ZhiCaiLRefreshListView.LoadMoreDataListener() {
            @Override
            public void loadMore() {
                getChatContent(false);
            }
        });
        mCommentView.setOnCommentFinishListener(new CommentView.OnCommentFinishListener() {
            @Override
            public void commentFinish(VisitorContent comment, int position) {
                mAdapter.addComment(comment, position);
            }
        });
    }

    @OnClick({R.id.rel_discussion, R.id.rl_discuss_group, R.id.rl_metting_content, R.id.rl_document, R.id.rl_dynamic})
    public void clickImg(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.rl_discuss_group://创建讨论组
                intent = new Intent(baseContext, NewMeetingGroupActivity.class);
                if (huanxins != null) {
                    intent.putExtra("huanxin", (Serializable) huanxins);
                }
                ActivityUtil.startActivityForResult(baseContext, intent, 0);
                break;
            case R.id.rl_metting_content://会议内容
                showPopuoWindow();
                break;
            case R.id.rl_document://文档
                intent = new Intent(this, MeetingDocument.class);
                intent.putExtra("file_path", meetingFilePath);
                ActivityUtil.startActivity(baseContext, intent);
                break;
            case R.id.rl_dynamic://发表意见
                intent = new Intent(this, PublishDynamicActivity.class);
                ActivityUtil.startActivityForResult(baseContext, intent, 0);
                break;

            case R.id.rel_discussion:
                // 进入聊天页面
                String huanXinGroupId = HXPreferenceUtils.getInstance().getHuanXinGroupId();
                if (huanXinGroupId.equals("")) {
                    String chat_group_name = getIntent().getStringExtra("chat_group_name");
                    HXPreferenceUtils.getInstance().setHuanXinGroupId(chat_group_name);
                }
                intent = new Intent(baseContext, ChatActivity.class);
                intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                intent.putExtra("groupId", HXPreferenceUtils.getInstance().getHuanXinGroupId());
                intent.putExtra("meet_name", meet_name);
                ActivityUtil.startActivity(baseContext, intent);
                break;
        }
    }


    private GridAdapter adapter;
    ExpandGridView userGridview;
    ImageView img_back;
    /**popwindow显示**/
    public void showPopuoWindow() {
        if (popupWindow == null) {
            View view = getLayoutInflater().inflate(R.layout.pop_metting_content, null);
            popupWindow = new PopupWindow(view, AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout rel_pop = (RelativeLayout) view.findViewById(R.id.rel_pop);
            TextView tv_description = (TextView) view.findViewById(R.id.tv_description);
            img_back = (ImageView)  view.findViewById(R.id.img_back);
            userGridview = (ExpandGridView)view.findViewById(R.id.gridview);

            tv_description.setText(meet_des);
//            rel_pop.getBackground().setAlpha(93);

            initGridView();

            if (huanxins.size()>12){
                img_back.setVisibility(View.VISIBLE);
                img_back.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(baseContext, MettingContent.class);
                        if (huanxins != null) {
                            intent.putExtra("huanxin", (Serializable) huanxins);
                        }
                        ActivityUtil.startActivity(baseContext, intent);
                    }
                });
            }



        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.update();
        popupWindow.showAsDropDown(ll_view);
    }


    private void initGridView() {
        UserDao userDao = new UserDao(baseContext);
        List<User> tempList = new ArrayList<>();
        for (int i = 0; i < huanxins.size(); i++) {
            String s = huanxins.get(i);
            User user = userDao.selectUser(s);
            tempList.add(user);
        }

        adapter = new GridAdapter(this, R.layout.grid_mettingcontent, tempList);
        userGridview.setAdapter(adapter);
    }

    public void popInVisible(){
        if (popupWindow!=null){
            popupWindow.dismiss();
        }
    }

    private void getChatContent(final boolean isFirst) {
        if (isFirst) {
            lastChatId = null;
        }
        String token = HXPreferenceUtils.getInstance().getToken();
        String tag = HXPreferenceUtils.getInstance().getTag();
        RequestParams params = new RequestParams();
        if (TextUtils.isEmpty(lastChatId)) {
            params.addQueryStringParameter("last_fc_id", "never");
        } else {
            params.addQueryStringParameter("last_fc_id", lastChatId);
        }
        params.addQueryStringParameter("count", String.valueOf(20));
        params.addQueryStringParameter("access_token", token);
        params.addQueryStringParameter("tag", !tag.equals("") ? tag : "");
        MyhttpUtils.getInstance().loadData(HttpMethod.GET, Constants.BYTEERA_SERVICE + "fc", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException error, String msg) {
                ToastUtil.showToastText("数据加载失败");
                mListView.loadComplete();
                mListView.refreshFinish();
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                final String result = resInfo.result;

                MyApp.getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            LogUtil.d(result);

                            List<ChatContent> listChatContents = ParseJsonStr.parseChatJson(result);

                            if (listChatContents.size() > 0) {
                                lastChatId = listChatContents.get(listChatContents.size() - 1).getChat_id();
                                if (isFirst) {
                                    mAdapter.setData(listChatContents);
                                } else {
                                    mAdapter.addAllItem(listChatContents);
                                }
                            }
                            mListView.loadComplete();
                            mListView.refreshFinish();
                            if (mAdapter.getCount() > 0) {
                                llEmpty.setVisibility(View.GONE);
                                mListView.setVisibility(View.VISIBLE);
                            } else {
                                mListView.setVisibility(View.GONE);
                                llEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                        catch (Exception ex)
                        {
                            LogUtil.e(ex, "parse chat content exception");
                        }
                    }
                });
            }
        });
    }

    private class ChatAdapter extends BaseAdapter {

        private List<ChatContent> listChatContents;

        public ChatAdapter() {
            this.listChatContents = new ArrayList<>();
        }

        public void setData(List<ChatContent> mList) {
            this.listChatContents = mList;
            notifyDataSetInvalidated();
        }

        public void addAllItem(List<ChatContent> mList) {
            this.listChatContents.addAll(mList);
            notifyDataSetChanged();
        }

        @Override public int getCount() {
            return listChatContents.size();
        }

        @Override public Object getItem(int position) {
            return listChatContents.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.dynamic_item, null);
            }
            ImageView img_title = ViewHolder.get(convertView, R.id.img_title);
            TextView tv_username = ViewHolder.get(convertView, R.id.tv_username);
            TextView tv_content = ViewHolder.get(convertView, R.id.tv_content);
            NoScrollGridView gv_gridview = ViewHolder.get(convertView, R.id.gv_gridview);
            TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
            TextView tvRevert = ViewHolder.get(convertView, R.id.img_revert);
            ListView lv_revert = ViewHolder.get(convertView, R.id.lv_revert);

            final ChatContent chatContent = listChatContents.get(position);
            tv_username.setText(chatContent.getName());
            tv_content.setText(chatContent.getContent());
            if(!StringUtils.isEmpty(chatContent.getHead_photo()))
                ImageLoader.getInstance().displayImage(chatContent.getHead_photo(), img_title);

            img_title.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MeetingCircleActivity.this, ContactDetailActivity.class);
                    intent.putExtra("user_id", chatContent.getPublisher());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            });

            String publish_time = chatContent.getPublish_time();
            Long time = Long.parseLong(publish_time) * 1000;
            tv_time.setText(StringUtil.checkTime(time));
            tvRevert.setText(chatContent.getListVisitorContents().size() + "");
            //imgLists
            if (chatContent.getImgLists().size() > 0) {
                gv_gridview.setVisibility(View.VISIBLE);
                LogUtil.d("ChatContent:%s, ImageList count: %d", chatContent.getContent(),
                        chatContent.getImgLists().size());
                gv_gridview.setNumColumns(3);
                gv_gridview.setAdapter(new DynamicItemGridAdapter(baseContext, 3,
                        chatContent.getImgLists()));
                gv_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override public void onItemClick(AdapterView<?> parent, View view,
                                                      int position, long id) {
                        imageBrower(position, chatContent.getImgLists());
                    }
                });
            } else {
                gv_gridview.setVisibility(View.GONE);
            }

            tvRevert.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View arg0) {
                    String token = HXPreferenceUtils.getInstance().getToken();
                    mCommentView.setVisibility(View.VISIBLE);
                    mCommentView.setComment(chatContent.getChat_id(), token, position, chatContent.getName());
                    mCommentView.getFocus();
                }
            });
            List<VisitorContent> listVisitorContents = chatContent.getListVisitorContents();
            if (listVisitorContents.size() > 0) {
                lv_revert.setVisibility(View.VISIBLE);
                lv_revert.setAdapter(new RevertAdapter(listVisitorContents));
            } else {
                lv_revert.setVisibility(View.GONE);
            }
            return convertView;
        }

        private void imageBrower(int position, List<String> imgList) {
            Intent intent = new Intent(baseContext, ImagePagerActivity.class);
            String images[] = new String[imgList.size()];
            for (int i = 0; i < imgList.size(); i++) {
                images[i] = imgList.get(i);
            }
            intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, images);
            intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
            ActivityUtil.startActivity(baseContext, intent);
        }

        public void addComment(VisitorContent comment, int position) {
            ChatContent chatContent = listChatContents.get(position);
            chatContent.getListVisitorContents().add(comment);
            mCommentView.quitFocus();
            this.notifyDataSetChanged();
        }
    }

    private class RevertAdapter extends BaseAdapter {
        List<VisitorContent> listVisitorContents;

        public RevertAdapter(List<VisitorContent> listVisitorContents) {
            this.listVisitorContents = listVisitorContents;
        }

        @Override
        public int getCount() {
            return listVisitorContents.size();
        }

        @Override
        public Object getItem(int position) {
            return listVisitorContents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.revertitem, null);
            }
            TextView tv_visitor = ViewHolder.get(convertView, R.id.tv_visitor);
            TextView tv_visitor_content = ViewHolder.get(convertView, R.id.tv_visitor_content);

            VisitorContent visitorContent = listVisitorContents.get(position);
            tv_visitor.setText(visitorContent.getName());
            tv_visitor_content.setText(visitorContent.getContent());
            return convertView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getChatContent(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float dy = Math.abs(y - downY);
                if (!CommentView.keyBoardIsHide && dy > 5) {
                    UIUtils.hideKeyboard(baseContext);
                    mCommentView.quitFocus();
                }
        }
        return super.dispatchTouchEvent(ev);
    }


    private class GridAdapter extends ArrayAdapter<User> {
        private int res;
        public boolean isInDeleteMode;
        private List<String> objects;
        private List<User> users;

        public GridAdapter(Context context, int textViewResourceId, List<User> users) {
            super(context, textViewResourceId, users);
            res = textViewResourceId;
            this.users = users;
        }

        @Override public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(res, null);
            }

            ImageView ivHead = ViewHolder.get(convertView, R.id.iv_head);
            TextView tvName = ViewHolder.get(convertView, R.id.tv_name);

            final User user = getItem(position);
            if (user != null) {
                if (user.getNickName() != null) {
                    tvName.setText(user.getNickName());
                }

                ivHead.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MeetingCircleActivity.this, ContactDetailActivity.class);
                        intent.putExtra("user_id", user.getUserId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                });

                if(!StringUtils.isEmpty(user.getAvatar()))
                    ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + user.getAvatar(), ivHead);
                else
                    ImageLoader.getInstance().displayImage(null, ivHead);
            } else {
                ImageLoader.getInstance().displayImage(null, ivHead);
            }
            return convertView;
        }

        @Override public int getCount() {
            return users.size()>=12?12: users.size();
        }
    }
}
