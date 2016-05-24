package com.byteera.bank.activity.enterprise;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.ExpandGridView;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MettingContent extends BaseActivity {
    
    protected static final String TAG = MettingContent.class.getSimpleName();
    
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.gridview) ExpandGridView userGridview;


    private GridAdapter adapter;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.metting_content);

        ViewUtils.inject(this);

        initGridView();



        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override
            public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });

    }


    private void initGridView() {
        // 获取传过来的groupid
        List<String> huanxin = (List<String>) getIntent().getSerializableExtra("huanxin");

        mHeadView.setTitleName("参会人员("+huanxin.size()+")");

        UserDao userDao = new UserDao(baseContext);
        List<User> tempList = new ArrayList<>();
        for (int i = 0; i < huanxin.size(); i++) {
            String s = huanxin.get(i);
            User user = userDao.selectUser(s);
            tempList.add(user);
        }


        adapter = new GridAdapter(this, R.layout.grid, tempList);
        userGridview.setAdapter(adapter);
    }




    private class GridAdapter extends ArrayAdapter<User> {
        private int res;
        public boolean isInDeleteMode;
        private List<String> objects;

        public GridAdapter(Context context, int textViewResourceId, List<User> users) {
            super(context, textViewResourceId, users);
            res = textViewResourceId;
        }

        @Override public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(res, null);
            }

            ImageView ivHead = ViewHolder.get(convertView, R.id.iv_head);
            TextView tvName = ViewHolder.get(convertView, R.id.tv_name);
            ImageView badge_delete = ViewHolder.get(convertView, R.id.badge_delete);
            RelativeLayout rl = ViewHolder.get(convertView, R.id.rl);
            badge_delete.setVisibility(View.GONE);

            final User user = getItem(position);
            if (user != null) {
                if (user.getNickName() != null) {
                    tvName.setText(user.getNickName());
                }
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
            return super.getCount();
        }
    }
    





}
