package com.sintho.smarthomestudy.reminders;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;

import com.sintho.smarthomestudy.Notifications;
import com.sintho.smarthomestudy.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ReminderService extends IntentService {
    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //service started by the receiver to send a notification
        Notifications.sendNotification(getApplicationContext(), "NFC Study", getString(R.string.feedback_reminder), null, Notification.PRIORITY_MAX);
    }
}
