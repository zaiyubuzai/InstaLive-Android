package com.venus.livesdk.rtc

import android.content.Context
import io.agora.rtc.Constants
import io.agora.rtc.RtcEngine

interface IAgoraConfig {

    //直播相关
    var mHandler: AgoraEventHandler?
    var mRtcEngine: RtcEngine?

    @Throws(Exception::class)
    fun configLive(context: Context, appId: String, fileSize: Int) {
        if (mHandler == null) {
            mHandler = AgoraEventHandler()
        }
        mRtcEngine = RtcEngine.create(context, appId, mHandler)
        mRtcEngine?.setLogFilter(Constants.LOG_FILTER_ERROR)
        mRtcEngine?.setLogFileSize(fileSize)
    }

    fun rtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    fun registerEventHandler(handler: EventHandler?) {
        mHandler?.addHandler(handler)
    }

    fun removeEventHandler(handler: EventHandler?) {
        mHandler?.removeHandler(handler)
    }

    fun getHandlerSize(): Int {
        return mHandler?.eventHandler ?: 0
    }

    fun destroyRtcEngine(){
        RtcEngine.destroy()
    }
}