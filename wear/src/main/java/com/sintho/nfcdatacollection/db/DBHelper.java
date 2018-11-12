package com.sintho.nfcdatacollection.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NFCDataset.db";

    private static final String SQL_CREATE_DB =
            "CREATE TABLE IF NOT EXISTS " + DBContract.DBEntry.TABLE_NAME + " (" +
                    DBContract.DBEntry._ID + " INTEGER PRIMARY KEY," +
                    DBContract.DBEntry.COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    DBContract.DBEntry.COLUMN_NFCID + " TEXT," +
                    DBContract.DBEntry.COLUMN_SYNCED + " INTEGER DEFAULT 0)";

    public DBHelper(Context context) {
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
