package com.byteera.bank.utils;

import com.byteera.bank.MyApp;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.Department;
import com.byteera.hxlib.utils.Constants;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.core.p;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EasemobUtil {

    public static List<EMGroup> getWorkingGroupWithFullInfo(boolean isSyncFromServer,
                                                            boolean isExcludeMeetingGroup)
    {
        try
        {
            List<EMGroup> allGroups = new ArrayList<>();

            if(isSyncFromServer)
            {
                Response response = getGroupsFromServer();
                JSONTokener jsonTokener = new JSONTokener(response.body().string());
                JSONObject json = new JSONObject(jsonTokener);
                List<EMGroup> remoteGroupList = new ArrayList<>();
                if(json.has("data"))
                {
                    JSONArray dataJsonArray = json.getJSONArray("data");

                    for(int i = 0; i < dataJsonArray.length(); ++i) {
                        JSONObject emGroupJsonObject = dataJsonArray.getJSONObject(i);
                        EMGroup emGroup = EasemobUtil.parseGroupFromData(emGroupJsonObject);
                        remoteGroupList.add(emGroup);
                    }
                    LogUtil.Object(remoteGroupList);

                    EasemobUtil.syncGroupsWithRemoteGroupList(remoteGroupList);
                }


                for (int i = 0; i < remoteGroupList.size(); i++) {
                    EMGroup group = EMGroupManager.getInstance().getGroupFromServer(remoteGroupList.get(i).getGroupId());

                    if (group == null) {
                        group = EMGroupManager.getInstance().getGroup(remoteGroupList.get(i).getGroupId());
                    }
                    else {
                        EMGroupManager.getInstance().createOrUpdateLocalGroup(group);
                    }

                    if (group != null) {
                        allGroups.add(group);
                    }
                }
            }
            else
            {
                allGroups = EMGroupManager.getInstance().getAllGroups();
            }

            if(!isExcludeMeetingGroup)
                return allGroups;

            final List<EMGroup> resultGroups = new ArrayList<>();

            for (int i = 0; i < allGroups.size(); i++)
            {
                if (allGroups.get(i).getDescription().equals("ZJSD_WORK_GROUP"))
                {
                    resultGroups.add(allGroups.get(i));
                }
            }

            return resultGroups;
        }
        catch (Exception ex)
        {
            LogUtil.e(ex, "GetUserGroupException");
        }

        return new ArrayList<>();
    }

    public static synchronized Response getGroupsFromServer() throws Exception {
            return getGroupsFromRestServer(true);
    }

    public static void syncGroupsWithRemoteGroupList(List<EMGroup> remoteGroupList) {
        Iterator remoteGroupListIterator = remoteGroupList.iterator();
        while(remoteGroupListIterator.hasNext()) {
            EMGroup emGroup = (EMGroup)remoteGroupListIterator.next();
            EMGroupManager.getInstance().createOrUpdateLocalGroup(emGroup);
        }

        List<EMGroup> localGroupList = EMGroupManager.getInstance().getAllGroups();

        for(EMGroup localGroup:localGroupList)
        {
            boolean isExistOnServer = false;
            for(EMGroup remoteGroup:remoteGroupList)
            {
                if(remoteGroup.getGroupId().equals(localGroup.getGroupId()))
                {
                    isExistOnServer = true;
                    break;
                }
            }

            if(!isExistOnServer)
            {
                EMGroupManager.getInstance().deleteLocalGroup(localGroup.getGroupId());
            }
        }
    }

    private static Response getGroupsFromRestServer(boolean isNeedDetail) throws Exception {
        String url = p.a().I() + "/users/" + EMChatManager.getInstance().getCurrentUser()
                + "/joined_chatgroups";

        if(isNeedDetail) {
            url = url + "?detail=true";
        }

        String token = p.a().v();

        RequestParams params = new RequestParams();
        params.setHeader("Authorization", String.format("Bearer %s", token));

        return MyhttpUtils.getInstance().send(HttpRequest.HttpMethod.GET, url, params);
    }

    public static EMGroup parseGroupFromData(JSONObject jsonObject) throws JSONException {

        String groupId = jsonObject.getString("groupid");
        String groupName = jsonObject.getString("groupname");
        EMGroup emGroup = new EMGroup(groupId);
        emGroup.setGroupName(groupName);

        try
        {
            if(jsonObject.has("owner")) {
                emGroup.setOwner(jsonObject.getString("owner"));
            }

            if(jsonObject.has("membersonly")) {
                Field field = EMGroup.class.getDeclaredField("membersOnly");
                field.setAccessible(true);
                field.set(emGroup, jsonObject.getBoolean("membersonly"));
            }

            if(jsonObject.has("allowinvites")) {
                Field field = EMGroup.class.getDeclaredField("allowInvites");
                field.setAccessible(true);
                field.set(emGroup, jsonObject.getBoolean("allowinvites"));
            }

            if(jsonObject.has("public")) {
                Field field = EMGroup.class.getDeclaredField("isPublic");
                field.setAccessible(true);
                field.set(emGroup, jsonObject.getBoolean("public"));
            }

            if(jsonObject.has("member")) {
                ArrayList<String> var6 = new ArrayList<>();
                JSONArray var7 = jsonObject.getJSONArray("member");

                for(int var8 = 0; var8 < var7.length(); ++var8) {
                    String var9 = var7.getString(var8);
                    var6.add(var9);
                }

                emGroup.setMembers(var6);
            }
        }
        catch (Exception ex)
        {
            LogUtil.e(ex, "Parse EMGroup exception");
        }

        return emGroup;
    }

    public static void getUserGroup() {
        final List<EMGroup> resultGroups = EasemobUtil
                .getWorkingGroupWithFullInfo(true, true);

        if (resultGroups == null) {
            LogUtil.e("Get working group failed");
            return;
        }

        LogUtil.d("Group list count: %d", resultGroups.size());

        MyApp.getInstance().setGroupList(resultGroups);
    }

    /**
     * 更新联系人列表
     */
    public static void updateUserList() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", MyApp.getInstance().getToken());
        params.addQueryStringParameter("version", MyApp.getInstance().getVersion());   //本地用户列表版本

        try {
            Response response = MyhttpUtils.getInstance().send(HttpRequest.HttpMethod.GET, Constants.BYTEERA_SERVICE + "syncuser?", params);
            if (response != null) {
                String result = response.body().string();
                parseUpdateJson(result);
            }
        } catch (Exception e) {
            LogUtil.e(e, "Update user list failed");
        }
    }


    private static void parseUpdateJson(String result) throws JSONException {
        JSONTokener jsonTokener = new JSONTokener(result);
        JSONObject json = new JSONObject(jsonTokener);
        String error = json.optString("error");
        String error_description = json.optString("error_description");
        JSONObject data = json.optJSONObject("data");
        String version = data.optString("version");

        LogUtil.d("remote version: %s, local version: %s", version, MyApp.getInstance().getVersion());
        if ("0".equals(error)) {
            MyApp.getInstance().setVersion(version);
            JSONObject diff = data.optJSONObject("diff");

            LogUtil.d("SyncUser, add: %d, modify: %d, delete: %d", diff.optJSONArray("add").length(),
                    diff.optJSONArray("modify").length(), diff.optJSONArray("delete").length());

            if (diff.optJSONArray("add").length() >= 1) {
                long startTime = System.currentTimeMillis();
                JSONArray jsonUserArray = diff.optJSONArray("add");
                DBManager.getInstance(MyApp.getInstance()).saveUserList(jsonUserArray, false);
                long endTime = System.currentTimeMillis();
                long diffTime = endTime - startTime;
                LogUtil.d("新增人员成功，总数：%d，耗时:%d秒", jsonUserArray.length(), diffTime / 1000);
            } else if (diff.optJSONArray("modify").length() >= 1) {
                JSONArray jsonArray = diff.optJSONArray("modify");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    if (!jsonObject.has("user_id")) {
                        LogUtil.e("Invalid user info, json: %s", jsonObject.toString());
                    }

                    String user_id = jsonObject.getString("user_id");

                    if (jsonObject.has("head_photo")) {
                        String avatar = jsonObject.optString("head_photo");
                        DBManager.getInstance(MyApp.getInstance()).updateUserInfo(user_id,
                                null, null, null, null, avatar, null, null, null,
                                null, null, null);
                        LogUtil.d("更新头像成功, UserId:%s, Avatar:%s", user_id, avatar);
                    }

                    if (jsonObject.has("email")) {
                        String email = jsonObject.optString("email");
                        DBManager.getInstance(MyApp.getInstance()).updateUserInfo(user_id,
                                null, null, null, null, null, email, null, null,
                                null, null, null);
                        LogUtil.d("更新email成功, UserId:%s, Email:%s", user_id, email);
                    }

                    if (jsonObject.has("mobil_phone")) {
                        String mobile = jsonObject.getString("mobil_phone");
                        DBManager.getInstance(MyApp.getInstance()).updateUserInfo(user_id,
                                null, mobile, null, null, null, null, null, null,
                                null, null, null);
                        LogUtil.d("更新mobile成功, UserId:%s, Mobile:%s", user_id, mobile);
                    }

                    if (jsonObject.has("phone")) {
                        String phone = jsonObject.getString("phone");
                        DBManager.getInstance(MyApp.getInstance()).updateUserInfo(user_id,
                                null, null, null, null, null, null, null, phone,
                                null, null, null);
                        LogUtil.d("更新phone成功, UserId:%s, Phone:%s", user_id, phone);
                    }

                    if (jsonObject.has("depart")) {
                        String depart = jsonObject.getString("depart");
                        DBManager.getInstance(MyApp.getInstance()).updateUserInfo(user_id,
                                null, null, null, null, null, null, null, null,
                                depart, null, null);
                        LogUtil.d("更新depart成功, UserId:%s, Depart:%s", user_id, depart);
                    }

                    if (jsonObject.has("opnum")) {
                        String opNum = jsonObject.getString("opnum");
                        DBManager.getInstance(MyApp.getInstance()).updateUserInfo(user_id,
                                opNum, null, null, null, null, null, null, null,
                                null, null, null);
                        LogUtil.d("更新opNum成功, UserId:%s, opNum:%s", user_id, opNum);
                    }
                }
            } else if (diff.optJSONArray("delete").length() >= 1) {
                JSONArray jsonAdd = diff.optJSONArray("delete");
                for (int i = 0; i < jsonAdd.length(); i++) {
                    final String user_id = jsonAdd.get(i).toString();
                    DBManager.getInstance(MyApp.getInstance()).deleteContactByUserId(user_id);
                    LogUtil.d("删除人员" + user_id);
                }
            }
        }
    }


    public static void getDepartmentList() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("access_token", MyApp.getInstance().getToken());

        try {
            Response response = MyhttpUtils.getInstance().send(HttpRequest.HttpMethod.GET, Constants.BYTEERA_SERVICE + "displayorder?", params);
            if (response != null) {
                String result = response.body().string();
                JSONTokener jsonTokener = new JSONTokener(result);
                JSONObject json = new JSONObject(jsonTokener);
                String error = json.optString("error");
                String error_description = json.optString("error_description");

                if ("0".equals(error)) {
                    JSONObject data = json.optJSONObject("data");
                    Iterator<?> keys = data.keys();
                    List<Department> departmentList = new ArrayList<>();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        String value = (String) data.get(key);

                        Department department = new Department();
                        department.setDepartmentName(key);
                        department.setOrderId(value);
                        departmentList.add(department);
                    }

                    DBManager.getInstance(MyApp.getInstance()).saveDepartmentList(departmentList);
                } else {
                    ToastUtil.showToastText("获取部门列表错误！");
                    LogUtil.d("获取部门列表错误, %s", error_description);
                }
            }
        } catch (Exception e) {
            LogUtil.e(e, "Get department list failed");
        }

    }
}
