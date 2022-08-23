package com.example.instalive.app.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.utils.hideKeyboard
import com.example.baselibrary.utils.onDone
import com.example.baselibrary.utils.showKeyboard
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.FragmentOriginalPhoneBinding
import kotlinx.android.synthetic.main.fragment_original_phone.*
import splitties.mainhandler.mainHandler
import splitties.systemservices.inputMethodManager
import splitties.views.onClick
import timber.log.Timber

@ExperimentalStdlibApi
class OriginalPhoneFragment : BaseFragment<OriginalPhoneViewModel, FragmentOriginalPhoneBinding>() {

    var currentCountryCode: String = (DEFAULT_COUNTRY_CODE)
    var currentCountry: String = (DEFAULT_COUNTRY)
    var source: String = ""
    var phone: String = ""
    var isFirst = true
    private var isShowToUser = true
    private var email = ""

    override fun initViewModel(): OriginalPhoneViewModel {
        return getActivityViewModel(OriginalPhoneViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_original_phone, viewModel)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    @SuppressLint("SetTextI18n")
    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        viewModel.reset()
        appBar.isVisible = true
        toolbar.setNavigationOnClickListener {
            edtPhone.hideKeyboard()
            activity.onBackPressed()
        }

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

        viewModel.errorCodeLiveData.observe(this, {
            btnSend.isEnabled = true

            if (it != 0) {
                errText.isVisible = true
                errText.text = viewModel.errorMessageLiveData.value
            }
        })
        viewModel.loadingStatsLiveData.observe(this, {
            progress.isVisible = it == StatusEvent.LOADING
        })

    }

    override fun onResume() {
        super.onResume()
        if (isShowToUser) edtPhone.requestFocus()
        if (isFirst) {
            isFirst = false
            selectCountry.text = "${currentCountry}${currentCountryCode}"
            edtPhone.setText(phone)
            edtPhone.setSelection(phone.length)
            btnSend.isEnabled = phone.length >= 5
            numberClear.isVisible = phone.isNotEmpty()
        }
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