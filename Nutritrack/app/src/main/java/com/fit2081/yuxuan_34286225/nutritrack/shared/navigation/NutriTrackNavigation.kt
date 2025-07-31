package com.fit2081.yuxuan_34286225.nutritrack.shared.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.fit2081.yuxuan_34286225.nutritrack.features.aichatbot.NutriChatScreen
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.WelcomeScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.ClinicianScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.settings.ClinicianLoginScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.home.FoodQualityScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.insights.InsightScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.nutricoach.NutriCoachScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.login.ChangePasswordScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.login.LoginScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.login.RegisterScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.login.AuthViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.ClinicianViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.aichatbot.ChatHistoryScreen
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.settings.SettingClinician
import com.fit2081.yuxuan_34286225.nutritrack.features.settings.SettingsScreen
import com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire.QuestionnaireViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire.QuestionnaireScreen
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager

/**
 * Navigation graph for NutriTrack app
 * @param innerPadding
 * @param navController
 */
@Composable
fun NutriNavHost(
    innerPadding: PaddingValues,
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    clinicianViewModel: ClinicianViewModel,
    genAIViewModel: GenAIViewModel,
    authViewModel: AuthViewModel,
    questionnaireViewModel: QuestionnaireViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "NutriNavigation"
    ) {
        composable("NutriNavigation") {
            NavigationScreen(navController, questionnaireViewModel)
        }

        composable("Welcome"){
            WelcomeScreen(Modifier.padding(innerPadding), navController)
        }

        composable("Login"){
            LoginScreen(authViewModel, questionnaireViewModel, navController)
        }

        composable("Register"){
            RegisterScreen(authViewModel, navController)
        }

        composable("Change Password"){
            ChangePasswordScreen(authViewModel, userProfileViewModel, genAIViewModel, navController)
        }

        composable("Questionnaire"){
            QuestionnaireScreen(questionnaireViewModel, navController)
        }

        composable("Home") {
            FoodQualityScreen(userProfileViewModel, navController)
        }

        composable("Insights") {
            InsightScreen(userProfileViewModel, navController)
        }

        composable("NutriCoach") {
            NutriCoachScreen(userProfileViewModel, genAIViewModel, navController)
        }

        // define the setting screen destination
        composable(SettingClinician.Settings.route) {
            SettingsScreen(userProfileViewModel, authViewModel, genAIViewModel, navController)
        }

        // define the clinician login screen destination
        composable(SettingClinician.Clinician.route) {
            ClinicianLoginScreen(clinicianViewModel, navController)
        }

        // define the clinician dashboard screen destination
        composable(SettingClinician.ClinicianDashboard.route) {
            ClinicianScreen(clinicianViewModel, genAIViewModel, navController)
        }

        composable("ChatBox") {
            NutriChatScreen(genAIViewModel, userProfileViewModel, navController, chatId = null)
        }

        // navigate to the chat history screen
        composable("ChatHistory"){
            ChatHistoryScreen(genAIViewModel, navController)
        }

        composable("ChatHistory/{chatId}", arguments = listOf(navArgument("chatId") { type = NavType.IntType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId")
            NutriChatScreen(genAIViewModel, userProfileViewModel, navController, chatId = chatId)
        }
    }
}

/**
 * Top Application Bar used across multiple screens with centered title and back button
 *
 * @param modifier Modifier to be applied to the screen container
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(modifier: Modifier = Modifier, text: String, navController: NavHostController){
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    CenterAlignedTopAppBar(
        // colors property used to customize the appearance of TopAppBar
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0XFFECE8F2)
        ),
        // title displayed in the center of the app bar
        title = {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.W400,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        // create the back button
        navigationIcon = {
            IconButton(onClick = {
                if (onBackPressedDispatcher?.hasEnabledCallbacks() == true){
                    // onBackPressDispatcher is used to handle the back button press in the app
                    // it takes the current activity out of back stack and shows previous activity
                    onBackPressedDispatcher?.onBackPressed()
                } else {
                    navController.navigate("Login")
                }
            }) {
                Icon(modifier = modifier.size(20.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Previous Screen",
                    tint = Color.Gray)
            }
        }
    )
}

/**
 * Top Application Bar specialised for NutriChat screen
 * Includes back button, title, buttons for new chat and views history
 */
@Composable
fun TopAppBarWithActions(
    genAIViewModel: GenAIViewModel,
    userId: String,
    text: String,
    navController: NavHostController,
    onHistoryClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECE8F2))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // back button and title section
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                // save the conversation if user clicks the back button
                genAIViewModel.saveCurrentConversation(userId)
                if (onBackPressedDispatcher?.hasEnabledCallbacks() == true) {
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    navController.navigate("Login")
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.W400,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // new chat and history section
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNewChatClick) {
                Icon(
                    painter = painterResource(R.drawable.new_chat),
                    contentDescription = "New Chat",
                    modifier = Modifier.size(25.dp),
                    tint = Color.Gray
                )
            }

            IconButton(onClick = onHistoryClick) {
                Icon(
                    painter = painterResource(R.drawable.history),
                    contentDescription = "Chat History",
                    modifier = Modifier.size(25.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}



/**
 * Composable function for creating the bottom navigation bar
 */
@Composable
fun BottomBar(navController: NavHostController){
    // list of navigation items
    val navItems = listOf(
        "Home",
        "Insights",
        "NutriCoach",
        "Settings"
    )

    // get the current route reactively
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // navigation bar composable to define the bottom navigation bar
    NavigationBar {
        // iterate through each item in the items list along with its index
        navItems.forEach { item ->
            var isSelected = when (item){
                "Settings" -> currentRoute == SettingClinician.Settings.route
                        || currentRoute == SettingClinician.Clinician.route
                        || currentRoute == SettingClinician.ClinicianDashboard.route
                else -> currentRoute == item
            }

            NavigationBarItem(
                icon = {
                    when (item) {
                        "Home" -> Icon(imageVector = Icons.Filled.Home,
                            contentDescription = "Go Home")
                        "Insights" -> Icon(painter = painterResource(R.drawable.insights),
                            contentDescription = "Insights")
                        "NutriCoach" -> Icon(painter = painterResource(R.drawable.nutricoach),
                            contentDescription = "NutriCoach",
                            modifier = Modifier.size(25.dp))
                        "Settings" -> Icon(imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings")
                    }
                },
                // display item's name as the label
                label = {Text(item)},
                colors = getIconColors(),
                selected = isSelected,
                // actions to perform when this item is clicked
                onClick = {
                    val targetRoute = when (item){
                        "Settings" -> SettingClinician.Settings.route
                        else -> item
                    }
                    // navigate to the corresponding screen based on the item's name
                    navController.navigate(targetRoute)
                }
            )
        }
    }
}

/**
 * Return NavigationBarItemColors used for customising icon and text colors in bottombar
 * */
@Composable
fun getIconColors(): NavigationBarItemColors {
    return NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF6200ED),
        selectedTextColor = Color(0xFF6200ED),
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray
    )
}

/**
 * Redirect the logic handler called at initial route
 * Decides whether user should navigate to
 * - "Login" (if not logged in)
 * - "Home" (if completed questionnaire)
 * - "Welcome" (first time user)
 */
@Composable
fun NavigationScreen(navController: NavHostController, questionnaireViewModel: QuestionnaireViewModel) {
    LaunchedEffect(Unit) {
        val userId = AuthManager.getPatientId()
        val alreadyFilled = questionnaireViewModel.checkIfQuestionnaireExists(userId ?: "")

        val target = when {
            userId == null && alreadyFilled != null -> "Login"
            userId != null && alreadyFilled != null -> "Home"
            else -> "Welcome"
        }

        // navigate and clear splash from back stack
        navController.navigate(target) {
            popUpTo("Splash") { inclusive = true }
        }
    }
}
