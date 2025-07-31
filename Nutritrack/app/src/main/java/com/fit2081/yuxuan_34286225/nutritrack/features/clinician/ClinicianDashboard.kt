package com.fit2081.yuxuan_34286225.nutritrack.features.clinician

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.BottomBar
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.shared.uistate.UiState
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import com.fit2081.yuxuan_34286225.nutritrack.features.settings.SettingClinician

class ClinicianDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val clinicianViewModel: ClinicianViewModel = ViewModelProvider(
            this, ClinicianViewModel.ClinicianViewModelFactory(this@ClinicianDashboard)
        )[ClinicianViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@ClinicianDashboard)
        )[GenAIViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                ClinicianScreen(clinicianViewModel, genAIViewModel, navController)
            }
        }
    }
}

/**
 * Main Screen after successful clinician login
 * Displays average HEIFA scores and allows clinicians view AI-generated insights, back to clinician login screen
 */
@Composable
fun ClinicianScreen(
    clinicianViewModel: ClinicianViewModel,
    genAIViewModel: GenAIViewModel,
    navController: NavHostController
){
    // variables for male/female average scores
    val maleAvgScore = clinicianViewModel.avgMaleScore
    val femaleAvgScore = clinicianViewModel.avgFemaleScore

    // observe the UI state from ViewModel as a state that triggers recomposition when changed
    val insightState by genAIViewModel.insightState.collectAsState()

    // create a scrollable state for the text display area
    val scrollState = rememberScrollState()

    Scaffold (modifier = Modifier.fillMaxSize(), bottomBar = { BottomBar(navController) }, containerColor = Color.White)
    { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState))
        {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                HeifaSection(maleAvgScore, femaleAvgScore)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                // call the gen AI to find the data pattern button
                FindDataPatternButton {
                    val dataset = clinicianViewModel.generatePatientDataset()
                    genAIViewModel.findDataPatterns(dataset)
                }
                Spacer(modifier = Modifier.height(5.dp))
                // display the result
                AIResultSection(insightState)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DoneButton(
                        modifier = Modifier
                            .padding(end = 10.dp)
                    ) {
                        // navigate users back to clinician login screen
                        clinicianViewModel.clearState()
                        navController.navigate(SettingClinician.Clinician.route)
                    }
                }
            }
        }
    }
}

/**
 * Display the section header and two HEIFA score cards (average scores for male/female)
 */
@Composable
fun HeifaSection(maleScore: Float, femaleScore: Float){
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Clinician Dashboard",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        HeifaScoreCard("Average HEIFA (Male)", String.format("%.1f", maleScore))
        HeifaScoreCard("Average HEIFA (Female)", String.format("%.1f", femaleScore))
    }
}

/**
 * A single row displaying label and corresponding HEIFA score
 */
@Composable
fun HeifaScoreCard(title: String, score: String){
    Row(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 4.dp)
            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.weight(3f)
        )

        Text(
            text = ":",
            fontSize = 14.sp,
            modifier = Modifier.weight(0.5f)
        )

        Text(
            text = score,
            fontSize = 14.sp,
            modifier = Modifier.weight(1.5f)
        )
    }
}

/**
 * Displays the result of AI pattern analysis
 * Handles 3 ui states
 * - Success: show ai pattern analysis in cards
 * - Error: show error message
 * - Loading: show loading spinner and message
 */
@Composable
fun AIResultSection(uiState: UiState){
    Column (modifier = Modifier.padding(16.dp)
    ){
        if (uiState is UiState.Success){
            val output = uiState.outputText

            Column (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // split output into two part: title and content, ensure particular is not blank
                output.split("\n")
                    .filter{ it.isNotBlank() }
                    .forEach { line ->
                        val parts = line.split(":", limit = 2)
                        val title = parts.getOrNull(0)?.trim()?: ""
                        val content = parts.getOrNull(1)?.trim()?: ""

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 6.dp)
                        ){
                            Column (modifier = Modifier
                                .wrapContentHeight()
                                .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                Text(
                                    // display the title in bold font
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("$title: ")
                                        }
                                        append(content)
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
            }
        } else if (uiState is UiState.Error){
            Text(
                text = uiState.errorMessage,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        } else if (uiState is UiState.Loading){
            // display the loading spinner and a message
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading data pattern...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Button that triggers the genAI to analyse the data patterns based on patient data
 */
@Composable
fun FindDataPatternButton(onClick: () -> Unit){
    Button(onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(painter = painterResource(R.drawable.search),
            contentDescription = "Find data pattern",
            tint = Color.White,
            modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Find Data Pattern", fontSize = 14.sp)
    }
}

/**
 * A done button used to return back to settings screen
 */
@Composable
fun DoneButton(modifier: Modifier = Modifier ,onClick: () -> Unit){
    Button(onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Text(text = "Done", fontSize = 14.sp)
    }
}
