package com.byteera.bank.activity.setting;

import android.os.Bundle;
import android.widget.EditText;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;

public class UpdatePwdActivity extends BaseActivity {

    private static final String TAG = UpdatePwdActivity.class.getSimpleName();

    @ViewInject(R.id.head_view) private HeadViewMain headview;
    @ViewInject(R.id.et_oldpwd) private EditText et_oldpwd;
    @ViewInject(R.id.et_newpwd) private EditText et_newpwd;
    @ViewInject(R.id.et_confirmpwd) private EditText et_confirmpwd;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_data);
        ViewUtils.inject(this);

        clickeListener();
    }

    private void clickeListener() {

        headview.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });

        headview.setRightTextClickListener(new HeadViewMain.RightTextClickListener() {
            @Override
            public void onRightTextClick() {
                String oldpwd = et_oldpwd.getText().toString();
                String newpwd = et_newpwd.getText().toString();
                String confirmpwd = et_confirmpwd.getText().toString();
                if (!confirmpwd.equals(newpwd)) {
                    ToastUtil.showToastText("两次密码不一样，请确认重新输入");
                    et_confirmpwd.setText("");
                    et_confirmpwd.setFocusable(true);
                } else {
                    String token = HXPreferenceUtils.getInstance().getToken();
                    submitUpdatepwd(token, oldpwd, newpwd);
                }
            }
        });
    }


    private void submitUpdatepwd(String token, String oldpwd, String newpwd) {
        dialog.setMessage("正在验证....");
        dialog.show();
        JSONObject json = new JSONObject();
        try {
            json.put("old_pass", oldpwd);
            json.put("new_pass", newpwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", token);
        try {
            params.setBodyEntity(new StringEntity(json.toString(), "utf-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        MyhttpUtils.getInstance().sendAsync(HttpMethod.POST, Constants.BYTEERA_SERVICE + "user/change_pass?", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                dialog.dismiss();
                ToastUtil.showToastText("修改密码失败，请重试");
                LogUtil.d("pwd err-->" + msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    LogUtil.d(TAG, "pwd succ-->" + result);
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);

                    String error = json.optString("error");
                    String error_description = json.optString("error_description");
                    if ("0".equals(error)) {
                        dialog.setResultStatusDrawable(true, "修改成功");
                        ActivityUtil.finishActivity(baseContext);
                    } else {
                        dialog.setResultStatusDrawable(false, "密码错误");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
