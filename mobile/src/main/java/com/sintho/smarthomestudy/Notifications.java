package com.sintho.smarthomestudy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

public class Notifications {

    //sending a local notification
    public static void sendNotification(Context context, String title, String message, PendingIntent pendingIntent, int priority) {
        NotificationManager notificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_iconfinder_nfc_tag_1613763)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        if (pendingIntent != null) {
            mBuilder.setContentIntent(pendingIntent);
        }
        notificationManager.notify(1, mBuilder.build());
    }
}
