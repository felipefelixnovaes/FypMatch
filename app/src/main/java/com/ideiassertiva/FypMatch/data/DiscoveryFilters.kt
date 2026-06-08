package com.ideiassertiva.FypMatch.data

import com.ideiassertiva.FypMatch.model.SearchFilters
import com.ideiassertiva.FypMatch.model.SubscriptionStatus
import com.ideiassertiva.FypMatch.model.User
import java.util.Date
import java.util.concurrent.TimeUnit

internal fun User.matchesDiscoveryFilters(
    filters: SearchFilters,
    anchorUser: User?,
    now: Date = Date()
): Boolean {
    if (!filters.ageRange.contains(profile.age)) return false

    if (filters.genderPreference.isNotEmpty() && profile.gender !in filters.genderPreference) {
        return false
    }

    if (filters.intentionPreference.isNotEmpty() && profile.intention !in filters.intentionPreference) {
        return false
    }

    if (filters.verifiedOnly && subscription == SubscriptionStatus.FREE) return false

    if (filters.recentlyActive && now.time - lastActive.time > TimeUnit.HOURS.toMillis(24)) {
        return false
    }

    if (profile.photos.size < filters.minPhotos) return false

    val heightRange = filters.heightRange
    if (heightRange != null && (profile.height <= 0 || profile.height !in heightRange)) {
        return false
    }

    if (filters.smokingStatus.isNotEmpty() && profile.smokingStatus !in filters.smokingStatus) {
        return false
    }

    if (filters.drinkingStatus.isNotEmpty() && profile.drinkingStatus !in filters.drinkingStatus) {
        return false
    }

    if (filters.hasChildren.isNotEmpty() && profile.hasChildren !in filters.hasChildren) {
        return false
    }

    if (filters.wantsChildren.isNotEmpty() && profile.wantsChildren !in filters.wantsChildren) {
        return false
    }

    if (filters.religions.isNotEmpty() && profile.religion !in filters.religions) {
        return false
    }

    val anchor = filters.anchorLocation(anchorUser?.profile?.location ?: return true)
    val distanceKm = BrazilLocationCatalog.distanceKm(anchor, profile.location)
    return distanceKm == null || distanceKm <= filters.maxDistance
}

internal fun discoveryDistanceKm(anchorUser: User?, filters: SearchFilters, target: User): Int {
    val anchor = filters.anchorLocation(anchorUser?.profile?.location ?: return 0)
    return BrazilLocationCatalog.distanceKm(anchor, target.profile.location) ?: 0
}
