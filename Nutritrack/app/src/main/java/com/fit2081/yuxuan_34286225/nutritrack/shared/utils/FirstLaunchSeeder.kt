package com.fit2081.yuxuan_34286225.nutritrack.shared.utils

import android.content.Context
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientDataSeeder
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Responsible for seeding the patient database on first launch of the app
 */
object FirstLaunchSeeder {
    private const val PREF_NAME ="nutritrack_prefs"
    private const val PREF_KEY = "is_db_initialised"

    /**
     * Use sharedpreferences to check and store the initialisation state
     */
    fun runIfNeeded(context: Context, repository: PatientsRepository){
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isInitialised = sharedPref.getBoolean(PREF_KEY, false)

        if (!isInitialised){
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    PatientDataSeeder(context, repository).setUpDatabase()
                    sharedPref.edit().putBoolean(PREF_KEY, true).apply()
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }
}