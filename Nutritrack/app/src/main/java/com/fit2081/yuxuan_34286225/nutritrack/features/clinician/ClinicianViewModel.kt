package com.fit2081.yuxuan_34286225.nutritrack.features.clinician

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.Patient
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClinicianViewModel(context: Context) : ViewModel(){
    private val patientsRepository = PatientsRepository(context = context)
    private val validKey = "dollar-entry-apples"

    private val _clinicianKey = MutableStateFlow("")
    val clinicianKey: StateFlow<String> = _clinicianKey.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var avgMaleScore by mutableStateOf(0f)
        private set

    var avgFemaleScore by mutableStateOf(0f)
        private set

    var patients by mutableStateOf<List<Patient>>(emptyList())
        private set

    var showValidationErrors by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            avgMaleScore = patientsRepository.getAverageMaleScore()
            avgFemaleScore = patientsRepository.getAverageFemaleScore()

            patientsRepository.getAllPatients().collectLatest { patientsList ->
                patients = patientsList
            }
        }
    }

    /**
     * Validate the entered clinician key against the predefined valid key
     * Update the login state and show error message if invalid key
     */
    fun onLoginClick(){
        isLoading = true
        viewModelScope.launch {
            // to-do: verify the admin credentials
            if (clinicianKey.value == validKey){
                isLoggedIn = true
                errorMessage = null
            } else {
                enableValidationErrors()
                errorMessage = "Invalid clinician key"
                isLoggedIn = false
            }
            isLoading = false
        }
    }

    /**
     * Enables showing validation error messages in ui
     */
    fun enableValidationErrors(){
        showValidationErrors = true
    }

    /**
     * Updates the clinician key as what the user types
     */
    fun onKeyChange(inputKey: String){
        _clinicianKey.value = inputKey
        errorMessage = null
    }

    /**
     * Reset the login related variables
     */
    fun clearState(){
        _clinicianKey.value = ""
        isLoggedIn = false
        errorMessage = null
        showValidationErrors = false
    }

    /**
     * generate a csv formatted string containing all the patient nutrition data
     * which will be send to ai for analysis
     */
    fun generatePatientDataset(): String{
        return buildString {
            appendLine("userId, userSex, totalHeiFaScore, discretionaryScore, " +
                    "vegetableScore, fruitScore, grainsAndCerealScore, wholeGrainScore, " +
                "meatAndAlternativesScore, diaryScore, sodiumScore, alcoholScore, waterScore, " +
                "sugarScore, saturatedFatScore, unsaturatedFatScore"
            )
            patients.forEach {
                appendLine("${it.userId}, ${it.userSex}, ${it.totalScore}, ${it.discretionaryScore}, " +
                        "${it.vegetableScore}, ${it.fruitScore}, ${it.grainsAndCerealScore}, ${it.wholeGrainScore}," +
                        "${it.meatAndAlternativesScore}, ${it.diaryScore}, ${it.sodiumScore}," +
                        "${it.alcoholScore}, ${it.waterScore}, ${it.sugarScore}, ${it.saturatedFatScore}," +
                        "${it.unsaturatedFatScore}")
            }
        }
    }

    class ClinicianViewModelFactory(context: Context): ViewModelProvider.Factory{
        private val context = context.applicationContext
        override fun <T: ViewModel> create(modelClass: Class<T>): T =
            ClinicianViewModel(context) as T
    }
}