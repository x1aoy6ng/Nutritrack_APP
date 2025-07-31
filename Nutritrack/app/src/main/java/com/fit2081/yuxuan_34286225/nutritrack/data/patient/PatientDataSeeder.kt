package com.fit2081.yuxuan_34286225.nutritrack.data.patient

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.sequences.forEach

class PatientDataSeeder(
    private val context: Context,
    private val repository: PatientsRepository
) {
    suspend fun setUpDatabase() = withContext(Dispatchers.IO) {
        val patients = loadUserData("data.csv")
        patients.forEach { repository.insertPatient(it) }
    }

    /**
     * Function for loading user data from csv file
     *
     * @param context: the context used to access the app's assets
     * @param fileName: name of the csv file
     * */
    private fun loadUserData(fileName: String): List<Patient> {
        val patients = mutableListOf<Patient>()
        try {
            // open and read the csv file
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // read header line and create a map: column name -> index
            val headerMap =
                reader.readLine().split(",").mapIndexed { index, name -> name.trim() to index }
                    .toMap()
            reader.useLines { lines ->
                lines.forEach { line ->
                    val values = line.split(",")

                    // ensure the valid line length
                    if (values.size < headerMap.size) return@forEach

                    val sex = values[headerMap["Sex"]!!].trim()

                    fun col(name: String): Float {
                        val idx = headerMap[name] ?: return 0f
                        return values[idx].trim().toFloatOrNull() ?: 0f
                    }

                    val patient = Patient(
                        userId = values[headerMap["User_ID"]!!].trim(),
                        phoneNum = values[headerMap["PhoneNumber"]!!].trim(),
                        userSex = sex,
                        totalScore = col(if (sex == "Male") "HEIFAtotalscoreMale" else "HEIFAtotalscoreFemale"),
                        discretionaryScore = col(if (sex == "Male") "DiscretionaryHEIFAscoreMale" else "DiscretionaryHEIFAscoreFemale"),
                        vegetableScore = col(if (sex == "Male") "VegetablesHEIFAscoreMale" else "VegetablesHEIFAscoreFemale"),
                        fruitScore = col(if (sex == "Male") "FruitHEIFAscoreMale" else "FruitHEIFAscoreFemale"),
                        grainsAndCerealScore = col(if (sex == "Male") "GrainsandcerealsHEIFAscoreMale" else "GrainsandcerealsHEIFAscoreFemale"),
                        wholeGrainScore = col(if (sex == "Male") "WholegrainsHEIFAscoreMale" else "WholegrainsHEIFAscoreFemale"),
                        meatAndAlternativesScore = col(if (sex == "Male") "MeatandalternativesHEIFAscoreMale" else "MeatandalternativesHEIFAscoreFemale"),
                        diaryScore = col(if (sex == "Male") "DairyandalternativesHEIFAscoreMale" else "DairyandalternativesHEIFAscoreFemale"),
                        sodiumScore = col(if (sex == "Male") "SodiumHEIFAscoreMale" else "SodiumHEIFAscoreFemale"),
                        alcoholScore = col(if (sex == "Male") "AlcoholHEIFAscoreMale" else "AlcoholHEIFAscoreFemale"),
                        waterScore = col(if (sex == "Male") "WaterHEIFAscoreMale" else "WaterHEIFAscoreFemale"),
                        sugarScore = col(if (sex == "Male") "SugarHEIFAscoreMale" else "SugarHEIFAscoreFemale"),
                        saturatedFatScore = col(if (sex == "Male") "SaturatedFatHEIFAscoreMale" else "SaturatedFatHEIFAscoreFemale"),
                        unsaturatedFatScore = col(if (sex == "Male") "UnsaturatedFatHEIFAscoreMale" else "UnsaturatedFatHEIFAscoreFemale"),
                        fruitServeSize = values[headerMap["Fruitservesize"]!!].trim().toFloat(),
                        fruitVariation = values[headerMap["Fruitvariationsscore"]!!].trim().toFloat()
                    )
                    patients.add(patient)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return patients
    }
}