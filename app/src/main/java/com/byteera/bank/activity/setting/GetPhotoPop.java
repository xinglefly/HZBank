package com.byteera.bank.activity.setting;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.byteera.R;


/** Created by lieeber on 15/8/7. */
public class GetPhotoPop {
    private PopupWindow takePhotoPop;

    public interface SelectTakePhotoListener {
        void takePhtoto();
    }

    public interface SelectPictureListener {
        void selectPicture();
    }

    private SelectTakePhotoListener selectTakePhotoListener;
    private SelectPictureListener selectPictureListener;

    public void setSelectTakePhotoListener(SelectTakePhotoListener selectTakePhotoListener) {
        this.selectTakePhotoListener = selectTakePhotoListener;
    }

    public void setSelectPictureListener(SelectPictureListener selectPictureListener) {
        this.selectPictureListener = selectPictureListener;
    }


    public void showAtLocation(View locationView, final Activity activity) {
        takePhotoPop.setFocusable(true);
        takePhotoPop.setOutsideTouchable(true);
        takePhotoPop.setBackgroundDrawable(new BitmapDrawable());
        takePhotoPop.setAnimationStyle(R.style.modifyUserInfoPop);
        takePhotoPop.update();
        takePhotoPop.showAtLocation(locationView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        final WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0.5f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animator) {
                float currentValue = (float) animator.getAnimatedValue();
                params.alpha = currentValue;
                activity.getWindow().setAttributes(params);
            }
        });

        valueAnimator.setDuration(200).start();
        takePhotoPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override public void onDismiss() {
                final WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.5f, 1);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animator) {
                        float currentValue = (float) animator.getAnimatedValue();
                        params.alpha = currentValue;
                        activity.getWindow().setAttributes(params);
                    }
                });
                valueAnimator.setDuration(200).start();
            }
        });
    }

    public GetPhotoPop(Activity mContext) {
        View view = mContext.getLayoutInflater().inflate(R.layout.get_photo_pop, null);
        takePhotoPop = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView tvNan = (TextView) view.findViewById(R.id.tv_nan);
        final TextView tvNv = (TextView) view.findViewById(R.id.tv_nv);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                takePhotoPop.dismiss();
            }
        });
        tvNan.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                takePhotoPop.dismiss();
                if (selectTakePhotoListener != null) {
                    selectTakePhotoListener.takePhtoto();
                }

            }
        });

        tvNv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                takePhotoPop.dismiss();
                if (selectPictureListener != null) {
                    selectPictureListener.selectPicture();
                }

            }
        });
    }
}
