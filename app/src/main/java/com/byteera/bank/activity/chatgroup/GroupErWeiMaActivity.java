package com.byteera.bank.activity.chatgroup;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.Hashtable;

/** Created by lieeber on 15/8/28. */
public class GroupErWeiMaActivity extends BaseActivity {

    private int QR_HEIGHT;
    private int QR_WIDTH;
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;

    @ViewInject(R.id.iv_head) private ImageView ivHead;
    @ViewInject(R.id.tv_name) private TextView tvName;

    @ViewInject(R.id.iv_code) private ImageView ivCode;
    private EMGroup emGroup;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.group_dimension_code);
        ViewUtils.inject(this);

        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });

        QR_HEIGHT = UIUtils.dip2px(this, 300);
        QR_WIDTH = UIUtils.dip2px(this, 300);
        mHeadView.setTitleName("群二维码");
        String groupId = getIntent().getStringExtra("groupId");
        emGroup = EMGroupManager.getInstance().getGroup(groupId);
        tvName.setText(emGroup.getName());
        //   ImageLoader.getInstance().displayImage(emGroup.);
        createImage();
    }

    private void createImage() {
        try {
            String url = emGroup.getGroupId();
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return;
            }
            url = "ZJSD_GROUP@" + url;
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
