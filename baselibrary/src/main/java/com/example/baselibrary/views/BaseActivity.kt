package com.example.baselibrary.views

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.baselibrary.BuildConfig
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity<VMD : ViewModel, VDB : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var viewModel: VMD
    protected lateinit var binding: VDB

    private val activityProvider: ViewModelProvider by lazy {
        ViewModelProvider(this)
    }

    private val commonProgressDialog: LoadingPopupView by lazy {
        XPopup.Builder(this).dismissOnBackPressed(false)
            .asLoading()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) // 跟系統表示要渲染 system bar 背景。
        window.statusBarColor = Color.TRANSPARENT

        preInit()
        viewModel = initViewModel()
        val binding = DataBindingUtil.setContentView<VDB>(this, getDataBindingConfig().layout)
        binding.lifecycleOwner = this
        getDataBindingConfig().bindingParams.forEach { key, value ->
            binding.setVariable(key, value)
        }
        this.binding = binding
        initData(savedInstanceState)
        initExtras()
    }

    open fun preInit() {}
    open fun initExtras() {}

    abstract fun initData(savedInstanceState: Bundle?)
    abstract fun initViewModel(): VMD
    abstract fun getDataBindingConfig(): DataBindingConfig

    fun getActivityViewModel(viewModelClass: Class<VMD>): VMD {
        return activityProvider[viewModelClass]
    }

    fun <T : ViewModel> getViewModel(viewModelClass: Class<T>): T {
        return activityProvider[viewModelClass]
    }

    //---------------------------------------
    protected open fun showDialogFragment(dialogFragment: DialogFragment) {
        val tag =
            if (dialogFragment.tag != null) dialogFragment.tag else dialogFragment.javaClass.simpleName
        showDialogFragment(dialogFragment, tag)
    }

    protected open fun showDialogFragment(
        dialogFragment: DialogFragment,
        tag: String?,
    ) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.executePendingTransactions()
            dialogFragment.show(supportFragmentManager, tag)
        }
    }

    protected fun showCommonProgress() {
        commonProgressDialog.show()
    }

    protected fun hideCommonProgress() {
        commonProgressDialog.dismiss()
    }

    @SuppressLint("CheckResult")
    fun oneTimeInternetCheck(observer: SingleObserver<Boolean>) {
        val settings = if (BuildConfig.DEBUG) {
            InternetObservingSettings.builder()
                .host("www.bing.com")
                .strategy(SocketInternetObservingStrategy())
                .build()
        } else {
            InternetObservingSettings.builder()
                .host("www.google.com")
                .strategy(SocketInternetObservingStrategy())
                .build()
        }
        val single = ReactiveNetwork.checkInternetConnectivity(settings)
        single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                if (!it) {
                    networkFailCallback()
                }
                it
            }
            .subscribe(observer)
    }

    open fun networkFailCallback() { }
}