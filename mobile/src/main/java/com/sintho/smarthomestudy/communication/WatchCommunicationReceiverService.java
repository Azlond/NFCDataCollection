package com.sintho.smarthomestudy.communication;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.sintho.smarthomestudy.KEYS;
import com.sintho.smarthomestudy.Notifications;
import com.sintho.smarthomestudy.R;
import com.sintho.smarthomestudy.db.DBContract;
import com.sintho.smarthomestudy.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class WatchCommunicationReceiverService extends WearableListenerService {
    private static final String LOGTAG = WatchCommunicationReceiverService.class.getName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(LOGTAG, "message received " + messageEvent);

        if (messageEvent.getPath().equals(KEYS.TAGFOUND)) {
            //normal NFC scan event
            Log.d(LOGTAG, "message event: " + KEYS.TAGFOUND);

            //get byte data from message
            byte[] jsonBytes = messageEvent.getData();
            Integer id = (Integer) getValueFromJSON(jsonBytes, KEYS.IDSTRING);
            String date = (String) getValueFromJSON(jsonBytes, KEYS.DATESTRING);
            String nfcID = (String) getValueFromJSON(jsonBytes, KEYS.NFCIDSTRING);
            if (id == null || date == null || nfcID == null) {
                try {
                    throw new Exception("Invalid id value -1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(LOGTAG, String.format("gotTag: %s; with date %s; entry # %d", nfcID, date, id));
            //save data to db
            DBHelper mDbHelper = new DBHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            SQLiteDatabase dbRead = mDbHelper.getReadableDatabase();
            String sortOrder = DBContract.DBEntry.COLUMN_ID + " DESC";

            Cursor cursor = dbRead.query(
                    DBContract.DBEntry.TABLE_NAMENFCLOG,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    DBContract.DBEntry.COLUMN_ID + " = ?",              // The columns for the WHERE clause
                    new String[]{String.valueOf(id)},          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            try {
                if (cursor.getCount() == 0) {
                    Log.d(LOGTAG, "new entry for db");
                    ContentValues values = new ContentValues();
                    values.put(DBContract.DBEntry.COLUMN_NFCID, nfcID);

                    Cursor cursor2 = getNameCursorFromDB(nfcID);

                    //if the tag ID has received a name, use it. Otherwise, use an empty string
                    if (cursor2.getCount() > 0) {
                        cursor2.moveToFirst();
                        String name = cursor2.getString(cursor2.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME));
                        values.put(DBContract.DBEntry.COLUMN_NAME, name);
                    } else {
                        values.put(DBContract.DBEntry.COLUMN_NAME, "");
                        addToRegister(nfcID);
                    }
                    cursor2.close();
                    values.put(DBContract.DBEntry.COLUMN_DATE, date);
                    values.put(DBContract.DBEntry.COLUMN_ID, id);

                    long newRowId = db.insert(DBContract.DBEntry.TABLE_NAMENFCLOG, null, values);
                    if (newRowId == -1) {
                        try {
                            throw new Exception(String.format("row not inserted: %d", newRowId));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(LOGTAG, "Successfully added new entry to log database");
                    Log.d(LOGTAG, String.format("Broadcasting %s to update NFC-Log fragment UI", KEYS.NFCTAGCAST));
                    //Update UI with the new entry.
                    Intent broadcastIntent = new Intent(KEYS.NFCTAGCAST);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
                }
            } finally {
                cursor.close();
                dbRead.close();
                db.close();
            }
            /*
             * confirm to watch that row #id has been received
             */
            Log.d(LOGTAG, "sending confirmation message");
            Intent watchIntent = new Intent(WatchCommunicationReceiverService.this, WatchCommunicationTransmitService.class);
            watchIntent.putExtra(KEYS.TASK, KEYS.CONFIRMATION);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEYS.IDSTRING, id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            watchIntent.putExtra(KEYS.JSONBYTEARRAY, jsonObject.toString().getBytes());
            startService(watchIntent);
            Log.d(LOGTAG, "Confirmation message sent");
        } else if (messageEvent.getPath().equals(KEYS.SCANTAG)) {
            //retrieving tag id event. We need to display a notification with the current name. If none has been given, empty string.
            byte[] jsonBytes = messageEvent.getData();
            String nfcID = (String) getValueFromJSON(jsonBytes, KEYS.NFCIDSTRING);
            Log.d(LOGTAG, String.format("gotTag: %s", nfcID));

            Cursor cursor = getNameCursorFromDB(nfcID);
            String name = "";
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                name = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NAME));
            }
            cursor.close();
//            Intent intent = new Intent(getApplicationContext(), Navigation.class);
//            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notifications.sendNotification(getApplicationContext(), getString(R.string.app_name), (getString(R.string.notification_tagID).replace("replacement1", nfcID).replace("replacement2", name)), null, Notification.PRIORITY_HIGH);
        } else if (messageEvent.getPath().equals(KEYS.BATTERYNOTIFICATION)) {
            //Battery of the smartwatch has dropped below 50%. Notify the user with a notification.
            Notifications.sendNotification(getApplicationContext(), getString(R.string.app_name), getString(R.string.notification_battery), null, Notification.PRIORITY_HIGH);
        }
    }

    /**
     *
     * @param jsonBytes byte array containing a json
     * @param key key to retrieve value from json
     * @return value of key
     */
    private Object getValueFromJSON(byte[] jsonBytes, String key) {
        Object value = null;
        try {
            //decode byte array to string
            String decoded = new String(jsonBytes, "UTF-8");
            //parse string to json
            JSONObject json = new JSONObject(decoded);
            //get values from json
            value = json.get(key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * checks the db to see if the ID already has a name
     * if cursor.getCount() > 0, it does already have a name
     * @param nfcID
     * @return
     */
    private Cursor getNameCursorFromDB(String nfcID) {
        DBHelper mDbRegisterHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase dbNames = mDbRegisterHelper.getReadableDatabase();
        String sortOrder = DBContract.DBEntry.COLUMN_NFCID + " DESC";
        return dbNames.query(
                DBContract.DBEntry.TABLE_NAMEREGISTER,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                DBContract.DBEntry.COLUMN_NFCID + " = ?",              // The columns for the WHERE clause
                new String[]{nfcID},          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
    }

    /**
     * adds ID to the register-names db
     * @param nfcID
     */
    private void addToRegister(String nfcID) {
        DBHelper mDbRegisterHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = mDbRegisterHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBContract.DBEntry.COLUMN_NFCID, nfcID);
        values.put(DBContract.DBEntry.COLUMN_NAME, "");

        long newRowId = db.insert(DBContract.DBEntry.TABLE_NAMEREGISTER, null, values);
        if (newRowId == -1) {
            try {
                throw new Exception(String.format("row not inserted: %d", newRowId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.close();
        //Update the UI with the newly added ID
        Intent broadcastIntent = new Intent(KEYS.FRAGREGISTER);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
    }
}