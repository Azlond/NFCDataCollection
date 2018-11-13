package com.sintho.nfcdatacollection.communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.sintho.nfcdatacollection.Navigation;
import com.sintho.nfcdatacollection.R;
import com.sintho.nfcdatacollection.db.DBLogContract;
import com.sintho.nfcdatacollection.db.DBLogHelper;
import com.sintho.nfcdatacollection.db.DBRegisterContract;
import com.sintho.nfcdatacollection.db.DBRegisterHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ReceiverService extends WearableListenerService {
    public static final String LOGTAG = ReceiverService.class.getName();
    public static final String NFCTAGCAST = ReceiverService.class.getName() + "NFCBroadcast";
    public static final String IDSTRING = "id";
    public static final String DATESTRING = "date";
    public static final String NFCIDSTRING = "nfcid";
    public static final String TAGFOUND = "FOUND_TAG";
    public static final String TAGREGISTER = "REGISTER";
    public static final String FRAGREGISTER = "FRAGMENTREGISTER";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(LOGTAG, "message received " + messageEvent);

        if (messageEvent.getPath().equals(TAGFOUND)) {
            Log.d(LOGTAG, "message event: " + TAGFOUND);

            //get byte data from message
            byte[] jsonBytes = messageEvent.getData();
            int id = -1;
            String date = null;
            String nfcID = null;
            try {
                //decode byte array to string
                String decoded = new String(jsonBytes, "UTF-8");
                //parse string to json
                JSONObject json = new JSONObject(decoded);
                //get values from json
                id = (Integer) json.get(IDSTRING);
                date = (String) json.get(DATESTRING);
                nfcID = (String) json.get(NFCIDSTRING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (id == -1) {
                try {
                    throw new Exception("Invalid id value -1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(LOGTAG, String.format("gotTag: %s; with date %s; entry # %d", nfcID, date, id));

            DBLogHelper mDbLogHelper = new DBLogHelper(getApplicationContext());
            SQLiteDatabase db = mDbLogHelper.getWritableDatabase();
            SQLiteDatabase dbRead = mDbLogHelper.getReadableDatabase();
            String sortOrder = DBLogContract.DBLogEntry.COLUMN_ID + " DESC";

            Cursor cursor = dbRead.query(
                    DBLogContract.DBLogEntry.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    DBLogContract.DBLogEntry.COLUMN_ID + " = ?",              // The columns for the WHERE clause
                    new String[]{String.valueOf(id)},          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                Log.d(LOGTAG, "new entry for db");
                ContentValues values = new ContentValues();
                values.put(DBLogContract.DBLogEntry.COLUMN_NFCID, nfcID);

                /*
                 * check if a name is already assigned to this tagID, else use empty string
                 */
                DBRegisterHelper mDbRegisterHelper = new DBRegisterHelper(getApplicationContext());
                SQLiteDatabase dbNames = mDbRegisterHelper.getReadableDatabase();
                sortOrder = DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " DESC";
                cursor = dbNames.query(
                        DBRegisterContract.DBRegisterEntry.TABLE_NAME,   // The table to query
                        null,             // The array of columns to return (pass null to get all)
                        DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " = ?",              // The columns for the WHERE clause
                        new String[]{nfcID},          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DBRegisterContract.DBRegisterEntry.COLUMN_NAME));
                    values.put(DBLogContract.DBLogEntry.COLUMN_NAME, name);
                } else {
                    values.put(DBLogContract.DBLogEntry.COLUMN_NAME, "");
                }
                cursor.close();
                values.put(DBLogContract.DBLogEntry.COLUMN_DATE, date);
                values.put(DBLogContract.DBLogEntry.COLUMN_ID, id);

                long newRowId = db.insert(DBLogContract.DBLogEntry.TABLE_NAME, null, values);
                if (newRowId == -1) {
                    try {
                        throw new Exception(String.format("row not inserted: %d", newRowId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.d(LOGTAG, "Successfully added new entry to log database");
                Log.d(LOGTAG, String.format("Broadcasting %s to update NFC-Log fragment UI", NFCTAGCAST));
                Intent broadcastIntent = new Intent(NFCTAGCAST);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            } else {
                Log.d(LOGTAG, "Already received row with id: " + id);
                cursor.close();
            }
            /*
             * confirm to watch that row #id has been received
             */
            Log.d(LOGTAG, "sending confirmation message");
            Intent watchIntent = new Intent(ReceiverService.this, TransmitService.class);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(IDSTRING, id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            watchIntent.putExtra(TransmitService.JSONBYTEARRAY, jsonObject.toString().getBytes());
            startService(watchIntent);
            Log.d(LOGTAG, "Confirmation message sent");
        } else if (messageEvent.getPath().equals(TAGREGISTER)) {
            Log.d(LOGTAG, "message event" + TAGREGISTER);

            byte[] jsonBytes = messageEvent.getData();
            String nfcID = null;
            try {
                //decode byte array to string
                String decoded = new String(jsonBytes, "UTF-8");
                //parse string to json
                JSONObject json = new JSONObject(decoded);
                //get values from json
                nfcID = (String) json.get(NFCIDSTRING);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(LOGTAG, String.format("gotTag: %s", nfcID));

            /*
             * check if a name is already assigned to this tagID, else use empty string
             */
            DBRegisterHelper mDbRegisterHelper = new DBRegisterHelper(getApplicationContext());
            SQLiteDatabase dbNames = mDbRegisterHelper.getReadableDatabase();
            String sortOrder = DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " DESC";
            Cursor cursor = dbNames.query(
                    DBRegisterContract.DBRegisterEntry.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    DBRegisterContract.DBRegisterEntry.COLUMN_NFCID + " = ?",              // The columns for the WHERE clause
                    new String[]{nfcID},          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            if (cursor.getCount() > 0) {
                //tag has already been registered
                cursor.moveToFirst();
                Log.d(LOGTAG, String.format("Tag %s has already been registered", nfcID));
                showNotification(getApplicationContext(), nfcID, cursor.getString(cursor.getColumnIndexOrThrow(DBRegisterContract.DBRegisterEntry.COLUMN_NAME)));
                cursor.close();
                return;
            } else {
                cursor.close();
            }
            SQLiteDatabase db = mDbRegisterHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBLogContract.DBLogEntry.COLUMN_NFCID, nfcID);
            values.put(DBLogContract.DBLogEntry.COLUMN_NAME, "");

            long newRowId = db.insert(DBRegisterContract.DBRegisterEntry.TABLE_NAME, null, values);
            if (newRowId == -1) {
                try {
                    throw new Exception(String.format("row not inserted: %d", newRowId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Intent broadcastIntent = new Intent(FRAGREGISTER);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
        }
    }

    public void showNotification(Context context, String nfcID, String name) {
        Intent intent = new Intent(context, Navigation.class);
        PendingIntent pi = PendingIntent.getActivity(context, 5000, intent, 0);
        String message = String.format("ID %s has already been registered with name %s", nfcID, name);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.cast_ic_notification_0)
                .setContentTitle(getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message);
        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(5000, mBuilder.build());
    }
}
