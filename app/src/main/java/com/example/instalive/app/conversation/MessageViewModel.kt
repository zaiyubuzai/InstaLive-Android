package com.example.instalive.app.conversation

import androidx.lifecycle.viewModelScope
import com.example.baselibrary.api.ErrorType
import com.example.baselibrary.api.RemoteEventEmitter
import com.example.baselibrary.api.StatusEvent
import com.example.instalive.InstaLiveApp
import com.example.instalive.api.ConversationDataRepository
import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.MessageComposer
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import com.venus.framework.util.isNeitherNullNorEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MessageViewModel : MessageBaseViewModel() {

    var conversationsEntity: ConversationsEntity? = null

    fun checkConversationBeingAt(
        conversationId: String,
        onAtMe: ((Boolean, String?) -> Unit)? = null//boolean:true:have at me message;false:haven`t at me message
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            val uuid = dao.getConversationMention(conversationId, SessionPreferences.id)
//            withContext(Dispatchers.Main) { onAtMe?.invoke(uuid != null, uuid) }
        }
    }

    fun getMessagedByUuid(uuid: String, result: ((MessageEntity?) -> Unit)?) {
        viewModelScope.launch(Dispatchers.IO){
            val messageEntity = dao.getMessagedByUuid(uuid, SessionPreferences.id)
            withContext(Dispatchers.Main) {
                result?.invoke(messageEntity)
            }
        }
    }

    fun getConversationUnreadCount(conId: String, timeToken: Long, result: (Int, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = dao.getConversationUnreadCount(
                SessionPreferences.id,
                conId,
                timeToken
            )
            val message = dao.getConversationUnreadFirstMessage(
                SessionPreferences.id,
                conId,
                timeToken
            )
            withContext(Dispatchers.Main) {
                message?.uuid?.let {
                    result.invoke(count,it)
                }
            }
        }
    }

    @ExperimentalStdlibApi
    fun updateConversationLastRead(conversationId: String, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val maxTimeToken = dao.getLatestRecievedTimetoken(
                conversationId,
                SessionPreferences.id
            ) ?: return@launch

            dao.updateConversationLastTimeToken(
                conversationId,
                SessionPreferences.id,
                maxTimeToken
            )
            dao.clearBeingAt(conversationId, SessionPreferences.id)
            Timber.d("updateConversationLastRead 1 $conversationId  type: $type")
            conversationsEntity?.lastRead = maxTimeToken
            dao.updateConversationLastRead(
                conversationId,
                maxTimeToken,
                SessionPreferences.id
            )
        }
    }

    suspend fun getMessageInTimeZone(conId: String, startTimestamp: Long, endTimestamp: Long, result:  (suspend (List<MessageEntity>) -> Unit)?){
            val messageEntityList = dao.getMessagesByConIdNewInsert(conId, SessionPreferences.id, startTimestamp, endTimestamp)
                if (messageEntityList.isNeitherNullNorEmpty()){
                    result?.invoke(messageEntityList)
                }
    }

    suspend fun getMessagesByConIdDesc(conId: String, timestamp: Long, result:  (suspend (List<MessageEntity>) -> Unit)?){
        val messageEntityList = dao.getMessagesByConIdDesc(conId, SessionPreferences.id, timestamp)
        if (messageEntityList.isNeitherNullNorEmpty()){
            result?.invoke(messageEntityList)
        }
    }

    fun disappearMessage() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.disappearMessage((System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000)
        }
    }

    fun sendMessage(conversationId: String, msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = MessageComposer.composeMessage(msg, conversationId, 1, )
            dao.insertOwnerMessageAndShow(message)
            dao.getMessagedByUuid(message.uuid, SessionPreferences.id)?.let {
                ConversationDataRepository.sendDm(it, object : RemoteEventEmitter {
                    override fun onError(code: Int, msg: String, errorType: ErrorType) {
                        buildPromptMessage(conversationId, msg, 1, "-1", message.sendTime + 1)
                    }

                    override fun onEvent(event: StatusEvent) {
                        this@MessageViewModel.onEvent(event)
                    }
                })
            }
        }
    }

}