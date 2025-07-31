package com.fit2081.yuxuan_34286225.nutritrack.features.questionnaire

import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.R
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.TopAppBar
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme

class QuestionnairePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the QuestionnaireViewModel using ViewModelProvider with a factory pattern
        val questionnaireViewModel: QuestionnaireViewModel = ViewModelProvider(
            this, QuestionnaireViewModel.QuestionnaireViewModelFactory(this@QuestionnairePage)
        )[QuestionnaireViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                QuestionnaireScreen(questionnaireViewModel, navController)

            }
        }
    }
}

/**
 * Function for displaying the Questionnaire Screen
 * */
@Composable
fun QuestionnaireScreen(questionnaireViewModel: QuestionnaireViewModel, navController: NavHostController){
    val context = LocalContext.current
    val userID = AuthManager.getPatientId().toString()
    val scrollState = rememberScrollState()
    val saveSuccess = questionnaireViewModel.saveSuccess
    val isQuestionnaireValid = questionnaireViewModel.isQuestionnaireValid
    val timeError = questionnaireViewModel.timeValidationError


    LaunchedEffect(Unit) {
        questionnaireViewModel.resetForm()
        questionnaireViewModel.loadFoodIntake(userID)
    }

    LaunchedEffect(timeError) {
        timeError?.let{
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            questionnaireViewModel.clearTimeValidationError()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
            questionnaireViewModel.clearSaveSuccess()
            // navigate to HomePage
            navController.navigate("Home")
        }
    }

    Scaffold (modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(
            text = "Food Intake Questionnaire",
            navController = navController
        )
    },
        containerColor = Color.White
    ){ innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(innerPadding)
            .padding(18.dp)
            .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top)
        {
            FoodCategoriesSelection(questionnaireViewModel)

            Spacer(modifier = Modifier.height(20.dp))
            PersonaSelection(questionnaireViewModel)

            Spacer(modifier = Modifier.height(25.dp))

            // update the selected persona on drop down menu
            PersonaDropdown(
                selectedPersona = questionnaireViewModel.selectedPersona
            ){
                questionnaireViewModel.updateSelectedPersona(it)
                questionnaireViewModel.onFieldChanged()
            }

            Spacer(modifier = Modifier.height(25.dp))
            Timing(
                mealTime = questionnaireViewModel.mealTime,
                sleepTime = questionnaireViewModel.sleepTime,
                wakeUpTime = questionnaireViewModel.wakeUpTime,
                questionnaireViewModel
            )
            Spacer(modifier = Modifier.height(30.dp))
            HorizontalDivider(color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            SaveButton(
                onClick = {questionnaireViewModel.saveQuestionnaireData(userID)},
                isQuestionnaireValid
            )
        }
    }
}

/**
 * Function for Food Categories Selection Check Box
 *
 * @param label: text label displayed next to the checkbox
 * @param checked: flag indicating whether the checkbox is currently selected
 * @param onCheckedChange: callback invoked when the checkbox state changes, receives the new checked state
 * */
@Composable
fun FoodCheckBox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit){
    Row(verticalAlignment = Alignment.CenterVertically){
        Checkbox(checked = checked, onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6200ED)))
        Text(text = label, fontSize = 14.sp)
    }
}

/**
 * Function for displaying the food categories selection
 *
 * */
