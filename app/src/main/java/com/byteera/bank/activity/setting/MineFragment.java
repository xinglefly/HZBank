package com.byteera.bank.activity.setting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.byteera.R;
import com.byteera.bank.activity.MainActivity;
import com.byteera.bank.activity.business_circle.activity.util.Bimp;
import com.byteera.bank.activity.business_circle.activity.util.FileUtils;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;

public class MineFragment extends Fragment {
    private GetPhotoPop getPhotoPop;
    @ViewInject(R.id.iv_mine_photo) private ImageView mineFhoto;

    private Uri photoUri;
    private String photoPath;
    private float dp;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.inject(this, getView());
        dp = getResources().getDimension(R.dimen.dp);
        User user = HXPreferenceUtils.getInstance().getUserInfo();
        if(!StringUtils.isEmpty(user.getAvatar()))
            ImageLoader.getInstance().displayImage(user.getAvatar(), mineFhoto);
        else
            ImageLoader.getInstance().displayImage(null, mineFhoto);
    }

    @OnClick({R.id.ll_mine_gongzuoquan, R.id.ll_mine_erweima, R.id.ll_mine_jibenxinxi, R.id.ll_mine_setting, R.id.fl_mine_photo})
    public void click(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.ll_mine_gongzuoquan:
                //ToastUtil.showToastText("跳转到工作圈");
                break;
            case R.id.ll_mine_erweima:
                intent = new Intent(getActivity(), DimensionCodeActivity.class);
                ActivityUtil.startActivity(getActivity(), intent);
                break;
            case R.id.ll_mine_jibenxinxi:
                intent = new Intent(getActivity(), MyBasicInformationActivity.class);
                ActivityUtil.startActivity(getActivity(), intent);
                break;
            case R.id.ll_mine_setting:
                intent = new Intent(getActivity(), SettingActivity.class);
                ActivityUtil.startActivity(getActivity(), intent);
                break;
            case R.id.fl_mine_photo:    //修改头像
                changePhoto();
                break;
        }
    }

    private void changePhoto() {
        if (getPhotoPop == null) {
            getPhotoPop = new GetPhotoPop(getActivity());
        }
        getPhotoPop.showAtLocation(mineFhoto, getActivity());
        getPhotoPop.setSelectPictureListener(new GetPhotoPop.SelectPictureListener() {
            @Override public void selectPicture() {
                //从相册选择图片
                FileUtils.getPicture(MineFragment.this);
            }
        });
        getPhotoPop.setSelectTakePhotoListener(new GetPhotoPop.SelectTakePhotoListener() {
            @Override public void takePhtoto() {
                photoUri = FileUtils.getPhoto(MineFragment.this);
            }
        });
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.TAKE_PICTURE) {
            //最终通过图片地址来上传图片
            photoPath = FileUtils.startPhotoZoom(MineFragment.this, photoUri);    //记得拍照和从相册选图片用的一个photoPath来表示
        }
        if (requestCode == Constants.CLIP_PHOTO) {
            if (resultCode == Activity.RESULT_OK && null != data) {// 裁剪返回
                Bitmap bitmap = Bimp.getLoacalBitmap(photoPath);
                bitmap = Bimp.createFramedPhoto(480, 480, bitmap, (int) (dp * 1.6f));
                changIcon(bitmap);       //通过网络修改图片
            }
        }
        if (requestCode == Constants.RESULT_LOAD_IMAGE) {
            if (resultCode == Activity.RESULT_OK && null != data) {// 裁剪返回
                Uri uri = data.getData();
                if (uri != null) {
                    photoPath = FileUtils.startPhotoZoom(MineFragment.this, uri);
                }
            }
        }
    }

    private void changIcon(final Bitmap photo) {
        String token = HXPreferenceUtils.getInstance().getToken();
        RequestParams params = new RequestParams();
        String fileName = String.valueOf(System.currentTimeMillis());
        params.addBodyParameter(fileName + ".jpg", new File(photoPath));
        params.addBodyParameter("access_token", token);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "user?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                ToastUtil.showToastText("更新个人头像失败，请稍后重试");
            }
            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    JSONObject data = json.getJSONObject("data");
                    String headPhoto = data.optString("head_photo");
                    if ("0".equals(error)) {
                        User user = HXPreferenceUtils.getInstance().getUserInfo();
                        final String headUrl = Constants.BYTEERA_SERVICE + headPhoto;
                        user.setAvatar(headUrl);
                        DBManager.getInstance(getActivity()).updateUserAvatar(user.getUserId(), headPhoto);

                        HXPreferenceUtils.getInstance().setUserInfo(user);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageLoader.getInstance().displayImage(headUrl, mineFhoto);
                            }
                        });

                        ToastUtil.showToastText("更新个人头像成功");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
