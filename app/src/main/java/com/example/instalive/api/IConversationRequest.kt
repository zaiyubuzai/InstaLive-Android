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

    suspend fun pullUnread(
        timeToken: Long,
        pullUUID: String,
        remoteEventEmitter: RemoteEventEmitter?
    )

    suspend fun reportConversationHaveRead(
        conversationId: String,
        timeToken: Long,
        remoteEventEmitter: RemoteEventEmitter?
    )

    suspend fun messageReportACK(
        uuids: String,
        remoteEventEmitter: RemoteEventEmitter?,
    )

    suspend fun fetchConversation(
        conversationId: String,
        remoteEventEmitter: RemoteEventEmitter?,
        pendingNotificationMessages: List<MessageEntity>?
    )

    suspend fun pinConversation(
        conversationId: String,
        result: () -> Unit,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun unpinConversation(
        conversationId: String,
        result: () -> Unit,
        remoteEventEmitter: RemoteEventEmitter
    )

    suspend fun muteOrUnmute(
        conversationId: String,
        mute: Int,
        muted: (() -> Unit)?,
        unMuted: (() -> Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    )
}