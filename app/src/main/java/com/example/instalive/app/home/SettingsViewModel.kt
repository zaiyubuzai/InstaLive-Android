package com.example.instalive.app.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import kotlinx.coroutines.launch

class SettingsViewModel: BaseViewModel() {
    val logoutData = MutableLiveData<Any>()

    fun logout() {
        viewModelScope.launch {
            DataRepository.logout(logoutData, this@SettingsViewModel)
        }
    }
}