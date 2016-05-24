package com.byteera.bank.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.byteera.bank.domain.Announcement;
import com.byteera.bank.domain.Department;
import com.byteera.bank.domain.FriendRelationShip;
import com.byteera.bank.domain.RobotUser;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.StringUtil;
import com.easemob.util.HanziToPinyin;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {
    static private DBManager dbMgr = null;
    private DbOpenHelper dbHelper;

    private DBManager(Context context){
        dbHelper = DbOpenHelper.getInstance(context);
    }

    public static synchronized DBManager getInstance(Context context){
        if(dbMgr == null)
        {
            dbMgr = new DBManager(context);
        }
        return dbMgr;
    }

    synchronized public boolean isExistUserList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db.isOpen()) {
            String sql = String.format("select 1 from %s limit 1", UserDao.TABLE_NAME);
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    return true;
                }
                cursor.close();
            }
        }
        return false;
    }

    synchronized public User getUserByUserId(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        User user = null;
        if (db.isOpen()) {
            String sql = String.format("select * from %s where %s='%s'",
                    UserDao.TABLE_NAME, UserDao.COLUMN_USER_ID, userId);

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_AVATAR));
                String depart = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_DEPARTMENT));
                String email = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EMAIL));
                String mobile = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_MOBILE));
                String nickName = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NICK_NAME));
                String opNum = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_OP_NUM));
                String sex = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_SEX));
                String tel = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_TEL));
                String easemobId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EASEMOB_ID));
                String firstLetter = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_FIRST_LETTER));
                String pinyin = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_PINYIN));

                user = new User(userId, opNum, mobile, nickName, sex, avatar, email, easemobId, tel,
                        depart, firstLetter, pinyin);
            }
            cursor.close();
        }
        return user;
    }

    synchronized public User getUserByEasemobId(String easemobId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        User user = null;
        if (db.isOpen()) {
            String sql = String.format("select * from %s where %s='%s'",
                        UserDao.TABLE_NAME, UserDao.COLUMN_EASEMOB_ID, easemobId);

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_AVATAR));
                String depart = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_DEPARTMENT));
                String email = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EMAIL));
                String mobile = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_MOBILE));
                String nickName = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NICK_NAME));
                String opNum = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_OP_NUM));
                String sex = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_SEX));
                String tel = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_TEL));
                String userId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_USER_ID));
                String firstLetter = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_FIRST_LETTER));
                String pinyin = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_PINYIN));

                user = new User(userId, opNum, mobile, nickName, sex, avatar, email, easemobId, tel,
                        depart, firstLetter, pinyin);
            }
            cursor.close();
        }
        return user;
    }

    synchronized public void saveDepartmentList(List<Department> departmentList){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if(departmentList.size() > 0)
            db.delete(DepartmentDao.TABLE_NAME, null, null);

        try{
            db.beginTransaction();
            for(Department department: departmentList){
                ContentValues contentValues=new ContentValues();
                contentValues.put(DepartmentDao.COLUMN_DEPARTMENT_NAME, department.getDepartmentName());
                contentValues.put(DepartmentDao.COLUMN_ORDER_ID, department.getOrderId());
                db.insert(DepartmentDao.TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
            LogUtil.d("Add department count: %d", departmentList.size());
        }
        catch (Exception ex){
            LogUtil.e(ex, "saveDepartmentList Exception");
        }
        finally {
            db.endTransaction();
        }
    }

    synchronized public int getAllUserCount()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int totalCount = 0;
        if (db.isOpen()) {
            String sql = String.format("select count(%s) from %s", UserDao.COLUMN_USER_ID,
                    UserDao.TABLE_NAME);

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                totalCount = cursor.getInt(0);
            }
            cursor.close();
        }

        return totalCount;
    }

    synchronized public void saveUserList(JSONArray jsonUserArray, boolean isTruncateTable){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if(isTruncateTable && jsonUserArray.length() > 0)
            db.delete(UserDao.TABLE_NAME, null, null);

        try{
            db.beginTransaction();

            for (int i = 0; i < jsonUserArray.length(); i++) {
                JSONObject user = (JSONObject) jsonUserArray.get(i);
                String user_id = user.optString("user_id");
                String opnum = user.optString("opnum");
                String mobile_phone = user.optString("mobile_phone");
                String username = user.optString("name");
                String sex = user.optString("sex");
                String head_photo = user.optString("head_photo");
                String email = user.optString("email");
                String huanxin = user.optString("huanxin");
                String phone = user.optString("phone");
                String depart = user.optString("depart");

                if (StringUtils.isBlank(user_id) || StringUtils.isBlank(username) ||
                        StringUtils.isBlank(sex)) {
                    continue;
                }

                String ease_username = huanxin.substring(0, huanxin.lastIndexOf(":"));
                String pinyin = StringUtil.getPinYin(username);
                String firstLetter = pinyin.substring(0, 1);

                ContentValues contentValues=new ContentValues();
                contentValues.put(UserDao.COLUMN_AVATAR, head_photo);
                contentValues.put(UserDao.COLUMN_DEPARTMENT, depart);
                contentValues.put(UserDao.COLUMN_EASEMOB_ID, ease_username);
                contentValues.put(UserDao.COLUMN_EMAIL, email);
                contentValues.put(UserDao.COLUMN_MOBILE, mobile_phone);
                contentValues.put(UserDao.COLUMN_NICK_NAME, username);
                contentValues.put(UserDao.COLUMN_OP_NUM, opnum);
                contentValues.put(UserDao.COLUMN_SEX, sex);
                contentValues.put(UserDao.COLUMN_TEL, phone);
                contentValues.put(UserDao.COLUMN_USER_ID, user_id);
                contentValues.put(UserDao.COLUMN_FIRST_LETTER, firstLetter);
                contentValues.put(UserDao.COLUMN_PINYIN, pinyin);
                db.insert(UserDao.TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
            LogUtil.d("Add userList count: %d", jsonUserArray.length());
        }
        catch (Exception ex){
            LogUtil.e(ex, "saveUserList Exception");
        }
        finally {
            db.endTransaction();
        }
    }

    synchronized public List<Announcement> getAnnouncementList(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Announcement> announcementList = new ArrayList<>();
        if (db.isOpen()) {
            String sql = String.format("select * from %s order by %s desc",
                    AnnouncementDao.TABLE_NAME, AnnouncementDao.COLUMN_TIMESTAMP);

            Cursor cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(AnnouncementDao.COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(AnnouncementDao.COLUMN_CONTENT));
                long timestamp = cursor.getLong(cursor.getColumnIndex(AnnouncementDao.COLUMN_TIMESTAMP));

                Announcement announcement = new Announcement(title, timestamp, content);

                announcementList.add(announcement);
            }
            cursor.close();
        }
        return announcementList;
    }

    synchronized public Announcement getLastAnnouncement(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Announcement announcement = null;
        if (db.isOpen()) {
            String sql = String.format("select * from %s order by %s desc limit 1", AnnouncementDao.TABLE_NAME,
                    AnnouncementDao.COLUMN_TIMESTAMP);

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(AnnouncementDao.COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(AnnouncementDao.COLUMN_CONTENT));
                long timestamp = cursor.getLong(cursor.getColumnIndex(AnnouncementDao.COLUMN_TIMESTAMP));

                announcement = new Announcement(title, timestamp, content);
            }
            cursor.close();
        }
        return announcement;
    }

    synchronized public void addAnnouncement(String title, String content, long timestamp)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AnnouncementDao.COLUMN_TITLE, title);
            contentValues.put(AnnouncementDao.COLUMN_CONTENT, content);
            contentValues.put(AnnouncementDao.COLUMN_TIMESTAMP, timestamp);
            db.insert(AnnouncementDao.TABLE_NAME, null, contentValues);
        }
    }

    synchronized public List<User> getUserListInDepartment(String departmentPath){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<User> userList = new ArrayList<>();
        if (db.isOpen()) {
            String sql = String.format("select * from %s where %s = '%s' order by %s",
                        UserDao.TABLE_NAME, UserDao.COLUMN_DEPARTMENT, departmentPath,
                    UserDao.COLUMN_PINYIN);

            Cursor cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String nickName = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NICK_NAME));
                String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_AVATAR));
                String department = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_DEPARTMENT));
                String easemobId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EASEMOB_ID));
                String email = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EMAIL));
                String mobile = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_MOBILE));
                String opNum = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_OP_NUM));
                String sex = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_SEX));
                String tel = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_TEL));
                String userId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_USER_ID));
                String firstLetter = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_FIRST_LETTER));
                String pinyin = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_PINYIN));

                User user = new User(userId, opNum, mobile, nickName, sex, avatar, email, easemobId,
                        tel, department, firstLetter, pinyin);
                userList.add(user);
            }
            cursor.close();
        }
        return userList;
    }

    synchronized public FriendRelationShip getFriendShipBySrcUser(String srcUser){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        FriendRelationShip friendRelationShip = null;
        if (db.isOpen()) {
            String sql = String.format("select * from %s where %s='%s'",
                    FriendshipDao.TABLE_NAME, FriendshipDao.COLUMN_SRC_USER, srcUser);

            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToNext())
            {
                int mode = cursor.getInt(cursor.getColumnIndex(FriendshipDao.COLUMN_MODE));
                int result = cursor.getInt(cursor.getColumnIndex(FriendshipDao.COLUMN_RESULT));

                friendRelationShip = new FriendRelationShip(srcUser, mode, result);
            }
            cursor.close();
        }
        return friendRelationShip;
    }

    synchronized public void AddFriendship(String srcUser, int mode, int result)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FriendshipDao.COLUMN_RESULT, result);
            contentValues.put(FriendshipDao.COLUMN_SRC_USER, srcUser);
            contentValues.put(FriendshipDao.COLUMN_MODE, mode);
            db.insertWithOnConflict(FriendshipDao.TABLE_NAME, null, contentValues,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    synchronized public void UpdateFriendship(String srcUser, int result){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendshipDao.COLUMN_RESULT, result);

        if(db.isOpen())
        {
            db.update(FriendshipDao.TABLE_NAME, contentValues,
                    FriendshipDao.COLUMN_SRC_USER + "=?", new String[]{srcUser});
        }
    }

    synchronized public List<FriendRelationShip> getAllFriendShip(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<FriendRelationShip> friendRelationShipList = new ArrayList<>();
        if (db.isOpen()) {
            String sql = String.format("select * from %s", FriendshipDao.TABLE_NAME);

            Cursor cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext())
            {
                String srcUser = cursor.getString(cursor.getColumnIndex(FriendshipDao.COLUMN_SRC_USER));
                int mode = cursor.getInt(cursor.getColumnIndex(FriendshipDao.COLUMN_MODE));
                int result = cursor.getInt(cursor.getColumnIndex(FriendshipDao.COLUMN_RESULT));

                FriendRelationShip friendRelationShip = new FriendRelationShip(srcUser, mode, result);
                friendRelationShipList.add(friendRelationShip);
            }
            cursor.close();
        }
        return friendRelationShipList;
    }

    synchronized public void deleteAllFriendShip(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen())
        {
            db.delete(FriendshipDao.TABLE_NAME, null, null);
        }
    }

    synchronized public List<User> getAllUserList() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<User> userList = new ArrayList<>();
        if (db.isOpen()) {
            String sql = String.format("select * from %s order by %s", UserDao.TABLE_NAME,
                    UserDao.COLUMN_PINYIN);

            Cursor cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_AVATAR));
                String depart = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_DEPARTMENT));
                String email = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EMAIL));
                String mobile = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_MOBILE));
                String nickName = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NICK_NAME));
                String opNum = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_OP_NUM));
                String sex = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_SEX));
                String tel = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_TEL));
                String userId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_USER_ID));
                String easemobId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EASEMOB_ID));
                String firstLetter = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_FIRST_LETTER));
                String pinyin = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_PINYIN));

                User user = new User(userId, opNum, mobile, nickName, sex, avatar, email, easemobId,
                        tel, depart, firstLetter, pinyin);
                userList.add(user);
            }
            cursor.close();
        }
        return userList;
    }

    synchronized public List<Department> getDepartmentList(String parentCode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Department> departmentList = new ArrayList<>();
        if (db.isOpen()) {
            String sql;

            if(StringUtils.isEmpty(parentCode))
            {
                sql = String.format("select * from %s where LENGTH(%s) = 3 order by %s",
                        DepartmentDao.TABLE_NAME, DepartmentDao.COLUMN_ORDER_ID, DepartmentDao.COLUMN_ORDER_ID);
            }
            else {
                sql = "select * from ${table} where ${order_id} LIKE '${parent_code}%' AND " +
                        "LENGTH(${order_id}) > ${min_len} AND LENGTH(${order_id}) <= ${max_len} " +
                        "order by ${order_id}";

                Map valuesMap = new HashMap();
                valuesMap.put("table", DepartmentDao.TABLE_NAME);
                valuesMap.put("order_id", DepartmentDao.COLUMN_ORDER_ID);
                valuesMap.put("parent_code", parentCode);
                valuesMap.put("min_len", parentCode.length());
                valuesMap.put("max_len", parentCode.length() + 3);

                StrSubstitutor sub = new StrSubstitutor(valuesMap);

                sql = sub.replace(sql);
            }

            Cursor cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String departmentName = cursor.getString(cursor.getColumnIndex(DepartmentDao.COLUMN_DEPARTMENT_NAME));
                String orderId = cursor.getString(cursor.getColumnIndex(DepartmentDao.COLUMN_ORDER_ID));
                Department department = new Department();
                department.setDepartmentName(departmentName);
                department.setOrderId(orderId);
                departmentList.add(department);
            }
            cursor.close();
        }
        return departmentList;
    }


    /**
     * 获取好友list
     * 
     * @return
     */
    synchronized public Map<String, User> getContactList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, User> users = new HashMap<String, User>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + UserDao.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_EASEMOB_ID));
                String nick = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NICK_NAME));

                User user = new User();
                user.setUsername(username);
                user.setNick(nick);

                users.put(username, user);
            }
            cursor.close();
        }
        return users;
    }
    
    /**
     * 删除一个联系人
     * @param easemobId
     */
    synchronized public void deleteContactByEasemobId(String easemobId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(UserDao.TABLE_NAME, UserDao.COLUMN_EASEMOB_ID + " = ?", new String[]{easemobId});
        }
    }

    synchronized public void deleteContactByUserId(String userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(UserDao.TABLE_NAME, UserDao.COLUMN_USER_ID + " = ?", new String[]{userId});
        }
    }
    
    /**
     * 保存一个联系人
     * @param user
     */
    synchronized public void saveContact(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDao.COLUMN_EASEMOB_ID, user.getUsername());
        if(user.getNick() != null)
            values.put(UserDao.COLUMN_NICK_NAME, user.getNick());
        if(user.getAvatar() != null)
            values.put(UserDao.COLUMN_AVATAR, user.getAvatar());
        if(db.isOpen()){
            db.replace(UserDao.TABLE_NAME, null, values);
        }
    }
    
    public void setDisabledGroups(List<String> groups){
        setList(UserDao.COLUMN_NAME_DISABLED_GROUPS, groups);
    }
    
    public List<String>  getDisabledGroups(){       
        return getList(UserDao.COLUMN_NAME_DISABLED_GROUPS);
    }
    
    public void setDisabledIds(List<String> ids){
        setList(UserDao.COLUMN_NAME_DISABLED_IDS, ids);
    }
    
    public List<String> getDisabledIds(){
        return getList(UserDao.COLUMN_NAME_DISABLED_IDS);
    }
    
    synchronized private void setList(String column, List<String> strList){
        StringBuilder strBuilder = new StringBuilder();
        
        for(String hxid:strList){
            strBuilder.append(hxid).append("$");
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(column, strBuilder.toString());

            db.insertWithOnConflict(UserDao.PREF_TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    synchronized public void updateUserInfo(String userId, String opNum, String mobile,
                                              String username, String sex, String avatar,
                                              String email, String easemobId, String tel,
                                              String department, String firstLetter, String pinYin){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues contentValues = new ContentValues();
        if(!StringUtils.isEmpty(firstLetter))
            contentValues.put(UserDao.COLUMN_FIRST_LETTER, firstLetter);

        if(!StringUtils.isEmpty(opNum))
            contentValues.put(UserDao.COLUMN_OP_NUM, opNum);

        if(!StringUtils.isEmpty(avatar))
            contentValues.put(UserDao.COLUMN_AVATAR, avatar);

        if(!StringUtils.isEmpty(department))
            contentValues.put(UserDao.COLUMN_DEPARTMENT, department);

        if(!StringUtils.isEmpty(easemobId))
            contentValues.put(UserDao.COLUMN_EASEMOB_ID, easemobId);

        if(!StringUtils.isEmpty(email))
            contentValues.put(UserDao.COLUMN_EMAIL, email);

        if(!StringUtils.isEmpty(mobile))
            contentValues.put(UserDao.COLUMN_MOBILE, mobile);

        if(!StringUtils.isEmpty(username))
            contentValues.put(UserDao.COLUMN_NICK_NAME, username);

        if(!StringUtils.isEmpty(sex))
            contentValues.put(UserDao.COLUMN_SEX, sex);

        if(!StringUtils.isEmpty(tel))
            contentValues.put(UserDao.COLUMN_TEL, tel);

        if(!StringUtils.isEmpty(pinYin))
            contentValues.put(UserDao.COLUMN_PINYIN, pinYin);

        if(db.isOpen())
        {
            db.update(UserDao.TABLE_NAME, contentValues, UserDao.COLUMN_USER_ID + "=?", new String[] {userId});
        }
    }

    synchronized public void updateUserAvatar(String userId, String avatar){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(UserDao.COLUMN_AVATAR, avatar);

        if(db.isOpen())
        {
            db.update(UserDao.TABLE_NAME, contentValues, UserDao.COLUMN_USER_ID + "=?", new String[] {userId});
        }
    }


    synchronized private List<String> getList(String column){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + column + " from " + UserDao.PREF_TABLE_NAME,null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String strVal = cursor.getString(0);
        if (strVal == null || strVal.equals("")) {
            return null;
        }
        
        cursor.close();
        
        String[] array = strVal.split("$");
        
        if(array != null && array.length > 0){
            List<String> list = new ArrayList<String>();
            for(String str:array){
                list.add(str);
            }
            
            return list;
        }
        
        return null;
    }

    synchronized public void closeDB(){
        if(dbHelper != null){
            dbHelper.closeDB();
        }
    }
    
    
    /**
     * Save Robot list
     */
	synchronized public void saveRobotList(List<RobotUser> robotList) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db.isOpen()) {
			db.delete(UserDao.ROBOT_TABLE_NAME, null, null);
			for (RobotUser item : robotList) {
				ContentValues values = new ContentValues();
				values.put(UserDao.ROBOT_COLUMN_NAME_ID, item.getUsername());
				if (item.getNick() != null)
					values.put(UserDao.ROBOT_COLUMN_NAME_NICK, item.getNick());
				if (item.getAvatar() != null)
					values.put(UserDao.ROBOT_COLUMN_NAME_AVATAR, item.getAvatar());
				db.replace(UserDao.ROBOT_TABLE_NAME, null, values);
			}
		}
	}
    
    /**
     * load robot list
     */
	synchronized public Map<String, RobotUser> getRobotList() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Map<String, RobotUser> users = null;
		if (db.isOpen()) {
			Cursor cursor = db.rawQuery("select * from " + UserDao.ROBOT_TABLE_NAME, null);
			if(cursor.getCount()>0){
				users = new HashMap<String, RobotUser>();
			};
			while (cursor.moveToNext()) {
				String username = cursor.getString(cursor.getColumnIndex(UserDao.ROBOT_COLUMN_NAME_ID));
				String nick = cursor.getString(cursor.getColumnIndex(UserDao.ROBOT_COLUMN_NAME_NICK));
				String avatar = cursor.getString(cursor.getColumnIndex(UserDao.ROBOT_COLUMN_NAME_AVATAR));
				RobotUser user = new RobotUser();
				user.setUsername(username);
				user.setNick(nick);
				user.setAvatar(avatar);
				String headerName = null;
				if (!TextUtils.isEmpty(user.getNick())) {
					headerName = user.getNick();
				} else {
					headerName = user.getUsername();
				}
				if(Character.isDigit(headerName.charAt(0))){
					user.setHeader("#");
				}else{
					user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target
							.substring(0, 1).toUpperCase());
					char header = user.getHeader().toLowerCase().charAt(0);
					if (header < 'a' || header > 'z') {
						user.setHeader("#");
					}
				}
				
				users.put(username, user);
			}
			cursor.close();
		}
		return users;
	}

    synchronized public boolean isTableExists(String tableName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.close();
                    return true;
                }
                cursor.close();
            }
        }
        return false;
    }
}