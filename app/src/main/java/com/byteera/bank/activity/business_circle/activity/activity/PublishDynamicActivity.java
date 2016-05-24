package com.byteera.bank.activity.business_circle.activity.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.business_circle.activity.adapter.PhotoGridAdapter;
import com.byteera.bank.activity.business_circle.activity.adapter.ShowSelectPhotoPopWindow;
import com.byteera.bank.activity.business_circle.activity.entity.ImageItem;
import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;
import com.byteera.bank.activity.business_circle.activity.widget.NoScrollGridView;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PublishDynamicActivity extends BaseActivity implements PublishDynamicActivityIV {
    private PhotoGridAdapter adapter;
    private ShowSelectPhotoPopWindow pop;

    private PhotoGetBroadcastReceiver mReceiver;

    @ViewInject(R.id.noScrollgridview) private NoScrollGridView noScrollgridview;
    @ViewInject(R.id.et_comment) private EditText etComment;
    @ViewInject(R.id.ll_parent) private View popParent;
    @ViewInject(R.id.activity_selectimg_send) private View tvSend;

    private PublishDynamicActivityPre publishDynamicActivityPre;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.public_dynamic_activity);
        ViewUtils.inject(this);
        publishDynamicActivityPre = new PublishDynamicActivityPre(this);
        initView();
        initEvent();
    }

    private void initEvent() {
        noScrollgridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == Bimp.tempSelectBitmap.size()) {
                    pop.showAtLocation(popParent, Gravity.BOTTOM, 0, 0);
                    UIUtils.hideKeyboard(baseContext);
                } else {
                    publishDynamicActivityPre.intentToGallery(position);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //清空被选中的图片
        publishDynamicActivityPre.clearPhotos();
    }

    public void initView() {
        registReceiver();
        pop = new ShowSelectPhotoPopWindow(baseContext, new ShowSelectPhotoPopWindow.ButtonClickListener() {
            @Override public void onCameraButtonClick() {
                publishDynamicActivityPre.photo();  //拍照
            }

            @Override public void onPhotoButtonClick() {
                ActivityUtil.startActivity(baseContext, new Intent(baseContext, AlbumActivity.class));  //选照片
            }
        });
        adapter = new PhotoGridAdapter(this);
        adapter.update();
        noScrollgridview.setAdapter(adapter);
    }

    @OnClick({R.id.activity_selectimg_send, R.id.tv_cancel})
    public void clickListener(View view) {
        switch (view.getId()) {
            case R.id.activity_selectimg_send:
                if (!TextUtils.isEmpty(etComment.getText().toString().trim()) || Bimp.tempSelectBitmap.size() != 0) {
                    publishDynamicActivityPre.pushDynamic();
                } else {
                    ToastUtil.showToastText("发布动态内容不能为空");
                }
                break;
            case R.id.tv_cancel:
                UIUtils.hideKeyboard(baseContext);
                ActivityUtil.finishActivity(this);
                publishDynamicActivityPre.clearPhotos();
                break;
        }
    }

    private void registReceiver() {
        mReceiver = new PhotoGetBroadcastReceiver();
        IntentFilter filter = new IntentFilter("update_photo");
        this.registerReceiver(mReceiver, filter);
    }

    protected void onRestart() {
        adapter.update();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PublishDynamicActivityPre.TAKE_PICTURE:
                if (Bimp.tempSelectBitmap.size() < 9 && resultCode == RESULT_OK) {
                    //将保存在本地的图片取出并缩小后显示在界面上
                    //  Bitmap camorabitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/workupload.jpg");
//                    String fileName = String.valueOf(System.currentTimeMillis());
//                    Bitmap bm = (Bitmap) data.getExtras().get("data");
//                    Bitmap bitmap = ImageUtils.compressImage(bm);   //图片压缩
//                    String filePath = FileUtils.saveBitmap(bitmap, fileName);
//                    ImageItem takePhoto = new ImageItem();
//                    takePhoto.setBitmap(bitmap);
//                    takePhoto.setImagePath(filePath);
//                    Bimp.tempSelectBitmap.add(takePhoto);

                    Bitmap bm = (Bitmap) data.getExtras().get("data");
                    ImageItem takePhoto = new ImageItem();
                    takePhoto.setBitmap(bm);
                    Bimp.tempSelectBitmap.add(takePhoto);
                }
                break;
        }
    }

    @Override public Activity getContext() {
        return this;
    }

    @Override public void setSendEnabled(boolean b) {
        tvSend.setEnabled(b);
    }

    @Override public String getContent() {
        return etComment.getText().toString();
    }

    @Override public void showPushDialog() {
        dialog.setMessage("发布中...");
        dialog.show();
    }

    @Override public void showPushSuccessDialog() {
        dialog.setResultStatusDrawable(true, "发布成功");
        Bimp.tempSelectBitmap.clear();
        setResult(RESULT_OK);
        ActivityUtil.finishActivity(baseContext);
    }

    @Override public void showPushFialeDialog() {
        dialog.setResultStatusDrawable(false, "发布失败");
    }

    @Override public void hidePushDialog() {
        dialog.dismiss();
    }

    public class PhotoGetBroadcastReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (Bimp.max == Bimp.tempSelectBitmap.size()) {
                adapter.notifyDataSetChanged();
            } else {
                Bimp.max += 1;
                adapter.notifyDataSetChanged();
            }
        }
    }
}
