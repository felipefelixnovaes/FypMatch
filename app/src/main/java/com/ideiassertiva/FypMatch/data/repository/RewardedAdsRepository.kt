package com.ideiassertiva.FypMatch.data.repository

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.ideiassertiva.FypMatch.util.AdMobConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdsRepository @Inject constructor(
    private val context: Context
) {
    
    // 🎥 ID do anúncio recompensado
    // Configuração centralizada para usar IDs de teste em desenvolvimento
    private val rewardedAdUnitId = AdMobConfig.REWARDED_AD_UNIT_ID
    
    // Estados do anúncio
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _credits = MutableStateFlow(0)
    val credits: StateFlow<Int> = _credits.asStateFlow()
    
    private var rewardedAd: RewardedAd? = null
    
    // Callbacks para eventos do anúncio
    private var onRewardEarned: ((credits: Int) -> Unit)? = null
    private var onAdClosed: (() -> Unit)? = null
    private var onAdFailed: ((error: String) -> Unit)? = null
    
    init {
        loadRewardedAd()
        // Créditos iniciais - pode ser carregado do SharedPreferences depois
        _credits.value = AdMobConfig.INITIAL_CREDITS
    }
    
    /**
     * 🎬 Carrega um novo anúncio recompensado
     */
    fun loadRewardedAd() {
        if (_isLoading.value) return
        
        _isLoading.value = true
        
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isAdLoaded.value = true
                    _isLoading.value = false
                    
                    // Configurar callbacks do anúncio carregado
                    setupAdCallbacks()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _isAdLoaded.value = false
                    _isLoading.value = false
                    onAdFailed?.invoke("Erro ao carregar anúncio: ${loadAdError.message}")
                }
            }
        )
    }
    
    /**
     * 🎥 Mostra o anúncio recompensado
     */
    fun showRewardedAd(
        activity: Activity,
        onReward: (credits: Int) -> Unit,
        onClosed: () -> Unit,
        onFailed: (error: String) -> Unit
    ) {
        this.onRewardEarned = onReward
        this.onAdClosed = onClosed
        this.onAdFailed = onFailed
        
        if (rewardedAd != null && _isAdLoaded.value) {
            rewardedAd?.show(activity) { rewardItem ->
                // 🎉 Usuário ganhou a recompensa!
                val creditsEarned = AdMobConfig.CREDITS_PER_AD
                addCredits(creditsEarned)
                onRewardEarned?.invoke(creditsEarned)
            }
        } else {
            onAdFailed?.invoke("Anúncio não está carregado. Tente novamente em alguns segundos.")
            // Tentar carregar um novo anúncio
            loadRewardedAd()
        }
    }
    
    /**
     * 🏆 Adiciona créditos ao usuário
     */
    private fun addCredits(amount: Int) {
        _credits.value += amount
        // TODO: Salvar no SharedPreferences ou Firebase
    }
    
    /**
     * 💰 Gasta créditos (para falar com a Fype)
     */
    fun spendCredits(amount: Int): Boolean {
        return if (_credits.value >= amount) {
            _credits.value -= amount
            // TODO: Salvar no SharedPreferences ou Firebase
            true
        } else {
            false
        }
    }
    
    /**
     * 🔧 Configura callbacks do anúncio
     */
    private fun setupAdCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Anúncio foi clicado
            }
            
            override fun onAdDismissedFullScreenContent() {
                // Anúncio foi fechado
                rewardedAd = null
                _isAdLoaded.value = false
                onAdClosed?.invoke()
                
                // Carregar próximo anúncio
                loadRewardedAd()
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Falha ao mostrar anúncio
                rewardedAd = null
                _isAdLoaded.value = false
                onAdFailed?.invoke("Erro ao exibir anúncio: ${adError.message}")
            }
            
            override fun onAdImpression() {
                // Anúncio foi exibido
            }
            
            override fun onAdShowedFullScreenContent() {
                // Anúncio começou a ser exibido
            }
        }
    }
    
    /**
     * 📊 Verifica se tem créditos suficientes
     */
    fun hasCredits(amount: Int = 1): Boolean {
        return _credits.value >= amount
    }
    
    /**
     * 🎯 Reseta créditos (para debug)
     */
    fun resetCredits() {
        _credits.value = 0
    }
} 