package com.fit2081.yuxuan_34286225.nutritrack.features.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.NutriNavHost
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.TopAppBar
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.ClinicianViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire.QuestionnaireViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel

sealed class AuthScreen(val route: String){
    object Login: AuthScreen("Login")
    object Register: AuthScreen("Register")
    object ChangePassword: AuthScreen("Change Password")
}

class LoginPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
            this, UserProfileViewModel.UserProfileViewModelFactory(this@LoginPage)
        )[UserProfileViewModel::class.java]

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val clinicianViewModel: ClinicianViewModel = ViewModelProvider(
            this, ClinicianViewModel.ClinicianViewModelFactory(this@LoginPage)
        )[ClinicianViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@LoginPage)
        )[GenAIViewModel::class.java]

        // initialise the authViewModel using ViewModelProvider with a factory pattern
        val authViewModel: AuthViewModel = ViewModelProvider(
            this, AuthViewModel.AuthViewModelFactory(this@LoginPage)
        )[AuthViewModel::class.java]

        // initialise the QuestionnaireViewModel using ViewModelProvider with a factory pattern
        val questionnaireViewModel: QuestionnaireViewModel = ViewModelProvider(
            this, QuestionnaireViewModel.QuestionnaireViewModelFactory(this@LoginPage)
        )[QuestionnaireViewModel::class.java]

        setContent {
            NutritrackTheme {
                val navController = rememberNavController()
                Scaffold (modifier = Modifier.fillMaxSize(), containerColor = Color.White){ innerPadding ->
                    // calls the NavHost composable to define the navigation graph
                    NutriNavHost(
                        innerPadding,
                        navController,
                        userProfileViewModel,
                        clinicianViewModel,
                        genAIViewModel,
                        authViewModel,
                        questionnaireViewModel
                    )
                }
            }
        }
    }
}

/**
 * Login screen for pre-registered users
 */
@Composable
fun LoginScreen(authViewModel: AuthViewModel, questionnaireViewModel: QuestionnaireViewModel ,navController: NavHostController){
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val isExpanded = remember {mutableStateOf(false)}
    val loginSuccess = authViewModel.loginSuccess
    val isPasswordFocused = remember { mutableStateOf(false) }
    val showErrors = authViewModel.showValidationErrors
    val loginError = authViewModel.errorMessage
    val registeredIds = authViewModel.registeredIds

    LaunchedEffect(loginSuccess) {
        if (loginSuccess){
            val userId = authViewModel.selectedId
            val alreadyFilled = questionnaireViewModel.checkIfQuestionnaireExists(userId)
            if (alreadyFilled != null){
                navController.navigate("Home")
            } else {
                navController.navigate("Questionnaire")
            }
            // clear the text field in login/register screen
            authViewModel.clearLogin()
            authViewModel.clearRegister()
        }
    }

    loginError?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        authViewModel.clearError()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ){
            Spacer(modifier = Modifier.height(60.dp))
            // login title
            Text(text = "Log in",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            // text field for username and password
            UserPasswordSection(authViewModel, isExpanded, registeredIds, isPasswordFocused, showErrors)

            Spacer(modifier = Modifier.height(24.dp))
            // disclaimer text
            Text(
                text = "This app is only for pre-registered users. Please have your ID and phone number handy before continuing.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.fillMaxWidth(0.95f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // continue/register button
            AuthButton("Continue"){
                authViewModel.onLoginClick(context)
            }
            Spacer(modifier = Modifier.height(16.dp))
            AuthButton("Register") {
                authViewModel.clearLogin()
                // navigate to register screen
                navController.navigate(AuthScreen.Register.route)
            }
        }
    }
}

/**
 * Screen to register new user by claiming their pre-registered ID
 */
