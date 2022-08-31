package com.example.instalive.app.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.api.DataRepository
import com.venus.dm.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: BaseViewModel() {
    fun getUserDetail(userId: String?, username: String?, result: (UserData) -> Unit){
        viewModelScope.launch {
            DataRepository.getUserDetail(userId, username, result, this@HomeViewModel)
        }
    }

    var resultData = MutableLiveData<String>()
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
                        this@HomeViewModel.onError(code, msg, errorType)
                    }

                    override fun onEvent(event: StatusEvent) {
                        if (event != StatusEvent.LOADING) {
                            this@HomeViewModel.onEvent(event)
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
}