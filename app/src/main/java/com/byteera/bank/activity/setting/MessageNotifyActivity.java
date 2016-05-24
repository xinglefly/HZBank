package com.byteera.bank.activity.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.WebViewActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.Announcement;
import com.byteera.bank.utils.StringUtil;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

public class MessageNotifyActivity extends BaseActivity{

    @ViewInject(R.id.head_view) private HeadViewMain headView;
    @ViewInject(R.id.lv_notify) private ListView lv_notify;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.row_received_gonggao);

        ViewUtils.inject(baseContext);

        List<Announcement> announcementList = DBManager.getInstance(baseContext)
                .getAnnouncementList();
        MyAdapter myAdapter = new MyAdapter(baseContext, announcementList);
        lv_notify.setAdapter(myAdapter);
        Constants.isRecieveMsgNotify = true;

        headView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override
            public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        private Context context;
        private List<Announcement> announcementList;

        MyAdapter(Context context,List<Announcement> announcementList) {
            this.context = context;
            this.announcementList = announcementList;
        }

        @Override
        public int getCount() {
            return announcementList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {convertView = LayoutInflater.from(context) .inflate(R.layout.msgnotify, parent, false); }

            TextView tv_title = ViewHolder.get(convertView, R.id.tv_title);
            TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
            Button btn_more = ViewHolder.get(convertView, R.id.more);

            Announcement msg = announcementList.get(position);
            tv_title.setText(msg.getTitle());
            tv_time.setText(StringUtil.checkTime(msg.getTime() * 1000));

            final String content = msg.getContent();

            btn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MessageNotifyActivity.this, WebViewActivity.class);
                    intent.putExtra("content", content);
                    ActivityUtil.startActivity(baseContext, intent);
                }
            });

            return convertView;
        }
    }
}
