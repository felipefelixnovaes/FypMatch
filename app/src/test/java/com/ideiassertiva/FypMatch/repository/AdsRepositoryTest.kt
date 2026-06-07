package com.ideiassertiva.FypMatch.repository

import com.ideiassertiva.FypMatch.data.repository.AdsRepository
import com.ideiassertiva.FypMatch.model.AiCreditLimits
import com.ideiassertiva.FypMatch.model.SubscriptionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdsRepositoryTest {

    @Test
    fun `rewarded ad adds credits and updates daily stats`() = runTest {
        val repository = AdsRepository()
        val userId = "user-ads"

        repository.initializeUserCredits(userId, SubscriptionStatus.FREE)
        val result = repository.showRewardedAd(userId)

        assertTrue(result.isSuccess)
        assertEquals(AiCreditLimits.AD_REWARD, result.getOrThrow())
        assertEquals(AiCreditLimits.AD_REWARD, repository.getUserCredits(userId).current)
        assertEquals(1, repository.getAdStats(userId).adsWatchedToday)
        assertEquals(AiCreditLimits.AD_REWARD, repository.getAdStats(userId).creditsEarnedToday)
    }

    @Test
    fun `rewarded ads stop after daily limit`() = runTest {
        val repository = AdsRepository()
        val userId = "user-daily-limit"
        val maxAds = AiCreditLimits.MAX_AD_CREDITS_DAILY / AiCreditLimits.AD_REWARD

        repository.initializeUserCredits(userId, SubscriptionStatus.FREE)
        repeat(maxAds) {
            assertTrue(repository.showRewardedAd(userId).isSuccess)
        }

        val extraAttempt = repository.showRewardedAd(userId)

        assertTrue(extraAttempt.isFailure)
        assertEquals(maxAds, repository.getAdStats(userId).adsWatchedToday)
        assertEquals(AiCreditLimits.MAX_AD_CREDITS_DAILY, repository.getUserCredits(userId).current)
    }
}
