package com.fit2081.yuxuan_34286225.nutritrack.data.patient

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    /**
     * Inserts a new patient into the database
     */
    @Insert
    suspend fun insertPatient(patient: Patient)

    /**
     * Delete a patient from the database
     */
    @Delete
    suspend fun deletePatient(patient: Patient)

    /***
     * Update patient's username
     */
    @Query("UPDATE patients SET userName =:newUsername WHERE userId =:userId")
    suspend fun updateUsername(userId: String, newUsername: String)

    /**
     * Update patient's password
     */
    @Query("UPDATE patients SET userPassword =:newPassword WHERE userId =:userId")
    suspend fun updatePassword(userId: String, newPassword: String)

    /**
     * Calculate the average male score
     */
    @Query("SELECT AVG(totalScore) FROM patients WHERE LOWER(userSex) = 'male'")
    suspend fun getAverageMaleScore(): Float?

    /**
     * Calculate the average female score
     */
    @Query("SELECT AVG(totalScore) FROM patients WHERE LOWER(userSex) = 'female'")
    suspend fun getAverageFemaleScore(): Float?

    /**
     * Retrieves a patient from database based on their ID
     *
     * @param userId The ID of the patient
     * @return The [Patient] object if found
     */
    // get a specific patient from the database by userId
    @Query("SELECT * FROM patients WHERE userId = :userId")
    suspend fun getPatientById(userId: String): Patient


    @Query("SELECT userPassword FROM patients WHERE userId = :userId")
    suspend fun getPasswordByUserId(userId: String): String?

    /**
     * Retrieves all patients from the database
     *
     * @return A [Flow] emitting a list of all [Patient] objects in the database
     */
    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>

}