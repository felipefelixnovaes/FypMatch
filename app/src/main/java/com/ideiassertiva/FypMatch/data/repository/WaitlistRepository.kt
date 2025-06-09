package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class WaitlistRepository {
    
    // Simulando dados locais (depois será Firebase)
    private val _waitlistUsers = MutableStateFlow<List<WaitlistUser>>(emptyList())
    val waitlistUsers: Flow<List<WaitlistUser>> = _waitlistUsers.asStateFlow()
    
    private val _currentUser = MutableStateFlow<WaitlistUser?>(null)
    val currentUser: Flow<WaitlistUser?> = _currentUser.asStateFlow()
    
    private val accessControlRepository = AccessControlRepository()
    
    suspend fun joinWaitlist(
        fullName: String,
        email: String,
        city: String,
        state: String,
        age: Int,
        gender: Gender,
        orientation: Orientation,
        intention: Intention,
        invitedByCode: String? = null
    ): Result<WaitlistUser> {
        return try {
            // Verificar se email já existe
            val existingUser = _waitlistUsers.value.find { it.email == email }
            if (existingUser != null) {
                return Result.failure(Exception("Email já cadastrado na lista de espera"))
            }
            
            // Gerar código de convite único
            val inviteCode = generateUniqueInviteCode()
            
            // Calcular posição na fila
            val currentUsers = _waitlistUsers.value
            val basePosition = currentUsers.size + 1
            
            // Se foi convidado, verificar se o código existe e dar posição melhor
            val invitedBy = invitedByCode?.let { code ->
                currentUsers.find { it.inviteCode == code }
            }
            
            // Verificar se é email especial para determinar posição e acesso
            val isSpecialEmail = accessControlRepository.isAdminEmail(email) || 
                               accessControlRepository.isVipEmail(email) ||
                               accessControlRepository.isBetaAccessEmail(email)
            
            val position = when {
                accessControlRepository.isAdminEmail(email) -> 1 // Admin sempre primeiro
                accessControlRepository.isVipEmail(email) -> 2 // VIP em segundo
                accessControlRepository.isBetaAccessEmail(email) -> maxOf(1, basePosition / 2) // Beta na frente
                invitedBy != null -> maxOf(1, basePosition - 5) // Convidados têm prioridade
                else -> basePosition
            }
            
            val specialAccessLevel = accessControlRepository.getAccessLevelByEmail(email)
            
            val newUser = WaitlistUser(
                id = UUID.randomUUID().toString(),
                fullName = fullName,
                email = email,
                city = city,
                state = state,
                age = age,
                gender = gender,
                orientation = orientation,
                intention = intention,
                inviteCode = inviteCode,
                invitedBy = invitedByCode,
                joinedAt = Date(),
                position = position,
                status = when {
                    accessControlRepository.isAdminEmail(email) -> WaitlistStatus.FULL_ACCESS
                    accessControlRepository.isVipEmail(email) -> WaitlistStatus.VIP_ACCESS
                    accessControlRepository.isBetaAccessEmail(email) -> WaitlistStatus.EARLY_ACCESS
                    invitedBy != null && invitedBy.rewards.contains(WaitlistReward.VIP_ACCESS) -> WaitlistStatus.VIP_ACCESS
                    else -> WaitlistStatus.WAITING
                },
                accessLevel = when {
                    specialAccessLevel != AccessLevel.WAITLIST -> specialAccessLevel
                    invitedBy != null && invitedBy.rewards.contains(WaitlistReward.VIP_ACCESS) -> AccessLevel.BETA_ACCESS
                    else -> AccessLevel.WAITLIST
                }
            )
            
            // Adicionar usuário à lista
            val updatedUsers = currentUsers + newUser
            _waitlistUsers.value = updatedUsers
            _currentUser.value = newUser
            
            // Atualizar estatísticas do usuário que convidou
            invitedBy?.let { inviter ->
                updateInviterStats(inviter.id, inviteCode)
            }
            
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateUniqueInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        do {
            code = (1..6).map { chars.random() }.joinToString("")
        } while (_waitlistUsers.value.any { it.inviteCode == code })
        return code
    }
    
    private suspend fun updateInviterStats(inviterId: String, newInviteCode: String) {
        val currentUsers = _waitlistUsers.value.toMutableList()
        val inviterIndex = currentUsers.indexOfFirst { it.id == inviterId }
        
        if (inviterIndex != -1) {
            val inviter = currentUsers[inviterIndex]
            val updatedInvitesSent = inviter.invitesSent + newInviteCode
            val updatedInvitesAccepted = inviter.invitesAccepted + 1
            
            // Calcular novas recompensas
            val newRewards = calculateRewards(updatedInvitesAccepted)
            
            // Atualizar status baseado nas recompensas
            val newStatus = when {
                newRewards.contains(WaitlistReward.VIP_ACCESS) -> WaitlistStatus.VIP_ACCESS
                newRewards.contains(WaitlistReward.PASSPORT_MODE) -> WaitlistStatus.EARLY_ACCESS
                else -> inviter.status
            }
            
            val updatedInviter = inviter.copy(
                invitesSent = updatedInvitesSent,
                invitesAccepted = updatedInvitesAccepted,
                rewards = newRewards,
                status = newStatus
            )
            
            currentUsers[inviterIndex] = updatedInviter
            _waitlistUsers.value = currentUsers
            
            // Atualizar usuário atual se for o mesmo
            if (_currentUser.value?.id == inviterId) {
                _currentUser.value = updatedInviter
            }
        }
    }
    
    private fun calculateRewards(invitesAccepted: Int): List<WaitlistReward> {
        val rewards = mutableListOf<WaitlistReward>()
        
        if (invitesAccepted >= 1) rewards.add(WaitlistReward.BETTER_POSITION)
        if (invitesAccepted >= 3) rewards.add(WaitlistReward.PREMIUM_7_DAYS)
        if (invitesAccepted >= 5) rewards.add(WaitlistReward.PASSPORT_MODE)
        if (invitesAccepted >= 10) rewards.add(WaitlistReward.VIP_ACCESS)
        if (invitesAccepted >= 20) rewards.add(WaitlistReward.EARLY_ADOPTER)
        
        return rewards
    }
    
    fun getWaitlistStats(): WaitlistStats {
        val currentUser = _currentUser.value ?: return WaitlistStats()
        val totalUsers = _waitlistUsers.value.size
        
        val nextReward = getNextReward(currentUser.invitesAccepted)
        val invitesNeeded = getInvitesNeededForNextReward(currentUser.invitesAccepted)
        
        return WaitlistStats(
            totalUsers = totalUsers,
            userPosition = currentUser.position,
            invitesSent = currentUser.invitesSent.size,
            invitesAccepted = currentUser.invitesAccepted,
            estimatedWaitTime = calculateEstimatedWaitTime(currentUser.position),
            nextReward = nextReward,
            invitesNeededForNextReward = invitesNeeded
        )
    }
    
    private fun getNextReward(currentInvites: Int): WaitlistReward? {
        return when {
            currentInvites < 1 -> WaitlistReward.BETTER_POSITION
            currentInvites < 3 -> WaitlistReward.PREMIUM_7_DAYS
            currentInvites < 5 -> WaitlistReward.PASSPORT_MODE
            currentInvites < 10 -> WaitlistReward.VIP_ACCESS
            currentInvites < 20 -> WaitlistReward.EARLY_ADOPTER
            else -> null
        }
    }
    
    private fun getInvitesNeededForNextReward(currentInvites: Int): Int {
        return when {
            currentInvites < 1 -> 1 - currentInvites
            currentInvites < 3 -> 3 - currentInvites
            currentInvites < 5 -> 5 - currentInvites
            currentInvites < 10 -> 10 - currentInvites
            currentInvites < 20 -> 20 - currentInvites
            else -> 0
        }
    }
    
    private fun calculateEstimatedWaitTime(position: Int): String {
        return when {
            position <= 100 -> "1-2 semanas"
            position <= 500 -> "2-4 semanas" 
            position <= 1000 -> "1-2 meses"
            else -> "2+ meses"
        }
    }
    
    fun validateInviteCode(code: String): Boolean {
        return _waitlistUsers.value.any { it.inviteCode == code }
    }
} 
