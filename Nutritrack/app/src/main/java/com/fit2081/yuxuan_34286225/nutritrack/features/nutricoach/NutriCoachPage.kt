package com.fit2081.yuxuan_34286225.nutritrack.features.nutricoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.BottomBar
import com.fit2081.yuxuan_34286225.nutritrack.features.clinician.DoneButton
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.data.network.fruit.Fruit
import com.fit2081.yuxuan_34286225.nutritrack.data.network.picsum.PicsumRepository
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.shared.uistate.UiState
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel

class NutriCoachPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
            this, UserProfileViewModel.UserProfileViewModelFactory(this@NutriCoachPage)
        )[UserProfileViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@NutriCoachPage)
        )[GenAIViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                NutriCoachScreen(userProfileViewModel, genAIViewModel, navController)
            }
        }
    }
}

/**
 * Main Screen displays fruit information and AI-generated motivational tips
 */
@Composable
fun NutriCoachScreen(
    userProfileViewModel: UserProfileViewModel,
    genAIViewModel: GenAIViewModel,
    navController: NavHostController
){
    val userId = AuthManager.getPatientId().toString()

    var fruitName by remember { mutableStateOf(userProfileViewModel.fruitName) }
    val fruitDetails = userProfileViewModel.fruitDetails

    // create a scrollable state for the text display area
    val firstScrollState = rememberScrollState()
    val secondScrollState = rememberScrollState()

    // observe the UI state from ViewModel as a state that triggers recomposition when changed
    val tipState by genAIViewModel.tipState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val userName by userProfileViewModel.userName.collectAsState()
    val fruitServeSize by userProfileViewModel.fruitServeSize.collectAsState()
    val fruitVariation by userProfileViewModel.fruitVariation.collectAsState()
    val foodIntakeSummary by userProfileViewModel.foodIntakeSummary.collectAsState()
    val isOptimalFruitScore = fruitVariation >= 5 && fruitServeSize >= 2

    Scaffold (modifier = Modifier.fillMaxSize(), bottomBar = { BottomBar(navController) }, containerColor = Color.White)
    { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()){
            Column (modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // fruit details section
                Column(
                    modifier = Modifier.weight(1f).padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = "NutriCoach",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isOptimalFruitScore) {
                        // display a random image
                        RandomImage(userProfileViewModel)
                    } else {
                        FruitInfoSection(
                            userProfileViewModel,
                            fruitName = fruitName,
                            onValueChange = { fruitName = it },
                            onClick = { name ->
                                userProfileViewModel.fetchFruitData(name)
                            },
                            fruitData = fruitDetails,
                            scrollState = firstScrollState
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.95f))

                // gen AI section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(18.dp)
                        .verticalScroll(secondScrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NutriCoachMessageSection(
                        tipState = tipState,
                        onGenerateTip = {
                            // clear previous message
                            genAIViewModel.resetTipState()

                            genAIViewModel.generateMotivationalMessage(
                                userId = userId,
                                userName = userName,
                                fruitServeSize = fruitServeSize,
                                fruitVariation = fruitVariation,
                                foodIntakeSummary = foodIntakeSummary
                            )
                        },
                        onShowTips = { showDialog = true }
                    )
                }
            }

            // display the dialog
            if (showDialog) {
                TipsDialog(
                    userId = userId,
                    onDismissRequest = { showDialog = false },
                    genAIViewModel = genAIViewModel
                )
            }
        }
    }
}

/**
 * Display the AI motivational message, includes generation and show all tips buttons
 */
@Composable
fun NutriCoachMessageSection(
    tipState: UiState,
    onGenerateTip: () -> Unit,
    onShowTips: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.Top
        ) {
            MessageButton(onClick = onGenerateTip)
            Spacer(modifier = Modifier.height(8.dp))
            MessageSection(tipState)
        }

        // Show Tips button at bottom end
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            ShowTipsButton(onClick = onShowTips)
        }
    }
}

/**
 * Display a random image from the Picsum API when user's fruit intake is optimal
 */
