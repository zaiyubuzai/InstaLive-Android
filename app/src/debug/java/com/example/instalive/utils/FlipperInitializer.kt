package com.example.instalive.utils

import android.app.Application
import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.Interceptor

object FlipperInitializer {
    private val networkPlugin = NetworkFlipperPlugin()
    var interceptor: Interceptor = FlipperOkhttpInterceptor(networkPlugin)

    fun initFlipper(app: Application) {
        SoLoader.init(app, false)
        val client = AndroidFlipperClient.getInstance(app)
        client.addPlugin(InspectorFlipperPlugin(app, DescriptorMapping.withDefaults()))
        client.addPlugin(networkPlugin)

        client.addPlugin(DatabasesFlipperPlugin(app))
        client.addPlugin(SharedPreferencesFlipperPlugin(app,
            listOf(
                SharedPreferencesFlipperPlugin.SharedPreferencesDescriptor("instaLiveState", Context.MODE_PRIVATE),
                SharedPreferencesFlipperPlugin.SharedPreferencesDescriptor("instaLiveUserState", Context.MODE_PRIVATE)
                )
            )
        )

        client.start()
    }
}