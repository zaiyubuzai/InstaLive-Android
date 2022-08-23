package com.example.baselibrary.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

object Utils {

    fun goToAppSettings(context: Context, applicationPackage: String) {
        val i = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$applicationPackage")
        )
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, i, null)
    }
}