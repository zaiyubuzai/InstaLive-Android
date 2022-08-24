package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.http.InstaApi
import timber.log.Timber

object DataRepository: BaseRemoteRepository(), IRemoteRequest {

    private val baseApi = RetrofitProvider.baseApi as InstaApi

    override suspend fun sendPasscode(
        phone: String,
        source: String,
        dialCode: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter){
            baseApi.sendPasscode(phone,source, dialCode)
        }
        Timber.d("response: ${response?.result}1")
        if (response?.resultOk() == true){
            Timber.d("response: ${response.result}")
            liveData.postValue(Any())
        }
    }


}