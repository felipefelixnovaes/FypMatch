package com.ideiassertiva.FypMatch.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.ideiassertiva.FypMatch.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.crashlytics.ktx.crashlytics
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * 📊 Analytics Manager - FypMatch
 * 
 * Gerencia Firebase Analytics e Crashlytics de forma centralizada
 * Tracking de eventos personalizados para o app de relacionamento
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AnalyticsManager"
    }
    
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    init {
        // Configurar debug mode se estivermos em build debug
        if (BuildConfig.FIREBASE_DEBUG) {
            // Ativar DebugView
            analytics.setDefaultEventParameters(Bundle().apply {
                putString("debug_mode", "true")
            })
            
            Log.d(TAG, "Firebase Analytics Debug Mode ATIVADO")
            Log.d(TAG, "Para ver eventos: Firebase Console > Analytics > DebugView")
            Log.d(TAG, "Ou use: adb shell setprop debug.firebase.analytics.app ${BuildConfig.APPLICATION_ID}")
        }
        
        // Configurar identificadores para debug
        setupDebugIdentifiers()
    }
    
    private fun setupDebugIdentifiers() {
        if (BuildConfig.FIREBASE_DEBUG) {
            // Adicionar propriedades de debug
            analytics.setUserProperty("debug_build", "true")
            analytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            analytics.setUserProperty("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            
            Log.d(TAG, "Propriedades de debug configuradas")
        }
    }
    
    // ===== INICIALIZAÇÃO ===== 
    fun initialize() {
        // Configurar coleta de dados
        analytics.setAnalyticsCollectionEnabled(true)
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Configurar propriedades do usuário padrão
        analytics.setDefaultEventParameters(Bundle().apply {
            putString("app_name", "FypMatch")
            putString("app_version", getAppVersion())
            putString("platform", "Android")
        })
        
        logAppStart()
    }
    
    // ===== EVENTOS DE USUÁRIO =====
    fun logUserSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
            putString("user_type", "new_user")
        }
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Sign Up Event: method=$method")
        }
    }
    
    fun logUserLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Login Event: method=$method")
        }
    }
    
    fun logUserProfile(age: Int?, gender: String?) {
        analytics.logEvent("user_profile_setup") {
            age?.let { param("user_age_range", getAgeRange(it)) }
            gender?.let { param("user_gender", it) }
        }
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Profile Updated Event: age=$age, gender=$gender")
        }
    }
    
    // ===== EVENTOS DE MATCHING =====
    fun logSwipeAction(action: String, profileId: String) {
        analytics.logEvent("swipe_action") {
            param("action", action) // like, dislike, superlike
            param("profile_id", profileId)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "profile")
        }
    }
    
    fun logMatch(matchId: String) {
        val bundle = Bundle().apply {
            putString("match_id", matchId)
            putString("match_type", "mutual")
            putBoolean(FirebaseAnalytics.Param.SUCCESS, true)
        }
        analytics.logEvent("match_created", bundle)
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Match Event: $matchId")
        }
    }
    
    fun logConversationStarted(matchId: String) {
        analytics.logEvent("conversation_started") {
            param("match_id", matchId)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "message")
        }
    }
    
    // ===== EVENTOS DE MONETIZAÇÃO =====
    fun logSubscriptionPurchase(planType: String, amount: Double) {
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.ITEM_ID, planType)
            param(FirebaseAnalytics.Param.ITEM_NAME, "FypMatch $planType")
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, "subscription")
            param(FirebaseAnalytics.Param.VALUE, amount)
            param(FirebaseAnalytics.Param.CURRENCY, "BRL")
        }
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Purchase Event: plan=$planType, amount=$amount")
        }
    }
    
    fun logAdView(adType: String, placement: String) {
        analytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
            param("ad_type", adType) // banner, interstitial, rewarded
            param("ad_placement", placement)
            param(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
        }
    }
    
    fun logAdReward(creditsEarned: Int) {
        analytics.logEvent("ad_reward_earned") {
            param("credits_earned", creditsEarned.toLong())
            param("reward_type", "fype_credits")
        }
    }
    
    // ===== EVENTOS DA FYPE (IA) =====
    fun logFypeInteraction(sessionId: String, messageCount: Int) {
        val bundle = Bundle().apply {
            putString("session_id", sessionId)
            putLong("message_count", messageCount.toLong())
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ai_chat")
        }
        analytics.logEvent("fype_interaction", bundle)
    }
    
    fun logFypeAdvice(adviceType: String, creditsUsed: Int) {
        analytics.logEvent("fype_advice_given") {
            param("advice_type", adviceType) // dating_tips, conversation_starter, etc
            param("credits_used", creditsUsed.toLong())
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "ai_advice")
        }
    }
    
    // ===== EVENTOS DE NAVEGAÇÃO =====
    fun logScreenView(screenName: String, screenClass: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "Screen View: $screenName ($screenClass)")
        }
    }
    
    fun logFeatureUsage(featureName: String) {
        analytics.logEvent("feature_used") {
            param("feature_name", featureName)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "app_feature")
        }
    }
    
    // ===== EVENTOS DE ENGAJAMENTO =====
    fun logDailyActive() {
        analytics.logEvent("daily_active_user") {
            param("session_date", getCurrentDate())
        }
    }
    
    fun logSessionDuration(durationMinutes: Long) {
        analytics.logEvent("session_duration") {
            param("duration_minutes", durationMinutes)
            param("engagement_level", getEngagementLevel(durationMinutes))
        }
    }
    
    // ===== CRASHLYTICS =====
    fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
        analytics.setUserId(userId)
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "User ID definido: $userId")
        }
    }
    
    fun setUserProperties(properties: Map<String, String>) {
        properties.forEach { (key, value) ->
            analytics.setUserProperty(key, value)
            crashlytics.setCustomKey(key, value)
        }
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.d(TAG, "User Properties: $properties")
        }
    }
    
    fun logError(exception: Throwable, context: String = "") {
        crashlytics.recordException(exception)
        crashlytics.setCustomKey("error_context", context)
        
        if (BuildConfig.FIREBASE_DEBUG) {
            Log.e(TAG, "Error logged - Context: $context", exception)
        }
        
        analytics.logEvent("app_error") {
            param("error_type", exception.javaClass.simpleName)
            param("error_context", context)
        }
    }
    
    fun logCustomCrash(message: String, details: Map<String, String> = emptyMap()) {
        crashlytics.log(message)
        details.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    private fun logAppStart() {
        val bundle = Bundle().apply {
            putString("app_version", getAppVersion())
            putLong("first_open_time", System.currentTimeMillis())
        }
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getAgeRange(age: Int): String {
        return when (age) {
            in 18..24 -> "18-24"
            in 25..34 -> "25-34" 
            in 35..44 -> "35-44"
            in 45..54 -> "45-54"
            else -> "55+"
        }
    }
    
    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
    
    private fun getEngagementLevel(minutes: Long): String {
        return when (minutes) {
            in 0..2 -> "low"
            in 3..10 -> "medium"
            in 11..30 -> "high"
            else -> "very_high"
        }
    }
}

// Extension function para facilitar logging de eventos
private inline fun FirebaseAnalytics.logEvent(name: String, block: Bundle.() -> Unit) {
    val bundle = Bundle()
    bundle.block()
    this.logEvent(name, bundle)
}

private fun Bundle.param(key: String, value: String) = putString(key, value)
private fun Bundle.param(key: String, value: Long) = putLong(key, value) 
private fun Bundle.param(key: String, value: Double) = putDouble(key, value)
private fun Bundle.param(key: String, value: Boolean) = putBoolean(key, value) 