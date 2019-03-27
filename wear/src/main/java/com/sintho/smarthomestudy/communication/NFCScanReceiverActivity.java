package com.sintho.smarthomestudy.communication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;

import com.sintho.smarthomestudy.KEYS;
import com.sintho.smarthomestudy.ScanningActivity;
import com.sintho.smarthomestudy.db.DBContract;
import com.sintho.smarthomestudy.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class NFCScanReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent startingIntent = getIntent();
        Tag tag = startingIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String tagUID = bytesToHex(tag.getId());
            //Vibrate subtly to indicate tag scanned
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);

            //check if scanning is enabled. If yes, we don't need to send the data to the smartphone for logging, only for a notification.
            if (ScanningActivity.SCANNINGACTIVITY) {
                Intent forwardingIntent = new Intent(this, PhoneCommunicationTransmitService.class);
                //forward tag to smartphone, but declare it as non-logging
                forwardingIntent.putExtra(KEYS.SCANTAGNOLOG, true);
                JSONObject json = new JSONObject();
                try {
                    json.put(KEYS.NFCIDSTRING, tagUID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                forwardingIntent.putExtra(KEYS.JSONBYTEARRAY, json.toString().getBytes());
                startService(forwardingIntent);
                //Notify activity that scanning is complete, to display the UID
                Intent broadcastIntent = new Intent(KEYS.ONFINISHINTENTFILTER);
                broadcastIntent.putExtra(KEYS.ONFINISHINTENTFILTER, tagUID);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            } else {
                //save NFC tag and date in db
                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(DBContract.DBEntry.COLUMN_NFCID, tagUID);

                long newRowId = db.insert(DBContract.DBEntry.TABLE_NAME, null, values);
                if (newRowId == -1) {
                    try {
                        throw new Exception(String.format("row not inserted: %d", newRowId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                String sortOrder =
                        DBContract.DBEntry._ID+ " DESC";
                Cursor cursor = db.query(
                        DBContract.DBEntry.TABLE_NAME,   // The table to query
                        null,             // The array of columns to return (pass null to get all)
                        DBContract.DBEntry._ID + " = ?",              // The columns for the WHERE clause
                        new String[] {String.valueOf(newRowId)},          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );
                //retrieve the newly added values to get the correct timestamp
                JSONObject json = new JSONObject();
                try {
                    json.put(KEYS.IDSTRING, newRowId);

                    while(cursor.moveToNext()) {
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_DATE));
                        json.put(KEYS.DATESTRING, date);
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NFCID));
                        json.put(KEYS.NFCIDSTRING, id);
                    }
                    cursor.close();
                    db.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //send data to smartphone
                Intent forwardingIntent = new Intent(this, PhoneCommunicationTransmitService.class);
                forwardingIntent.putExtra(KEYS.JSONBYTEARRAY, json.toString().getBytes());
                startService(forwardingIntent);
            }
        }

       finish();
    }
    /**
     * Snippet from Stack Overflow
     * http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
