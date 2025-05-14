package com.example.pantausehat.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.pantausehat.data.Medication;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device rebooted — rescheduling alarms");

            // Do DB work off the main thread:
            new Thread(() -> {
                List<Medication> meds = AppDatabase
                        .getInstance(ctx)
                        .medicationDao()
                        .getAllSynchronously();      // your synchronous DAO method

                MedAlarmManager.scheduleAll(ctx, meds);
            }).start();
        }
    }
}
