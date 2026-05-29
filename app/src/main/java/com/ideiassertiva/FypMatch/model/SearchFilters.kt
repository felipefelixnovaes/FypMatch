package com.ideiassertiva.FypMatch.model

data class SearchFilters(
    val ageRange: IntRange = 18..55,
    val maxDistance: Int = 50,
    val verifiedOnly: Boolean = false,
    val recentlyActive: Boolean = false,
    val minPhotos: Int = 1,
    val heightRange: IntRange? = null,
    val smokingStatus: List<SmokingStatus> = emptyList(),
    val drinkingStatus: List<DrinkingStatus> = emptyList(),
    val hasChildren: List<ChildrenStatus> = emptyList(),
    val wantsChildren: List<ChildrenStatus> = emptyList(),
    val religions: List<Religion> = emptyList()
) {
    fun countActive(): Int {
        var count = 0
        if (verifiedOnly) count++
        if (recentlyActive) count++
        if (smokingStatus.isNotEmpty()) count++
        if (drinkingStatus.isNotEmpty()) count++
        if (hasChildren.isNotEmpty()) count++
        if (wantsChildren.isNotEmpty()) count++
        if (religions.isNotEmpty()) count++
        return count
    }
}