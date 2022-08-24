package com.example.instalive.app.login

import android.os.Bundle
import androidx.core.view.isVisible
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.hideKeyboard
import com.example.baselibrary.utils.showKeyboard
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.InstaLiveApp
import com.example.instalive.R
import com.example.instalive.app.Constants
import com.example.instalive.app.SESSION
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.app.home.HomeActivity
import com.example.instalive.databinding.FragmentVerifyCodePhoneBinding
import com.example.instalive.utils.marsToast
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.fragment_verify_code_phone.*
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault
import splitties.fragmentargs.argOrNull
import splitties.fragments.start
import splitties.views.onClick

@ExperimentalStdlibApi
class PhonePasscodeFragment :
    BaseFragment<PhonePasscodeViewModel, FragmentVerifyCodePhoneBinding>() {
    var phone: String? by argOrNull()
    var email: String? by argOrNull()
    var dialCode: String? by argOrNull()
    var source: String by arg()
    var newPhone: Boolean by argOrDefault(false)
    var oldPhoneNumber: String by argOrDefault("")
    var emailVerifyToken: String by argOrDefault("")
    private var isShowToUser = true

    private val sharedViewModel by lazy {
        InstaLiveApp.appInstance.getAppViewModelProvider().get(SharedViewModel::class.java)
    }

    override fun initViewModel(): PhonePasscodeViewModel {
        return getActivityViewModel(PhonePasscodeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_verify_code_phone, viewModel)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isShowToUser = !hidden
    }

    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        passcodeInput.setText("")
        passcodeInput.requestFocus()
        passcodeInput.showKeyboard()
        toolbar.setNavigationOnClickListener {
            activity.onBackPressed()
        }
        passcodeInput.setOnTextFinishListener { text, _ ->
            if (phone != null) {
                viewModel.phoneLogin(phone!!, text as String, "")
            }
        }

        btnResendCode.onClick {
            if (phone != null) {
                btnResendCode.isEnabled = false
                val count = sharedViewModel.verifyCodePhoneLeft.value
                if (count == 0) {
                    progress.isVisible = true
                    dialCode?.let { it1 -> viewModel.sendPhonePasscode(phone ?: "", source, it1) }
                }
            }
        }
        initObserver()
    }

    private fun initObserver() {
        if (phone != null) {
            sharedViewModel.verifyCodePhoneLeft.observe(this, {
                if (newPhone) return@observe
                if (it > 0) {
                    btnResendCode.isEnabled = false
                    btnResendCode.text = getString(R.string.fb_code_count_down, it.toString())
                } else {
                    btnResendCode.text = getString(R.string.fb_did_not_get_the_code_span)
                    btnResendCode.isEnabled = true
                }
            })
        }

        viewModel.loginResponse.observe(this, {
            passcodeInput.hideKeyboard()
            SESSION.saveLoginData(it)
            start<HomeActivity> { }
            LiveEventBus.get(Constants.EVENT_BUS_KEY_LOGIN).post(Constants.EVENT_BUS_LOGIN_SUCCESS)
            requireActivity().finish()
        })
        viewModel.loadingStatsLiveData.observe(this, {
            progress.isVisible = it == StatusEvent.LOADING
        })
        viewModel.phoneVerifyResult.observe(this, {
            if (phone != null) {
                val timestamp = System.currentTimeMillis()
                sharedViewModel.phonePasscodeMap["${phone}_phone"] = timestamp
                sharedViewModel.startGlobalVerifyCodePhoneTicking(timestamp)
            }
            marsToast(R.string.fb_code_sent)
        })
        viewModel.errorCodeLiveData.observe(this, {
            if (it == 1154) {
                //没用户名，去填用户名
                if (isShowToUser) {
                    (activity as LoginActivity).redirectFullName(
                        phone ?: "",
                        passcodeInput.text.toString()
                    )
                }
            } else {
                errText?.isVisible = true
                errText?.text = viewModel.errorMessageLiveData.value.toString()
                passcodeInput?.requestFocus()
            }
        })
    }
}