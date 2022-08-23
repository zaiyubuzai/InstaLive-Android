package com.example.instalive.api

import android.content.Context
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import androidx.core.app.NotificationManagerCompat
import com.example.baselibrary.network.BaseProvider
import com.example.baselibrary.utils.NetworkUtils
import com.example.instalive.BuildConfig
import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.app.SessionPreferences
import com.example.instalive.http.InstaApi
import com.example.instalive.utils.FlipperInitializer
import com.example.instalive.utils.SysUtils
import com.venus.framework.rest.signature.UrlSignatureConfig
import com.venus.framework.rest.signature.VenusPlayUrlSignatureInterceptor
import com.venus.framework.rest.signature.VenusUrlSignatureInterceptor
import com.venus.framework.util.Tracker
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import splitties.systemservices.storageStatsManager
import timber.log.Timber
import java.net.URLEncoder
import java.util.*

object RetrofitProvider: BaseProvider() {

    private var userAgent: String? = null
    var webViewUA: String? = null
    var pagePath: String = ""

    override fun getBaseUrl(): String {
        return ""
    }

    override fun createApi(retrofit: Retrofit) {
        baseApi = retrofit.create(InstaApi::class.java)
    }

    override fun addInterceptor(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        val interceptor: Interceptor = if (BuildConfig.BUILD_TYPE == "debug") {
            VenusUrlSignatureInterceptor(object : UrlSignatureConfig {
                override fun isSignatureRequired(url: HttpUrl?): Boolean {
                    return true
                }

                override fun getApiBaseUrl(): String {
                    return ""
                }

                override fun getTracker(): Tracker? {
                    return null
                }

                override fun getContext(): Context {
                    return appInstance
                }
            })
        } else {
            VenusPlayUrlSignatureInterceptor(object : UrlSignatureConfig {
                override fun isSignatureRequired(url: HttpUrl?): Boolean {
                    return true
                }

                override fun getApiBaseUrl(): String {
                    return ""
                }

                override fun getTracker(): Tracker? {
                    return null
                }

                override fun getContext(): Context {
                    return appInstance
                }
            })
        }
        builder.addInterceptor(interceptor)
            .addNetworkInterceptor { chain ->
                val newBuilder = chain.request().newBuilder()
                newBuilder.addHeader("X-FM-DI", "")
                val lat = SessionPreferences.lastLat ?: SessionPreferences.lat
                val lon = SessionPreferences.lastLon ?: SessionPreferences.lon
                if (lat != null && lon != null) {
                    newBuilder.addHeader("X-FM-LC", "$lat,$lon")
                }
                val acc = SessionPreferences.lastLocAcc
                if (acc != null) {
                    newBuilder.addHeader("X-FM-LCA", acc)
                }
                newBuilder.addHeader("X-FM-UA", getAgent())
                newBuilder.addHeader("Accept-Language", Locale.getDefault().toLanguageTag())
                newBuilder.addHeader("X-FM-TIMEZONE", SysUtils.getTimezone())
                if (SessionPreferences.id.isNotEmpty()) {
                    newBuilder.addHeader("X-FM-UI", SessionPreferences.id)
                }
                if (SessionPreferences.token.isNotEmpty()) {
                    newBuilder.addHeader("X-FM-UT", SessionPreferences.token)
                }
//                if (BuildConfig.FLAVOR == "internaltest"||BuildConfig.FLAVOR == "stage"){
//                    newBuilder.addHeader("X-FM-INTERNALTEST", "1")
//                }
                chain.proceed(newBuilder.build())
            }

        if (BuildConfig.DEBUG) {
            val i = FlipperInitializer.interceptor
            builder.addNetworkInterceptor(i)
        }
        return super.addInterceptor(builder)
    }

    private fun getAgent(): String {
        val begin = System.currentTimeMillis()
        var ua = userAgent ?: generateUA()
        if (pagePath.isNotEmpty()) {
            ua = "${ua}rf_pag:$pagePath,"
        }
        ua = "${ua}network:${getNetwork()},"
        Timber.d("getAgent cost: ${System.currentTimeMillis() - begin} milliseconds")
        return ua
    }

    private fun generateUA(): String {
        val builder = StringBuilder()
        builder
            .append("os:android,os_version:${Build.VERSION.RELEASE}")
            .append(",device:${getModel()}")
            .append(",brand:${getBrand()}")
            .append(",w:${appInstance.screenWidth}")
            .append(",h:${appInstance.screenHeight}")
            .append(",app_version:${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})")
            .append(",manufacturer:${SysUtils.encodeUrl(Build.MANUFACTURER)}")
            .append(",os_full:android:${Build.VERSION.RELEASE},")
            .append("Accept-Language:${Locale.getDefault().toLanguageTag()},")
            .append("country:${Locale.getDefault().country},")
            .append("carrier:${SysUtils.encodeUrl(SysUtils.getSimOperatorName())},")
            .append("pp:${getNotificationImportance()},")
            .append("nu:${calcUserRegisteredDays()},")
            .append("wua:$webViewUA,")

        //时区，屏幕，运营商信息，launcher，region，locale信息
        if (Build.VERSION.SDK_INT > 26) {
            val storageStats = storageStatsManager.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                appInstance.packageName,
                Process.myUserHandle()
            )
            builder.append("as:${storageStats.appBytes},cs:${storageStats.cacheBytes},ds:${storageStats.dataBytes},")
        }
        userAgent = builder.toString()
        return userAgent ?: ""
    }

    private fun calcUserRegisteredDays(): Int {
        val createdAt = SessionPreferences.createdAt
        return if (createdAt == 0L) {
            -1
        } else {
            ((System.currentTimeMillis() / 1000 - createdAt) / (24 * 60 * 60)).toInt()
        }
    }

    private fun getNetwork(): String {
        return when (NetworkUtils.getNetworkType(appInstance)) {
            NetworkUtils.NetworkType.NETWORK_ETHERNET -> "Ethernet"
            NetworkUtils.NetworkType.NETWORK_WIFI -> "WIFI"
            NetworkUtils.NetworkType.NETWORK_5G -> "5G"
            NetworkUtils.NetworkType.NETWORK_4G -> "4G"
            NetworkUtils.NetworkType.NETWORK_3G -> "3G"
            NetworkUtils.NetworkType.NETWORK_2G -> "2G"
            NetworkUtils.NetworkType.NETWORK_UNKNOWN -> "Unknown"
            NetworkUtils.NetworkType.NETWORK_NO -> "No"
            else -> "Unknown"
        }
    }

    private fun getNotificationImportance(): String {
        val isEnable = NotificationManagerCompat
            .from(appInstance).areNotificationsEnabled()
        val stringBuilder = java.lang.StringBuilder()

        stringBuilder.append("0")

        if (isEnable) {
            stringBuilder.append("1")
        } else {
            stringBuilder.append("0")
        }

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManagerCompat
                .from(appInstance).notificationChannels
                .forEach {
                    if (it.importance > 0) {
                        stringBuilder.append("1")
                    } else {
                        stringBuilder.append("0")
                    }
                }
        }
        return stringBuilder.toString()
    }

    private fun getModel(): String {
        return try {
            URLEncoder.encode(Build.MODEL, "utf-8")
        } catch (e: Exception) {
            ""
        }
    }

    private fun getBrand(): String {
        return try {
            URLEncoder.encode(Build.BRAND, "utf-8")
        } catch (e: Exception) {
            ""
        }
    }
}