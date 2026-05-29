package com.ideiassertiva.FypMatch.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val analytics = FirebaseAnalytics.getInstance(context)

    fun logScreenView(screenName: String, screenClass: String = "") {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logSwipeAction(direction: String, targetUserId: String, compatibilityScore: Int = 0) {
        val bundle = Bundle().apply {
            putString("swipe_direction", direction)
            putString("target_user_id", targetUserId)
            putInt("compatibility_score", compatibilityScore)
        }
        analytics.logEvent("swipe_action", bundle)
    }

    fun logMatchEvent(matchedUserId: String, matchType: String = "mutual_like", compatibilityScore: Int = 0) {
        val bundle = Bundle().apply {
            putString("matched_user_id", matchedUserId)
            putString("match_type", matchType)
            putInt("compatibility_score", compatibilityScore)
        }
        analytics.logEvent("match_event", bundle)
    }

    fun logUpgradeAttempt(feature: String) {
        val bundle = Bundle().apply { putString("feature", feature) }
        analytics.logEvent("upgrade_attempt", bundle)
    }

    fun logUpgradeStart(source: String = "discovery") {
        val bundle = Bundle().apply { putString("source", source) }
        analytics.logEvent("upgrade_start", bundle)
    }

    fun logCreditUsed(remainingCredits: Int) {
        val bundle = Bundle().apply { putInt("remaining_credits", remainingCredits) }
        analytics.logEvent("credit_used", bundle)
    }

    fun logAdWatched(creditsEarned: Int = 3) {
        val bundle = Bundle().apply { putInt("credits_earned", creditsEarned) }
        analytics.logEvent("ad_watched", bundle)
    }

    fun logLogin(method: String = "google") {
        val bundle = Bundle().apply { putString(FirebaseAnalytics.Param.METHOD, method) }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logShareCode(codeType: String = "waitlist") {
        val bundle = Bundle().apply { putString(FirebaseAnalytics.Param.CONTENT_TYPE, codeType) }
        analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    fun setUserProperty(key: String, value: String) { analytics.setUserProperty(key, value) }

    fun setUserId(userId: String) { analytics.setUserId(userId) }
}

enum class AnalyticsEvent(val eventName: String) {
    SWIPE_ACTION("swipe_action"),
    MATCH_EVENT("match_event"),
    UPGRADE_ATTEMPT("upgrade_attempt"),
    UPGRADE_START("upgrade_start"),
    CREDIT_USED("credit_used"),
    AD_WATCHED("ad_watched"),
    LOGIN("login"),
    SHARE("share"),
    SCREEN_VIEW(FirebaseAnalytics.Event.SCREEN_VIEW)
}

object ScreenNames {
    const val WELCOME = "Welcome"
    const val LOGIN = "Login"
    const val WAITLIST = "Waitlist"
    const val ACCESS_CODE = "AccessCode"
    const val DISCOVERY = "Discovery"
    const val MATCHES = "Matches"
    const val PREMIUM = "Premium"
    const val PROFILE = "Profile"
    const val PROFILE_EDIT = "ProfileEdit"
    const val USER_DETAILS = "UserDetails"
    const val CHAT = "Chat"
    const val AI_COUNSELOR = "AICounselor"
    const val POLICIES = "Policies"
    const val REPORT = "Report"
    const val SETTINGS = "Settings"
}