package com.ideiassertiva.FypMatch.model

data class SearchFilters(
    val ageRange: IntRange = DEFAULT_AGE_RANGE,
    val maxDistance: Int = 50,
    val genderPreference: List<Gender> = emptyList(),
    val intentionPreference: List<Intention> = emptyList(),
    val verifiedOnly: Boolean = false,
    val recentlyActive: Boolean = false,
    val minPhotos: Int = DEFAULT_MIN_PHOTOS,
    val heightRange: IntRange? = null,
    val smokingStatus: List<SmokingStatus> = emptyList(),
    val drinkingStatus: List<DrinkingStatus> = emptyList(),
    val hasChildren: List<ChildrenStatus> = emptyList(),
    val wantsChildren: List<ChildrenStatus> = emptyList(),
    val religions: List<Religion> = emptyList(),
    val travelModeEnabled: Boolean = false,
    val travelLocation: Location = Location()
) {
    fun countActive(): Int {
        var count = 0
        if (ageRange != DEFAULT_AGE_RANGE) count++
        if (maxDistance != DEFAULT_MAX_DISTANCE) count++
        if (genderPreference.isNotEmpty()) count++
        if (intentionPreference.isNotEmpty()) count++
        if (verifiedOnly) count++
        if (recentlyActive) count++
        if (minPhotos > DEFAULT_MIN_PHOTOS) count++
        if (heightRange != null) count++
        if (smokingStatus.isNotEmpty()) count++
        if (drinkingStatus.isNotEmpty()) count++
        if (hasChildren.isNotEmpty()) count++
        if (wantsChildren.isNotEmpty()) count++
        if (religions.isNotEmpty()) count++
        if (travelModeEnabled && travelLocation.city.isNotBlank()) count++
        return count
    }

    fun anchorLocation(defaultLocation: Location): Location {
        return if (travelModeEnabled && travelLocation.city.isNotBlank()) {
            travelLocation
        } else {
            defaultLocation
        }
    }

    fun toPreferences(existing: UserPreferences = UserPreferences()): UserPreferences {
        return existing.copy(
            minAge = ageRange.first.coerceIn(18, 99),
            maxAge = ageRange.last.coerceIn(18, 99),
            maxDistance = maxDistance.coerceIn(1, 500),
            genderPreference = genderPreference,
            intentionPreference = intentionPreference,
            onlyVerified = verifiedOnly,
            recentlyActive = recentlyActive,
            minPhotos = minPhotos.coerceIn(0, 6),
            minHeight = heightRange?.first ?: 0,
            maxHeight = heightRange?.last ?: 0,
            smokingStatusPreference = smokingStatus,
            drinkingStatusPreference = drinkingStatus,
            hasChildrenPreference = hasChildren,
            wantsChildrenPreference = wantsChildren,
            religionPreference = religions,
            travelModeEnabled = travelModeEnabled,
            searchLocation = if (travelModeEnabled) travelLocation else Location()
        )
    }

    companion object {
        val DEFAULT_AGE_RANGE: IntRange = 18..99
        const val DEFAULT_MAX_DISTANCE: Int = 50
        const val DEFAULT_MIN_PHOTOS: Int = 0
    }
}

fun UserPreferences.toSearchFilters(): SearchFilters {
    val normalizedMinAge = minAge.coerceIn(18, 99)
    val normalizedMaxAge = maxAge.coerceIn(normalizedMinAge, 99)
    val normalizedHeightRange = if (minHeight > 0 && maxHeight >= minHeight) {
        minHeight..maxHeight
    } else {
        null
    }

    return SearchFilters(
        ageRange = normalizedMinAge..normalizedMaxAge,
        maxDistance = maxDistance.coerceIn(1, 500),
        genderPreference = genderPreference,
        intentionPreference = intentionPreference,
        verifiedOnly = onlyVerified,
        recentlyActive = recentlyActive,
        minPhotos = minPhotos.coerceIn(0, 6),
        heightRange = normalizedHeightRange,
        smokingStatus = smokingStatusPreference,
        drinkingStatus = drinkingStatusPreference,
        hasChildren = hasChildrenPreference,
        wantsChildren = wantsChildrenPreference,
        religions = religionPreference,
        travelModeEnabled = travelModeEnabled,
        travelLocation = searchLocation
    )
}
