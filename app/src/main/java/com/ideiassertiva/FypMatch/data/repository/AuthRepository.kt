package com.ideiassertiva.FypMatch.data.repository

import android.content.Context
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class AuthRepository(private val context: Context) {
    
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
    
    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("123456789-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com") // Web client ID do Firebase
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            _isLoading.value = true
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                val user = createOrUpdateUser(firebaseUser)
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Falha na autenticação"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun createOrUpdateUser(firebaseUser: FirebaseUser): User {
        val userDoc = firestore.collection("users").document(firebaseUser.uid)
        val userSnapshot = userDoc.get().await()
        
        return if (userSnapshot.exists()) {
            // Usuário já existe, atualizar última atividade
            val existingUser = userSnapshot.toObject(User::class.java) ?: User()
            val updatedUser = existingUser.copy(
                lastActive = Date(),
                email = firebaseUser.email ?: existingUser.email,
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl
            )
            
            userDoc.set(updatedUser).await()
            updatedUser
        } else {
            // Novo usuário - verificar acesso especial baseado no email
            val email = firebaseUser.email ?: ""
            val (accessLevel, betaFlags) = accessControlRepository.getSpecialAccessConfig(email)
            
            val newUser = User(
                id = firebaseUser.uid,
                email = email,
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                accessLevel = accessLevel,
                betaFlags = betaFlags,
                createdAt = Date(),
                lastActive = Date()
            )
            
            userDoc.set(newUser).await()
            newUser
        }
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
                }
            }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut().await()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
} 
