package com.ideiassertiva.FypMatch.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.ideiassertiva.FypMatch.model.Location

object DeviceLocationResolver {
    val requiredPermissions: Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasLocationPermission(context: Context): Boolean {
        return requiredPermissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun resolveCurrentCity(context: Context): Result<Location> {
        if (!hasLocationPermission(context)) {
            return Result.failure(Exception("Permita o acesso à localização para usar sua cidade atual."))
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return Result.failure(Exception("Serviço de localização indisponível."))

        val lastLocation = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )
            .filter { provider -> locationManager.isProviderEnabled(provider) }
            .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
            .maxByOrNull { it.time }
            ?: return Result.failure(Exception("Não consegui ler sua localização atual. Ative o GPS e tente novamente."))

        return Result.success(
            BrazilLocationCatalog.nearest(lastLocation.latitude, lastLocation.longitude)
        )
    }
}
