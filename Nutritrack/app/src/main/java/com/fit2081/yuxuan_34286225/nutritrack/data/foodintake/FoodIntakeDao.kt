package com.fit2081.yuxuan_34286225.nutritrack.data.foodintake

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodIntakeDao {
    /**
     * Inserts a new [FoodIntake] into database
     *
     * @param foodIntake the [FoodIntake] object to be inserted
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodIntake(foodIntake: FoodIntake)

    /**
     * Retrieves all [FoodIntake]s from the database
     */
    @Query("SELECT * FROM food_intake")
    fun getAllFoodIntake(): Flow<List<FoodIntake>>

    /**
     * Retrieves all [FoodIntake]s for specific patient ID
     * reactive data from Compose
     */
    @Query("SELECT * FROM food_intake where patientId = :patientId")
    fun getFoodIntakesByPatientId(patientId: String): Flow<FoodIntake?>
}