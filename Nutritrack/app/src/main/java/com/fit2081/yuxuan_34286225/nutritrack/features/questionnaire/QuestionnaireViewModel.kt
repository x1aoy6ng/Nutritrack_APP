package com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.yuxuan_34286225.nutritrack.data.foodintake.FoodIntake
import com.fit2081.yuxuan_34286225.nutritrack.data.foodintake.FoodIntakesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class QuestionnaireViewModel(context: Context) : ViewModel() {
    private val repository = FoodIntakesRepository(context = context)

    // UI States
    var selectedPersona by mutableStateOf("")
        private set

    var mealTime = mutableStateOf("00:00")
        private set

    var sleepTime = mutableStateOf("00:00")
        private set

    var wakeUpTime = mutableStateOf("00:00")
        private set

    var selectedFoods by mutableStateOf(mutableSetOf<String>())
        private set

    var fruitChecked by mutableStateOf(false)
        private set

    var vegetableChecked by mutableStateOf(false)
        private set

    var grainChecked by mutableStateOf(false)
        private set

    var redMeatChecked by mutableStateOf(false)
        private set

    var seafoodChecked by mutableStateOf(false)
        private set

    var poultryChecked by mutableStateOf(false)
        private set

    var fishChecked by mutableStateOf(false)
        private set

    var eggChecked by mutableStateOf(false)
        private set

    var nutSeedChecked by mutableStateOf(false)
        private set

    var saveSuccess by mutableStateOf(false)
        private set

    var isQuestionnaireValid by mutableStateOf(false)
        private set

    var timeValidationError by mutableStateOf<String?>(null)
        private set

    var isMealTimeSelected by mutableStateOf(false)
        private set

    var isSleepTimeSelected by mutableStateOf(false)
        private set

    var isWakeUpTimeSelected by mutableStateOf(false)
        private set

    /**
     * Load the saved food intake data for specific patient
     */
    fun loadFoodIntake(patientId: String) {
        Log.d("Questionnaire", "Loading form for user ID: $patientId")
        viewModelScope.launch {
            repository.getFoodIntakesByPatientId(patientId).collect { food ->
                food?.let {
                    selectedPersona = it.selectedPersona
                    mealTime.value = it.mealTime
                    sleepTime.value = it.sleepTime
                    wakeUpTime.value = it.wakeUpTime

                    isMealTimeSelected = true
                    isSleepTimeSelected = true
                    isWakeUpTimeSelected = true

                    selectedFoods = it.selectedFoods.split(",").map(String::trim).toMutableSet()

                    fruitChecked = it.fruit
                    vegetableChecked = it.vegetable
                    grainChecked = it.grain
                    redMeatChecked = it.redMeat
                    seafoodChecked = it.seafood
                    poultryChecked = it.poultry
                    fishChecked = it.fish
                    eggChecked = it.egg
                    nutSeedChecked = it.nutSeed

                    onFieldChanged()
                }
            }
        }
    }

    /**
     * Updates the selected persona for the questionnaire
     */
    fun updateSelectedPersona(persona: String){
        selectedPersona = persona
    }

    /**
     * Uodate the state of Food Intake checkbox
     */
    fun updateFruitChecked(fruit: Boolean){
        fruitChecked = fruit
    }

    fun updateRedMeatChecked(redMeat: Boolean){
        redMeatChecked = redMeat
    }

    fun updateFishChecked(fish: Boolean){
        fishChecked = fish
    }

    fun updateVegetableChecked(vegetable: Boolean){
        vegetableChecked = vegetable
    }

    fun updateSeafoodChecked(seafood: Boolean){
        seafoodChecked = seafood
    }

    fun updateEggChecked(egg: Boolean){
        eggChecked = egg
    }

    fun updateGrainChecked(grain: Boolean){
        grainChecked = grain
    }

    fun updatePoultryChecked(poultry: Boolean){
        poultryChecked = poultry
    }

    fun updateNutSeedChecked(nutSeed: Boolean){
        nutSeedChecked = nutSeed
    }

    /**
     * Saves/updates the patient's food intake questionnaire
     */
    fun saveQuestionnaireData(patientId: String) {
        val intake = FoodIntake(
            patientId = patientId,
            selectedFoods = selectedFoods.joinToString(),
            selectedPersona = selectedPersona,
            mealTime = mealTime.value,
            sleepTime = sleepTime.value,
            wakeUpTime = wakeUpTime.value,
            fruit = fruitChecked,
            vegetable = vegetableChecked,
            grain = grainChecked,
            redMeat = redMeatChecked,
            seafood = seafoodChecked,
            poultry = poultryChecked,
            fish = fishChecked,
            egg = eggChecked,
            nutSeed = nutSeedChecked
        )

        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getFoodIntakesByPatientId(patientId).firstOrNull()
            if (existing != null) {
                repository.insertFoodIntake(intake.copy(intakeId = existing.intakeId))
            } else {
                repository.insertFoodIntake(intake)
            }
            saveSuccess = true
        }
    }

    /**
     * Resets the flag that indicates whether saving was successful
     */
    fun clearSaveSuccess(){
        saveSuccess = false
    }

    /**
     * Called whenever a form field is changed to revalidate the questionnaire
     */
    fun onFieldChanged(){
        validateQuestionnaire()
    }

    /**
     * Clear any validation error related to time field
     */
    fun clearTimeValidationError(){
        timeValidationError = null
    }

    /**
     * Check whether food intake questionnaire already exists for specific user
     */
    suspend fun checkIfQuestionnaireExists(userId: String): FoodIntake? {
        return repository.getFoodIntakesByPatientId(userId).firstOrNull()
    }

    /**
     * Validate the meal time cannot same as sleep time or wakeup time
     */
    fun validateTime(mealTime: MutableState<String>, sleepTime: MutableState<String>, wakeUpTime: MutableState<String>, isMeal: Boolean, isSleep: Boolean): Boolean{
        val isValid = (mealTime.value != sleepTime.value ) && (mealTime.value != wakeUpTime.value) && (sleepTime != wakeUpTime)
        return if (!isValid){
            timeValidationError = "Meal, sleep, and wake-up times must be different"
            if (isMeal){
                mealTime.value = "00:00"
                isMealTimeSelected = false
            } else if (isSleep){
                sleepTime.value = "00:00"
                isSleepTimeSelected = false
            } else {
                wakeUpTime.value = "00:00"
                isWakeUpTimeSelected = false
            }

            false
        } else {
            timeValidationError = null
            true
        }
    }

    fun markMealAsSelected(){
        isMealTimeSelected = true
    }

    fun markSleepAsSelected(){
        isSleepTimeSelected = true
    }

    fun markWakeUpAsSelected(){
        isWakeUpTimeSelected = true
    }

    /**
     * Validate the entire questionnaire
     */
    private fun validateQuestionnaire() {
        // check at least one checkbox is selected
        val hasFoodSelection = listOf(
            fruitChecked,
            vegetableChecked,
            grainChecked,
            redMeatChecked,
            seafoodChecked,
            poultryChecked,
            fishChecked,
            eggChecked,
            nutSeedChecked
        ).any {it}

        // ensure all times are picked
        val allTimeSelected = isMealTimeSelected && isSleepTimeSelected && isWakeUpTimeSelected

        // ensures all times are different from each other
        val allUniqueTime = (mealTime.value != sleepTime.value) && (sleepTime != wakeUpTime) &&(mealTime != wakeUpTime)

        isQuestionnaireValid = hasFoodSelection &&
                selectedPersona.isNotBlank() &&
                allTimeSelected &&
                allUniqueTime
    }

    fun resetForm() {
        selectedPersona = ""
        mealTime.value = "00:00"
        sleepTime.value = "00:00"
        wakeUpTime.value = "00:00"

        selectedFoods.clear()

        fruitChecked = false
        vegetableChecked = false
        grainChecked = false
        redMeatChecked = false
        seafoodChecked = false
        poultryChecked = false
        fishChecked = false
        eggChecked = false
        nutSeedChecked = false

        isMealTimeSelected = false
        isSleepTimeSelected = false
        isWakeUpTimeSelected = false

        timeValidationError = null
        isQuestionnaireValid = false
        saveSuccess = false
    }



    class QuestionnaireViewModelFactory(context: Context): ViewModelProvider.Factory{
        private val context = context.applicationContext
        override fun <T: ViewModel> create(modelClass: Class<T>): T =
            QuestionnaireViewModel(context) as T
    }

}