package com.example.instalive.api

import com.example.baselibrary.api.RemoteEventEmitter
import com.venus.dm.db.entity.MessageEntity


interface IConversationRequest {
    suspend fun createConversation(
        userId: String,
        result: (()->Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun sendDm(
        messageEntity: MessageEntity,
        remoteEventEmitter: RemoteEventEmitter
    )
}