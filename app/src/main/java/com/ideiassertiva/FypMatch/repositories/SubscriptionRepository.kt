package com.ideiassertiva.FypMatch.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.models.SubscriptionPlan
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAvailablePlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan("free", "Gratuito", 0.0, 10, 1, 0,
                listOf("10 curtidas/dia", "1 Super Curtida/dia", "Conselheiro IA via anuncios")),
            SubscriptionPlan("premium", "Premium", 19.90, 100, 5, 10,
                listOf("100 curtidas/dia", "5 Super Curtidas/dia", "10 creditos IA/dia", "Ver quem curtiu", "Sem anuncios"),
                isPopular = true),
            SubscriptionPlan("vip", "VIP", 39.90, -1, -1, 25,
                listOf("Curtidas ilimitadas", "Super Curtidas ilimitadas", "25 creditos IA/dia", "5 Boosts/mes", "Selo VIP", "Prioridade"),
                badge = "VIP")
        )
    }

    suspend fun getCurrentSubscription(userId: String): SubscriptionPlan {
        return try {
            val doc = firestore.collection("subscriptions").document(userId).get().await()
            if (doc.exists()) {
                val planId = doc.getString("planId") ?: "free"
                getAvailablePlans().find { it.planId == planId } ?: getFreePlan()
            } else getFreePlan()
        } catch (e: Exception) { getFreePlan() }
    }

    suspend fun createCheckoutSession(userId: String, planId: String): String {
        firestore.collection("subscriptions").document(userId)
            .set(mapOf("planId" to planId, "startedAt" to System.currentTimeMillis(), "status" to "active")).await()
        return "checkout_${planId}"
    }

    suspend fun cancelSubscription(userId: String) {
        firestore.collection("subscriptions").document(userId)
            .update(mapOf("status" to "cancelled", "cancelledAt" to System.currentTimeMillis())).await()
    }

    private fun getFreePlan() = SubscriptionPlan("free", "Gratuito", 0.0, 10, 1, 0,
        listOf("10 curtidas/dia", "Conselheiro IA via anuncios"))
}