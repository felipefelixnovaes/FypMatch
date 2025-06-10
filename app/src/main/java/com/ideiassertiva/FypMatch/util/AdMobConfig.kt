package com.ideiassertiva.FypMatch.util

/**
 * 📺 Configuração centralizada do AdMob
 * 
 * IDs de teste garantem que os anúncios funcionem durante desenvolvimento
 * sem afetar métricas reais ou violar políticas do Google AdMob
 */
object AdMobConfig {
    
    /**
     * 🎥 ID do anúncio recompensado
     * - Test ID: Anúncios de teste funcionais do Google
     * - Production ID: ID real do projeto no AdMob
     */
    val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Google Test Ad Unit ID
    
    /**
     * 📱 ID do app no AdMob
     */
    const val APP_ID = "ca-app-pub-9657321458227740~9100657445"
    
    /**
     * 🎯 Configurações de recompensa
     */
    const val CREDITS_PER_AD = 3
    const val INITIAL_CREDITS = 10
    const val MAX_ADS_PER_DAY = 10
    
    /**
     * 🛠️ Configurações de debug
     */
    val USE_TEST_ADS = true // Sempre usar test ads por enquanto
    
    /**
     * 📊 Device IDs para teste (opcional)
     * Adicione seu device ID aqui para sempre receber anúncios de teste
     */
    val TEST_DEVICE_IDS = listOf(
        "YOUR_DEVICE_ID_HERE" // Substitua pelo ID do seu dispositivo
    )
} 