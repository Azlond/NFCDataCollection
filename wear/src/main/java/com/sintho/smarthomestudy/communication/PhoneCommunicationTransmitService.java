package com.sintho.smarthomestudy.communication;

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
import com.sintho.smarthomestudy.KEYS;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PhoneCommunicationTransmitService extends IntentService {
    private static final String LOGTAG = PhoneCommunicationTransmitService.class.getName();
    private static final String WAKELOCK = PhoneCommunicationTransmitService.class.getName() + ".NfcRelayingWakelock";

    public PhoneCommunicationTransmitService()
    {
        super("PhoneCommunicationTransmitService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock sendWakelock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK);//set wakelock to ensure sending without problems
        sendWakelock.acquire(1000*60);
        try {
            if (intent.hasExtra(KEYS.SCANTAGNOLOG)) {
                //the results of the scan should not get logged, only be displayed as notification on the phone.
                Log.d(LOGTAG, KEYS.SCANTAGNOLOG);

                if (sendMessage(intent.getByteArrayExtra(KEYS.JSONBYTEARRAY), KEYS.SCANTAGNOLOG)) {
                    Log.d(LOGTAG, "Sent SCANTAGNOLOG message");
                }
            } else if (intent.hasExtra(KEYS.BATTERYNOTIFICATION)) {
                //battery is low, should display notification on phone
                if (sendMessage(new byte[]{}, KEYS.BATTERYNOTIFICATION)) {
                    //setting a pref to true to ensure only one notification gets displayed, to not annoy the user.
                    SharedPreferences.Editor editor = getSharedPreferences(KEYS.SHAREDPREFERENCESKEY, MODE_PRIVATE).edit();
                    editor.putBoolean(KEYS.BATTERYNOTIFICATION, true);
                    editor.apply();
                }
            } else {
                //basic message informing the phone of a new scanned tag.
                if (sendMessage(intent.getByteArrayExtra(KEYS.JSONBYTEARRAY), KEYS.TAGFOUND)) {
                    Log.d(LOGTAG, "Sent Tagfound message");
                }
            }
        } finally {
            //releasing wakelock to avoid battery drain after the message has been sent.
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
        Log.d(PhoneCommunicationTransmitService.class.getName(), "Number of connected Nodes: " + connectedNodes.size());

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
