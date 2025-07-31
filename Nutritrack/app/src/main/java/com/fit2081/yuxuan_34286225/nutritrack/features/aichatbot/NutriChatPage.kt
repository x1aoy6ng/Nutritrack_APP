package com.fit2081.yuxuan_34286225.nutritrack.features.aichatbot

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import com.fit2081.yuxuan_34286225.nutritrack.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fit2081.yuxuan_34286225.nutritrack.data.chathistory.ChatHistory
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.TopAppBar
import com.fit2081.yuxuan_34286225.nutritrack.shared.navigation.TopAppBarWithActions
import com.fit2081.yuxuan_34286225.nutritrack.features.settings.CancelButton
import com.fit2081.yuxuan_34286225.nutritrack.features.settings.SaveButton
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.UserProfileViewModel
import com.fit2081.yuxuan_34286225.nutritrack.ui.theme.NutritrackTheme
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel.GenAIViewModel
import java.text.DateFormat
import java.util.Date

class NutriChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // initialise the UserProfileViewModel using ViewModelProvider with a factory pattern
        val userProfileViewModel: UserProfileViewModel = ViewModelProvider(
            this, UserProfileViewModel.UserProfileViewModelFactory(this@NutriChatPage)
        )[UserProfileViewModel::class.java]

        // initialise the genAIViewModel using ViewModelProvider with a factory pattern
        val genAIViewModel: GenAIViewModel = ViewModelProvider(
            this, GenAIViewModel.GenAIViewModelFactory(this@NutriChatPage)
        )[GenAIViewModel::class.java]

        setContent {
            NutritrackTheme {
                // initialise the NavHostController for managing navigation within the app
                val navController: NavHostController = rememberNavController()
                NutriChatScreen(genAIViewModel, userProfileViewModel, navController)
            }
        }
    }
}

/**
 * Displays the main NutriChat AI screen where users interact with the AI assistant
 * Loads the past conversation if chatId is provided, otherwise start new chat
 */
@Composable
fun NutriChatScreen(
    genAIViewModel: GenAIViewModel,
    userProfileViewModel: UserProfileViewModel,
    navController: NavHostController,
    chatId: Int? = null
){
    val userId = AuthManager.getPatientId().toString()
    val userName by userProfileViewModel.userName.collectAsState()
    val foodIntakeSummary by userProfileViewModel.foodIntakeSummary.collectAsState()

    val chatMessages by genAIViewModel.chatMessages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // check whether chatId exists
    LaunchedEffect(chatId) {
        if (chatId != null){
            // load specific conversation with the genAI
            genAIViewModel.loadChatById(chatId)
        } else {
            genAIViewModel.clearChat()
        }
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // check whether chatId exists to differentiate current VS history session
            if (chatId == null){
                TopAppBarWithActions(genAIViewModel, userId, text = "NutriTrack AI Assistant", navController = navController,
                    onHistoryClick = {
                        genAIViewModel.saveCurrentConversation(userId)
                        navController.navigate("ChatHistory")},
                    onNewChatClick = {
                        genAIViewModel.saveCurrentConversation(userId)
                        genAIViewModel.clearChat()
                    })
            } else {
                TopAppBar(
                    text = "NutriTrack AI Assistant",
                    navController = navController
                )
            }
        },
        // user input the message
        bottomBar = { ChatInputSection(
            message = inputText,
            onTextChange = {inputText = it},
            onSendClick = {
                if (inputText.isNotBlank()){
                    genAIViewModel.sendChatMessage(inputText, userName, foodIntakeSummary)
                    // after sending the message, clear the text filed
                    inputText = ""
                }
            }
        ) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            reverseLayout = false
        ) {
            items(chatMessages.size) { index ->
                val (message, isUser) = chatMessages[index]
                ChatBubble(message = message, isUser = isUser)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Display single message bubble in chat screen
 */
@Composable
fun ChatBubble(
    message: String,
    isUser: Boolean
){
    // shows loading indicator before display the genAI message
    if(message == "LOADING"){
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ){
            CircularProgressIndicator(color = Color(0xFF6200EE), strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        }
        return
    }

    val bubbleColor = if (isUser) Color(0xFF6200EE) else Color.LightGray
    val textColor = if (isUser) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ){
        if (!isUser){
            // add the ai assistant image with the genAI message
            Image(
                painter = painterResource(R.drawable.ai_assistant),
                contentDescription = "AI assistant",
                modifier = Modifier.size(30.dp).padding(end = 5.dp),
                contentScale = ContentScale.Fit
            )
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            modifier = Modifier.padding(vertical = 4.dp).defaultMinSize(minWidth = 40.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).wrapContentWidth(),
            )
        }
    }
}

/**
 * Section at the bottom of the chat screen for typing and sending messages
 */
@Composable
fun ChatInputSection(
    message: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
){
    Box(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 35.dp)){
        OutlinedTextField(
            value = message,
            onValueChange = onTextChange,
            placeholder = {Text("Send a message...")},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            trailingIcon = {
                IconButton(onClick = onSendClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF6200EE)
                    )
                }
            }
        )
    }
}

