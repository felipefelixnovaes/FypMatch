package com.ideiassertiva.FypMatch.model

import com.google.firebase.firestore.util.CustomClassMapper
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class UserPreferencesFirestoreTest {

    @Test
    fun `legacy string ageRange is ignored without crashing`() {
        val legacyUserDoc = mapOf(
            "id" to "user-1",
            "email" to "tester@example.com",
            "preferences" to mapOf(
                "ageRange" to "18..99",
                "maxDistance" to 25
            )
        )

        val user = CustomClassMapper.convertToCustomClass(
            legacyUserDoc,
            User::class.java,
            null
        )

        assertEquals("user-1", user.id)
        assertEquals(18..99, user.preferences.ageRange)
        assertEquals(25, user.preferences.maxDistance)
    }

    @Test
    fun `new min and max age fields build ageRange for compatibility checks`() {
        val userDoc = mapOf(
            "id" to "user-2",
            "preferences" to mapOf(
                "minAge" to 30,
                "maxAge" to 45,
                "maxDistance" to 10
            )
        )

        val user = CustomClassMapper.convertToCustomClass(
            userDoc,
            User::class.java,
            null
        )

        assertEquals(30..45, user.preferences.ageRange)
        assertEquals(10, user.preferences.maxDistance)
    }

    @Test
    fun `ageRange is not serialized back to firestore`() {
        val plain = CustomClassMapper.convertToPlainJavaTypes(
            UserPreferences(minAge = 21, maxAge = 35)
        ) as Map<*, *>

        assertEquals(21, plain["minAge"])
        assertEquals(35, plain["maxAge"])
        assertFalse(plain.containsKey("ageRange"))
    }
}
