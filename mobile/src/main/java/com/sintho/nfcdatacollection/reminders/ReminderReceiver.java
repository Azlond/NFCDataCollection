package com.sintho.nfcdatacollection.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, ReminderService.class);
        context.startService(intent1);    }
}
