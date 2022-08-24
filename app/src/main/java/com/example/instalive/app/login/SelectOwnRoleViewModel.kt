package com.example.instalive.app.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import com.example.instalive.model.LoginData
import kotlinx.coroutines.launch

class SelectOwnRoleViewModel: BaseViewModel() {
    val loginResponse = MutableLiveData<LoginData>()

    fun phoneLogin(phone: String, passcode: String, username: String, portrait: String, birthday: String, gender: String, identity: String) {
        if (loadingStatsLiveData.value == StatusEvent.LOADING) {
            return
        }
        viewModelScope.launch {
            DataRepository.loginByPhone(phone, passcode, username, portrait, birthday,gender,identity, null,null, null, loginResponse, this@SelectOwnRoleViewModel)
        }
    }
}