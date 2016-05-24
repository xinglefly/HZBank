package com.byteera.bank.activity.contact;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.db.FriendshipDao;
import com.byteera.bank.domain.FriendRelationShip;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.LoadingDialogShow;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ViewHolder;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class FriendNewRequest extends BaseActivity {
    protected static final String TAG = FriendNewRequest.class.getSimpleName();
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.newrequest_list) private ListView newrequest_list;

    private List<FriendRelationShip> friendRelationShips = null;
    private LoadingDialogShow dialog;

    TextView tv_accept;


    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.friend_request);

        ViewUtils.inject(this);

        dialog = new LoadingDialogShow(baseContext);

        initView();

        friendRelationShips = DBManager.getInstance(baseContext).getAllFriendShip();

        newrequest_list.setAdapter(new FriendListAdapter(baseContext, friendRelationShips));

        newrequest_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_userid = ViewHolder.get(view, R.id.tv_userid);
                tv_accept = ViewHolder.get(view, R.id.tv_accept);
                tv_accept.setFocusable(true);
                tv_accept.setFocusableInTouchMode(true);
                if (tv_accept.getText().toString().equals("接受")) {
                    ResponseResult(tv_userid.getText().toString());
                }
            }
        });
    }

    /** 好友应答 @param user_id */
    private void ResponseResult(final String user_id) {
        dialog.show();
        String token = HXPreferenceUtils.getInstance().getToken();
        JSONObject json = new JSONObject();
        try {
            json.put("result", 1);
            json.put("srcuser", user_id);
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

        MyhttpUtils.getInstance().sendAsync(HttpRequest.HttpMethod.POST, Constants.BYTEERA_SERVICE + "friends/addresult?", params, new RequestCallBack<String>() {
            @Override
            public void onFailure(HttpException error, String msg) {
                dialog.setResultStatusDrawable(false, "发送请求失败");
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
                        try {
                            FriendRelationShip friend = DBManager.getInstance(baseContext)
                                    .getFriendShipBySrcUser(user_id);

                            if (friend != null) {
                                DBManager.getInstance(baseContext).UpdateFriendship(user_id, FriendshipDao.RESULT_SUCCESS);
                                LogUtil.d("update ok!");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                tv_accept.setText("已接受");
                                tv_accept.setBackgroundResource(R.drawable.textview_result_corner);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override
            public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });
    }

    private class FriendListAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private List<FriendRelationShip> friendsList;

        private FriendListAdapter(Context context, List<FriendRelationShip> friendsList) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            this.friendsList = friendsList;
        }

        @Override
        public int getCount() {
            if (friendsList != null)
                return friendsList.size();
            else
                return 0;
        }


        @Override
        public Object getItem(int position) {
            return friendsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.new_friend_request_item, null);
            }
            ImageView img_new_request = ViewHolder.get(convertView, R.id.img_new_request);
            TextView tv_requestname = ViewHolder.get(convertView, R.id.tv_requestname);
            TextView tv_accept = ViewHolder.get(convertView, R.id.tv_accept);
            TextView tv_userid = ViewHolder.get(convertView, R.id.tv_userid);

            FriendRelationShip friend = friendsList.get(position);

            //判断是否已经接受
            if (friend.getResult() == FriendshipDao.RESULT_REQUEST) {
                tv_accept.setText("接受");
                tv_accept.setBackgroundResource(R.drawable.textview_corner);
            } else {
                tv_accept.setText("已接受");
                tv_accept.setBackgroundResource(R.drawable.textview_result_corner);
            }

            try {
                final User user = DBManager.getInstance(baseContext).getUserByUserId(friend.getSrcUser());
                if (user != null) {
                    tv_requestname.setText(user.getNickName());
                    tv_userid.setText(user.getUserId());
                    if (!StringUtils.isEmpty(user.getAvatar())) {
                        ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE +
                                user.getAvatar(), img_new_request);
                    }
                }
            } catch (Exception e) {
                LogUtil.e(e, "GetViewItem in FriendNewRequest");
            }

            return convertView;
        }


        @Override
        public boolean areAllItemsEnabled() {   //表示adapter下所有item是否可以点击
            return true;
        }
    }


}
