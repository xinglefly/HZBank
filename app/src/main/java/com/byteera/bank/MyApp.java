package com.byteera.bank;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.byteera.R;
import com.byteera.bank.domain.FriendRelationShip;
import com.byteera.hxlib.utils.HXPreferenceUtils;
import com.easemob.EMCallBack;
import com.easemob.chat.EMGroup;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.sender.HttpSender;

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://119.254.111.19:60084/acra-myapp/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "hzbank",
        formUriBasicAuthPassword = "hzbank2015"
)
public class MyApp extends Application {

    public static Context applicationContext;
    private static MyApp instance;
    public static String access_token = "";
    private static long mMainThreadId = -1;
    private boolean mIsCreateGroup = false;
    private List<EMGroup> mGroupList = new ArrayList<>();
    private int mBadgerCount = 0;

    /** 当前用户nickname,为了苹果推送不是userid而是昵称 */
    public static String currentUserNick = "";
    public static BankHXSDKHelper hxSDKHelper = new BankHXSDKHelper();
    private List<FriendRelationShip> friendRequests = new ArrayList<>();

    public ExecutorService executorService =null; //ExecutorService通常根据系统资源情况灵活定义线程池大小
    int cpuNums = Runtime.getRuntime().availableProcessors();//获取当前系统的CPU 数目

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        mMainThreadId = android.os.Process.myTid();
        instance = this;
        ACRA.init(this);
        hxSDKHelper.onInit(applicationContext);

        executorService = Executors.newFixedThreadPool(cpuNums);

        setImageLoaderConfig();
    }


    public static BankHXSDKHelper getHxSdkHelper() {
        return hxSDKHelper;
    }

    /**配置imageLoader**/
    private void setImageLoaderConfig() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_avatar)
                .showImageOnFail(R.drawable.default_avatar).cacheInMemory(true).cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(instance)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
               // .writeDebugLogs() // Remove for release app
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static MyApp getInstance() {
        return instance;
    }

    /**获取当前登陆用户名*/
    public String getUserName() {
        return hxSDKHelper.getHXId();
    }

    /**获取密码*/
    public String getPassword() {
        return hxSDKHelper.getPassword();
    }

    /**设置token*/
    public String getToken() {
        return HXPreferenceUtils.getInstance().getToken();
    }

    /**设置version*/
    public String getVersion() {
        return HXPreferenceUtils.getInstance().getVersion();
    }


    /**设置用户名*/
    public void setUserName(String username) {
        hxSDKHelper.setHXId(username);
    }

    /**设置密码*/
    public void setPassword(String pwd) {
        hxSDKHelper.setPassword(pwd);
    }

    /**设置token*/
    public void setToken(String token) {
        HXPreferenceUtils.getInstance().setToken(token);
    }

    /**设置version*/
    public void setVersion(String version) {
        HXPreferenceUtils.getInstance().setVersion(version);
    }


    /***私有部署后逻辑服务器地址***/
    public String getLogicServerHttp() {
        return HXPreferenceUtils.getInstance().getServer1()+":"+HXPreferenceUtils.getInstance().getServer2();
    }



    /**退出登录,清空数据*/
    public void logout(final EMCallBack emCallBack) {
        // 先调用sdk logout，在清理app中自己的数据
        hxSDKHelper.logout(emCallBack);
    }



    /** 判断当前是否运行在主线程 */

    private static Handler mainThreadHandler = new Handler();

    public static Handler getMainThreadHandler() {
        return mainThreadHandler;
    }

    public static long getMainThreadId() {
        return mMainThreadId;
    }

    public static Handler getHandler() {
        return MyApp.getMainThreadHandler();
    }


    /** 在主线程执行runnable */
    public static boolean post(Runnable runnable) {
        return getHandler().post(runnable);
    }


    public synchronized List<FriendRelationShip> getFriendRequests() {
        return friendRequests;
    }

    public synchronized void addFriendRequests(FriendRelationShip friendRequest) {
        boolean isExist = false;
        for(FriendRelationShip request : friendRequests)
        {
            if(request.getSrcUser().equals(friendRequest.getSrcUser()))
            {
                isExist = true;
                break;
            }
        }
        if(isExist)
            return;
        friendRequests.add(friendRequest);
    }

    public synchronized void clearFriendRequest() {
        friendRequests.clear();
    }

    public synchronized int increaseBadgerCount()
    {
        return ++this.mBadgerCount;
    }

    public synchronized void clearBadgerCount()
    {
        this.mBadgerCount = 0;
    }

    public synchronized void setGroupList(List<EMGroup> groupList)
    {
        this.mGroupList.clear();
        this.mGroupList.addAll(groupList);
    }

    public synchronized List<EMGroup> getGroupList()
    {
        return this.mGroupList;
    }
}
