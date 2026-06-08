package com.ideiassertiva.FypMatch.domain

import com.ideiassertiva.FypMatch.model.ConnectionStatus

class PushNotificationService {
    
    // Serviço simulado para disparar Push Notification (FCM)
    suspend fun sendConnectionLevelUpPush(userId: String, matchName: String, newStatus: ConnectionStatus) {
        val title = "Conexão Viva 🔥"
        val body = when(newStatus) {
            ConnectionStatus.ON_FIRE -> "Sua conexão com $matchName subiu de nível! A conversa tá pegando fogo. Que tal marcar um encontro?"
            ConnectionStatus.ACTIVE -> "Sua sintonia com $matchName está crescendo. Continuem assim!"
            else -> "Você tem novas afinidades descobertas com $matchName."
        }
        
        // Chamada fake para a API do Firebase Cloud Messaging
        // fcmClient.sendNotification(userId, title, body)
        println("PUSH ENVIADO PARA $userId: $title - $body")
    }
}
