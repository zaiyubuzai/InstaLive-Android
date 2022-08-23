package com.example.instalive.utils

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
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=com.fambase.venus")))
        }
    }

    fun showPlayReview(activity: Activity) {
        redirectVenusFambasePlayStore(activity)
    }
}