@Composable
fun RegisterScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isExpanded = remember {mutableStateOf(false)}
    val isPasswordFocused = remember { mutableStateOf(false) }
    val showErrors = authViewModel.showValidationErrors
    val errorMessage = authViewModel.errorMessage

    val registrationSuccess = authViewModel.registrationSuccess
    val unregisteredIds = authViewModel.unregisteredIds

    // get the password suggestions list
    val passwordSuggestions = authViewModel.getPasswordSuggestion(authViewModel.password)

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
            // register success - direct navigate to login screen
            navController.navigate(AuthScreen.Login.route)
            // clear the text field in login/register screen
            authViewModel.clearLogin()
            authViewModel.clearRegister()
        }
    }

    LaunchedEffect(errorMessage) {
        // display toast error message
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Register",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            // drop down menu for User ID
            UserIDDropdown(
                selectedID = authViewModel.selectedId,
                isExpanded = isExpanded.value,
                userIDs = unregisteredIds,
                onExpandedChange = { isExpanded.value = !isExpanded.value },
                isError = showErrors && authViewModel.selectedId.isBlank(),
                errorMessage = "Please select a User ID"
            ) {
                authViewModel.updateSelectedId(it)  // update the selected ID
            }

            Spacer(modifier = Modifier.height(14.dp))

            // phone number outlined text field
            AuthScreenTextField(
                label = "Phone Number",
                value = authViewModel.phoneNumber,
                placeholder = "Enter your number",
                onValueChange = {authViewModel.updatePhoneNumber(it)},
                keyboardType = KeyboardType.Phone,
                isError = (authViewModel.phoneNumber.isNotBlank() && authViewModel.phoneNumberError) || (showErrors && authViewModel.phoneNumber.isBlank()),
                errorMessage = when {
                    showErrors && authViewModel.phoneNumber.isBlank() -> "Phone number is required"
                    authViewModel.phoneNumber.isNotBlank() && authViewModel.phoneNumberError -> "Phone number must be at least 11 characters"
                    else -> ""
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // username outlined text field
            AuthScreenTextField(
                label = "Username",
                value = authViewModel.username,
                placeholder = "Enter your username",
                onValueChange = {authViewModel.updateUsername(it)},
                keyboardType = KeyboardType.Text,
                isError = (authViewModel.username.isNotBlank() && !authViewModel.isUsernameValid ) || (showErrors && authViewModel.username.isBlank()),
                errorMessage = when {
                    showErrors && authViewModel.username.isBlank() -> "Username is required"
                    authViewModel.username.isNotBlank() && !authViewModel.isUsernameValid -> "Username must be between 3 and 15 characters"
                    else -> ""
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            PasswordSection(authViewModel, showErrors, isPasswordFocused, passwordSuggestions)

            Spacer(modifier = Modifier.height(16.dp))
            // disclaimer text
            Text(
                text = "This app is only for pre-registered users. Please enter your ID, phone number and password to claim your account.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.fillMaxWidth(0.95f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            AuthButton(AuthScreen.Register.route) {
                if (authViewModel.validateAllFields()){
                    authViewModel.onRegisterClick()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AuthButton(AuthScreen.Login.route) {
                navController.navigate(AuthScreen.Login.route)
                authViewModel.clearRegister()
            }
        }
    }
}

/**
 * Screen to update an existing user's password
 */
@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    genAIViewModel: GenAIViewModel,
    navController: NavHostController
){
    val scrollState = rememberScrollState()
    val isPasswordFocused = remember { mutableStateOf(false) }
    val showErrors = authViewModel.showValidationErrors

    LaunchedEffect(Unit) {
        // clear the text field
        authViewModel.clearRegister()
        authViewModel.clearLogin()
    }

    Scaffold (modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(
            text = "",
            navController = navController
        )
    },
        containerColor = Color.White
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(18.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ChangePasswordSection(authViewModel, userProfileViewModel, genAIViewModel, navController ,showErrors, isPasswordFocused)
        }
    }
}

/**
 * Section of change password screen, shows validation criteria and password fields
 */
@Composable
fun ChangePasswordSection(
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    genAIViewModel: GenAIViewModel,
    navController: NavHostController,
    showErrors: Boolean,
    isPasswordFocused: MutableState<Boolean>
){
    val context = LocalContext.current
    val passwordSuggestions = remember(authViewModel.password, authViewModel.oldPassword) {
        authViewModel.getPasswordSuggestion(authViewModel.password, isChangePassword = true)
    }

    Text(
        text = "Create new password",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))

    Column(modifier = Modifier.fillMaxWidth(0.95f)){
        Text(
            text = "Your new password must be different from previous used passwords",
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    // password and confirm password
    // to-do: make sure new password not similar as old password
    PasswordSection(authViewModel, showErrors, isPasswordFocused, passwordSuggestions)
    Spacer(modifier = Modifier.height(32.dp))
    // reset password button
    AuthButton("Update password") {
        // to-do: validate and update the user password
        authViewModel.updatePasswordIfValid(
            onSuccess = {
                Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                // navigate back to login screen
                authViewModel.onLogoutClick(onNavigateToLogin = { navController.navigate(AuthScreen.Login.route) },
                    userProfileViewModel, genAIViewModel, context)
            },
            onFailure = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Reusable section for password and confirm password fields
 */
@Composable
fun PasswordSection(
    authViewModel: AuthViewModel,
    showErrors: Boolean,
    isPasswordFocused: MutableState<Boolean>,
    passwordSuggestions: List<AuthViewModel.PasswordRule>,
    passwordMatchOld: Boolean = false
){
    // password outlined text field
    AuthScreenTextField(
        label = "Password",
        value = authViewModel.password,
        placeholder = "Enter your password",
        onValueChange = {authViewModel.updatePassword(it)},
        keyboardType = KeyboardType.Password,
        isPassword = true,
        // only show red border (error state) when the password is not blank
        // and not all rules being satisfied
        isError = (authViewModel.password.isNotBlank() && !authViewModel.isPasswordStrong(authViewModel.password)) || (showErrors && authViewModel.password.isBlank()) || passwordMatchOld,
        errorMessage = when {
            showErrors && authViewModel.password.isBlank() -> "Password is required"
            !isPasswordFocused.value && authViewModel.password.isNotBlank() && !authViewModel.isPasswordStrong(authViewModel.password) -> "Password does not meet required criteria"
            passwordMatchOld -> "New password must be different from the old one"
            else -> ""
        },
        onFocusChanged = {isPasswordFocused.value = it}
    )

    if (authViewModel.password.isNotBlank() && isPasswordFocused.value){
        PasswordChecklist(passwordSuggestions)
    }

    Spacer(modifier = Modifier.height(14.dp))

    // confirm password outlined text field
    AuthScreenTextField(
        label = "Confirm Password",
        value = authViewModel.confirmPassword,
        placeholder = "Enter your password again",
        onValueChange = {authViewModel.updateConfirmPassword(it) },
        keyboardType = KeyboardType.Password,
        isPassword = true,
        isError = (authViewModel.confirmPassword.isNotBlank() && !authViewModel.passwordsMatch) || (showErrors && authViewModel.confirmPassword.isBlank()),
        errorMessage = when {
            showErrors && authViewModel.confirmPassword.isBlank() -> "Please confirm your password"
            authViewModel.confirmPassword.isNotBlank() && !authViewModel.passwordsMatch -> "Password does not match"
            else -> ""
        }
    )
}

/**
 * Section in login screen that includes user ID dropdown and password field
 */
@Composable
fun UserPasswordSection(
    authViewModel: AuthViewModel,
    isExpanded: MutableState<Boolean>,
    userIds: List<String>,
    isPasswordFocused: MutableState<Boolean>,
    showErrors: Boolean
){
    val triedToOpenEmpty = remember { mutableStateOf(false) }
    val hasNoUsers = userIds.isEmpty()

    UserIDDropdown(
        selectedID = authViewModel.selectedId,
        isExpanded = isExpanded.value,
        userIDs = userIds,
        onExpandedChange = {
            if (hasNoUsers){
            triedToOpenEmpty.value = true
        } else {
            isExpanded.value = !isExpanded.value }
        },
        isError = (triedToOpenEmpty.value && hasNoUsers) || (showErrors && authViewModel.selectedId.isBlank()),
        errorMessage = when {
            triedToOpenEmpty.value && hasNoUsers -> "No registered users available"
            showErrors && authViewModel.selectedId.isBlank() -> "Please select a User ID"
            else -> ""
        },
    ) {
        authViewModel.updateSelectedId(it)  // update the selected ID
    }

    Spacer(modifier = Modifier.height(16.dp))

    // password outlined text field
    AuthScreenTextField(
        label = "Password",
        value = authViewModel.password,
        placeholder = "Enter your password",
        onValueChange = {authViewModel.updatePassword(it)},
        keyboardType = KeyboardType.Password,
        isPassword = true,
        // only show red border (error state) when the password is blank
        isError = (showErrors && authViewModel.password.isBlank()),
        errorMessage = if (showErrors && authViewModel.password.isBlank()) "Password is required" else "",
        onFocusChanged = {isPasswordFocused.value = it}
    )
}


/**
 * Default Button Function
 * */
@Composable
fun AuthButton(text: String, onClick:() -> Unit){
    Button(onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(55.dp),
        // rounded the button corner
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )) {
        Text(text = text, fontSize = 18.sp)
    }
}

/**
 * Reusable outlined text field
 */
@Composable
fun AuthScreenTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            },
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        placeholder = { Text(text = placeholder, fontSize = 14.sp, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        // only available for password & confirm password
        trailingIcon = {
            if (isPassword && value.isNotEmpty()) {
                val icon =
                    if (isPasswordVisible) painterResource(R.drawable.hide_password) else painterResource(
                        R.drawable.view_password
                    )

                Icon(
                    painter = icon,
                    contentDescription = "Password Visibility",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { isPasswordVisible = !isPasswordVisible }
                        .padding(end = 8.dp)
                )
            }
        },
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Gray,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.Gray
        )
    )

    if (isError && errorMessage.isNotBlank()) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(start = 8.dp, top = 4.dp)
        )
    }
}

/**
 * Function for displaying a drop down menu of user ID
 *
 * @param selectedID: currently selected user ID to display in text field
 * @param isExpanded: flag that indicates whether the dropdown menu is currently expanded
 * @param userIDs: list of user ID string to be displayed
 * @param onExpandedChange: callback to toggle dropdown menu's expanded state
 * @param onSelect: callback invoked when a user selects an ID from the dropdown list.
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserIDDropdown(
    selectedID: String,
    isExpanded: Boolean,
    userIDs: List<String>,
    isError: Boolean = false,
    errorMessage: String = "",
    onExpandedChange: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.95f)) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange() }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selectedID,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        text = "My ID (Provided by your Clinician)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Dropdown Arrow"
                    )
                },
                shape = RoundedCornerShape(18.dp),
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    disabledBorderColor = Color.Gray
                )
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { onExpandedChange() }) {
                userIDs.forEach { id ->
                    DropdownMenuItem(
                        text = { Text(id) },
                        onClick = {
                            onSelect(id)
                            onExpandedChange()
                        }
                    )
                }
            }
        }

        if (isError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Function to display the password checklist display
 */
@Composable
fun PasswordChecklist(rules: List<AuthViewModel.PasswordRule>){
    Column(modifier = Modifier.fillMaxWidth(0.90f))
    {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Password must include:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        rules.forEach { rule ->
            val icon = if (rule.passed) {
                painterResource(R.drawable.tick)
            } else {
                painterResource(R.drawable.cross)
            }
            val color = if (rule.passed) Color.Green else Color.Red

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = icon,
                    contentDescription = "Password Check",
                    tint = color,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = rule.message,
                    modifier = Modifier.weight(1f),
                    color = color,
                    fontSize = 12.sp
                )
            }
        }
    }
}
