package com.example.pantausehat.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medications")
public class Medication {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "group_id")
    public long groupId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "dosage")
    public String dosage;

    @ColumnInfo(name = "frequency")
    public String frequency;

    @ColumnInfo(name = "hour")
    public int hour;

    @ColumnInfo(name = "minute")
    public int minute;
}