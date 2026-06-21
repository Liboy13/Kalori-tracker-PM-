package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateLabel: String,  // e.g. "01 Okt", "15 Okt"
    val weight: Double,     // e.g. 78.5
    val timestamp: Long     // for sorting chronologically
)
