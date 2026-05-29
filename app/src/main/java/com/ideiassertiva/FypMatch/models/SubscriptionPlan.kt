package com.ideiassertiva.FypMatch.models

data class SubscriptionPlan(
    val planId: String,
    val name: String,
    val price: Double,
    val dailyLikes: Int,
    val dailySuperLikes: Int,
    val dailyIACredits: Int,
    val features: List<String>,
    val isPopular: Boolean = false,
    val badge: String? = null
)