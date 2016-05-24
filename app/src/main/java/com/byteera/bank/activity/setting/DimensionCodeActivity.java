package com.byteera.bank.activity.setting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;
import com.byteera.bank.domain.User;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Hashtable;

/** Created by lieeber on 15/8/23. */
public class DimensionCodeActivity extends BaseActivity {
    private int QR_HEIGHT;
    private int QR_WIDTH;
    @ViewInject(R.id.iv_code) private ImageView ivCode;
    @ViewInject(R.id.iv_head) private ImageView ivHead;
    @ViewInject(R.id.tv_name) private TextView tvName;
    @ViewInject(R.id.tv_depart) private TextView tvDepart;
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    private User my;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.dimension_code);
        ViewUtils.inject(this);
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override
            public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });
        QR_HEIGHT = UIUtils.dip2px(this, 300);
        QR_WIDTH = UIUtils.dip2px(this, 300);
        my = HXPreferenceUtils.getInstance().getUserInfo();
        ImageLoader.getInstance().displayImage(my.getAvatar(), ivHead);
        tvName.setText(my.getNickName());
        tvDepart.setText(my.getDepart());
        createImage();
    }

    private void createImage() {
        try {
            String url = "ZJSD_USER@" + my.getUserId();
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
            ivCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

}
