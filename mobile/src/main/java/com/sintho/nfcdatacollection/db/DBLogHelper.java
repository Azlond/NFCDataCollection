package com.sintho.nfcdatacollection.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBLogHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NFCDataset.db";

    private static final String SQL_CREATE_DB =
            "CREATE TABLE IF NOT EXISTS " + DBLogContract.DBLogEntry.TABLE_NAME + " (" +
                    DBLogContract.DBLogEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBLogContract.DBLogEntry.COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    DBLogContract.DBLogEntry.COLUMN_NAME + " TEXT," +
                    DBLogContract.DBLogEntry.COLUMN_NFCID + " TEXT)";

    public DBLogHelper(Context context) {
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
