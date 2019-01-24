package com.sintho.smarthomestudy.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NFCDataset.db";

    private static final String SQL_CREATE_TABLE_LOG =
            "CREATE TABLE IF NOT EXISTS " + DBContract.DBEntry.TABLE_NAMENFCLOG + " (" +
                    DBContract.DBEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DBContract.DBEntry.COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    DBContract.DBEntry.COLUMN_NAME + " TEXT," +
                    DBContract.DBEntry.COLUMN_NFCID + " TEXT)";

    private static final String SQL_CREATE_TABLE_REGISTER =
            "CREATE TABLE IF NOT EXISTS " + DBContract.DBEntry.TABLE_NAMEREGISTER+ " (" +
                    DBContract.DBEntry.COLUMN_NFCID + " TEXT PRIMARY KEY," +
                    DBContract.DBEntry.COLUMN_NAME + " TEXT)";

    private static final String SQL_CREATE_TABLE_UXSAMPLING =
            "CREATE TABLE IF NOT EXISTS " + DBContract.DBEntry.TABLE_UXSAMPLING + " (" +
                    DBContract.DBEntry.COLUMN_DATE + " DATETIME PRIMARY KEY," +
                    DBContract.DBEntry.COLUMN_DATA + " TEXT," +
                    DBContract.DBEntry.COLUMN_TEXT
                    + " TEXT)";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_LOG);
        db.execSQL(SQL_CREATE_TABLE_REGISTER);
        db.execSQL(SQL_CREATE_TABLE_UXSAMPLING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
