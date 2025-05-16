package com.example.pantausehat.ui;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.data.MedicationDao;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MedicationAdapter adapter;
    private CountDownTimer countDownTimer;
    private long repeatIntervalMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Insets handling
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Next-dose UI
        TextView tvNextName = findViewById(R.id.tvNextMedName);
        tvNextName.setMaxLines(1);
        tvNextName.setEllipsize(TextUtils.TruncateAt.END);
        TextView tvNextDosage = findViewById(R.id.tvNextMedDosage);
        TextView tvCountdown = findViewById(R.id.tvCountdown);

        // RecyclerView setup
        RecyclerView rv = findViewById(R.id.rvMedications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationAdapter(med -> {
            new Thread(() -> {
                // Delete from DB
                AppDatabase.getInstance(MainActivity.this)
                        .medicationDao()
                        .delete(med);

                // Inside the delete handler:
                runOnUiThread(() -> {
                    MedAlarmManager.cancelAlarm(MainActivity.this, med.id);

                    // Cancel notification immediately
                    NotificationManager nm = (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(med.id); // Directly cancel using medId

                    Toast.makeText(this, "Dihapus “" + med.name + "”", Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
        rv.setAdapter(adapter);


        // FAB
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddMedicationActivity.class)));

        // Observe meds
        MedicationDao dao = AppDatabase.getInstance(this).medicationDao();
        dao.getAll().observe(this, meds -> {
            adapter.submitList(meds);

            // Cancel previous countdown
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }

            long now = System.currentTimeMillis();
            Medication nextMed = null;
            long nextTime = Long.MAX_VALUE;

            // Find next scheduled dose and compute interval
            for (Medication m : meds) {
                int hoursInterval = 24;
                if (m.frequency != null && m.frequency.startsWith("Setiap")) {
                    try {
                        hoursInterval = Integer.parseInt(m.frequency.split(" ")[1]);
                    } catch (Exception e) { }
                }
                long intervalMs = hoursInterval * AlarmManager.INTERVAL_HOUR;

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, m.hour);
                cal.set(Calendar.MINUTE, m.minute);
                cal.set(Calendar.SECOND, 0);
                long trigger = cal.getTimeInMillis();
                if (trigger <= now) {
                    long delta = now - trigger;
                    long intervalsPassed = delta / intervalMs + 1;
                    trigger += intervalsPassed * intervalMs;
                }

                if (trigger < nextTime) {
                    nextTime = trigger;
                    nextMed = m;
                    repeatIntervalMs = intervalMs;
                }
            }

            if (nextMed != null) {
                tvNextName.setText("Next: " + nextMed.name);
                tvNextDosage.setText(nextMed.dosage);
                long millisUntil = nextTime - now;
                startCountdown(millisUntil, tvCountdown);
            } else {
                tvNextName.setText("Tidak ada jadwal");
                tvNextDosage.setText("");
                tvCountdown.setText("");
            }

            MedAlarmManager.scheduleAll(this, meds);
        });
    }

    private void startCountdown(long initialDelayMs, TextView tvCountdown) {
        countDownTimer = new CountDownTimer(initialDelayMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hrs = millisUntilFinished / 3600000;
                long mins = (millisUntilFinished % 3600000) / 60000;
                long secs = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("00:00:00");
                // restart countdown for next interval
                countDownTimer = new CountDownTimer(repeatIntervalMs, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long hrs = millisUntilFinished / 3600000;
                        long mins = (millisUntilFinished % 3600000) / 60000;
                        long secs = (millisUntilFinished % 60000) / 1000;
                        tvCountdown.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
                    }

                    @Override
                    public void onFinish() {
                        tvCountdown.setText("00:00:00");
                    }
                }.start();
            }
        }.start();
    }
}