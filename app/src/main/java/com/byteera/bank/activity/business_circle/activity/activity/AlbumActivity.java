package com.byteera.bank.activity.business_circle.activity.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.byteera.R;
import com.byteera.bank.activity.BaseFragmentActivity;
import com.byteera.bank.activity.business_circle.activity.adapter.AlbumGridViewAdapter;
import com.byteera.bank.activity.business_circle.activity.entity.ImageBucket;
import com.byteera.bank.activity.business_circle.activity.entity.ImageItem;
import com.byteera.bank.activity.business_circle.activity.util.AlbumHelper;
import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.hxlib.utils.Constants;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends BaseFragmentActivity {
    //gridView的adapter
    private AlbumGridViewAdapter gridImageAdapter;
    //完成按钮
    private Button okButton;
    // 预览按钮
    private Button preview;
    private ArrayList<ImageItem> dataList;
    public static List<ImageBucket> contentList;
    public static Bitmap bitmap;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_camera_album);
        //注册一个广播，这个广播主要是用于在GalleryActivity进行预览时，防止当所有图片都删除完后，再回到该页面时被取消选中的图片仍处于选中状态
        IntentFilter filter = new IntentFilter("data.broadcast.action");
        registerReceiver(broadcastReceiver, filter);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plugin_camera_no_pictures);
        init();
        initListener();
        //这个函数主要用来控制预览和完成按钮的状态
        isShowOkBt();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            gridImageAdapter.notifyDataSetChanged();
        }
    };

    // 预览按钮的监听
    private class PreviewListener implements OnClickListener {
        public void onClick(View v) {
            if (Bimp.tempSelectBitmap.size() > 0) {
                ActivityUtil.startActivity(AlbumActivity.this, new Intent(AlbumActivity.this, GalleryActivity.class));
            }
        }
    }

    // 完成按钮的监听
    private class AlbumSendListener implements OnClickListener {
        public void onClick(View v) {
            ActivityUtil.finishActivity(AlbumActivity.this);
        }
    }

    // 相册按钮监听
    private class BackListener implements OnClickListener {
        public void onClick(View v) {
            ActivityUtil.startActivity(AlbumActivity.this, new Intent(AlbumActivity.this, ImageFileActivity.class));
        }
    }

    // 取消按钮的监听
    private class CancelListener implements OnClickListener {
        public void onClick(View v) {
            Bimp.tempSelectBitmap.clear();
            ActivityUtil.finishActivity(AlbumActivity.this);
        }
    }


    // 初始化，给一些对象赋值
    private void init() {
        AlbumHelper helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        contentList = helper.getImagesBucketList(false);
        dataList = new ArrayList<ImageItem>();
        for (int i = 0; i < contentList.size(); i++) {
            dataList.addAll(contentList.get(i).imageList);
        }

        Button back = (Button) findViewById(R.id.back);
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new CancelListener());
        back.setOnClickListener(new BackListener());
        preview = (Button) findViewById(R.id.preview);
        preview.setOnClickListener(new PreviewListener());
        GridView gridView = (GridView) findViewById(R.id.myGrid);
        gridImageAdapter = new AlbumGridViewAdapter(this, dataList, Bimp.tempSelectBitmap);
        gridView.setAdapter(gridImageAdapter);
        TextView tv = (TextView) findViewById(R.id.myText);
        gridView.setEmptyView(tv);
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
    }

    private void initListener() {
        gridImageAdapter
                .setOnItemClickListener(new AlbumGridViewAdapter.OnItemClickListener() {
                    @Override public void onItemClick(final ToggleButton toggleButton, int position, boolean isChecked, Button chooseBt) {
                        if (Bimp.tempSelectBitmap.size() >= Constants.PHOTO_NUM) {
                            toggleButton.setChecked(false);
                            chooseBt.setVisibility(View.GONE);
                            if (!removeOneData(dataList.get(position))) {
                                ToastUtil.showToastText("超出可选图片张数");
                            }
                            return;
                        }
                        if (isChecked) {
                            chooseBt.setVisibility(View.VISIBLE);
                            Bimp.tempSelectBitmap.add(dataList.get(position));
                            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
                        } else {
                            Bimp.tempSelectBitmap.remove(dataList.get(position));
                            chooseBt.setVisibility(View.GONE);
                            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
                        }
                        isShowOkBt();
                    }
                });

        okButton.setOnClickListener(new AlbumSendListener());

    }

    private boolean removeOneData(ImageItem imageItem) {
        if (Bimp.tempSelectBitmap.contains(imageItem)) {
            Bimp.tempSelectBitmap.remove(imageItem);
            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
            return true;
        }
        return false;
    }

    public void isShowOkBt() {
        if (Bimp.tempSelectBitmap.size() > 0) {
            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
            preview.setPressed(true);
            okButton.setPressed(true);
            preview.setClickable(true);
            okButton.setClickable(true);
            okButton.setTextColor(Color.WHITE);
            preview.setTextColor(Color.WHITE);
        } else {
            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
            preview.setPressed(false);
            preview.setClickable(false);
            okButton.setPressed(false);
            okButton.setClickable(false);
            okButton.setTextColor(Color.parseColor("#E1E0DE"));
            preview.setTextColor(Color.parseColor("#E1E0DE"));
        }
    }


    @Override
    protected void onRestart() {
        isShowOkBt();
        super.onRestart();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
