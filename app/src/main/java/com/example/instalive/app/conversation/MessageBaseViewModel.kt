package com.example.instalive.app.conversation

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.baselibrary.views.BaseViewModel
import com.example.instalive.InstaLiveApp
import com.example.instalive.api.ConversationDataRepository
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.example.instalive.db.MessageComposer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.db.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class MessageBaseViewModel:BaseViewModel() {
    protected val dao = InstaLiveDBProvider.db.directMessagingDao()
    fun buildPromptMessage(
        conversationId: String,
        msg: String,
        showType: Int,
        liveId: String,
        timeToken: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (msg.isNotEmpty()) {
                val message = MessageComposer.composePromptMessage(
                    msg,
                    conversationId,
                    showType,
                    liveId,
                    timeToken = timeToken
                )
                dao.insertOwnerMessageAndShow(message)
            }
        }
    }

    fun resendTextMessage(messageEntity: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            messageEntity.sendStatus = MessageEntity.SEND_STATUS_SENDING
            dao.deleteMessage(messageEntity.sendTime + 1) //删掉可能存在的这条消息的附属消息
//            LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(
//                MessageEvent(
//                    7,
//                    null,
//                    null,
//                    null,
//                    System.currentTimeMillis(),
//                    false,
//                    messageEntity.sendTime + 1
//                )
//            )
            messageEntity.sendTime =
                (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) * 10000L
            dao.updateMessageAndUpdateConversation(messageEntity)
            ConversationDataRepository.sendDm(
                messageEntity,
                object :
                    RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        buildPromptMessage(
                            messageEntity.conId,
                            msg,
                            messageEntity.showType,
                            messageEntity.liveId ?: "-1",
                            messageEntity.sendTime + 1
                        )
                    }

                    override fun onEvent(event: StatusEvent) {
                        this@MessageBaseViewModel.onEvent(event)
                    }
                })
        }
    }

    fun deleteMessage(messageEntity: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteMessage(messageEntity)
            dao.deleteMessage(messageEntity.sendTime + 1) //删掉可能存在的这条消息的附属消息
//            LiveEventBus.get(Constants.EVENT_BUS_KEY_DEFAULT).post(
//                MessageEvent(
//                    7,
//                    null,
//                    null,
//                    null,
//                    System.currentTimeMillis(),
//                    false,
//                    messageEntity.timeToken + 1
//                )
//            )
            if ((messageEntity.type == 1 || messageEntity.type == 32)
            ) {
//                val conversation =
//                    dao.getConversationByConId(messageEntity.conId, SessionPreferences.id)
//                if (conversation != null && conversation.isMentionMe == messageEntity.uuid) {
//                    dao.updateConversationATMeContent(
//                        messageEntity.conId,
//                        messageEntity.userId,
//                        0,
//                        null
//                    )
//                }
            }
        }
    }
}