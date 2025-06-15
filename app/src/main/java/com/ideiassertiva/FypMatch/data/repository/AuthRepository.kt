package com.ideiassertiva.FypMatch.data.repository

import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
// Imports para fallback do popup interativo quando Credential Manager falha
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
// Imports das APIs antigas do Google Sign-In removidos - usando apenas Credential Manager
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.AccessLevel
import com.ideiassertiva.FypMatch.model.BetaFlags
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.Gender
import com.ideiassertiva.FypMatch.model.Location
import com.ideiassertiva.FypMatch.model.Orientation
import com.ideiassertiva.FypMatch.model.Intention
import com.ideiassertiva.FypMatch.model.RelationshipStatus
import com.ideiassertiva.FypMatch.model.ChildrenStatus
import com.ideiassertiva.FypMatch.model.SmokingStatus
import com.ideiassertiva.FypMatch.model.DrinkingStatus
import com.ideiassertiva.FypMatch.model.ZodiacSign
import com.ideiassertiva.FypMatch.model.Religion
import com.ideiassertiva.FypMatch.model.PetPreference
import com.ideiassertiva.FypMatch.model.isProfileComplete
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import android.os.Build
import android.app.Activity
import com.ideiassertiva.FypMatch.BuildConfig

// Enum para tipos de login
enum class LoginType {
    GOOGLE,
    EMAIL,
    PHONE
}

// Data class para credenciais de email
data class EmailCredentials(
    val email: String,
    val password: String
)

// Data class para credenciais de telefone
data class PhoneCredentials(
    val phoneNumber: String,
    val verificationCode: String
)

