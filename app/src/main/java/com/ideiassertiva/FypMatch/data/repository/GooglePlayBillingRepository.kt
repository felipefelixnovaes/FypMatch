package com.ideiassertiva.FypMatch.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.ideiassertiva.FypMatch.model.SubscriptionStatus
import com.ideiassertiva.FypMatch.util.AnalyticsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 💳 Google Play Billing Repository - FypMatch
 * 
 * Gerencia todas as compras in-app usando o cartão já cadastrado do usuário no Google Play
 * Facilita compras sem precisar inserir dados do cartão novamente
 */
@Singleton
class GooglePlayBillingRepository @Inject constructor(
    private val context: Context,
    private val analyticsManager: AnalyticsManager
) : PurchasesUpdatedListener {
    
    private var billingClient: BillingClient? = null
    
    // Estados do billing
    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected.asStateFlow()
    
    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: Flow<List<ProductDetails>> = _products.asStateFlow()
    
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: Flow<PurchaseState> = _purchaseState.asStateFlow()
    
    // IDs dos produtos de assinatura
    companion object {
        const val PREMIUM_MONTHLY = "fypmatch_premium_monthly"
        const val PREMIUM_YEARLY = "fypmatch_premium_yearly"
        const val VIP_MONTHLY = "fypmatch_vip_monthly"
        const val VIP_YEARLY = "fypmatch_vip_yearly"
        
        val ALL_PRODUCT_IDS = listOf(
            PREMIUM_MONTHLY,
            PREMIUM_YEARLY,
            VIP_MONTHLY,
            VIP_YEARLY
        )
    }
    
    init {
        initializeBilling()
    }
    
    private fun initializeBilling() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        connectToBilling()
    }
    
    private fun connectToBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _isConnected.value = true
                    loadAvailableProducts()
                    
                    // Analytics: Billing conectado
                    analyticsManager.logCustomCrash("billing_connected", mapOf(
                        "response_code" to billingResult.responseCode.toString()
                    ))
                } else {
                    _isConnected.value = false
                    
                    // Analytics: Erro na conexão
                    analyticsManager.logError(
                        Exception("Billing connection failed: ${billingResult.debugMessage}"),
                        "billing_connection_error"
                    )
                }
            }
            
            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
                
                // Analytics: Billing desconectado
                analyticsManager.logCustomCrash("billing_disconnected", emptyMap())
            }
        })
    }
    
    private fun loadAvailableProducts() {
        val productList = ALL_PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        
        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                _products.value = productDetailsList
                
                // Analytics: Produtos carregados
                analyticsManager.logCustomCrash("billing_products_loaded", mapOf(
                    "products_count" to productDetailsList.size.toString(),
                    "products" to productDetailsList.joinToString(",") { it.productId }
                ))
            } else {
                // Analytics: Erro ao carregar produtos
                analyticsManager.logError(
                    Exception("Failed to load products: ${billingResult.debugMessage}"),
                    "billing_load_products_error"
                )
            }
        }
    }
    
    /**
     * 🛒 Iniciar compra de assinatura
     * Usa o cartão já cadastrado no Google Play do usuário
     */
    suspend fun purchaseSubscription(
        activity: Activity,
        subscriptionType: SubscriptionStatus
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        
        val productId = when (subscriptionType) {
            SubscriptionStatus.PREMIUM -> PREMIUM_MONTHLY
            SubscriptionStatus.VIP -> VIP_MONTHLY
            else -> {
                continuation.resume(Result.failure(Exception("Tipo de assinatura inválido")))
                return@suspendCancellableCoroutine
            }
        }
        
        val product = _products.value.find { it.productId == productId }
        if (product == null) {
            continuation.resume(Result.failure(Exception("Produto não encontrado")))
            return@suspendCancellableCoroutine
        }
        
        val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            continuation.resume(Result.failure(Exception("Oferta não disponível")))
            return@suspendCancellableCoroutine
        }
        
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offerToken)
                .build()
        )
        
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        
        _purchaseState.value = PurchaseState.Purchasing
        
        // Analytics: Compra iniciada
        analyticsManager.logSubscriptionPurchase(
            productId,
            product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros?.div(1_000_000.0) ?: 0.0
        )
        
        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)
        
        if (billingResult?.responseCode != BillingResponseCode.OK) {
            _purchaseState.value = PurchaseState.Error(billingResult?.debugMessage ?: "Erro desconhecido")
            continuation.resume(Result.failure(Exception(billingResult?.debugMessage)))
        } else {
            // O resultado da compra será tratado em onPurchasesUpdated
            continuation.resume(Result.success(Unit))
        }
    }
    
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
                
                // Analytics: Compra cancelada
                analyticsManager.logCustomCrash("purchase_cancelled", emptyMap())
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
                
                // Analytics: Erro na compra
                analyticsManager.logError(
                    Exception("Purchase failed: ${billingResult.debugMessage}"),
                    "purchase_error"
                )
            }
        }
    }
    
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Verificar e confirmar a compra
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            
            _purchaseState.value = PurchaseState.Success(purchase)
            
            // Analytics: Compra bem-sucedida
            analyticsManager.logCustomCrash("purchase_successful", mapOf(
                "product_id" to purchase.products.joinToString(","),
                "order_id" to (purchase.orderId ?: "unknown"),
                "package_name" to purchase.packageName
            ))
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseState.value = PurchaseState.Pending
            
            // Analytics: Compra pendente
            analyticsManager.logCustomCrash("purchase_pending", mapOf(
                "product_id" to purchase.products.joinToString(",")
            ))
        }
    }
    
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                // Analytics: Compra confirmada
                analyticsManager.logCustomCrash("purchase_acknowledged", mapOf(
                    "order_id" to (purchase.orderId ?: "unknown")
                ))
            }
        }
    }
    
    /**
     * 🔍 Verificar assinaturas ativas do usuário
     */
    suspend fun queryActivePurchases(): List<Purchase> = suspendCancellableCoroutine { continuation ->
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        
        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                val activePurchases = purchases.filter { 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED 
                }
                continuation.resume(activePurchases)
                
                // Analytics: Assinaturas verificadas
                analyticsManager.logCustomCrash("subscriptions_queried", mapOf(
                    "active_count" to activePurchases.size.toString()
                ))
            } else {
                continuation.resume(emptyList())
                
                // Analytics: Erro ao verificar assinaturas
                analyticsManager.logError(
                    Exception("Failed to query purchases: ${billingResult.debugMessage}"),
                    "query_purchases_error"
                )
            }
        }
    }
    
    /**
     * 💰 Obter preço formatado de um produto
     */
    fun getProductPrice(productId: String): String? {
        return _products.value.find { it.productId == productId }
            ?.subscriptionOfferDetails?.firstOrNull()
            ?.pricingPhases?.pricingPhaseList?.firstOrNull()
            ?.formattedPrice
    }
    
    /**
     * 🔌 Limpar recursos
     */
    fun disconnect() {
        billingClient?.endConnection()
        _isConnected.value = false
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Purchasing : PurchaseState()
    object Pending : PurchaseState()
    object Cancelled : PurchaseState()
    data class Success(val purchase: Purchase) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
} 