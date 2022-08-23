package com.example.instalive.app.login

import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import com.example.baselibrary.utils.showKeyboard
import com.example.baselibrary.views.BaseFragment
import com.example.baselibrary.views.DataBindingConfig
import com.example.instalive.R
import com.example.instalive.databinding.FragmentVerifyCodePhoneBinding
import kotlinx.android.synthetic.main.fragment_verify_code_phone.*
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault
import splitties.fragmentargs.argOrNull
import splitties.views.onClick

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

    @OptIn(ExperimentalStdlibApi::class)
    override fun initData(savedInstanceState: Bundle?) {
        screenName = "login_view"
        passcodeInput.setText("")
        passcodeInput.requestFocus()
        passcodeInput.showKeyboard()
        toolbar.setNavigationOnClickListener {
            activity.onBackPressed()
        }
        passcodeInput.setOnTextFinishListener { text, _ ->
            (activity as LoginActivity).redirectFullName(
                phone ?: "",
                text as String
            )
        }



        btnResendCode.onClick {
            if (phone != null) {
                btnResendCode.isEnabled = false

            }
        }
//        subTitle.text = getString(R.string.phone_code_sent_to, phone ?: email)
    }
}