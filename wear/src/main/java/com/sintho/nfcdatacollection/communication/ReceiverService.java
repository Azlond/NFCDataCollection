package com.sintho.nfcdatacollection.communication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.sintho.nfcdatacollection.db.DBContract;
import com.sintho.nfcdatacollection.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.sintho.nfcdatacollection.communication.ReceiverActivity.IDSTRING;

public class ReceiverService extends WearableListenerService {
    private static final String LOGTAG = ReceiverService.class.getName();

    //for communication with phone
    private static final String RECEIVED = "RECEIVED";
    private static final String SYNC = "SYNC";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(LOGTAG, "message received " + messageEvent);

        if (messageEvent.getPath().equals(RECEIVED)) {
            //get byte data from message
            byte[] jsonBytes = messageEvent.getData();
            int id = -1;
            try {
                //decode byte array to string
                String decoded = new String(jsonBytes, "UTF-8");
                //parse string to json
                JSONObject json = new JSONObject(decoded);
                //get values from json
                id = (Integer) json.get(IDSTRING);
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
            values.put(DBContract.DBEntry.COLUMN_SYNCED, 1);
            db.update(DBContract.DBEntry.TABLE_NAME, values, DBContract.DBEntry._ID+ " = ?", new String[]{String.valueOf(id)});
            db.close();
        } else if (messageEvent.getPath().equals(SYNC)) {
            Log.d(LOGTAG, "Received SYNC request");
            Intent i = new Intent(getApplicationContext(), Sync.class);
            startService(i);
        }
    }
}
