package com.sintho.nfcdatacollection.fcm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sintho.nfcdatacollection.Navigation;
import com.sintho.nfcdatacollection.Notifications;

public class NotificationsListenerService extends GcmListenerService {
    public static final String FEEDBACKFRAGMENT = "feedback_fragment";
    private static final String LOGTAG = NotificationsListenerService.class.getName();
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString("title");
        String message = data.getString("body");

        Intent intent = new Intent(this, Navigation.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(FEEDBACKFRAGMENT, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notifications.sendNotification(this, title, message, pendingIntent, Notification.PRIORITY_DEFAULT);
        Log.d(LOGTAG, message);
    }
}
