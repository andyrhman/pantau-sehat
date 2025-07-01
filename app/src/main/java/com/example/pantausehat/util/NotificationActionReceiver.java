package com.example.pantausehat.util;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.pantausehat.data.Medication;
import com.example.pantausehat.data.MedicationDao;
import com.example.pantausehat.db.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int    medId  = intent.getIntExtra("medId",   -1);
        long   grpId  = intent.getLongExtra("groupId", -1L);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Executors.newSingleThreadExecutor().execute(() -> {
            MedicationDao dao = AppDatabase.getInstance(context).medicationDao();

            if ("ACTION_DELETE_ONE".equals(action) && medId >= 0) {
                MedAlarmManager.cancelAlarm(context, medId);

                dao.deleteByIdSync(medId);

                new Handler(Looper.getMainLooper()).post(() -> {
                    nm.cancel(medId);
                    Toast.makeText(context,
                            "Satu jadwal dihapus", Toast.LENGTH_SHORT).show();
                });
            }
            else if ("ACTION_DELETE_GROUP".equals(action) && grpId >= 0) {
                List<Medication> list = dao.getByGroupSync(grpId);

                for (Medication m : list) {
                    MedAlarmManager.cancelAlarm(context, m.id);
                    dao.deleteByIdSync(m.id);
                }
                dao.deleteByGroupSync(grpId);

                new Handler(Looper.getMainLooper()).post(() -> {
                    for (Medication m : list) {
                        nm.cancel(m.id);
                    }
                    Toast.makeText(context,
                            "Semua jadwal dihapus", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}

