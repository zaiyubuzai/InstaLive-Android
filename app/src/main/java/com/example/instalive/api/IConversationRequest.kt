package com.example.instalive.api

import com.example.baselibrary.api.RemoteEventEmitter


interface IConversationRequest {
    suspend fun createConversation(
        userId: String,
        result: (()->Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    )
}