package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_progress")
data class DailyProgress(
    @PrimaryKey val dateString: String, // e.g. "SELASA, 24 OKT"
    val stepsLogged: Int = 0,
    val stepsGoal: Int = 10000,
    val waterLogged: Float = 0.0f,     // e.g. 1.5 (in Liters)
    val waterGoal: Float = 2.5f,       // e.g. 2.5 (in Liters)
    val weight: Double = 74.5          // e.g. 74.5 kg
)
