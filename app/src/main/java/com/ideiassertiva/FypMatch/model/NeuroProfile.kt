package com.ideiassertiva.FypMatch.model

data class NeuroProfile(
    val preferences: NeuroPreferences = NeuroPreferences(),
    val accommodations: List<Accommodation> = emptyList()
)

data class NeuroPreferences(
    val needsClearCommunication: Boolean = false,
    val prefersDirectness: Boolean = false,
    val sensitiveToCriticism: Boolean = false,
    val needsRoutine: Boolean = false,
    val prefersTextOverVoice: Boolean = false
)

data class Accommodation(
    val type: AccommodationType = AccommodationType.SIMPLIFIED_INTERFACE,
    val description: String = ""
)

enum class AccommodationType {
    SIMPLIFIED_INTERFACE, REDUCED_STIMULATION, CLEAR_INSTRUCTIONS, SENSORY_FILTERING
}