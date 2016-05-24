package com.byteera.bank.activity.business_circle.activity.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import com.byteera.bank.utils.LogUtil;

import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.bank.activity.business_circle.activity.util.FileUtils;
import com.byteera.bank.activity.business_circle.activity.util.ImageUtils;
import com.byteera.hxlib.utils.Constants;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Created by bing on 2015/6/30. */
public class PublishDynamicActivityPre {
    public static final int TAKE_PICTURE = 0x000001;
    private PublishDynamicActivityIV publishDynamicActivityIV;
    private Activity mContext;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PublishDynamicActivityPre(PublishDynamicActivityIV publishDynamicActivityIV) {
        this.publishDynamicActivityIV = publishDynamicActivityIV;
        this.mContext = publishDynamicActivityIV.getContext();
    }

    public void photo() {
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"workupload.jpg"));
        //openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        mContext.startActivityForResult(openCameraIntent, TAKE_PICTURE);
    }

    public void pushDynamic() {
        publishDynamicActivityIV.setSendEnabled(false);
        publishDynamicActivityIV.showPushDialog();
        executorService.execute(new Runnable() {        //发布动态  最好是放到子线程中去执行
            @Override public void run() {
                String token = HXPreferenceUtils.getInstance().getToken();
                String tag = HXPreferenceUtils.getInstance().getTag();
                submitContent(token, publishDynamicActivityIV.getContent(), tag);
            }
        });
    }

    private void submitContent(String token, String content, String tag) {
        RequestParams params = new RequestParams();
        if (Bimp.tempSelectBitmap.size() > 0) {
            for (int i = 0; i < Bimp.tempSelectBitmap.size(); i++) {
//                String imagePath = Bimp.tempSelectBitmap.get(i).getImagePath();
//                String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
                String fileName = String.valueOf(System.currentTimeMillis());
                Bitmap bm = Bimp.tempSelectBitmap.get(i).getBitmap();
                if(bm == null)
                    continue;
                Bitmap bitmap = ImageUtils.compressImage(bm);   //图片压缩
                String filePath = FileUtils.saveBitmap(bitmap, fileName);
                params.addBodyParameter(fileName, new File(filePath));
            }
        }
        params.addBodyParameter("access_token", token);
        params.addBodyParameter("content", content);
        params.addBodyParameter("tag", tag);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "fc/publish?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                publishDynamicActivityIV.showPushFialeDialog();
            }

            @Override public void onLoading(long total, long current, boolean isUploading) {
                LogUtil.e("current", total + "::" + current + "::" + isUploading);
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    String error_description = json.optString("error_description");
                    if ("0".equals(error)) {
                        publishDynamicActivityIV.showPushSuccessDialog();
                    } else {
                        publishDynamicActivityIV.showPushFialeDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void clearPhotos() {
        Bimp.tempSelectBitmap.clear();
    }

    public void intentToGallery(int position) {
        Intent intent = new Intent(mContext, GalleryActivity.class);
        intent.putExtra("ID", position);
        ActivityUtil.startActivity(mContext, intent);
    }
}
