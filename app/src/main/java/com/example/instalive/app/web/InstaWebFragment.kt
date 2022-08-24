package com.example.instalive.app.web

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.example.baselibrary.utils.BillingHelper
import com.example.instalive.R
import com.example.instalive.api.RetrofitProvider
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.web.bridge.BridgeHandler
import com.example.instalive.app.web.bridge.BridgeWebViewClient
import com.example.instalive.app.web.bridge.CallBackFunction
import com.example.instalive.app.web.bridge.NewNestedScrollWebView
import com.example.instalive.utils.marsToast
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import splitties.dimensions.dp
import splitties.fragmentargs.argOrNull
import timber.log.Timber

class InstaWebFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, BridgeHandler {

        lateinit var swipeRefreshLayout: SwipeRefreshLayout
        lateinit var webView: NewNestedScrollWebView
        var onTitle: ((String) -> Unit)? = null
        var onIcon: ((Int) -> Unit)? = null

        var hideBackIcon: (() -> Unit)? = null
        var showBackIcon: (() -> Unit)? = null

        var webViewBlack = false
        var webNeedFresh = true

        var url: String? by argOrNull()

        var rechargeData: JSONObject? = null
        var isRecharging: Boolean = false

        var sourceFrom: String = "VenusWebFragment"

        private var fileCallback: ValueCallback<Array<Uri>>? = null // Android5+的file upload callback

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val ctx = context ?: return super.onCreateView(inflater, container, savedInstanceState)
            swipeRefreshLayout = SwipeRefreshLayout(ctx)
            swipeRefreshLayout.setPadding(0, 0, 0, 0)
            swipeRefreshLayout.isEnabled = false
            swipeRefreshLayout.setOnRefreshListener(this)
            swipeRefreshLayout.setBackgroundColor(0x282f3f)
            activity?.dp(60)?.let {
                swipeRefreshLayout.setProgressViewOffset(true, 0, it)
            }

            try {
                webView = NewNestedScrollWebView(ctx)
                webView.setDefaultHandler(this)
                webView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                configWebView(webView)
                if (url != null) {
                    loadUrl(url ?: "")
                }
                webView.setBackgroundColor(0x282f3f)
                webView.requestFocus(View.FOCUS_DOWN)
                webView.isEnabled = true
                swipeRefreshLayout.addView(webView)
            }catch (e: Exception){
                e.printStackTrace()
                marsToast(R.string.fb_failed_to_load_browser)
                activity?.finish()
            }

