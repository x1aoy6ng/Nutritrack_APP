package com.fit2081.yuxuan_34286225.nutritrack.features.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.BottomBar
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.features.login.AuthScreen
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.features.login.AuthViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.ClinicianViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.login.AuthScreenTextField
import com.fit2081.yuxuan_34286225.nutritrack.features.login.LoginPage
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel

sealed class SettingClinician(val route: String){
    object Settings: SettingClinician("settings")
    object Clinician: SettingClinician("clinician_login")
    object ClinicianDashboard: SettingClinician("clinician_dashboard")
}

class SettingsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
            this, UserProfileViewModel.UserProfileViewModelFactory(this@SettingsPage)
        )[UserProfileViewModel::class.java]

        // initialise the authViewModel using ViewModelProvider with a factory pattern
        val authViewModel: AuthViewModel = ViewModelProvider(
            this, AuthViewModel.AuthViewModelFactory(this@SettingsPage)
        )[AuthViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@SettingsPage)
        )[GenAIViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                SettingsScreen(userProfileViewModel, authViewModel, genAIViewModel, navController)
            }
        }
    }
}

/**
 * Main settings screen for patients
 * Displays user account info and other settings option
 */
@Composable
fun SettingsScreen(
    userProfileViewModel: UserProfileViewModel,
    authViewModel: AuthViewModel,
    genAIViewModel: GenAIViewModel,
    navController: NavHostController
){
    val userID = AuthManager.getPatientId().toString()
    val userName by userProfileViewModel.userName.collectAsState()
    val phoneNumber by userProfileViewModel.phoneNumber.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold (modifier = Modifier.fillMaxSize(), bottomBar = { BottomBar(navController) }, containerColor = Color.White)
    { innerPadding ->
        Column (modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(innerPadding)
            .padding(18.dp)
            .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ){
            Spacer(modifier = Modifier.height(10.dp))
            AccountSection(userName, phoneNumber, userID, authViewModel ,userProfileViewModel)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))
            OtherSection(navController, userProfileViewModel, authViewModel, genAIViewModel)
        }
    }
}

/**
 * Display user's account information
 * Opens a dialog for user to edit their username
 */
@Composable
fun AccountSection(userName: String, phoneNumber: String, userID: String, authViewModel: AuthViewModel, userProfileViewModel: UserProfileViewModel){
    var showDialog by remember { mutableStateOf(false)}

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
        Text(
            text = "Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Text(text = "ACCOUNT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            AccountRow(
                painter = painterResource(R.drawable.person),
                contentDescription = "Username",
                label = userName,
                trailingIcon = painterResource(R.drawable.edit),
                onClick = {showDialog = true}
            )

            // display the dialog
            if (showDialog) {
                var validationError by remember { mutableStateOf<String?>(null) }
                var updatedUsername by remember { mutableStateOf(userName) }

                // clear the error automatically when valid
                LaunchedEffect(updatedUsername) {
                    if (authViewModel.isValidUsername(updatedUsername)){
                        validationError = null
                    }
                }

                EditUsernameDialog(
                    currentUsername = updatedUsername,
                    onDismissRequest = {
                        showDialog = false
                        validationError = null
                    },
                    onConfirm = {
                        if (authViewModel.isValidUsername(updatedUsername)) {
                            userProfileViewModel.updateUsername(updatedUsername)
                            showDialog = false
                        } else {
                            validationError = "Username must be 3–15 characters and only include letters, digits, or underscores"
                        }
                    },
                    onValueChange = { updatedUsername = it },
                    errorMessage = validationError
                )
            }


            AccountRow(painter = painterResource(R.drawable.phone), contentDescription = "Phone Number", label = phoneNumber)
            AccountRow(painter = painterResource(R.drawable.badge), contentDescription = "Badge", label = userID)
        }
    }
}

/**
 * Reusable row displaying the the label with optional icon
 */
@Composable
fun AccountRow(
    painter: Painter,
    contentDescription: String,
    label: String,
    onClick: (() -> Unit)? = null,
    trailingIcon: Painter? = null
){
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ){
        Icon(painter, contentDescription = contentDescription, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        if (trailingIcon != null && onClick != null){
            Icon(
                painter = trailingIcon,
                contentDescription = "Edit",
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onClick() },
                tint = Color.Gray
            )
        }
    }
}

/**
 * Row in other settings section for navigation-based actions
 */
