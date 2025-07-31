package com.fit2081.yuxuan_34286225.nutritrack.shared.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fit2081.yuxuan_34286225.nutritrack.BuildConfig
import com.fit2081.yuxuan_34286225.nutritrack.data.chathistory.ChatHistory
import com.fit2081.yuxuan_34286225.nutritrack.data.chathistory.ChatHistoryRepository
import com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips.NutriCoachTips
import com.fit2081.yuxuan_34286225.nutritrack.data.nutricoachtips.NutriCoachTipsRepository
import com.fit2081.yuxuan_34286225.nutritrack.shared.uistate.UiState
import com.fit2081.yuxuan_34286225.nutritrack.shared.utils.AuthManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenAIViewModel(context: Context): ViewModel() {
    private val nutriCoachTipsRepository = NutriCoachTipsRepository(context = context)
    private val chatHistoryRepository = ChatHistoryRepository(context = context)

    private val _tipState = MutableStateFlow<UiState>(UiState.Initial)
    val tipState: StateFlow<UiState> = _tipState.asStateFlow()

    private val _insightState = MutableStateFlow<UiState>(UiState.Initial)
    val insightState: StateFlow<UiState> = _insightState.asStateFlow()

    private val _tips = MutableStateFlow<List<NutriCoachTips>>(emptyList())
    val tips: StateFlow<List<NutriCoachTips>> = _tips

    private val _chatHistories = MutableStateFlow<List<ChatHistory>>(emptyList())
    val chatHistories: StateFlow<List<ChatHistory>> = _chatHistories

    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages

    /**
     * A constant used to insert a temporary loading message into chat state
     * so that the UI can show the loading indicator while waiting for AI response
     */
    private val LOADING_MARKER = "LOADING"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    /**
     * A list of dynamic prompt templates for generating motivational messages
     */
    private val promptList = listOf(
        "Generate a short encouraging message to help {userName} improve their fruit intake.",
        "{userName}'s fruit intake is {fruitServeSize} serves per day with a variety score of {fruitVariation}. Motivate this patient with a short message to improve their fruit habits.",
        "Given {userName}'s health summary: {foodIntakeSummary}, motivate them with a short weekly food intake challenge message.",
        "Share one fun fruit fact followed by encouragement message for {userName}",
        "Use a joke to make {userName} smile while nudging them (fruit serves: {fruitServeSize}, variety: {fruitVariation}) to eat more fruits. Add some emojis!",
    )

    /**
     * Sends a prompt to the generative AI model and updates the UI state based on response
     */
    fun generateMotivationalMessage(
        userId: String,
        userName: String,
        fruitServeSize: Float,
        fruitVariation: Float,
        foodIntakeSummary: String
    ){
        val prompt = promptList.random()
            .replace("{userName}", userName)
            .replace("{fruitServeSize}", fruitServeSize.toInt().toString())
            .replace("{fruitVariation}", fruitVariation.toInt().toString())
            .replace("{foodIntakeSummary}", foodIntakeSummary)

        // set the UI state to Loading before making API call
        _tipState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _tipState.value = UiState.Success(outputContent)
                    // save the output into database
                    val tip = NutriCoachTips(userId = userId, tip = outputContent)
                    nutriCoachTipsRepository.insertTip(tip)
                }
            } catch (e: Exception){
                _tipState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    /**
     * Identifies dataset and returns three key data patterns
     */
    fun findDataPatterns(data: String){
        val prompt = """
            Based on the following dataset, generate exactly three interesting insights.
            
            Each insight must be presented using this format:
            Title: Insight
            
            - The part before the colon is title. It should be a concise summary of the pattern
            - The part after the colon is insight. It should describe the pattern clearly
            - Do not use bullet points or numbers
            - Do not write any extra labels or text like "title:" or "content:"
            - Do not include any introductory or closing text
            
            $data
        """.trimIndent()

        _insightState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _insightState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception){
                _insightState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    /**
     * Sends a message from user to generative AI and updates the conversation state
     * If user message relates to nutrition, include their food intake summary and prompt the AI
     * to act as nutritionist
     */
    fun sendChatMessage(input: String, userName: String, foodIntakeSummary: String){
        viewModelScope.launch {
            _chatMessages.value += (input to true)
            _chatMessages.value += (LOADING_MARKER to false)
            try {
                // convert user input to lowercase and check whether contains any nutrition related keyword
                // if yes, prompt with patient's food intake summary
                val userInput = input.lowercase()
                val checkNutritionQuery = listOf("diet", "food intake", "nutrients", "meal", "healthy eating",
                    "weight loss", "calories", "nutrition tips").any{ keyword -> userInput.contains(keyword)}

                val prompt = if (checkNutritionQuery){
                    """
                        Act as a nutritionist who provides friendly, practical and safe nutritional advice.
                        Use the patient's data to personalise response positively and clearly.
                        
                        $userName's question: "$input"
                        $userName's food intake summary: $foodIntakeSummary
                    """.trimIndent()
                } else{
                    input
                }

                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )
                val genAiMessage = response.text?.trim() ?: "No response"
                _chatMessages.value = _chatMessages.value
                    .filterNot { it.first == LOADING_MARKER }
                    .plus(genAiMessage to false)
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value
                    .filterNot { it.first == LOADING_MARKER }
                    .plus(("Oops! ${e.localizedMessage}" to false))
            }
        }
    }

    /**
     * Load the tips from database
     */
    fun loadTips(userId: String){
        viewModelScope.launch {
            nutriCoachTipsRepository.getAllTipsById(userId).collect { tipsList ->
                _tips.value = tipsList
            }
        }
    }

    /**
     * Reset the tip state
     */
    fun resetTipState(){
        _tipState.value = UiState.Initial
    }

    /**
     * Delete the tips from database
     */
    fun deleteTips(tip: NutriCoachTips, userId: String){
        viewModelScope.launch {
            nutriCoachTipsRepository.deleteTip(tip)
            // refresh the tips
            loadTips(userId)
        }
    }

    /**
     * Clear the current conversation with genAI
     */
    fun clearChat(){
        _chatMessages.value = emptyList()
    }

    /**
     * Save the current conversation with genAI
     */
    fun saveCurrentConversation(userId: String) {
        // only save the message when user type something (at least one valid message)
        val filterMessages = chatMessages.value.filter {
            it.first.isNotBlank() && it.first != "LOADING"
        }
        if (filterMessages.isEmpty()) return    // don't save if is empty

        val conversation = chatMessages.value
            // use a separator to differentiate message by user and genAI
            .joinToString(separator = "\n---MESSAGE LINE BREAKER---\n") { (message, isUser) ->
                if (isUser) "User: $message" else "GenAI: $message"
            }

        // make the first 20 characters as the current chat title
        val title = chatMessages.value.firstOrNull()?.first?.take(20) ?: "New Chat"
        saveChat(userId, title, conversation)
    }

    /**
     * Save the current conversation with genAI
     */
    fun saveChat(userId: String, title: String, fullConversation: String) {
        viewModelScope.launch {
            chatHistoryRepository.insertChatConversation(userId, title, fullConversation)
        }
    }

    /**
     * Load specific conversation with genAI
     */
    fun loadChatById(chatId: Int) {
        viewModelScope.launch {
            val chat = chatHistoryRepository.getConversationByChatId(chatId)
            chat?.let {
                // split the message line breaker
                val lines = it.conversation.split("\n---MESSAGE LINE BREAKER---\n")
                val messages = lines.map { line ->
                    when {
                        line.startsWith("User: ") -> line.removePrefix("User: ") to true
                        line.startsWith("GenAI: ") -> line.removePrefix("GenAI: ") to false
                        else -> line to false
                    }
                }
                _chatMessages.value = messages
            }
        }
    }

    /**
     * Delete the conversation
     */
    fun deleteConversation(chatId: Int) {
        viewModelScope.launch {
            chatHistoryRepository.deleteConversationByChatId(chatId)
        }
    }

    /**
     * Load all the chat history from database
     */
    fun loadChatHistory(userId: String) {
        viewModelScope.launch {
            chatHistoryRepository.getAllChatHistories(userId).collect {
                _chatHistories.value = it
            }
        }
    }

    /**
     * Allow user to modified the chat title
     */
    fun changeChatTitle(chatId: Int, newTitle: String){
        viewModelScope.launch {
            chatHistoryRepository.updateChatTitle(chatId, newTitle)
                // reload the updated history session
                val userId = AuthManager.getPatientId().toString()
                loadChatHistory(userId)
            }
        }

    /**
     * Reset all GenAI-related UI states and data flows
     */
    fun resetState() {
        _tipState.value = UiState.Initial
        _insightState.value = UiState.Initial
        _tips.value = emptyList()
        _chatMessages.value = emptyList()
    }


    class GenAIViewModelFactory(context: Context): ViewModelProvider.Factory{
        private val context = context.applicationContext
        override fun <T: ViewModel> create(modelClass: Class<T>): T =
            GenAIViewModel(context) as T
    }
}