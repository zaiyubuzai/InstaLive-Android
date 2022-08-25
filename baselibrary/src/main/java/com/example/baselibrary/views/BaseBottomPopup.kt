package com.example.baselibrary.views

import android.content.Context
import androidx.lifecycle.ViewModel
import com.lxj.xpopup.core.BottomPopupView

/**
 * 再次启动popup时，ViewModel中的LiveData必须重制
 */
abstract class BaseBottomPopup<VMD : ViewModel>(context: Context) :
    BottomPopupView(context) {

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