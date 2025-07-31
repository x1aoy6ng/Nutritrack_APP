package com.fit2081.yuxuan_34286225.nutritrack.features.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.Patient
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientsRepository
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel


class AuthViewModel(context: Context): ViewModel() {
    private val repository = PatientsRepository(context = context)

    // shared login/register fields
    var selectedId by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    // register-only fields
    var phoneNumber by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    // ui state flags
    var loginSuccess by mutableStateOf(false)
        private set

    var registrationSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var showValidationErrors by mutableStateOf(false)
        private set

    var passwordMatchOld by mutableStateOf(false)
        private set

    var oldPassword by mutableStateOf("")

    // user ID lists from database
    var userIds by mutableStateOf<List<String>>(emptyList())
        private set

    var registeredIds by mutableStateOf<List<String>>(emptyList())
        private set

    var unregisteredIds by mutableStateOf<List<String>>(emptyList())
        private set


    init {
        loadPatients()
    }

    /**
     * Load all patients from the repository
     * Separates them into registered and unregistered lists
     */
    private fun loadPatients(){
        viewModelScope.launch {
            repository.getAllPatients().collectLatest { list ->
                userIds = list.map { it.userId }
                registeredIds = list.filter { it.userPassword.isNotBlank() }.map { it.userId }
                unregisteredIds = list.filter { it.userPassword.isBlank() }.map { it.userId }
            }
        }
    }

    /**
     * Handles the login logic: validates user ID and password
     */
    fun onLoginClick(context: Context){
        enableValidationErrors()

        if (selectedId.isBlank() || password.isBlank()){
            return
        }
        viewModelScope.launch {
            try{
                val patient: Patient? = repository.getPatientById(selectedId)
                if (patient != null && patient.userPassword == password){
                    oldPassword = password
                    AuthManager.login(context, selectedId)
                    oldPassword = password
                    loginSuccess = true
                } else {
                    errorMessage = "Invalid ID or Password"
                }
            } catch (e: Exception){
                errorMessage = "Login failed: ${e.message}"
            }
        }
    }

    /**
     * Handles user registration logic
     */
    fun onRegisterClick(){
        enableValidationErrors()

        viewModelScope.launch {
            try{
                val matchedPatient = repository.getPatientById(selectedId)
                if (matchedPatient != null && matchedPatient.phoneNum == phoneNumber){
                    // patient id and phone number match an existing user
                    if (matchedPatient.userPassword.isNotBlank()){
                        errorMessage = "This user has already registered"
                        return@launch
                    }
                    // username is valid
                    if (!isValidUsername(username)){
                        errorMessage = "Username must be 3-15 characters"
                        return@launch
                    }
                    // password meet with strength requirements
                    if (password != confirmPassword || !isPasswordStrong(password)){
                        errorMessage = "Password is invalid or does not match"
                        return@launch
                    }
                    // update username and password in repository
                    repository.updateUsername(userId = selectedId, newUsername = username)
                    repository.updatePassword(userId = selectedId, newPassword = password)
                    registrationSuccess = true
                } else {
                    errorMessage = "ID and phone number do not match"
                }
            } catch (e: Exception){
                errorMessage = "Registration failed: ${e.message}"
            }
        }
    }

    /**
     * Log out the current user
     */
    fun onLogoutClick(
        onNavigateToLogin: () -> Unit,
        userProfileViewModel: UserProfileViewModel,
        genAIViewModel: GenAIViewModel,
        context: Context
    ){
        viewModelScope.launch {
            // clear user profile, reset auth manager login state, navigates to login screen
            updateLoginSuccess(false)
            clearLogin()
            userProfileViewModel.clearUserProfile()
            userProfileViewModel.logout(context)
            genAIViewModel.resetState()
            onNavigateToLogin()
        }
    }

    /**
     * Compares new password against the current one from database
     */
    fun validateNewPassword(onResult: () -> Unit){
        viewModelScope.launch {
            val userId = AuthManager.getPatientId() ?: return@launch
            val currentPassword = getCurrentPassword(userId)
            passwordMatchOld = currentPassword == password
            onResult()
        }
    }

