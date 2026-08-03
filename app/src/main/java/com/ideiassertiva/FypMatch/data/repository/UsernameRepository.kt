package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ideiassertiva.FypMatch.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsernameRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) {

    private val usernamesCollection = firestore.collection("usernames")
    private val usersCollection = firestore.collection("users")

    fun normalizeUsername(raw: String): String? {
        val normalized = raw.trim().lowercase()
        if (normalized.length < 3 || normalized.length > 20) return null
        val pattern = Regex("^[a-z][a-z0-9_]{2,19}$")
        return if (pattern.matches(normalized)) normalized else null
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        val normalized = normalizeUsername(username) ?: return false
        val doc = usernamesCollection.document(normalized).get().await()
        if (!doc.exists()) return true
        val ownerId = doc.getString("userId")
        return ownerId == userRepository.getCurrentUserId()
    }

    suspend fun claimUsername(rawUsername: String): Result<String> {
        val normalized = normalizeUsername(rawUsername)
            ?: return Result.failure(
                IllegalArgumentException(
                    "Username inválido (3–20 letras minúsculas, números ou _, começando com letra)"
                )
            )
        val userId = userRepository.getCurrentUserId()
            ?: return Result.failure(IllegalStateException("Usuário não autenticado"))
        val previousUsername = userRepository.currentUser.value?.username

        return try {
            firestore.runTransaction { transaction ->
                val newRef = usernamesCollection.document(normalized)
                val existing = transaction.get(newRef)
                if (existing.exists() && existing.getString("userId") != userId) {
                    throw IllegalStateException("Esse nome de usuário já está em uso.")
                }
                transaction.set(newRef, mapOf("userId" to userId))
                if (!previousUsername.isNullOrBlank() && previousUsername != normalized) {
                    transaction.delete(usernamesCollection.document(previousUsername))
                }
                transaction.update(usersCollection.document(userId), "username", normalized)
                null
            }.await()
            userRepository.loadCurrentUser()
            Result.success(normalized)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun userIdForUsername(rawUsername: String): String? {
        val normalized = normalizeUsername(rawUsername) ?: return null
        val doc = usernamesCollection.document(normalized).get().await()
        return doc.getString("userId")
    }

    suspend fun userForUsername(rawUsername: String): User? {
        val userId = userIdForUsername(rawUsername) ?: return null
        return userRepository.getUserById(userId)
    }
}
