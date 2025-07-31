package com.fit2081.yuxuan_34286225.nutritrack.data.patient

import android.content.Context
import kotlinx.coroutines.flow.Flow
import com.fit2081.yuxuan_34286225.nutritrack.data.NutriDatabase

class PatientsRepository(context: Context) {
    // Get the PatientDao instance from database
    private val patientDao = NutriDatabase.getDatabase(context).patientDao()

    /**
     * Inserts a new patient into the database
     * @param patient The patient object to be inserted
     */
    suspend fun insertPatient(patient: Patient) {
        return patientDao.insertPatient(patient)
    }

    /**
     * Update patient's username
     */
    suspend fun updateUsername(userId: String, newUsername: String){
        return patientDao.updateUsername(userId, newUsername)
    }

    /**
     * Update patient's password
     */
    suspend fun updatePassword(userId: String, newPassword: String){
        return patientDao.updatePassword(userId, newPassword)
    }

    /**
     * Get password by user Id
     */
    suspend fun getPasswordById(userId: String): String? {
        return patientDao.getPasswordByUserId(userId)
    }

    /**
     * Get the average HEIFA male score
     */
    suspend fun getAverageMaleScore(): Float {
        return patientDao.getAverageMaleScore() ?: 0f
    }

    /**
     * Get the average HEIFA female score
     */
    suspend fun getAverageFemaleScore(): Float {
        return patientDao.getAverageFemaleScore() ?: 0f
    }


    /**
     * Retrieves patient from database by their ID
     * @param userId The ID of the patient
     * @return The patient object with given ID
     */
    suspend fun getPatientById(userId: String): Patient {
        return patientDao.getPatientById(userId)
    }


    /**
     * Retrieves all patients from database as Flow
     * @return A Flow emitting a list of all students
     */
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAllPatients()
}