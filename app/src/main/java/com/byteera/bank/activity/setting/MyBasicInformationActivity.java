package com.byteera.bank.activity.setting;

import android.os.Bundle;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ChangeSexDialog;
import com.byteera.bank.utils.LoadingDialogShow;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.widget.ChangeDianhuaDialog;
import com.byteera.bank.widget.ChangeEmailDialog;
import com.byteera.bank.widget.ChangeNickNameDialog;
import com.byteera.bank.widget.ChangePhoneDialog;
import com.byteera.bank.widget.HeadViewMain;
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

import org.json.JSONObject;
import org.json.JSONTokener;

public class MyBasicInformationActivity extends BaseActivity {
    @ViewInject(R.id.tv_zhanghao) private TextView tvZhanghao;
    @ViewInject(R.id.tv_xingming) private TextView tvXingming;
    @ViewInject(R.id.tv_xingbie) private TextView tvXingbie;
    @ViewInject(R.id.tv_shouji) private TextView tvShouji;
    @ViewInject(R.id.tv_bangongdianhua) private TextView tvbangongdianhua;
    @ViewInject(R.id.tv_youxiang) private TextView tvYouxiang;
    @ViewInject(R.id.tv_bumen) private TextView tvBumen;
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;

    private ChangeNickNameDialog changeNickNameDialog;
    private ChangePhoneDialog changePhoneDialog;
    private ChangeSexDialog changeSexDialog;
    private ChangeDianhuaDialog changeDianhuaDialog;
    private ChangeEmailDialog changeEmailDialog;

