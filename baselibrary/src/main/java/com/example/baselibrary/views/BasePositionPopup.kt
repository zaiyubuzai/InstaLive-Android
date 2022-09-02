package com.example.baselibrary.views

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import com.lxj.xpopup.core.PositionPopupView

abstract class BasePositionPopup<VMD : ViewModel>(context: Context) :
    PositionPopupView(context){

    protected lateinit var viewModel: VMD
    protected var screenName: String? = null

    override fun onCreate() {
        super.onCreate()
        viewModel = initViewModel()
        initData()
    }

    protected abstract fun initData()

    protected abstract fun initViewModel(): VMD

}