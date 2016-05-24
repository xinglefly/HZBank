/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.byteera.bank.db;

import android.content.Context;

import com.byteera.bank.domain.RobotUser;
import com.byteera.bank.domain.User;

import java.util.List;
import java.util.Map;

public class UserDao {

    public static final String TABLE_NAME = "users";
    public static final String COLUMN_EASEMOB_ID = "easemob_id";
    public static final String COLUMN_NICK_NAME = "nick_name";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_MOBILE = "mobile";
    public static final String COLUMN_TEL = "tel";
    public static final String COLUMN_DEPARTMENT = "department";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_OP_NUM = "op_num";
    public static final String COLUMN_SEX = "sex";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FIRST_LETTER = "first_letter";
    public static final String COLUMN_PINYIN = "pinyin";

    public static final String PREF_TABLE_NAME = "pref";
    public static final String COLUMN_NAME_DISABLED_GROUPS = "disabled_groups";
    public static final String COLUMN_NAME_DISABLED_IDS = "disabled_ids";

    public static final String ROBOT_TABLE_NAME = "robots";
    public static final String ROBOT_COLUMN_NAME_ID = "username";
    public static final String ROBOT_COLUMN_NAME_NICK = "nick";
    public static final String ROBOT_COLUMN_NAME_AVATAR = "avatar";
    private Context mContext;

    public UserDao(Context context) {
        mContext = context;
    }

    /**
     * 获取好友list
     *
     * @return
     */
    public Map<String, User> getContactList() {

        return DBManager.getInstance(mContext).getContactList();
    }

    /**
     * 删除一个联系人
     * @param username
     */
    public void deleteContact(String username) {
        DBManager.getInstance(mContext).deleteContactByEasemobId(username);
    }

    /**
     * 保存一个联系人
     * @param user
     */
    public void saveContact(User user) {
        DBManager.getInstance(mContext).saveContact(user);
    }

    public void setDisabledGroups(List<String> groups) {
        DBManager.getInstance(mContext).setDisabledGroups(groups);
    }

    public List<String> getDisabledGroups() {
        return DBManager.getInstance(mContext).getDisabledGroups();
    }

    public void setDisabledIds(List<String> ids) {
        DBManager.getInstance(mContext).setDisabledIds(ids);
    }

    public List<String> getDisabledIds() {
        return DBManager.getInstance(mContext).getDisabledIds();
    }

    public Map<String, RobotUser> getRobotUser() {
        return DBManager.getInstance(mContext).getRobotList();
    }

    public void saveRobotUser(List<RobotUser> robotList) {
        DBManager.getInstance(mContext).saveRobotList(robotList);
    }

    public User selectUser(String easemobId) {
        return DBManager.getInstance(mContext).getUserByEasemobId(easemobId);
    }
}
