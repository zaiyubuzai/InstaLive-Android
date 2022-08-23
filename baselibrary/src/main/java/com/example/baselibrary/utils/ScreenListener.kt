package com.example.baselibrary.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter


class ScreenListener(context: Context) {

    private var mContext: Context? = null
    private var mScreenStatusFilter: IntentFilter? = null
    private var mScreenStatusListener: ScreenStatusListener? = null

    init {
        mContext = context
        mScreenStatusFilter = IntentFilter()
        mScreenStatusFilter?.addAction(Intent.ACTION_SCREEN_ON)
        mScreenStatusFilter?.addAction(Intent.ACTION_SCREEN_OFF)
        mScreenStatusFilter?.addAction(Intent.ACTION_USER_PRESENT)
    }

    private val mScreenStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_SCREEN_ON == action) { // 开屏
                mScreenStatusListener?.onScreenOn()
            } else if (Intent.ACTION_SCREEN_OFF == action) { // 锁屏
                mScreenStatusListener?.onScreenOff()
            } else if (Intent.ACTION_USER_PRESENT == action) { //解锁
                mScreenStatusListener?.userPresent()
            }
        }
    }

    //设置监听
    fun setScreenStatusListener(l: ScreenStatusListener) {
        mScreenStatusListener = l
    }


    //开始监听
    fun startListen() {
        if (mContext != null) {
            mContext!!.registerReceiver(mScreenStatusReceiver, mScreenStatusFilter)
        }
    }

    //结束监听
    fun stopListen() {
        if (mContext != null) {
            mContext!!.unregisterReceiver(mScreenStatusReceiver)
        }
    }

    interface ScreenStatusListener {
        fun onScreenOn()
        fun onScreenOff()
        fun userPresent()
    }






}