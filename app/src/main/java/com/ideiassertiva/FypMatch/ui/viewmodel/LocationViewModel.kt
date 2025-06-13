package com.ideiassertiva.FypMatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideiassertiva.FypMatch.data.repository.LocationRepository
import com.ideiassertiva.FypMatch.data.repository.NearbyUser
import com.ideiassertiva.FypMatch.data.repository.CurrentLocationData
import com.ideiassertiva.FypMatch.util.LocationManager
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val locationManager: LocationManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    // Estados da UI
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()
    
    // Localização atual do usuário
    private val _currentLocation = MutableStateFlow<CurrentLocationData?>(null)
    val currentLocation: StateFlow<CurrentLocationData?> = _currentLocation.asStateFlow()
    
    // Usuários próximos
    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()
    
    // Cache de distâncias calculadas
    private val distanceCache = mutableMapOf<String, Double>()

    init {
        observeCurrentUserLocation()
        checkPermissions()
    }

    // === OBSERVAÇÃO DE LOCALIZAÇÃO ===
    
    private fun observeCurrentUserLocation() {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            locationRepository.observeCurrentLocation(currentUser.uid)
                .collect { location ->
                    _currentLocation.value = location
                    
                    if (location != null) {
                        _uiState.value = _uiState.value.copy(
                            hasLocation = true,
                            lastLocationUpdate = System.currentTimeMillis()
                        )
                        
                        // Buscar usuários próximos quando localização atualizar
                        searchNearbyUsers()
                    }
                }
        }
    }
    
    // === GERENCIAMENTO DE PERMISSÕES ===
    
    fun checkPermissions() {
        val hasLocationPermissions = locationManager.hasLocationPermissions()
        val hasNotificationPermissions = locationManager.hasNotificationPermissions()
        val isLocationEnabled = locationManager.isLocationEnabled()
        
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = hasLocationPermissions,
            hasNotificationPermission = hasNotificationPermissions,
            isLocationEnabled = isLocationEnabled,
            permissionStatusText = locationManager.getPermissionStatusText()
        )
        
        println("🔍 DEBUG - Status das permissões: ${locationManager.getPermissionStatusText()}")
    }
    
    fun requestPermissions(activity: android.app.Activity) {
        _uiState.value = _uiState.value.copy(isRequestingPermissions = true)
        
        locationManager.requestAllPermissions(activity)
        
        // Analytics
        analyticsManager.logCustomCrash("location_permissions_requested_from_vm", emptyMap())
    }
    
    fun requestLocationPermission() {
        _uiState.value = _uiState.value.copy(
            isRequestingPermissions = true,
            errorMessage = "Para ver usuários próximos, ative as permissões de localização nas configurações do app"
        )
        
        // Analytics
        analyticsManager.logCustomCrash("location_permission_request_initiated", emptyMap())
        
        // Verificar permissões novamente após um tempo
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            checkPermissions()
            _uiState.value = _uiState.value.copy(isRequestingPermissions = false)
        }
    }
    
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val result = locationManager.handlePermissionResult(requestCode, permissions, grantResults)
        
        _uiState.value = _uiState.value.copy(isRequestingPermissions = false)
        
        when (result) {
            com.ideiassertiva.FypMatch.util.PermissionResult.GRANTED -> {
                checkPermissions()
                startLocationService()
            }
            com.ideiassertiva.FypMatch.util.PermissionResult.DENIED -> {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Permissões de localização são necessárias para encontrar matches próximos"
                )
            }
            else -> {
                // Ignorar resultados desconhecidos
            }
        }
    }
    
    // === CONTROLE DO SERVIÇO ===
    
    fun startLocationService() {
        if (!locationManager.hasAllRequiredPermissions()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Permissões insuficientes para iniciar localização"
            )
            return
        }
        
        val success = locationManager.startLocationService()
        
        _uiState.value = _uiState.value.copy(
            isLocationServiceRunning = success,
            errorMessage = if (!success) "Erro ao iniciar serviço de localização" else null
        )
        
        if (success) {
            println("🔍 DEBUG - Serviço de localização iniciado via ViewModel")
        }
    }
    
    fun stopLocationService() {
        locationManager.stopLocationService()
        
        _uiState.value = _uiState.value.copy(
            isLocationServiceRunning = false
        )
        
        println("🔍 DEBUG - Serviço de localização parado via ViewModel")
    }
    
    // === BUSCA DE USUÁRIOS PRÓXIMOS ===
    
    fun searchNearbyUsers(radiusKm: Double = 50.0) {
        val currentUser = auth.currentUser ?: return
        val location = _currentLocation.value ?: return
        
        _uiState.value = _uiState.value.copy(isSearchingNearby = true)
        
        viewModelScope.launch {
            try {
                val result = locationRepository.findNearbyUsers(
                    currentLatitude = location.latitude,
                    currentLongitude = location.longitude,
                    radiusKm = radiusKm,
                    excludeUserId = currentUser.uid
                )
                
                result.fold(
                    onSuccess = { users ->
                        _nearbyUsers.value = users
                        _uiState.value = _uiState.value.copy(
                            isSearchingNearby = false,
                            nearbyUsersCount = users.size,
                            lastNearbySearch = System.currentTimeMillis()
                        )
                        
                        println("🔍 DEBUG - ${users.size} usuários próximos encontrados")
                        
                        // Analytics
                        analyticsManager.logCustomCrash("nearby_users_search_completed", mapOf(
                            "users_found" to users.size.toString(),
                            "radius_km" to radiusKm.toString()
                        ))
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSearchingNearby = false,
                            errorMessage = "Erro ao buscar usuários próximos: ${error.message}"
                        )
                        
                        println("🔍 DEBUG - Erro na busca de usuários próximos: ${error.message}")
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearchingNearby = false,
                    errorMessage = "Erro inesperado na busca: ${e.message}"
                )
                
                analyticsManager.logError(e, "nearby_users_search_error")
            }
        }
    }
    
    // === CÁLCULO DE DISTÂNCIAS ===
    
    suspend fun calculateDistanceToUser(userId: String): Double? {
        // Verificar cache primeiro
        distanceCache[userId]?.let { cachedDistance ->
            return cachedDistance
        }
        
        val currentUser = auth.currentUser ?: return null
        
        return try {
            val result = locationRepository.calculateDistanceBetweenUsers(currentUser.uid, userId)
            val distance = result.getOrNull()
            
            // Armazenar no cache se válido
            distance?.let { distanceCache[userId] = it }
            
            distance
        } catch (e: Exception) {
            println("🔍 DEBUG - Erro ao calcular distância para $userId: ${e.message}")
            null
        }
    }
    
    fun formatDistance(distanceKm: Double): String {
        return locationManager.formatDistance(distanceKm)
    }
    
    fun getDistanceEmoji(distanceKm: Double): String {
        return locationManager.getDistanceEmoji(distanceKm)
    }
    
    // === UTILITÁRIOS ===
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearDistanceCache() {
        distanceCache.clear()
        println("🔍 DEBUG - Cache de distâncias limpo")
    }
    
    fun refreshLocation() {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                // Forçar atualização da localização
                val result = locationRepository.getCurrentLocation(currentUser.uid)
                result.getOrNull()?.let { location ->
                    _currentLocation.value = location
                    searchNearbyUsers()
                }
            } catch (e: Exception) {
                println("🔍 DEBUG - Erro ao atualizar localização: ${e.message}")
            }
        }
    }
    
    // === ANALYTICS ===
    
    fun logLocationEvent(event: String, params: Map<String, String> = emptyMap()) {
        analyticsManager.logCustomCrash("location_$event", params)
    }
}

data class LocationUiState(
    val hasLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isLocationServiceRunning: Boolean = false,
    val isRequestingPermissions: Boolean = false,
    val isSearchingNearby: Boolean = false,
    val hasLocation: Boolean = false,
    val nearbyUsersCount: Int = 0,
    val lastLocationUpdate: Long = 0L,
    val lastNearbySearch: Long = 0L,
    val permissionStatusText: String = "",
    val errorMessage: String? = null
) {
    val canUseLocation: Boolean
        get() = hasLocationPermission && isLocationEnabled
    
    val isFullyConfigured: Boolean
        get() = hasLocationPermission && hasNotificationPermission && isLocationEnabled
    
    val statusText: String
        get() = when {
            !hasLocationPermission -> "Permissão de localização necessária"
            !isLocationEnabled -> "GPS desabilitado"
            !hasLocation -> "Obtendo localização..."
            isSearchingNearby -> "Buscando usuários próximos..."
            else -> "Localização ativa"
        }
} 