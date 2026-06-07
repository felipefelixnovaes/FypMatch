package com.ideiassertiva.FypMatch.data.repository

import android.content.Context
import com.ideiassertiva.FypMatch.model.*
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.AccessLevel
import com.ideiassertiva.FypMatch.model.BetaFlags
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

@Singleton
class AuthRepository @Inject constructor(@ApplicationContext private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val accessControlRepository = AccessControlRepository()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Observar mudanças no estado de autenticação
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                loadUserData(firebaseUser)
            } else {
                _currentUser.value = null
            }
        }
    }
    
    // Web Client ID (OAuth client_type 3) do projeto Firebase fypmatch-8ac3c.
    // Origem: google-services.json (Firebase Console -> fypmatch-8ac3c).
    private val GOOGLE_WEB_CLIENT_ID = "98859676437-chnsb65d35smaed10idl756aunqmsap2.apps.googleusercontent.com"

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            _isLoading.value = true
            FirebaseCrashlytics.getInstance().log("signInWithGoogle:start")
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                FirebaseCrashlytics.getInstance().setUserId(firebaseUser.uid)
                firebaseUser.email?.let {
                    FirebaseCrashlytics.getInstance().setCustomKey("auth_email", it)
                }
                val user = createOrUpdateUser(firebaseUser)
                _currentUser.value = user
                FirebaseCrashlytics.getInstance().log("signInWithGoogle:success")
                Result.success(user)
            } else {
                Result.failure(Exception("Falha na autenticação"))
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun createOrUpdateUser(firebaseUser: FirebaseUser): User {
        val userDoc = firestore.collection("users").document(firebaseUser.uid)

        val email = firebaseUser.email ?: ""
        val displayName = firebaseUser.displayName ?: ""
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        val now = Date()
        val (accessLevel, betaFlags) = accessControlRepository.getSpecialAccessConfig(email)

        val authFields = mapOf(
            "id" to firebaseUser.uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "lastActive" to now,
            "accessLevel" to accessLevel,
            "betaFlags" to betaFlags
        )

        // Primeiro grava/mescla dados mínimos do Auth. Assim o login não depende
        // de uma leitura prévia do documento, que é justamente onde rules antigas
        // costumam retornar PERMISSION_DENIED.
        userDoc.set(authFields, SetOptions.merge()).await()

        return try {
            val userSnapshot = userDoc.get().await()
            userSnapshot.toFypUserOrNull(firebaseUser.uid) ?: User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                accessLevel = accessLevel,
                betaFlags = betaFlags,
                createdAt = now,
                lastActive = now
            )
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            // Se a rule publicada ainda bloqueia leitura mas permitiu o merge,
            // mantemos o usuário autenticado com o estado mínimo local.
            User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                accessLevel = accessLevel,
                betaFlags = betaFlags,
                createdAt = now,
                lastActive = now
            )
        }
    }
    
    private fun loadUserData(firebaseUser: FirebaseUser) {
        firestore.collection("users").document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    FirebaseCrashlytics.getInstance().recordException(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toFypUserOrNull(firebaseUser.uid) ?: User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    user.let {
                        FirebaseCrashlytics.getInstance().setUserId(it.id.ifBlank { firebaseUser.uid })
                        if (it.email.isNotBlank()) {
                            FirebaseCrashlytics.getInstance().setCustomKey("auth_email", it.email)
                        }
                    }
                    _currentUser.value = user
                }
            }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().await()
            FirebaseCrashlytics.getInstance().setUserId("")
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
} 
