package com.sintho.smarthomestudy.communication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.sintho.smarthomestudy.KEYS;
import com.sintho.smarthomestudy.db.DBContract;
import com.sintho.smarthomestudy.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class PhoneCommunicationReceiverService extends WearableListenerService {
    private static final String LOGTAG = PhoneCommunicationReceiverService.class.getName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(LOGTAG, "message received " + messageEvent);

        if (messageEvent.getPath().equals(KEYS.RECEIVED)) {
            //message handshake to confirm that the phone has received the data
            //get byte data from message
            byte[] jsonBytes = messageEvent.getData();
            int id = -1;
            try {
                //decode byte array to string
                String decoded = new String(jsonBytes, "UTF-8");
                //parse string to json
                JSONObject json = new JSONObject(decoded);
                //get values from json
                id = (Integer) json.get(KEYS.IDSTRING);
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
            if (id == -1) {
                try {
                    throw new Exception("Invalid id value -1");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DBHelper mdbHelper = new DBHelper(this);
            SQLiteDatabase db = mdbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            //setting the db entry in column synced to 1 to mark it as completely synced.
            values.put(DBContract.DBEntry.COLUMN_SYNCED, 1);
            db.update(DBContract.DBEntry.TABLE_NAME, values, DBContract.DBEntry._ID+ " = ?", new String[]{String.valueOf(id)});
            db.close();
        } else if (messageEvent.getPath().equals(KEYS.SYNC)) {
            //user requests a data sync from the phone
            Log.d(LOGTAG, "Received SYNC request");
            Intent i = new Intent(getApplicationContext(), SyncService.class);
            startService(i);
        }
    }
}
