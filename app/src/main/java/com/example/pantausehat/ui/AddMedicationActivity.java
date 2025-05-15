package com.example.pantausehat.ui;

import android.os.Bundle;
import android.util.Log;
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
            final Medication med = new Medication();
            med.name      = etMedName.getText().toString().trim();
            med.dosage    = etDosage.getText().toString().trim();
            med.frequency = spinnerFrequency.getSelectedItem().toString();
            med.hour      = timePicker.getHour();
            med.minute    = timePicker.getMinute();

            if (med.name == null || med.name.isEmpty()) {
                Toast.makeText(this, "Masukkan nama (misal, Minum obat demam)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (med.dosage == null || med.dosage.isEmpty()) {
                Toast.makeText(this, "Masukkan dosis yang tertulis di obat", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                long newId = AppDatabase
                        .getInstance(AddMedicationActivity.this)
                        .medicationDao()
                        .insert(med);
                if (newId > 0) {
                    MedAlarmManager.scheduleRepeatingAlarm(this, med);
                } else {
                    Log.e("AddMedication", "Insert failed!");
                }
            }).start();

            finish();
        });
    }
}