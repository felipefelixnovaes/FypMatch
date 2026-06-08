package com.ideiassertiva.FypMatch.data

import com.ideiassertiva.FypMatch.model.Location
import java.text.Normalizer
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object BrazilLocationCatalog {
    val cities: List<Location> = listOf(
        Location("Sao Paulo", "SP", latitude = -23.5505, longitude = -46.6333),
        Location("Rio de Janeiro", "RJ", latitude = -22.9068, longitude = -43.1729),
        Location("Belo Horizonte", "MG", latitude = -19.9167, longitude = -43.9345),
        Location("Brasilia", "DF", latitude = -15.7939, longitude = -47.8828),
        Location("Salvador", "BA", latitude = -12.9777, longitude = -38.5016),
        Location("Fortaleza", "CE", latitude = -3.7319, longitude = -38.5267),
        Location("Curitiba", "PR", latitude = -25.4284, longitude = -49.2733),
        Location("Recife", "PE", latitude = -8.0476, longitude = -34.8770),
        Location("Porto Alegre", "RS", latitude = -30.0346, longitude = -51.2177),
        Location("Manaus", "AM", latitude = -3.1190, longitude = -60.0217),
        Location("Belem", "PA", latitude = -1.4558, longitude = -48.4902),
        Location("Goiania", "GO", latitude = -16.6869, longitude = -49.2648),
        Location("Campinas", "SP", latitude = -22.9056, longitude = -47.0608),
        Location("Santos", "SP", latitude = -23.9608, longitude = -46.3336),
        Location("Guarulhos", "SP", latitude = -23.4545, longitude = -46.5333),
        Location("Osasco", "SP", latitude = -23.5329, longitude = -46.7918),
        Location("Santo Andre", "SP", latitude = -23.6639, longitude = -46.5383),
        Location("Sao Bernardo do Campo", "SP", latitude = -23.6914, longitude = -46.5646),
        Location("Barueri", "SP", latitude = -23.5112, longitude = -46.8764),
        Location("Sorocaba", "SP", latitude = -23.5015, longitude = -47.4526),
        Location("Ribeirao Preto", "SP", latitude = -21.1704, longitude = -47.8103),
        Location("Florianopolis", "SC", latitude = -27.5949, longitude = -48.5482),
        Location("Joinville", "SC", latitude = -26.3045, longitude = -48.8487),
        Location("Balneario Camboriu", "SC", latitude = -26.9926, longitude = -48.6352),
        Location("Vitoria", "ES", latitude = -20.2976, longitude = -40.2958),
        Location("Vila Velha", "ES", latitude = -20.3478, longitude = -40.2949),
        Location("Niteroi", "RJ", latitude = -22.8832, longitude = -43.1034),
        Location("Petropolis", "RJ", latitude = -22.5050, longitude = -43.1786),
        Location("Juiz de Fora", "MG", latitude = -21.7622, longitude = -43.3434),
        Location("Uberlandia", "MG", latitude = -18.9128, longitude = -48.2755),
        Location("Campo Grande", "MS", latitude = -20.4697, longitude = -54.6201),
        Location("Cuiaba", "MT", latitude = -15.6014, longitude = -56.0979),
        Location("Maceio", "AL", latitude = -9.6498, longitude = -35.7089),
        Location("Natal", "RN", latitude = -5.7793, longitude = -35.2009),
        Location("Joao Pessoa", "PB", latitude = -7.1195, longitude = -34.8450),
        Location("Aracaju", "SE", latitude = -10.9472, longitude = -37.0731),
        Location("Teresina", "PI", latitude = -5.0892, longitude = -42.8016),
        Location("Sao Luis", "MA", latitude = -2.5307, longitude = -44.3068),
        Location("Palmas", "TO", latitude = -10.1849, longitude = -48.3336),
        Location("Porto Velho", "RO", latitude = -8.7612, longitude = -63.9004),
        Location("Boa Vista", "RR", latitude = 2.8235, longitude = -60.6758),
        Location("Rio Branco", "AC", latitude = -9.9740, longitude = -67.8243),
        Location("Macapa", "AP", latitude = 0.0349, longitude = -51.0694)
    )

    fun suggestions(query: String, limit: Int = 8): List<Location> {
        val normalized = query.normalizedKey()
        if (normalized.length < 2) return cities.take(limit)
        return cities
            .filter { city ->
                city.city.normalizedKey().contains(normalized) ||
                    city.state.normalizedKey().contains(normalized)
            }
            .sortedWith(
                compareBy<Location> { !it.city.normalizedKey().startsWith(normalized) }
                    .thenBy { it.city }
            )
            .take(limit)
    }

    fun resolve(location: Location): Location {
        if (location.latitude != 0.0 || location.longitude != 0.0) return location
        val cityKey = location.city.normalizedKey()
        val stateKey = location.state.normalizedKey()
        return cities.firstOrNull {
            it.city.normalizedKey() == cityKey &&
                (stateKey.isBlank() || it.state.normalizedKey() == stateKey)
        } ?: location
    }

    fun nearest(latitude: Double, longitude: Double): Location {
        return cities.minBy { distanceKm(latitude, longitude, it.latitude, it.longitude) }
    }

    fun distanceKm(from: Location, to: Location): Int? {
        val resolvedFrom = resolve(from)
        val resolvedTo = resolve(to)

        if (resolvedFrom.city.isNotBlank() &&
            resolvedTo.city.isNotBlank() &&
            resolvedFrom.city.normalizedKey() == resolvedTo.city.normalizedKey() &&
            (resolvedFrom.state.isBlank() ||
                resolvedTo.state.isBlank() ||
                resolvedFrom.state.normalizedKey() == resolvedTo.state.normalizedKey())
        ) {
            return 0
        }

        if (!resolvedFrom.hasCoordinates() || !resolvedTo.hasCoordinates()) return null

        return distanceKm(
            resolvedFrom.latitude,
            resolvedFrom.longitude,
            resolvedTo.latitude,
            resolvedTo.longitude
        ).roundToInt()
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
        return radiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

fun Location.displayName(): String {
    return listOf(city, state)
        .filter { it.isNotBlank() }
        .joinToString(", ")
}

fun Location.hasCoordinates(): Boolean = latitude != 0.0 || longitude != 0.0

private fun String.normalizedKey(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase()
        .trim()
}
