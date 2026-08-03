package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.ideiassertiva.FypMatch.data.repository.UsernameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeepLinkViewModel @Inject constructor(
    private val usernameRepository: UsernameRepository
) : ViewModel() {
    suspend fun resolveUsername(username: String): String? =
        usernameRepository.userIdForUsername(username)
}
