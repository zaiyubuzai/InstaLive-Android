package com.example.instalive.api

import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.http.InstaApi
import com.venus.dm.db.entity.MessageEntity
import com.venus.framework.util.isNeitherNullNorEmpty
import kotlinx.coroutines.*

object ConversationDataRepository : BaseRemoteRepository(), IConversationRequest {

    private val instaApi = RetrofitProvider.baseApi as InstaApi
    private val dao = InstaLiveDBProvider.db.directMessagingDao()
    private val pendingMessageJobsMap = mutableMapOf<String, Job>()

    override suspend fun createConversation(
        userId: String,
        result: (() -> Unit)?,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.createConversation(userId)
        }
        if (response?.resultOk() == true) {
            withContext(Dispatchers.Main) {
                result?.invoke()
            }
        }
    }

    override suspend fun sendDm(
        messageEntity: MessageEntity,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        //以下为发送30秒失败的逻辑
        //给发出去的消息开个job，如果消息发送成功或失败，直接cancel掉job，
        //如果job没有被cancel掉，30秒后查询数据库看发送状态，如果还是正在发送，就置为失败
        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(30000)
            val messages =
                dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
            if (messages != null) {
                if (messages.sendStatus == MessageEntity.SEND_STATUS_SENDING) {
                    messages.sendStatus = MessageEntity.SEND_STATUS_FAILED
                    dao.updateMessageAndUpdateConversation(messages)
                }
            }
        }
        pendingMessageJobsMap[messageEntity.uuid] = job
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.sendDm(
                messageEntity.conId,
                messageEntity.type,
                messageEntity.payload,
                messageEntity.uuid,
                1,
                0
            )
        }
        if (response != null) {
            val existingMessage =
                dao.getMessagedByUuid(messageEntity.uuid, SessionPreferences.id)
            if (existingMessage != null) {
                existingMessage.sendStatus = MessageEntity.SEND_STATUS_SUCCESS
                existingMessage.extraInfo = ""
                existingMessage.payload = messageEntity.payload

                pendingMessageJobsMap.remove(messageEntity.uuid)?.cancel()
                val messageUser = dao.getMessageUserById(SessionPreferences.id)
                messageUser?.let {
                    if (it.portraitIc != response.data.portraitIc) {
                        val mUser = it
                        mUser.portraitIc = response.data.portraitIc
                        dao.updateMessageUser(mUser)
                    }
                    SessionPreferences.portraitIc = response.data.portraitIc
                }
                dao.updateMessageAndUpdateConversation(
                    existingMessage,
                    true,
                )
            }
        } else {
            messageEntity.sendStatus = MessageEntity.SEND_STATUS_FAILED
            pendingMessageJobsMap.remove(messageEntity.uuid)?.cancel()
            dao.updateMessageAndUpdateConversation(messageEntity, true)
        }
    }


}