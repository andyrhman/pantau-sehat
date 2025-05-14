package com.example.pantausehat.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY hour, minute")
    LiveData<List<Medication>> getAll();

    @Insert
    long insert(Medication med);

    @Delete
    void delete(Medication med);

    @Update
    void update(Medication med);

    @Query("SELECT * FROM medications")
    List<Medication> getAllSynchronously();
}