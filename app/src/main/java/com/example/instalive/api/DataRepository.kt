package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.*
import com.example.baselibrary.model.CountryCodeData
import com.example.baselibrary.model.CountryCodeListData
import com.example.instalive.app.InstaLivePreferences
import com.example.instalive.app.SessionPreferences
import com.example.instalive.http.InstaApi
import com.example.instalive.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.venus.dm.model.GroupMember
import com.venus.dm.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

object DataRepository : BaseRemoteRepository(), IRemoteRequest {

    private val baseApi = RetrofitProvider.baseApi as InstaApi

    override suspend fun init(
        type: Int,
        liveData: MutableLiveData<AppInitData?>,
        remoteEventEmitter: RemoteEventEmitter?,
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            baseApi.init(type)
        }
        if (response?.resultOk() == true) {
            liveData.postValue(response.data)
            val initData = response.data
            val initJson = Gson().toJson(initData)
            SessionPreferences.initDataJson = initJson
            response.data.cacheConfig.stringTemplate?.let {
                Timber.d("local version = ${InstaLivePreferences.stringTemplateVersion} new version = ${it.version}")
                if (InstaLivePreferences.stringTemplateVersion != it.version) {
                    fetchStringTemplate(it)
                }
            }

            response.data.cacheConfig.levelIcons?.let {
                if (InstaLivePreferences.levelIconsVersion != it.version) {
                    fetchLevelIcons(it)
                }
            }

            response.data.cacheConfig.countryCode?.let {
                if (InstaLivePreferences.countryCodeVersion != it.version) {
                    fetchCountryCode(it)
                }
            }
        }
    }

    override suspend fun fetchStringTemplate(stringCache: CacheConfig.Cache) {
        val response = safeApiCall(null) {
            baseApi.getAnyData<StringTemplate>(stringCache.apiPath)
        }
        if (response != null) {
            val string = Gson().toJson(response.data, object : TypeToken<StringTemplate>() {}.type)
            InstaLiveStringTemplate.cacheTemplate(string)
            InstaLivePreferences.stringTemplateVersion = stringCache.version
        }
    }

    override suspend fun fetchLevelIcons(levelIcons: CacheConfig.Cache) {

    }

    override suspend fun fetchCountryCode(countryCode: CacheConfig.Cache) {
        val response = safeApiCall(null) {
            baseApi.getAnyData<CountryCodeListData>(countryCode.apiPath)
        }
        if (response != null) {
            val countryMutableList = mutableListOf<CountryCodeData>()
            response.data.countryCode.forEach {
                countryMutableList.addAll(it)
            }
            val string = Gson().toJson(
                countryMutableList.toList(),
                object : TypeToken<List<CountryCodeData>>() {}.type
            )
            Timber.d("country code: $string")
            InstaLivePreferences.countryCodeJson = string
            InstaLivePreferences.countryCodeVersion = countryCode.version
        }
    }

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
        LoadRemoteRepository.uploadMedia(
            path,
            "image/*",
            remoteEventEmitter,
            {
                resultData.postValue(it)
            },
            {
                return@uploadMedia baseApi.preSignPortrait()
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
                return@uploadMedia baseApi.uploadResourceToS3(
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

    override suspend fun uploadDMImage(
        path: String,
        conId: String,
        remoteEventEmitter: RemoteEventEmitter,
        onSuccess: (String) -> Unit,
    ) {
        try {

            LoadRemoteRepository.uploadMedia(
                path,
                "image/*",
                remoteEventEmitter,
                {
                    onSuccess.invoke(it)
                },
                {
                    return@uploadMedia baseApi.preSignDMImage(conId)
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
                    return@uploadMedia baseApi.uploadResourceToS3(
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun uploadDMVideo(
        path: String,
        conId: String,
        remoteEventEmitter: RemoteEventEmitter,
        onSuccess: (String) -> Unit,
    ) {
        try {
            LoadRemoteRepository.uploadMedia(
                path,
                "video/*",
                remoteEventEmitter,
                {
                    onSuccess.invoke(it)
                },
                presignFunction = {
                    return@uploadMedia baseApi.preSignDMVideo(conId)
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
                    return@uploadMedia baseApi.uploadResourceToS3(
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            withContext(Dispatchers.Main) {
                result?.invoke(response.data)
            }
        }
    }

    override suspend fun mentionSearch(
        conId: String,
        keyword: String,
        isRefresh: Boolean,
        meta: MutableLiveData<Meta>,
        liveData: MutableLiveData<List<GroupMember>>,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        var offset = 0
        val metaData = if (isRefresh) Meta() else meta.value
        if (metaData != null) {
            offset = metaData.nextOffset
        }
        if (metaData?.hasNext == false) {
            remoteEventEmitter.onEvent(StatusEvent.SUCCESS)
            return
        }
//        val response = safeApiCall(remoteEventEmitter) {
//            baseApi.mentionSearch(conId, keyword, offset)
//        }
//        if (response != null) {
//            processListData<GroupMember>(response, meta, liveData, isRefresh)
//        }

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