package com.example.instalive.app.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.baselibrary.views.BaseActivity
import com.example.instalive.InstaLiveApp
import com.example.instalive.app.Constants
import com.example.instalive.app.Constants.EVENT_BUS_KEY_LOGOUT
import com.jeremyliao.liveeventbus.LiveEventBus
import com.jeremyliao.liveeventbus.core.Observable

@ExperimentalStdlibApi
abstract class InstaBaseActivity<VMD : ViewModel, VDB : ViewDataBinding>: BaseActivity<VMD, VDB>() {

    protected var screenName: String? = null

    protected val defaultEventBus: Observable<Any> by lazy {
        LiveEventBus.get(Constants.EVENT_BUS_KEY_DEFAULT)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        InstaLiveApp.appInstance.addActivity(this)
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(AppBackgroundObserver)
        LiveEventBus.get(EVENT_BUS_KEY_LOGOUT).observe(this, {
            if (!localClassName.contains("app.login.NotLoginYetActivity")) {
                finish()
            }
        })
    }

    override fun onDestroy() {
        InstaLiveApp.appInstance.removeActivity(this)
        super.onDestroy()
    }
}