@Composable
fun RandomImage(userProfileViewModel: UserProfileViewModel){
    val picsumRepository = remember { PicsumRepository() }
    val imageUrl = remember { picsumRepository.getRandomImageUrl() }

    // inform viewmodel when loading is complete
    LaunchedEffect(imageUrl) {
        userProfileViewModel.updateImageLoading(true)
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(350.dp),
        contentAlignment = Alignment.Center
    ){
        if (userProfileViewModel.isImageLoading){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading image...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        AsyncImage(
            model = imageUrl,
            contentDescription = "Random Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(8.dp)),
            onSuccess = {
                userProfileViewModel.updateImageLoading(false)
            },
            onError = {
                userProfileViewModel.updateImageLoading(false)
            }
        )
    }
}

/**
 * Section for inputting fruit name and displaying the nutritional information
 */
@Composable
fun FruitInfoSection(
    userProfileViewModel: UserProfileViewModel,
    fruitName: String,
    onValueChange: (String) -> Unit,
    onClick: (String) -> Unit,
    fruitData: Fruit?,
    scrollState: ScrollState
){
    var fruitNameError by remember {mutableStateOf(false)}
    val isFruitLoading = userProfileViewModel.isFruitLoading
    val fruitNotFound = userProfileViewModel.fruitNotFound

    Column(modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Fruit Name",
            fontWeight = FontWeight.W400,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // text field for user to input fruit name
            OutlinedTextField(
                value = fruitName,
                onValueChange = {
                    onValueChange(it)
                    if (fruitNameError && it.isNotBlank()) {
                        fruitNameError = false
                    }
                },
                singleLine = true,
                enabled = !isFruitLoading,
                isError = fruitNameError,
                trailingIcon = {
                    if (isFruitLoading){
                        CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(20.dp)
            )

            // details button
            DetailsButton(onClick = {
                if (fruitName.isBlank()) {
                    fruitNameError = true
                } else {
                    onClick(fruitName)
                }
            })
        }

        if (fruitNameError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, top = 5.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Fruit name is required",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
        if(fruitNotFound){
            Text(
                text = "Fruit not found",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top=8.dp).fillMaxWidth(0.95f).align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
            FruitInfoCard("family", fruitData?.family ?: "--")
            FruitInfoCard("calories", fruitData?.nutritions?.calories?.toString() ?: "--")
            FruitInfoCard("fat", fruitData?.nutritions?.fat?.toString() ?: "--")
            FruitInfoCard("sugar", fruitData?.nutritions?.sugar?.toString() ?: "--")
            FruitInfoCard("carbohydrates", fruitData?.nutritions?.carbohydrates?.toString() ?: "--")
            FruitInfoCard("protein", fruitData?.nutritions?.protein?.toString() ?: "--")
        }
    }
}

/**
 * Display key-value pair of fruit nutrition information
 */
@Composable
fun FruitInfoCard(title: String, value: String){
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
            modifier = Modifier.weight(2f)
        )

        Text(
            text = ":",
            fontSize = 14.sp,
            modifier = Modifier.weight(0.5f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            modifier = Modifier.weight(2.5f)
        )
    }
}

/**
 * Button for submitting the fruit name to fetch nutrition details
 */
@Composable
fun DetailsButton(onClick: () -> Unit){
    Button(onClick = onClick,
        modifier = Modifier.height(55.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(painter = painterResource(R.drawable.search),
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Details", fontSize = 14.sp)
    }
}


/**
 * Button used to generate motivational message
 */
@Composable
fun MessageButton(onClick: () -> Unit){
    Button(onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(painter = painterResource(R.drawable.chat_message),
            contentDescription = "Motivational Message",
            tint = Color.White,
            modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Motivational Message (AI)", fontSize = 14.sp)
    }
}

/**
 * Button that opens dialog showing all previously generated motivational tips
 */
@Composable
fun ShowTipsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
){
    Button(onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200ED)
        )
    ) {
        Icon(painter = painterResource(R.drawable.history),
            contentDescription = "Show Tips",
            tint = Color.White,
            modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text = "Shows All Tips", fontSize = 14.sp)
    }
}

/**
 * Dialog to display all motivational tips generated for the user
 */
@Composable
fun TipsDialog(
    userId: String,
    genAIViewModel: GenAIViewModel,
    onDismissRequest: () -> Unit
){
    // load the tips when dialog being open
    LaunchedEffect(true) {
        genAIViewModel.loadTips(userId)
    }

    val tips by genAIViewModel.tips.collectAsState()

    Dialog(onDismissRequest = {onDismissRequest()}) {
        // rectangle shape with rounded corners
        Card (
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ){
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI Tips",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // check whether there are AI tips, if not show empty message, else shows the tip cards
                if (tips.isEmpty()){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "No AI tips found.",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    // display the scrollable tip cards
                    Column (
                        modifier = Modifier
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        tips.forEach { tip ->
                            TipCard(
                                tip = tip.tip,
                                onDelete = {genAIViewModel.deleteTips(tip, userId)}
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DoneButton(
                        onClick = onDismissRequest
                    )
                }
            }
        }
    }
}

/**
 * Card that shows a single motivational tip and can be delete
 */
@Composable
fun TipCard(
    tip: String,
    onDelete: () -> Unit
){
    Card (
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 6.dp)
    ){
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = tip,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            IconButton(onClick = {onDelete()}) {
                Icon(
                    painter = painterResource(R.drawable.trash),
                    contentDescription = "Delete",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(20.dp)
                )
            }
        }
    }
}

/**
 * Display the result of motivational message generated by AI
 */
@Composable
fun MessageSection(uiState: UiState){
    // State that recomputes when uiState changes
    val result = when (uiState) {
        is UiState.Loading -> ""
        is UiState.Error -> uiState.errorMessage
        is UiState.Success -> uiState.outputText
        else -> "" // clear the previous message
    }

    val textColor = when (uiState) {
        is UiState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        contentAlignment = Alignment.Center
    ) {
        if (uiState is UiState.Loading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading message...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        // display the result text
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            Text(
                text = result,
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}
