package com.fit2081.yuxuan_34286225.nutritrack.data.chathistory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chatHistory")
data class ChatHistory(
    /**
     * Identifier for the chat session
     */
    @PrimaryKey(autoGenerate = true) val chatId: Int = 0,
    val userId: String,
    val title: String,
    /**
     * The full text content of the chat
     */
    val conversation: String,
    /**
     * The time when the chat was create or last modifier
     */
    val timestamp: Long = System.currentTimeMillis()
)