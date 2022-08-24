package com.example.instalive.app.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import com.example.instalive.model.LoginData
import kotlinx.coroutines.launch

class PhonePasscodeViewModel : BaseViewModel() {
    val loginResponse = MutableLiveData<LoginData>()
    var phoneVerifyResult = MutableLiveData<Any>()

    fun phoneLogin(phone: String, passcode: String, source: String) {
        if (loadingStatsLiveData.value == StatusEvent.LOADING) {
            return
        }
        viewModelScope.launch {
            DataRepository.loginByPhone(phone, passcode, null, null, null,null,null, null,null, null, loginResponse, this@PhonePasscodeViewModel)
        }
    }

    fun sendPhonePasscode(phone: String, source: String, dialCode: String) {
        if (loadingStatsLiveData.value == StatusEvent.LOADING) {
            return
        }
        viewModelScope.launch {
            DataRepository.sendPasscode(phone, source, dialCode, phoneVerifyResult, this@PhonePasscodeViewModel)
        }
    }
}