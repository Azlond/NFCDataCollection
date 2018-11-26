package com.sintho.nfcdatacollection.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBRegisterHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NFCNames.db";

    private static final String SQL_CREATE_DB =
            "CREATE TABLE IF NOT EXISTS " + DBRegisterContract.DBRegisterEntry.TABLE_NAME + " (" +
                    DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " TEXT PRIMARY KEY," +
                    DBRegisterContract.DBRegisterEntry.COLUMN_NAME + " TEXT)";

    public DBRegisterHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}