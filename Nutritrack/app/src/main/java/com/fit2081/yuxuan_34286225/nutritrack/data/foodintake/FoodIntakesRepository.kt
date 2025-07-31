package com.fit2081.yuxuan_34286225.nutritrack.data.foodintake

import android.content.Context
import com.fit2081.yuxuan_34286225.nutritrack.data.NutriDatabase
import kotlinx.coroutines.flow.Flow

class FoodIntakesRepository(context: Context) {
    // create an instance of FoodIntakeDAO
    private val foodIntakeDao = NutriDatabase.getDatabase(context).foodIntakeDao()

    /**
     * Retrieve all food intake from the database
     */
    val allFoodIntake: Flow<List<FoodIntake>> = foodIntakeDao.getAllFoodIntake()

    /**
     * Insert a new food intake into the database
     * @param foodIntake The [FoodIntake] object to be inserted
     */
    suspend fun insertFoodIntake(foodIntake: FoodIntake){
        foodIntakeDao.insertFoodIntake(foodIntake)
    }

    /**
     * Retrieve food intake for specific user ID
     * @param patientId The ID of patient
     * @return A Flow emitting a list of food intakes for specific patient
     */
    fun getFoodIntakesByPatientId(patientId: String): Flow<FoodIntake?> =
        foodIntakeDao.getFoodIntakesByPatientId(patientId)

}