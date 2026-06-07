package com.ideiassertiva.FypMatch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ideiassertiva.FypMatch.model.ComplementaryProfile
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.withCompletionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val usersCollection = firestore.collection("users")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Ouvir mudanças de autenticação e carregar usuário automaticamente
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                scope.launch { loadCurrentUser() }
            } else {
                _currentUser.value = null
            }
        }
    }

    suspend fun loadCurrentUser() {
        val firebaseUser = auth.currentUser ?: return
        try {
            val doc = usersCollection.document(firebaseUser.uid).get().await()
            if (doc.exists()) {
                val user = doc.toFypUserOrNull(doc.id) ?: User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: ""
                )
                _currentUser.value = user
            } else {
                // Usuário novo — criar entrada básica com dados do Firebase Auth
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: ""
                )
                usersCollection.document(firebaseUser.uid).set(newUser).await()
                _currentUser.value = newUser
            }
        } catch (e: Exception) {
            // Fallback: criar usuário mínimo com dados do FirebaseAuth
            _currentUser.value = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: ""
            )
        }
    }

    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            if (user.id.isBlank()) return Result.failure(Exception("userId vazio"))
            val updatedUser = user.copy(
                profile = user.profile.withCompletionStatus(),
                lastActive = Date()
            )
            usersCollection.document(updatedUser.id)
                .set(updatedUser, SetOptions.merge())
                .await()
            _currentUser.value = updatedUser
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfile(userId: String, profile: UserProfile): Result<Unit> {
        return try {
            if (userId.isBlank()) return Result.failure(Exception("userId vazio"))
            val now = Date()
            val completedProfile = profile.withCompletionStatus()
            usersCollection.document(userId)
                .set(
                    mapOf(
                        "profile" to completedProfile,
                        "lastActive" to now
                    ),
                    SetOptions.merge()
                )
                .await()

            _currentUser.value = (_currentUser.value ?: User(id = userId)).copy(
                id = userId,
                profile = completedProfile,
                lastActive = now
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) doc.toFypUserOrNull(doc.id) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(userId: String, fields: Map<String, Any>): Result<Unit> {
        return try {
            if (userId.isBlank()) return Result.failure(Exception("userId vazio"))
            usersCollection.document(userId).update(fields).await()
            loadCurrentUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveComplementaryProfile(userId: String, profile: ComplementaryProfile): Result<Unit> {
        return try {
            if (userId.isBlank()) return Result.failure(Exception("userId vazio"))
            usersCollection.document(userId)
                .set(mapOf("complementaryProfile" to profile), SetOptions.merge())
                .await()

            _currentUser.value = _currentUser.value?.copy(complementaryProfile = profile)
            loadCurrentUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearComplementaryProfile(userId: String): Result<Unit> {
        return saveComplementaryProfile(userId, ComplementaryProfile())
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun clearCurrentUser() {
        _currentUser.value = null
    }
}
