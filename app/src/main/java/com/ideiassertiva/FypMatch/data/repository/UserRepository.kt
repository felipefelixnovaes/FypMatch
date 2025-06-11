package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.UserPreferences
import com.ideiassertiva.FypMatch.model.ShowMe
import com.ideiassertiva.FypMatch.model.Gender
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Coleções no Firestore
    private val usersCollection = firestore.collection("users")
    private val profilesCollection = firestore.collection("profiles")
    private val preferencesCollection = firestore.collection("preferences")
    
    /**
     * FIRESTORE - DADOS ESTÁTICOS E ESTRUTURADOS
     * - Perfil do usuário (nome, idade, bio, educação, profissão, etc.)
     * - Preferências de match (idade, distância, orientação, etc.)
     * - Dados de acesso e assinatura
     * - Histórico de ações (matches, likes, etc.)
     */
    
    // === 👤 OPERAÇÕES DE USUÁRIO ===
    
    suspend fun createUserInFirestore(user: User): Result<User> {
        return try {
            val userId = user.id.ifEmpty { auth.currentUser?.uid ?: throw Exception("Usuário não autenticado") }
            val userToSave = user.copy(id = userId, createdAt = Date(), lastActive = Date())
            
            // Salvar dados principais do usuário
            usersCollection.document(userId).set(userToSave).await()
            
            // Salvar perfil separadamente para otimização
            profilesCollection.document(userId).set(userToSave.profile).await()
            
            // Salvar preferências separadamente
            preferencesCollection.document(userId).set(userToSave.preferences).await()
            
            // Analytics
            analyticsManager.logUserSignUp("firestore_creation")
            analyticsManager.setUserId(userId)
            
            Result.success(userToSave)
        } catch (e: Exception) {
            analyticsManager.logError(e, "user_creation_error")
            Result.failure(e)
        }
    }
    
    suspend fun getUserFromFirestore(userId: String): Result<User?> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            analyticsManager.logError(e, "user_fetch_error")
            Result.failure(e)
        }
    }
    
    suspend fun updateUserInFirestore(user: User): Result<User> {
        return try {
            val userId = user.id.ifEmpty { auth.currentUser?.uid ?: throw Exception("Usuário não autenticado") }
            val updatedUser = user.copy(id = userId, lastActive = Date())
            
            // Atualizar dados principais
            usersCollection.document(userId).set(updatedUser).await()
            
            // Atualizar perfil
            profilesCollection.document(userId).set(updatedUser.profile).await()
            
            // Atualizar preferências
            preferencesCollection.document(userId).set(updatedUser.preferences).await()
            
            // Analytics
            analyticsManager.logUserProfile(
                updatedUser.profile.age.takeIf { it > 0 },
                updatedUser.profile.gender.name.takeIf { it != "NOT_SPECIFIED" }
            )
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            analyticsManager.logError(e, "user_update_error")
            Result.failure(e)
        }
    }
    
    // === 👤 OPERAÇÕES DE PERFIL ===
    
    suspend fun updateProfileInFirestore(userId: String, profile: UserProfile): Result<UserProfile> {
        return try {
            val updatedProfile = profile.copy()
            
            // Atualizar perfil no Firestore
            profilesCollection.document(userId).set(updatedProfile).await()
            
            // Atualizar também no documento principal do usuário
            usersCollection.document(userId).update("profile", updatedProfile, "lastActive", Date()).await()
            
            // Analytics
            analyticsManager.logUserProfile(
                profile.age.takeIf { it > 0 },
                profile.gender.name.takeIf { it != "NOT_SPECIFIED" }
            )
            
            Result.success(updatedProfile)
        } catch (e: Exception) {
            analyticsManager.logError(e, "profile_update_error")
            Result.failure(e)
        }
    }
    
    suspend fun getProfileFromFirestore(userId: String): Result<UserProfile?> {
        return try {
            val profileDoc = profilesCollection.document(userId).get().await()
            
            if (profileDoc.exists()) {
                val profile = profileDoc.toObject(UserProfile::class.java)
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            analyticsManager.logError(e, "profile_fetch_error")
            Result.failure(e)
        }
    }
    
    // === ⚙️ OPERAÇÕES DE PREFERÊNCIAS ===
    
    suspend fun updatePreferencesInFirestore(userId: String, preferences: UserPreferences): Result<UserPreferences> {
        return try {
            // Atualizar preferências no Firestore
            preferencesCollection.document(userId).set(preferences).await()
            
            // Atualizar também no documento principal do usuário
            usersCollection.document(userId).update("preferences", preferences, "lastActive", Date()).await()
            
            // Analytics
            analyticsManager.logCustomCrash("user_preferences_updated", mapOf(
                "min_age" to preferences.ageRange.first.toString(),
                "max_age" to preferences.ageRange.last.toString(),
                "max_distance" to preferences.maxDistance.toString(),
                "show_me" to preferences.showMe.name
            ))
            
            Result.success(preferences)
        } catch (e: Exception) {
            analyticsManager.logError(e, "preferences_update_error")
            Result.failure(e)
        }
    }
    
    suspend fun getPreferencesFromFirestore(userId: String): Result<UserPreferences?> {
        return try {
            val preferencesDoc = preferencesCollection.document(userId).get().await()
            
            if (preferencesDoc.exists()) {
                val preferences = preferencesDoc.toObject(UserPreferences::class.java)
                Result.success(preferences)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            analyticsManager.logError(e, "preferences_fetch_error")
            Result.failure(e)
        }
    }
    
    // === 👁️ LISTENERS EM TEMPO REAL ===
    
    fun observeUserInFirestore(userId: String): Flow<User?> = callbackFlow {
        var registration: ListenerRegistration? = null
        
        try {
            registration = usersCollection.document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val user = snapshot?.toObject(User::class.java)
                    trySend(user)
                }
                
            awaitClose { registration?.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }
    
    fun observeProfileInFirestore(userId: String): Flow<UserProfile?> = callbackFlow {
        var registration: ListenerRegistration? = null
        
        try {
            registration = profilesCollection.document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val profile = snapshot?.toObject(UserProfile::class.java)
                    trySend(profile)
                }
                
            awaitClose { registration?.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }
    
    // === 🔍 BUSCA E DESCOBERTA ===
    
    suspend fun searchUsersInFirestore(
        currentUserId: String,
        preferences: UserPreferences,
        limit: Int = 10
    ): Result<List<User>> {
        return try {
            // Buscar usuários baseado nas preferências
            val query = usersCollection
                .whereNotEqualTo("id", currentUserId)
                .whereEqualTo("isActive", true)
                .limit(limit.toLong())
            
            val snapshot = query.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            
            // Filtrar por preferências (idade, distância, etc.)
            val filteredUsers = users.filter { user ->
                val age = user.profile.age
                age in preferences.ageRange.first..preferences.ageRange.last &&
                (preferences.showMe == ShowMe.EVERYONE || 
                 (preferences.showMe == ShowMe.MEN && user.profile.gender == Gender.MALE) ||
                 (preferences.showMe == ShowMe.WOMEN && user.profile.gender == Gender.FEMALE))
            }
            
            // Analytics
            analyticsManager.logCustomCrash("users_search_completed", mapOf(
                "total_found" to users.size.toString(),
                "filtered_count" to filteredUsers.size.toString(),
                "preferences_age_min" to preferences.ageRange.first.toString(),
                "preferences_age_max" to preferences.ageRange.last.toString()
            ))
            
            Result.success(filteredUsers)
        } catch (e: Exception) {
            analyticsManager.logError(e, "users_search_error")
            Result.failure(e)
        }
    }
    
    // === 🗑️ UTILIDADES ===
    
    suspend fun deleteUserFromFirestore(userId: String): Result<Unit> {
        return try {
            // Deletar todos os documentos relacionados
            usersCollection.document(userId).delete().await()
            profilesCollection.document(userId).delete().await()
            preferencesCollection.document(userId).delete().await()
            
            // Analytics
            analyticsManager.logCustomCrash("user_deleted", mapOf("user_id" to userId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "user_deletion_error")
            Result.failure(e)
        }
    }
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
} 