package com.example.baselibrary.api;

import com.example.baselibrary.model.PresignData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object LoadRemoteRepository : BaseRemoteRepository() {

    suspend fun uploadMedia(
        path: String,
        fileRequestBodyStr: String,
        remoteEventEmitter: RemoteEventEmitter,
        onSuccess: (String) -> Unit,
        presignFunction: suspend () -> BaseResponse<PresignData>,
        uploadFunction: suspend (
            url: String,
            key: RequestBody,
            keyId: RequestBody,
            policy: RequestBody,
            sig: RequestBody,
            acl: RequestBody?,
            token: RequestBody,
            file: MultipartBody.Part,
        ) -> Response<Unit>,
    ) {
        try {
            val fileName = File(path).name
            val data = safeApiCall(remoteEventEmitter) {
                presignFunction.invoke()
            }?.data ?: return

            val policy =
                RequestBody.create(MediaType.parse("multipart/form-data"), data.fields.policy)
            val key = RequestBody.create(MediaType.parse("multipart/form-data"), data.fields.key)
            val AWSAccessKeyId = RequestBody.create(
                MediaType.parse("multipart/form-data"),
                data.fields.AWSAccessKeyId
            )
            val signature =
                RequestBody.create(MediaType.parse("multipart/form-data"), data.fields.signature)
            val acl =
                RequestBody.create(MediaType.parse("multipart/form-data"), data.fields.acl)
            val token =
                RequestBody.create(MediaType.parse("multipart/form-data"), data.fields.token)
            val fileRequestBody =
                RequestBody.create(MediaType.parse(fileRequestBodyStr), File(path))
            val filePartBody = MultipartBody.Part.createFormData("file", fileName, fileRequestBody)

            withContext(Dispatchers.IO) {
                try {
                    remoteEventEmitter.onEvent(StatusEvent.LOADING)
                    val resource = uploadFunction.invoke(
                        data.url,
                        key,
                        AWSAccessKeyId,
                        policy,
                        signature,
                        acl,
                        token,
                        filePartBody
                    )
                    if (resource.isSuccessful) {
                        onSuccess(data.resourceUrl)
                    }
                } catch (e: Exception) {
                    remoteEventEmitter.onEvent(StatusEvent.FAILURE)
                    if (e is HttpException) {
                        remoteEventEmitter.onError(0, "", ErrorType.NETWORK)
                    } else {
                        remoteEventEmitter.onError(0, "", ErrorType.UNKNOWN)
                    }
                }
            }
        } catch (e: Exception) {
            remoteEventEmitter.onEvent(StatusEvent.FAILURE)
            remoteEventEmitter.onError(0, "", ErrorType.UNKNOWN)
        }
    }

    suspend fun downloadFile(url: String, savePath: String, callFunction: suspend () -> Response<ResponseBody>,progressFunc: (Int)->Unit){
        if (url.isEmpty()) return

        var netPath = url


        Timber.d("cache save path: $savePath")
        val response = safeApiCall(null) {
            callFunction.invoke()
        }
        if (response?.isSuccessful == true) {
            val inputStream = response.body()?.byteStream()
            val length = response.body()?.contentLength()
            var outputStream: FileOutputStream? = null
            if (inputStream != null && length != null) {
                try {
                    outputStream = FileOutputStream(savePath)
                    var currentLength = 0L
                    var len: Int
                    val buff = ByteArray(1024)
                    while ((inputStream.read(buff).also { len = it }) != -1) {
                        outputStream?.write(buff, 0, len)
                        currentLength += len
                        //计算当前下载百分比，并经由回调传出
                        val progressInt = (100L * currentLength / length).toInt()
                        withContext(Dispatchers.Main) {
                            progressFunc.invoke(progressInt)
                        }
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
//                    remoteEventEmitter?.onError(0, "", ErrorType.NETWORK)
                } catch (e: IOException) {
                    e.printStackTrace()
//                    remoteEventEmitter?.onError(0, "", ErrorType.NETWORK)
                } catch (e: Exception) {
                    e.printStackTrace()
//                    remoteEventEmitter?.onError(0, "", ErrorType.NETWORK)
                } finally {
                    try {
                        inputStream.close()
                        outputStream?.close()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}
