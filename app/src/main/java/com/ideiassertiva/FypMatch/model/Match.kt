package com.ideiassertiva.FypMatch.model

import java.util.Date

data class Match(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val lastMessage: String = "",
    val lastMessageAt: Date = Date()
)

data class SwipeRecord(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val action: SwipeType = SwipeType.PASS,
    val createdAt: Date = Date(),
    val isMatch: Boolean = false
)

enum class SwipeType {
    PASS,
    LIKE,
    SUPER_LIKE
}

data class DiscoveryCard(
    val user: User,
    val distance: Int = 0,
    val commonInterests: List<String> = emptyList(),
    val compatibilityScore: Float = 0.0f,
    val photos: List<String> = emptyList(),
    val isVerified: Boolean = false
) 
