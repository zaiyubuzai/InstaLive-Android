package com.example.instalive.app.login

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.model.CountryCodeData
import com.example.baselibrary.utils.hideKeyboard
import com.example.baselibrary.utils.onDone
import com.example.baselibrary.utils.readAssetsFile
import com.example.baselibrary.utils.showKeyboard
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.InstaLiveApp
import com.example.instalive.R
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.base.SharedViewModel
import com.example.instalive.databinding.FragmentPhoneLoginBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_phone_login.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import splitties.dimensions.dp
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault
import splitties.mainhandler.mainHandler
import splitties.systemservices.inputMethodManager
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class PhoneLoginFragment : BaseFragment<PhoneLoginViewModel, FragmentPhoneLoginBinding>() {

    private var currentCountryCode = DEFAULT_COUNTRY_CODE
    private var currentCountry = DEFAULT_COUNTRY
    private val sharedViewModel by lazy {
        InstaLiveApp.appInstance.getAppViewModelProvider().get(SharedViewModel::class.java)
    }

    var showAppBar: Boolean by argOrDefault(false)
    var source: String by arg()

    private var isShowToUser = true

    override fun initViewModel(): PhoneLoginViewModel {
        return getActivityViewModel(PhoneLoginViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_phone_login, viewModel)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    @SuppressLint("SetTextI18n")
    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        if (showAppBar) {
            appBar.isVisible = true
            toolbar.navigationIcon = activity.getDrawable(R.mipmap.icon_back_white)
            toolbar.setNavigationOnClickListener {
                edtPhone.hideKeyboard()
                activity.finish()
            }
        } else {
            appBar.isVisible = false
        }

        val countryCode = InstaLivePreferences.countryCodeJson
            ?: activity.assets?.readAssetsFile("country_codes.json")
        if (!countryCode.isNullOrEmpty()) {
            val type = object : TypeToken<List<CountryCodeData>>() {}.type
            val countryList = Gson().fromJson<List<CountryCodeData>>(countryCode, type)
            val country = Locale.getDefault().country
            val d = countryList.firstOrNull {
                it.code == country
            }
            if (d != null) {
                currentCountryCode = d.dialCode
                currentCountry = d.code
                selectCountry.text = "${d.code}${d.dialCode}"
                edtPhone.requestFocus()
            }
        }
        val isLogin = activity.intent.getBooleanExtra("is_login", false)
        if (isLogin) {
            phoneLoginTitle1.text = getString(R.string.fb_login_welcome_back)
            phoneLoginTitle2.isVisible = false
            phoneLoginTitle1.isVisible = true
            policyText.textResource = R.string.fb_login_lost_access_phone_number

            policyText.compoundDrawablePadding = context?.dp(4) ?: 10
        } else {
            phoneLoginTitle2.text = getString(R.string.fb_login_welcome_to_fambase)
            phoneLoginTitle2.isVisible = true
            phoneLoginTitle1.isVisible = false
            val policy = Spanner(getString(R.string.fb_login_policy))
                .span(
                    getString(R.string.fb_login_term_service),
                    Spans.bold(),
                    Spans.underline(),
                    Spans.click {
                        edtPhone.hideKeyboard()
                        // FIXME: 2022/8/18
                    },
                    Spans.foreground(Color.parseColor("#aab2e6"))
                )
                .span(
                    getString(R.string.fb_login_privacy_policy),
                    Spans.bold(),
                    Spans.underline(),
                    Spans.click {
                        edtPhone.hideKeyboard()
                        // FIXME: 2022/8/18
                    },
                    Spans.foreground(Color.parseColor("#aab2e6"))
                )
            policyText.movementMethod = LinkMovementMethod()
            policyText.text = policy
            if (!showAppBar) {
                policyText.isVisible = false
            }
        }

        edtPhone.requestFocus()
        edtPhone.showKeyboard()
        inputMethodManager.showSoftInput(
            edtPhone,
            InputMethodManager.SHOW_FORCED,
        )
        selectCountry.onClick {
            edtPhone.hideKeyboard()
            showDialogFragment(SelectCountryDialog {
                currentCountryCode = it.dialCode
                currentCountry = it.code
                selectCountry.text = "${it.code}${it.dialCode}"
                edtPhone.requestFocus()
            })
        }
        btnSend.onClick {
            val number = "$currentCountryCode${edtPhone.text}"
            val timestamp = sharedViewModel.phonePasscodeMap["${number}_phone"]
            if (timestamp != null && System.currentTimeMillis() - timestamp < 60 * 1000) {
                toPasscode(timestamp, number)
            } else {
                viewModel.sendPhonePasscode(number, source, currentCountryCode)
                btnSend.isEnabled = false
            }
        }
        edtPhone.doAfterTextChanged {
            it?.let {
                btnSend.isEnabled = it.length >= 5
                numberClear.isVisible = it.isNotEmpty()
            }
        }
        edtPhone.onDone {
            if (btnSend.isEnabled) {

                btnSend.isEnabled = false
            }
        }

        edtPhone.setOnFocusChangeListener { v, hasFocus ->
            Timber.d("hasFocus:$hasFocus")
        }

        numberClear.onClick {
            edtPhone.setText("")
        }

        policyText.onClick {
            if (isLogin) {
                (activity as LoginActivity).redirectOriginalPhone(
                    edtPhone.text?.toString().toString().trim(), currentCountry, currentCountryCode
                )
            }
        }

        initObserver()

    }

    private fun initObserver(){
        viewModel.phoneVerifyResult.observe(this, {
            val number = "$currentCountryCode${edtPhone.text}"
            val timestamp = System.currentTimeMillis()
            sharedViewModel.phonePasscodeMap["${number}_phone"] = timestamp
            btnSend.isEnabled = true
            if (it != null) {
                toPasscode(timestamp, number)
            }
        })

        viewModel.errorCodeLiveData.observe(this, {
            btnSend?.isEnabled = true
            errText?.isVisible = true
            errText?.text = viewModel.errorMessageLiveData.value
        })

        viewModel.loadingStatsLiveData.observe(this, {
            progress?.isVisible = it == StatusEvent.LOADING
        })
    }

    private fun toPasscode(timestamp: Long, phone: String){
        if (activity is LoginActivity) {
            sharedViewModel.startGlobalVerifyCodePhoneTicking(timestamp)
            (activity as LoginActivity).redirectPhonePasscode(
                phone,
                currentCountryCode
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isShowToUser) edtPhone.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        edtPhone.hideKeyboard()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isShowToUser = !hidden
        if (!hidden) {
            mainHandler.postDelayed({
                edtPhone?.requestFocus()
                edtPhone?.showKeyboard()
                inputMethodManager.showSoftInput(
                    edtPhone,
                    InputMethodManager.SHOW_FORCED,
                )

            }, 300)
        }
    }

    companion object {
        const val DEFAULT_COUNTRY_CODE = "+1"
        const val DEFAULT_COUNTRY = "US"
    }
}