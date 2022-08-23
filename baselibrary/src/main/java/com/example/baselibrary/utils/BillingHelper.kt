package com.example.baselibrary.utils

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

object BillingHelper {

    private lateinit var billingClient: BillingClient
    var billingClientOnline = false
    var billingCallback: BillingCallback? = null

    fun initBillingClient(c: Context) {
        billingClient = BillingClient.newBuilder(c)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        Timber.d("iap success: ${purchase.originalJson}")
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            billingCallback?.onSuccess(billingResult, purchases)
                        }
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    billingCallback?.onCancel(billingResult, UUID.randomUUID().toString())
                } else {
                    // Handle any other error codes.
                    billingCallback?.onFailed(billingResult, UUID.randomUUID().toString())
                }
            }
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(p0: BillingResult) {
                billingClientOnline = p0.responseCode == BillingClient.BillingResponseCode.OK
                if (billingClientOnline) {
                    checkUnconsumedTransactions {}
                }
            }

            override fun onBillingServiceDisconnected() {
                billingClientOnline = false
            }
        })

    }

    private fun consume(token: String) {
        val param = ConsumeParams.newBuilder().setPurchaseToken(token).build()
        billingClient.consumeAsync(param) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d("iap consume success: $token, outToken: $outToken")
            }
        }
    }

    fun queryAndPay(
        id: String,
        userId: String,
        activity: Activity,
        onProductEmpty: () -> Unit,
        onCompleted: (BillingResult) -> Unit
    ) {
        if (billingClientOnline) {
            GlobalScope.launch(Dispatchers.IO) {
                val productList =
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                billingClient.queryProductDetailsAsync(params.build()) { _, productDetailsList ->
                    Timber.d("iap skuDetailsList: $productDetailsList")
                    if (productDetailsList.isEmpty()) {
                        onProductEmpty.invoke()
                        return@queryProductDetailsAsync
                    }

                    val productDetails = productDetailsList[0] ?: return@queryProductDetailsAsync
                    // Retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                    // Get the offerToken of the selected offer
                    val offerToken = productDetails.subscriptionOfferDetails?.first()?.offerToken
                    val productDetailsParamsList =
                        listOf(
                            if (offerToken == null) {
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                            } else {
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            }
                        )
                    val billingFlowParams =
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                    // Launch the billing flow
                    val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                    onCompleted.invoke(billingResult)
                }
            }
        }
    }

    fun checkUnconsumedTransactions(onUnAcknowledged: (Purchase) -> Unit) {
        if (billingClientOnline) {
            Timber.d("checkUnconsumedTransactions, billingClientOnline: $billingClientOnline")
            GlobalScope.launch(Dispatchers.IO) {
                val purchases = billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build())
                purchases.purchasesList.forEach {
                    if (it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!it.isAcknowledged) {
                            onUnAcknowledged.invoke(it)
                        } else {
                            consume(it.purchaseToken)
                        }
                    }
                }
            }
        }
    }

    interface BillingCallback {
        fun onSuccess(billingResult: BillingResult, purchases: MutableList<Purchase>)
        fun onFailed(billingResult: BillingResult, purchases: String)
        fun onCancel(billingResult: BillingResult, purchases: String)
        fun onIapError(code: Int, msg: String, json: String, isFirstRecharge: Int?)
    }
}