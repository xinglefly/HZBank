package com.byteera.bank.activity.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.easemob.chat.EMChatManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;


public class ContactDetailActivity extends BaseActivity {

    private static final String TAG = ContactDetailActivity.class.getSimpleName();

    @ViewInject(R.id.iv_back) private ImageView ivBack;
    @ViewInject(R.id.img_header) private ImageView img_header;
    @ViewInject(R.id.tv_name) private TextView tv_name;
    @ViewInject(R.id.tv_depart) private TextView tv_department; //sex + department
    @ViewInject(R.id.tv_account_value) private TextView tv_account_value;
    @ViewInject(R.id.tv_phone_value) private TextView tv_phone_value;
    @ViewInject(R.id.tv_telephone_value) private TextView tv_telephone_value;
    @ViewInject(R.id.tv_email_value) private TextView tv_email_value;

    //显示隐藏添加好友，删除好友，解除好友关系
    @ViewInject(R.id.tv_openchat) private TextView tv_openchat;
    @ViewInject(R.id.tv_addfriend) private TextView tv_addfriend;
    @ViewInject(R.id.tv_deletefriend) private TextView tv_deletefriend;

    private String user_id;
    private String name;
    private String easemobId;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.contactdetail);
        ViewUtils.inject(this);
        setTextView();
        visibleController();
    }

    private void visibleController() {
        LogUtil.d("--Constants.friendList->" + Constants.friendList.size());
        if (Constants.friendList.contains(user_id)) {
            tv_openchat.setVisibility(View.VISIBLE);
            tv_deletefriend.setVisibility(View.VISIBLE);
        } else if (MyApp.getInstance().getUserName().equals(easemobId)) {
            tv_openchat.setVisibility(View.GONE);
            tv_deletefriend.setVisibility(View.GONE);
        } else {
            tv_addfriend.setVisibility(View.VISIBLE);
        }
    }

    private void setTextView() {
        user_id = getIntent().getStringExtra("user_id");
        try {
            User user = DBManager.getInstance(baseContext).getUserByUserId(user_id);
            if (user != null) {
                LogUtil.d("-user->" + user.toString());
                name = user.getNickName();
                easemobId = user.getEasemobId();
                tv_name.setText(user.getNickName());
                tv_department.setText(user.getSex() + "  " + user.getDepart());
                tv_account_value.setText(user.getOpnum());
                tv_phone_value.setText(user.getMobile());
                tv_telephone_value.setText(user.getTel());
                tv_email_value.setText(user.getEmail());
                if (!user.getAvatar().equals("")) {
                    ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + user.getAvatar(), img_header);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ActivityUtil.finishActivity(baseContext);
            }
        });
    }


    @OnClick({R.id.tv_openchat, R.id.tv_addfriend, R.id.tv_deletefriend})
    public void clickListener(View view) {
        switch (view.getId()) {
            case R.id.tv_openchat:
                if (easemobId.toLowerCase().equals(MyApp.getInstance().getUserName())) {
                    ToastUtil.showToastText("不能和自己聊天");
                } else {
                    ActivityUtil.startActivity(baseContext, new Intent(baseContext, ChatActivity.class).putExtra("userId", easemobId).putExtra("nick", name));
                }
                break;

            case R.id.tv_addfriend:
                requstAddFriend();
                break;
            case R.id.tv_deletefriend:
                deleteFriend();
                break;
        }
    }


    /** 添加好友发送请求 */
    private void requstAddFriend() {
        dialog.show();
        String token = HXPreferenceUtils.getInstance().getToken();
        JSONObject json = new JSONObject();
        try {
            json.put("dstuser", user_id);
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

        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "friends/addrequest?", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                ToastUtil.showToastText("发送请求失败，请稍后重试！");
                dialog.dismiss();
                LogUtil.d("err-->" + msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    LogUtil.d("succ-->" + result);
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);

                    String error = json.optString("error");
                    String error_description = json.optString("error_description");
                    if ("0".equals(error)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setResultStatusDrawable(true, "发送请求成功");
                            }
                        });

                        MyApp.getMainThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtil.finishActivity(baseContext);
                            }
                        }, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        visibleController();
    }

    /** 解除好友关系 **/
    private void deleteFriend() {
        dialog.show();
        JSONObject json = new JSONObject();
        try {
            json.put("dstuser", user_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", MyApp.getInstance().getToken());
        try {
            params.setBodyEntity(new StringEntity(json.toString(), "utf-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "friends/delete?", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setResultStatusDrawable(true, "解除失败！");
                    }
                });
                LogUtil.e(msg);
            }

            @Override
            public void onSuccess(ResponseInfo<String> resInfo) {
                try {
                    String result = resInfo.result;
                    LogUtil.d("succ-->" + result);
                    JSONTokener jsonTokener = new JSONTokener(result);
                    JSONObject json = new JSONObject(jsonTokener);
                    String error = json.optString("error");
                    if ("0".equals(error)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setResultStatusDrawable(true, "解除成功");
                            }
                        });

                        Constants.friendList.remove(user_id);
                        LogUtil.d("移除好友");

                        User user = DBManager.getInstance(baseContext).getUserByUserId(user_id);
                        if (user != null) {
                            EMChatManager.getInstance().deleteConversation(user.getEasemobId(), false);
                            LogUtil.d("删除会话");
                        }

                        MyApp.getMainThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ActivityUtil.finishActivity(baseContext);
                            }
                        }, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setResultStatusDrawable(true, "解除成功");
                        }
                    });
                }
            }
        });
    }


}
