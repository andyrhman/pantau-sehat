package com.example.pantausehat.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.data.MedicationDao;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

public class MainActivity extends AppCompatActivity {
    private ExpandableListView elvMeds;
    private CountDownTimer countDownTimer;
    private static final long DAY_MS = AlarmManager.INTERVAL_DAY;
    private List<List<Medication>> childLists = Collections.emptyList();
    private static final int REQ_NOTIFY = 1001;
    private long computeDelayToNext(List<List<Medication>> childLists) {
        long now = System.currentTimeMillis();
        long best = Long.MAX_VALUE;

        for (List<Medication> slots : childLists) {
            for (Medication m : slots) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, m.hour);
                cal.set(Calendar.MINUTE, m.minute);
                cal.set(Calendar.SECOND, 0);
                long t = cal.getTimeInMillis();
                if (t <= now) t += DAY_MS;
                if (t < best) best = t;
            }
        }
        return (best == Long.MAX_VALUE) ? DAY_MS : (best - now);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAndRequestNotificationPermission();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
            startActivity(
                    new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            );
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkExactAlarmPermission();
        requestAutoStartPermission();
        elvMeds = findViewById(R.id.elvMedications);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        TextView tvNextName = findViewById(R.id.tvNextMedName);
        tvNextName.setMaxLines(1);
        tvNextName.setEllipsize(TextUtils.TruncateAt.END);
        TextView tvNextDosage = findViewById(R.id.tvNextMedDosage);
        TextView tvCountdown = findViewById(R.id.tvCountdown);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddMedicationActivity.class)));

        MedicationDao dao = AppDatabase.getInstance(this).medicationDao();
        dao.getAll().observe(this, meds -> {

            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }

            Map<Long, List<Medication>> grouped = new LinkedHashMap<>();
            for (Medication m : meds) {
                List<Medication> list = grouped.get(m.groupId);
                if (list == null) {
                    list = new ArrayList<>();
                    grouped.put(m.groupId, list);
                }
                list.add(m);
            }

            List<Long> groupIds = new ArrayList<>(grouped.keySet());
            List<String> groupTitles = new ArrayList<>();
            this.childLists = childLists = new ArrayList<>();

            for (Map.Entry<Long, List<Medication>> e : grouped.entrySet()) {
                List<Medication> slots = e.getValue();

                Collections.sort(slots, Comparator.comparingInt(x -> x.hour * 60 + x.minute));

                Medication first = slots.get(0);
                String title = first.name
                        + " — " + first.dosage
                        + " (" + first.frequency + ")";
                groupTitles.add(title);
                childLists.add(slots);
            }

            MyExpandableAdapter expAdapter = new MyExpandableAdapter(
                    this,
                    groupIds,
                    groupTitles,
                    childLists,
                    med -> {
                        // ketika tombol hapus ditekan.
                        new Thread(() -> {
                            AppDatabase.getInstance(MainActivity.this)
                                    .medicationDao()
                                    .delete(med);

                            MedAlarmManager.cancelAlarm(MainActivity.this, med.id);

                            NotificationManager nm =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            nm.cancel(med.id);

                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "Dihapus “" + med.name + "”",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                        }).start();
                    },
                    // hapus dengan grup
                    gid -> {
                        new Thread(() -> {
                            List<Medication> toDelete = dao.getByGroup(gid);

                            for (Medication m : toDelete) {
                                MedAlarmManager.cancelAlarm(this, m.id);
                            }

                            dao.deleteByGroup(gid);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Semua jadwal dihapus", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    }
            );
            elvMeds.setAdapter(expAdapter);

            long now = System.currentTimeMillis();
            Medication nextMed = null;
            long nextTime = Long.MAX_VALUE;

            for (List<Medication> slots : childLists) {
                for (Medication m : slots) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, m.hour);
                    cal.set(Calendar.MINUTE, m.minute);
                    cal.set(Calendar.SECOND, 0);
                    long t = cal.getTimeInMillis();
                    if (t <= now) t += DAY_MS;
                    if (t < nextTime) {
                        nextTime = t;
                        nextMed  = m;
                    }
                }
            }

            if (nextMed != null) {
                tvNextName.setText("Next: " + nextMed.name);
                tvNextDosage.setText(nextMed.dosage);
                startCountdown(computeDelayToNext(childLists), tvCountdown);
            } else {
                tvNextName.setText("Tidak ada jadwal");
                tvNextDosage.setText("");
                tvCountdown.setText("");
            }
        });
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void requestAutoStartPermission() {
        Intent intent = new Intent();
        String manufacturer = android.os.Build.MANUFACTURER;
        if ("realme".equalsIgnoreCase(manufacturer) || "oppo".equalsIgnoreCase(manufacturer)) {
            intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity");
        } else if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        } else if ("huawei".equalsIgnoreCase(manufacturer)) {
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
        } else {
            return;
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountdown(long initialDelayMs, TextView tvCountdown) {
        countDownTimer = new CountDownTimer(initialDelayMs, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hrs  = millisUntilFinished / 3_600_000;
                long mins = (millisUntilFinished % 3_600_000) / 60_000;
                long secs = (millisUntilFinished % 60_000) / 1_000;

                // build verbose string
                StringBuilder sb = new StringBuilder();
                sb.append(hrs).append(" jam ").append(mins).append(" menit ").append(secs).append(" detik");

                tvCountdown.setText(sb.toString());
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("0 jam 0 menit 0 detik");
                long nextDelay = computeDelayToNext(childLists);
                startCountdown(nextDelay, tvCountdown);
            }
        }.start();
    }

    private void checkAndRequestNotificationPermission() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // ask the user
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                            REQ_NOTIFY
                    );
                    return;
                }
            }
            openNotificationSettings();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFY) {
            if (grantResults.length == 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                openNotificationSettings();
            }
        }
    }

    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        }
        startActivity(intent);
    }
}