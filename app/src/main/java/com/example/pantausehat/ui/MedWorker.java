package com.example.pantausehat.ui;  // Create this package

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.util.MedAlarmManager;
import java.util.List;

public class MedWorker extends Worker {

    public MedWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Use proper Context from Worker
        Context context = getApplicationContext();

        AppDatabase db = AppDatabase.getInstance(context);
        List<Medication> meds = db.medicationDao().getAllSynchronously();
        MedAlarmManager.scheduleAll(context, meds);

        return Result.success();
    }
}