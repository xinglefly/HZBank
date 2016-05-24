package com.byteera.bank.utils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

public class MyhttpUtils extends HttpUtils {

    private static MyhttpUtils instance;

    private MyhttpUtils() {

    }

    public static MyhttpUtils getInstance() {
        if (instance == null) {
            synchronized (MyhttpUtils.class) {
                if (instance == null) {
                    instance = new MyhttpUtils();
                }
            }
        }
        return instance;
    }

    public void loadData(HttpRequest.HttpMethod httpMethod, String url,RequestParams params,
                         RequestCallBack<String> callBack) {
        instance.sendAsync(httpMethod, url, params, callBack);
    }
}
