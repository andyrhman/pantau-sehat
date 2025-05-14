package com.example.pantausehat.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMedicationActivity extends AppCompatActivity {
    private LinearLayout daySelector;
    private Chip chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun;
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

        // Use 24-hour mode or AM/PM as you prefer
        timePicker.setIs24HourView(false);
        daySelector = findViewById(R.id.daySelector);
        chipMon = findViewById(R.id.chipMon);
        chipTue = findViewById(R.id.chipTue);
        chipWed = findViewById(R.id.chipWed);
        chipThu = findViewById(R.id.chipThu);
        chipFri = findViewById(R.id.chipFri);
        chipSat = findViewById(R.id.chipSat);
        chipSun = findViewById(R.id.chipSun);

        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                daySelector.setVisibility(selected.equals("Custom days") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> {
            final Medication med = new Medication();
            med.name      = etMedName.getText().toString().trim();
            med.dosage    = etDosage.getText().toString().trim();
            med.frequency = spinnerFrequency.getSelectedItem().toString();
            med.hour      = timePicker.getHour();
            med.minute    = timePicker.getMinute();

            String frequency = spinnerFrequency.getSelectedItem().toString();
            setFrequencyType(med, frequency);

            new Thread(() -> {
                long newId = AppDatabase
                        .getInstance(AddMedicationActivity.this)
                        .medicationDao()
                        .insert(med);
                if (newId > 0) {
                    MedAlarmManager.scheduleAlarm(AddMedicationActivity.this, med);
                } else {
                    Log.e("AddMedication", "Insert failed!");
                }
            }).start();

            finish();  // back to MainActivity
        });
    }


    private void setFrequencyType(Medication med, String frequency) {
        if (frequency.equals("Once")) {
            med.frequencyType = "once";
        } else if (frequency.equals("Daily")) {
            med.frequencyType = "daily";
        } else if (frequency.startsWith("Every ")) {
            med.frequencyType = "hours";
            med.intervalHours = Integer.parseInt(frequency.replaceAll("\\D+", ""));
        } else if (frequency.equals("Custom days")) {
            med.frequencyType = "custom";
            med.days = getSelectedDays();
        }
    }

    private String getSelectedDays() {
        List<String> days = new ArrayList<>();
        if (chipMon.isChecked()) days.add("Mon");
        if (chipTue.isChecked()) days.add("Tue");
        // ... check other chips ...
        return TextUtils.join(",", days);
    }
}
