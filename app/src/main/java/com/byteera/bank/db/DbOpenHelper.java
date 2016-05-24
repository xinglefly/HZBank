package com.byteera.bank.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.byteera.hxlib.controller.HXSDKHelper;

public class DbOpenHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 4;
	private static DbOpenHelper instance;

	private static final String USERNAME_TABLE_CREATE = "CREATE TABLE "
			+ UserDao.TABLE_NAME + " ("
            + UserDao.COLUMN_EASEMOB_ID +" TEXT, "
            + UserDao.COLUMN_NICK_NAME +" TEXT, "
            + UserDao.COLUMN_AVATAR +" TEXT, "
            + UserDao.COLUMN_MOBILE +" TEXT, "
			+ UserDao.COLUMN_TEL +" TEXT, "
            + UserDao.COLUMN_DEPARTMENT +" TEXT, "
            + UserDao.COLUMN_USER_ID +" TEXT PRIMARY KEY, "
            + UserDao.COLUMN_OP_NUM +" TEXT, "
            + UserDao.COLUMN_SEX +" TEXT, "
			+ UserDao.COLUMN_FIRST_LETTER +" TEXT, "
            + UserDao.COLUMN_PINYIN +" TEXT, "
            + UserDao.COLUMN_EMAIL +" TEXT);";

	private static final String DEPARTMENT_TABLE_CREATE = "CREATE TABLE "
			+ DepartmentDao.TABLE_NAME + " ("
			+ DepartmentDao.COLUMN_ORDER_ID +" TEXT, "
			+ DepartmentDao.COLUMN_DEPARTMENT_NAME + " TEXT PRIMARY KEY);";

	private static final String PREF_TABLE_CREATE = "CREATE TABLE "
			+ UserDao.PREF_TABLE_NAME + " ("
			+ UserDao.COLUMN_NAME_DISABLED_GROUPS +" TEXT, "
			+ UserDao.COLUMN_NAME_DISABLED_IDS + " TEXT);";

	private static final String ROBOT_TABLE_CREATE = "CREATE TABLE "
			+ UserDao.ROBOT_TABLE_NAME + " ("
			+ UserDao.ROBOT_COLUMN_NAME_ID +" TEXT, "
			+ UserDao.ROBOT_COLUMN_NAME_NICK +" TEXT, "
			+ UserDao.ROBOT_COLUMN_NAME_AVATAR + " TEXT);";

	private static final String INIVTE_MESSAGE_TABLE_CREATE = "CREATE TABLE "
			+ InviteMessgeDao.TABLE_NAME + " ("
			+ InviteMessgeDao.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ InviteMessgeDao.COLUMN_NAME_FROM + " TEXT, "
			+ InviteMessgeDao.COLUMN_NAME_GROUP_ID + " TEXT, "
			+ InviteMessgeDao.COLUMN_NAME_GROUP_Name + " TEXT, "
			+ InviteMessgeDao.COLUMN_NAME_REASON + " TEXT, "
			+ InviteMessgeDao.COLUMN_NAME_STATUS + " INTEGER, "
			+ InviteMessgeDao.COLUMN_NAME_ISINVITEFROMME + " INTEGER, "
			+ InviteMessgeDao.COLUMN_NAME_TIME + " TEXT); ";

    private static final String FRIENDSHIP_TABLE_CREATE = "CREATE TABLE "
            + FriendshipDao.TABLE_NAME + " ("
            + FriendshipDao.COLUMN_SRC_USER +" TEXT, "
            + FriendshipDao.COLUMN_RESULT +" INTEGER, "
            + FriendshipDao.COLUMN_MODE + " INTEGER);";

	private static final String ANNOUNCEMENT_TABLE_CREATE = "CREATE TABLE "
            + AnnouncementDao.TABLE_NAME + " ("
            + AnnouncementDao.COLUMN_TITLE +" TEXT, "
            + AnnouncementDao.COLUMN_CONTENT +" TEXT, "
            + AnnouncementDao.COLUMN_TIMESTAMP + " INTEGER);";



    private DbOpenHelper(Context context) {
		super(context, getUserDatabaseName(), null, DATABASE_VERSION);
	}
	
	public static DbOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DbOpenHelper(context.getApplicationContext());
		}
		return instance;
	}
	
	private static String getUserDatabaseName() {
        return  HXSDKHelper.getInstance().getHXId() + "_demo.db";
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		onCreateDB(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table " + UserDao.PREF_TABLE_NAME);
		db.execSQL("drop table " + UserDao.ROBOT_TABLE_NAME);
	    db.execSQL("drop table " + UserDao.TABLE_NAME);
	    db.execSQL("drop table " + InviteMessgeDao.TABLE_NAME);
        db.execSQL("drop table " + FriendshipDao.TABLE_NAME);
        db.execSQL("drop table " + AnnouncementDao.TABLE_NAME);
	    onCreateDB(db);
	}
	
	
	private void onCreateDB(SQLiteDatabase db){
		db.execSQL(ROBOT_TABLE_CREATE);
	    db.execSQL(USERNAME_TABLE_CREATE);
		db.execSQL(PREF_TABLE_CREATE);
        db.execSQL(INIVTE_MESSAGE_TABLE_CREATE);
		db.execSQL(DEPARTMENT_TABLE_CREATE);
        db.execSQL(FRIENDSHIP_TABLE_CREATE);
        db.execSQL(ANNOUNCEMENT_TABLE_CREATE);
	}
	
	public void closeDB() {
	    if (instance != null) {
	        try {
	            SQLiteDatabase db = instance.getWritableDatabase();
	            db.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        instance = null;
	    }
	}
	
}
