package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.AuthRepository
import com.ideiassertiva.FypMatch.data.repository.AvailabilityRepository
import com.ideiassertiva.FypMatch.data.repository.DiscoveryRepository
import com.ideiassertiva.FypMatch.data.repository.FirebaseChatRepository
import com.ideiassertiva.FypMatch.data.repository.ProfileViewsRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.data.repository.QuestionnaireRepository
import com.ideiassertiva.FypMatch.repositories.ReportRepository
import com.ideiassertiva.FypMatch.models.Report
import com.ideiassertiva.FypMatch.models.ReportReason
import com.ideiassertiva.FypMatch.model.CompatibilityHighlightCalculator
import com.ideiassertiva.FypMatch.model.CompatibilityHighlights
import com.ideiassertiva.FypMatch.model.SwipeType
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.UserAvailability
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
    private val chatRepository: FirebaseChatRepository,
    private val reportRepository: ReportRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val questionnaireRepository: QuestionnaireRepository,
    private val profileViewsRepository: ProfileViewsRepository
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

    private val _reportSuccess = MutableStateFlow(false)
    val reportSuccess: StateFlow<Boolean> = _reportSuccess.asStateFlow()

    private val _blockSuccess = MutableStateFlow(false)
    val blockSuccess: StateFlow<Boolean> = _blockSuccess.asStateFlow()

    private val _availability = MutableStateFlow<UserAvailability?>(null)
    val availability: StateFlow<UserAvailability?> = _availability.asStateFlow()

    private val _compatibilityHighlights = MutableStateFlow(CompatibilityHighlights())
    val compatibilityHighlights: StateFlow<CompatibilityHighlights> = _compatibilityHighlights.asStateFlow()

    fun loadUser(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            val cachedCard = discoveryRepository.getDiscoveryCards().firstOrNull { it.user.id == userId }
            val cachedUser = cachedCard?.user ?: discoveryRepository.getCachedUserById(userId)
            if (cachedUser != null) {
                _user.value = cachedUser
            }
            if (cachedCard != null) {
                _compatibilityHighlights.value = cachedCard.compatibilityHighlights
            }

            val remoteUser = userRepository.getUserById(userId)
            val resolvedUser = remoteUser ?: cachedUser
            _user.value = resolvedUser
            _availability.value = availabilityRepository.load(userId)?.takeIf { it.hasAnyBlocks }
            profileViewsRepository.recordProfileView(userId)

            if (cachedCard == null && resolvedUser != null) {
                val currentUserId = authRepository.getCurrentFirebaseUser()?.uid.orEmpty()
                val currentUser = userRepository.currentUser.value
                    ?: currentUserId.takeIf { it.isNotBlank() }?.let { userRepository.getUserById(it) }
                val currentSelfKnowledge = currentUserId
                    .takeIf { it.isNotBlank() }
                    ?.let { questionnaireRepository.loadSelfKnowledge(it) }
                val targetSelfKnowledge = questionnaireRepository.loadSelfKnowledge(userId)
                _compatibilityHighlights.value = CompatibilityHighlightCalculator.between(
                    currentUser = currentUser,
                    targetUser = resolvedUser,
                    currentSelfKnowledge = currentSelfKnowledge,
                    targetSelfKnowledge = targetSelfKnowledge
                )
            }
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

    fun reportUser(reportedUserId: String, reason: ReportReason, description: String = "") {
        viewModelScope.launch {
            _isActionLoading.value = true
            _error.value = null
            try {
                val currentUserId = authRepository.getCurrentFirebaseUser()?.uid.orEmpty()
                if (currentUserId.isBlank()) {
                    _error.value = "Usuário não autenticado"
                    return@launch
                }

                val report = Report(
                    reporterUserId = currentUserId,
                    reportedUserId = reportedUserId,
                    reason = reason,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )

                reportRepository.submitReport(report).fold(
                    onSuccess = {
                        _reportSuccess.value = true
                    },
                    onFailure = { error ->
                        _error.value = error.message ?: "Erro ao enviar denúncia"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao enviar denúncia"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun blockUser(targetUserId: String) {
        viewModelScope.launch {
            _isActionLoading.value = true
            _error.value = null
            try {
                val currentUserId = authRepository.getCurrentFirebaseUser()?.uid.orEmpty()
                if (currentUserId.isBlank()) {
                    _error.value = "Usuário não autenticado"
                    return@launch
                }

                reportRepository.blockUser(currentUserId, targetUserId).fold(
                    onSuccess = {
                        _blockSuccess.value = true
                    },
                    onFailure = { error ->
                        _error.value = error.message ?: "Erro ao bloquear usuário"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao bloquear usuário"
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun clearReportSuccess() {
        _reportSuccess.value = false
    }

    fun clearBlockSuccess() {
        _blockSuccess.value = false
    }
}
