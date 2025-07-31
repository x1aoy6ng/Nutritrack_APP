package com.fit2081.yuxuan_34286225.nutritrack.features.insights

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.BottomBar
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.features.login.AuthViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.ClinicianViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire.QuestionnaireViewModel
import kotlin.math.roundToInt

class InsightPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
            this, UserProfileViewModel.UserProfileViewModelFactory(this@InsightPage)
        )[UserProfileViewModel::class.java]

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val clinicianViewModel: ClinicianViewModel = ViewModelProvider(
            this, ClinicianViewModel.ClinicianViewModelFactory(this@InsightPage)
        )[ClinicianViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@InsightPage)
        )[GenAIViewModel::class.java]

        // initialise the authViewModel using ViewModelProvider with a factory pattern
        val authViewModel: AuthViewModel = ViewModelProvider(
            this, AuthViewModel.AuthViewModelFactory(this@InsightPage)
        )[AuthViewModel::class.java]

        // initialise the QuestionnaireViewModel using ViewModelProvider with a factory pattern
        val questionnaireViewModel: QuestionnaireViewModel = ViewModelProvider(
            this, QuestionnaireViewModel.QuestionnaireViewModelFactory(this@InsightPage)
        )[QuestionnaireViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                InsightScreen(userProfileViewModel, navController)
            }
        }
    }
}

/**
 * Function for displaying the insight screen
 *
 * @param modifier Modifier to be applied to the screen container
 * */
@Composable
fun InsightScreen(userProfileViewModel: UserProfileViewModel, navController: NavHostController){
    val context = LocalContext.current
    val totalScore by userProfileViewModel.totalScore.collectAsState()
    val foodScores by userProfileViewModel.foodScore.collectAsState()
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
            FoodScoreSection(foodScores)
            Spacer(modifier = Modifier.height(40.dp))
            TotalFoodScoreSection(totalScore, maxScore = 100f)
            Spacer(modifier = Modifier.height(10.dp))
            ShareButton (context, totalScore)
            ImproveButton {
                // navigate to NutriCoach
                navController.navigate("NutriCoach")
            }
        }
    }
}

/**
 * Function for displaying the food score section
 *
 * @param foodScores: A list of triples represented food category label, (user's score and the maximum possible score for that category)
 * */
@Composable
fun FoodScoreSection(foodScores: List<Pair<String, Pair<Float, Float>>>){
    Column (modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = "Insights: Food Score", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))
        // loop through each food scores and create a slider for it
        foodScores.forEach { (label, scoreData) ->
            val (score, maxScore) = scoreData
            FoodSlider(label, score, maxScore)
        }
    }
}

/**
 * Function for creating the food slider
 *
 * @param label: label representing the food type
 * @param score: score corresponding to each food type
 * @param maxScore: maximum score of each food type (5f or 10f)
 * */
@Composable
fun FoodSlider(label: String, score: Float, maxScore: Float){
    val progress = score / maxScore
    // number of tick on slider
    val tickCount = 4

    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
    ){
        Text(text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.W500,
            lineHeight = 18.sp,
            modifier = Modifier.width(110.dp)) // fixed width to align the text

        Spacer(modifier = Modifier.width(8.dp))

        Slider(
            value = progress,
            onValueChange = {},
            valueRange = 0f..1f,
            enabled = false,   // disable user interaction
            steps = tickCount - 1,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                disabledThumbColor = Color(0xFF5400D5),
                disabledActiveTrackColor = Color(0xFF6316D9),
                disabledInactiveTrackColor = Color(0xFFCAB9E1)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${score.roundToInt()}/${maxScore.roundToInt()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.Start
        )
    }
}

/**
 * Function for displaying the total food score section
 *
 * @param score: user's total food quality score
 * @param maxScore: maximum score which is 100
 * */
@Composable
fun TotalFoodScoreSection(score: Float, maxScore: Float){
    val progress = score / maxScore
    // number of tick on the slider
    val tickCount = 9

    Column (modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Text(text = "Total Food Quality Score", fontWeight = FontWeight.Bold, fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()){
            Slider(
                value = progress,
                onValueChange = { },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                enabled = false,    // disables the user interaction
                steps = tickCount - 1,
                colors = SliderDefaults.colors(
                    disabledThumbColor = Color(0xFF5400D5),
                    disabledActiveTrackColor = Color(0xFF6316D9),
                    disabledInactiveTrackColor = Color(0xFFCAB9E1)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            // display the food quality score
            Text(text = "${score.roundToInt()}/${maxScore.roundToInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.W500)
        }
    }
}

/**
 * Function for the share button
 *
 * @param context: context used to launch the share intent
 * @param totalScore: user's total food quality score
 * */
@Composable
fun ShareButton(context: Context, totalScore: Float){
    Button (onClick = {
        val shareIntent = Intent(ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Heiya, my total food quality score: $totalScore/100")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share your text via"))
    },
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(imageVector = Icons.Default.Share,
            contentDescription = "Edit",
            modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Share with someone", fontSize = 14.sp)
    }
}

/**
 * Function for the improve button
 *
 * @param onClick: callback invoked when the button is clicked
 * */
@Composable
fun ImproveButton(onClick:() -> Unit){
    Button (onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(painter = painterResource(R.drawable.rocket),
            contentDescription = "Edit",
            tint = Color.White,
            modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Improve my diet!", fontSize = 14.sp)
    }
}