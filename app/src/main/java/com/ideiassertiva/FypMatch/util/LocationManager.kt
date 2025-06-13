package com.ideiassertiva.FypMatch.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager as SystemLocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ideiassertiva.FypMatch.service.LocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsManager: AnalyticsManager
) {
    
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        
        private val REQUIRED_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        private val REQUIRED_NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
    
    private var isServiceRunning = false
    
    // === VERIFICAÇÃO DE PERMISSÕES ===
    
    fun hasLocationPermissions(): Boolean {
        return REQUIRED_LOCATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasNotificationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_NOTIFICATION_PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            true // Versões antigas não precisam de permissão explícita
        }
    }
    
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as SystemLocationManager
        return locationManager.isProviderEnabled(SystemLocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(SystemLocationManager.NETWORK_PROVIDER)
    }
    
    fun hasAllRequiredPermissions(): Boolean {
        return hasLocationPermissions() && hasNotificationPermissions() && isLocationEnabled()
    }
    
    // === SOLICITAÇÃO DE PERMISSÕES ===
    
    fun requestLocationPermissions(activity: Activity) {
        println("🔍 DEBUG - Solicitando permissões de localização")
        
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_LOCATION_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
        
        // Analytics
        analyticsManager.logCustomCrash("location_permission_requested", emptyMap())
    }
    
    fun requestNotificationPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            println("🔍 DEBUG - Solicitando permissões de notificação")
            
            ActivityCompat.requestPermissions(
                activity,
                REQUIRED_NOTIFICATION_PERMISSIONS,
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            
            // Analytics
            analyticsManager.logCustomCrash("notification_permission_requested", emptyMap())
        }
    }
    
    fun requestAllPermissions(activity: Activity) {
        val permissionsToRequest = mutableListOf<String>()
        
        // Adicionar permissões de localização se necessário
        if (!hasLocationPermissions()) {
            permissionsToRequest.addAll(REQUIRED_LOCATION_PERMISSIONS)
        }
        
        // Adicionar permissões de notificação se necessário
        if (!hasNotificationPermissions()) {
            permissionsToRequest.addAll(REQUIRED_NOTIFICATION_PERMISSIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            println("🔍 DEBUG - Solicitando ${permissionsToRequest.size} permissões")
            
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    // === CONTROLE DO SERVIÇO ===
    
    fun startLocationService(): Boolean {
        if (!hasAllRequiredPermissions()) {
            println("🔍 DEBUG - Não é possível iniciar serviço: permissões insuficientes")
            return false
        }
        
        if (isServiceRunning) {
            println("🔍 DEBUG - Serviço de localização já está rodando")
            return true
        }
        
        try {
            val intent = Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_START_LOCATION_UPDATES
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            isServiceRunning = true
            
            println("🔍 DEBUG - Serviço de localização iniciado")
            
            // Analytics
            analyticsManager.logCustomCrash("location_service_start_requested", emptyMap())
            
            return true
            
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao iniciar serviço de localização: ${e.message}")
            analyticsManager.logError(e, "location_service_start_error")
            return false
        }
    }
    
    fun stopLocationService() {
        if (!isServiceRunning) {
            println("🔍 DEBUG - Serviço de localização já está parado")
            return
        }
        
        try {
            val intent = Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP_LOCATION_UPDATES
            }
            
            context.startService(intent)
            isServiceRunning = false
            
            println("🔍 DEBUG - Serviço de localização parado")
            
            // Analytics
            analyticsManager.logCustomCrash("location_service_stop_requested", emptyMap())
            
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao parar serviço de localização: ${e.message}")
            analyticsManager.logError(e, "location_service_stop_error")
        }
    }
    
    // === UTILITÁRIOS ===
    
    fun getPermissionStatusText(): String {
        val status = mutableListOf<String>()
        
        if (!hasLocationPermissions()) {
            status.add("Localização: ❌")
        } else {
            status.add("Localização: ✅")
        }
        
        if (!hasNotificationPermissions()) {
            status.add("Notificações: ❌")
        } else {
            status.add("Notificações: ✅")
        }
        
        if (!isLocationEnabled()) {
            status.add("GPS: ❌")
        } else {
            status.add("GPS: ✅")
        }
        
        return status.joinToString(", ")
    }
    
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): PermissionResult {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                
                return if (allGranted) {
                    println("🔍 DEBUG - Permissões de localização concedidas")
                    analyticsManager.logCustomCrash("location_permission_granted", emptyMap())
                    PermissionResult.GRANTED
                } else {
                    println("🔍 DEBUG - Permissões de localização negadas")
                    analyticsManager.logCustomCrash("location_permission_denied", emptyMap())
                    PermissionResult.DENIED
                }
            }
            
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                
                return if (allGranted) {
                    println("🔍 DEBUG - Permissões de notificação concedidas")
                    analyticsManager.logCustomCrash("notification_permission_granted", emptyMap())
                    PermissionResult.GRANTED
                } else {
                    println("🔍 DEBUG - Permissões de notificação negadas")
                    analyticsManager.logCustomCrash("notification_permission_denied", emptyMap())
                    PermissionResult.DENIED
                }
            }
            
            else -> return PermissionResult.UNKNOWN
        }
    }
    
    // === FORMATAÇÃO DE DISTÂNCIA ===
    
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 0.1 -> "Muito próximo"
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()}m"
            distanceKm < 10.0 -> String.format("%.1fkm", distanceKm)
            else -> "${distanceKm.toInt()}km"
        }
    }
    
    fun getDistanceEmoji(distanceKm: Double): String {
        return when {
            distanceKm < 0.1 -> "🔥" // Muito próximo
            distanceKm < 0.5 -> "💕" // Bem próximo
            distanceKm < 1.0 -> "❤️" // Próximo
            distanceKm < 5.0 -> "💙" // Perto
            else -> "💜" // Longe
        }
    }
}

enum class PermissionResult {
    GRANTED,
    DENIED,
    UNKNOWN
} 