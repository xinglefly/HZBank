package com.byteera.bank.activity.business_circle.activity.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

//文件操作相关类
public class FileUtils {
    public static String PHOTO_PAHT = getSDPath() + "/hzyh/";
    public static String sdcardState = Environment.getExternalStorageState();
    public static String sdcardPathDir = android.os.Environment.getExternalStorageDirectory().getPath() + Constants.CACHE_DIR + "/photo/";
    public static String SDPATH = Environment.getExternalStorageDirectory() + Constants.CACHE_DIR + "/photo/thumb";
    public static String downloadDir = android.os.Environment.getExternalStorageDirectory().getPath() + Constants.CACHE_DIR + "/download/";


    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();

    }

    /** 保存bitmap */
    public static String saveBitmap(Bitmap bm, String picName) {
        try {
            File dirFile = new File(PHOTO_PAHT);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File f = new File(PHOTO_PAHT, picName + ".jpg");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return PHOTO_PAHT + picName + ".jpg";
    }

    /** 获得应用的logo所对应的本地的地址 */
    public static String getLogo() {
        //获得通过Drawable获得bitmap
        Bitmap bitmap = FormatTools.drawable2Bitmap(MyApp.getInstance().getResources().getDrawable(R.drawable.bank_logo));
        //保存bitMap
        return FileUtils.saveBitmap(bitmap, "logo");
    }

    /**
     * 判断sd卡是否可用
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 返回图片保存地址对应的Uri
     */
    public static Uri getCaptureFilePath() {
        String saveDir;
        if (hasSdcard()) {
            saveDir = PHOTO_PAHT;
            createSaveDir(saveDir);
        } else {
            return null;
        }
        String fileName = "picture" + System.currentTimeMillis() + ".JPEG";
        return Uri.fromFile(new File(saveDir, fileName));
    }

    /**
     * 根据目录名来创建目录
     */
    public static void createSaveDir(String savePath) {
        File savedir = new File(savePath);
        if (!savedir.exists()) {
            savedir.mkdirs();
        }
    }


    public static void startPhotoZoom(Activity mActivity, Uri captureFilePath) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(captureFilePath, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("return-data", true);
        mActivity.startActivityForResult(intent, Constants.CLIP_PHOTO);
    }

    public static String startPhotoZoom(Fragment mActivity, Uri captureFilePath) {
        try {
            /****判断目录是否已存在****/
            File file = null;
            // 有sd卡，是否有myImage文件夹
            File fileDir = new File(sdcardPathDir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            // 获取系统时间 然后将裁剪后的图片保存至指定的文件夹
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
            String address = sDateFormat.format(new java.util.Date());
            if (!FileUtils.isFileExist("")) {
                FileUtils.createSDDir("");
            }
            //获得文件的路径
            String photoPath = FileUtils.SDPATH + address + ".jpg";
            //这里需要和FileUtils.SDPATH 一致，而且在之前要创建文件夹
            Uri imageUri = Uri.parse("file:///sdcard/" + Constants.CACHE_DIR + "/photo/thumb" + address + ".jpg");
            System.out.println("uri====" + FileUtils.SDPATH + address + ".jpg");
            final Intent intent = new Intent("com.android.camera.action.CROP");
            // 照片URL地址
            intent.setDataAndType(captureFilePath, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 480);
            intent.putExtra("outputY", 480);
            // 输出路径
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            // 输出格式
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            // 不启用人脸识别
            intent.putExtra("noFaceDetection", false);
            intent.putExtra("return-data", false);
            mActivity.startActivityForResult(intent, Constants.CLIP_PHOTO);
            return photoPath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        file.isFile();
        return file.exists();
    }

    public static File createSDDir(String dirName) throws IOException {
        File dir = new File(SDPATH + dirName);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        }
        return dir;
    }

    public static void getPicture(Activity mActivity) {
        Intent intentFromGallery = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentFromGallery.setType("image/*"); // 设置文件类型
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        mActivity.startActivityForResult(intentFromGallery, Constants.FROM_ALBUM);
    }

    public static void getPicture(Fragment mFragment) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mFragment.startActivityForResult(intent, Constants.RESULT_LOAD_IMAGE);
    }

    public static File getPhoto(Activity mContext) {
        if (!FileUtils.hasSdcard()) {
            ToastUtil.showToastText("SD卡不存在，不能拍照");
            return null;
        }
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File captureFile = FileUtils.getCaptureFile();
        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(captureFile));
        mContext.startActivityForResult(openCameraIntent, Constants.TAKE_PICTURE);
        return captureFile;
    }

    public static Uri getPhoto(Fragment mContext) {
        try {
            Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = null;
            if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                // 有sd卡，是否有myImage文件夹
                File fileDir = new File(sdcardPathDir);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                // 是否有headImg文件
                file = new File(sdcardPathDir + System.currentTimeMillis() + ".jpg");
            }
            if (file != null) {
                String path = file.getPath();
                Uri photoUri = Uri.fromFile(file);
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                mContext.startActivityForResult(openCameraIntent, Constants.TAKE_PICTURE);
                return photoUri;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getCaptureFile() {
        String saveDir;
        if (hasSdcard()) {
            saveDir = PHOTO_PAHT;
            createSaveDir(saveDir);
        } else {
            return null;
        }
        String fileName = "picture" + System.currentTimeMillis() + ".JPEG";
        return new File(saveDir, fileName);
    }

}
