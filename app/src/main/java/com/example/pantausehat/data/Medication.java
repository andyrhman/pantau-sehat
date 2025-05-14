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

    @ColumnInfo(name = "frequency")
    public String frequency;    // e.g., "Twice a day"

    @ColumnInfo(name = "hour")
    public int hour;            // 0–23 from TimePicker

    @ColumnInfo(name = "minute")
    public int minute;          // 0–59

    @ColumnInfo(name = "frequency_type")
    public String frequencyType; // "once", "daily", "hours", "custom"

    @ColumnInfo(name = "interval_hours")
    public int intervalHours;    // Only for hourly frequency

    @ColumnInfo(name = "days")
    public String days;          // Comma-separated days e.g., "Mon,Tue,Wed"
}