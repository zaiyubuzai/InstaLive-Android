package com.example.instalive.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import com.example.instalive.InstaLiveApp
import splitties.systemservices.audioManager
import splitties.systemservices.telephonyManager
import java.net.URLEncoder
import java.util.*
import android.os.Build
import android.provider.Settings
import timber.log.Timber


object SysUtils {
    fun getPackageInfo(): PackageInfo? {
        return try {
            val packageName = InstaLiveApp.appInstance.packageName
            InstaLiveApp.appInstance.packageManager.getPackageInfo(packageName, 0)
        } catch (e: Exception) {
            null
        }
    }

    fun encodeUrl(str: String): String? {
        if (str.isEmpty()) {
            return ""
        }
        return try {
            URLEncoder.encode(str, "UTF-8")
        } catch (e: Exception) {
            return ""
        }
    }

    fun getLanguageCode(): String {
        return encodeUrl(Locale.getDefault().language) ?: ""
    }

    fun getSimOperatorName(): String {
        return telephonyManager.simOperatorName
    }

    fun getTimezone(): String {
        return TimeZone.getDefault().id
    }

    fun isHeadphonesPlugged(): Boolean {
        return audioManager.isWiredHeadsetOn
    }

    private fun redirectVenusFambasePlayStore(context: Context) {
        val uri: Uri = Uri.parse("market://details?id=com.fambase.venus")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.fambase.venus")
                )
            )
        }
    }

    fun showPlayReview(activity: Activity) {
        redirectVenusFambasePlayStore(activity)
    }

    @SuppressLint("HardwareIds")
    fun getUniqueId(context: Context): String? {
        var id: String? = null
        val androidID: String =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidID
    }

    /**
     * 获取设备唯一ID
     * @return
     */
    @SuppressLint("MissingPermission")
    fun getUUID(): String {
        var serial: String? = null
        val m_szDevIDShort =
            "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10 //13 位
        try {
            serial = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                Build.USER
            }
            //API>=9 使用serial号
            return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (exception: java.lang.Exception) {
            //serial需要一个初始化
            serial = "serial_insta_live" // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
    }

    fun printUUID() {
        Timber.d("Build.BOARD: ${Build.BOARD}")
        Timber.d("Build.BRAND: ${Build.BRAND}")
        Timber.d("Build.CPU_ABI: ${Build.CPU_ABI}")
        Timber.d("Build.DEVICE: ${Build.DEVICE}")
        Timber.d("Build.DISPLAY: ${Build.DISPLAY}")
        Timber.d("Build.HOST: ${Build.HOST}")
        Timber.d("Build.ID: ${Build.ID}")
        Timber.d("Build.MANUFACTURER: ${Build.MANUFACTURER}")
        Timber.d("Build.MODEL: ${Build.MODEL}")
        Timber.d("Build.TAGS: ${Build.TAGS}")
        Timber.d("Build.TYPE: ${Build.TYPE}")
        Timber.d("Build.USER: ${Build.USER}")
        Timber.d("Build.BOOTLOADER: ${Build.BOOTLOADER}")
        Timber.d("Build.FINGERPRINT: ${Build.FINGERPRINT}")
        Timber.d("Build.HARDWARE: ${Build.HARDWARE}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Timber.d("Build.SKU: ${Build.SKU}")
            Timber.d("Build.SOC_MANUFACTURER: ${Build.SOC_MANUFACTURER}")
            Timber.d("Build.SOC_MODEL: ${Build.SOC_MODEL}")
        }
    }


}