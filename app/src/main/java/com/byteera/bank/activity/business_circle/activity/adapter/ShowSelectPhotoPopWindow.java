package com.byteera.bank.activity.business_circle.activity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.byteera.R;


public class ShowSelectPhotoPopWindow extends PopupWindow {
    private ButtonClickListener listener;

    public interface ButtonClickListener {
        void onCameraButtonClick();

        void onPhotoButtonClick();
    }

    public ShowSelectPhotoPopWindow(Activity activitys, ButtonClickListener cameraClickListener) {
        super(activitys);
        listener = cameraClickListener;
        Activity activity = activitys;

        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.item_popupwindows, null);
        LinearLayout ll_popup = (LinearLayout) contentView.findViewById(R.id.ll_popup);

        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.ShowSelectPopAnimation);
        this.setContentView(contentView);

        RelativeLayout parent = (RelativeLayout) contentView.findViewById(R.id.parent);
        Button bt1 = (Button) contentView.findViewById(R.id.item_popupwindows_camera);
        Button bt2 = (Button) contentView.findViewById(R.id.item_popupwindows_Photo);
        Button bt3 = (Button) contentView.findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                listener.onCameraButtonClick();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                listener.onPhotoButtonClick();
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
