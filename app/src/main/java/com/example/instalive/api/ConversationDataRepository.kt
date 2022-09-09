package com.example.instalive.api

import androidx.lifecycle.MutableLiveData
import com.example.baselibrary.api.BaseRemoteRepository
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.instalive.app.SessionPreferences
import com.example.instalive.app.conversation.RecentConversation
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.http.InstaApi
import com.example.instalive.model.ConversationInfo
import com.example.instalive.model.ConversationListData
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import kotlinx.coroutines.*
import timber.log.Timber

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

    override suspend fun getConversationList(
        liveData: MutableLiveData<ConversationListData>,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.getConversationList()
        }
        if (response?.resultOk() == true) {
            liveData.postValue(response.data)
            withContext(Dispatchers.IO) {
                syncConversationList(response.data)
            }
        }
    }

    private suspend fun syncConversationList(conversationListData: ConversationListData) {
        val allConversations = dao.getAllConversations(SessionPreferences.id)
        allConversations.forEach { ce ->
            val con =
                conversationListData.conversationList.filter { it.id == ce.conversationId }
            if (con.isNullOrEmpty()) {
                dao.deleteConversation(
                    ce.conversationId,
                    SessionPreferences.id
                )
            }
        }
        conversationListData.conversationList.forEach { ci ->
            val con = allConversations.firstOrNull { it.conversationId == ci.id }
            syncConversation(con, ci)
        }
    }

    private suspend fun syncConversation(con: ConversationsEntity?, ci: ConversationInfo) {
        val recipient =
            ci.recipients.firstOrNull { it.id != SessionPreferences.id } ?: return
        if (con != null) {
            var shouldUpdate = false
            if (recipient.nickname != con.recipientName) {
                con.recipientName = recipient.nickname
                shouldUpdate = true
            }
            if (recipient.username != con.recipientUsername) {
                con.recipientUsername = recipient.username
                shouldUpdate = true
            }
            if (recipient.portrait != con.recipientPortrait) {
                con.recipientPortrait = recipient.portrait
                shouldUpdate = true
            }
            if (ci.pin != con.isPin) {
                con.isPin = ci.pin ?: 0
                shouldUpdate = true
            }
            if (shouldUpdate) {
                dao.updateConversation(con)
            }
        } else {
            dao.insertConversation(
                ConversationsEntity(
                    userId = SessionPreferences.id,
                    conversationId = ci.id,
                    recipientId = recipient.id,
                    recipientName = recipient.nickname,
                    recipientPortrait = recipient.portrait,
                    recipientUsername = recipient.username,
                    living = 0,
                    chatState = ci.state,
                    relationship = recipient.relationship,
                    lastMsgTimetoken = ci.lastMessageTimestamp ?: 0L,
                    type = ci.type,
                    ownerId = SessionPreferences.id,
                    mute = ci.mute,
                    isPin = ci.pin,
                )
            )
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

    override suspend fun pullUnread(
        timeToken: Long,
        pullUUID: String,
        remoteEventEmitter: RemoteEventEmitter?,
    ) {
        safeApiCall(remoteEventEmitter, false) {
            instaApi.pullUnread(timeToken, pullUUID)
        }
    }

    override suspend fun reportConversationHaveRead(
        conversationId: String,
        timeToken: Long,
        remoteEventEmitter: RemoteEventEmitter?,
    ) {
        safeApiCall(remoteEventEmitter, false) {
            instaApi.reportConversationHaveRead(conversationId, timeToken)
        }
    }

    override suspend fun messageReportACK(
        uuids: String,
        remoteEventEmitter: RemoteEventEmitter?,
    ) {
        safeApiCall(remoteEventEmitter, false) {
            instaApi.messageReportACK(uuids)
        }
    }

    override suspend fun fetchConversation(
        conversationId: String,
        remoteEventEmitter: RemoteEventEmitter?,
        pendingNotificationMessages: List<MessageEntity>?,
    ) {
        Timber.d("ConversationDetail 4 $conversationId")
        if (conversationId.isEmpty()) return
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.conversationDetail(conversationId)
        }
        if (response != null) {
            withContext(Dispatchers.IO) {
                //单聊
                val recipient = response.data.recipients?.findLast {
                    it.id != SessionPreferences.id
                }
                if (recipient != null) {
                    val con = dao.getConversationByConId(
                        conversationId,
                        SessionPreferences.id
                    )
                    if (con != null) {
                        var shouldUpdate = false
                        if (response.data.chatState != con.chatState) {
                            con.chatState = response.data.chatState ?: 1
                            shouldUpdate = true
                        }
                        if (recipient.portrait != con.recipientPortrait) {
                            con.recipientPortrait = recipient.portrait
                            shouldUpdate = true
                        }
                        if (recipient.relationship != con.relationship) {
                            con.relationship = recipient.relationship
                            shouldUpdate = true
                        }
                        if (recipient.nickname != con.recipientName) {
                            con.recipientName = recipient.nickname
                            shouldUpdate = true
                        }
                        if (recipient.username != con.recipientUsername) {
                            con.recipientUsername = recipient.nickname
                            shouldUpdate = true
                        }

                        if (response.data.mute != con.mute) {
                            con.mute = response.data.mute
                            shouldUpdate = true
                        }
                        if (response.data.pin != con.isPin) {
                            con.isPin = response.data.pin
                            shouldUpdate = true
                        }
                        if (shouldUpdate) {
                            dao.updateConversation(con)
                        }
                    } else {

                        dao.insertConversation(
                            ConversationsEntity(
                                userId = SessionPreferences.id,
                                conversationId = response.data.id,
                                recipientId = recipient.id,
                                recipientName = recipient.nickname,
                                recipientPortrait = recipient.portrait,
                                recipientUsername = recipient.nickname,
                                living = 0,
                                chatState = response.data.chatState ?: 1,
                                relationship = recipient.relationship,
                                type = 1,
                                ownerId = recipient.id,
                                mute = null,
                                lastSenderName = "",
                                memberCount = 2,
                                isPin = response.data.pin
                            )
                        )
                    }
//                        if (pendingNotificationMessages != null) {
//                            withContext(Dispatchers.Main) {
//                                //补上本地notification
//                                MarsFirebaseMessagingService.createMessageNotification(
//                                    pendingNotificationMessages,
//                                    appInstance,
//                                    false
//                                )
//                            }
//                        }
                }

            }
        }
    }

    override suspend fun pinConversation(
        conversationId: String,
        result: () -> Unit,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.pinConversation(conversationId)
        }
        if (response != null) {
            GlobalScope.launch {
                val con =
                    dao.getConversationByConId(conversationId, SessionPreferences.id)
                if (con != null && con.isPin != 1) {
                    con.isPin = 1
                    dao.updateConversation(con)
                    if (RecentConversation.conversationsEntity?.conversationId == conversationId) RecentConversation.conversationsEntity?.isPin =
                        1
                }
                withContext(Dispatchers.Main) {
                    result.invoke()
                }
            }
        }
    }

    override suspend fun unpinConversation(
        conversationId: String,
        result: () -> Unit,
        remoteEventEmitter: RemoteEventEmitter
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            instaApi.unpinConversation(conversationId)
        }
        if (response != null) {
            GlobalScope.launch {
                val con =
                    dao.getConversationByConId(conversationId, SessionPreferences.id)
                if (con != null && con.isPin != 0) {
                    con.isPin = 0
                    dao.updateConversation(con)
                    if (RecentConversation.conversationsEntity?.conversationId == conversationId) RecentConversation.conversationsEntity?.isPin =
                        0
                }
                withContext(Dispatchers.Main) {
                    result.invoke()
                }
            }
        }
    }


    override suspend fun muteOrUnmute(
        conversationId: String,
        mute: Int,
        muted: (() -> Unit)?,
        unMuted: (() -> Unit)?,
        remoteEventEmitter: RemoteEventEmitter,
    ) {
        val response = safeApiCall(remoteEventEmitter) {
            if (mute == 0) {
                instaApi.unMuteConversation(conversationId)
            } else {
                instaApi.muteConversation(conversationId)
            }
        }

        if (response?.resultOk() == true) {
            GlobalScope.launch {
                val conversationsEntity =
                    dao.getConversationByConId(conversationId, SessionPreferences.id)
                withContext(Dispatchers.Main) {
                    if (mute == 0) {
                        unMuted?.invoke()
                    } else {
                        muted?.invoke()
                    }
                }
                conversationsEntity?.let {
                    it.mute = mute
                    dao.updateConversation(it)
                    if (RecentConversation.conversationsEntity?.conversationId == conversationId) RecentConversation.conversationsEntity?.mute =
                        mute
                }
            }
        }
    }
}