    private static final int FROM_CAMERA = 0;
    private static final int FROM_ALBUM = 1;
    private static final int CLIP_PHOTO = 2;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_basic_information_activity);
        ViewUtils.inject(this);
        initDialog();
        setTextData();
        setEvent();
    }

    private void setTextData() {
        User user = HXPreferenceUtils.getInstance().getUserInfo();
        if (user != null) {
            tvXingming.setText(user.getNickName());
            tvXingbie.setText(user.getSex());
            tvZhanghao.setText(user.getOpnum());
            tvShouji.setText(user.getMobile());
            tvbangongdianhua.setText(user.getTel());
            tvYouxiang.setText(user.getEmail());
            tvBumen.setText(user.getDepart());

        }
    }


    private void setEvent() {
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });

        changeNickNameDialog.setOnOkClickListener(new ChangeNickNameDialog.OnOkClickListener() {
            @Override public void onOk() {
                changeUserName(changeNickNameDialog.getNickName());
            }
        });
        changeSexDialog.setOkClickListener(new ChangeSexDialog.OnOkClickListener() {
            @Override public void onOk() {
                changSex(changeSexDialog.getSex());
            }
        });
        changePhoneDialog.setOnOkClickListener(new ChangePhoneDialog.OnOkClickListener() {
            @Override public void onOk() {
                changePhone(changePhoneDialog.getPhone());
            }
        });
        changeDianhuaDialog.setOnOkClickListener(new ChangeDianhuaDialog.OnOkClickListener() {
            @Override public void onOk() {
                changeDianHua(changeDianhuaDialog.getDianHua());
            }
        });
        changeEmailDialog.setOnOkClickListener(new ChangeEmailDialog.OnOkClickListener() {
            @Override public void onOk() {
                changeEmail(changeEmailDialog.getEmail());
            }
        });
    }

    private void changeEmail(final String email) {
        String token = HXPreferenceUtils.getInstance().getToken();
        RequestParams params = new RequestParams();
        params.addBodyParameter("access_token", token);
        params.addBodyParameter("email", email);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "user?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                ToastUtil.showToastText("修改失败，请稍后重试");
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        ToastUtil.showToastText("修改成功");
                        tvYouxiang.setText(email);
                        User user = HXPreferenceUtils.getInstance().getUserInfo();
                        user.setEmail(email);
                        HXPreferenceUtils.getInstance().setUserInfo(user);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeDianHua(final String dianHua) {
        String token = HXPreferenceUtils.getInstance().getToken();
        RequestParams params = new RequestParams();
        params.addBodyParameter("access_token", token);
        params.addBodyParameter("phone", dianHua);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "user?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                ToastUtil.showToastText("修改失败，请稍后重试");
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        ToastUtil.showToastText("修改成功");
                        tvbangongdianhua.setText(dianHua);
                        User user = HXPreferenceUtils.getInstance().getUserInfo();
                        user.setTel(dianHua);
                        HXPreferenceUtils.getInstance().setUserInfo(user);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changePhone(final String phone) {
        String token = HXPreferenceUtils.getInstance().getToken();
        RequestParams params = new RequestParams();
        params.addBodyParameter("access_token", token);
        params.addBodyParameter("mobile_phone", phone);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "user?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                ToastUtil.showToastText("修改失败，请稍后重试");
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        ToastUtil.showToastText("修改成功");
                        tvShouji.setText(phone);
                        User user = HXPreferenceUtils.getInstance().getUserInfo();
                        user.setMobile(phone);
                        HXPreferenceUtils.getInstance().setUserInfo(user);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void changSex(final String sex) {
        String token = HXPreferenceUtils.getInstance().getToken();
        RequestParams params = new RequestParams();
        params.addBodyParameter("access_token", token);
        params.addBodyParameter("sex", sex);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "user?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                ToastUtil.showToastText("修改失败，请稍后重试");
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        ToastUtil.showToastText("修改成功");
                        tvXingbie.setText(sex);
                        User user = HXPreferenceUtils.getInstance().getUserInfo();
                        user.setSex(sex);
                        HXPreferenceUtils.getInstance().setUserInfo(user);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeUserName(final String nickName) {
        String token = HXPreferenceUtils.getInstance().getToken();
        RequestParams params = new RequestParams();
        params.addBodyParameter("access_token", token);
        params.addBodyParameter("name", nickName);
        MyhttpUtils.getInstance().loadData(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "user?", params, new RequestCallBack<String>() {
            @Override public void onFailure(HttpException arg0, String msg) {
                ToastUtil.showToastText("修改失败，请稍后重试");
            }

            @Override public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        ToastUtil.showToastText("修改成功");
                        tvXingming.setText(nickName);
                        User user = HXPreferenceUtils.getInstance().getUserInfo();
                        user.setUsername(nickName);
                        HXPreferenceUtils.getInstance().setUserInfo(user);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initDialog() {
        dialog = new LoadingDialogShow(baseContext);
        changeNickNameDialog = new ChangeNickNameDialog(this);
        changePhoneDialog = new ChangePhoneDialog(this);
        changeSexDialog = new ChangeSexDialog(this);
        changeDianhuaDialog = new ChangeDianhuaDialog(this);
        changeEmailDialog = new ChangeEmailDialog(this);
    }


//    @OnClick({R.id.ll_xingming, R.id.ll_xingbie, R.id.ll_shouji, R.id.ll_bangongdianhua, R.id.ll_youxiang})
//    public void clickView(View view) {
//        switch (view.getId()) {
//            case R.id.ll_xingming:
//                showChangeNameDialog();
//                break;
//            case R.id.ll_xingbie:
//                showChangeSexDialog();
//                break;
//            case R.id.ll_shouji:
//                showChangPhoneDialog();
//                break;
//            case R.id.ll_bangongdianhua:
//                showChangeDianHuaDialog();
//                break;
//            case R.id.ll_youxiang:
//                showChangeEmailDialog();
//                break;
//        }
//    }

    private void showChangeEmailDialog() {
        changeEmailDialog.show();
        changeEmailDialog.setEmail(tvYouxiang.getText().toString());
    }

    private void showChangeDianHuaDialog() {
        changeDianhuaDialog.show();
        changeDianhuaDialog.setDianhua(tvbangongdianhua.getText().toString());
    }

    private void showChangPhoneDialog() {
        changePhoneDialog.show();
        changePhoneDialog.setPhone(tvShouji.getText().toString());
    }

    private void showChangeSexDialog() {
        changeSexDialog.show();
        changeSexDialog.setSex(tvXingbie.getText().toString());
    }

    private void showChangeNameDialog() {
        changeNickNameDialog.show();
        changeNickNameDialog.setNickName(tvXingming.getText().toString());
    }
}
