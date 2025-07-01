package com.example.pantausehat.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.pantausehat.data.Medication;
import com.example.pantausehat.ui.AlarmReceiver;
import com.example.pantausehat.ui.MedWorker;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MedAlarmManager {
    public static void scheduleDailyAlarm(Context ctx, Medication med) {
        AlarmManager am = ctx.getSystemService(AlarmManager.class);

        Intent i0 = new Intent(ctx, AlarmReceiver.class)
         .putExtra("medId",     med.id)
         .putExtra("medName",   med.name)
         .putExtra("medDosage", med.dosage)
         .putExtra("groupId",   med.groupId);
        PendingIntent old = PendingIntent.getBroadcast(
                ctx, med.id, i0,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (old != null) {
            am.cancel(old);
            old.cancel();
        }

        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, med.id, i0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, med.hour);
        cal.set(Calendar.MINUTE, med.minute);
        cal.set(Calendar.SECOND, 0);
        long trigger = cal.getTimeInMillis();
        if (trigger <= System.currentTimeMillis()) {
            trigger += AlarmManager.INTERVAL_DAY;
        }

        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger,
                pi
        );
    }

    public static void cancelAlarm(Context ctx, int medId) {
        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx, medId, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }


    public static void scheduleAll(Context ctx, List<Medication> meds) {
        for (Medication med : meds) {
            scheduleDailyAlarm(ctx, med);
            scheduleWorkManagerBackup(ctx, med);
        }
    }

    private static void scheduleWorkManagerBackup(Context ctx, Medication med) {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MedWorker.class)
                .setInitialDelay(1, TimeUnit.HOURS)
                .addTag("med_" + med.id)
                .build();

        WorkManager.getInstance(ctx).enqueue(workRequest);
    }
}
