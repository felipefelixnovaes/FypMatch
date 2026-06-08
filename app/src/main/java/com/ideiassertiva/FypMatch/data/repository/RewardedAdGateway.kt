package com.ideiassertiva.FypMatch.data.repository

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.ideiassertiva.FypMatch.BuildConfig
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

interface RewardedAdGateway {
    suspend fun showRewardedAd(activity: Activity?): Result<Unit>
}

class AdMobRewardedAdGateway : RewardedAdGateway {
    override suspend fun showRewardedAd(activity: Activity?): Result<Unit> {
        val hostActivity = activity
            ?: return Result.failure(Exception("Tela do anúncio indisponível. Tente novamente."))
        val adUnitId = BuildConfig.ADMOB_REWARDED_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            return Result.failure(Exception("AdMob Rewarded Ad Unit ID não configurado."))
        }

        return suspendCancellableCoroutine { continuation ->
            hostActivity.runOnUiThread {
                RewardedAd.load(
                    hostActivity,
                    adUnitId,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            if (continuation.isActive) {
                                continuation.resume(
                                    Result.failure(Exception("Anúncio indisponível: ${error.message}"))
                                )
                            }
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                    if (continuation.isActive) {
                                        continuation.resume(
                                            Result.failure(Exception("Não foi possível exibir o anúncio: ${error.message}"))
                                        )
                                    }
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    if (continuation.isActive) {
                                        continuation.resume(
                                            Result.failure(Exception("Anúncio fechado antes da recompensa."))
                                        )
                                    }
                                }
                            }

                            ad.show(hostActivity) {
                                if (continuation.isActive) {
                                    continuation.resume(Result.success(Unit))
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

class ImmediateRewardedAdGateway : RewardedAdGateway {
    override suspend fun showRewardedAd(activity: Activity?): Result<Unit> = Result.success(Unit)
}
