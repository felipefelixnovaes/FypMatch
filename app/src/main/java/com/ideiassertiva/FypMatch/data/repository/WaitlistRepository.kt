package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.model.WaitlistUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaitlistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("waitlist")

    suspend fun addToWaitlist(user: WaitlistUser): Result<Unit> {
        return try {
            collection.document(user.email).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isOnWaitlist(email: String): Boolean {
        return try {
            collection.document(email).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getWaitlistCount(): Int {
        return try {
            collection.count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getWaitlistPosition(email: String): Int {
        return try {
            val doc = collection.document(email).get().await()
            (doc.getLong("position") ?: 0L).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
