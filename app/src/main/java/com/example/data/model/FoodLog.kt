package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,    // e.g. "SELASA, 24 OKT"
    val mealType: String,      // "BREAKFAST", "LUNCH", "DINNER", "SNACKS"
    val foodName: String,      // Nama makanan
    val portionInfo: String,   // Informasi porsi (e.g., "1 mangkuk (350g)")
    val calories: Int,         // Nilai kalori (Kcal)
    val protein: Int,          // Protein (g)
    val carbs: Int,            // Karbohidrat (g)
    val fat: Int               // Lemak (g)
)
