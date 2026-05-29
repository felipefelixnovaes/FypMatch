package com.ideiassertiva.FypMatch.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.models.Report
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun submitReport(report: Report): Result<Unit> {
        return try {
            firestore.collection("reports").document(report.reportId).set(report).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(userId: String, blockedUserId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("blocked").document(blockedUserId)
                .set(mapOf("blockedUserId" to blockedUserId, "timestamp" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isUserBlocked(userId: String, targetUserId: String): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .collection("blocked").document(targetUserId).get().await().exists()
        } catch (e: Exception) { false }
    }

    suspend fun getBlockedUserIds(userId: String): List<String> {
        return try {
            firestore.collection("users").document(userId)
                .collection("blocked").get().await()
                .documents.mapNotNull { it.getString("blockedUserId") }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun unblockUser(userId: String, blockedUserId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("blocked").document(blockedUserId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}