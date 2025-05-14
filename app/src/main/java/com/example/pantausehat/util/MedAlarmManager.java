// File: MedAlarmManager.java in com.example.pantausehat.util

package com.example.pantausehat.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.pantausehat.data.Medication;
import com.example.pantausehat.ui.AlarmReceiver;

import java.util.Calendar;
import java.util.List;

public class MedAlarmManager {

    // Add these constants at the top
    private static final int FLAGS = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

    // Add this helper method
    private static void setExactAlarm(AlarmManager mgr, long triggerAt, PendingIntent pi) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                mgr.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException e) {
            mgr.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void scheduleAlarm(Context ctx, Medication med) {
        switch (med.frequencyType) {
            case "once":
                scheduleOneTimeAlarm(ctx, med);
                break;
            case "daily":
                scheduleDailyAlarm(ctx, med);
                break;
            case "hours":
                scheduleHourlyAlarm(ctx, med);
                break;
            case "custom":
                scheduleCustomDaysAlarm(ctx, med);
                break;
            default:
                Log.w("MedAlarmManager", "Unknown frequency type: " + med.frequencyType);
        }
    }

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

    private static void scheduleOneTimeAlarm(Context ctx, Medication med) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = buildBaseIntent(ctx, med);
        PendingIntent pi = createPendingIntent(ctx, med, intent);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, med.hour);
        cal.set(Calendar.MINUTE, med.minute);
        cal.set(Calendar.SECOND, 0);

        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DATE, 1);
        }

        setExactAlarm(mgr, cal.getTimeInMillis(), pi);
    }

    public static void scheduleHourlyAlarm(Context ctx, Medication med) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = buildBaseIntent(ctx, med);
        PendingIntent pi = createPendingIntent(ctx, med, intent);

        long intervalMillis = med.intervalHours * 3600_000L;
        long firstTrigger = System.currentTimeMillis() + intervalMillis;

        mgr.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTrigger,
                intervalMillis,
                pi
        );
    }

    private static void scheduleCustomDaysAlarm(Context ctx, Medication med) {
        if (med.days == null || med.days.isEmpty()) {
            Log.e("MedAlarmManager", "No days selected for custom alarm");
            return;
        }

        String[] days = med.days.split(",");
        for (String day : days) {
            scheduleWeeklyAlarmForDay(ctx, med, day.trim());
        }
    }

    private static void scheduleWeeklyAlarmForDay(Context ctx, Medication med, String dayAbbr) {
        AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = buildBaseIntent(ctx, med);

        // Use unique request code for each day
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                med.id + dayAbbr.hashCode(),  // Unique ID per day
                intent,
                FLAGS
        );

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, med.hour);
        cal.set(Calendar.MINUTE, med.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, getDayOfWeek(dayAbbr));

        // If the time has already passed this week, schedule for next week
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DATE, 7);
        }

        mgr.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pi
        );
    }

    private static PendingIntent createPendingIntent(Context ctx, Medication med, Intent intent) {
        return PendingIntent.getBroadcast(
                ctx,
                med.id,  // Unique ID using medication ID
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    // Add this helper method
    private static int getDayOfWeek(String dayAbbr) {
        switch (dayAbbr.toLowerCase()) {
            case "mon": return Calendar.MONDAY;
            case "tue": return Calendar.TUESDAY;
            case "wed": return Calendar.WEDNESDAY;
            case "thu": return Calendar.THURSDAY;
            case "fri": return Calendar.FRIDAY;
            case "sat": return Calendar.SATURDAY;
            case "sun": return Calendar.SUNDAY;
            default: return Calendar.MONDAY;
        }
    }

    private static Intent buildBaseIntent(Context ctx, Medication med) {
        return new Intent(ctx, AlarmReceiver.class)
                .putExtra("medId", med.id)
                .putExtra("medName", med.name)
                .putExtra("medDosage", med.dosage)
                .putExtra("medHour", med.hour)
                .putExtra("medMinute", med.minute)
                .putExtra("frequencyType", med.frequencyType)
                .putExtra("intervalHours", med.intervalHours)
                .putExtra("days", med.days);
    }

    /** Schedule all existing meds from the DB. Call this on app startup or boot. */
    public static void scheduleAll(Context ctx, List<Medication> meds) {
        for (Medication med : meds) {
            scheduleAlarm(ctx, med);
        }
    }
}
