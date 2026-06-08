package com.ideiassertiva.FypMatch.data

import com.ideiassertiva.FypMatch.model.Gender
import com.ideiassertiva.FypMatch.model.Intention
import com.ideiassertiva.FypMatch.model.Location
import com.ideiassertiva.FypMatch.model.SearchFilters
import com.ideiassertiva.FypMatch.model.SubscriptionStatus
import com.ideiassertiva.FypMatch.model.User
import com.ideiassertiva.FypMatch.model.UserProfile
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscoveryFiltersTest {

    @Test
    fun `age and distance filters remove incompatible cards`() {
        val currentUser = user(
            id = "current",
            age = 32,
            location = Location(city = "Sao Paulo", state = "SP")
        )
        val nearby = user(
            id = "nearby",
            age = 30,
            location = Location(city = "Santos", state = "SP")
        )
        val tooFar = user(
            id = "far",
            age = 30,
            location = Location(city = "Recife", state = "PE")
        )
        val tooYoung = user(
            id = "young",
            age = 22,
            location = Location(city = "Sao Paulo", state = "SP")
        )

        val filters = SearchFilters(ageRange = 28..35, maxDistance = 100)

        assertTrue(nearby.matchesDiscoveryFilters(filters, currentUser))
        assertFalse(tooFar.matchesDiscoveryFilters(filters, currentUser))
        assertFalse(tooYoung.matchesDiscoveryFilters(filters, currentUser))
    }

    @Test
    fun `travel mode uses destination instead of current city`() {
        val currentUser = user(
            id = "current",
            age = 32,
            location = Location(city = "Sao Paulo", state = "SP")
        )
        val recifeUser = user(
            id = "recife",
            age = 31,
            location = Location(city = "Recife", state = "PE")
        )

        val localFilters = SearchFilters(maxDistance = 50)
        val travelFilters = SearchFilters(
            maxDistance = 50,
            travelModeEnabled = true,
            travelLocation = Location(city = "Recife", state = "PE")
        )

        assertFalse(recifeUser.matchesDiscoveryFilters(localFilters, currentUser))
        assertTrue(recifeUser.matchesDiscoveryFilters(travelFilters, currentUser))
    }

    @Test
    fun `lifestyle filters match target profile fields`() {
        val currentUser = user("current", age = 32)
        val target = user(
            id = "target",
            age = 30,
            subscription = SubscriptionStatus.PREMIUM,
            profile = UserProfile(
                fullName = "Target",
                age = 30,
                photos = listOf("https://example.com/1.jpg", "https://example.com/2.jpg"),
                location = Location(city = "Sao Paulo", state = "SP"),
                gender = Gender.FEMALE,
                intention = Intention.DATING,
                height = 170
            )
        )

        val filters = SearchFilters(
            genderPreference = listOf(Gender.FEMALE),
            intentionPreference = listOf(Intention.DATING),
            verifiedOnly = true,
            minPhotos = 2,
            heightRange = 165..180
        )

        assertTrue(target.matchesDiscoveryFilters(filters, currentUser))
    }

    private fun user(
        id: String,
        age: Int,
        location: Location = Location(city = "Sao Paulo", state = "SP"),
        subscription: SubscriptionStatus = SubscriptionStatus.FREE,
        profile: UserProfile = UserProfile(
            fullName = id,
            age = age,
            location = location,
            gender = Gender.NOT_SPECIFIED,
            intention = Intention.NOT_SPECIFIED,
            photos = listOf("https://example.com/photo.jpg")
        )
    ): User {
        return User(
            id = id,
            profile = profile,
            subscription = subscription,
            lastActive = Date()
        )
    }
}
