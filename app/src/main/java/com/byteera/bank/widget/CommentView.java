package com.byteera.bank.widget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.byteera.R;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;
import com.byteera.bank.domain.VisitorContent;
import com.byteera.hxlib.utils.Constants;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.ToastUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;


/** Created by bing on 2015/6/25. */
public class CommentView extends FrameLayout implements View.OnClickListener, ImeEditText.OnImeHideListener {
    public static boolean keyBoardIsHide = true;
    private ImeEditText etComment;
    private String chat_id;
    private String token;
    private int position;

    @Override public void imeHide() {
        quitFocus();
    }

    public void setComment(String chat_id, String token, int position, String name) {
        this.chat_id = chat_id;
        this.token = token;
        this.position = position;
        etComment.setHint("@" + name);
    }

    public interface OnCommentFinishListener {
        void commentFinish(VisitorContent comment, int position);
    }

    private OnCommentFinishListener onCommentFinishListener;

    public void setOnCommentFinishListener(OnCommentFinishListener listener) {
        this.onCommentFinishListener = listener;
    }

    public CommentView(Context context) {
        this(context, null);
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.comment_view, this, true);
        etComment = (ImeEditText) findViewById(R.id.et_comment);
        findViewById(R.id.btn).setOnClickListener(this);
        etComment.setOnImeHideListener(this);
    }

    public void getFocus() {
        keyBoardIsHide = false;
        etComment.setFocusable(true);
        etComment.setFocusableInTouchMode(true);
        etComment.requestFocus();
        UIUtils.showKeyboard((Activity) getContext(), etComment);
    }

    public void quitFocus() {
        etComment.setText("");
        UIUtils.hideKeyboard((Activity) getContext(), etComment);
        this.setVisibility(GONE);
        keyBoardIsHide = true;
    }

    @Override public void onClick(View v) {
        String comment = etComment.getText().toString();
        if (TextUtils.isEmpty(comment)) {
            ToastUtil.showToastText("内容不能为空");
        } else {
            JSONObject json = new JSONObject();
            try {
                json.put("fc_id", chat_id);
                json.put("content", comment);
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
            MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "/fc/reply?", params, new RequestCallBack<String>() {
                @Override
                public void onFailure(HttpException error, String msg) {
                    ToastUtil.showToastText("评论失败，请重试");
                }

                @Override
                public void onSuccess(ResponseInfo<String> resInfo) {
                    try {
                        String result = resInfo.result;
                        JSONTokener jsonTokener = new JSONTokener(result);
                        JSONObject json = new JSONObject(jsonTokener);
                        String error = json.optString("error");
                        if ("0".equals(error)) {
                            ToastUtil.showToastText("评论成功");
                            JSONObject data = json.getJSONObject("data");
                            String content = data.optString("content");
                            String userId = data.optString("user_id");
                            String name = data.optString("name");
                            if (onCommentFinishListener != null) {

                                VisitorContent visitorContent = new VisitorContent();
                                visitorContent.setContent(content);
                                visitorContent.setName(name);
                                visitorContent.setUser_id(userId);
                                onCommentFinishListener.commentFinish(visitorContent, position);
                            }
                        } else {
                            ToastUtil.showToastText("评论失败，请重试");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
