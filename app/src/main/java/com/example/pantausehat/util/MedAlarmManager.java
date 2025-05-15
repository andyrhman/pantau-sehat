package com.example.pantausehat.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.pantausehat.data.Medication;
import com.example.pantausehat.ui.AlarmReceiver;

import java.util.Calendar;
import java.util.List;

public class MedAlarmManager {

    /**
     * Schedule a repeating alarm based on the medication's frequency.
     * Frequency strings like "Every N hours" will be parsed into intervals.
     */
    public static void scheduleRepeatingAlarm(Context ctx, Medication med) {
        AlarmManager mgr = ctx.getSystemService(AlarmManager.class);

        Intent i = new Intent(ctx, AlarmReceiver.class)
                .putExtra("medId", med.id)
                .putExtra("medName", med.name)
                .putExtra("medDosage", med.dosage)
                .putExtra("medFrequency", med.frequency)
                .putExtra("medHour", med.hour)
                .putExtra("medMinute", med.minute);

        // 1) Try to retrieve an existing PendingIntent
        PendingIntent existing = PendingIntent.getBroadcast(
                ctx, med.id, i,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (existing != null) {
            mgr.cancel(existing);    // cancel it at the AlarmManager level
            existing.cancel();       // cancel the PendingIntent itself
        }

        // 2) Re-create a fresh one
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, med.id, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // compute next trigger time
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, med.hour);
        cal.set(Calendar.MINUTE, med.minute);
        cal.set(Calendar.SECOND, 0);
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DATE, 1);
        }

        mgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                pi
        );
    }

    public static void scheduleTestRepeatAlarm(Context ctx, int medId, String medName, String medDosage) {
        AlarmManager mgr = ctx.getSystemService(AlarmManager.class);

        // Build an Intent with only the fields you need for notification
        Intent i = new Intent(ctx, AlarmReceiver.class)
                .putExtra("medId", medId)
                .putExtra("medName", medName)
                .putExtra("medDosage", medDosage);
        // no need for frequency/hour/minute here

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, medId, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMs = System.currentTimeMillis() + 30_000L;  // 30 seconds

        mgr.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                pi
        );

        Log.d("MedAlarmManager", "Scheduled test alarm in 30s for medId=" + medId);
    }

    /** Schedule all existing meds from the DB. Call this on app startup or boot. */
    public static void scheduleAll(Context ctx, List<Medication> meds) {
        for (Medication med : meds) {
            scheduleRepeatingAlarm(ctx, med);
        }
    }
}
