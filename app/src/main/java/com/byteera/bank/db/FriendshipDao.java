package com.byteera.bank.db;


public class FriendshipDao {
    public static final String TABLE_NAME = "friendship";
    public static final String COLUMN_SRC_USER = "src_user";
    public static final String COLUMN_MODE = "mode";
    public static final String COLUMN_RESULT = "result";

    public static final int MODE_ADD = 1;
    public static final int MODE_DEL = 2;

    public static final int RESULT_REQUEST = 0;
    public static final int RESULT_SUCCESS = 1;
}
