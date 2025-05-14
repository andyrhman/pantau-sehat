package com.example.pantausehat.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medications")
public class Medication {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "dosage")
    public String dosage;

    @ColumnInfo(name = "hour")
    public int hour;            // 0–23 from TimePicker

    @ColumnInfo(name = "minute")
    public int minute;          // 0–59
}