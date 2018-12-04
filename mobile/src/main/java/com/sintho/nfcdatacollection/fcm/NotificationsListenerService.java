package com.sintho.nfcdatacollection.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.sintho.nfcdatacollection.Navigation;
import com.sintho.nfcdatacollection.R;

import java.util.Map;

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

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_iconfinder_nfc_tag_1613763)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);
        notificationManager.notify(1, mBuilder.build());
        Log.d(LOGTAG, message);
//        super.onMessageReceived(from, data);
    }
}
