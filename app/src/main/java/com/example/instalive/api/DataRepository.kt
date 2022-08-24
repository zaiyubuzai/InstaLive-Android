package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.BaseRepositoryError
import com.example.baselibrary.api.LoadRemoteRepository
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.http.InstaApi
import com.example.instalive.model.LoginData
import com.venus.dm.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import timber.log.Timber

object DataRepository : BaseRemoteRepository(), IRemoteRequest {

    private val baseApi = RetrofitProvider.baseApi as InstaApi

    override suspend fun sendPasscode(
        phone: String,
        source: String,
        dialCode: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            baseApi.sendPasscode(phone, source, dialCode)
        }
        Timber.d("response: ${response?.result}1")
        if (response?.resultOk() == true) {
            Timber.d("response: ${response.result}")
            liveData.postValue(Any())
        }
    }

    override suspend fun loginByPhone(
        phone: String,
        passcode: String,
        userName: String?,
        portrait: String?,
        birth: String?,
        gender: String?,
        identity: String?,
        nickname: String?,
        timezone: String?,
        s: String?,
        liveData: MutableLiveData<LoginData>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            baseApi.loginByPhone(
                phone,
                passcode,
                userName,
                portrait,
                birth,
                gender,
                identity,
                nickname,
                timezone,
                s
            )
        }

        if (response?.resultOk() == true) {
            liveData.postValue(response.data)
        }
    }

    override suspend fun uploadPortraitInRegister(
        path: String,
        resultData: MutableLiveData<String>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        LoadRemoteRepository.uploadDMVideo(
            path,
            "image/*",
            remoteEventEmitter,
            {
                resultData.postValue(it)
            },
            {
                return@uploadDMVideo baseApi.preSignPortrait()
            },
            {
                    url: String,
                    key: RequestBody,
                    keyId: RequestBody,
                    policy: RequestBody,
                    sig: RequestBody,
                    acl: RequestBody?,
                    token: RequestBody,
                    file: MultipartBody.Part,
                ->
                return@uploadDMVideo baseApi.uploadResourceToS3(
                    url,
                    key,
                    keyId,
                    policy,
                    sig,
                    acl,
                    token,
                    file
                )
            })
    }

    override suspend fun checkUsernameAvailability(
        username: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            baseApi.checkUsernameAvailability(username)
        }
        if (response?.resultOk() == true) {
            liveData.postValue(Any())
        }
    }

    override suspend fun checkBirthday(
        birthday: String,
        liveData: MutableLiveData<Any>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            baseApi.checkBirthday(birthday)
        }
        if (response?.resultOk() == true) {
            liveData.postValue(Any())
        }
    }

    override suspend fun getUserDetail(
        userId: String?,
        userName: String?,
        result: ((UserData) -> Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            baseApi.getUserDetail(userId, userName)
        }
        if (response?.resultOk() == true) {
            withContext(Dispatchers.Main){
                result?.invoke(response.data)
            }
        }
    }

    override fun responseError(mError: BaseRepositoryError) {

    }

    override fun httpError(mError: HttpException) {

    }

    override fun repositorySocketTimeout() {

    }

    override fun repositoryIOException() {

    }


}