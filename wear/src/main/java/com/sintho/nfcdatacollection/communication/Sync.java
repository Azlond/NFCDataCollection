package com.sintho.nfcdatacollection.communication;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sintho.nfcdatacollection.MainActivity;
import com.sintho.nfcdatacollection.db.DBContract;
import com.sintho.nfcdatacollection.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sintho.nfcdatacollection.communication.ReceiverActivity.DATESTRING;
import static com.sintho.nfcdatacollection.communication.ReceiverActivity.IDSTRING;
import static com.sintho.nfcdatacollection.communication.ReceiverActivity.NFCIDSTRING;

public class Sync extends IntentService {
    private static final String LOGTAG = Sync.class.getName();

    public Sync(String name) {
        super(name);
    }
    public Sync() {
        super("Sync");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOGTAG, "Starting sync");
        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.

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
        for (int i = 0; i < itemIds.size(); i++) {
            JSONObject json = new JSONObject();
            try {
                json.put(IDSTRING, itemIds.get(i));
                json.put(DATESTRING, dates.get(i));
                json.put(NFCIDSTRING, nfcIds.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent forwardingIntent = new Intent(this, TransmitService.class);
            forwardingIntent.putExtra(TransmitService.JSONBYTEARRAY, json.toString().getBytes());
            startService(forwardingIntent);
            Log.d(LOGTAG, "Sent synced item");
        }
        checkBatteryLife();
    }

    /**
     * check battery life and notify phone if it's too low
     */
    private void checkBatteryLife() {
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        Log.d("MAINACTIVITY", Integer.toString(batteryLevel));
        SharedPreferences prefs = getSharedPreferences(MainActivity.SHAREDPREFERENCESKEY, MODE_PRIVATE);
        boolean batteryNotificationSent = prefs.getBoolean(MainActivity.BATTERYNOTIFICATION, false);
        if (batteryLevel < 50 && !batteryNotificationSent) {
            Intent forwardingIntent = new Intent(this, TransmitService.class);
            forwardingIntent.putExtra(MainActivity.BATTERYNOTIFICATION, true);
            startService(forwardingIntent);
        } else if (batteryLevel > 50 && batteryNotificationSent) {
            //reset notification
            SharedPreferences.Editor editor = getSharedPreferences(MainActivity.SHAREDPREFERENCESKEY, MODE_PRIVATE).edit();
            editor.putBoolean(MainActivity.BATTERYNOTIFICATION, false);
            editor.apply();
        }
    }
}
