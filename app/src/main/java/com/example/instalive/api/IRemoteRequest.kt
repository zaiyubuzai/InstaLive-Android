package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.model.LoginData
import com.venus.dm.model.UserData
import retrofit2.http.Field
import retrofit2.http.Query

interface IRemoteRequest {
    suspend fun sendPasscode(
        phone: String,
        source: String,
        dialCode: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun loginByPhone(
        phone: String,
        passcode: String,
        userName: String? = null,
        portrait: String? = null,
        birth: String? = null,
        gender: String? = null,
        identity: String? = null,
        nickname: String? = null,
        timezone: String? = null,
        s: String? = null,
        liveData: MutableLiveData<LoginData>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun uploadPortraitInRegister(
        path: String,
        resultData: MutableLiveData<String>,
        remoteEventEmitter: RemoteEventEmitter,
    )

    suspend fun checkUsernameAvailability(
        username: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun checkBirthday(
        birthday: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun getUserDetail(
        userId: String?,
        userName: String?,
        result:((UserData)->Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    )
}