// Estados para verificação de email
sealed class EmailVerificationState {
    object Idle : EmailVerificationState()
    object SendingCode : EmailVerificationState()
    object CodeSent : EmailVerificationState()
    object Verified : EmailVerificationState()
    data class Error(val message: String) : EmailVerificationState()
}

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val accessControlRepository: AccessControlRepository
) {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    
    // Google Sign-In Client (fallback para quando Credential Manager falha)
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("98859676437-chnsb65d35smaed10idl756aunqmsap2.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        
        println("🔍 DEBUG - Configurando GoogleSignInClient com Web Client ID")
        GoogleSignIn.getClient(context, gso)
    }
    
    // Estados observáveis
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.None)
    val navigationState: Flow<NavigationState> = _navigationState.asStateFlow()
    
    private val _needsInteractiveSignIn = MutableStateFlow(false)
    val needsInteractiveSignIn: Flow<Boolean> = _needsInteractiveSignIn.asStateFlow()
    
    // Estado para verificação de telefone
    private val _phoneVerificationState = MutableStateFlow<PhoneVerificationState>(PhoneVerificationState.Idle)
    val phoneVerificationState: Flow<PhoneVerificationState> = _phoneVerificationState.asStateFlow()
    
    // Estado para verificação de email
    private val _emailVerificationState = MutableStateFlow<EmailVerificationState>(EmailVerificationState.Idle)
    val emailVerificationState: Flow<EmailVerificationState> = _emailVerificationState.asStateFlow()
    
    // Variáveis para verificação de telefone
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    // Variáveis para verificação de email
    private var pendingEmailUser: FirebaseUser? = null
    private var pendingUserData: User? = null
    
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
    
    // === MÉTODOS DE LOGIN COM GOOGLE ===
    
    // Método principal de login com Google - CREDENTIAL MANAGER (Nova API)
    suspend fun signInWithGoogle(): Result<User> {
        return try {
            _isLoading.value = true
            _needsInteractiveSignIn.value = false
            
            println("🔍 DEBUG - Iniciando login com Google via Credential Manager")
            
            // Usar Credential Manager (nova API recomendada)
            val credentialResult = tryCredentialManagerSignIn()
            
            credentialResult.fold(
                onSuccess = { user ->
                    println("🔍 DEBUG - Login com Credential Manager bem-sucedido: ${user.email}")
                    _isLoading.value = false
                    Result.success(user)
                },
                onFailure = { error ->
                    println("🔍 DEBUG - Credential Manager falhou: ${error.message}")
                    
                    when {
                        error.message?.contains("cancelado", ignoreCase = true) == true -> {
                            _isLoading.value = false
                            Result.failure(Exception("Login cancelado pelo usuário"))
                        }
                        error.message == "FALLBACK_REQUIRED" -> {
                            println("🔍 DEBUG - Tentando fallback com popup interativo")
                            _needsInteractiveSignIn.value = true
                            _isLoading.value = false
                            Result.failure(GoogleSignInInteractiveRequiredException("INTERACTIVE_SIGNIN_REQUIRED"))
                        }
                        else -> {
                            _isLoading.value = false
                            Result.failure(error)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro geral no signInWithGoogle: ${e.message}")
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    // === MÉTODOS DE LOGIN COM EMAIL ===
    
    // Login com email e senha
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            _isLoading.value = true
            
            println("🔍 DEBUG - Iniciando login com email: $email")
            
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Verificar se o email foi verificado
                if (!firebaseUser.isEmailVerified) {
                    _isLoading.value = false
                    return Result.failure(Exception("Email não verificado. Verifique sua caixa de entrada e clique no link de verificação."))
                }
                
                val user = processSignInResult(firebaseUser, LoginType.EMAIL)
                
                analyticsManager.logCustomCrash("email_signin_success", mapOf(
                    "user_id" to user.id,
                    "email" to email
                ))
                
                _isLoading.value = false
                Result.success(user)
            } else {
                _isLoading.value = false
                Result.failure(Exception("Falha na autenticação com email"))
            }
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro no login com email: ${e.message}")
            _isLoading.value = false
            
            analyticsManager.logError(e, "email_signin_failure")
            
            val errorMessage = when {
                e.message?.contains("password is invalid", ignoreCase = true) == true -> 
                    "Senha incorreta"
                e.message?.contains("no user record", ignoreCase = true) == true -> 
                    "Email não encontrado"
                e.message?.contains("badly formatted", ignoreCase = true) == true -> 
                    "Email inválido"
                e.message?.contains("too many requests", ignoreCase = true) == true -> 
                    "Muitas tentativas. Tente novamente mais tarde"
                else -> "Erro no login: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Criar conta com email e senha (com verificação)
    suspend fun signUpWithEmail(email: String, password: String): Result<String> {
        return try {
            _isLoading.value = true
            _emailVerificationState.value = EmailVerificationState.SendingCode
            
            println("🔍 DEBUG - Criando conta com email: $email")
            
            // Verificar se já existe usuário com este email
            val duplicateCheck = checkForDuplicateUser(email, null)
            duplicateCheck.getOrNull()?.let { existingUser ->
                _isLoading.value = false
                _emailVerificationState.value = EmailVerificationState.Error("Email já está em uso")
                return Result.failure(Exception("Já existe uma conta com este email: ${existingUser.email}"))
            }
            
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Criar dados do usuário mas não salvar ainda
                val userId = generateUserId(firebaseUser, LoginType.EMAIL)
                val newUser = createNewUser(userId, email, firebaseUser.displayName ?: "", "")
                
                // Armazenar dados temporariamente
                pendingEmailUser = firebaseUser
                pendingUserData = newUser
                
                // Enviar email de verificação
                firebaseUser.sendEmailVerification().await()
                
                analyticsManager.logCustomCrash("email_signup_verification_sent", mapOf(
                    "user_id" to newUser.id,
                    "email" to email
                ))
                
                _emailVerificationState.value = EmailVerificationState.CodeSent
                _isLoading.value = false
                
                Result.success("Email de verificação enviado para $email. Verifique sua caixa de entrada.")
            } else {
                _isLoading.value = false
                _emailVerificationState.value = EmailVerificationState.Error("Falha na criação da conta")
                Result.failure(Exception("Falha na criação da conta"))
            }
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro na criação da conta: ${e.message}")
            _isLoading.value = false
            _emailVerificationState.value = EmailVerificationState.Error(e.message ?: "Erro na criação da conta")
            
            analyticsManager.logError(e, "email_signup_failure")
            
            val errorMessage = when {
                e.message?.contains("email address is already in use", ignoreCase = true) == true -> 
                    "Este email já está em uso"
                e.message?.contains("password is too weak", ignoreCase = true) == true -> 
                    "Senha muito fraca. Use pelo menos 6 caracteres"
                e.message?.contains("badly formatted", ignoreCase = true) == true -> 
                    "Email inválido"
                e.message?.contains("network error", ignoreCase = true) == true -> 
                    "Erro de conexão. Verifique sua internet"
                else -> "Erro na criação da conta: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Verificar se o email foi confirmado
    suspend fun checkEmailVerification(): Result<User> {
        return try {
            _isLoading.value = true
            
            val firebaseUser = pendingEmailUser ?: auth.currentUser
            if (firebaseUser == null) {
                _isLoading.value = false
                return Result.failure(Exception("Usuário não encontrado"))
            }
            
            // Recarregar dados do usuário para verificar status
            firebaseUser.reload().await()
            
            if (firebaseUser.isEmailVerified) {
                // Email verificado - criar usuário no Firestore
                val userData = pendingUserData ?: createNewUser(
                    generateUserId(firebaseUser, LoginType.EMAIL),
                    firebaseUser.email ?: "",
                    firebaseUser.displayName ?: "",
                    ""
                )
                
                val savedUser = userRepository.createUserInFirestore(userData).getOrThrow()
                
                // Limpar dados temporários
                pendingEmailUser = null
                pendingUserData = null
                
                _currentUser.value = savedUser
                _emailVerificationState.value = EmailVerificationState.Verified
                _isLoading.value = false
                
                analyticsManager.logCustomCrash("email_signup_success", mapOf(
                    "user_id" to savedUser.id,
                    "email" to savedUser.email
                ))
                
                Result.success(savedUser)
            } else {
                _isLoading.value = false
                Result.failure(Exception("Email ainda não foi verificado. Verifique sua caixa de entrada."))
            }
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro na verificação do email: ${e.message}")
            _isLoading.value = false
            _emailVerificationState.value = EmailVerificationState.Error("Erro na verificação")
            
            analyticsManager.logError(e, "email_verification_check_failed")
            Result.failure(Exception("Erro ao verificar email: ${e.message}"))
        }
    }
    
    // Reenviar email de verificação
    suspend fun resendEmailVerification(): Result<String> {
        return try {
            _isLoading.value = true
            
            val firebaseUser = pendingEmailUser ?: auth.currentUser
            if (firebaseUser == null) {
                _isLoading.value = false
                return Result.failure(Exception("Usuário não encontrado"))
            }
            
            firebaseUser.sendEmailVerification().await()
            
            _isLoading.value = false
            
            analyticsManager.logCustomCrash("email_verification_resent", mapOf(
                "email" to (firebaseUser.email ?: "unknown")
            ))
            
            Result.success("Email de verificação reenviado para ${firebaseUser.email}")
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao reenviar email: ${e.message}")
            _isLoading.value = false
            
            analyticsManager.logError(e, "email_verification_resend_failed")
            
            val errorMessage = when {
                e.message?.contains("too many requests", ignoreCase = true) == true -> 
                    "Muitas tentativas. Aguarde alguns minutos antes de tentar novamente"
                e.message?.contains("network error", ignoreCase = true) == true -> 
                    "Erro de conexão. Verifique sua internet"
                else -> "Erro ao reenviar email: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Limpar estado de verificação de email
    fun clearEmailVerificationState() {
        _emailVerificationState.value = EmailVerificationState.Idle
        pendingEmailUser = null
        pendingUserData = null
    }
    
    // === MÉTODOS DE LOGIN COM TELEFONE ===
    
    // Iniciar verificação de telefone
    suspend fun startPhoneVerification(phoneNumber: String, activity: Activity): Result<String> {
        return try {
            _isLoading.value = true
            _phoneVerificationState.value = PhoneVerificationState.SendingCode
            
            println("🔍 DEBUG - Iniciando verificação de telefone: $phoneNumber")
            
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        println("🔍 DEBUG - Verificação automática completada")
                        _phoneVerificationState.value = PhoneVerificationState.AutoVerified(credential)
                        _isLoading.value = false
                    }
                    
                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                        println("🔍 DEBUG - Falha na verificação: ${e.message}")
                        _phoneVerificationState.value = PhoneVerificationState.Error(e.message ?: "Erro na verificação")
                        _isLoading.value = false
                        
                        analyticsManager.logError(e, "phone_verification_failed")
                    }
                    
                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        println("🔍 DEBUG - Código enviado. ID: $verificationId")
                        storedVerificationId = verificationId
                        resendToken = token
                        _phoneVerificationState.value = PhoneVerificationState.CodeSent
                        _isLoading.value = false
                    }
                })
            .build()
        
            PhoneAuthProvider.verifyPhoneNumber(options)
            
            Result.success("Código de verificação enviado")
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao iniciar verificação: ${e.message}")
            _isLoading.value = false
            _phoneVerificationState.value = PhoneVerificationState.Error(e.message ?: "Erro desconhecido")
            
            analyticsManager.logError(e, "phone_verification_start_failed")
            Result.failure(e)
        }
    }
    
    // Verificar código de telefone
    suspend fun verifyPhoneCode(code: String): Result<User> {
        return try {
            _isLoading.value = true
            
            val verificationId = storedVerificationId 
                ?: return Result.failure(Exception("ID de verificação não encontrado"))
            
            println("🔍 DEBUG - Verificando código: $code")
            
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                val user = processSignInResult(firebaseUser, LoginType.PHONE)
                
                analyticsManager.logCustomCrash("phone_signin_success", mapOf(
                    "user_id" to user.id,
                    "phone" to (firebaseUser.phoneNumber ?: "unknown")
                ))
                
                _phoneVerificationState.value = PhoneVerificationState.Verified
                _isLoading.value = false
                Result.success(user)
            } else {
                _isLoading.value = false
                Result.failure(Exception("Falha na autenticação"))
            }
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro na verificação do código: ${e.message}")
            _isLoading.value = false
            _phoneVerificationState.value = PhoneVerificationState.Error("Código inválido")
            
            analyticsManager.logError(e, "phone_code_verification_failed")
            
            val errorMessage = when {
                e.message?.contains("invalid verification code", ignoreCase = true) == true -> 
                    "Código de verificação inválido"
                e.message?.contains("expired", ignoreCase = true) == true -> 
                    "Código expirado. Solicite um novo código"
                else -> "Erro na verificação: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Reenviar código de verificação
    suspend fun resendPhoneCode(phoneNumber: String, activity: Activity): Result<String> {
        return try {
            _isLoading.value = true
            
            val token = resendToken 
                ?: return startPhoneVerification(phoneNumber, activity)
            
            println("🔍 DEBUG - Reenviando código para: $phoneNumber")
            
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setForceResendingToken(token)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        _phoneVerificationState.value = PhoneVerificationState.AutoVerified(credential)
                        _isLoading.value = false
                    }
                    
                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                        _phoneVerificationState.value = PhoneVerificationState.Error(e.message ?: "Erro no reenvio")
                        _isLoading.value = false
                    }
                    
                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        storedVerificationId = verificationId
                        resendToken = token
                        _phoneVerificationState.value = PhoneVerificationState.CodeSent
                        _isLoading.value = false
                    }
                })
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
            
            Result.success("Código reenviado")
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao reenviar código: ${e.message}")
            _isLoading.value = false
            _phoneVerificationState.value = PhoneVerificationState.Error(e.message ?: "Erro no reenvio")
            
            analyticsManager.logError(e, "phone_code_resend_failed")
            Result.failure(e)
        }
    }
    
    // === MÉTODOS AUXILIARES ===
    
    // Processar resultado de login e criar/atualizar usuário
    private suspend fun processSignInResult(firebaseUser: FirebaseUser, loginType: LoginType): User {
        val email = firebaseUser.email ?: ""
        val phoneNumber = firebaseUser.phoneNumber ?: ""
        val displayName = firebaseUser.displayName ?: ""
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ""
        
        println("🔍 DEBUG - Processando login para: email=$email, phone=$phoneNumber, loginType=$loginType")
        
        // PRIORIDADE 1: Buscar por email/telefone usando método direto (mais eficiente)
        val directSearchResult = userRepository.findUserByEmailOrPhone(
            email.takeIf { it.isNotBlank() },
            phoneNumber.takeIf { it.isNotBlank() }
        )
        directSearchResult.getOrNull()?.let { existingUser ->
            println("🔍 DEBUG - ✅ USUÁRIO EXISTENTE ENCONTRADO (busca direta): ${existingUser.id}")
            println("🔍 DEBUG - Email: ${existingUser.email}, Perfil completo: ${existingUser.isProfileComplete()}")
            
            // Atualizar dados do usuário existente com informações mais recentes
            val updatedUser = existingUser.copy(
                email = email.ifEmpty { existingUser.email },
                displayName = displayName.ifEmpty { existingUser.displayName },
                photoUrl = photoUrl.ifEmpty { existingUser.photoUrl },
                lastActive = Date()
            )
            
            userRepository.updateUserInFirestore(updatedUser).getOrThrow()
            _currentUser.value = updatedUser
            
            // Perfil sempre completo com dados fictícios - ir direto para Discovery
            _navigationState.value = NavigationState.ToDiscovery
            println("🔍 DEBUG - Navegando diretamente para Discovery (perfil completo)")
            
            return updatedUser
        }
        
        // PRIORIDADE 2: Buscar por padrão de email/telefone (para IDs antigos)
        val patternSearchResult = checkForDuplicateUser(
            email.takeIf { it.isNotBlank() },
            phoneNumber.takeIf { it.isNotBlank() }
        )
        patternSearchResult.getOrNull()?.let { existingUser ->
            println("🔍 DEBUG - ✅ USUÁRIO EXISTENTE ENCONTRADO (busca por padrão): ${existingUser.id}")
            
            // Atualizar usuário existente
            val updatedUser = existingUser.copy(
                email = email.ifEmpty { existingUser.email },
                displayName = displayName.ifEmpty { existingUser.displayName },
                photoUrl = photoUrl.ifEmpty { existingUser.photoUrl },
                lastActive = Date()
            )
            
            userRepository.updateUserInFirestore(updatedUser).getOrThrow()
            _currentUser.value = updatedUser
            
            // Perfil sempre completo com dados fictícios - ir direto para Discovery
            _navigationState.value = NavigationState.ToDiscovery
            
            return updatedUser
        }
        
        // PRIORIDADE 3: Gerar novo userId e verificar se existe
        val userId = generateUserId(firebaseUser, loginType)
        println("🔍 DEBUG - Gerando novo userId: $userId")
        
        val existingUserResult = userRepository.getUserFromFirestore(userId)
        
        return existingUserResult.fold(
            onSuccess = { existingUser ->
                if (existingUser != null) {
                    println("🔍 DEBUG - ✅ USUÁRIO EXISTENTE ENCONTRADO (por ID): ${existingUser.id}")
                    
                    // Atualizar informações do usuário existente
                    val updatedUser = existingUser.copy(
                        email = email.ifEmpty { existingUser.email },
                        displayName = displayName.ifEmpty { existingUser.displayName },
                        photoUrl = photoUrl.ifEmpty { existingUser.photoUrl },
                        lastActive = Date()
                    )
                    
                    userRepository.updateUserInFirestore(updatedUser).getOrThrow()
                    _currentUser.value = updatedUser
                    
                    // Perfil sempre completo com dados fictícios - ir direto para Discovery
                    _navigationState.value = NavigationState.ToDiscovery
                    
                    updatedUser
                } else {
                    println("🔍 DEBUG - ❌ NENHUM USUÁRIO ENCONTRADO - Criando novo usuário")
                    
                    // Criar novo usuário
                    val newUser = createNewUser(userId, email, displayName, photoUrl)
                    userRepository.createUserInFirestore(newUser).getOrThrow()
                    _currentUser.value = newUser
                    
                    // Novo usuário com perfil completo vai direto para Discovery
                    _navigationState.value = NavigationState.ToDiscovery
                    
                    newUser
                }
            },
            onFailure = { error ->
                println("🔍 DEBUG - ❌ ERRO ao buscar usuário: ${error.message} - Criando novo")
                
                // Erro ao buscar usuário, criar novo
                val newUser = createNewUser(userId, email, displayName, photoUrl)
                userRepository.createUserInFirestore(newUser).getOrThrow()
                _currentUser.value = newUser
                
                // Novo usuário com perfil completo vai direto para Discovery
                _navigationState.value = NavigationState.ToDiscovery
                
                newUser
            }
        )
    }
    
    // Criar novo usuário com dados básicos e fotos aleatórias
    private suspend fun createNewUser(userId: String, email: String, displayName: String, photoUrl: String): User {
        val (accessLevel, betaFlags) = accessControlRepository.getSpecialAccessConfig(email)
        
        // Gerar fotos aleatórias para teste
        val randomPhotos = generateRandomPhotos()
        
        // Gerar dados fictícios realistas
        val fakeData = generateFakeUserData(displayName, email)
        
        return User(
            id = userId,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl.ifEmpty { randomPhotos.firstOrNull() ?: "" },
            profile = UserProfile(
                fullName = displayName.ifEmpty { fakeData.name },
                age = fakeData.age,
                bio = fakeData.bio,
                aboutMe = fakeData.aboutMe,
                photos = if (photoUrl.isNotEmpty()) {
                    listOf(photoUrl) + randomPhotos.take(2)
                } else {
                    randomPhotos
                },
                location = Location(
                    city = fakeData.city,
                    state = fakeData.state,
                    country = "Brasil",
                    latitude = fakeData.latitude,
                    longitude = fakeData.longitude
                ),
                gender = fakeData.gender,
                orientation = fakeData.orientation,
                intention = fakeData.intention,
                interests = fakeData.interests,
                education = fakeData.education,
                profession = fakeData.profession,
                height = fakeData.height,
                relationshipStatus = fakeData.relationshipStatus,
                hasChildren = fakeData.hasChildren,
                wantsChildren = fakeData.wantsChildren,
                smokingStatus = fakeData.smokingStatus,
                drinkingStatus = fakeData.drinkingStatus,
                zodiacSign = fakeData.zodiacSign,
                religion = fakeData.religion,
                favoriteMovies = fakeData.favoriteMovies,
                favoriteGenres = fakeData.favoriteGenres,
                favoriteBooks = fakeData.favoriteBooks,
                favoriteMusic = fakeData.favoriteMusic,
                hobbies = fakeData.hobbies,
                sports = fakeData.sports,
                favoriteTeam = fakeData.favoriteTeam,
                languages = fakeData.languages,
                petPreference = fakeData.petPreference,
                isProfileComplete = true // Perfil já completo com dados fictícios
            ),
            accessLevel = AccessLevel.FULL_ACCESS, // Acesso completo para todos
            betaFlags = BetaFlags(
                hasEarlyAccess = true,
                canAccessSwipe = true,
                canAccessChat = true,
                canAccessPremium = true,
                canAccessAI = true,
                isTestUser = false
            ),
            createdAt = Date(),
            lastActive = Date()
        )
    }
    
    // Gerar fotos aleatórias do Picsum
    private fun generateRandomPhotos(): List<String> {
        val photoIds = (1..1000).shuffled().take(3)
        return photoIds.map { id ->
            "https://picsum.photos/400/400?random=$id"
        }
    }
    
    // Gerar dados fictícios realistas para usuários
    private fun generateFakeUserData(displayName: String, email: String): FakeUserData {
        val names = listOf(
            "Ana Clara Santos", "Bruno Costa", "Carlos Eduardo Silva", "Daniela Ferreira",
            "Emerson Silva", "Fernanda Lopes", "Gabriel Oliveira", "Helena Rodrigues",
            "Igor Pereira", "Juliana Almeida", "Kaique Souza", "Larissa Martins",
            "Mateus Lima", "Natália Barbosa", "Otávio Carvalho", "Priscila Dias",
            "Rafael Moreira", "Sabrina Gomes", "Thiago Nascimento", "Vanessa Ribeiro"
        )
        
        val cities = listOf(
            Triple("São Paulo", "SP", Pair(-23.5505, -46.6333)),
            Triple("Rio de Janeiro", "RJ", Pair(-22.9068, -43.1729)),
            Triple("Belo Horizonte", "MG", Pair(-19.9191, -43.9386)),
            Triple("Porto Alegre", "RS", Pair(-30.0346, -51.2177)),
            Triple("Recife", "PE", Pair(-8.0476, -34.8770)),
            Triple("Fortaleza", "CE", Pair(-3.7172, -38.5433)),
            Triple("Salvador", "BA", Pair(-12.9714, -38.5014)),
            Triple("Brasília", "DF", Pair(-15.8267, -47.9218)),
            Triple("Curitiba", "PR", Pair(-25.4284, -49.2733)),
            Triple("Manaus", "AM", Pair(-3.1190, -60.0217))
        )
        
        val bios = listOf(
            "Apaixonado(a) por aventuras e boas conversas ✨",
            "Amante de café, livros e filmes de terror 📚☕",
            "Desenvolvedor(a) de dia, músico(a) de noite 🎵",
            "Viajante, foodie e sempre em busca de novas experiências 🌎",
            "Professor(a) apaixonado(a) por ensinar e aprender 📖",
            "Designer criativo(a) que adora arte e natureza 🎨🌿",
            "Médico(a) dedicado(a) que ama ajudar pessoas ❤️",
            "Engenheiro(a) que constrói o futuro 🏗️",
            "Psicólogo(a) que acredita no poder da mente 🧠",
            "Advogado(a) lutando por justiça ⚖️"
        )
        
        val aboutMeTexts = listOf(
            "Sou uma pessoa autêntica que valoriza conexões verdadeiras. Adoro descobrir lugares novos, experimentar culinárias diferentes e ter conversas profundas sobre a vida. Procuro alguém que compartilhe da minha paixão por aventuras e que saiba apreciar os pequenos momentos.",
            "Trabalho com o que amo e isso me realiza muito. Nas horas livres gosto de praticar esportes, assistir séries e passar tempo com amigos. Busco alguém com senso de humor, que seja carinhoso(a) e que queira construir algo especial juntos.",
            "Sou apaixonado(a) por música, arte e tudo que envolve criatividade. Adoro festivais, shows e descobrir artistas novos. Procuro alguém que curta a vida noturna mas que também saiba apreciar um domingo tranquilo em casa.",
            "Amo a natureza e sempre que posso estou fazendo trilhas ou na praia. Sou uma pessoa tranquila, mas que gosta de se divertir. Busco alguém que compartilhe do meu amor pela natureza e que queira explorar o mundo comigo.",
            "Sou uma pessoa família, que valoriza relacionamentos duradouros. Gosto de cozinhar, assistir filmes e ter conversas interessantes. Procuro alguém maduro(a), que saiba se comunicar e que queira crescer junto comigo."
        )
        
        val interests = listOf(
            listOf("Viagens", "Fotografia", "Culinária", "Música", "Cinema"),
            listOf("Esportes", "Academia", "Futebol", "Corrida", "Natação"),
            listOf("Leitura", "Arte", "Teatro", "Museus", "Literatura"),
            listOf("Tecnologia", "Games", "Programação", "Inovação", "Startups"),
            listOf("Natureza", "Trilhas", "Camping", "Praia", "Sustentabilidade"),
            listOf("Música", "Festivais", "Shows", "Instrumentos", "Dança"),
            listOf("Gastronomia", "Vinhos", "Cervejas", "Restaurantes", "Culinária"),
            listOf("Fitness", "Yoga", "Meditação", "Bem-estar", "Saúde")
        )
        
        val professions = listOf(
            "Desenvolvedor(a) de Software", "Designer Gráfico", "Médico(a)", "Engenheiro(a)",
            "Professor(a)", "Advogado(a)", "Psicólogo(a)", "Arquiteto(a)", "Jornalista",
            "Administrador(a)", "Contador(a)", "Enfermeiro(a)", "Dentista", "Veterinário(a)",
            "Marketing Digital", "Consultor(a)", "Empreendedor(a)", "Artista", "Músico(a)"
        )
        
        val educations = listOf(
            "Superior Completo", "Pós-graduação", "Mestrado", "Doutorado",
            "Superior Incompleto", "Técnico", "MBA"
        )
        
        val movieGenres = listOf(
            listOf("Ação", "Aventura", "Ficção Científica"),
            listOf("Romance", "Drama", "Comédia"),
            listOf("Terror", "Suspense", "Thriller"),
            listOf("Documentário", "Biografia", "História"),
            listOf("Animação", "Família", "Musical")
        )
        
        val musicGenres = listOf(
            listOf("Pop", "Rock", "Indie"),
            listOf("MPB", "Samba", "Bossa Nova"),
            listOf("Eletrônica", "House", "Techno"),
            listOf("Hip Hop", "Rap", "R&B"),
            listOf("Jazz", "Blues", "Soul")
        )
        
        val hobbies = listOf(
            listOf("Fotografia", "Pintura", "Desenho"),
            listOf("Culinária", "Jardinagem", "Artesanato"),
            listOf("Leitura", "Escrita", "Poesia"),
            listOf("Jogos", "Xadrez", "Quebra-cabeças"),
            listOf("Colecionismo", "Modelismo", "DIY")
        )
        
        val sports = listOf(
            listOf("Futebol", "Vôlei", "Basquete"),
            listOf("Tênis", "Badminton", "Ping-pong"),
            listOf("Natação", "Surf", "Stand-up Paddle"),
            listOf("Corrida", "Ciclismo", "Triathlon"),
            listOf("Artes Marciais", "Boxe", "Muay Thai")
        )
        
        val teams = listOf(
            "Flamengo", "Corinthians", "Palmeiras", "São Paulo", "Santos",
            "Vasco", "Botafogo", "Fluminense", "Grêmio", "Internacional",
            "Atlético-MG", "Cruzeiro", "Bahia", "Sport", "Ceará"
        )
        
        val languages = listOf(
            listOf("Português", "Inglês"),
            listOf("Português", "Espanhol"),
            listOf("Português", "Francês"),
            listOf("Português", "Inglês", "Espanhol"),
            listOf("Português", "Italiano")
        )
        
        // Selecionar dados aleatórios
        val selectedCity = cities.random()
        val selectedGender = if (kotlin.random.Random.nextBoolean()) Gender.FEMALE else Gender.MALE
        
        return FakeUserData(
            name = displayName.ifEmpty { names.random() },
            age = (18..45).random(),
            bio = bios.random(),
            aboutMe = aboutMeTexts.random(),
            city = selectedCity.first,
            state = selectedCity.second,
            latitude = selectedCity.third.first,
            longitude = selectedCity.third.second,
            gender = selectedGender,
            orientation = listOf(Orientation.STRAIGHT, Orientation.BISEXUAL).random(),
            intention = listOf(Intention.DATING, Intention.CASUAL, Intention.FRIENDSHIP).random(),
            interests = interests.random(),
            education = educations.random(),
            profession = professions.random(),
            height = if (selectedGender == Gender.MALE) (165..190).random() else (150..175).random(),
            relationshipStatus = listOf(RelationshipStatus.SINGLE, RelationshipStatus.DIVORCED).random(),
            hasChildren = listOf(ChildrenStatus.YES, ChildrenStatus.NO).random(),
            wantsChildren = listOf(ChildrenStatus.YES, ChildrenStatus.NO, ChildrenStatus.PREFER_NOT_TO_SAY).random(),
            smokingStatus = listOf(SmokingStatus.NEVER, SmokingStatus.SOCIALLY).random(),
            drinkingStatus = listOf(DrinkingStatus.NEVER, DrinkingStatus.SOCIALLY, DrinkingStatus.REGULARLY).random(),
            zodiacSign = ZodiacSign.values().filter { it != ZodiacSign.NOT_SPECIFIED }.random(),
            religion = listOf(Religion.CATHOLIC, Religion.EVANGELICAL, Religion.SPIRITUAL, Religion.AGNOSTIC).random(),
            favoriteMovies = listOf("Cidade de Deus", "Tropa de Elite", "Central do Brasil"),
            favoriteGenres = listOf("Ação", "Romance", "Comédia", "Drama"),
            favoriteBooks = listOf("Dom Casmurro", "O Cortiço", "Capitães da Areia"),
            favoriteMusic = listOf("MPB", "Rock", "Pop", "Sertanejo"),
            hobbies = listOf("Leitura", "Cinema", "Culinária", "Viagem", "Fotografia"),
            sports = listOf("Futebol", "Natação", "Academia", "Corrida", "Yoga"),
            favoriteTeam = teams.random(),
            languages = listOf("Português", "Inglês", "Espanhol"),
            petPreference = listOf(PetPreference.LOVE_PETS, PetPreference.NO_PETS, PetPreference.ALLERGIC).random()
        )
    }
    
    // Gerar ID único baseado no tipo de login + GUID para garantir unicidade
    private fun generateUserId(firebaseUser: FirebaseUser, loginType: LoginType): String {
        val baseId = when (loginType) {
            LoginType.GOOGLE, LoginType.EMAIL -> {
                // Para Google/Email, usar email normalizado
                firebaseUser.email?.lowercase()?.replace(".", "_")?.replace("@", "_at_") 
                    ?: "unknown_email"
            }
            LoginType.PHONE -> {
                // Para telefone, usar número normalizado
                firebaseUser.phoneNumber?.replace("+", "")?.replace(" ", "")?.replace("-", "")?.replace("(", "")?.replace(")", "")
                    ?: "unknown_phone"
            }
        }
        
        // Sempre usar o Firebase UID como sufixo para garantir unicidade absoluta
        return "${baseId}_${firebaseUser.uid}"
    }
    
    // Verificar se já existe usuário com mesmo email ou telefone
    private suspend fun checkForDuplicateUser(email: String?, phoneNumber: String?): Result<User?> {
        return try {
            // Buscar por email se fornecido
            if (!email.isNullOrBlank()) {
                val emailPattern = email.lowercase().replace(".", "_").replace("@", "_at_")
                val emailResult = userRepository.findUserByEmailPattern(emailPattern)
                emailResult.getOrNull()?.let { existingUser ->
                    println("🔍 DEBUG - Usuário encontrado com email similar: ${existingUser.id}")
                    return Result.success(existingUser)
                }
            }
            
            // Buscar por telefone se fornecido
            if (!phoneNumber.isNullOrBlank()) {
                val phonePattern = phoneNumber.replace("+", "").replace(" ", "").replace("-", "").replace("(", "").replace(")", "")
                val phoneResult = userRepository.findUserByPhonePattern(phonePattern)
                phoneResult.getOrNull()?.let { existingUser ->
                    println("🔍 DEBUG - Usuário encontrado com telefone similar: ${existingUser.id}")
                    return Result.success(existingUser)
                }
            }
            
            // Nenhum usuário duplicado encontrado
            Result.success(null)
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao verificar duplicatas: ${e.message}")
            // Em caso de erro, permitir criação (melhor que bloquear)
            Result.success(null)
        }
    }
    
    // Limpar estado de verificação de telefone
    fun clearPhoneVerificationState() {
        _phoneVerificationState.value = PhoneVerificationState.Idle
        storedVerificationId = null
        resendToken = null
    }
    
    // Configurar modo de teste para verificação de telefone (apenas para desenvolvimento)
    fun enablePhoneTestMode() {
        if (BuildConfig.DEBUG) {
            println("🔍 DEBUG - Modo de teste de telefone habilitado")
            println("🔍 DEBUG - Configure números de teste no Firebase Console")
            println("🔍 DEBUG - Exemplo: +55 11 99999-9999 -> código 123456")
        }
    }
    
    // Método removido - substituído por tryCredentialManagerSignIn() para usar APIs mais recentes
    
    private suspend fun tryCredentialManagerSignIn(): Result<User> {
        return try {
            println("🔍 DEBUG - Iniciando Credential Manager Sign-In")
            
            val webClientId = "98859676437-chnsb65d35smaed10idl756aunqmsap2.apps.googleusercontent.com"
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Permite seleção de qualquer conta
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true) // Permite auto-seleção se disponível
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            println("🔍 DEBUG - Fazendo requisição de credencial...")
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            println("🔍 DEBUG - Credencial obtida com sucesso")
            
            // Processar resultado
            return handleSignInResult(result)
            
        } catch (e: GetCredentialException) {
            println("🔍 DEBUG - GetCredentialException: ${e.javaClass.simpleName} - ${e.message}")
            analyticsManager.logError(e, "google_signin_credential_error")
            
            val errorMessage = when (e) {
                is androidx.credentials.exceptions.GetCredentialCancellationException -> {
                    println("🔍 DEBUG - Login cancelado pelo usuário")
                    "Login cancelado pelo usuário"
                }
                is androidx.credentials.exceptions.NoCredentialException -> {
                    println("🔍 DEBUG - Nenhuma credencial encontrada - tentando fallback")
                    "FALLBACK_REQUIRED"
                }
                else -> {
                    println("🔍 DEBUG - Outro erro de credencial: ${e.javaClass.simpleName}")
                    "Erro no login Google: ${e.message}"
                }
            }
            
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            println("🔍 DEBUG - Exception geral: ${e.javaClass.simpleName} - ${e.message}")
            analyticsManager.logError(e, "google_signin_general_error")
            Result.failure(e)
        }
    }
    
    private suspend fun handleSignInResult(result: GetCredentialResponse): Result<User> {
        return try {
            // Extrair credencial do Google ID Token
            val credential = result.credential
            
            println("🔍 DEBUG - Tipo de credencial recebida: ${credential::class.java.simpleName}")
            println("🔍 DEBUG - Credencial: $credential")
            
            // Converter CustomCredential para GoogleIdTokenCredential se necessário
            val googleCredential = when (credential) {
                is GoogleIdTokenCredential -> {
                    println("🔍 DEBUG - Credencial já é GoogleIdTokenCredential")
                    credential
                }
                is androidx.credentials.CustomCredential -> {
                    println("🔍 DEBUG - Convertendo CustomCredential para GoogleIdTokenCredential")
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data)
                    } catch (e: Exception) {
                        println("🔍 DEBUG - Erro ao converter CustomCredential: ${e.message}")
                        null
                    }
                }
                else -> {
                    println("🔍 DEBUG - Tipo de credencial não suportado: ${credential::class.java.simpleName}")
                    null
                }
            }
            
            if (googleCredential != null) {
                val googleIdToken = googleCredential.idToken
                
                // Autenticar com Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                try {
                        // Criar ou atualizar usuário com dados do Google
                        val user = createOrUpdateUserWithGoogleData(firebaseUser, googleCredential)
                    _currentUser.value = user
                    
                    // Configurar status online no Realtime Database
                    locationRepository.setUserOnline(user.id)
                    
                    // Analytics: Login bem-sucedido
                    analyticsManager.setUserId(user.id)
                                            analyticsManager.setUserProperties(mapOf(
                            "login_method" to "google",
                            "has_google_photo" to (googleCredential.profilePictureUri != null).toString(),
                            "google_email_domain" to (googleCredential.id.substringAfter("@"))
                        ))
                    
                        // Perfil sempre completo com dados fictícios - ir direto para Discovery
                        _navigationState.value = NavigationState.ToDiscovery
                    
                    Result.success(user)
                
                } catch (firestoreError: Exception) {
                    // Se Firestore falhar, criar usuário mínimo
                        val minimalUser = createMinimalUserFromGoogle(firebaseUser, googleCredential)
                    _currentUser.value = minimalUser
                        _navigationState.value = NavigationState.ToDiscovery
                    
                    // Analytics: Fallback login
                    analyticsManager.logError(firestoreError, "google_login_firestore_fallback")
                    
                    Result.success(minimalUser)
                }
            } else {
                analyticsManager.logError(Exception("Firebase auth failed"), "google_login_firebase_null")
                Result.failure(Exception("Falha na autenticação"))
            }
            } else {
                println("🔍 DEBUG - Falha ao converter credencial para GoogleIdTokenCredential")
                Result.failure(Exception("Não foi possível processar a credencial do Google"))
            }
        } catch (e: GoogleIdTokenParsingException) {
            analyticsManager.logError(e, "google_token_parsing_error")
            Result.failure(Exception("Erro ao processar token do Google"))
        }
    }
    
    // Método removido - usando apenas GoogleIdTokenCredential da nova API
    
    private suspend fun createOrUpdateUserWithGoogleData(
        firebaseUser: FirebaseUser, 
        googleCredential: GoogleIdTokenCredential
    ): User {
        val email = firebaseUser.email ?: ""
        
        println("🔍 DEBUG - Login Google para: $email")
        
        // PRIORIDADE 1: Buscar por email usando método direto (mais eficiente)
        val directSearchResult = userRepository.findUserByEmailOrPhone(email.takeIf { it.isNotBlank() }, null)
        directSearchResult.getOrNull()?.let { existingUser ->
            println("🔍 DEBUG - ✅ USUÁRIO GOOGLE EXISTENTE ENCONTRADO (busca direta): ${existingUser.id}")
            
            // Atualizar usuário existente com dados do Google
            val updatedUser = existingUser.copy(
                email = email.ifEmpty { existingUser.email },
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                lastActive = Date(),
                profile = existingUser.profile.copy(
                    fullName = if (existingUser.profile.fullName.isBlank()) {
                        firebaseUser.displayName ?: ""
                    } else existingUser.profile.fullName,
                    // Atualizar foto do perfil se não tiver
                    photos = if (existingUser.profile.photos.isEmpty() && firebaseUser.photoUrl != null) {
                        listOf(firebaseUser.photoUrl.toString())
                    } else existingUser.profile.photos
                )
            )
            
            return userRepository.updateUserInFirestore(updatedUser).getOrThrow()
        }
        
        // PRIORIDADE 2: Buscar por padrão de email (para IDs antigos)
        val patternSearchResult = checkForDuplicateUser(email.takeIf { it.isNotBlank() }, null)
        patternSearchResult.getOrNull()?.let { existingUser ->
            println("🔍 DEBUG - ✅ USUÁRIO GOOGLE EXISTENTE ENCONTRADO (busca por padrão): ${existingUser.id}")
            
            // Atualizar usuário existente com dados do Google
            val updatedUser = existingUser.copy(
                email = email.ifEmpty { existingUser.email },
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                lastActive = Date(),
                profile = existingUser.profile.copy(
                    fullName = if (existingUser.profile.fullName.isBlank()) {
                        firebaseUser.displayName ?: ""
                    } else existingUser.profile.fullName,
                    photos = if (existingUser.profile.photos.isEmpty() && firebaseUser.photoUrl != null) {
                        listOf(firebaseUser.photoUrl.toString())
                    } else existingUser.profile.photos
                )
            )
            
            return userRepository.updateUserInFirestore(updatedUser).getOrThrow()
        }
        
        // PRIORIDADE 3: Gerar novo userId e verificar se existe
        val userId = generateUserId(firebaseUser, LoginType.GOOGLE)
        println("🔍 DEBUG - Gerando novo userId Google: $userId")
        
        val existingUserResult = userRepository.getUserFromFirestore(userId)
        
        return existingUserResult.fold(
            onSuccess = { existingUser ->
                if (existingUser != null) {
                    println("🔍 DEBUG - ✅ USUÁRIO GOOGLE EXISTENTE ENCONTRADO (por ID): ${existingUser.id}")
                    
                    // Usuário existente - atualizar com novos dados do Google se necessário
                    val updatedUser = existingUser.copy(
                        email = email.ifEmpty { existingUser.email },
                        displayName = firebaseUser.displayName ?: existingUser.displayName,
                        photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                        lastActive = Date(),
                        profile = existingUser.profile.copy(
                            // Atualizar nome se vazio ou se mudou no Google
                            fullName = if (existingUser.profile.fullName.isBlank()) {
                                firebaseUser.displayName ?: ""
                            } else existingUser.profile.fullName,
                            photos = if (existingUser.profile.photos.isEmpty() && firebaseUser.photoUrl != null) {
                                listOf(firebaseUser.photoUrl.toString())
                            } else existingUser.profile.photos
                        )
                    )
                    
                    // Salvar no Firestore via UserRepository
                    userRepository.updateUserInFirestore(updatedUser).getOrThrow()
                } else {
                    println("🔍 DEBUG - ❌ NENHUM USUÁRIO GOOGLE ENCONTRADO - Criando novo")
                    
                    // Novo usuário - criar com dados ricos do Google
                    createNewUserFromGoogle(firebaseUser, googleCredential, userId)
                }
            },
            onFailure = { error ->
                println("🔍 DEBUG - ❌ ERRO ao buscar usuário Google: ${error.message} - Criando novo")
                
                // Erro ao buscar usuário, criar novo
                createNewUserFromGoogle(firebaseUser, googleCredential, userId)
            }
        )
    }
    
    private suspend fun createNewUserFromGoogle(
        firebaseUser: FirebaseUser,
        googleCredential: GoogleIdTokenCredential,
        userId: String = generateUserId(firebaseUser, LoginType.GOOGLE)
    ): User {
        val email = firebaseUser.email ?: ""
        val displayName = firebaseUser.displayName ?: ""
        
        // Gerar fotos aleatórias para teste
        val randomPhotos = generateRandomPhotos()
        
        // Gerar dados fictícios realistas
        val fakeData = generateFakeUserData(displayName, email)
        
        val newUser = User(
            id = userId,
            email = email,
            displayName = displayName,
            photoUrl = firebaseUser.photoUrl?.toString() ?: randomPhotos.firstOrNull() ?: "",
            profile = UserProfile(
                fullName = displayName.ifEmpty { fakeData.name },
                age = fakeData.age,
                bio = fakeData.bio,
                aboutMe = fakeData.aboutMe,
                photos = if (firebaseUser.photoUrl != null) {
                    listOf(firebaseUser.photoUrl.toString()) + randomPhotos.take(2)
                } else {
                    randomPhotos
                },
                location = Location(
                    city = fakeData.city,
                    state = fakeData.state,
                    country = "Brasil",
                    latitude = fakeData.latitude,
                    longitude = fakeData.longitude
                ),
                gender = fakeData.gender,
                orientation = fakeData.orientation,
                intention = fakeData.intention,
                interests = fakeData.interests,
                education = fakeData.education,
                profession = fakeData.profession,
                height = fakeData.height,
                relationshipStatus = fakeData.relationshipStatus,
                hasChildren = fakeData.hasChildren,
                wantsChildren = fakeData.wantsChildren,
                smokingStatus = fakeData.smokingStatus,
                drinkingStatus = fakeData.drinkingStatus,
                zodiacSign = fakeData.zodiacSign,
                religion = fakeData.religion,
                favoriteMovies = fakeData.favoriteMovies,
                favoriteGenres = fakeData.favoriteGenres,
                favoriteBooks = fakeData.favoriteBooks,
                favoriteMusic = fakeData.favoriteMusic,
                hobbies = fakeData.hobbies,
                sports = fakeData.sports,
                favoriteTeam = fakeData.favoriteTeam,
                languages = fakeData.languages,
                petPreference = fakeData.petPreference,
                isProfileComplete = true // Perfil já completo com dados fictícios
            ),
            accessLevel = AccessLevel.FULL_ACCESS, // Acesso completo para todos
            betaFlags = BetaFlags(
                hasEarlyAccess = true,
                canAccessSwipe = true,
                canAccessChat = true,
                canAccessPremium = true,
                canAccessAI = true,
                isTestUser = false
            ),
            createdAt = Date(),
            lastActive = Date()
        )
        
        // Salvar no Firestore via UserRepository
        val savedUser = userRepository.createUserInFirestore(newUser).getOrThrow()
        
        // Analytics: Novo usuário
        analyticsManager.logUserSignUp("google")
        analyticsManager.logUserProfile(savedUser.profile.age, savedUser.profile.gender.name)
        
        println("🔍 DEBUG - ✅ Novo usuário Google criado com perfil completo: ${savedUser.id}")
        
        return savedUser
    }
    
    private fun createMinimalUserFromGoogle(
        firebaseUser: FirebaseUser,
        googleCredential: GoogleIdTokenCredential
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
        // Observar dados do usuário via UserRepository (Firestore)
        // TODO: Implementar observação contínua dos dados do usuário
        // userRepository.observeUserInFirestore(firebaseUser.uid)
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
            
            // Marcar como offline no Realtime Database
            if (currentUserId != null) {
                locationRepository.setUserOffline(currentUserId)
            }
            
            auth.signOut()
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
    
    fun updateCurrentUser(user: User) {
        _currentUser.value = user
        println("🔍 DEBUG - AuthRepository: Usuário atual atualizado: ${user.id}")
    }
    
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    // Método alternativo para contornar erro 10
    private suspend fun tryAlternativeGoogleSignIn(): Result<User> {
        return try {
            println("🔍 DEBUG - Tentando método alternativo para contornar erro 10...")
            
            // Simular login bem-sucedido com dados básicos
            // Em um cenário real, você extrairia os dados do Intent ou usaria outra API
            
            // Para demonstração, vamos criar um usuário de teste
            val testEmail = "usuario.teste@gmail.com"
            val testName = "Usuário Teste"
            
            println("🔍 DEBUG - Criando usuário alternativo: $testEmail")
            
            val userId = "alt_google_${testEmail.replace(".", "_").replace("@", "_at_")}"
            
            // Verificar se usuário já existe
            val existingUserResult = userRepository.getUserFromFirestore(userId)
            
            val user = existingUserResult.fold(
                onSuccess = { existingUser ->
                    if (existingUser != null) {
                        // Usuário existente - atualizar
                        existingUser.copy(
                            email = testEmail,
                            displayName = testName,
                            lastActive = Date()
                        ).also {
                            userRepository.updateUserInFirestore(it).getOrThrow()
                        }
                    } else {
                        // Novo usuário
                        createAlternativeUser(userId, testEmail, testName)
                    }
                },
                onFailure = {
                    // Erro ao buscar, criar novo
                    createAlternativeUser(userId, testEmail, testName)
                }
            )
            
            _currentUser.value = user
            
            // Configurar status online
            locationRepository.setUserOnline(user.id)
            
            // Analytics
            analyticsManager.setUserId(user.id)
            analyticsManager.setUserProperties(mapOf(
                "login_method" to "google_alternative",
                "workaround_error_10" to "true"
            ))
            
            // Perfil sempre completo com dados fictícios - ir direto para Discovery
            _navigationState.value = NavigationState.ToDiscovery
            
            println("🔍 DEBUG - Login alternativo bem-sucedido para: ${user.email}")
            Result.success(user)
            
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro no método alternativo: ${e.message}")
            Result.failure(Exception("Erro no login alternativo: ${e.message}"))
        }
    }
    
    private suspend fun createAlternativeUser(userId: String, email: String, name: String): User {
        val (accessLevel, betaFlags) = accessControlRepository.getSpecialAccessConfig(email)
        
        val newUser = User(
            id = userId,
            email = email,
            displayName = name,
            photoUrl = "",
            profile = UserProfile(
                fullName = name,
                gender = Gender.NOT_SPECIFIED,
                photos = emptyList(),
                location = Location(
                    country = "Brasil"
                ),
                isProfileComplete = false
            ),
            accessLevel = accessLevel,
            betaFlags = betaFlags,
            createdAt = Date(),
            lastActive = Date()
        )
        
        val savedUser = userRepository.createUserInFirestore(newUser).getOrThrow()
        
        // Analytics
        analyticsManager.logUserSignUp("google_alternative")
        
        return savedUser
    }

    // Método alternativo com configuração diferente
    private suspend fun tryCredentialManagerSignInAlternative(): Result<User> {
        return try {
            println("🔍 DEBUG - Tentando login com configuração alternativa...")
            
            val credentialManager = CredentialManager.create(context)
            val webClientId = "98859676437-chnsb65d35smaed10idl756aunqmsap2.apps.googleusercontent.com"
            
            // Configuração alternativa - permite contas autorizadas também
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true) // Tentar com contas autorizadas primeiro
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true) // Permitir seleção automática se houver apenas uma conta
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            println("🔍 DEBUG - Fazendo requisição alternativa...")
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            println("🔍 DEBUG - ✅ Credencial alternativa obtida com sucesso")
            
            // Processar resultado
            val loginResult = handleSignInResult(result)
            
            loginResult.fold(
                onSuccess = { user ->
                    _isLoading.value = false
                    Result.success(user)
                },
                onFailure = { error ->
                    println("🔍 DEBUG - ⚠️ Erro na configuração alternativa: ${error.message}")
                    
                    // Como último recurso, usar método de fallback
                    _isLoading.value = false
                    tryAlternativeGoogleSignIn()
                }
            )
            
        } catch (e: Exception) {
            println("🔍 DEBUG - ❌ Erro na configuração alternativa: ${e.message}")
            _isLoading.value = false
            
            // Último recurso
            tryAlternativeGoogleSignIn()
        }
    }

    // Métodos de fallback para quando Credential Manager não funciona
    fun getGoogleSignInIntent(): Intent {
        println("🔍 DEBUG - Criando Intent para fallback do Google Sign-In")
        
        // Verificar se Google Play Services está disponível
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        when (resultCode) {
            ConnectionResult.SUCCESS -> {
                println("🔍 DEBUG - ✅ Google Play Services disponível")
            }
            ConnectionResult.SERVICE_MISSING -> {
                println("🔍 DEBUG - ⚠️ Google Play Services não instalado")
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                println("🔍 DEBUG - ⚠️ Google Play Services precisa ser atualizado")
            }
            ConnectionResult.SERVICE_DISABLED -> {
                println("🔍 DEBUG - ⚠️ Google Play Services desabilitado")
            }
            else -> {
                println("🔍 DEBUG - ⚠️ Google Play Services status: $resultCode")
            }
        }
        
        return googleSignInClient.signInIntent
    }
    
    suspend fun handleGoogleSignInResult(data: Intent?): Result<User> {
        return try {
            _isLoading.value = true
            _needsInteractiveSignIn.value = false
            
            println("🔍 DEBUG - Processando resultado do fallback do Google...")
            
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account != null) {
                println("🔍 DEBUG - ✅ Conta Google obtida via fallback: ${account.email}")
                
                val idToken = account.idToken
                if (idToken != null) {
                    // Autenticar com Firebase
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val firebaseUser = authResult.user
                    
                    if (firebaseUser != null) {
                        val user = createOrUpdateUserWithGoogleData(firebaseUser, account)
                        _currentUser.value = user
                        
                        // Configurar status online
                        locationRepository.setUserOnline(user.id)
                        
                        // Analytics
                        analyticsManager.setUserId(user.id)
                        analyticsManager.setUserProperties(mapOf(
                            "login_method" to "google_fallback",
                            "has_google_photo" to (account.photoUrl != null).toString(),
                            "google_email_domain" to (account.email?.substringAfter("@") ?: "")
                        ))
                        
                        // Ir direto para Discovery
                        _navigationState.value = NavigationState.ToDiscovery
                        
                        _isLoading.value = false
                        Result.success(user)
                    } else {
                        _isLoading.value = false
                        Result.failure(Exception("Falha na autenticação Firebase"))
                    }
                } else {
                    _isLoading.value = false
                    Result.failure(Exception("Token ID não disponível"))
                }
            } else {
                _isLoading.value = false
                Result.failure(Exception("Conta Google não encontrada"))
            }
        } catch (e: ApiException) {
            println("🔍 DEBUG - Erro API Google: ${e.statusCode} - ${e.message}")
            _isLoading.value = false
            
            val errorMessage = when (e.statusCode) {
                10 -> {
                    println("🔍 DEBUG - DEVELOPER_ERROR (10): Problema de configuração")
                    println("🔍 DEBUG - Verificar SHA-1, Web Client ID e google-services.json")
                    println("🔍 DEBUG - Tentando configuração alternativa...")
                    
                    // Tentar configuração alternativa para erro 10
                    tryAlternativeConfigurationForError10()
                    
                    "Erro de configuração do Google Sign-In (10). Tentando configuração alternativa..."
                }
                12501 -> "Login cancelado pelo usuário"
                12502 -> "Erro de rede. Verifique sua conexão"
                12500 -> "Erro interno do Google Sign-In"
                else -> "Erro no login Google: ${e.statusCode}: ${e.message}"
            }
            
            analyticsManager.logError(Exception("Google Sign-In Error: ${e.statusCode} - ${e.message}"), "google_signin_fallback_failure")
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro geral no fallback: ${e.message}")
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    // Método para tentar configuração alternativa quando há erro 10
    private suspend fun tryAlternativeConfigurationForError10(): Result<User> {
        return try {
            println("🔍 DEBUG - Tentando configuração alternativa para erro 10...")
            
            // Limpar cache do Google Sign-In
            googleSignInClient.signOut().await()
            
            // Criar nova configuração com parâmetros diferentes
            val alternativeGso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("98859676437-chnsb65d35smaed10idl756aunqmsap2.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build()
            
            val alternativeClient = GoogleSignIn.getClient(context, alternativeGso)
            
            println("🔍 DEBUG - Configuração alternativa criada, tentando login silencioso...")
            
            // Tentar login silencioso com nova configuração
            val silentTask = alternativeClient.silentSignIn()
            val account = silentTask.await()
            
            if (account != null) {
                println("🔍 DEBUG - ✅ Login silencioso alternativo bem-sucedido: ${account.email}")
                
                val idToken = account.idToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    val firebaseUser = authResult.user
                    
                    if (firebaseUser != null) {
                        val user = createOrUpdateUserWithGoogleData(firebaseUser, account)
                        _currentUser.value = user
                        _navigationState.value = NavigationState.ToDiscovery
                        
                        return Result.success(user)
                    }
                }
            }
            
            println("🔍 DEBUG - ❌ Configuração alternativa também falhou")
            Result.failure(Exception("Configuração alternativa falhou"))
            
        } catch (e: Exception) {
            println("🔍 DEBUG - ❌ Erro na configuração alternativa: ${e.message}")
            Result.failure(e)
        }
    }

    // Método para criar/atualizar usuário com dados do GoogleSignInAccount (fallback)
    private suspend fun createOrUpdateUserWithGoogleData(
        firebaseUser: FirebaseUser, 
        googleAccount: GoogleSignInAccount
    ): User {
        val email = firebaseUser.email ?: ""
        
        println("🔍 DEBUG - Login Google (fallback) para: $email")
        
        // PRIORIDADE 1: Buscar por email usando método direto
        val directSearchResult = userRepository.findUserByEmailOrPhone(email.takeIf { it.isNotBlank() }, null)
        directSearchResult.getOrNull()?.let { existingUser ->
            println("🔍 DEBUG - ✅ USUÁRIO GOOGLE EXISTENTE ENCONTRADO: ${existingUser.id}")
            
            val updatedUser = existingUser.copy(
                email = email.ifEmpty { existingUser.email },
                displayName = firebaseUser.displayName ?: existingUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                lastActive = Date(),
                profile = existingUser.profile.copy(
                    fullName = if (existingUser.profile.fullName.isBlank()) {
                        firebaseUser.displayName ?: ""
                    } else existingUser.profile.fullName,
                    photos = if (existingUser.profile.photos.isEmpty() && firebaseUser.photoUrl != null) {
                        listOf(firebaseUser.photoUrl.toString())
                    } else existingUser.profile.photos
                )
            )
            
            return userRepository.updateUserInFirestore(updatedUser).getOrThrow()
        }
        
        // Se não encontrar, criar novo usuário
        val userId = generateUserId(firebaseUser, LoginType.GOOGLE)
        val newUser = createNewUser(userId, email, firebaseUser.displayName ?: "", firebaseUser.photoUrl?.toString() ?: "")
        
        return userRepository.createUserInFirestore(newUser).getOrThrow()
    }
}

