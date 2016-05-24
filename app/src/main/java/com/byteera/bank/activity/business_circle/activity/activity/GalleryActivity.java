package com.byteera.bank.activity.business_circle.activity.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.byteera.R;
import com.byteera.bank.activity.BaseFragmentActivity;
import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.bank.activity.business_circle.activity.widget.photoview.PhotoView;
import com.byteera.bank.activity.business_circle.activity.widget.photoview.ViewPagerFixed;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.ActivityUtil;

import java.util.ArrayList;

public class GalleryActivity extends BaseFragmentActivity {
    // 发送按钮
    private Button send_bt;
    //当前的位置
    private int location = 0;

    private ArrayList<View> listViews = null;
    private ViewPagerFixed pager;
    private MyPageAdapter adapter;

    private Activity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_camera_gallery);
        mContext = this;
        send_bt = (Button) findViewById(R.id.send_button);
        Button del_bt = (Button) findViewById(R.id.gallery_del);
        send_bt.setOnClickListener(new GallerySendListener());
        del_bt.setOnClickListener(new DelListener());
        Intent intent = getIntent();
        isShowOkBt();
        // 为发送按钮设置文字
        pager = (ViewPagerFixed) findViewById(R.id.gallery01);
        pager.setOnPageChangeListener(pageChangeListener);
        for (int i = 0; i < Bimp.tempSelectBitmap.size(); i++) {
            initListViews(Bimp.tempSelectBitmap.get(i).getBitmap());
        }

        adapter = new MyPageAdapter(listViews);
        pager.setAdapter(adapter);
        pager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.ui_10_dip));
        int id = intent.getIntExtra("ID", 0);
        pager.setCurrentItem(id);
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {
            location = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void initListViews(Bitmap bm) {
        if (listViews == null)
            listViews = new ArrayList<View>();
        PhotoView img = new PhotoView(this);
        img.setBackgroundColor(0xff000000);
        img.setImageBitmap(bm);
        img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        listViews.add(img);
    }

    // 删除按钮添加的监听器
    private class DelListener implements OnClickListener {
        public void onClick(View v) {
            if (listViews.size() == 1) {
                Bimp.tempSelectBitmap.clear();
                Bimp.max = 0;
                send_bt.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
                Intent intent = new Intent("data.broadcast.action");
                sendBroadcast(intent);
              ActivityUtil.finishActivity(GalleryActivity.this);
            } else {
                Bimp.tempSelectBitmap.remove(location);
                Bimp.max--;
                pager.removeAllViews();
                listViews.remove(location);
                adapter.setListViews(listViews);
                send_bt.setText("完成"  + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
                adapter.notifyDataSetChanged();
            }
        }
    }

    // 完成按钮的监听
    private class GallerySendListener implements OnClickListener {
        public void onClick(View v) {
            ActivityUtil.finishActivity(mContext);
        }
    }

    public void isShowOkBt() {
        if (Bimp.tempSelectBitmap.size() > 0) {
            send_bt.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
            send_bt.setPressed(true);
            send_bt.setClickable(true);
            send_bt.setTextColor(Color.WHITE);
        } else {
            send_bt.setPressed(false);
            send_bt.setClickable(false);
            send_bt.setTextColor(Color.parseColor("#E1E0DE"));
        }
    }

    class MyPageAdapter extends PagerAdapter {
        private ArrayList<View> listViews;
        private int size;
        public MyPageAdapter(ArrayList<View> listViews) {
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public void setListViews(ArrayList<View> listViews) {
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public int getCount() {
            return size;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPagerFixed) arg0).removeView(listViews.get(arg1 % size));
        }

        public void finishUpdate(View arg0) {
        }

        public Object instantiateItem(View arg0, int arg1) {
            try {
                ((ViewPagerFixed) arg0).addView(listViews.get(arg1 % size), 0);

            } catch (Exception e) {
            }
            return listViews.get(arg1 % size);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }
}
