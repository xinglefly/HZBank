package com.byteera.bank.activity.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.byteera.R;
import com.byteera.bank.BankHXSDKHelper;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.MainActivity;
import com.byteera.bank.activity.RegisterActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.AES256Cipher;
import com.byteera.bank.utils.CommonUtils;
import com.byteera.bank.utils.EasemobUtil;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.controller.HXSDKHelper;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity {
    protected static final String TAG = LoginActivity.class.getSimpleName();
    private EditText usernameEditText;
    private EditText passwordEditText;
    public static final int REQUEST_CODE_SETNICK = 1;
    private boolean progressShow;
    private boolean autoLogin = false;
    private List<User> users;
    public User user;

    private String password;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditText = (EditText)findViewById(R.id.username);
        passwordEditText = (EditText)findViewById(R.id.password);

        // 如果用户名密码都有，直接进入主页面
        if (BankHXSDKHelper.getInstance().isLogined()) {
            autoLogin = true;

            EasemobUtil.getUserGroup();
            EasemobUtil.updateUserList();
            EasemobUtil.getDepartmentList();

            ActivityUtil.startActivity(baseContext, new Intent(LoginActivity.this, MainActivity.class));
        }

        String storeUserName = HXPreferenceUtils.getInstance().getUsername();
        String storePassword = HXPreferenceUtils.getInstance().getPassword();

        if(!StringUtils.isEmpty(storeUserName) && !StringUtils.isEmpty(storePassword))
        {
            usernameEditText.setText(storeUserName);
            passwordEditText.setText(storePassword);
        }
    }

    public void login(View view) {
        if (!CommonUtils.isNetWorkConnected(this)) {
            ToastUtil.showToastText("连接已断开，请检查您的网络连接！");
            return;
        }
        isLogin();
    }

    public void isLogin() {
        final String username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            progressShow = true;
            final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
            pd.setCanceledOnTouchOutside(false);
            pd.setOnCancelListener(new OnCancelListener() {
                @Override public void onCancel(DialogInterface dialog) {
                    progressShow = false;
                }
            });
            loginService(username, password);
        }
    }

    private void loginService(final String username, String password) {
        String encryptingCode = null;
        try {
            encryptingCode = AES256Cipher.encrypt("0123456789abcdef", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setMessage("正在登录....");
        dialog.show();
        final String encryptPassword = encryptingCode;

        MyApp.getInstance().executorService.execute(new Runnable() {
            @Override
            public void run() {
                getData(username, encryptPassword);
            }
        });
    }

    private void getData(final String username, String encryptPassword) {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("password", encryptPassword);
        LogUtil.d("server http--> %s", Constants.BYTEERA_SERVICE);
        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.GET, Constants.BYTEERA_SERVICE + "login/" + username, params, new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtil.d("获取信息失败:" + msg);
                        ToastUtil.showToastText("获取信息失败");
                        dialog.dismiss();
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resInfo) {
                        try {
                            String result = resInfo.result;
                            LogUtil.d("result-->" + result);
                            JSONTokener jsonTokener = new JSONTokener(result);
                            JSONObject json = new JSONObject(jsonTokener);
                            String access_token = json.optString("access_token");
                            MyApp.getInstance().setToken(access_token);
                            String error = json.optString("error");
                            if ("0".equals(error)) {
                                JSONObject data = json.optJSONObject("data");
                                String head_photo = data.optString("head_photo");
                                String opnum = data.optString("opnum");
                                String mobile_phone = data.getString("mobile_phone");
                                String phone = data.optString("phone");
                                String user_id = data.optString("user_id");
                                String name = data.optString("name");
                                String depart = data.optString("depart");
                                String email = data.optString("email");
                                String huanxin = data.optString("huanxin");
                                String sex = data.optString("sex");

                                User user = new User();
                                user.setAvatar(Constants.BYTEERA_SERVICE + head_photo);
                                user.setOpnum(opnum);
                                user.setMobile(mobile_phone);
                                user.setTel(phone);
                                user.setUserId(user_id);
                                user.setNickName(name);
                                user.setDepart(depart);
                                user.setEmail(email);
                                user.setEasemobId(huanxin);
                                user.setSex(sex);
                                HXPreferenceUtils.getInstance().setUserInfo(user);

                                final String ease_username = huanxin.substring(0, huanxin.lastIndexOf(":"));
                                final String ease_pwd = huanxin.substring(huanxin.lastIndexOf(":") + 1);

                                MyApp.getInstance().setUserName(ease_username);
                                HXPreferenceUtils.getInstance().setHeadAvator(head_photo);
                                HXPreferenceUtils.getInstance().setUsername(username);
                                HXPreferenceUtils.getInstance().setPassword(password);

                                if (!DBManager.getInstance(baseContext).isExistUserList()) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getUserList(MyApp.getInstance().getToken(), ease_username,
                                                    ease_pwd);
                                        }
                                    });
                                } else {
                                    LoginEase(ease_username, ease_pwd);
                                }
                            } else {
                                MyApp.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.setResultStatusDrawable(false, "密码错误");
                                    }
                                });

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            dialog.dismiss();
                        }
                    }
                }
        );
    }


    public void LoginEase(final String username, final String password) {
        EMChatManager.getInstance().login(username, password, new EMCallBack() {
            @Override
            public void onSuccess() {

                if (!progressShow) {
                    return;
                }
                // 登陆成功，保存用户名密码
                MyApp.getInstance().setUserName(username);
                MyApp.getInstance().setPassword(password);
                try {
                    // ** 第一次登录或者之前logout后，加载所有本地群和回话
                    EMGroupManager.getInstance().loadAllGroups();
                    EMChatManager.getInstance().loadAllConversations();
                    // 处理好友和群组
                    initializeContacts();
                } catch (Exception e) {
                    e.printStackTrace();
                    // 取好友或者群聊失败，不让进入主页面
                    runOnUiThread(new Runnable() {
                        public void run() {
                            dialog.dismiss();
                            BankHXSDKHelper.getInstance().logout(null);
                            dialog.setResultStatusDrawable(false, "登录失败");
                        }
                    });
                    return;
                }

                // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(MyApp.currentUserNick);
                if (!updatenick) {
                    LogUtil.e("update current user nick fail");
                }

                EasemobUtil.getUserGroup();
                EasemobUtil.updateUserList();
                EasemobUtil.getDepartmentList();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();

                    }
                });

                startActivity(new Intent(LoginActivity.this, MainActivity.class));

                finish();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(final int code, final String message) {
                LogUtil.e("Easemob login failed, code: %d, message: %s", code, message);
                if (!progressShow) {
                    return;
                }
                MyApp.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        ToastUtil.showLongToastText("登录失败");
                    }
                });

            }
        });
    }


    private void initializeContacts() {
        Map<String, User> userlist = new HashMap<String, User>();

        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constants.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(R.string.Application_and_notify);
        newFriends.setNick(strChat);

        userlist.put(Constants.NEW_FRIENDS_USERNAME, newFriends);
        // 添加"群聊"
        User groupUser = new User();
        String strGroup = getResources().getString(R.string.group_chat);
        groupUser.setUsername(Constants.GROUP_USERNAME);
        groupUser.setNick(strGroup);
        groupUser.setHeader("");
        userlist.put(Constants.GROUP_USERNAME, groupUser);

        // 存入内存
        ((BankHXSDKHelper) HXSDKHelper.getInstance()).setContactList(userlist);
    }


    /** 获取联系人列表 */
    private void getUserList(String token, final String ease_username, final String ease_pwd) {
        dialog.setMessage("加载全行通讯录...");
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", token);
        params.addQueryStringParameter("version", "0");
        final Long getUserListStart = System.currentTimeMillis();
        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.GET, Constants.BYTEERA_SERVICE_CONTACT, params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                dialog.setResultStatusDrawable(false, "获取联系人列表失败");
                LogUtil.e("--->", msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    Long getUserListDone = System.currentTimeMillis();
                    String result = resInfo.result;
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        JSONObject obj = json.optJSONObject("data");
                        String version = obj.optString("version");
                        LogUtil.d("remote version: %s, total user: %d", version, obj.optJSONArray("user").length());
                        MyApp.getInstance().setVersion(version);

                        JSONArray jsonUserArray = obj.optJSONArray("user");
                        long parseUserListDone = System.currentTimeMillis();
                        DBManager.getInstance(baseContext).saveUserList(jsonUserArray, true);
                        long storeUserListDone = System.currentTimeMillis();

                        LogUtil.d("GetUserListTime: %dms, ParseUserListTime: %dms, " +
                                        "StoreUserList: %dms",
                                (getUserListDone - getUserListStart),
                                (parseUserListDone - getUserListDone),
                                (storeUserListDone - getUserListDone));

                        LoginEase(ease_username, ease_pwd);
                    } else {
                        BankHXSDKHelper.getInstance().logout(null);
                        dialog.setResultStatusDrawable(false, "获取联系人列表失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    BankHXSDKHelper.getInstance().logout(null);
                    dialog.setResultStatusDrawable(false, "获取联系人列表失败");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SETNICK) {
                MyApp.currentUserNick = data.getStringExtra("edittext");
            }
        }
    }

    public void register(View view) {
        startActivityForResult(new Intent(this, RegisterActivity.class), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoLogin) {
            return;
        }

        String username = HXPreferenceUtils.getInstance().getUsername();

        if (!StringUtils.isEmpty(username)) {
            usernameEditText.setText(username);
        }
    }
}
