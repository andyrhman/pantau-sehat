package com.example.pantausehat.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;
import com.google.android.material.textfield.TextInputEditText;


public class AddMedicationActivity extends AppCompatActivity {

    private TextInputEditText etMedName, etDosage;
    private TimePicker timePicker;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        etMedName = findViewById(R.id.etMedName);
        etDosage = findViewById(R.id.etDosage);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);

        // Use 24-hour mode or AM/PM as you prefer
        timePicker.setIs24HourView(false);

        btnSave.setOnClickListener(v -> {
            final Medication med = new Medication();
            med.name      = etMedName.getText().toString().trim();
            med.dosage    = etDosage.getText().toString().trim();
            med.hour      = timePicker.getHour();
            med.minute    = timePicker.getMinute();

            new Thread(() -> {
                long newId = AppDatabase
                        .getInstance(AddMedicationActivity.this)
                        .medicationDao()
                        .insert(med);
                if (newId > 0) {
                    MedAlarmManager.scheduleDailyAlarm(AddMedicationActivity.this, med);
                } else {
                    Log.e("AddMedication", "Insert failed!");
                }
            }).start();

            finish();  // back to MainActivity
        });
    }
}
