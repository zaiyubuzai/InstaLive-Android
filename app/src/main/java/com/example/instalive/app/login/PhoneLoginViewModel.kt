package com.example.instalive.app.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import kotlinx.coroutines.launch

class PhoneLoginViewModel : BaseViewModel() {

    var phoneVerifyResult = MutableLiveData<Any>()

    fun sendPhonePasscode(phone: String, source: String, dialCode: String) {
        if (loadingStatsLiveData.value == StatusEvent.LOADING) {
            return
        }
        viewModelScope.launch {
            DataRepository.sendPasscode(phone, source, dialCode, phoneVerifyResult, this@PhoneLoginViewModel)
        }
    }

}