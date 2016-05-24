package com.byteera.hxlib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.byteera.bank.domain.User;

import org.apache.commons.lang3.StringUtils;

public class HXPreferenceUtils {
	/**
	 * 保存Preference的name
	 */
	public static final String PREFERENCE_NAME = "saveInfo";
	private static SharedPreferences mSharedPreferences;
	private static HXPreferenceUtils mPreferenceUtils;
	private static SharedPreferences.Editor editor;

	private String SHARED_KEY_SETTING_NOTIFICATION = "shared_key_setting_notification";
	private String SHARED_KEY_SETTING_SOUND = "shared_key_setting_sound";
	private String SHARED_KEY_SETTING_VIBRATE = "shared_key_setting_vibrate";
	private String SHARED_KEY_SETTING_SPEAKER = "shared_key_setting_speaker";

	private static String SHARED_KEY_SETTING_CHATROOM_OWNER_LEAVE = "shared_key_setting_chatroom_owner_leave";
	private static String SHARED_KEY_SETTING_GROUPS_SYNCED = "SHARED_KEY_SETTING_GROUPS_SYNCED";
	private static String SHARED_KEY_SETTING_CONTACT_SYNCED = "SHARED_KEY_SETTING_CONTACT_SYNCED";
	private static String SHARED_KEY_SETTING_BALCKLIST_SYNCED = "SHARED_KEY_SETTING_BALCKLIST_SYNCED";

	//bank
	private String SHARED_KEY_TOKEN = "shared_key_token";
	private String SHARED_KEY_VERSION = "shared_key_version";
	private String SHARED_KEY_HEADAVATOR = "shared_key_headavator";
	private String SHARED_KEY_USERNAME = "shared_key_username";
    private String SHARED_KEY_PASSWORD = "shared_key_password";
	private String SHARED_KEY_USER_INFO = "shared_key_user_info";
	private String SHARED_KEY_HUANXINGROUPID = "huanxingroupid";
	private String SHARED_KEY_TAG = "tag";


	//私有部署
	private String SHARED_KEY_SERVER1 = "shared_key_server1";
	private String SHARED_KEY_SERVER2 = "shared_key_server2";


	private HXPreferenceUtils(Context cxt) {
		mSharedPreferences = cxt.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}

	public static synchronized void init(Context cxt){
		if(mPreferenceUtils == null){
			mPreferenceUtils = new HXPreferenceUtils(cxt);
		}
	}

	public static HXPreferenceUtils getInstance() {
		if (mPreferenceUtils == null) {
			throw new RuntimeException("please init first!");
		}
		return mPreferenceUtils;
	}

	public void setSettingMsgNotification(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_NOTIFICATION, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgNotification() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_NOTIFICATION, true);
	}

	public void setSettingMsgSound(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_SOUND, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgSound() {

		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_SOUND, true);
	}

	public void setSettingMsgVibrate(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_VIBRATE, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgVibrate() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_VIBRATE, true);
	}

	public void setSettingMsgSpeaker(boolean paramBoolean) {
		editor.putBoolean(SHARED_KEY_SETTING_SPEAKER, paramBoolean);
		editor.commit();
	}

	public boolean getSettingMsgSpeaker() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_SPEAKER, true);
	}

	public void setSettingAllowChatroomOwnerLeave(boolean value) {
		editor.putBoolean(SHARED_KEY_SETTING_CHATROOM_OWNER_LEAVE, value);
		editor.commit();
	}

	public boolean getSettingAllowChatroomOwnerLeave() {
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_CHATROOM_OWNER_LEAVE, true);
	}

	public void setGroupsSynced(boolean synced){
		editor.putBoolean(SHARED_KEY_SETTING_GROUPS_SYNCED, synced);
		editor.commit();
	}

	public boolean isGroupsSynced(){
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_GROUPS_SYNCED, false);
	}

	public void setContactSynced(boolean synced){
		editor.putBoolean(SHARED_KEY_SETTING_CONTACT_SYNCED, synced);
		editor.commit();
	}

	public boolean isContactSynced(){
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_CONTACT_SYNCED, false);
	}

	public void setBlacklistSynced(boolean synced) {
		editor.putBoolean(SHARED_KEY_SETTING_BALCKLIST_SYNCED, synced);
		editor.commit();
	}

	public boolean isBacklistSynced(){
		return mSharedPreferences.getBoolean(SHARED_KEY_SETTING_BALCKLIST_SYNCED, false);
	}

	/************************************************************/

	public void setToken(String token){
		editor.putString(SHARED_KEY_TOKEN, token).commit();
	}

	public String getToken(){
		return mSharedPreferences.getString(SHARED_KEY_TOKEN, "");
	}


	public void setVersion(String version){
		editor.putString(SHARED_KEY_VERSION, version).commit();
	}

	public String getVersion(){
		return mSharedPreferences.getString(SHARED_KEY_VERSION, "0");
	}



	public void setHeadAvator(String avator_path){
		editor.putString(SHARED_KEY_HEADAVATOR, avator_path).commit();
	}

	public String getHeadAvator(){
		return mSharedPreferences.getString(SHARED_KEY_HEADAVATOR, "");
	}


	public void setUsername(String username){
		editor.putString(SHARED_KEY_USERNAME, username).commit();
	}

	public String getUsername(){
		return mSharedPreferences.getString(SHARED_KEY_USERNAME, "");
	}


	public void setUserInfo(User user)
	{
        if(user == null)
        {
            editor.remove(SHARED_KEY_USER_INFO).commit();
            return;
        }
		editor.putString(SHARED_KEY_USER_INFO, user.toString()).commit();
	}

	public User getUserInfo()
	{
		String jsonUserInfo = mSharedPreferences.getString(SHARED_KEY_USER_INFO, "");
		if(StringUtils.isEmpty(jsonUserInfo))
			return new User();

		return User.loadFromJson(jsonUserInfo);
	}

    public void setPassword(String password) {
        editor.putString(SHARED_KEY_PASSWORD, password).commit();
    }

    public String getPassword() {
        return mSharedPreferences.getString(SHARED_KEY_PASSWORD, "");
    }

	public void setHuanXinGroupId(String huanxingroupid){
		editor.putString(SHARED_KEY_HUANXINGROUPID, huanxingroupid).commit();
	}

	public String getHuanXinGroupId(){
		return mSharedPreferences.getString(SHARED_KEY_HUANXINGROUPID, "");
	}

	public void setTag(String tag){
		editor.putString(SHARED_KEY_TAG, tag).commit();
	}

	public String getTag(){
		return mSharedPreferences.getString(SHARED_KEY_TAG, "");
	}


	public void setServer1(String server){
		editor.putString(SHARED_KEY_SERVER1, server).commit();
	}

	public String getServer1(){
		return mSharedPreferences.getString(SHARED_KEY_SERVER1, "");
	}

	public void setServer2(String server){
		editor.putString(SHARED_KEY_SERVER2, server).commit();
	}

	public String getServer2(){
		return mSharedPreferences.getString(SHARED_KEY_SERVER2, "");
	}


}
