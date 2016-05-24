package com.byteera.bank.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.byteera.R;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;

/** Created by bing on 2015/5/7. */
public class GetPhotoDialog extends AlertDialog {
    private Context mContext;

    public interface  OnCameraClikListener{
       void  onCamerClick();
    }
    public interface  OnPhotoClickListener{
        void onPhotoClick();
    }
    private OnCameraClikListener cameraClickListener;
    private OnPhotoClickListener photoClickListener;

    public void setCameraClickListener(OnCameraClikListener cameraClickListener) {
        this.cameraClickListener = cameraClickListener;
    }

    public void setPhotoClickListener(OnPhotoClickListener photoClickListener) {
        this.photoClickListener = photoClickListener;
    }

    public GetPhotoDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public GetPhotoDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_photo_dialog);
        WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
        attributes.width = UIUtils.dip2px(mContext, 300);
        this.getWindow().setAttributes(attributes);
        findViewById(R.id.ll_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraClickListener.onCamerClick();

            }
        });
        findViewById(R.id.ll_get_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从相册获取图片
                photoClickListener.onPhotoClick();
            }
        });
    }
}
