package com.byteera.bank.test;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.byteera.bank.utils.LogUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class TestUrl extends AndroidTestCase {

    private static final String TAG = TestUrl.class.getSimpleName();

    
    public void getMemory(){
        long maxMemory = Runtime.getRuntime().maxMemory();
        LogUtil.d("--getmemory-->"+maxMemory/1024/1024);
    }

    public void getimage(){
        String Path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/storage/emulated/0/Photo/IMG_20130727_165809.jpg";
        LogUtil.d("--getimagefile-->"+Path);
    }

    public void getSDlistFile(){
        String Path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/zhicai";
        LogUtil.d("--getfile-->" + Path);
        File file = new File(Path);
        LogUtil.d("--isexit-->" + file.exists());
        File[] files = new File(Path).listFiles();
        for (int i = 0; i < files.length; i++)
        {
           LogUtil.d("--getNickName-->"+files[i].getName());
           LogUtil.d("--getAbPath-->"+files[i].getAbsolutePath());
           LogUtil.d("--getPath-->"+files[i].getPath());
        }
//        02-04 11:21:58.130: D/TestUrl(21057): --getPath-->/storage/emulated/0/zhicai/私有云部署文档.doc

    }
    
    
    
    public void testLogin() {
        String url = "http://119.254.108.108:5000/login/13901234500?password=123";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        // Http拦截器必须实现为线程安全的parameter 参数
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 8000);
        HttpConnectionParams.setSoTimeout(params, 5000);
        get.setParams(params);
        try {
            // 发送请求
            HttpResponse response = client.execute(get);
            // 得到应答的字符串，一个JSON格式保存的数据
            String jsonStr = EntityUtils
                    .toString(response.getEntity(), "UTF-8");
            // 生成JSON对象
            JSONObject result = new JSONObject(jsonStr);
            LogUtil.e(TAG, "打印JsonObject数据：" + jsonStr.toString());

            // 返回值
            // {"resultcode":"000000","resultinfo":"交易成功","videosplits":[{"duration":"300","realurl":"http://118.186.239.231/sohu/s26h23eab6/6//27/5/xYegEiBxkJXxzf1SGqMEq2.mp4?key=jL5wYENDcbyoR4R0G-Q0uFMhDxWnjJEwv6pHwNhttg8."},{"duration":"300","realurl":"http://118.186.239.226/sohu/s26h23eab6/9//52/16/Hc26qYq8PQhhbevzI5qMs6.mp4?key=qlBMiQDAZ5QhqD_ayaIDDn4jPM8hEQ-GqEJ0EQZiiag."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/4//18/57/f9WbQ6O4Q1QaGqt9QFkhg4.mp4?key=ufOBlJTxcSfnytaC-s3bUpBwuF1j9sdDXYOAvrbGG18."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/7//81/228/KuMHBYBa74oYXzpjD6Q0s3.mp4?key=8H-NANZ-qMlP4uD95W_ata0aLGqePFMEKoepNXpRUFA."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/11//90/98/olUxccztHBpHrDn6GEkSl3.mp4?key=DH9EdgyeSPEJm60c-6hqH0WhuOfnJGabtsSGud8c4Uw."},{"duration":"300","realurl":"http://118.186.239.226/sohu/s26h23eab6/11//68/41/OeGWTcRrzV4uepZ5lcG225.mp4?key=nSvr1uC8d-gjeQ8CmpH6X6gAJoN4o86YVVnzvE5hQ1Y."},{"duration":"300","realurl":"http://118.186.239.229/sohu/s26h23eab6/2//93/151/Mp00QDOvf9Gc81MXx8oX37.mp4?key=EWSE449UEC508Mctj8QxoleAxjuLbgTk9svVvER-e_0."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/2//47/52/W1FghUrqzJ3sh6td17k6K6.mp4?key=Axfc-ZrmnVL05ATgwKfjohJiBM28vRWWIdE7g9iJmWw."},{"duration":"112","realurl":"http://118.186.239.228/sohu/s26h23eab6/10//106/136/qGw1n9DcpZ1NRMHqzKC3o3.mp4?key=nbWBuXCo8szOnwM-dZzwX89VmPtY45laRSnc6hg9HD4."}]}

            /**
             * { "access_token":
             * "VQAAAM8wMDA0NzcwMzEyMygBAAfgMTQyMjA4NzU5NzAzNjABAPAONDE1NDk1NGMzM2ZjY2VhMGU4MjFiYzgxYTJmNWU="
             * , "data": { "username": "宋鹏飞", "phone": "", "user_id":
             * "54c33fccea0e821bc81a2f5e", "easemob_id": "spf:123456",
             * "signature": "", "depart": [ "根.软件部.服务器" ], "region": "",
             * "publish_level": [ ], "email": "", "sex": "男" },
             * "error_description": "", "error": 0 }
             */

            // json解析类
            JSONTokener jsonParser = new JSONTokener(jsonStr.toString());
            JSONObject json = new JSONObject(jsonParser);
            JSONObject data = json.optJSONObject("data");
            String easemob_id = data.optString("easemob_id");
            String error = json.optString("error");
            String access_token = json.optString("access_token");

            LogUtil.d("");

            // JSONObject jsonValue = (JSONObject) jsonParser.nextValue();
            // String videosplits = jsonValue.getString("videosplits");
            // JSONArray splitsArray = new JSONArray(videosplits);
            // for (int i = 0; i < splitsArray.length(); i++) {
            // EpisodeInfo episodeInfo = new EpisodeInfo();
            // JSONObject obj = (JSONObject) splitsArray.get(i);
            // episodeInfo.setDuration(obj.getString("duration"));
            // episodeInfo.setRealurl(obj.getString("realurl"));
            // episodeUrlList.add(episodeInfo);
            // }
            // String realurl = episodeUrlList.get(0).getRealurl();
            // LogUtil.i( "status:" + realurl);

            // return result.toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 强制释放HttpClient
            if (client != null) {
                try {
                    client.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client = null;
            }
        }
    }

    /**
     * 获取用户列表
     * 
     * 还有数组，keys的使用（范例）
     */
    public void testGetUsers() {
        String url = "http://119.254.108.108:5000/contact?access_token=VQAAAM8wMDA0MTEzMTEyMygBAAfgMTQyMjM0NjQzMjAzNjABAPAOOTI2MDE1NGMzM2ZjY2VhMGU4MjFiYzgxYTJmNWU=";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        // Http拦截器必须实现为线程安全的parameter 参数
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 8000);
        HttpConnectionParams.setSoTimeout(params, 5000);
        get.setParams(params);
        try {
            // 发送请求
            HttpResponse response = client.execute(get);
            // 得到应答的字符串，一个JSON格式保存的数据
            String jsonStr = EntityUtils
                    .toString(response.getEntity(), "UTF-8");
            // 生成JSON对象
            JSONObject result = new JSONObject(jsonStr);
            LogUtil.e(TAG, "打印JsonObject数据：" + jsonStr.toString());

            // 返回值
            // {"resultcode":"000000","resultinfo":"交易成功","videosplits":[{"duration":"300","realurl":"http://118.186.239.231/sohu/s26h23eab6/6//27/5/xYegEiBxkJXxzf1SGqMEq2.mp4?key=jL5wYENDcbyoR4R0G-Q0uFMhDxWnjJEwv6pHwNhttg8."},{"duration":"300","realurl":"http://118.186.239.226/sohu/s26h23eab6/9//52/16/Hc26qYq8PQhhbevzI5qMs6.mp4?key=qlBMiQDAZ5QhqD_ayaIDDn4jPM8hEQ-GqEJ0EQZiiag."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/4//18/57/f9WbQ6O4Q1QaGqt9QFkhg4.mp4?key=ufOBlJTxcSfnytaC-s3bUpBwuF1j9sdDXYOAvrbGG18."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/7//81/228/KuMHBYBa74oYXzpjD6Q0s3.mp4?key=8H-NANZ-qMlP4uD95W_ata0aLGqePFMEKoepNXpRUFA."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/11//90/98/olUxccztHBpHrDn6GEkSl3.mp4?key=DH9EdgyeSPEJm60c-6hqH0WhuOfnJGabtsSGud8c4Uw."},{"duration":"300","realurl":"http://118.186.239.226/sohu/s26h23eab6/11//68/41/OeGWTcRrzV4uepZ5lcG225.mp4?key=nSvr1uC8d-gjeQ8CmpH6X6gAJoN4o86YVVnzvE5hQ1Y."},{"duration":"300","realurl":"http://118.186.239.229/sohu/s26h23eab6/2//93/151/Mp00QDOvf9Gc81MXx8oX37.mp4?key=EWSE449UEC508Mctj8QxoleAxjuLbgTk9svVvER-e_0."},{"duration":"300","realurl":"http://118.186.239.230/sohu/s26h23eab6/2//47/52/W1FghUrqzJ3sh6td17k6K6.mp4?key=Axfc-ZrmnVL05ATgwKfjohJiBM28vRWWIdE7g9iJmWw."},{"duration":"112","realurl":"http://118.186.239.228/sohu/s26h23eab6/10//106/136/qGw1n9DcpZ1NRMHqzKC3o3.mp4?key=nbWBuXCo8szOnwM-dZzwX89VmPtY45laRSnc6hg9HD4."}]}

            /**
             * 
             * { "data": [ { "user_id": "54c341a0ea0e821bc81a2f66", "name":
             * "高齐", "huanxin": "gq:123456" }, { "user_id":
             * "54c340e4ea0e821bc81a2f61", "name": "王博", "huanxin": "gq:123456"
             * }
             * 
             * 
             * ], "error_description": "", "error": 0 }
             * 
             * 
             */

            // json解析类
            JSONTokener jsonParser = new JSONTokener(jsonStr.toString());
            JSONObject json = new JSONObject(jsonParser);
            JSONArray array = json.optJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                String user_id = obj.optString("user_id");
                String name = obj.optString("name");
            }

            /**
             * 循环没有key的json格式
             * 
             * "users": { "54c340b2ea0e821bc81a2f60": [ "陈振兴", "czx:123456" ],
             * "54c3415bea0e821bc81a2f64": [ "韩威", "hw:123456" ] }
             */
            /*
             * Iterator<?> keys = data.keys(); HashMap<String, Object> map = new
             * HashMap<String, Object>(); while( keys.hasNext() ){ String key =
             * (String)keys.next(); Object value = (Object) data.get(key) ; if(
             * data.get(key) instanceof JSONObject ){ value = (String)
             * data.get(key); } map.put(key, value);
             * 
             * }
             * 
             * Iterator iter = map.entrySet().iterator(); while (iter.hasNext())
             * { Map.Entry entry = (Map.Entry) iter.next(); Object key =
             * entry.getKey(); Object val = entry.getValue(); LogUtil.d(TAG,
             * "iterator-->"+key+""+val); }
             */

            /*
             * 遍历数组
             * 
             * JSONObject jsonValue = (JSONObject) jsonParser.nextValue();
             * String videosplits = jsonValue.getString("videosplits");
             * JSONArray splitsArray = new JSONArray(videosplits); for (int i =
             * 0; i < splitsArray.length(); i++) { EpisodeInfo episodeInfo = new
             * EpisodeInfo(); JSONObject obj = (JSONObject) splitsArray.get(i);
             * episodeInfo.setDuration(obj.getString("duration"));
             * episodeInfo.setRealurl(obj.getString("realurl"));
             * episodeUrlList.add(episodeInfo); } String realurl =
             * episodeUrlList.get(0).getRealurl(); LogUtil.i( "status:" +
             * realurl);
             */

            // return result.toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 强制释放HttpClient
            if (client != null) {
                try {
                    client.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client = null;
            }
        }
    }

    /**
     * 访问方式 HttpClient() 请求方式 post 联调接口 请求数据和响应数据 OK
     */
    public void testUserlogin() {
        // sn=test1234&method=init&data={info:appcallwxb}
        String u2 = "http://58.30.240.29/index.php/config/sys/sys_get_ver?method=wxb&sponsor=1&token=303520516";

        DefaultHttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost(u2);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 5000);
        post.setParams(params);
        JSONObject json = new JSONObject();
        try {
            // json.put("proto", "pppoe");
            // json.put("username", "qwertty");
            // json.put("password", "qwertdg");
            // json.put("ppp_enable", "1");
            // json.put("network", "OFF");
            // JSONObject js=new JSONObject();
            // js.put("access", json);
            // LogUtil.i( js.toString()+"tototot");
            StringEntity se = new StringEntity(json.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(se);
            // post.setEntity(new UrlEncodedFormEntity(pairList, HTTP.UTF_8));
            HttpResponse response = client.execute(post);
            String jsonStr = EntityUtils
                    .toString(response.getEntity(), "UTF-8");
            // JSONObject jsObject = new JSONObject(jsonStr);
            LogUtil.i( json.toString() + "*****" + jsonStr);
            // json解析类
            JSONTokener jsonParser = new JSONTokener(jsonStr);
            JSONObject jsonValue = (JSONObject) jsonParser.nextValue();
            String status = (String) jsonValue.get("status");
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    
}
