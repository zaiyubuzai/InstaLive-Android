package com.example.baselibrary.utils

import android.Manifest.permission
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import android.net.NetworkCapabilities

import android.net.Network

import android.os.Build




/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/02
 * desc  : utils about network
</pre> *
 */
object NetworkUtils {
    enum class NetworkType {
        NETWORK_ETHERNET, NETWORK_WIFI, NETWORK_4G, NETWORK_5G, NETWORK_3G, NETWORK_2G, NETWORK_UNKNOWN, NETWORK_NO
    }

    @RequiresPermission(permission.ACCESS_NETWORK_STATE)
    fun isConnected(context: Context?): Boolean {
        return context?.let {
            val info = getActiveNetworkInfo(it)
            info != null && info.isConnected
        } ?: false
    }

    /**
     * Return type of network.
     *
     * Must hold `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`
     *
     * @return type of network
     *
     *  * [NetworkUtils.NetworkType.NETWORK_ETHERNET]
     *  * [NetworkUtils.NetworkType.NETWORK_WIFI]
     *  * [NetworkUtils.NetworkType.NETWORK_4G]
     *  * [NetworkUtils.NetworkType.NETWORK_3G]
     *  * [NetworkUtils.NetworkType.NETWORK_2G]
     *  * [NetworkUtils.NetworkType.NETWORK_UNKNOWN]
     *  * [NetworkUtils.NetworkType.NETWORK_NO]
     *
     */
    @RequiresPermission(permission.ACCESS_NETWORK_STATE)
    fun getNetworkType(context: Context): NetworkType {
        if (getEthernet(context)) {
            return NetworkType.NETWORK_ETHERNET
        }
        val info = getActiveNetworkInfo(context)
        if (info != null && info.isAvailable) {
            if (info.type == ConnectivityManager.TYPE_WIFI) {
                return NetworkType.NETWORK_WIFI
            } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
                when (info.subtype) {
                    TelephonyManager.NETWORK_TYPE_GSM, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> return NetworkType.NETWORK_2G
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> return NetworkType.NETWORK_3G
                    TelephonyManager.NETWORK_TYPE_IWLAN, TelephonyManager.NETWORK_TYPE_LTE -> return NetworkType.NETWORK_4G
                    TelephonyManager.NETWORK_TYPE_NR -> return NetworkType.NETWORK_5G
                    else -> {
                        val subtypeName = info.subtypeName
                        if ("TD-SCDMA".equals(subtypeName, ignoreCase = true)
                            || "WCDMA".equals(subtypeName, ignoreCase = true)
                            || "CDMA2000".equals(subtypeName, ignoreCase = true)
                        ) {
                            return NetworkType.NETWORK_3G
                        }
                    }
                }
            }
        }
        return NetworkType.NETWORK_UNKNOWN
    }

    /**
     * Return whether using ethernet.
     *
     * Must hold
     * `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`
     *
     * @return `true`: yes<br></br>`false`: no
     */
    @RequiresPermission(permission.ACCESS_NETWORK_STATE)
    private fun getEthernet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            ?: return false
        val info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET) ?: return false
        val state = info.state ?: return false
        return state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING
    }

    @RequiresPermission(permission.ACCESS_NETWORK_STATE)
    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            ?: return null
        return cm.activeNetworkInfo
    }

    @RequiresPermission(permission.ACCESS_NETWORK_STATE)
    fun networkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        if (network != null) {
            val nc = cm.getNetworkCapabilities(network)
            if (nc != null) {
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) { //WIFI
                    return true
                } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) { //移动数据
                    return true
                }
            }
        }
        return false
    }


}