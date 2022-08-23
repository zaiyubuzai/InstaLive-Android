package com.example.instalive.utils

import android.content.Context
import android.net.Uri
import com.example.instalive.app.web.InstaWebActivity
import splitties.intents.start
import timber.log.Timber

@ExperimentalStdlibApi
object DeeplinkHelper {
    const val DEEPLINK_SCHEME = "insta_live"
    const val HTTPS_SCHEME = "https"
    const val HOST = ""

    fun handleDeeplink(
        uri: Uri,
        context: Context,
    ): Boolean {
        return when (uri.scheme) {
            DEEPLINK_SCHEME -> handleScheme(uri, context)
            HTTPS_SCHEME -> handleHttpsScheme(uri, context)
            else -> false
        }
    }

    private fun handleHttpsScheme(uri: Uri, context: Context): Boolean {
        Timber.d("uri = ${uri.host}")
        if (uri.host == HOST) {
            if (uri.pathSegments.size == 0) {
                return false
            }
            val firstPath = uri.pathSegments[0]
            val lastSegment = uri.lastPathSegment
            when (firstPath) {
            }
        } else {
            redirectCustomTabs(uri.toString(), context)
        }
        return true
    }

    //    insta://leave_group?id={{group_id}}
    private fun handleScheme(
        uri: Uri,
        context: Context,
    ): Boolean {
        Timber.d("uri.host: ${uri.host} ${uri}")
        return when (uri.host) {
            "web" -> redirectWeb(uri, context)
            "profile" -> redirectProfile(uri, context)
            "message" -> redirectMessage(uri, context)
            "upgrade_app" -> redirectPlay(context)
            "group" -> redirectGroupInvitation(uri, context)
            "live" -> redirectLive(uri, context)
            else -> false
        }
    }


    private fun redirectLive(
        uri: Uri,
        context: Context,
    ): Boolean {

        return false
    }

    private fun redirectGroupInvitation(uri: Uri, context: Context): Boolean {
        return true
    }

    private fun redirectPlay(context: Context): Boolean {
        return true
    }

    private fun redirectProfile(uri: Uri, context: Context): Boolean {
        return true
    }

    private fun redirectWeb(uri: Uri, context: Context): Boolean {
        return true
    }

    private fun redirectMessage(uri: Uri, context: Context): Boolean {
        return true
    }

    private fun redirectCustomTabs(url: String, context: Context) {
        context.start(InstaWebActivity) { _, extrasSpec ->
            extrasSpec.url = url
        }
    }
}