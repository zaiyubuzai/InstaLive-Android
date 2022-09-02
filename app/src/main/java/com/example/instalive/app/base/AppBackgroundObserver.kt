package com.example.instalive.app.base

import com.example.baselibrary.views.BaseBackgroundObserver
import com.example.instalive.app.Constants
import com.example.instalive.app.SessionPreferences
import com.example.instalive.utils.DMSocketIO
import com.jeremyliao.liveeventbus.LiveEventBus

@ExperimentalStdlibApi
object AppBackgroundObserver: BaseBackgroundObserver() {
    var isAppBackground = false
    override fun listenerOnStart() {
        if (SessionPreferences.id.isNotEmpty()){
            DMSocketIO.initSocket()
        }
        isAppBackground = false
        LiveEventBus.get(Constants.EVENT_BUS_KEY_APP_STOP).post(0)
    }

    override fun listenerOnStop() {
        DMSocketIO.removeConversationTimeToken()
        DMSocketIO.releaseSocket()
        isAppBackground = true
        LiveEventBus.get(Constants.EVENT_BUS_KEY_APP_STOP).post(1)
    }
}