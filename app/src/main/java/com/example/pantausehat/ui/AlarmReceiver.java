package com.example.pantausehat.ui;

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
import com.example.pantausehat.ui.MainActivity;
import com.example.pantausehat.util.MedAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "med_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract all data from the intent
        int medId = intent.getIntExtra("medId", -1);
        String medName = intent.getStringExtra("medName");
        String medDosage = intent.getStringExtra("medDosage");
        int medHour = intent.getIntExtra("medHour", 0);
        int medMinute = intent.getIntExtra("medMinute", 0);
        String frequencyType = intent.getStringExtra("frequencyType");
        int intervalHours = intent.getIntExtra("intervalHours", 0);
        String days = intent.getStringExtra("days");

        // Create notification
        createNotificationChannel(context);
        showNotification(context, medName, medDosage);

        // Only reschedule if not a one-time alarm
        if (!"once".equals(frequencyType)) {
            Medication med = new Medication();
            med.id = medId;
            med.name = medName;
            med.dosage = medDosage;
            med.hour = medHour;
            med.minute = medMinute;
            med.frequencyType = frequencyType;
            med.intervalHours = intervalHours;
            med.days = days;

            MedAlarmManager.scheduleAlarm(context, med);
        }

        if ("hours".equals(frequencyType)) {
            Medication med = new Medication();
            med.id = medId;
            med.name = medName;
            med.dosage = medDosage;
            med.frequencyType = frequencyType;
            med.intervalHours = intervalHours;

            // Schedule next alarm
            MedAlarmManager.scheduleHourlyAlarm(context, med);
            Log.d("AlarmReceiver", "Rescheduled hourly alarm for " + medName);
        }
    }

    private void showNotification(Context context, String medName, String medDosage) {
        Intent tapIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle("Time to take your medication")
                .setContentText(medName + " — " + medDosage)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        nm.notify((int) System.currentTimeMillis(), builder.build());
        Log.d("AlarmReceiver", "Notification sent for " + medName);
    }

    private void createNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for medication alarms");
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
