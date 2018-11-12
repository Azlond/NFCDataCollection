package com.sintho.nfcdatacollection;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.sintho.nfcdatacollection.db.DBLogContract;
import com.sintho.nfcdatacollection.db.DBLogHelper;

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
                    throw new Exception("Invlid id value -1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(LOGTAG, String.format("gotTag: %s; with date %s; entry # %d", nfcID, date, id));

            DBLogHelper mDbLogHelper = new DBLogHelper(getApplicationContext());
            SQLiteDatabase db = mDbLogHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBLogContract.DBLogEntry.COLUMN_NFCID, nfcID);
            values.put(DBLogContract.DBLogEntry.COLUMN_NAME, "");
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
            Log.d(LOGTAG, "Successfully added new entry to database");
            Log.d(LOGTAG, String.format("Broadcasting %s to update NFC-Log fragment UI", NFCTAGCAST));
            Intent broadcastIntent = new Intent(NFCTAGCAST);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
        } else if (messageEvent.getPath().equals(TAGREGISTER)) {
//            Log.d(TAG, "register message");
//            Intent intent = new Intent(this, RegisterTagActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            byte[] tagId = messageEvent.getData();
//            String tagIdString = bytesToHex(tagId);
//            intent.putExtra(NFCTAGID, tagId);
//            startActivity(intent);
        }
    }
}