@Composable
fun OtherSettingsRow(painter: Painter, contentDescription: String, label: String, onClick: () -> Unit){
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ){
        Icon(painter, contentDescription=contentDescription, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Arrow Right",
            tint = Color.Gray,
            modifier =  Modifier.size(25.dp))
    }
}

/**
 * Displays a list of additional setting options for user
 * - change password
 * - access nutri ai assistant
 * - login as clinician
 * - log out the account
 */
@Composable
fun OtherSection(navController: NavHostController, userProfileViewModel: UserProfileViewModel, authViewModel: AuthViewModel, genAIViewModel: GenAIViewModel){
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = "OTHER SETTINGS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        // change password
        OtherSettingsRow(painter = painterResource(R.drawable.password), contentDescription = "Change password", label = "Change password") {
            // to-do: navigate to change password screen
            navController.navigate(AuthScreen.ChangePassword.route)
        }

        // nutritrack ai chatbox
        OtherSettingsRow(painter = painterResource(R.drawable.chat), contentDescription = "AI chatbox", label = "Nutri AI Assistant") {
            // to-do: navigate to reminders section
            navController.navigate("ChatBox")
        }

        // clinician login function
        OtherSettingsRow(painter = painterResource(R.drawable.clinician), contentDescription = "Clinician", label = "Clinician Login") {
            // to-do: navigate user to the Clinician Login Screen
            navController.navigate(SettingClinician.Clinician.route)
        }

        // log out function
        OtherSettingsRow(painter = painterResource(R.drawable.log_out), contentDescription = "LogOut", label = "Logout") {
            authViewModel.onLogoutClick(onNavigateToLogin = { navController.navigate(AuthScreen.Login.route) },
                userProfileViewModel, genAIViewModel, context)
        }
    }
}

/**
 * Clinician Login Screen where clinician access the clinician dashboard using their special login key
 */
@Composable
fun ClinicianLoginScreen(
    clinicianViewModel: ClinicianViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val clinicianKey by clinicianViewModel.clinicianKey.collectAsState()

    var isLoggedIn = clinicianViewModel.isLoggedIn
    val loginError = clinicianViewModel.errorMessage
    val showErrors = clinicianViewModel.showValidationErrors
    val isPasswordFocused = remember { mutableStateOf(false) }

    // navigate to clinician dashboard if successfully login
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn){
            Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
            navController.navigate(SettingClinician.ClinicianDashboard.route)
        }
    }

    loginError?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }

    Scaffold (modifier = Modifier.fillMaxSize(), bottomBar = { BottomBar(navController) }, containerColor = Color.White)
    { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(innerPadding)
            .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ){
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text="Clinician Login",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            AuthScreenTextField(
                label = "Clinician Key",
                value = clinicianKey,
                placeholder = "Enter your clinician key",
                onValueChange = {clinicianViewModel.onKeyChange(it)},
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = showErrors && clinicianKey.isBlank(),
                errorMessage = if(showErrors && clinicianKey.isBlank()) "Clinician key is required" else "",
                onFocusChanged = {isPasswordFocused.value = it}
            )

            Spacer(modifier = Modifier.height(20.dp))

            // clinician login button
            Button(
                onClick = {
                    clinicianViewModel.onLoginClick()
                }, modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200ED)
                )
            ) {
                Icon(painter = painterResource(R.drawable.login),
                    contentDescription = "Login",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "Clinician Login", fontSize = 18.sp)
            }
        }
    }
}

/**
 * Modal dialog allows user to edit their username
 * Consist of text field and cancel/save buttons
 */
@Composable
fun EditUsernameDialog(
    currentUsername: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null
){
    Dialog(onDismissRequest = {onDismissRequest()}) {
        // rectangle shape with rounded corners
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Username",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = currentUsername,
                    onValueChange = onValueChange,
                    singleLine = true,
                    isError = errorMessage != null
                )
                if (errorMessage != null){
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons (Cancel and Save buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    CancelButton(onClick = { onDismissRequest() })
                    Spacer(modifier = Modifier.width(8.dp))
                    SaveButton(onClick = onConfirm)
                }
            }
        }
    }
}

/**
 * Button for user to cancel update his/her username
 */
@Composable
fun CancelButton(onClick: () -> Unit){
    OutlinedButton (onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color(0xFF6200ED))
    ) {
        Text(
            text = "Cancel",
            fontSize = 16.sp,
            color =  Color(0xFF6200ED)
        )
    }
}

/**
 * Button for saving user's updated username
 */
@Composable
fun SaveButton(onClick: () -> Unit){
    Button (onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Text(text = "Save", fontSize = 16.sp)
    }
}
