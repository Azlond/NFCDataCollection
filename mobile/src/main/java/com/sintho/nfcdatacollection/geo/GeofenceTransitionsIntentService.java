package com.sintho.nfcdatacollection.geo;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.sintho.nfcdatacollection.Notifications;
import com.sintho.nfcdatacollection.R;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String LOGTAG = GeofenceTransitionsIntentService.class.getName();
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOGTAG, "onHandleIntent");
        Intent i = intent.getParcelableExtra("geoIntent");

        GeofencingEvent g = GeofencingEvent.fromIntent(i);

        Location l = g.getTriggeringLocation();

        if (l == null) {
            //happens if the user switches the location mode (Settings-Location-Mode) while the app is running
            Log.e(LOGTAG, "NullPointer location");
            return;
        }
        Notifications.sendNotification(getApplicationContext(), getString(R.string.app_name), getString(R.string.notification_geoReminder), null, Notification.PRIORITY_MAX);
    }

}
