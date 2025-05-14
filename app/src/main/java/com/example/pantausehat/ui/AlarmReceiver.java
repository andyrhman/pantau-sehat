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
import com.example.pantausehat.util.MedAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "med_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medName   = intent.getStringExtra("medName");
        String medDosage = intent.getStringExtra("medDosage");

        createNotificationChannel(context);

        Intent tapIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle("Time to take your medication")
                .setContentText(medName + " — " + medDosage)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), builder.build());

        Medication med = new Medication();
        med.id        = intent.getIntExtra("medId", -1);

        if (med.id != -1) {
            Log.d("AlarmReceiver", "Rescheduling next exact alarm for medId=" + med.id);
            MedAlarmManager.scheduleRepeatingAlarm(context, med);
        }
    }

    private void createNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name        = "Medication Reminders";
            String description       = "Channel for medication alarms";
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
