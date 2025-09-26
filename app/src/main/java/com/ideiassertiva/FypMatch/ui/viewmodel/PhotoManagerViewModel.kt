package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.ideiassertiva.FypMatch.model.ProfilePhoto
import java.util.*

data class PhotoManagerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class PhotoManagerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoManagerUiState())
    val uiState: StateFlow<PhotoManagerUiState> = _uiState.asStateFlow()
    
    private val _photos = MutableStateFlow<List<ProfilePhoto>>(emptyList())
    val photos: StateFlow<List<ProfilePhoto>> = _photos.asStateFlow()
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    fun loadUserPhotos(userId: String) {
        // Mock data for demonstration
        // In real app, this would fetch from repository/API
        _photos.value = listOf(
            ProfilePhoto(
                id = "1",
                url = "https://images.unsplash.com/photo-1494790108755-2616b9c26851?w=400",
                isMain = true,
                uploadedAt = Date(),
                qualityScore = 0.9f,
                isVerified = true,
                faceDetected = true
            ),
            ProfilePhoto(
                id = "2", 
                url = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                isMain = false,
                uploadedAt = Date(),
                qualityScore = 0.8f,
                isVerified = false,
                faceDetected = true
            ),
            ProfilePhoto(
                id = "3",
                url = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400",
                isMain = false,
                uploadedAt = Date(),
                qualityScore = 0.7f,
                isVerified = false,
                faceDetected = true
            )
        )
    }
    
    fun addPhoto(userId: String) {
        _isUploading.value = true
        
        // Simulate photo upload
        // In real app, this would handle image picker and upload to storage
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(2000) // Simulate upload time
            
            val newPhoto = ProfilePhoto(
                id = UUID.randomUUID().toString(),
                url = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                isMain = false,
                uploadedAt = Date(),
                qualityScore = 0.8f,
                isVerified = false,
                faceDetected = true
            )
            
            _photos.value = _photos.value + newPhoto
            _isUploading.value = false
            _uiState.value = _uiState.value.copy(
                successMessage = "Foto adicionada com sucesso!"
            )
            
            // Clear success message after 3 seconds
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(successMessage = null)
        }
    }
    
    fun setAsMainPhoto(userId: String, photoId: String) {
        val currentPhotos = _photos.value
        val updatedPhotos = currentPhotos.map { photo ->
            photo.copy(isMain = photo.id == photoId)
        }
        _photos.value = updatedPhotos
        
        _uiState.value = _uiState.value.copy(
            successMessage = "Foto principal definida!"
        )
        
        // Clear success message
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(successMessage = null)
        }
    }
    
    fun deletePhoto(userId: String, photoId: String) {
        val currentPhotos = _photos.value
        val photoToDelete = currentPhotos.find { it.id == photoId }
        
        if (photoToDelete?.isMain == true && currentPhotos.size > 1) {
            // If deleting main photo, set another as main
            val updatedPhotos = currentPhotos.filter { it.id != photoId }
            val newMainPhoto = updatedPhotos.firstOrNull()
            _photos.value = updatedPhotos.map { photo ->
                if (photo.id == newMainPhoto?.id) {
                    photo.copy(isMain = true)
                } else {
                    photo.copy(isMain = false)
                }
            }
        } else {
            _photos.value = currentPhotos.filter { it.id != photoId }
        }
        
        _uiState.value = _uiState.value.copy(
            successMessage = "Foto removida!"
        )
        
        // Clear success message
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(successMessage = null)
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}