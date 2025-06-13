package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.data.repository.PhotoRepository
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.UserProfile
import com.ideiassertiva.FypMatch.model.isProfileComplete
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Primeiro, tentar obter o usuário atual do Firebase Auth
                val firebaseUser = authRepository.getCurrentFirebaseUser()
                if (firebaseUser != null) {
                    println("🔍 DEBUG - ProfileViewModel: Firebase user encontrado: ${firebaseUser.uid}")
                    
                    // Buscar diretamente no Firestore usando o UID do Firebase
                    println("🔍 DEBUG - ProfileViewModel: Buscando usuário no Firestore...")
                    loadUserFromFirestore(firebaseUser.uid)
                } else {
                    println("🔍 DEBUG - ProfileViewModel: Nenhum usuário Firebase encontrado")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Usuário não encontrado - faça login novamente"
                    )
                }
            } catch (e: Exception) {
                println("🔍 DEBUG - ProfileViewModel: Erro ao carregar usuário: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao carregar usuário: ${e.message}"
                )
                analyticsManager.logError(e, "profile_load_error")
            }
        }
    }
    
    private suspend fun loadUserFromFirestore(userId: String) {
        try {
            println("🔍 DEBUG - ProfileViewModel: Buscando usuário no Firestore com ID: $userId")
            val result = userRepository.getUserFromFirestore(userId)
            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        println("🔍 DEBUG - ProfileViewModel: ✅ Usuário encontrado no Firestore: ${user.id}")
                        println("🔍 DEBUG - ProfileViewModel: Nome: ${user.profile.fullName}, Email: ${user.email}")
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                        
                        // Atualizar também o AuthRepository
                        authRepository.updateCurrentUser(user)
                    } else {
                        println("🔍 DEBUG - ProfileViewModel: ❌ Usuário não encontrado no Firestore")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Perfil não encontrado - complete seu cadastro"
                        )
                    }
                },
                onFailure = { error ->
                    println("🔍 DEBUG - ProfileViewModel: ❌ Erro ao buscar no Firestore: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro ao buscar perfil: ${error.message}"
                    )
                }
            )
        } catch (e: Exception) {
            println("🔍 DEBUG - ProfileViewModel: ❌ Exceção ao buscar usuário: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Erro inesperado: ${e.message}"
            )
        }
    }
    
    fun updateUser(updatedUser: User) {
        _uiState.value = _uiState.value.copy(user = updatedUser)
    }
    
    fun saveProfile(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveSuccess = false)
            
            try {
                val result = userRepository.updateUserInFirestore(user)
                result.fold(
                    onSuccess = { savedUser ->
                        _uiState.value = _uiState.value.copy(
                            user = savedUser,
                            isSaving = false,
                            saveSuccess = true,
                            error = null
                        )
                        
                        // Analytics
                        analyticsManager.logUserProfile(
                            savedUser.profile.age.takeIf { it > 0 },
                            savedUser.profile.gender.name.takeIf { it != "NOT_SPECIFIED" }
                        )
                        
                        analyticsManager.logCustomCrash("profile_updated", mapOf(
                            "user_id" to savedUser.id,
                            "profile_complete" to savedUser.isProfileComplete().toString(),
                            "has_photos" to savedUser.profile.photos.isNotEmpty().toString()
                        ))
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            error = "Erro ao salvar perfil: ${error.message}"
                        )
                        analyticsManager.logError(error, "profile_save_error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Erro inesperado ao salvar: ${e.message}"
                )
                analyticsManager.logError(e, "profile_save_unexpected_error")
            }
        }
    }
    
    fun addPhoto(photoUrl: String) {
        val currentUser = _uiState.value.user ?: return
        
        viewModelScope.launch {
            try {
                // Salvar metadados da foto
                val photoResult = photoRepository.savePhotoMetadata(
                    userId = currentUser.id,
                    photoUrl = photoUrl,
                    source = com.ideiassertiva.FypMatch.data.repository.PhotoSource.EXTERNAL_URL,
                    isMainPhoto = currentUser.profile.photos.isEmpty() // Primeira foto é principal
                )
                
                photoResult.fold(
                    onSuccess = {
                        // Atualizar lista de fotos do usuário
                        val updatedUser = currentUser.copy(
                            profile = currentUser.profile.copy(
                                photos = currentUser.profile.photos + photoUrl
                            )
                        )
                        updateUser(updatedUser)
                        
                        // Salvar no Firestore
                        saveProfile(updatedUser)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Erro ao adicionar foto: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erro inesperado ao adicionar foto: ${e.message}"
                )
            }
        }
    }
    
    fun removePhoto(photoIndex: Int) {
        val currentUser = _uiState.value.user ?: return
        
        if (photoIndex >= 0 && photoIndex < currentUser.profile.photos.size) {
            val updatedPhotos = currentUser.profile.photos.toMutableList().apply {
                removeAt(photoIndex)
            }
            
            val updatedUser = currentUser.copy(
                profile = currentUser.profile.copy(photos = updatedPhotos)
            )
            
            updateUser(updatedUser)
            saveProfile(updatedUser)
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
    
    fun getCurrentUserId(): String? {
        return _uiState.value.user?.id ?: authRepository.getCurrentFirebaseUser()?.uid
    }
    
    fun isProfileComplete(): Boolean {
        return _uiState.value.user?.isProfileComplete() ?: false
    }
    
    fun uploadPhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    // Simular upload da foto (em um app real, você faria upload para Firebase Storage)
                    // Por enquanto, vamos apenas adicionar a URI como string na lista de fotos
                    val photoUrl = uri.toString() // Em produção, seria a URL do Firebase Storage
                    
                    val updatedPhotos = currentUser.profile.photos.toMutableList()
                    updatedPhotos.add(0, photoUrl) // Adicionar no início da lista
                    
                    val updatedUser = currentUser.copy(
                        profile = currentUser.profile.copy(
                            photos = updatedPhotos
                        )
                    )
                    
                    // Salvar no Firestore
                    val result = userRepository.updateUserInFirestore(updatedUser)
                    
                    result.fold(
                        onSuccess = { savedUser ->
                            _uiState.value = _uiState.value.copy(
                                user = savedUser,
                                isSaving = false,
                                saveSuccess = true,
                                error = null
                            )
                            
                            // Analytics
                            analyticsManager.logFeatureUsage("photo_uploaded")
                            analyticsManager.setUserProperties(mapOf(
                                "user_id" to savedUser.id,
                                "photo_count" to savedUser.profile.photos.size.toString(),
                                "is_first_photo" to (savedUser.profile.photos.size == 1).toString()
                            ))
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                error = "Erro ao salvar foto: ${error.message}"
                            )
                            
                            // Analytics
                            analyticsManager.logError(error, "photo_upload_failed - user_id: ${currentUser.id}")
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Usuário não encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Erro inesperado: ${e.message}"
                )
                
                // Analytics
                analyticsManager.logError(e, "photo_upload_exception - user_id: ${_uiState.value.user?.id ?: "unknown"}")
            }
        }
    }
    
    fun removePhoto(photoUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    val updatedPhotos = currentUser.profile.photos.filter { it != photoUrl }
                    
                    val updatedUser = currentUser.copy(
                        profile = currentUser.profile.copy(
                            photos = updatedPhotos
                        )
                    )
                    
                    // Salvar no Firestore
                    val result = userRepository.updateUserInFirestore(updatedUser)
                    
                    result.fold(
                        onSuccess = { savedUser ->
                            _uiState.value = _uiState.value.copy(
                                user = savedUser,
                                isSaving = false,
                                saveSuccess = true,
                                error = null
                            )
                            
                            // Analytics
                            analyticsManager.logFeatureUsage("photo_removed")
                            analyticsManager.setUserProperties(mapOf(
                                "user_id" to savedUser.id,
                                "remaining_photos" to savedUser.profile.photos.size.toString()
                            ))
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                error = "Erro ao remover foto: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Erro inesperado: ${e.message}"
                )
            }
        }
    }
} 