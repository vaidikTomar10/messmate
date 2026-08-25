package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY dayOfWeek ASC, CASE mealType WHEN 'BREAKFAST' THEN 1 WHEN 'LUNCH' THEN 2 WHEN 'SNACKS' THEN 3 WHEN 'DINNER' THEN 4 ELSE 5 END ASC")
    fun getAllMealsFlow(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals ORDER BY dayOfWeek ASC, CASE mealType WHEN 'BREAKFAST' THEN 1 WHEN 'LUNCH' THEN 2 WHEN 'SNACKS' THEN 3 WHEN 'DINNER' THEN 4 ELSE 5 END ASC")
    suspend fun getAllMealsSync(): List<MealEntity>

    @Query("SELECT * FROM meals WHERE dayOfWeek = :dayOfWeek ORDER BY CASE mealType WHEN 'BREAKFAST' THEN 1 WHEN 'LUNCH' THEN 2 WHEN 'SNACKS' THEN 3 WHEN 'DINNER' THEN 4 ELSE 5 END ASC")
    fun getMealsForDayFlow(dayOfWeek: Int): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE dayOfWeek = :dayOfWeek ORDER BY CASE mealType WHEN 'BREAKFAST' THEN 1 WHEN 'LUNCH' THEN 2 WHEN 'SNACKS' THEN 3 WHEN 'DINNER' THEN 4 ELSE 5 END ASC")
    suspend fun getMealsForDaySync(dayOfWeek: Int): List<MealEntity>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: Long): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<MealEntity>)

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query("UPDATE meals SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun getMealCount(): Int
}