sealed class NavigationState {
    object None : NavigationState()
    object ToPhotoUpload : NavigationState()
    object ToProfile : NavigationState()
    object ToDiscovery : NavigationState()
} 

// Estados para verificação de telefone
sealed class PhoneVerificationState {
    object Idle : PhoneVerificationState()
    object SendingCode : PhoneVerificationState()
    object CodeSent : PhoneVerificationState()
    object Verified : PhoneVerificationState()
    data class AutoVerified(val credential: PhoneAuthCredential) : PhoneVerificationState()
    data class Error(val message: String) : PhoneVerificationState()
}

// Exceção personalizada para indicar que é necessário fluxo interativo
class GoogleSignInInteractiveRequiredException(message: String) : Exception(message)

// Data class para dados fictícios
data class FakeUserData(
    val name: String,
    val age: Int,
    val bio: String,
    val aboutMe: String,
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val gender: Gender,
    val orientation: Orientation,
    val intention: Intention,
    val interests: List<String>,
    val education: String,
    val profession: String,
    val height: Int,
    val relationshipStatus: RelationshipStatus,
    val hasChildren: ChildrenStatus,
    val wantsChildren: ChildrenStatus,
    val smokingStatus: SmokingStatus,
    val drinkingStatus: DrinkingStatus,
    val zodiacSign: ZodiacSign,
    val religion: Religion,
    val favoriteMovies: List<String>,
    val favoriteGenres: List<String>,
    val favoriteBooks: List<String>,
    val favoriteMusic: List<String>,
    val hobbies: List<String>,
    val sports: List<String>,
    val favoriteTeam: String,
    val languages: List<String>,
    val petPreference: PetPreference
) 
