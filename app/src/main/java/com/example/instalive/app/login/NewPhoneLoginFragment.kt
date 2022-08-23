package com.example.instalive.app.login

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.example.instalive.R
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.databinding.FragmentPhoneLoginBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_phone_login.*
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault
import splitties.mainhandler.mainHandler
import splitties.systemservices.inputMethodManager
import splitties.views.onClick
import splitties.views.textResource
import timber.log.Timber
import java.util.*

@ExperimentalStdlibApi
class NewPhoneLoginFragment : BaseFragment<PhoneLoginViewModel, FragmentPhoneLoginBinding>() {

    private var currentCountryCode = DEFAULT_COUNTRY_CODE
    private var currentCountry = DEFAULT_COUNTRY
    var source: String by arg()
    var showAppBar: Boolean by argOrDefault(false)
    var oldPhoneNumber: String by argOrDefault("")
    var emailVerifyToken: String by argOrDefault("")
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

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        viewModel.reset()
        if (showAppBar) {
            appBar.isVisible = true
            toolbar.navigationIcon = activity.getDrawable(R.mipmap.icon_back_white)
            toolbar.setNavigationOnClickListener {
                edtPhone.hideKeyboard()
                activity.onBackPressed()
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

        phoneLoginTitle2.textResource = R.string.fb_login_new_phone_number
        phoneLoginTitle2.isVisible = true
        phoneLoginTitle1.isVisible = false
        signUpDesc.textResource = R.string.fb_login_enter_new_phone_number
        policyText.isVisible = false


        edtPhone.requestFocus()
        edtPhone.showKeyboard()
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
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
            (activity as LoginActivity).redirectOriginalPhone(
                edtPhone.text?.toString().toString().trim(), currentCountry, currentCountryCode
            )
        }

        viewModel.errorCodeLiveData.observe(this, {
            btnSend.isEnabled = true

            errText.isVisible = true
            errText.text = viewModel.errorMessageLiveData.value
        })

        viewModel.loadingStatsLiveData.observe(this, {
            progress.isVisible = it == StatusEvent.LOADING
        })
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
                inputMethodManager.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }, 300)
        }
    }

    companion object {
        const val DEFAULT_COUNTRY_CODE = "+1"
        const val DEFAULT_COUNTRY = "US"
    }
}