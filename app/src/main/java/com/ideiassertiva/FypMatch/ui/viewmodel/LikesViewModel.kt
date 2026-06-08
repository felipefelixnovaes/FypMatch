package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
import com.ideiassertiva.FypMatch.data.repository.FirebaseChatRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikesViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: FirebaseChatRepository
) : ViewModel() {

    private val _received = MutableStateFlow<List<User>>(emptyList())
    val received: StateFlow<List<User>> = _received.asStateFlow()

    private val _sent = MutableStateFlow<List<User>>(emptyList())
    val sent: StateFlow<List<User>> = _sent.asStateFlow()

    private val _matches = MutableStateFlow<List<User>>(emptyList())
    val matches: StateFlow<List<User>> = _matches.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _chatNavigation = MutableStateFlow<String?>(null)
    val chatNavigation: StateFlow<String?> = _chatNavigation.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true

            // Abrir a tela zera o badge de novidades no coração
            discoveryRepository.markLikesSeen()

            val currentUserId = authRepository.getCurrentFirebaseUser()?.uid.orEmpty()

            // MATCHES — carrega do Firestore (ambos os lados) e resolve os IDs para User
            discoveryRepository.loadUserMatches(currentUserId)
            val matchUserIds = discoveryRepository.getMatchUserIds(currentUserId).toSet()
            _matches.value = matchUserIds
                .mapNotNull { userRepository.getUserById(it) }

            // Curtidas RECEBIDAS — perfis que deram like no usuário atual e ainda não viraram match
            _received.value = discoveryRepository.getReceivedLikeProfiles(currentUserId)

            // Curtidas ENVIADAS — likes persistidos no Firestore, sem repetir quem já está em Matches
            _sent.value = discoveryRepository.getSentLikeIds(currentUserId)
                .filterNot { it in matchUserIds }
                .mapNotNull { userRepository.getUserById(it) }

            _isLoading.value = false
        }
    }

    fun openMatchChat(otherUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentFirebaseUser()?.uid.orEmpty()
                if (currentUserId.isBlank()) {
                    _error.value = "Usuário não autenticado"
                    return@launch
                }
                _chatNavigation.value = chatRepository.getOrCreateConversationBetween(
                    currentUserId = currentUserId,
                    otherUserId = otherUserId
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao abrir conversa"
            }
        }
    }

    fun clearChatNavigation() {
        _chatNavigation.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
