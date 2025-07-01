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

    @Query("SELECT * FROM medications WHERE group_id = :groupId")
    List<Medication> getByGroup(long groupId);

    @Query("DELETE FROM medications WHERE group_id = :groupId")
    void deleteByGroup(long groupId);

    // get a single med
    @Query("SELECT * FROM medications WHERE id = :id")
    Medication getByIdSync(int id);

    // delete a single med
    @Query("DELETE FROM medications WHERE id = :id")
    void deleteByIdSync(int id);

    // get all in a group
    @Query("SELECT * FROM medications WHERE group_id = :groupId")
    List<Medication> getByGroupSync(long groupId);

    // delete all in a group
    @Query("DELETE FROM medications WHERE group_id = :groupId")
    void deleteByGroupSync(long groupId);
}