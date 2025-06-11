package com.ideiassertiva.FypMatch.data.repository

import com.ideiassertiva.FypMatch.model.Location
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    private val database = FirebaseDatabase.getInstance("https://fypmatch-8ac3c-default-rtdb.firebaseio.com/")
    private val auth = FirebaseAuth.getInstance()
    
    // Referências do Realtime Database
    private val locationsRef = database.getReference("localizacoes")
    private val onlineUsersRef = database.getReference("usuarios_online")
    private val lastSeenRef = database.getReference("ultimo_visto")
    
    /**
     * REALTIME DATABASE - DADOS DINÂMICOS E TEMPORAIS
     * - Localização atual do usuário (GPS)
     * - Status online/offline
     * - Última vez visto
     * - Atividade em tempo real
     */
    
    // === STATUS ONLINE/OFFLINE ===
    
    suspend fun setUserOnline(userId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            // Marcar como online
            onlineUsersRef.child(userId).setValue(mapOf(
                "online" to true,
                "lastActivity" to currentTime,
                "timestamp" to currentTime
            )).await()
            
            // Configurar desconexão automática
            onlineUsersRef.child(userId).onDisconnect().setValue(mapOf(
                "online" to false,
                "lastActivity" to currentTime
            ))
            
            // Analytics
            analyticsManager.logCustomCrash("user_set_online", mapOf(
                "user_id" to userId,
                "timestamp" to currentTime.toString()
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "set_user_online_error")
            Result.failure(e)
        }
    }
    
    suspend fun setUserOffline(userId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            // Marcar como offline e atualizar último visto
            onlineUsersRef.child(userId).setValue(mapOf(
                "online" to false,
                "lastActivity" to currentTime
            )).await()
            
            lastSeenRef.child(userId).setValue(currentTime).await()
            
            // Analytics
            analyticsManager.logCustomCrash("user_set_offline", mapOf(
                "user_id" to userId,
                "timestamp" to currentTime.toString()
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "set_user_offline_error")
            Result.failure(e)
        }
    }
    
    suspend fun updateLastActivity(userId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            onlineUsersRef.child(userId).child("lastActivity").setValue(currentTime).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "update_last_activity_error")
            Result.failure(e)
        }
    }
    
    // === LOCALIZAÇÃO ATUAL ===
    
    suspend fun updateCurrentLocation(
        userId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float? = null
    ): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            val locationData = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "accuracy" to accuracy,
                "timestamp" to currentTime,
                "lastUpdated" to currentTime
            )
            
            locationsRef.child(userId).setValue(locationData).await()
            
            // Analytics
            analyticsManager.logCustomCrash("location_updated", mapOf(
                "user_id" to userId,
                "accuracy" to (accuracy?.toString() ?: "unknown"),
                "timestamp" to currentTime.toString()
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "location_update_error")
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentLocation(userId: String): Result<CurrentLocationData?> {
        return try {
            val snapshot = locationsRef.child(userId).get().await()
            
            if (snapshot.exists()) {
                val data = snapshot.value as? Map<String, Any>
                val locationData = CurrentLocationData(
                    latitude = data?.get("latitude") as? Double ?: 0.0,
                    longitude = data?.get("longitude") as? Double ?: 0.0,
                    accuracy = (data?.get("accuracy") as? Number)?.toFloat(),
                    timestamp = data?.get("timestamp") as? Long ?: 0L,
                    lastUpdated = data?.get("lastUpdated") as? Long ?: 0L
                )
                Result.success(locationData)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            analyticsManager.logError(e, "get_current_location_error")
            Result.failure(e)
        }
    }
    
    // === LISTENERS EM TEMPO REAL ===
    
    fun observeUserOnlineStatus(userId: String): Flow<OnlineStatus?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val data = snapshot.value as? Map<String, Any>
                        val onlineStatus = OnlineStatus(
                            online = data?.get("online") as? Boolean ?: false,
                            lastActivity = data?.get("lastActivity") as? Long ?: 0L,
                            timestamp = data?.get("timestamp") as? Long ?: 0L
                        )
                        trySend(onlineStatus)
                    } else {
                        trySend(null)
                    }
                } catch (e: Exception) {
                    close(e)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        val reference = onlineUsersRef.child(userId)
        reference.addValueEventListener(listener)
        
        awaitClose {
            reference.removeEventListener(listener)
        }
    }
    
    fun observeCurrentLocation(userId: String): Flow<CurrentLocationData?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val data = snapshot.value as? Map<String, Any>
                        val locationData = CurrentLocationData(
                            latitude = data?.get("latitude") as? Double ?: 0.0,
                            longitude = data?.get("longitude") as? Double ?: 0.0,
                            accuracy = (data?.get("accuracy") as? Number)?.toFloat(),
                            timestamp = data?.get("timestamp") as? Long ?: 0L,
                            lastUpdated = data?.get("lastUpdated") as? Long ?: 0L
                        )
                        trySend(locationData)
                    } else {
                        trySend(null)
                    }
                } catch (e: Exception) {
                    close(e)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        val reference = locationsRef.child(userId)
        reference.addValueEventListener(listener)
        
        awaitClose {
            reference.removeEventListener(listener)
        }
    }
    
    // === BUSCA POR PROXIMIDADE ===
    
    suspend fun findNearbyUsers(
        currentLatitude: Double,
        currentLongitude: Double,
        radiusKm: Double,
        excludeUserId: String
    ): Result<List<NearbyUser>> {
        return try {
            val snapshot = locationsRef.get().await()
            val nearbyUsers = mutableListOf<NearbyUser>()
            
            snapshot.children.forEach { userSnapshot ->
                val userId = userSnapshot.key ?: return@forEach
                if (userId == excludeUserId) return@forEach
                
                val data = userSnapshot.value as? Map<String, Any>
                val latitude = data?.get("latitude") as? Double ?: return@forEach
                val longitude = data?.get("longitude") as? Double ?: return@forEach
                val timestamp = data?.get("timestamp") as? Long ?: 0L
                
                // Calcular distância
                val distance = calculateDistance(currentLatitude, currentLongitude, latitude, longitude)
                
                if (distance <= radiusKm) {
                    nearbyUsers.add(
                        NearbyUser(
                            userId = userId,
                            latitude = latitude,
                            longitude = longitude,
                            distance = distance,
                            lastUpdated = timestamp
                        )
                    )
                }
            }
            
            // Ordenar por distância
            nearbyUsers.sortBy { it.distance }
            
            // Analytics
            analyticsManager.logCustomCrash("nearby_users_search", mapOf(
                "radius_km" to radiusKm.toString(),
                "users_found" to nearbyUsers.size.toString(),
                "current_lat" to currentLatitude.toString(),
                "current_lng" to currentLongitude.toString()
            ))
            
            Result.success(nearbyUsers)
        } catch (e: Exception) {
            analyticsManager.logError(e, "nearby_users_search_error")
            Result.failure(e)
        }
    }
    
    // === UTILIDADES ===
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Raio da Terra em km
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    suspend fun clearUserLocationData(userId: String): Result<Unit> {
        return try {
            // Remover dados de localização
            locationsRef.child(userId).removeValue().await()
            onlineUsersRef.child(userId).removeValue().await()
            lastSeenRef.child(userId).removeValue().await()
            
            // Analytics
            analyticsManager.logCustomCrash("user_location_data_cleared", mapOf("user_id" to userId))
            
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsManager.logError(e, "clear_location_data_error")
            Result.failure(e)
        }
    }
}

// === MODELOS DE DADOS ===

data class CurrentLocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float? = null,
    val timestamp: Long = 0L,
    val lastUpdated: Long = 0L
)

data class OnlineStatus(
    val online: Boolean = false,
    val lastActivity: Long = 0L,
    val timestamp: Long = 0L
) {
    fun isRecentlyActive(thresholdMinutes: Int = 5): Boolean {
        val currentTime = System.currentTimeMillis()
        val threshold = thresholdMinutes * 60 * 1000 // Converter para milliseconds
        return (currentTime - lastActivity) <= threshold
    }
    
    fun getLastSeenText(): String {
        val currentTime = System.currentTimeMillis()
        val diffMinutes = (currentTime - lastActivity) / (1000 * 60)
        
        return when {
            online -> "Online agora"
            diffMinutes < 1 -> "Visto agora"
            diffMinutes < 60 -> "Visto há ${diffMinutes}min"
            diffMinutes < 1440 -> "Visto há ${diffMinutes / 60}h"
            else -> "Visto há ${diffMinutes / 1440}d"
        }
    }
}

data class NearbyUser(
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distance: Double = 0.0, // em km
    val lastUpdated: Long = 0L
) 