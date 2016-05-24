/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.byteera.bank.utils.LogUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.multipart.content.ContentBody;
import com.lidroid.xutils.http.client.multipart.content.FileBody;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    private OkHttpClient mOkHttpClient = new OkHttpClient();


    public HttpUtils() {
        mOkHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
    }

    public Response send(HttpRequest.HttpMethod method, String url) throws IOException {
        return send(method, url, null);
    }

    public Response send(HttpRequest.HttpMethod method, String url, RequestParams params) throws IOException {

        Request.Builder builder = buildRequest(method, url, params);

        return mOkHttpClient.newCall(builder.build()).execute();
    }


    public <T> void sendAsync(HttpRequest.HttpMethod method, String url,
                              RequestCallBack<T> callBack) {
        sendAsync(method, url, null, callBack);
    }

    public <T> void sendAsync(HttpRequest.HttpMethod method, String url, RequestParams params,
                              final RequestCallBack<T> callBack) {

        Request.Builder builder = buildRequest(method, url, params);

        mOkHttpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callBack.onFailure(new HttpException(), e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response != null && response.body() != null) {
                    callBack.onSuccess(new ResponseInfo<T>(null, (T) response.body().string(), false));
                } else {
                    callBack.onFailure(new HttpException("Response is null"), "Response is null");
                }
            }
        });
    }

    @NonNull
    private Request.Builder buildRequest(HttpRequest.HttpMethod method, String url, RequestParams params) {
        Uri.Builder uriBuilder = Uri.parse(url).buildUpon();

        if(params != null)
        {
            List<NameValuePair> queryParams = params.getQueryStringParams();
            if(queryParams != null)
            {
                for(NameValuePair param : queryParams)
                {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }
            }
        }

        url = uriBuilder.build().toString();

        Request.Builder builder = new Request.Builder().url(url);

        builder.addHeader("Content-Encoding", "gzip, deflate");

        if(params != null && params.getHeaders() != null)
        {
            for(RequestParams.HeaderItem header : params.getHeaders())
            {
                builder.addHeader(header.header.getName(), header.header.getValue());
            }
        }

        if(method == HttpRequest.HttpMethod.POST && params != null)
        {
            RequestBody requestBody;

            HashMap<String, ContentBody> fileParams = params.getFileParams();

            if(fileParams != null)
            {
                requestBody = buildFileRequestBody(params, fileParams);
            }
            else
            {
                requestBody = buildFormRequestBody(params);
            }

            builder.post(requestBody);
        }

        LogUtil.d("RequestUrl: %s", url);
        return builder;
    }

    @NonNull
    private RequestBody buildFormRequestBody(RequestParams params) {


        if (params != null)
        {
            List<NameValuePair> bodyParams = params.getBodyParams();

            if(bodyParams != null)
            {
                FormEncodingBuilder builder = new FormEncodingBuilder();

                for(NameValuePair param: params.getBodyParams())
                {
                    builder.add(param.getName(), param.getValue());
                }

                return builder.build();
            }

            HttpEntity entity = params.getEntity();
            if(entity != null)
            {
                try
                {
                    String body = InputStreamToString(entity.getContent(), "utf-8");
                    return RequestBody.create(MediaType.parse("application/javascript"), body);
                }
                catch (Exception ex)
                {
                    LogUtil.e(ex.getMessage());
                }
            }
        }

        return RequestBody.create(MediaType.parse("application/javascript"), "");
    }

    public static String InputStreamToString(InputStream in, String encoding) throws Exception{

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int count = -1;
        while((count = in.read(data,0,4096)) != -1)
            outStream.write(data, 0, count);
        data = null;
        return new String(outStream.toByteArray(), encoding);
    }


    @NonNull
    private RequestBody buildFileRequestBody(RequestParams params, HashMap<String, ContentBody> fileParams) {
        MultipartBuilder builder = new MultipartBuilder();

        builder = builder.type(MultipartBuilder.FORM);
        for(String fileName : fileParams.keySet())
        {
            builder.addFormDataPart(fileName, fileName, RequestBody.create(MediaType.parse("application/octet-stream"),
                    ((FileBody) fileParams.get(fileName)).getFile()));
        }

        List<NameValuePair> bodyParams = params.getBodyParams();

        if(bodyParams != null)
        {
            for(NameValuePair param: params.getBodyParams())
            {
                builder.addFormDataPart(param.getName(), param.getValue());
            }
        }

        return builder.build();
    }

    public void download(String path, String target, RequestCallBack<File> callback) {
            try{
                URL url = new URL(path);
                HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                int total = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File file = new File(target);
                if(file.exists())
                {
                    file.delete();
                }

                File storePath = file.getParentFile();
                if(!storePath.exists())
                {
                    storePath.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                int len ;
                int current=0;
                while((len =bis.read(buffer))!=-1){
                    fos.write(buffer, 0, len);
                    current+= len;
                    callback.onLoading(total, current, false);
                }
                fos.close();
                bis.close();
                is.close();
                callback.onSuccess(new ResponseInfo<>(null, file, false));
            }
            catch (Exception ex)
            {
                callback.onFailure(new HttpException(ex), "Download Failed");
            }
    }
}
