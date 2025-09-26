package com.ideiassertiva.FypMatch.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ideiassertiva.FypMatch.MainActivity
import com.ideiassertiva.FypMatch.R

class FypMatchMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle different types of messages
        when (remoteMessage.data["type"]) {
            "new_message" -> handleNewMessage(remoteMessage)
            "new_match" -> handleNewMatch(remoteMessage)
            "typing_indicator" -> handleTypingIndicator(remoteMessage)
            else -> handleGenericMessage(remoteMessage)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Send token to server or save locally
        // In a real implementation, you would send this to your backend
        // to associate the token with the current user
        saveTokenToPreferences(token)
    }
    
    private fun handleNewMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Nova mensagem"
        val body = remoteMessage.data["body"] ?: "Você recebeu uma nova mensagem"
        val conversationId = remoteMessage.data["conversationId"]
        val senderId = remoteMessage.data["senderId"]
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "chat")
            putExtra("conversation_id", conversationId)
            putExtra("sender_id", senderId)
        }
        
        showNotification(title, body, intent, CHANNEL_ID_MESSAGES)
    }
    
    private fun handleNewMatch(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Novo match! 💕"
        val body = remoteMessage.data["body"] ?: "Você tem um novo match!"
        val matchId = remoteMessage.data["matchId"]
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "matches")
            putExtra("match_id", matchId)
        }
        
        showNotification(title, body, intent, CHANNEL_ID_MATCHES)
    }
    
    private fun handleTypingIndicator(remoteMessage: RemoteMessage) {
        // For typing indicators, we typically don't show notifications
        // but instead update the UI if the app is in foreground
        // This could be handled by a broadcast or event bus
    }
    
    private fun handleGenericMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "FypMatch"
        val body = remoteMessage.notification?.body ?: ""
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        showNotification(title, body, intent, CHANNEL_ID_GENERAL)
    }
    
    private fun showNotification(title: String, body: String, intent: Intent, channelId: String) {
        createNotificationChannel(channelId)
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon since R.mipmap.ic_launcher might not exist
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        
        // Add action buttons for message notifications
        if (channelId == CHANNEL_ID_MESSAGES) {
            val replyIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("action", "reply")
                putExtra("conversation_id", intent.getStringExtra("conversation_id"))
            }
            val replyPendingIntent = PendingIntent.getActivity(
                this, 1, replyIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            notificationBuilder.addAction(
                android.R.drawable.ic_menu_send, // Using system icon
                "Responder",
                replyPendingIntent
            )
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, description) = when (channelId) {
                CHANNEL_ID_MESSAGES -> "Mensagens" to "Notificações de novas mensagens"
                CHANNEL_ID_MATCHES -> "Matches" to "Notificações de novos matches"
                else -> "Geral" to "Notificações gerais do app"
            }
            
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun saveTokenToPreferences(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }
    
    companion object {
        private const val CHANNEL_ID_MESSAGES = "messages_channel"
        private const val CHANNEL_ID_MATCHES = "matches_channel"
        private const val CHANNEL_ID_GENERAL = "general_channel"
        
        fun getToken(context: Context): String? {
            val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            return prefs.getString("fcm_token", null)
        }
    }
}