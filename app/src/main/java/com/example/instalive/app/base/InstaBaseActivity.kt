package com.example.instalive.app.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.baselibrary.views.BaseActivity
import com.example.instalive.InstaLiveApp
@ExperimentalStdlibApi
abstract class InstaBaseActivity<VMD : ViewModel, VDB : ViewDataBinding>: BaseActivity<VMD, VDB>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        InstaLiveApp.appInstance.addActivity(this)
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(AppBackgroundObserver)
    }

    override fun onDestroy() {
        InstaLiveApp.appInstance.removeActivity(this)
        super.onDestroy()
    }
}