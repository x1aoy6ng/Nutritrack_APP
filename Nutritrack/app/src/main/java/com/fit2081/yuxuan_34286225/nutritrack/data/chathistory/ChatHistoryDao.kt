package com.fit2081.yuxuan_34286225.nutritrack.data.chathistory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    /**
     * Insert new chat conversation into the database
     */
    @Insert
    suspend fun insertChatConversation(history: ChatHistory)

    /**
     * Updates the chat conversation title based on its id
     */
    @Query("UPDATE chatHistory SET title = :newTitle WHERE chatId = :chatId")
    suspend fun updateChatTitle(chatId: Int, newTitle: String)

    /**
     * Delete a specific chat conversation
     */
    @Query("DELETE FROM chatHistory WHERE chatId = :chatId")
    suspend fun deleteConversationByChatId(chatId: Int)

    /**
     * Get a specific chat conversation by its id
     */
    @Query("SELECT * FROM chatHistory WHERE chatId = :chatId LIMIT 1")
    suspend fun getConversationByChatId(chatId: Int): ChatHistory?

    /**
     * Get all the chat history records for specific user, sorted it by latest
     */
    @Query("SELECT * FROM chatHistory WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllChatHistories(userId: String): Flow<List<ChatHistory>>

}

