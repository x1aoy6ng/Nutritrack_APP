package com.fit2081.yuxuan_34286225.nutritrack

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.data.patient.PatientsRepository
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.NutriNavHost
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.features.login.AuthViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.ClinicianViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire.QuestionnaireViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.FirstLaunchSeeder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthManager.loadUserId(this)

        val repository = PatientsRepository(this)
        FirstLaunchSeeder.runIfNeeded(this, repository)

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
            this, UserProfileViewModel.UserProfileViewModelFactory(this@MainActivity)
        )[UserProfileViewModel::class.java]

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val clinicianViewModel: ClinicianViewModel = ViewModelProvider(
            this, ClinicianViewModel.ClinicianViewModelFactory(this@MainActivity)
        )[ClinicianViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@MainActivity)
        )[GenAIViewModel::class.java]

        // initialise the authViewModel using ViewModelProvider with a factory pattern
        val authViewModel: AuthViewModel = ViewModelProvider(
            this, AuthViewModel.AuthViewModelFactory(this@MainActivity)
        )[AuthViewModel::class.java]

        // initialise the QuestionnaireViewModel using ViewModelProvider with a factory pattern
        val questionnaireViewModel: QuestionnaireViewModel = ViewModelProvider(
            this, QuestionnaireViewModel.QuestionnaireViewModelFactory(this@MainActivity)
        )[QuestionnaireViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()

                Scaffold (modifier = Modifier.fillMaxSize()) {innerPadding ->
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
 * Function for Welcome Screen of NutriTrack
 * This displays the app title, disclaimer text, clickable link, login button and student ID at the bottom
 * @param modifier Modifier to be applied to the screen container
 */
@Composable
fun WelcomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppTitleLogo()
                Spacer(modifier = Modifier.height(8.dp))
                DisclaimerText()
                ClickableText(
                    url = "https://www.monash.edu/medicine/scs/nutrition/clinics/nutrition",
                    text = "https://www.monash.edu/medicine/scs/nutrition/clinics/nutrition"
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoginButton(navController)
            }

            // make sure StudentID is at the bottom
            StudentIDView(studentName = "Yeo Yu Xuan",
                studentID = "34286225",
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Function for title logo
 * */
@Composable
fun AppTitleLogo(){
    Text(text = "NutriTrack",
        style = TextStyle(
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        ),
        textAlign = TextAlign.Center
    )

    // add space between title and logo image
    Spacer(modifier = Modifier.padding(4.dp))

    Image(
        painter = painterResource(R.drawable.nutritrack),
        contentDescription = "NutriTrack Logo",
        modifier = Modifier.width(200.dp)
    )
}

/**
 * Function for disclaimer text include the clickable link
 * */
@Composable
fun DisclaimerText(){
    Text(
        text = "This app provides general health and nutrition information for educational purposes only. It is not intended as medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional before making any changes to your diet, exercise, or health regimen.\n" +
                "Use this app at your own risk.\n" +
                "If you’d like to see an Accredited Practicing Dietitian (APD), please visit the Monash Nutrition/Dietetics Clinic\n" + "(discounted rates for students):",
        fontSize = 12.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.W500,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

/**
 * Function for Login Button
 * */
@Composable
fun LoginButton(navController: NavHostController){
    Button(
        onClick = {
            navController.navigate("Login")
        },
        modifier = Modifier.fillMaxWidth(0.95f).height(55.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Text(text = "Login", fontSize = 18.sp)
    }
}

/**
 * Function for Clickable Link Text
 * */
@Composable
fun ClickableText(url: String, text: String){
    val context = LocalContext.current
    Text(text = AnnotatedString(text),
        fontSize = 12.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.W500,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,

        modifier = Modifier.clickable {
            // parse the url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }.fillMaxWidth(0.95f)
    )
}

/**
 * Function for Placing Student ID at the bottom
 * */
@Composable
fun StudentIDView(studentName: String, studentID: String, modifier: Modifier = Modifier){
    Column (
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Designed with ❤\uFE0F by $studentName ($studentID)",
            fontSize = 13.sp,
            fontWeight = FontWeight.W400)
    }
}