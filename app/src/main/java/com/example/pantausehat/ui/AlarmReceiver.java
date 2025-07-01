package com.example.pantausehat.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.data.MedicationDao;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.NotificationActionReceiver;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "med_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        int medId    = intent.getIntExtra("medId",   -1);
        long groupId = intent.getLongExtra("groupId", -1L);
        if (medId < 0) return;

        String name   = intent.getStringExtra("medName");
        String dosage = intent.getStringExtra("medDosage");

        createNotificationChannel(context);
        showNotification(context, name, dosage, medId, groupId);
    }

    private void showNotification(Context context, String medName, String medDosage, int medId, long groupId) {
        Intent tapIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, medId, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent delOne = new Intent(context, NotificationActionReceiver.class);
        delOne.setAction("ACTION_DELETE_ONE");
        delOne.putExtra("medId", medId);
        PendingIntent pDelOne = PendingIntent.getBroadcast(
                context, medId, delOne,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 2) Intent to delete the entire group:
        Intent delAll = new Intent(context, NotificationActionReceiver.class)
                .setAction("ACTION_DELETE_GROUP")
                .putExtra("groupId", groupId);

        PendingIntent pDelAll = PendingIntent.getBroadcast(
                context,
                medId + 10000,
                delAll,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.pill_24px)
                .setContentTitle("Waktunya minum obat 💊")
                .setContentText(medName + " — " + medDosage)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.ic_delete, "Hapus satu", pDelOne)
                .addAction(R.drawable.ic_delete_sweep, "Hapus semua", pDelAll)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(medId, builder.build());
    }

    private long getGroupIdForMed(Context ctx, int medId) {
        MedicationDao dao = AppDatabase.getInstance(ctx).medicationDao();
        Medication m = dao.getByIdSync(medId);  // write a synchronous DAO query
        return (m != null) ? m.groupId : -1;
    }
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
