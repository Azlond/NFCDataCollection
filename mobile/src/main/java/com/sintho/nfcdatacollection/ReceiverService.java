package com.sintho.nfcdatacollection;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

public class ReceiverService extends WearableListenerService {
    public static final String NFCTAGCAST = ReceiverService.class.getName() + "NFCBroadcast";
    public static final String NFCTAGID = ReceiverService.class.getName() + "ID";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("WearTagRelay", "onMessageReceived " + messageEvent);

        if (!messageEvent.getPath().equals("FOUND_TAG"))
            return;

        byte[] tagId = messageEvent.getData();
        String tagIdString = bytesToHex(tagId);

        Log.d("WearTagRelay", "gotTag: " + tagIdString);

        Intent broadcastIntent = new Intent(NFCTAGCAST);
        broadcastIntent.putExtra(NFCTAGID, tagIdString);
//        sendBroadcast(broadcastIntent);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

//        sendBroadcast(new Intent("com.sintho.nfcdatacollection.TAG_" + tagIdString));
//        ArrayList<String> list = new ArrayList<>();
//
//        for (String a : list)
//        {
//            //Naredi nekaj z a
//        }
    }

    /**
     * Snippet from Stack Overflow
     * http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     */
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
