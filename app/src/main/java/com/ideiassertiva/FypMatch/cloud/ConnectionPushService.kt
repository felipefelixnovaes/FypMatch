package com.ideiassertiva.FypMatch.cloud

import com.ideiassertiva.FypMatch.model.ConnectionStatus

// Simulação da trigger que seria rodada num Cloud Function Firebase
class ConnectionPushService {
    fun onStatusChanged(userId: String, oldStatus: ConnectionStatus, newStatus: ConnectionStatus) {
        if (newStatus == ConnectionStatus.ON_FIRE && oldStatus != ConnectionStatus.ON_FIRE) {
            sendPushNotification(
                userId = userId,
                title = "A conexão esquentou! 🔥",
                body = "Vocês têm uma baita sintonia. Que tal usar a Conexão Viva para planejar um café?"
            )
        }
    }
    
    private fun sendPushNotification(userId: String, title: String, body: String) {
        // Integração Firebase Cloud Messaging
    }
}