    /**
     * Updates password if
     * - not same as old
     * - strong and valid
     * - matches the confirm password
     */
    fun updatePasswordIfValid(onSuccess: () -> Unit, onFailure: (String) -> Unit){
        enableValidationErrors()
        validateNewPassword{
            when {
                passwordMatchOld -> {
                    onFailure("New password must not be the same as old password")
                }
                !isPasswordStrong(password) -> {
                    onFailure("Invalid password format")
                }
                password != confirmPassword -> {
                    onFailure("Password does not match with confirm password")
                }
                password.isBlank() -> {
                    onFailure("Password cannot be blank")
                }
                else -> {
                    viewModelScope.launch {
                        try {
                            val userId = AuthManager.getPatientId() ?: return@launch
                            repository.updatePassword(userId, password)
                            oldPassword = password
                            onSuccess()
                        } catch (e: Exception){
                            errorMessage = "Failed to update the password"
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the current password from database
     */
    suspend fun getCurrentPassword(userId: String): String? {
        return repository.getPasswordById(userId)
    }

    /**
     * Enables showing validation error messages in ui
     */
    fun enableValidationErrors(){
        showValidationErrors = true
    }

    /**
     * Clear any existing error messages
     */
    fun clearError(){
        errorMessage = null
    }

    /**
     * updates state
     */
    fun updateSelectedId(id: String){
        selectedId = id
    }

    fun updatePhoneNumber(phoneNum: String){
        phoneNumber = phoneNum
    }

    fun updateUsername(name: String){
        username = name
    }

    fun updatePassword(pwd: String){
        password = pwd
    }

    fun updateConfirmPassword(confirmPwd: String){
        confirmPassword = confirmPwd
    }

    fun updateLoginSuccess(value: Boolean){
        loginSuccess = value
    }

    /**
     * Check whether username follows valid format
     */
    fun isValidUsername(username: String): Boolean{
        val regex = "^[a-zA-Z][a-zA-Z0-9_]{2,15}$".toRegex()
        return regex.matches(username)
    }

    /**
     * Check whether password passes all strength rules
     */
    fun isPasswordStrong(pwd: String): Boolean {
        return getPasswordSuggestion(pwd).all { it.passed }
    }

    // validation checks
    val phoneNumberError: Boolean
        get() = phoneNumber.length < 11

    val passwordsMatch: Boolean
        get() = password == confirmPassword

    val isUsernameValid: Boolean
        get() = isValidUsername(username)

    /**
     * Check whether all required fields are valid for registration
     */
    fun validateAllFields(): Boolean{
        return selectedId.isNotBlank() &&
                !phoneNumberError &&
                isUsernameValid &&
                password.isNotBlank() &&
                passwordsMatch &&
                isPasswordStrong(password)
    }

    /**
     * Represents an individual password rule
     */
    data class PasswordRule(val passed: Boolean, val message: String)

    /**
     * Return list of password rules and whether each rule is satisfied
     */
    fun getPasswordSuggestion(password: String, isChangePassword: Boolean = false): List<PasswordRule>{
        val suggestions = mutableListOf<PasswordRule>()

        val minLength = password.length >= 8
        val hasLower = password.any {it.isLowerCase()}
        val hasUpper = password.any {it.isUpperCase()}
        val hasDigit = password.any {it.isDigit()}
        val hasSpecial = password.contains(Regex("(?=.*[@\$!%*?&_])"))
        val noWhiteSpace = !password.contains(Regex("\\s"))

        if (isChangePassword){
            val notSameAsOld = password != oldPassword
            suggestions.add(PasswordRule(notSameAsOld, "Must not be the same as your old password"))
        }

        suggestions.add(PasswordRule(minLength, "At least 8 characters"))
        suggestions.add(PasswordRule(hasLower, "Contains lowercase letter"))
        suggestions.add(PasswordRule(hasUpper, "Contains uppercase letter"))
        suggestions.add(PasswordRule(hasDigit, "Contains digit"))
        suggestions.add(PasswordRule(hasSpecial, "Contains special character"))
        suggestions.add(PasswordRule(noWhiteSpace, "Must not contain spaces"))

        return suggestions
    }

    /**
     * Clears the login state and validation errors
     */
    fun clearLogin(){
        selectedId = ""
        password = ""
        confirmPassword = ""
        loginSuccess = false
        showValidationErrors = false
        clearError()
    }

    /**
     * Clear registration state and validation errors
     */
    fun clearRegister(){
        selectedId = ""
        password = ""
        confirmPassword = ""
        phoneNumber = ""
        username = ""
        showValidationErrors = false
        registrationSuccess = false
        clearError()
    }

    class AuthViewModelFactory(context: Context): ViewModelProvider.Factory{
        private val context = context.applicationContext
        override fun <T: ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(context) as T
    }

}