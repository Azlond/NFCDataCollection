package com.sintho.smarthomestudy.communication;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sintho.smarthomestudy.KEYS;
import com.sintho.smarthomestudy.db.DBContract;
import com.sintho.smarthomestudy.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SyncService extends IntentService {
    private static final String LOGTAG = SyncService.class.getName();

    public SyncService(String name) {
        super(name);
    }
    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOGTAG, "Starting sync");
        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.

        //get all unsynced entries from the DB
        String sortOrder =
                DBContract.DBEntry._ID + " DESC";
        Cursor cursor = db.query(
                DBContract.DBEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                DBContract.DBEntry.COLUMN_SYNCED + " = ?",              // The columns for the WHERE clause
                new String[]{"0"},          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );


        List<Long> itemIds = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<String> nfcIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBContract.DBEntry._ID));
            itemIds.add(itemId);
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_DATE));
            dates.add(date);
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NFCID));
            nfcIds.add(id);
        }
        cursor.close();
        db.close();
        //send a message for each unsynced item to the smartphone
        for (int i = 0; i < itemIds.size(); i++) {
            JSONObject json = new JSONObject();
            try {
                json.put(KEYS.IDSTRING, itemIds.get(i));
                json.put(KEYS.DATESTRING, dates.get(i));
                json.put(KEYS.NFCIDSTRING, nfcIds.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent forwardingIntent = new Intent(this, PhoneCommunicationTransmitService.class);
            forwardingIntent.putExtra(KEYS.JSONBYTEARRAY, json.toString().getBytes());
            startService(forwardingIntent);
            Log.d(LOGTAG, "Sent synced item");
        }
        checkBatteryLife();
    }

    /**
     * check battery life and notify phone if it's too low (lower than 50%)
     */
    private void checkBatteryLife() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        Log.d(LOGTAG, String.format("Battery level: %d", batteryLevel));
        SharedPreferences prefs = getSharedPreferences(KEYS.SHAREDPREFERENCESKEY, MODE_PRIVATE);
        boolean batteryNotificationSent = prefs.getBoolean(KEYS.BATTERYNOTIFICATION, false);
        //only send a single notification
        if (batteryLevel < 50 && !batteryNotificationSent) {
            Intent forwardingIntent = new Intent(this, PhoneCommunicationTransmitService.class);
            forwardingIntent.putExtra(KEYS.BATTERYNOTIFICATION, true);
            startService(forwardingIntent);
        } else if (batteryLevel > 50 && batteryNotificationSent) {
            //reset notification if one has been sent and the battery is above 50% again
            SharedPreferences.Editor editor = getSharedPreferences(KEYS.SHAREDPREFERENCESKEY, MODE_PRIVATE).edit();
            editor.putBoolean(KEYS.BATTERYNOTIFICATION, false);
            editor.apply();
        }
    }
}
