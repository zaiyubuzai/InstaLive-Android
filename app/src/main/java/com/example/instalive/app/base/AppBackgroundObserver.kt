package com.example.instalive.app.base

import com.example.baselibrary.views.BaseBackgroundObserver
import com.example.instalive.app.SessionPreferences
import com.example.instalive.utils.DMSocketIO

@ExperimentalStdlibApi
object AppBackgroundObserver: BaseBackgroundObserver() {

    override fun listenerOnStart() {
        if (SessionPreferences.id.isNotEmpty()){
            DMSocketIO.initSocket()
        }
    }

    override fun listenerOnStop() {
        DMSocketIO.removeConversationTimeToken()
        DMSocketIO.releaseSocket()
    }
}