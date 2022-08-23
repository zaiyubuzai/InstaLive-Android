package com.example.baselibrary.views

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import com.lxj.xpopup.core.CenterPopupView

abstract class BaseCenterPopup<VMD : ViewModel>(context: Context) :
    CenterPopupView(context), LifecycleOwner {

    protected lateinit var viewModel: VMD
    private lateinit var lifecycleRegistry: LifecycleRegistry
    protected var screenName: String? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        viewModel = initViewModel()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun onDismiss() {
        super.onDismiss()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    protected abstract fun initViewModel(): VMD

    protected abstract fun initData()

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}