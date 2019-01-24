package com.sintho.smarthomestudy.communication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class autostart extends BroadcastReceiver {
    private static final int REQUESTCODE = 1337;
    private static final String LOGTAG = autostart.class.getName();

    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "starting alarm");
        Intent i = new Intent(context, Sync.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, REQUESTCODE, i, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_HOUR, alarmIntent);
        }
    }
}
