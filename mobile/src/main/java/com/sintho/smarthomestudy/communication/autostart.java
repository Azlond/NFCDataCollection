package com.sintho.smarthomestudy.communication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.sintho.smarthomestudy.db.DBSyncService;
import com.sintho.smarthomestudy.reminders.ReminderReceiver;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class autostart extends BroadcastReceiver {
    private static final int REQUESTCODE = 1337;
    private static final String LOGTAG = autostart.class.getName();

    public void onReceive(Context context, Intent intent) {
        Log.d(LOGTAG, "starting alarm");
        Intent i = new Intent(context, DBSyncService.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, REQUESTCODE, i, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    AlarmManager.INTERVAL_HALF_DAY, alarmIntent);
            //create reminders for Feeback
            Calendar cur_cal = new GregorianCalendar();
            cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar

            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
            cal.set(Calendar.HOUR_OF_DAY, 22);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
            cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));

            Intent notifyIntent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast
                    (context, 1017, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  cal.getTimeInMillis(),
                    1000 * 60 * 60 * 24, pendingIntent);
            Log.d(LOGTAG, "Daily notification registered");
        }

    }
}
