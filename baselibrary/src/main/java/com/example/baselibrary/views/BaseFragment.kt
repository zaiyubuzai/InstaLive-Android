package com.example.baselibrary.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.baselibrary.BuildConfig
import com.example.baselibrary.R
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.utils.baseToast
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseFragment<VMD : ViewModel, VDB : ViewDataBinding> : Fragment() {
    private lateinit var binding: VDB
    protected val viewModel: VMD by lazy {
        initViewModel()
    }
    protected var screenName: String? = null
    private val activityProvider: ViewModelProvider by lazy {
        ViewModelProvider(requireActivity())
    }
    private val fragmentProvider: ViewModelProvider by lazy {
        ViewModelProvider(this)
    }
    private val commonProgressDialog: LoadingPopupView by lazy {
        XPopup.Builder(context).dismissOnBackPressed(false)
            .asLoading()
    }

    protected lateinit var activity: AppCompatActivity


    var mResume = false
    private var mHidden = false

    open fun isRealVisible(): Boolean {
        return !mHidden && mResume
    }

    override fun onStop() {
        super.onStop()
        mResume = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mHidden = hidden
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as AppCompatActivity
    }

    override fun onResume() {
        super.onResume()
    }
    protected fun showCommonProgress() {
        commonProgressDialog.show()
    }

    protected fun hideCommonProgress() {
        commonProgressDialog.dismiss()
    }
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initExtras()
        if (viewModel is BaseViewModel) {
            (viewModel as BaseViewModel).errorTypeLiveData.observe(this) {
                if (it == ErrorType.NETWORK) {
                    baseToast(R.string.no_network_connection_base)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (this@BaseFragment::binding.isInitialized.not()) {
            val databindingConfig = getDataBindingConfig()
            val binding =
                DataBindingUtil.inflate<VDB>(inflater, databindingConfig.layout, container, false)
            getDataBindingConfig().bindingParams.forEach { key, value ->
                binding.setVariable(key, value)
            }
            this.binding = binding
        }
        return this@BaseFragment.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData(savedInstanceState)
    }

    open fun initExtras(){}

    protected abstract fun initViewModel(): VMD

    protected abstract fun getDataBindingConfig(): DataBindingConfig

    protected abstract fun initData(savedInstanceState: Bundle?)

    fun <T : ViewModel> getActivityViewModel(viewModelClass: Class<T>): T {
        return activityProvider[viewModelClass]
    }

    fun <VMD : ViewModel> getFragmentViewModel(viewModelClass: Class<VMD>): VMD {
        return fragmentProvider[viewModelClass]
    }

    //---------------------------------------
    protected open fun showDialogFragment(dialogFragment: DialogFragment): Unit {
        val tag =
            if (dialogFragment.tag != null) dialogFragment.tag else dialogFragment.javaClass.simpleName
        showDialogFragment(dialogFragment, tag)
    }

    protected open fun showDialogFragment(
        dialogFragment: DialogFragment,
        tag: String?,
    ) {
        dialogFragment.show(parentFragmentManager, tag)
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

    open fun networkFailCallback(){}
}