            return swipeRefreshLayout
        }

        fun setTitle(onTitle: (String) -> Unit) {
            this.onTitle = onTitle
        }

        fun setIcon(onIcon: (Int) -> Unit) {
            this.onIcon = onIcon
        }

        fun setHideBackIcon1(hideBackIcon: () -> Unit) {
            this.hideBackIcon = hideBackIcon
        }

        fun setShowBackIcon1(showBackIcon: () -> Unit) {
            this.showBackIcon = showBackIcon
        }

        private fun configWebView(webView: NewNestedScrollWebView) {
            webView.webChromeClient = object : WebChromeClient() {
                @TargetApi(21)
                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams,
                ): Boolean {
                    val context: Context = webView.context
                    fileCallback?.onReceiveValue(null) // cancel之前的callback
                    fileCallback = null

                    fileCallback = filePathCallback
                    val intent = fileChooserParams.createIntent()
                    try {
                        startActivityForResult(intent, REQUEST_SELECT_FILE)
                    } catch (e: ActivityNotFoundException) {
                        fileCallback = null
                        marsToast("Cannot Open File Chooser")
                        return false
                    }
                    return true
                }

            }
            webView.webViewClient = object : BridgeWebViewClient(webView) {

                override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                    return super.shouldOverrideKeyEvent(view, event)
                    val hitTestResult = view?.hitTestResult
                    return hitTestResult == null
                }

                override fun doOnPageFinished(view: WebView?, url: String?) {
                    super.doOnPageFinished(view, url)
                    swipeRefreshLayout.isRefreshing = false
                    onTitle?.invoke(view?.title ?: "")
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    if (request?.isForMainFrame == true) {
                        marsToast(
                            context?.getString(
                                R.string.fb_something_goes_wrong_error_code,
                                errorResponse?.statusCode.toString()
                            ) ?: ""
                        )
                        lifecycleScope.launch(Dispatchers.IO) {
//                            CollectHelper.reportAction(
//                                errorResponse?.reasonPhrase.toString(),
//                                null,
//                                null,
//                                errorResponse?.statusCode.toString()
//                            )
                        }
                    }
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        marsToast(context?.getString(R.string.fb_poor_network_you_may_try_later) ?: "")
                        lifecycleScope.launch(Dispatchers.IO) {
//                            CollectHelper.reportAction(
//                                error?.getDescription().toString(),
//                                null,
//                                null,
//                                error?.errorCode.toString()
//                            )
                        }
                    }
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_SELECT_FILE) {
                fileCallback?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )
                )
                fileCallback = null
            }
        }

        override fun onRefresh() {
            if (webNeedFresh) {
                swipeRefreshLayout.isRefreshing = true
                webView.reload()
            }
        }

        private fun loadUrl(url: String) {
            swipeRefreshLayout.isRefreshing = true
            this.url = url
            webView.loadUrl(this.url ?: "", getHeaders())
        }

        private fun getHeaders(): Map<String, String> {
            val header = mutableMapOf<String, String>()
            header["X-FM-DI"] = ""
            val lat = SessionPreferences.lastLat ?: SessionPreferences.lat
            val lon = SessionPreferences.lastLon ?: SessionPreferences.lon
            if (lat != null && lon != null) {
                header["X-FM-LC"] = "$lat,$lon"
            }
            val acc = SessionPreferences.lastLocAcc
            if (acc != null) {
                header["X-FM-LCA"] = acc
            }
            header["X-FM-UA"] = RetrofitProvider.getAgent()
            if (SessionPreferences.id.isNotEmpty()) {
                header["X-FM-UI"] = SessionPreferences.id
            }
            if (SessionPreferences.token.isNotEmpty()) {
                header["X-FM-UT"] = SessionPreferences.token
            }
            return header
        }

        @OptIn(ExperimentalStdlibApi::class)
        override fun handler(data: String?, function: CallBackFunction?) {
            try {
                if (data != null) {
                    val payload = JSONObject(data)
                    val jsFieldData = payload.optJSONObject(JS_FIELD_DATA)
                    when (payload.optString(JS_FIELD_ACTION, "")) {
                    }
                }
            } catch (e: Exception) {
            }
        }

        private fun sendMessageSyncingEventToWeb(step: Int, error: String?) {
            val json = JSONObject()
                .put("act", "sync_event")

            val data = JSONObject()
                .put("step", step)
                .put("status", if (error == null) 1 else 2)

            Timber.d("sendMessageSyncingEventToWeb: $step")

            if (error != null) {
                data.put("msg", error)
            }

            json.put("data", data.toString())

            lifecycleScope.launch(Dispatchers.Main) {
                webView.send(json.toString())
            }
        }

        override fun onDestroy() {
            super.onDestroy()
        }

        private fun checkOutWithIap(jsFieldData: JSONObject?) {
            val act = activity ?: return
            val id = try {
                jsFieldData?.getString("iap_product_id") ?: null
            } catch (e: Exception) {
                null
            } ?: return
            BillingHelper.billingCallback = object : BillingHelper.BillingCallback {
                override fun onSuccess(
                    billingResult: BillingResult,
                    purchases: MutableList<Purchase>
                ) {

                }

                override fun onFailed(billingResult: BillingResult, purchases: String) {

                }

                override fun onCancel(billingResult: BillingResult, purchases: String) {

                }

                override fun onIapError(
                    code: Int,
                    msg: String,
                    json: String,
                    isFirstRecharge: Int?
                ) {

                }

            }
            BillingHelper.queryAndPay(id, SessionPreferences.id, requireActivity(), {

            },{

            })
            isRecharging = false
        }


        companion object {
            const val REQUEST_SELECT_FILE = 100
            const val BRIDGE_HANDLER_NAME = "bridge_handler"
            const val JS_FIELD_ACTION = "act"
            const val JS_FIELD_DATA = "data"



            fun createResponseData(act: String, status: Int, msg: String): String {
                return try {
                    JSONObject()
                        .put("act", act)
                        .put("state", status)
                        .put("msg", msg)
                        .toString()
                } catch (e: JSONException) {
                    ""
                }
            }

            fun createResponseData(act: String, data: JSONObject): String {
                return try {
                    JSONObject()
                        .put("act", act)
                        .put("state", BridgeHandler.STATUS_SUCCESS)
                        .put("data", data)
                        .toString()
                } catch (e: JSONException) {
                    ""
                }
            }
        }
}