package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.model.ConversationListData
import com.venus.dm.db.entity.MessageEntity


interface IConversationRequest {
    suspend fun createConversation(
        userId: String,
        result: (()->Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun getConversationList(
        liveData: MutableLiveData<ConversationListData>,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun sendDm(
        messageEntity: MessageEntity,
        remoteEventEmitter: RemoteEventEmitter
    )
}