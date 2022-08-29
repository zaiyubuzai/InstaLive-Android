package com.example.instalive.utils

import com.example.instalive.InstaLiveApp.Companion.appInstance
import com.example.instalive.api.ConversationDataRepository
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.venus.dm.db.dao.DirectMessageDao
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageUserEntity
import com.venus.dm.message.ChatMessagesDecomposer
import com.venus.dm.model.VenusDirectMessage
import com.venus.dm.model.VenusDirectMessageWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@ExperimentalStdlibApi
abstract class BaseInstaSocket : ChatMessagesDecomposer() {
    protected open var lastTimeStamp: Long = 0L

    override var dao: DirectMessageDao = InstaLiveDBProvider.db.directMessagingDao()
    override var ownerId: String = SessionPreferences.id

    override fun <T : MessageUserEntity> toMessageUserEntity(message: VenusDirectMessage): T {
        return message.toMessageUserEntity() as T
    }

    override fun <T : MessageEntity> toMessageEntity(message: VenusDirectMessage): T {
        return message.toMessageEntity(ownerId) as T
    }

    override fun <T : MessageUserEntity> getMessageUserEntity(userId: String): T? {
        return null
    }

    override suspend fun handleEveryMessage(message: VenusDirectMessage) {

    }

    override fun fetchConversation(conId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            ConversationDataRepository.fetchConversation(
                conId,
                null,
                null
            )
        }
    }


    override suspend fun handleMessageWrapper(messageWrapper: VenusDirectMessageWrapper) {
        reportMessageReceived(messageWrapper)
        reportConversationHaveRead(messageWrapper)
    }

    override suspend fun handleMessageWrapperAfter() {
        if (idTimeTokenMap.isNotEmpty() && System.currentTimeMillis() - circleCount >= 2000) {
            circleCount = System.currentTimeMillis()
            idTimeTokenMap.forEach {
                dao.updateConversationLastTimeToken(
                    it.key,
                    SessionPreferences.id,
                    it.value
                )
            }
            idTimeTokenMap.clear()
        }
    }

    abstract fun initSocket(initDM: Boolean)
    abstract fun releaseSocket()

    private fun reportConversationHaveRead(
        messageWrapper: VenusDirectMessageWrapper
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            conIdMap.forEach { conId ->
                val message = messageWrapper.messages.findLast { it.conId == conId }
                if (message != null) {
//                    val jsonObject = JSONObject()
//                    jsonObject.put("user_id", SessionPreferences.id)
//                    jsonObject.put("device_id", InstaLiveApp.appInstance.getDeviceId())
//                    jsonObject.put("conversation_id", conId)
//                    jsonObject.put("timetoken", message.timetoken)
//                    DMSocketIO.sendMessage("report_read", jsonObject.toString())

                    ConversationDataRepository.reportConversationHaveRead(
                        conId,
                        message.timetoken,
                        null
                    )
                }
            }
        }
    }

    @ExperimentalStdlibApi
    private fun reportMessageReceived(messageWrapper: VenusDirectMessageWrapper) {
        if (SessionPreferences.id.isEmpty() || messageWrapper.messages.isEmpty()) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val jsonArray = JSONArray()

            messageWrapper.messages.forEach {
                jsonArray.put(it.uuid)
            }

            when (3) {
                1 -> {
                    ConversationDataRepository.messageReportACK(jsonArray.toString(), null)
                    val jsonObject = JSONObject()
                    jsonObject.put("user_id", SessionPreferences.id)
                    jsonObject.put("device_id", appInstance.getDeviceId())
                    jsonObject.put("uuids", jsonArray)
                    DMSocketIO.sendMessage("report_ack", jsonObject.toString())
                }
                2 -> {
                    val jsonObject = JSONObject()
                    jsonObject.put("user_id", SessionPreferences.id)
                    jsonObject.put("device_id",appInstance.getDeviceId())
                    jsonObject.put("uuids", jsonArray)
                    DMSocketIO.sendMessage("report_ack", jsonObject.toString())
                }
                3 -> {
                    ConversationDataRepository.messageReportACK(jsonArray.toString(), null)
                }
            }

        }
    }
}