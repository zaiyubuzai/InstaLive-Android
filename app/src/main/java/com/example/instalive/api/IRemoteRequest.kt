package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.RemoteEventEmitter

interface IRemoteRequest {
    suspend fun sendPasscode(
        phone: String,
        source: String,
        dialCode: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )
}