package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meals",
    indices = [Index(value = ["dayOfWeek", "mealType"], unique = true)]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val mealType: String, // BREAKFAST, LUNCH, SNACKS, DINNER
    val items: String,
    val startTime: String, // "HH:mm" e.g. "07:30"
    val endTime: String,   // "HH:mm" e.g. "09:30"
    val specialNote: String = "",
    val isFavorite: Boolean = false
)
