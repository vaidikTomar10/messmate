package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_library ORDER BY category ASC, name ASC")
    fun getAllFoodItemsFlow(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_library ORDER BY category ASC, name ASC")
    suspend fun getAllFoodItemsSync(): List<FoodItemEntity>

    @Query("SELECT * FROM food_library WHERE category = :category ORDER BY name ASC")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItemEntity>>

    @Query("SELECT DISTINCT category FROM food_library ORDER BY category ASC")
    fun getAllCategoriesFlow(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM food_library")
    suspend fun getFoodItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Update
    suspend fun updateFoodItem(item: FoodItemEntity)

    @Delete
    suspend fun deleteFoodItem(item: FoodItemEntity)

    @Query("DELETE FROM food_library WHERE id = :id")
    suspend fun deleteFoodItemById(id: Long)

    @Query("DELETE FROM food_library")
    suspend fun deleteAllFoodItems()
}
