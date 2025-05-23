package com.example.pantausehat.ui;

import android.app.AlarmManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;


public class AddMedicationActivity extends AppCompatActivity {
    private TextInputEditText etMedName, etDosage;
    private Spinner spinnerFrequency;
    private TimePicker timePicker;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        etMedName = findViewById(R.id.etMedName);
        etDosage = findViewById(R.id.etDosage);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);

        timePicker.setIs24HourView(false);

        btnSave.setOnClickListener(v -> {
            String name     = etMedName.getText().toString().trim();
            String dosage   = etDosage.getText().toString().trim();
            int hourStart   = timePicker.getHour();
            int minuteStart = timePicker.getMinute();
            String freqText = spinnerFrequency.getSelectedItem().toString();

            if (name == null || name.isEmpty()) {
                Toast.makeText(this, "Masukkan nama (misal, Minum obat demam)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dosage == null || dosage.isEmpty()) {
                Toast.makeText(this, "Masukkan dosis yang tertulis di obat", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.length() > 100) {
                Toast.makeText(this, "Nama terlalu panjang (maks. 100 karakter)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dosage.length() > 100) {
                Toast.makeText(this, "Dosis terlalu panjang (maks. 100 karakter)", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts      = freqText.toLowerCase().split("\\s+");     // ["setiap","4","jam"]
            int intervalHours   = Integer.parseInt(parts[1]);
            long intervalMs     = intervalHours * AlarmManager.INTERVAL_HOUR;
            int slots           = (int)(AlarmManager.INTERVAL_DAY / intervalMs);

            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(AddMedicationActivity.this);

                Calendar baseCal = Calendar.getInstance();
                baseCal.set(Calendar.HOUR_OF_DAY, hourStart);
                baseCal.set(Calendar.MINUTE, minuteStart);
                baseCal.set(Calendar.SECOND, 0);
                long baseTime = baseCal.getTimeInMillis();
                long groupId = System.currentTimeMillis();

                for (int slot = 0; slot < slots; slot++) {
                    long slotTime = baseTime + slot * intervalMs;
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(slotTime);

                    Medication m = new Medication();
                    m.name   = name;
                    m.dosage = dosage;
                    m.hour   = c.get(Calendar.HOUR_OF_DAY);
                    m.minute = c.get(Calendar.MINUTE);
                    m.frequency = freqText;
                    m.groupId   = groupId;

                    long newId = db.medicationDao().insert(m);
                    if (newId > 0) {
                        m.id = (int)newId;
                        MedAlarmManager.scheduleDailyAlarm(
                                AddMedicationActivity.this,
                                m
                        );
                    }
                }
            }).start();

            finish();
        });
    }
}