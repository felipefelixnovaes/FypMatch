package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ideiassertiva.FypMatch.model.User
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class ProfileViewer(
    val user: User,
    val viewedAt: Date
)

@Singleton
class ProfileViewsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {

    private val usersCollection = firestore.collection("users")

    suspend fun recordProfileView(viewedUserId: String) {
        val viewerId = userRepository.getCurrentUserId() ?: return
        if (viewerId == viewedUserId) return
        try {
            usersCollection.document(viewedUserId)
                .collection("viewers")
                .document(viewerId)
                .set(mapOf("viewerId" to viewerId, "viewedAt" to Date()))
                .await()
        } catch (e: Exception) {
            // Best-effort: falha ao registrar visualização não deve travar a tela de perfil
        }
    }

    suspend fun fetchProfileViewers(limit: Long = 50): List<ProfileViewer> {
        val userId = userRepository.getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = usersCollection.document(userId)
                .collection("viewers")
                .orderBy("viewedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val viewerId = doc.getString("viewerId") ?: return@mapNotNull null
                val viewedAt = doc.getDate("viewedAt") ?: Date()
                val viewerUser = userRepository.getUserById(viewerId) ?: return@mapNotNull null
                ProfileViewer(user = viewerUser, viewedAt = viewedAt)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
