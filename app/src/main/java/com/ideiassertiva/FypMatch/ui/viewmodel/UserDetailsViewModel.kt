package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
import com.ideiassertiva.FypMatch.data.repository.FirebaseChatRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.SwipeType
import com.ideiassertiva.FypMatch.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val discoveryRepository: DiscoveryRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: FirebaseChatRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    private val _actionCompleted = MutableStateFlow(false)
    val actionCompleted: StateFlow<Boolean> = _actionCompleted.asStateFlow()

    private val _chatNavigation = MutableStateFlow<String?>(null)
    val chatNavigation: StateFlow<String?> = _chatNavigation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUser(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            val cachedUser = discoveryRepository.getCachedUserById(userId)
            if (cachedUser != null) {
                _user.value = cachedUser
            }

            val remoteUser = userRepository.getUserById(userId)
            _user.value = remoteUser ?: cachedUser
            _isLoading.value = false
        }
    }

    fun performSwipe(targetUserId: String, swipeType: SwipeType) {
        if (targetUserId.isBlank() || _isActionLoading.value) return

        viewModelScope.launch {
            _isActionLoading.value = true
            _error.value = null
            try {
                val currentUserId = authRepository.getCurrentFirebaseUser()?.uid.orEmpty()
                if (currentUserId.isBlank()) {
                    _error.value = "Usuário não autenticado"
                    return@launch
                }

                val result = discoveryRepository.performSwipe(
                    fromUserId = currentUserId,
                    toUserId = targetUserId,
                    swipeType = swipeType
                )

                result.fold(
                    onSuccess = { swipeResult ->
                        if (swipeResult.isMatch && swipeResult.match != null) {
                            _chatNavigation.value = chatRepository.getOrCreateConversationForMatch(
                                match = swipeResult.match,
                                currentUserId = currentUserId
                            )
                        } else {
                            _actionCompleted.value = true
                        }
                    },
                    onFailure = { error ->
                        _error.value = error.message ?: "Erro ao processar ação"
                    }
                )
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun clearActionCompleted() {
        _actionCompleted.value = false
    }

    fun clearChatNavigation() {
        _chatNavigation.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
