package com.sintho.nfcdatacollection.communication;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.sintho.nfcdatacollection.MainActivity;
import com.sintho.nfcdatacollection.Registering;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransmitService extends IntentService {
    private static final String LOGTAG = TransmitService.class.getName();
    public static final String WAKELOCK = TransmitService.class.getName() + ".NfcRelayingWakelock";
    public static final String REGISTER = "REGISTER";
    public static final String JSONBYTEARRAY = "JSONBYTEARRAY";
    public TransmitService()
    {
        super("TransmitService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        PowerManager.WakeLock sendWakelock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK);
        sendWakelock.acquire();
        try {
            if (intent.hasExtra(REGISTER)) {
                Log.d(LOGTAG, REGISTER);

                //Notify activity that registering is complete
                Intent broadcastIntent = new Intent(Registering.ONFINISH);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);

                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
                googleApiClient.blockingConnect();

                String connectedNode = getConnectedNodeID(googleApiClient);
                if (connectedNode == null) {
                    Log.d(LOGTAG, "Phone not connected");
                    return;
                }

                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, REGISTER, intent.getByteArrayExtra(JSONBYTEARRAY)).await();
                if (!result.getStatus().isSuccess()) {
                    Log.d(LOGTAG, "Could not transmit NFC: " + result.getStatus().getStatusMessage());
                }
                Log.d(LOGTAG, "Sent register message");
            } else {
                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
                googleApiClient.blockingConnect();

                String connectedNode = getConnectedNodeID(googleApiClient);
                if (connectedNode == null) {
                    Log.d(LOGTAG, "Phone not connected");
                    return;
                }

                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, "FOUND_TAG", intent.getByteArrayExtra(JSONBYTEARRAY)).await();
                if (!result.getStatus().isSuccess()) {
                    Log.d(LOGTAG, "Could not transmit NFC: " + result.getStatus().getStatusMessage());
                }
                Log.d(LOGTAG, "Sent");
            }
        } finally {
            sendWakelock.release();
        }
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
        public static final NodeNearbyComparator INSTANCE = new NodeNearbyComparator();

        @Override
        public int compare(Node a, Node b)
        {
            int nearbyA = a.isNearby() ? 1 : 0;
            int nearbyB = b.isNearby() ? 1 : 0;
            return nearbyB - nearbyA;
        }
    }
}
