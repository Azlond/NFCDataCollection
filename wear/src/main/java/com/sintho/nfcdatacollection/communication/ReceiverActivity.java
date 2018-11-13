package com.sintho.nfcdatacollection.communication;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;

import com.sintho.nfcdatacollection.Registering;
import com.sintho.nfcdatacollection.db.DBContract;
import com.sintho.nfcdatacollection.db.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceiverActivity extends Activity {
    public static final String IDSTRING = "id";
    public static final String DATESTRING = "date";
    public static final String NFCIDSTRING = "nfcid";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent startingIntent = getIntent();
        Tag tag = startingIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String tagUID = bytesToHex(tag.getId());
        // noinspection ConstantConditions
        if (tag != null) {
            //Vibrate subtly to indicate tag scanned
            //noinspection ConstantConditions
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(75);

            if (Registering.registering) {
                Intent forwardingIntent = new Intent(this, TransmitService.class);
                forwardingIntent.putExtra(TransmitService.REGISTER, true);
                JSONObject json = new JSONObject();
                try {
                    json.put(NFCIDSTRING, tagUID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                forwardingIntent.putExtra(TransmitService.JSONBYTEARRAY, json.toString().getBytes());
                startService(forwardingIntent);
            } else {
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

                JSONObject json = new JSONObject();
                try {
                    json.put(IDSTRING, newRowId);

                    while(cursor.moveToNext()) {
                        String date = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_DATE));
                        json.put(DATESTRING, date);
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.DBEntry.COLUMN_NFCID));
                        json.put(NFCIDSTRING, id);
                    }
                    cursor.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent forwardingIntent = new Intent(this, TransmitService.class);
                forwardingIntent.putExtra(TransmitService.JSONBYTEARRAY, json.toString().getBytes());
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
