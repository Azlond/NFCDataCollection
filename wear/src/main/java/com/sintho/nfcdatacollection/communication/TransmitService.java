package com.sintho.nfcdatacollection.communication;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.sintho.nfcdatacollection.MainActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransmitService extends IntentService {
    private static final String LOGTAG = TransmitService.class.getName();
    private static final String WAKELOCK = TransmitService.class.getName() + ".NfcRelayingWakelock";
    private static final String TAGFOUND = "FOUND_TAG";

    public static final String SCANTAG = "SCANTAG";
    public static final String JSONBYTEARRAY = "JSONBYTEARRAY";

    public TransmitService()
    {
        super("TransmitService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        @SuppressWarnings("ConstantConditions") PowerManager.WakeLock sendWakelock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK);
        sendWakelock.acquire(1000*60);
        try {
            if (intent.hasExtra(SCANTAG)) {
                Log.d(LOGTAG, SCANTAG);

                if (sendMessage(intent.getByteArrayExtra(JSONBYTEARRAY), SCANTAG)) {
                    Log.d(LOGTAG, "Sent SCANTAG message");
                }
            } else if (intent.hasExtra(MainActivity.BATTERYNOTIFICATION)) {
                if (sendMessage(new byte[]{}, MainActivity.BATTERYNOTIFICATION)) {
                    SharedPreferences.Editor editor = getSharedPreferences(MainActivity.SHAREDPREFERENCESKEY, MODE_PRIVATE).edit();
                    editor.putBoolean(MainActivity.BATTERYNOTIFICATION, true);
                    editor.apply();
                }
            } else {
                if (sendMessage(intent.getByteArrayExtra(JSONBYTEARRAY), TAGFOUND)) {
                    Log.d(LOGTAG, "Sent REGISTER message");
                }
            }
        } finally {
            sendWakelock.release();
        }
    }

    private boolean sendMessage(byte[] array, String tag) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        googleApiClient.blockingConnect();

        String connectedNode = getConnectedNodeID(googleApiClient);
        if (connectedNode == null) {
            Log.d(LOGTAG, "Phone not connected");
            return false;
        }

        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, tag, array).await();
        if (!result.getStatus().isSuccess()) {
            Log.d(LOGTAG, "Could not transmit NFC: " + result.getStatus().getStatusMessage());
            return false;
        }
        return true;
    }


    private static @Nullable String getConnectedNodeID(GoogleApiClient googleApiClient) {
        List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
        if (connectedNodes == null || connectedNodes.isEmpty())
            return null;

        //Sort to grab nearby node first
        Collections.sort(connectedNodes, NodeNearbyComparator.INSTANCE);
        Log.d(TransmitService.class.getName(), "Number of connected Nodes: " + connectedNodes.size());

        return connectedNodes.get(0).getId();
    }

    private static class NodeNearbyComparator implements Comparator<Node> {
        private static final NodeNearbyComparator INSTANCE = new NodeNearbyComparator();

        @Override
        public int compare(Node a, Node b)
        {
            int nearbyA = a.isNearby() ? 1 : 0;
            int nearbyB = b.isNearby() ? 1 : 0;
            return nearbyB - nearbyA;
        }
    }
}
