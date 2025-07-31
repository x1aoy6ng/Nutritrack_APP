package com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.yuxuan_34286225.nutritrack.data.foodintake.FoodIntake
import com.fit2081.yuxuan_34286225.nutritrack.data.foodintake.FoodIntakesRepository
import com.fit2081.yuxuan_34286225.nutritrack.data.network.fruit.Fruit
import com.fit2081.yuxuan_34286225.nutritrack.data.network.fruit.FruitsRepository
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.Patient
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientsRepository
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class UserProfileViewModel(context: Context): ViewModel() {
    private val foodRepository = FoodIntakesRepository(context = context)
    private val patientsRepository = PatientsRepository(context = context)

    private val userId: String
        get() = AuthManager.getPatientId() ?: ""

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _totalScore = MutableStateFlow(0f)
    val totalScore: StateFlow<Float> = _totalScore.asStateFlow()

    private val _fruitServeSize = MutableStateFlow(0f)
    val fruitServeSize: StateFlow<Float> = _fruitServeSize.asStateFlow()

    private val _fruitVariation = MutableStateFlow(0f)
    val fruitVariation: StateFlow<Float> = _fruitVariation.asStateFlow()

    private val _foodIntakeSummary = MutableStateFlow("")
    val foodIntakeSummary: StateFlow<String> = _foodIntakeSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var foodScore = MutableStateFlow<List<Pair<String, Pair<Float, Float>>>>(emptyList())
        private set

    var foodIntake by mutableStateOf<FoodIntake?>(null)
        private set

    var isFruitLoading by mutableStateOf(false)
        private set

    var fruitNotFound by mutableStateOf(false)
        private set

    var isImageLoading by mutableStateOf(false)
        private set

    var fruitName by mutableStateOf("")
        private set

    var fruitDetails by mutableStateOf<Fruit?>(null)
        private set

    private val fruitsRepository = FruitsRepository()

    init {
        loadUserProfile(userId)
    }

    /**
     * Load user profile and food intake data based on given user id
     */
    fun loadUserProfile(userId: String){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                patientsRepository.getAllPatients().collectLatest { list ->
                    val currentPatient = list.find { it.userId == userId }
                    currentPatient?.let {
                        _userName.value = it.userName
                        _phoneNumber.value = it.phoneNum
                        _totalScore.value = it.totalScore
                        _fruitServeSize.value = it.fruitServeSize
                        _fruitVariation.value = it.fruitVariation
                        foodScore.value = loadFoodScores(it)
                        _foodIntakeSummary.value = generateFoodIntakeSummary(it)
                    }
                }
            } catch (e: Exception){
                _errorMessage.value = "Failed to load the profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            try {
                foodRepository.getFoodIntakesByPatientId(userId).collectLatest {
                    foodIntake = it
                }
            } catch (e: Exception){
                _errorMessage.value = "Failed to load food intake: ${e.message}"
            }
        }
    }

    /**
     * Update current user's username in database
     */
    fun updateUsername(newName: String) {
        viewModelScope.launch {
            try {
                patientsRepository.updateUsername(userId, newName)
            } catch (e: Exception) {
                _errorMessage.value = "Username update failed : ${e.message}"
            }
        }
    }

    /**
     * Log out the current user by clearing authentication data through AuthManager
     */
    fun logout(context: Context){
        AuthManager.logout(context)
    }

    /**
     * Updates the loading state of an image fetch operation
     */
    fun updateImageLoading(value: Boolean){
        isImageLoading = value
    }

    /**
     * Fetches nutritional information for specified fruit by name
     * Updates the fruitstate if found, or sets error flags if not
     */
    fun fetchFruitData(fruit: String){
        viewModelScope.launch {
            isFruitLoading = true
            fruitNotFound = false
            try {
                fruitName = fruit
                val result = fruitsRepository.getFruitsByName(fruit)
                if (result != null){
                    fruitDetails = result
                } else {
                    fruitDetails = null
                    fruitNotFound = true
                }
            } catch (e: Exception){
                _errorMessage.value = "Failed to fetch the fruit data"
                fruitDetails = null
                fruitNotFound = true
            } finally {
                isFruitLoading = false
            }
        }
    }

    /**
     * Clear current user's profile info and reset related data
     */
    fun clearUserProfile(){
        _userName.value = "User"
        _phoneNumber.value = ""
        _totalScore.value = 0f
        _fruitServeSize.value = 0f
        _fruitVariation.value = 0f
        _foodIntakeSummary.value = ""
        foodIntake = null
        foodScore.value = emptyList()

        // reset fruit related states
        fruitName = ""
        fruitDetails = null
        isFruitLoading = false
        fruitNotFound = false
        isImageLoading = false

        // reset the ui state indicators
        _isLoading.value = false
        _errorMessage.value = null
    }


    /**
     * Load a list of food categories with their current and maximum scores
     */
    private fun loadFoodScores(patient: Patient): List<Pair<String, Pair<Float, Float>>>{
        val foodItems = listOf(
            "Vegetables" to patient.vegetableScore,
            "Fruits" to patient.fruitScore,
            "Grains & Cereals" to patient.grainsAndCerealScore,
            "Whole Grains" to patient.wholeGrainScore,
            "Meat & Alternatives" to patient.meatAndAlternativesScore,
            "Dairy" to patient.diaryScore,
            "Water" to patient.waterScore,
            "Saturated Fats" to patient.saturatedFatScore,
            "Unsaturated Fats" to patient.unsaturatedFatScore,
            "Sodium" to patient.sodiumScore,
            "Sugar" to patient.sugarScore,
            "Alcohol" to patient.alcoholScore,
            "Discretionary Foods" to patient.discretionaryScore,
        )

        // map each food category to its corresponding score and max score
        return foodItems.map { (label, score) ->
            val maxScore = when (label){
                "Whole Grains", "Grains & Cereals", "Water",
                "Alcohol", "Saturated Fats", "Unsaturated Fats" -> 5f
                else -> 10f
            }
            label to (score to maxScore)
        }
    }

    /**
     * Generate a summary string of user's food intake scores
     */
    private fun generateFoodIntakeSummary(patient: Patient): String{
        return buildString {
            append("Vegetables: ${patient.vegetableScore}, ")
            append("Fruits: ${patient.fruitScore}, ")
            append("Grains & Cereals: ${patient.grainsAndCerealScore}, ")
            append("Whole Grains: ${patient.wholeGrainScore}, ")
            append("Meat & Alternatives: ${patient.meatAndAlternativesScore}, ")
            append("Dairy: ${patient.diaryScore}, ")
            append("Water: ${patient.waterScore}, ")
            append("Saturated Fat: ${patient.saturatedFatScore}, ")
            append("Unsaturated Fat: ${patient.unsaturatedFatScore}, ")
            append("Sodium: ${patient.sodiumScore}, ")
            append("Sugar: ${patient.sugarScore}, ")
            append("Alcohol: ${patient.alcoholScore}, ")
            append("Discretionary Foods: ${patient.discretionaryScore}")
        }
    }

    class UserProfileViewModelFactory(context: Context): ViewModelProvider.Factory{
        private val context = context.applicationContext
        override fun <T: ViewModel> create(modelClass: Class<T>): T =
            UserProfileViewModel(context) as T
    }
}