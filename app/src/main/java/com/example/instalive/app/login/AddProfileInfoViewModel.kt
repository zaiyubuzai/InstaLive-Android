package com.example.instalive.app.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProfileInfoViewModel : BaseViewModel() {
    var resultData = MutableLiveData<String>()
    var checkUsernameData = MutableLiveData<Any>()
    fun uploadAvatar(path: String, onStartLoading: () -> Unit, onFinish: (Boolean) -> Unit) {
        if (loadingStatsLiveData.value == StatusEvent.LOADING) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            DataRepository.uploadPortraitInRegister(path,
                resultData,
                object : RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        onFinish(false)
                        this@AddProfileInfoViewModel.onError(code, msg, errorType)
                    }

                    override fun onEvent(event: StatusEvent) {
                        if (event != StatusEvent.LOADING) {
                            this@AddProfileInfoViewModel.onEvent(event)
                        }
                        if (event == StatusEvent.SUCCESS) {
                            onFinish(true)
                        } else if (event == StatusEvent.LOADING) {
                            onStartLoading()
                        }
                    }
                })
        }
    }

    fun checkUsernameAvailability(username: String){
        viewModelScope.launch {
            DataRepository.checkUsernameAvailability(username, checkUsernameData, this@AddProfileInfoViewModel)
        }
    }

    fun checkBirthday(birthday: String){
        viewModelScope.launch {
            DataRepository.checkBirthday(birthday, checkUsernameData, this@AddProfileInfoViewModel)
        }
    }
}