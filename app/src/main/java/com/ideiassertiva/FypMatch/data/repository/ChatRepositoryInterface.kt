package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.Conversation
import com.ideiassertiva.FypMatch.model.Message
import com.ideiassertiva.FypMatch.model.MessageType
import kotlinx.coroutines.flow.Flow

interface ChatRepositoryInterface {

    fun getConversations(): Flow<List<Conversation>>

    fun getConversationMessages(conversationId: String): Flow<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        type: MessageType = MessageType.TEXT
    ): Message

    suspend fun addReaction(
        conversationId: String,
        messageId: String,
        emoji: String,
        userId: String
    )

    suspend fun getConversationById(conversationId: String): Conversation?
}
