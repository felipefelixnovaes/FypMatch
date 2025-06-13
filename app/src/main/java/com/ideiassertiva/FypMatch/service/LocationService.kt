package com.ideiassertiva.FypMatch.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.ideiassertiva.FypMatch.MainActivity
import com.ideiassertiva.FypMatch.R
import com.ideiassertiva.FypMatch.data.repository.LocationRepository
import com.ideiassertiva.FypMatch.data.repository.UserRepository
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository
    
    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val auth = FirebaseAuth.getInstance()
    
    private var isLocationUpdatesActive = false
    private var lastKnownLocation: Location? = null
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "location_service_channel"
        const val ACTION_START_LOCATION_UPDATES = "START_LOCATION_UPDATES"
        const val ACTION_STOP_LOCATION_UPDATES = "STOP_LOCATION_UPDATES"
        
        // Configurações de localização
        const val LOCATION_UPDATE_INTERVAL = 30000L // 30 segundos
        const val LOCATION_FASTEST_INTERVAL = 15000L // 15 segundos
        const val PROXIMITY_THRESHOLD_KM = 1.0 // 1km para notificações
    }

    override fun onCreate() {
        super.onCreate()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationRequest()
        setupLocationCallback()
        
        println("🔍 DEBUG - LocationService criado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_LOCATION_UPDATES -> {
                startLocationUpdates()
            }
            ACTION_STOP_LOCATION_UPDATES -> {
                stopLocationUpdates()
                stopSelf()
            }
        }
        
        return START_STICKY // Reiniciar se o sistema matar o serviço
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Localização FypMatch",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Serviço de localização para encontrar matches próximos"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
        }.build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (isLocationUpdatesActive) return
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            println("🔍 DEBUG - Usuário não autenticado, parando serviço")
            stopSelf()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("🔍 DEBUG - Permissão de localização não concedida")
            stopSelf()
            return
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            isLocationUpdatesActive = true
            startForeground(NOTIFICATION_ID, createNotification())
            
            println("🔍 DEBUG - Atualizações de localização iniciadas")
            
            // Analytics
            analyticsManager.logCustomCrash("location_service_started", mapOf(
                "user_id" to currentUser.uid
            ))
            
        } catch (e: SecurityException) {
            println("🔍 DEBUG - Erro de segurança ao iniciar localização: ${e.message}")
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        if (!isLocationUpdatesActive) return
        
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isLocationUpdatesActive = false
        
        println("🔍 DEBUG - Atualizações de localização paradas")
        
        // Analytics
        analyticsManager.logCustomCrash("location_service_stopped", emptyMap())
    }

    private fun handleLocationUpdate(location: Location) {
        val currentUser = auth.currentUser ?: return
        
        lastKnownLocation = location
        
        serviceScope.launch {
            try {
                // Atualizar localização no Firebase
                locationRepository.updateCurrentLocation(
                    userId = currentUser.uid,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy
                )
                
                // Verificar usuários próximos para notificações
                checkNearbyUsersForNotifications(location)
                
                println("🔍 DEBUG - Localização atualizada: ${location.latitude}, ${location.longitude}")
                
            } catch (e: Exception) {
                println("🔍 DEBUG - Erro ao atualizar localização: ${e.message}")
                analyticsManager.logError(e, "location_update_error")
            }
        }
    }

    private suspend fun checkNearbyUsersForNotifications(currentLocation: Location) {
        try {
            val currentUser = auth.currentUser ?: return
            
            // Buscar usuários próximos
            val nearbyResult = locationRepository.findNearbyUsers(
                currentLatitude = currentLocation.latitude,
                currentLongitude = currentLocation.longitude,
                radiusKm = PROXIMITY_THRESHOLD_KM,
                excludeUserId = currentUser.uid
            )
            
            nearbyResult.getOrNull()?.let { nearbyUsers ->
                // Filtrar usuários muito próximos (< 1km)
                val veryCloseUsers = nearbyUsers.filter { it.distance < PROXIMITY_THRESHOLD_KM }
                
                if (veryCloseUsers.isNotEmpty()) {
                    println("🔍 DEBUG - ${veryCloseUsers.size} usuários próximos encontrados")
                    
                    // Enviar notificação para cada usuário próximo
                    veryCloseUsers.forEach { nearbyUser ->
                        sendProximityNotification(nearbyUser)
                    }
                    
                    // Analytics
                    analyticsManager.logCustomCrash("nearby_users_detected", mapOf(
                        "count" to veryCloseUsers.size.toString(),
                        "closest_distance" to veryCloseUsers.minOf { it.distance }.toString()
                    ))
                }
            }
            
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao verificar usuários próximos: ${e.message}")
        }
    }

    private suspend fun sendProximityNotification(nearbyUser: com.ideiassertiva.FypMatch.data.repository.NearbyUser) {
        try {
            // Buscar dados do usuário próximo
            val userResult = userRepository.getUserFromFirestore(nearbyUser.userId)
            val user = userResult.getOrNull() ?: return
            
            val distanceText = if (nearbyUser.distance < 0.1) {
                "muito próximo"
            } else {
                "${(nearbyUser.distance * 1000).toInt()}m"
            }
            
            // Criar intent para abrir o app
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_discovery", true)
                putExtra("highlight_user", nearbyUser.userId)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                nearbyUser.userId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Criar notificação
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("💕 Match próximo!")
                .setContentText("${user.profile.fullName} está a $distanceText de você")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("${user.profile.fullName} está a $distanceText de você. Que tal dar um match? 😉"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "Ver Perfil",
                    pendingIntent
                )
                .build()
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(nearbyUser.userId.hashCode(), notification)
            
            println("🔍 DEBUG - Notificação enviada para usuário próximo: ${user.profile.fullName}")
            
            // Analytics
            analyticsManager.logCustomCrash("proximity_notification_sent", mapOf(
                "nearby_user_id" to nearbyUser.userId,
                "distance_km" to nearbyUser.distance.toString()
            ))
            
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao enviar notificação de proximidade: ${e.message}")
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("FypMatch ativo")
        .setContentText("Procurando matches próximos...")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
        
        println("🔍 DEBUG - LocationService destruído")
    }
} 