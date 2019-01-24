package com.sintho.smarthomestudy.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeoReceiver extends BroadcastReceiver {
    protected static final String LOGTAG = GeoReceiver.class.getName();

    /**
     * Receives the PendingIntent of the Geofencing event
     * Passes is on to the GeofenceTransitionsIntentService
     * @param context context
     * @param intent contains the Geofencing event
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "Receiver received broadcast");
        Intent i = new Intent(context, GeofenceTransitionsIntentService.class);
        i.putExtra("geoIntent", intent);
        context.startService(i);
    }
}
