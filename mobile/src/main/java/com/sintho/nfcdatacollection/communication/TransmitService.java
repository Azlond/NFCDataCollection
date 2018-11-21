package com.sintho.nfcdatacollection.communication;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransmitService extends IntentService {
    private static final String LOGTAG = TransmitService.class.getName();
    private static final String WAKELOCK = TransmitService.class.getName() + ".NfcRelayingWakelock";
    private static final String RECEIVED = "RECEIVED";
    public static final String JSONBYTEARRAY = "JSONBYTEARRAY";
    public static final String TASK = "TASK";
    public static final String CONFIRMATION = "CONFIRMATION";
    public static final String SYNC = "SYNC";
    public TransmitService()
    {
        super("TransmitService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOGTAG, "onHandleIntent TransmitService");
        if (intent.hasExtra(TASK)) {
            @SuppressWarnings("ConstantConditions") PowerManager.WakeLock sendWakelock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK);
            sendWakelock.acquire(60 * 1000L /*1 minute*/);
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
            googleApiClient.blockingConnect();
            String connectedNode = getConnectedNodeID(googleApiClient);
            if (connectedNode == null) {
                Log.d(LOGTAG, "Watch not connected");
                return;
            }
            try {
                if (intent.getStringExtra(TASK).equals(CONFIRMATION)) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, RECEIVED, intent.getByteArrayExtra(JSONBYTEARRAY)).await();
                    if (!result.getStatus().isSuccess()) {
                        Log.d(LOGTAG, "Could not transmit NFC confirmation: " + result.getStatus().getStatusMessage());
                    }
                    Log.d(LOGTAG, "Sent");
                } else if (intent.getStringExtra(TASK).equals(SYNC)) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, SYNC, new byte[]{}).await();
                    if (!result.getStatus().isSuccess()) {
                        Log.d(LOGTAG, "Could not transmit sync request: " + result.getStatus().getStatusMessage());
                    }
                    Log.d(LOGTAG, "Sent");
                }
            } finally {
                Log.d(LOGTAG, "Releasing wakelock");
                sendWakelock.release();
            }
        } else {
            try {
                throw new Exception("Unknown intent");
            } catch (Exception e) {
                e.printStackTrace();
            }
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
