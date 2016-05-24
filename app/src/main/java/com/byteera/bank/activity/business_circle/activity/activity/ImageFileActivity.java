package com.byteera.bank.activity.business_circle.activity.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.business_circle.activity.adapter.FolderAdapter;
import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.hxlib.utils.ActivityUtil;


public class ImageFileActivity extends BaseActivity {
    private Context mContext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_camera_image_file);
        mContext = this;
        Button bt_cancel = (Button) findViewById(R.id.cancel);
        bt_cancel.setOnClickListener(new CancelListener());
        GridView gridView = (GridView) findViewById(R.id.fileGridView);
        TextView textView = (TextView) findViewById(R.id.headerTitle);
        textView.setText("选择相册");
        FolderAdapter folderAdapter = new FolderAdapter(this);
        gridView.setAdapter(folderAdapter);
    }

    private class CancelListener implements OnClickListener {// 取消按钮的监听
        public void onClick(View v) {
            //清空选择的图片
            Bimp.tempSelectBitmap.clear();
            Intent intent = new Intent(mContext, PublishDynamicActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityUtil.startActivity(ImageFileActivity.this, intent);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(mContext, PublishDynamicActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ActivityUtil.startActivity(ImageFileActivity.this, intent);
            ActivityUtil.finishActivity(baseContext);
        }
        return true;
    }

}
