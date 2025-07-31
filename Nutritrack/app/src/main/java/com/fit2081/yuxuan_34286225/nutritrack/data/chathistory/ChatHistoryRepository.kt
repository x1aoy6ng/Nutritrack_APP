package com.fit2081.yuxuan_34286225.nutritrack.data.chathistory

import android.content.Context
import com.fit2081.yuxuan_34286225.nutritrack.data.NutriDatabase
import kotlinx.coroutines.flow.Flow

class ChatHistoryRepository(context: Context) {
    private val chatHistoryDao = NutriDatabase.getDatabase(context).chatHistoryDao()

    /**
     * Insert new conversation into database
     */
    suspend fun insertChatConversation(userId: String, title: String, conversation: String) {
        return chatHistoryDao.insertChatConversation(ChatHistory(userId = userId, title = title, conversation = conversation))
    }

    /**
     * Updates the chat conversation title based on its id
     */
    suspend fun updateChatTitle(chatId: Int, newTitle: String){
        return chatHistoryDao.updateChatTitle(chatId, newTitle)
    }

    /**
     * Get all the chat history records for specific user, sorted it by latest
     */
    fun getAllChatHistories(userId: String): Flow<List<ChatHistory>>{
        return chatHistoryDao.getAllChatHistories(userId)
    }

    /**
     * Delete a specific chat conversation
     */
    suspend fun deleteConversationByChatId(chatId: Int) {
        return chatHistoryDao.deleteConversationByChatId(chatId)
    }

    /**
     * Get a specific chat conversation by its id
     */
    suspend fun getConversationByChatId(chatId: Int): ChatHistory? {
        return chatHistoryDao.getConversationByChatId(chatId)
    }

}
