package com.sintho.smarthomestudy.fcm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sintho.smarthomestudy.KEYS;
import com.sintho.smarthomestudy.Navigation;
import com.sintho.smarthomestudy.Notifications;

public class NotificationsListenerService extends GcmListenerService {
    private static final String LOGTAG = NotificationsListenerService.class.getName();
    //receives messages from Firebase cloud messaging, and displays them in a notification.
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString("title");
        String message = data.getString("body");

        Intent intent = new Intent(this, Navigation.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KEYS.FEEDBACKFRAGMENT, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notifications.sendNotification(this, title, message, pendingIntent, Notification.PRIORITY_DEFAULT);
        Log.d(LOGTAG, message);
    }
}
