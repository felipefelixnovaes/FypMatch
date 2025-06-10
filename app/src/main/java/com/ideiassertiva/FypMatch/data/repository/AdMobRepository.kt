package com.ideiassertiva.FypMatch.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎯 **AdMobRepository - Fase 4**
 * 
 * Gerencia anúncios intersticiais exibidos após swipes para usuários básicos.
 * Implementa controle de frequência e carregamento inteligente.
 */
@Singleton
class AdMobRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "AdMobRepository"
        
        // 🎯 ID real do bloco intersticial - FypMatch Brasil
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9657321458227740/7105049493"
        
        // Controle de frequência - mostrar anúncio a cada X swipes
        private const val SWIPE_COUNT_THRESHOLD = 3
        
        // Tempo mínimo entre anúncios (em milissegundos)
        private const val MIN_TIME_BETWEEN_ADS = 30_000L // 30 segundos
    }
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var swipeCount = 0
    private var lastAdShownTime = 0L
    private var isAdMobInitialized = false
    
    /**
     * 🚀 Inicializa o AdMob SDK
     */
    suspend fun initializeAdMob() {
        if (isAdMobInitialized) return
        
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob inicializado: ${initializationStatus.adapterStatusMap}")
                isAdMobInitialized = true
                
                // Pré-carrega o primeiro anúncio
                loadInterstitialAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar AdMob", e)
        }
    }
    
    /**
     * 📱 Carrega anúncio intersticial
     */
    private fun loadInterstitialAd() {
        if (!isAdMobInitialized || isLoadingAd || interstitialAd != null) return
        
        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Anúncio intersticial carregado com sucesso")
                    interstitialAd = ad
                    isLoadingAd = false
                    
                    // Configura callbacks do anúncio
                    setupAdCallbacks(ad)
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Falha ao carregar anúncio: ${adError.message}")
                    interstitialAd = null
                    isLoadingAd = false
                }
            }
        )
    }
    
    /**
     * ⚙️ Configura callbacks do anúncio
     */
    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Anúncio intersticial fechado")
                interstitialAd = null
                lastAdShownTime = System.currentTimeMillis()
                
                // Pré-carrega próximo anúncio
                loadInterstitialAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Falha ao exibir anúncio: ${adError.message}")
                interstitialAd = null
                loadInterstitialAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Anúncio intersticial exibido")
            }
        }
    }
    
    /**
     * 👆 Registra swipe do usuário
     * 
     * @param isBasicUser - Se true, conta swipe para exibir anúncio
     */
    fun onUserSwipe(isBasicUser: Boolean) {
        if (!isBasicUser) return
        
        swipeCount++
        Log.d(TAG, "Swipe registrado. Contagem: $swipeCount")
    }
    
    /**
     * 🎯 Verifica se deve exibir anúncio após swipe
     * 
     * @param activity - Activity onde o anúncio será exibido
     * @param isBasicUser - Se o usuário é básico (não premium)
     * @return true se anúncio foi exibido
     */
    suspend fun showAdIfNeeded(activity: Activity, isBasicUser: Boolean): Boolean {
        if (!isBasicUser) return false
        if (!shouldShowAd()) return false
        
        return showInterstitialAd(activity)
    }
    
    /**
     * 🔍 Verifica se deve exibir anúncio baseado na frequência
     */
    private fun shouldShowAd(): Boolean {
        val timeSinceLastAd = System.currentTimeMillis() - lastAdShownTime
        
        return swipeCount >= SWIPE_COUNT_THRESHOLD && 
               timeSinceLastAd >= MIN_TIME_BETWEEN_ADS &&
               interstitialAd != null
    }
    
    /**
     * 📺 Exibe anúncio intersticial
     */
    private suspend fun showInterstitialAd(activity: Activity): Boolean {
        val ad = interstitialAd ?: return false
        
        try {
            ad.show(activity)
            swipeCount = 0 // Reset contador após exibir anúncio
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exibir anúncio", e)
            return false
        }
    }
    
    /**
     * 🔄 Força carregamento de novo anúncio
     */
    fun preloadNextAd() {
        if (interstitialAd == null && !isLoadingAd) {
            loadInterstitialAd()
        }
    }
    
    /**
     * 📊 Obtém estatísticas de anúncios
     */
    fun getAdMobStats(): AdMobStats {
        return AdMobStats(
            swipeCount = swipeCount,
            isAdLoaded = interstitialAd != null,
            isLoading = isLoadingAd,
            timeSinceLastAd = System.currentTimeMillis() - lastAdShownTime
        )
    }
}

/**
 * 📊 Dados estatísticos dos anúncios AdMob
 */
data class AdMobStats(
    val swipeCount: Int,
    val isAdLoaded: Boolean,
    val isLoading: Boolean,
    val timeSinceLastAd: Long
) 