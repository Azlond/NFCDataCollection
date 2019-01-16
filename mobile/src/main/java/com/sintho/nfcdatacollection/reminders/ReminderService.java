package com.sintho.nfcdatacollection.reminders;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.Context;

import com.sintho.nfcdatacollection.Notifications;
import com.sintho.nfcdatacollection.R;

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
        Notifications.sendNotification(getApplicationContext(), "NFC Study", getString(R.string.feedback_reminder), null, Notification.PRIORITY_MAX);
    }
}
