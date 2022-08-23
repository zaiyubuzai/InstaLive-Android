package com.example.baselibrary.utils

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import splitties.systemservices.telephonyManager
import java.lang.Exception

/**
 * 电话监听管理
 */
object TelephonyUtil {
    private var mPhoneListener: TXPhoneStateListener? = null
    private var mOnStopListener: OnTelephoneListener? = null
    fun initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = TXPhoneStateListener()
            try {
                telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
            } catch (e: Exception) {
            }
        }
    }

    fun uninitPhoneListener() {
        if (mPhoneListener != null) {
            telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    class TXPhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(
            state: Int,
            incomingNumber: String
        ) {
            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    mOnStopListener?.onRinging()
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    mOnStopListener?.onOffhook()
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    mOnStopListener?.onIdle()
                }
            }
        }
    }

    fun setOnTelephoneListener(listener: OnTelephoneListener?) {
        mOnStopListener = listener
    }

    interface OnTelephoneListener {
        fun onRinging()
        fun onOffhook()
        fun onIdle()
    }
}