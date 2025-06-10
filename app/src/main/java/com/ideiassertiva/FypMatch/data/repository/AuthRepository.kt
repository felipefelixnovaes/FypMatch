package com.ideiassertiva.FypMatch.data.repository

import android.content.Context
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.AccessLevel
import com.ideiassertiva.FypMatch.model.BetaFlags
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.Gender
import com.ideiassertiva.FypMatch.model.Location
import com.ideiassertiva.FypMatch.model.isProfileComplete
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsManager: AnalyticsManager
) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val accessControlRepository = AccessControlRepository()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.None)
    val navigationState: Flow<NavigationState> = _navigationState.asStateFlow()
    
    init {
        // Observar mudanças no estado de autenticação
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                loadUserData(firebaseUser)
            } else {
                _currentUser.value = null
                _navigationState.value = NavigationState.None
            }
        }
    }
    
    fun getGoogleSignInClient(): GoogleSignInClient {
        // Web Client ID correto do Firebase Console - Projeto fypmatch-8ac3c
        val webClientId = "98859676437-v8u5n4dqnk2bjqvaqh4bvt0uqitj7n5f.apps.googleusercontent.com"
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(webClientId) // Adiciona server auth code
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            _isLoading.value = true
            
            // Analytics: Login iniciado
            analyticsManager.logUserLogin("google")
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                try {
                    val user = createOrUpdateUserWithGoogleData(firebaseUser, account)
                    _currentUser.value = user
                    
                    // Analytics: Login bem-sucedido
                    analyticsManager.setUserId(user.id)
                    analyticsManager.setUserProperties(mapOf(
                        "login_method" to "google",
                        "has_google_photo" to (account.photoUrl != null).toString(),
                        "google_email_domain" to (account.email?.substringAfter("@") ?: "unknown")
                    ))
                    
                    // Determinar navegação
                    _navigationState.value = if (user.isProfileComplete()) {
                        NavigationState.ToDiscovery
                    } else {
                        NavigationState.ToProfile
                    }
                    
                    Result.success(user)
                } catch (firestoreError: Exception) {
                    // Se Firestore falhar, criar usuário mínimo
                    val minimalUser = createMinimalUserFromGoogle(firebaseUser, account)
                    _currentUser.value = minimalUser
                    _navigationState.value = NavigationState.ToProfile
                    
                    // Analytics: Fallback login
                    analyticsManager.logError(firestoreError, "google_login_firestore_fallback")
                    
                    Result.success(minimalUser)
                }
            } else {
                analyticsManager.logError(Exception("Firebase auth failed"), "google_login_firebase_null")
                Result.failure(Exception("Falha na autenticação"))
            }
        } catch (e: Exception) {
            analyticsManager.logError(e, "google_login_general_error")
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun createOrUpdateUserWithGoogleData(
        firebaseUser: FirebaseUser, 
        googleAccount: GoogleSignInAccount
    ): User {
        val userDoc = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = userDoc.get().await()
        
        return if (snapshot.exists()) {
            // Usuário existente - atualizar com novos dados do Google se necessário
            val existingUser = snapshot.toObject(User::class.java) ?: 
                throw Exception("Erro ao carregar dados do usuário")
            
            val updatedUser = existingUser.copy(
                email = firebaseUser.email ?: existingUser.email,
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                lastActive = Date(),
                profile = existingUser.profile.copy(
                    // Atualizar nome se vazio ou se mudou no Google
                    fullName = if (existingUser.profile.fullName.isBlank()) {
                        firebaseUser.displayName ?: ""
                    } else existingUser.profile.fullName
                )
            )
            
            try {
                userDoc.set(updatedUser).await()
            } catch (e: Exception) {
                // Continuar mesmo se não conseguir salvar
            }
            
            updatedUser
        } else {
            // Novo usuário - criar com dados ricos do Google
            val email = firebaseUser.email ?: ""
            val (accessLevel, betaFlags) = accessControlRepository.getSpecialAccessConfig(email)
            
            val newUser = User(
                id = firebaseUser.uid,
                email = email,
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                profile = UserProfile(
                    fullName = firebaseUser.displayName ?: "",
                    // Tentar inferir gênero pelo nome (opcional)
                    gender = Gender.NOT_SPECIFIED,
                    photos = if (firebaseUser.photoUrl != null) {
                        listOf(firebaseUser.photoUrl.toString())
                    } else emptyList(),
                    location = Location(
                        country = "Brasil" // Padrão para o mercado brasileiro
                    ),
                    isProfileComplete = false // Sempre false para novos usuários
                ),
                accessLevel = accessLevel,
                betaFlags = betaFlags,
                createdAt = Date(),
                lastActive = Date()
            )
            
            try {
                userDoc.set(newUser).await()
                
                // Analytics: Novo usuário
                analyticsManager.logUserSignUp("google")
                analyticsManager.logUserProfile(null, null) // Será atualizado depois
            } catch (e: Exception) {
                // Continuar mesmo se não conseguir salvar
            }
            
            newUser
        }
    }
    
    private fun createMinimalUserFromGoogle(
        firebaseUser: FirebaseUser,
        googleAccount: GoogleSignInAccount
    ): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            profile = UserProfile(
                fullName = firebaseUser.displayName ?: "",
                photos = if (firebaseUser.photoUrl != null) {
                    listOf(firebaseUser.photoUrl.toString())
                } else emptyList(),
                isProfileComplete = false
            )
        )
    }
    
    private fun loadUserData(firebaseUser: FirebaseUser) {
        firestore.collection("users").document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    _currentUser.value = user
                    
                    // Determinar navegação se usuário mudou
                    user?.let {
                        _navigationState.value = if (it.isProfileComplete()) {
                            NavigationState.ToDiscovery
                        } else {
                            NavigationState.ToProfile
                        }
                    }
                }
            }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().await()
            _currentUser.value = null
            _navigationState.value = NavigationState.None
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun clearNavigationState() {
        _navigationState.value = NavigationState.None
    }
    
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}

sealed class NavigationState {
    object None : NavigationState()
    object ToProfile : NavigationState()
    object ToDiscovery : NavigationState()
} 
