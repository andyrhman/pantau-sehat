// File: MedAlarmManager.java in com.example.pantausehat.util

package com.example.pantausehat.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.pantausehat.data.Medication;
import com.example.pantausehat.ui.AlarmReceiver;

import java.util.Calendar;
import java.util.List;

public class MedAlarmManager {

    /** Schedule a daily repeating alarm at med.hour:med.minute */
    public static void scheduleDailyAlarm(Context ctx, Medication med) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, AlarmReceiver.class)
                .putExtra("medName", med.name)
                .putExtra("medDosage", med.dosage);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                med.id, // Ensure this is unique
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, med.hour);
        cal.set(Calendar.MINUTE, med.minute);
        cal.set(Calendar.SECOND, 0);
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DATE, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mgr.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pi
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mgr.setExact(
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pi
            );
        } else {
            mgr.set(
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pi
            );
        }
    }

    /** Schedule all existing meds from the DB. Call this on app startup or boot. */
    public static void scheduleAll(Context ctx, List<Medication> meds) {
        for (Medication med : meds) {
            scheduleDailyAlarm(ctx, med);
        }
    }
}
