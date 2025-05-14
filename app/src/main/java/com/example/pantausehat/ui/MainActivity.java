package com.example.pantausehat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantausehat.R;
import com.example.pantausehat.data.MedicationDao;
import com.example.pantausehat.db.AppDatabase;
import com.example.pantausehat.util.MedAlarmManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MedicationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // handle window insets
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // RecyclerView + Adapter
        RecyclerView rv = findViewById(R.id.rvMedications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationAdapter();
        rv.setAdapter(adapter);

        // FAB opens AddMedicationActivity
        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, AddMedicationActivity.class))
        );

        // Observe Room data
        MedicationDao dao = AppDatabase.getInstance(this).medicationDao();
        dao.getAll().observe(this, meds -> {
            Log.d("MainActivity", "Loaded " + meds.size() + " meds from DB");
            adapter.submitList(meds);

            MedAlarmManager.scheduleAll(this, meds);
        });

    }
}