/**
 * Display a list of previous chat histories for the logged-in user
 * Allow editing or deleting a chat
 */
@Composable
fun ChatHistoryScreen(
    genAIViewModel: GenAIViewModel,
    navController: NavHostController
){
    val userId = AuthManager.getPatientId().toString()
    val chatHistories by genAIViewModel.chatHistories.collectAsState()
    var selectedChatHistory by remember { mutableStateOf<ChatHistory?>(null) }

    // load all the chat history from database
    LaunchedEffect(Unit) {
        genAIViewModel.loadChatHistory(userId)
    }

    Scaffold (topBar = { TopAppBar(text = "Chat History", navController = navController) }
    ) { innerPadding -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(chatHistories.size) { index ->
                val chatHistory = chatHistories[index]
                ChatHistoryCard(
                    history = chatHistory,
                    onChatSelected = {
                        // navigate to that particular chat history
                        navController.navigate("ChatHistory/${chatHistory.chatId}")
                    },
                    onEdit = {
                        selectedChatHistory = it
                    },
                    onDelete = {
                        genAIViewModel.deleteConversation(it.chatId)
                    }
                )
            }
        }

        // display the dialog
        selectedChatHistory?.let { chat ->
            EditTitleDialog(
                currentTitle = chat.title,
                onDismissRequest = {selectedChatHistory = null},
                onConfirm = { newTitle ->
                    genAIViewModel.changeChatTitle(chat.chatId, newTitle)
                    selectedChatHistory = null
                }
            )
        }
    }
}

/**
 * Dialog popup that allows editing the title of save chat history
 */
@Composable
fun EditTitleDialog(
    currentTitle: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
){
    var text by remember { mutableStateOf(currentTitle) }

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
                    text = "Title",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {text = it},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons (Cancel and Save buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    CancelButton(onClick = { onDismissRequest() })
                    Spacer(modifier = Modifier.width(8.dp))
                    SaveButton(onClick = {onConfirm(text)})
                }
            }
        }
    }
}

/**
 * Card that displays individual chat history items
 */
@Composable
fun ChatHistoryCard(
    history: ChatHistory,
    onChatSelected: (ChatHistory) -> Unit,
    onEdit: (ChatHistory) -> Unit,
    onDelete: (ChatHistory) -> Unit
) {
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECE8F2))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .clickable { onChatSelected(history) },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)
            ) {
                Text(text = history.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = DateFormat.getDateTimeInstance().format(Date(history.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row {
                // edit the chat title
                IconButton(onClick = {onEdit(history)}) {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = "Edit Chat Title",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // delete the chat
                IconButton(onClick = { onDelete(history) }) {
                    Icon(
                        painter = painterResource(R.drawable.trash),
                        contentDescription = "Delete Chat",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
