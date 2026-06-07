package com.ideiassertiva.FypMatch.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserProfileCompletionTest {

    @Test
    fun `profile setup is complete without photos while photo upload is unavailable`() {
        val user = User(
            profile = UserProfile(
                fullName = "Felipe Novaes",
                age = 32,
                bio = "Perfil de teste completo",
                location = Location(city = "Sao Paulo"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.DATING,
                photos = emptyList()
            )
        )

        assertTrue(user.isProfileComplete())
    }

    @Test
    fun `profile remains incomplete when required selection is missing`() {
        val user = User(
            profile = UserProfile(
                fullName = "Felipe Novaes",
                age = 32,
                bio = "Perfil de teste incompleto",
                location = Location(city = "Sao Paulo"),
                gender = Gender.MALE,
                orientation = Orientation.STRAIGHT,
                intention = Intention.NOT_SPECIFIED
            )
        )

        assertFalse(user.isProfileComplete())
    }
}
