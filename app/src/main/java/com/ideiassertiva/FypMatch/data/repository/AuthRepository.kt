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
        // Web Client ID correto do Firebase Console - Projeto fypmatch-8ac3c
        // Este é o Client ID web OAuth 2.0 correto para o projeto
        val webClientId = "98859676437-v8u5n4dqnk2bjqvaqh4bvt0uqitj7n5f.apps.googleusercontent.com"
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
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
                try {
                    val user = createOrUpdateUser(firebaseUser)
                    _currentUser.value = user
                    Result.success(user)
                } catch (firestoreError: Exception) {
                    // Se Firestore falhar, criar usuário mínimo para permitir login
                    val minimalUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    _currentUser.value = minimalUser
                    Result.success(minimalUser)
                }
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
        
        // Forçar busca do servidor para evitar erro offline
        val userSnapshot = try {
            userDoc.get(com.google.firebase.firestore.Source.SERVER).await()
        } catch (serverError: Exception) {
            try {
                // Se falhar do servidor, tentar cache local
                userDoc.get(com.google.firebase.firestore.Source.CACHE).await()
            } catch (cacheError: Exception) {
                // Se ambos falharem, assumir que é novo usuário
                null
            }
        }
        
        return if (userSnapshot != null && userSnapshot.exists()) {
            // Usuário já existe, atualizar última atividade
            val existingUser = userSnapshot.toObject(User::class.java) ?: User()
            val updatedUser = existingUser.copy(
                lastActive = Date(),
                email = firebaseUser.email ?: existingUser.email,
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl
            )
            
            try {
                userDoc.set(updatedUser).await()
            } catch (e: Exception) {
                // Ignorar erro de escrita e continuar com dados atualizados
            }
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
            
            try {
                userDoc.set(newUser).await()
            } catch (e: Exception) {
                // Ignorar erro de escrita e continuar com novo usuário
            }
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
