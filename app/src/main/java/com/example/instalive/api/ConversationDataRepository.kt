package com.example.instalive.api

import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.http.InstaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ConversationDataRepository: BaseRemoteRepository(),  IConversationRequest {

    private val instaApi = RetrofitProvider.baseApi as InstaApi

    override suspend fun createConversation(
        userId: String,
        result: (() -> Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter){
            instaApi.createConversation(userId)
        }
        if (response?.resultOk() == true){
            withContext(Dispatchers.Main){
                result?.invoke()
            }
        }
    }
}