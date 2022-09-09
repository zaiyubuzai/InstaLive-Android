package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.model.AppInitData
import com.example.instalive.model.CacheConfig
import com.example.instalive.model.LoginData
import com.venus.dm.model.UserData
import retrofit2.http.Field
import retrofit2.http.Query

interface IRemoteRequest {

    suspend fun init(
        type: Int,
        liveData: MutableLiveData<AppInitData?>,
        remoteEventEmitter: RemoteEventEmitter?,
    )

    suspend fun fetchStringTemplate(stringCache: CacheConfig.Cache)

    suspend fun fetchLevelIcons(leveIcons: CacheConfig.Cache)

    suspend fun fetchCountryCode(countryCode: CacheConfig.Cache)

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

    suspend fun logout(
        liveData: MutableLiveData<Any>,
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
        result: ((UserData) -> Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun uploadDMVideo(
        path: String,
        conId: String,
        remoteEventEmitter: RemoteEventEmitter,
        onSuccess: (String) -> Unit
    )

    suspend fun uploadDMImage(
        path: String,
        conId: String,
        remoteEventEmitter: RemoteEventEmitter,
        onSuccess: (String) -> Unit
    )

    suspend fun updateProfile(
        username: String?,
        nickname: String?,
        portrait: String?,
        bio: String?,
        liveData: MutableLiveData<UserData>,
        remoteEventEmitter: RemoteEventEmitter
    )
}