@Composable
fun FoodCategoriesSelection(questionnaireViewModel: QuestionnaireViewModel) {

    Text(
        text = "Tick all the food categories you can eat",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "You must select at least one",
        fontSize = 12.sp,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant ,
        modifier = Modifier.padding(bottom = 5.dp)
    )

    // we have 3 column and we need to create 3 checkboxes in each column
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween)
    {
        Column {
            FoodCheckBox(
                "Fruits",
                checked = questionnaireViewModel.fruitChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateFruitChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
            FoodCheckBox(
                "Red Meat",
                checked = questionnaireViewModel.redMeatChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateRedMeatChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
            FoodCheckBox(
                "Fish",
                checked = questionnaireViewModel.fishChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateFishChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
        }

        Column{
            FoodCheckBox(
                "Vegetables",
                checked = questionnaireViewModel.vegetableChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateVegetableChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
            FoodCheckBox(
                "Seafood",
                checked = questionnaireViewModel.seafoodChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateSeafoodChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
            FoodCheckBox(
                "Eggs",
                checked = questionnaireViewModel.eggChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateEggChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
        }

        Column{
            FoodCheckBox(
                "Grains",
                checked = questionnaireViewModel.grainChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateGrainChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
            FoodCheckBox(
                "Poultry",
                checked = questionnaireViewModel.poultryChecked,
                onCheckedChange = {
                    questionnaireViewModel.updatePoultryChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
            FoodCheckBox(
                "Nuts/Seeds",
                checked = questionnaireViewModel.nutSeedChecked,
                onCheckedChange = {
                    questionnaireViewModel.updateNutSeedChecked(it)
                    questionnaireViewModel.onFieldChanged()
                }
            )
        }
    }
}

/**
 * Function for displaying the persona selection section
 * */
@Composable
fun PersonaSelection(questionnaireViewModel: QuestionnaireViewModel) {
    // temporary UI states
    var showDialog by remember { mutableStateOf(false) }
    var selectedPersona by remember { mutableStateOf("") }

    Text(text = "Your Persona", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "People can be broadly classified into 6 different types based on their eating preferences. Click on each button below to find out the different types, and select the type that best fits you!",
        fontSize = 11.8.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.W500 // thickness of the font
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 2 columns, in each row consist of 3 buttons
    Column {
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PersonaButton("Health Devotee"){ selectedPersona = "Health Devotee" ; showDialog = true}
            PersonaButton("Mindful Eater") { selectedPersona = "Mindful Eater"; showDialog = true}
            PersonaButton("Wellness Striver") { selectedPersona =  "Wellness Striver"; showDialog = true}
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

    Column {
        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PersonaButton("Balance Seeker") { selectedPersona = "Balance Seeker"; showDialog = true}
            PersonaButton("Health Procrastinator") { selectedPersona = "Health Procrastinator"; showDialog = true}
            PersonaButton("Food Carefree") { selectedPersona = "Food Carefree"; showDialog = true}
        }
    }

    // handle the dialog
    if (showDialog && selectedPersona.isNotEmpty()){
        DialogWithImage(onDismissRequest = {showDialog = false},
            image = getPersonaImage(selectedPersona),
            imageDescription = selectedPersona,
            label = selectedPersona,
            labelInfo = getPersonaDescription(selectedPersona)
        )
    }
}

/**
 * Function for displaying the persona section button
 *
 * @param label: text label displayed on button
 * @param onClick: callback invoked when the button is clicked
 * */
@Composable
fun PersonaButton(label: String, onClick:() -> Unit){
    Button(onClick = onClick,
        shape = RoundedCornerShape(3.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200ED)),
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = Modifier.height(35.dp)
    )
    {
        Text(text = label, fontSize = 13.sp)
    }
}

/**
 * Function for getting the Persona Image
 *
 * @param persona: name of the persona
 * */
@Composable
// Int class due to (resource ID of image) stored as integers
fun getPersonaImage(persona: String): Int{
    return when (persona) {
        "Health Devotee" -> R.drawable.persona_1
        "Mindful Eater" -> R.drawable.persona_2
        "Wellness Striver" -> R.drawable.persona_3
        "Balance Seeker" -> R.drawable.persona_4
        "Health Procrastinator" -> R.drawable.persona_5
        "Food Carefree" -> R.drawable.persona_6
        else -> R.drawable.default_image
    }
}

/**
 * Function for getting the Persona Description
 *
 * @param persona: name of the persona
 * */
@Composable
// int class due to (resource ID of image) stored as integers
fun getPersonaDescription(persona: String): String{
    return when (persona) {
        "Health Devotee" -> "I’m passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy."
        "Mindful Eater" -> "I’m health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media."
        "Wellness Striver" -> "I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I’ve tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I’ll give it a go."
        "Balance Seeker" -> "I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn’t have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips."
        "Health Procrastinator" -> "I’m contemplating healthy eating but it’s not a priority for me right now. I know the basics about what it means to be healthy, but it doesn’t seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life."
        "Food Carefree" -> "I’m not bothered about healthy eating. I don’t really see the point and I don’t think about it. I don’t really notice healthy eating tips or recipes and I don’t care what I eat."
        else -> "No description"
    }
}

/**
 * Function for displaying dialog with image
 *
 * @param onDismissRequest: callback function invoked when the user dismisses the dialog
 * @param image: image to be displayed on the dialog
 * @param imageDescription: description for the image
 * @param label: title to be displayed under the image (persona name)
 * @param labelInfo: description to be displayed under the title (persona description)
 * */
@Composable
fun DialogWithImage(onDismissRequest: () -> Unit, image: Int, imageDescription: String, label: String, labelInfo: String){
    Dialog(onDismissRequest = {onDismissRequest()}) {
        // draw a rectangle shape with rounded corners
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column (modifier = Modifier
                .wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Spacer(modifier = Modifier.padding(6.dp))
                Image(painter = painterResource(id = image),
                    contentDescription = "$imageDescription Image",
                    modifier = Modifier.height(120.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // limits the text width to 90% of the dialog
                ){
                    Text(text = labelInfo, fontSize = 10.sp, lineHeight = 15.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.W400)
                }

                Spacer(modifier = Modifier.padding(5.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    TextButton(onClick = {onDismissRequest()},
                        modifier = Modifier
                            .width(120.dp)
                            .height(35.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200ED)
                        )
                    ) {
                        Text("Dismiss", fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.padding(5.dp))
            }
        }
    }
}

/**
 * Function for displaying dropdown menu of selecting the best fitted persona
 *
 * @param selectedPersona: selected persona to be shown in the text field
 * @param onSelected: callback function that is invoked when a persona is selected from the dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDropdown(selectedPersona: String, onSelected: (String) -> Unit){
    // track whether the dropdown menu is currently expanded or collapsed
    var isExpanded by remember { mutableStateOf(false) }
    // list of available persona options
    val personaOptions = listOf(
        "Health Devotee", "Mindful Eater", "Wellness Striver",
        "Balance Seeker", "Health Procrastinator", "Food Carefree"
    )

    Text(
        text = "Which persona best fits you?",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(5.dp))

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {isExpanded = !isExpanded}
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedPersona,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(text = "Select option", color = Color.Gray, fontSize = 14.sp)},
            trailingIcon = {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Dropdown Arrow"
                )
            },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color.Gray
            )
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {isExpanded = false}
        ) {
            personaOptions.forEach{option ->
                DropdownMenuItem(
                    text = { Text(text = option)},
                    onClick = {
                        onSelected(option)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * Function for displaying the timing section
 *
 * @param mealTime: time string for the user's biggest meal of the day
 * @param sleepTime: time string for when the user typically goes to sleep
 * @param wakeUpTime: time string for when the user typically wakes up
 */
@Composable
fun Timing(mealTime: MutableState<String>, sleepTime: MutableState<String>, wakeUpTime: MutableState<String>, questionnaireViewModel: QuestionnaireViewModel){
    // call the function to return TimePickerDialog
    val mTimePickerDialog = timePickerDialogSection(
        mTime = questionnaireViewModel.mealTime,
        setFlag = {questionnaireViewModel.markMealAsSelected()},
        onTimeChanged = {
            val isValid = questionnaireViewModel.validateTime(
                mealTime = mealTime,
                sleepTime = sleepTime,
                wakeUpTime = wakeUpTime,
                isMeal = true,
                isSleep = false
            )
            if (isValid) questionnaireViewModel.onFieldChanged()
        })

    val sTimePickerDialog = timePickerDialogSection(
        mTime = sleepTime,
        setFlag = {questionnaireViewModel.markSleepAsSelected()},
        onTimeChanged = {
            val isValid = questionnaireViewModel.validateTime(
                mealTime = mealTime,
                sleepTime = sleepTime,
                wakeUpTime = wakeUpTime,
                isMeal = false,
                isSleep = true
            )
            if (isValid) questionnaireViewModel.onFieldChanged()
        })

    val wTimePickerDialog = timePickerDialogSection(
        mTime = wakeUpTime,
        setFlag = {questionnaireViewModel.markWakeUpAsSelected()},
        onTimeChanged = {
            val isValid = questionnaireViewModel.validateTime(
                mealTime = mealTime,
                sleepTime = sleepTime,
                wakeUpTime = wakeUpTime,
                isMeal = false,
                isSleep = false
            )
            if (isValid) questionnaireViewModel.onFieldChanged()
        })

    Column {
        Text(text = "Timings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        TimingPickerRow(mTimePickerDialog,"What time of day approx. do you normally eat your biggest meal?", mealTime)
        Spacer(modifier = Modifier.height(16.dp))

        TimingPickerRow(sTimePickerDialog,"What time of day approx. do you go to sleep at night?", sleepTime)
        Spacer(modifier = Modifier.height(16.dp))

        TimingPickerRow(wTimePickerDialog, "What time of day approx. do you wake up in the morning?", wakeUpTime)
    }
}

/**
 * Function of creating timing picker row
 *
 * @param timePickerDialog: the `TimePickerDialog` that used to display the time selection UI
 * @param text: description shown on the left of the time picker
 * @param timeState: mutable state that holds the currently selected time
 */
@Composable
fun TimingPickerRow(timePickerDialog: TimePickerDialog, text: String, timeState: MutableState<String>){
    Row {
        Text(text = text,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.W500
        )
        Spacer(modifier = Modifier.padding(6.dp))

        OutlinedButton(onClick = {timePickerDialog.show()},
            modifier = Modifier
                .width(120.dp)
                .height(35.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(painter = painterResource(R.drawable.clock),
                    contentDescription = "Clock",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black)
                // displayed selected time inside the button
                Text(text = timeState.value,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray)
            }
        }
    }
}

/**
 * Function of creating time picker dialog
 *
 * @param mTime: mutable state used to store and reflect the selected time as a string
 * */
@Composable
fun timePickerDialogSection(
    mTime: MutableState<String>,
    setFlag: () -> Unit,
    onTimeChanged: () -> Unit): TimePickerDialog
{
    // get the current context
    val mContext = LocalContext.current
    // get a calendar instance
    val calendar = Calendar.getInstance()

    // get the current hour and minute
    val mHour = calendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = calendar.get(Calendar.MINUTE)

    // set the calendar's time to the current time
    calendar.time = Calendar.getInstance().time

    // return a TimePickerDialog
    return TimePickerDialog(mContext,
        {_, mHour: Int, mMinute: Int ->
            mTime.value = String.format("%02d:%02d", mHour, mMinute)
            setFlag()
            onTimeChanged()
        }, mHour, mMinute, false
    )
}

/**
 * Function for the save button
 *
 * @param onClick: callback invoked when the button is clicked
 * */
@Composable
fun SaveButton(onClick: () -> Unit, isQuestionnaireValid: Boolean){
    var showValidationMessage by remember {mutableStateOf(false)}

    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // display a helper text when the questionnaire form is invalid
        if (showValidationMessage){
            Text(
                text = "Please complete all required sections to enable saving.",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        Button (
            onClick = {
                if (isQuestionnaireValid){
                    showValidationMessage = false
                    onClick()
                } else {
                    showValidationMessage = true
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isQuestionnaireValid) Color(0xFF6200ED) else Color.Gray
            ),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Icon(painter = painterResource(R.drawable.save_button),
                contentDescription = "Save Logo",
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(text = "Save", fontSize = 16.sp)
        }
    }
}