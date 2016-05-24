package com.byteera.hxlib.utils;

import com.byteera.bank.MyApp;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    /******************** 定义服务器接口 begin **********************/
    public static String BYTEERA_SERVICE = MyApp.getInstance().getLogicServerHttp()+ "/";
    public static final String CACHE_DIR = "/hzbank/";

    public static String BYTEERA_SERVICE_CONTACT = BYTEERA_SERVICE + "/user";

    public static int RESULT_LOAD_IMAGE = 101;
    public static int CLIP_PHOTO = 111;
    public static int FROM_ALBUM = 112;
    public static int TAKE_PICTURE = 113;
    public static int PHOTO_NUM = 9;

    public static boolean isRecieveMsgNotify = false;

    public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
    public static final String GROUP_USERNAME = "item_groups";
    public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
    public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";
    public static final String ACCOUNT_REMOVED = "account_removed";
    public static final String CMD_BROADCAST = "com.byteera.cmd";
    public static final String CMD_BROADCAST_ADDFRIEND = "com.byteera.cmd.addfriend";
    public static final List<String> friendList = new ArrayList<>();
}
