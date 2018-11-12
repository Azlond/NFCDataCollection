package com.sintho.nfcdatacollection;

import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransmitService extends IntentService {
    private static final String TAG = TransmitService.class.getName();
    public static final String TAGUID = "TagId";
    public static final String WAKELOCK = TransmitService.class.getName() + ".NfcRelayingWakelock";
    public static final String REGISTER = "Registering";
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
            String tagID = intent.getStringExtra(TAGUID);
            if (intent.hasExtra(REGISTER)) {
                Log.d(TAG, "registering");
                MainActivity.registering = false;

                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
                googleApiClient.blockingConnect();

                String connectedNode = getConnectedNodeID(googleApiClient);
                if (connectedNode == null) {
//                Toast.makeText(this, "Phone not connected.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Phone not connected");
                    return;
                }

                byte[] tagBytes = intent.getByteArrayExtra(JSONBYTEARRAY);
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, REGISTER, tagBytes).await();
                if (!result.getStatus().isSuccess()) {
//                Toast.makeText(this, "Could not transmit NFC: " + result.getStatus().getStatusMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Could not transmit NFC: " + result.getStatus().getStatusMessage());
                }
                Log.d(TAG, "Sent register message");
            } else {

                Log.d(TAG, "Got Tag, ID: " + tagID);

                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
                googleApiClient.blockingConnect();

                String connectedNode = getConnectedNodeID(googleApiClient);
                if (connectedNode == null) {
//                Toast.makeText(this, "Phone not connected.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Phone not connected");
                    return;
                }

                byte[] jsonBytes = intent.getByteArrayExtra(JSONBYTEARRAY);
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, connectedNode, "FOUND_TAG", jsonBytes).await();
                if (!result.getStatus().isSuccess()) {
//                Toast.makeText(this, "Could not transmit NFC: " + result.getStatus().getStatusMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Could not transmit NFC: " + result.getStatus().getStatusMessage());
                }
                Log.d(TAG, "Sent");

            }
        }
        finally
        {
            sendWakelock.release();
        }
    }


    private static @Nullable String getConnectedNodeID(GoogleApiClient googleApiClient)
    {
        List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await().getNodes();
        if (connectedNodes == null || connectedNodes.isEmpty())
            return null;

        //Sort to grab nearby node first
        Collections.sort(connectedNodes, NodeNearbyComparator.INSTANCE);
        Log.d(TransmitService.class.getName(), "Number of connected Nodes: " + connectedNodes.size());

        return connectedNodes.get(0).getId();
    }

    private static class NodeNearbyComparator implements Comparator<Node>
    {
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
