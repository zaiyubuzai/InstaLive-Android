package com.example.baselibrary.views

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent

abstract class BaseViewModel : ViewModel(), RemoteEventEmitter {

    var errorMessageLiveData = MutableLiveData<String>()
    var errorInfo = MutableLiveData<Pair<Int,String>>()
    var errorCodeLiveData = MutableLiveData<Int>()
    var loadingStatsLiveData = MutableLiveData<StatusEvent>()
    var errorTypeLiveData = MutableLiveData<ErrorType>()
    var rfTag: String? = null

    open fun reset(){
        errorMessageLiveData = MutableLiveData<String>()
        errorInfo = MutableLiveData<Pair<Int,String>>()
        errorCodeLiveData = MutableLiveData<Int>()
        loadingStatsLiveData = MutableLiveData<StatusEvent>()
        errorTypeLiveData = MutableLiveData<ErrorType>()
    }

    override fun onError(code: Int, msg: String, errorType: ErrorType) {
        errorMessageLiveData.postValue(msg)
        errorCodeLiveData.postValue(code)
        errorInfo.postValue(Pair(code,msg))
        errorTypeLiveData.postValue(errorType)
    }

    override fun onEvent(event: StatusEvent) {
        loadingStatsLiveData.postValue(event)
    }
}