package com.ideiassertiva.FypMatch.repository

import com.ideiassertiva.FypMatch.data.repository.parseConversationDocument
import com.ideiassertiva.FypMatch.data.repository.parseMessageDocument
import com.ideiassertiva.FypMatch.data.repository.stableConversationIdForParticipants
import com.ideiassertiva.FypMatch.model.MessageStatus
import com.ideiassertiva.FypMatch.model.MessageType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class FirebaseChatRepositoryContractTest {

    @Test
    fun `stable conversation id is independent of participant order`() {
        val aToB = stableConversationIdForParticipants("userA", "userB")
        val bToA = stableConversationIdForParticipants("userB", "userA")

        assertEquals(aToB, bToA)
        assertEquals("conv_userA_userB", aToB)
    }

    @Test
    fun `conversation parser accepts participantIds and integer timestamps`() {
        val conversation = parseConversationDocument(
            mapOf(
                "id" to "conv_userA_userB",
                "matchId" to "match-1",
                "participantIds" to listOf("userA", "userB"),
                "status" to "ACTIVE",
                "createdAt" to 1_717_000_000,
                "lastMessageAt" to 1_717_000_100,
                "unreadCount" to mapOf("userA" to 0, "userB" to 2)
            )
        )

        assertNotNull(conversation)
        assertEquals("conv_userA_userB", conversation.id)
        assertEquals(listOf("userA", "userB"), conversation.participants.map { it.userId })
        assertEquals(2, conversation.getUnreadCount("userB"))
    }

    @Test
    fun `message parser keeps top-level participant contract fields optional for model`() {
        val message = parseMessageDocument(
            mapOf(
                "id" to "message-1",
                "conversationId" to "conv_userA_userB",
                "participantIds" to listOf("userA", "userB"),
                "senderId" to "userA",
                "receiverId" to "userB",
                "content" to "Oi!",
                "type" to "TEXT",
                "status" to "SENT",
                "timestamp" to 1_717_000_200,
                "reactions" to emptyList<Map<String, Any>>(),
                "isEdited" to false
            )
        )

        assertNotNull(message)
        assertEquals("conv_userA_userB", message.conversationId)
        assertEquals(MessageType.TEXT, message.type)
        assertEquals(MessageStatus.SENT, message.status)
        assertNotEquals("system", message.senderId)
    }
}
