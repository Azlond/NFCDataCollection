package com.sintho.smarthomestudy.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //receiver notified by the alarm to send a notification
        Intent intent1 = new Intent(context, ReminderService.class);
        context.startService(intent1);    }
}
