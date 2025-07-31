package com.fit2081.yuxuan_34286225.nutritrack.features.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.BottomBar
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel


class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
            val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
                this, UserProfileViewModel.UserProfileViewModelFactory(this@HomePage)
            )[UserProfileViewModel::class.java]

            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                FoodQualityScreen(userProfileViewModel, navController)
            }
        }
    }
}

/**
 * Function for displaying Home Screen
 *
 * @param modifier Modifier to be applied to the screen container
 * */
@Composable
fun FoodQualityScreen(
    userProfileViewModel: UserProfileViewModel,
    navController: NavHostController
){
    val userId = AuthManager.getPatientId().toString()

    LaunchedEffect(Unit) {
        userProfileViewModel.loadUserProfile(userId)
    }

    val userName by userProfileViewModel.userName.collectAsState()
    val totalScore by userProfileViewModel.totalScore.collectAsState()

    Scaffold (modifier = Modifier.fillMaxSize(), bottomBar = { BottomBar(navController) }, containerColor = Color.White)
    { innerPadding ->
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
        ){
            Spacer(modifier = Modifier.height(8.dp))
            GreetingSection(userName, navController)
            Spacer(modifier = Modifier.height(8.dp))
            FoodQualityScore(totalScore) {
                // navigate to Insight screen
                navController.navigate("Insights")
            }
            Spacer(modifier = Modifier.height(30.dp))
            ExplanationText()
        }
    }
}

/**
 * Function for displaying Greeting Section
 * */
@Composable
fun GreetingSection(
    userName: String,
    navController: NavHostController
){
    Column (modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
    ){
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Hello,", fontSize = 18.sp, fontWeight = FontWeight.W500, color = Color.Gray)
        Text(text = userName, fontSize = 36.sp, fontWeight = FontWeight.W500)

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = "You've already filled in your Food intake Questionnaire, but you can change details here:",
                fontSize = 12.sp, fontWeight = FontWeight.W500,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f))
            EditButton {
                // navigate to the Questionnaire Page
                navController.navigate("Questionnaire")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(R.drawable.food_quality),
            contentDescription = "Food Quality Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    }
}

/**
 * Function for displaying the Food Quality Score Section
 *
 * @param totalScore: user's total food score
 * @param onClick: callback invoked when the button is clicked
 * */
@Composable
fun FoodQualityScore(totalScore: Float?, onClick: () -> Unit){
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
    {
        Text(text = "My Score", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        // when user clicks on see all scores, go to Insight Screen
        Row(modifier = Modifier.clickable {onClick()}){
            Text(text = "See all scores", fontSize = 14.sp, fontWeight = FontWeight.W500, color = Color.Gray)
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                tint = Color.Gray,
                modifier =  Modifier.size(25.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
    {
        Box(modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color = Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ){
            Icon(painter = painterResource(R.drawable.arrow_upward),
                contentDescription = "Arrow Upward",
                modifier = Modifier.size(25.dp))
        }

        Spacer(modifier = Modifier.width(15.dp))
        Text(text = "Your Food Quality Score")
        Spacer(modifier = Modifier.weight(1f))
        // display the total score in integer format
        Text(text = "${totalScore?.roundToInt()}/100", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
    }
}

/**
 * Function for explanation text
 * */
@Composable
fun ExplanationText(){
    Text(text = "What is the Food Quality Score?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Text(text = "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.",
        fontSize = 12.sp,
        fontWeight = FontWeight.W400,
        lineHeight = 18.sp
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(text = "The personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food choices.",
        fontSize = 12.sp,
        fontWeight = FontWeight.W400,
        lineHeight = 18.sp)
}

/**
 * Function for the edit button
 *
 * @param onClick: callback invoked when the button is clicked
 * */
@Composable
fun EditButton(onClick: () -> Unit){
    Button (onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Edit", fontSize = 16.sp)
    }
}