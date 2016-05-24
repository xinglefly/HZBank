package com.byteera.bank.activity.business_circle.activity.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.business_circle.activity.adapter.AlbumGridViewAdapter;
import com.byteera.bank.activity.business_circle.activity.entity.ImageItem;
import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.hxlib.utils.Constants;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;

import java.util.ArrayList;


/**
 * 这个是显示一个文件夹里面的所有图片时的界面
 *
 * @author king
 * @version 2014年10月18日  下午11:49:10
 * @QQ:595163260
 */
public class ShowAllPhotoActivity extends BaseActivity {
    private AlbumGridViewAdapter gridImageAdapter;
    // 完成按钮
    private Button okButton;
    // 预览按钮
    private Button preview;
    private Intent intent;
    private Activity mContext;
    public static ArrayList<ImageItem> dataList = new ArrayList<ImageItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_camera_show_all_photo);
        mContext = this;
        Button back = (Button) findViewById(R.id.showallphoto_back);
        Button cancel = (Button) findViewById(R.id.showallphoto_cancel);
        preview = (Button) findViewById(R.id.showallphoto_preview);
        okButton = (Button) findViewById(R.id.showallphoto_ok_button);
        TextView headTitle = (TextView) findViewById(R.id.showallphoto_headtitle);
        this.intent = getIntent();
        String folderName = intent.getStringExtra("folderName");
        if (folderName.length() > 8) {
            folderName = folderName.substring(0, 9) + "...";
        }
        headTitle.setText(folderName);
        cancel.setOnClickListener(new CancelListener());
        back.setOnClickListener(new BackListener(intent));
        preview.setOnClickListener(new PreviewListener());
        init();
        initListener();
        isShowOkBt();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            gridImageAdapter.notifyDataSetChanged();
        }
    };

    private class PreviewListener implements OnClickListener {
        public void onClick(View v) {
            if (Bimp.tempSelectBitmap.size() > 0) {
                intent.putExtra("position", "2");
                intent.setClass(ShowAllPhotoActivity.this, GalleryActivity.class);
                ActivityUtil.startActivity(ShowAllPhotoActivity.this, intent);
            }
        }
    }

    private class BackListener implements OnClickListener {// 返回按钮监听
        Intent intent;

        public BackListener(Intent intent) {
            this.intent = intent;
        }

        public void onClick(View v) {
            intent.setClass(ShowAllPhotoActivity.this, ImageFileActivity.class);
            ActivityUtil.startActivity(ShowAllPhotoActivity.this,intent);
        }

    }

    private class CancelListener implements OnClickListener {// 取消按钮的监听

        public void onClick(View v) {
            //清空选择的图片
            Bimp.tempSelectBitmap.clear();
            intent.setClass(mContext, PublishDynamicActivity.class);
            ActivityUtil.startActivity(mContext, intent);
        }
    }

    private void init() {
        IntentFilter filter = new IntentFilter("data.broadcast.action");
        registerReceiver(broadcastReceiver, filter);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.showallphoto_progressbar);
        progressBar.setVisibility(View.GONE);
        GridView gridView = (GridView) findViewById(R.id.showallphoto_myGrid);
        gridImageAdapter = new AlbumGridViewAdapter(this, dataList,
                Bimp.tempSelectBitmap);
        gridView.setAdapter(gridImageAdapter);
        okButton = (Button) findViewById(R.id.showallphoto_ok_button);
    }

    private void initListener() {
        gridImageAdapter
                .setOnItemClickListener(new AlbumGridViewAdapter.OnItemClickListener() {
                    public void onItemClick(final ToggleButton toggleButton,
                                            int position, boolean isChecked,
                                            Button button) {
                        if (Bimp.tempSelectBitmap.size() >= Constants.PHOTO_NUM && isChecked) {
                            button.setVisibility(View.GONE);
                            toggleButton.setChecked(false);
                            ToastUtil.showToastText("超出可选图片张数");
                            return;
                        }

                        if (isChecked) {
                            button.setVisibility(View.VISIBLE);
                            Bimp.tempSelectBitmap.add(dataList.get(position));
                            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
                        } else {
                            button.setVisibility(View.GONE);
                            Bimp.tempSelectBitmap.remove(dataList.get(position));
                            okButton.setText("完成" + "(" + Bimp.tempSelectBitmap.size() + "/" + Constants.PHOTO_NUM + ")");
                        }
                        isShowOkBt();
                    }
                });
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                okButton.setClickable(false);
//				if (PublicWay.photoService != null) {
//					PublicWay.selectedDataList.addAll(Bimp.tempSelectBitmap);
//					Bimp.tempSelectBitmap.clear();
//					PublicWay.photoService.onActivityResult(0, -2,
//							intent);
//				}
                intent.setClass(mContext, PublishDynamicActivity.class);
                ActivityUtil.startActivity(mContext, intent);
                // Intent intent = new Intent();
                // Bundle bundle = new Bundle();
                // bundle.putStringArrayList("selectedDataList",
                // selectedDataList);
                // intent.putExtras(bundle);
                // intent.setClass(ShowAllPhotoActivity.this, UploadPhoto.class);
                // startActivity(intent);
                ActivityUtil.finishActivity(baseContext);

            }
        });

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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtil.finishActivity(baseContext);
            intent.setClass(ShowAllPhotoActivity.this, ImageFileActivity.class);
            ActivityUtil.startActivity(ShowAllPhotoActivity.this,intent);
        }
        return false;
    }

    @Override
    protected void onRestart() {
        isShowOkBt();
        super.onRestart();
    }

}
