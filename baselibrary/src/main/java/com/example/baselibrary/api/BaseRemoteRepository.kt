package com.example.baselibrary.api

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseRemoteRepository {
    /**
     * Function that executes the given function on Dispatchers.IO context and switch to Dispatchers.Main context when an error occurs
     * @param callFunction is the function that is returning the wanted object. It must be a suspend function. Eg:
     * override suspend fun loginUser(body: LoginUserBody, emitter: RemoteErrorEmitter): LoginUserResponse?  = safeApiCall( { authApi.loginUser(body)} , emitter)
     * @param emitter is the interface that handles the error messages. The error messages must be displayed on the MainThread, or else they would throw an Exception.
     */
    protected suspend inline fun <T> safeApiCall(
        emitter: RemoteEventEmitter?,
        isToast: Boolean = true,
        crossinline callFunction: suspend () -> T,
    ): T? {
        emitter?.onEvent(StatusEvent.LOADING)
        return try {
            val myObject = withContext(Dispatchers.IO) { callFunction.invoke() }
            emitter?.onEvent(StatusEvent.SUCCESS)
            myObject
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                e.printStackTrace()
                when (e) {
                    is HttpException -> {
                        val mError = withContext(Dispatchers.IO) {
                            val body = e.response()?.errorBody()?.string()
                            if (body != null) {
                                try {
                                    Gson().fromJson(body, BaseRepositoryError::class.java)
                                } catch (e: java.lang.Exception) {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                        withContext(Dispatchers.Main) {
                            emitter?.onEvent(StatusEvent.ERROR)
                            if (mError != null) {
                                responseError(mError)
                                emitter?.onError(
                                    mError.error?.code ?: -1,
                                    mError.error?.message ?: "",
                                    ErrorType.SESSION_EXPIRED
                                )
                            } else if (e.code() == 403) {
                                // ip forbidden
                                httpError(e)
                                emitter?.onError(
                                    e.code(),
                                    e.message ?: "",
                                    ErrorType.SESSION_EXPIRED
                                )
                            } else {
                                emitter?.onError(0, e.message ?: "Unknown Error", ErrorType.BACKEND)
                            }
                        }
                    }
                    is SocketTimeoutException -> {
                        emitter?.onEvent(StatusEvent.FAILURE)
                        if (isToast) {
                            emitter?.onError(0, "", ErrorType.TIMEOUT)
                            withContext(Dispatchers.Main){
                                repositorySocketTimeout()
                            }
                        } else {
                            null
                        }
                    }
                    is IOException -> {
                        emitter?.onEvent(StatusEvent.FAILURE)
                        if (isToast) {
                            emitter?.onError(0, "", ErrorType.NETWORK)
                            withContext(Dispatchers.Main){
                                repositoryIOException()
                            }
                        } else {
                            null
                        }
                    }
                    else -> {
                        emitter?.onEvent(StatusEvent.ERROR)
                        emitter?.onError(0, "", ErrorType.UNKNOWN)
                    }
                }
            }
            null
        }
    }

    open fun responseError(mError: BaseRepositoryError) {}

    open fun httpError(mError: HttpException) {}

    open fun repositorySocketTimeout() {}

    open fun repositoryIOException() {}

    protected fun <T> processListData(
        response: BaseResponse<List<T>>,
        meta: MutableLiveData<Meta>,
        liveData: MutableLiveData<List<T>>,
        isRefresh: Boolean = false,
    ) {
        meta.postValue(response.meta)
        if (isRefresh) {
            liveData.postValue(response.data)
        } else {
            val oldList = liveData.value
            if (oldList == null || oldList.isEmpty()) {
                liveData.postValue(response.data)
            } else {
                val newItems = response.data
                val newList = oldList.toMutableList()
                newList.addAll(newItems)
                liveData.postValue(newList)
            }
        }
    }

    protected fun <T, E> processListData(
        response: BaseResponseWithExt<List<T>, E>,
        meta: MutableLiveData<Meta>,
        liveData: MutableLiveData<List<T>>,
        isRefresh: Boolean = false,
    ) {
        meta.postValue(response.meta)
        if (isRefresh) {
            liveData.postValue(response.data)
        } else {
            val oldList: List<T>? = liveData.value
            if (oldList == null || oldList.isEmpty()) {
                liveData.postValue(response.data)
            } else {
                val newItems = response.data
                val newList = oldList.toMutableList()
                newList.addAll(newItems)
                liveData.postValue(newList)
            }
        }
    }
}