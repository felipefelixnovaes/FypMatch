package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ideiassertiva.FypMatch.model.*

class PhotoManagerViewModel : ViewModel() {
    private val _photos = MutableStateFlow<List<ProfilePhoto>>(emptyList())
    val photos: StateFlow<List<ProfilePhoto>> = _photos.asStateFlow()
    
    private val _uiState = MutableStateFlow(PhotoManagerUiState())
    val uiState: StateFlow<PhotoManagerUiState> = _uiState.asStateFlow()
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    fun loadUserPhotos(userId: String) {
        viewModelScope.launch {
            delay(500)
            _photos.value = listOf(
                ProfilePhoto("p1", "https://picsum.photos/400/600?random=1", isMain = true, isVerified = false),
                ProfilePhoto("p2", "https://picsum.photos/400/600?random=2", isMain = false, isVerified = false),
                ProfilePhoto("p3", "https://picsum.photos/400/600?random=3", isMain = false, isVerified = true)
            )
        }
    }
    
    fun addPhoto(userId: String) {
        viewModelScope.launch {
            _isUploading.value = true
            delay(1000)
            val newPhoto = ProfilePhoto(id = "p${_photos.value.size + 1}", url = "https://picsum.photos/400/600?random=${_photos.value.size + 10}", isMain = false, isVerified = false)
            _photos.value = _photos.value + newPhoto
            _isUploading.value = false
        }
    }
    
    fun setAsMainPhoto(userId: String, photoId: String) {
        viewModelScope.launch {
            delay(300)
            _photos.value = _photos.value.map { it.copy(isMain = it.id == photoId) }
        }
    }
    
    fun deletePhoto(userId: String, photoId: String) {
        viewModelScope.launch {
            delay(300)
            _photos.value = _photos.value.filter { it.id != photoId }
        }
    }
}

data class PhotoManagerUiState(val message: String = "")