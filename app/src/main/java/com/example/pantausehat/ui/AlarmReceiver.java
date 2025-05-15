package com.example.pantausehat.ui;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.util.MedAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "med_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        int    medId     = intent.getIntExtra("medId", -1);
        String medName   = intent.getStringExtra("medName");
        String medDosage = intent.getStringExtra("medDosage");
        String medFrequency = intent.getStringExtra("medFrequency");

        // Show notification
        createNotificationChannel(context);
        showNotification(context, medName, medDosage, medId);

        // Reschedule next alarm based on frequency
        if (medId != -1 && medFrequency != null) {
            try {
                // Parse frequency (e.g., "Every 4 hours" → 4)
                String[] parts = medFrequency.split(" ");
                int hoursInterval = Integer.parseInt(parts[1]);
                long intervalMs = hoursInterval * 60 * 60 * 1000;

                // Schedule next alarm
                long nextTriggerTime = System.currentTimeMillis() + intervalMs;
                Intent nextIntent = new Intent(context, AlarmReceiver.class)
                        .putExtras(intent.getExtras());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, medId, nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextTriggerTime,
                            pendingIntent
                    );
                }
            } catch (Exception e) {
                Log.e("AlarmReceiver", "Error rescheduling alarm", e);
            }
        }
    }

    private void showNotification(Context context, String medName, String medDosage, int medId) {
        Intent tapIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, medId, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.pill_24px)
                .setContentTitle("Waktunya meminum obatmu 💊")
                .setContentText(medName + " — " + medDosage)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(medId, builder.build());
    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String medName   = intent.getStringExtra("medName");
//        String medDosage = intent.getStringExtra("medDosage");
//        int    medId     = intent.getIntExtra("medId", -1);
//        String freq      = intent.getStringExtra("medFrequency");
//
//        createNotificationChannel(context);
//        Intent tapIntent = new Intent(context, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(
//                context, 0, tapIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_add)
//                .setContentTitle("Time to take your medication")
//                .setContentText(medName + " — " + medDosage)
//                .setContentIntent(contentIntent)
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//        NotificationManager nm = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.notify((int) System.currentTimeMillis(), builder.build());
//
//        if (freq == null && medId != -1) {
//            MedAlarmManager.scheduleTestRepeatAlarm(
//                    context,
//                    medId,
//                    medName,
//                    medDosage
//            );
//        }
//    }

    private void createNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name        = "Jadwal Pengobatan";
            String description       = "Channel untuk alarm obat";
            int importance           = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID, name, importance
            );
            chan.setDescription(description);
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(chan);
        }
    }
}
