package com.venus.dm.message

import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.app.ChatConstants
import com.venus.dm.db.dao.DirectMessageDao
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageUserEntity
import com.venus.dm.model.VenusDirectMessage
import com.venus.dm.model.VenusDirectMessageWrapper
import com.venus.dm.model.event.MessageEvent
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

/**
 * 消息解析器
 */
abstract class ChatMessagesDecomposer {

    protected abstract var dao: DirectMessageDao
    protected abstract var ownerId: String

    private val messageSyncList = ConcurrentLinkedQueue<VenusDirectMessageWrapper>()
    private var messageJob: Job? = null

    protected val idTimeTokenMap = ConcurrentHashMap<String, Long>()

    private val messageUserEntityList = mutableListOf<MessageUserEntity>()
    private val messageEntityList = mutableListOf<MessageEntity>()
    protected val conIdMap = ConcurrentSkipListSet<String>()

    protected var circleCount = 0L

    //正在解压未读消息
    private var isDecomposerUnreadMessage = false

    //打开处理消息的协程
    @ExperimentalStdlibApi
    fun startMessageJob() {
        if (messageJob == null) {
            messageJob = CoroutineScope(Dispatchers.IO).launch {
                messageSyncList.clear()
                idTimeTokenMap.clear()
                conIdMap.clear()
                circleCount = System.currentTimeMillis()
                while (this.isActive) {
                    if (messageSyncList.isNotEmpty()) {
                        messageUserEntityList.clear()
                        messageEntityList.clear()
                        val messageWrapper = messageSyncList.poll() ?: break
                        val isUnreadMessage = messageWrapper.isUnreadMessage

                        if (!isUnreadMessage && isDecomposerUnreadMessage) {
                            isDecomposerUnreadMessage = false
                            LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(
                                MessageEvent(
                                    3,
                                    null,
                                    null,
                                    System.currentTimeMillis(),
                                    false
                                )
                            )
                        } else {
                            isDecomposerUnreadMessage = true
                        }

                        var senderId = ""
                        messageWrapper.messages.forEach {
                            try {
                                val messageUser = toMessageUserEntity<MessageUserEntity>(it)
                                val message = toMessageEntity<MessageEntity>(it)

                                messageEntityList.add(message)

                                //处理 message user
                                if (it.senderId != senderId) {
                                    senderId = it.senderId
                                    messageUserEntityList.add(messageUser)
                                    val userDB = getMessageUserEntity<MessageUserEntity>(messageUser.userId)
                                    if (userDB == null) {
                                        dao.insertMessageUser(messageUser)
                                    } else {
                                        var needUpdate = false
                                        if (userDB.name != messageUser.name && messageUser.name.isNotEmpty()) {
                                            needUpdate = true
                                            userDB.name = messageUser.name
                                        }
                                        if (userDB.username != messageUser.username && messageUser.username.isNotEmpty()) {
                                            needUpdate = true
                                            userDB.username = messageUser.username
                                        }
                                        if (userDB.portrait != messageUser.portrait && messageUser.portrait.isNotEmpty()) {
                                            needUpdate = true
                                            userDB.portrait = messageUser.portrait
                                        }
                                        if (userDB.portraitIc != messageUser.portraitIc) {
                                            needUpdate = true
                                            userDB.portraitIc = messageUser.portraitIc
                                        }
                                        if (needUpdate) dao.updateMessageUser(userDB)
                                    }
                                }
                                //记录每个群最新消息时间戳
                                if (it.timetoken > idTimeTokenMap[it.conId] ?: 0L) {
                                    idTimeTokenMap[it.conId] = it.timetoken
                                }
                                val conversationsEntity =
                                    dao.getConversationByConId(message.conId, ownerId)
                                if (conversationsEntity == null){
                                    fetchConversation(message.conId)
                                }
                                if (message.type != 8) conIdMap.add(message.conId)
                                handleEveryMessage(it)
                            } catch (e: Exception) {
                            }
                        }
                        val messageList = messageEntityList.toList()

                        dao.insertMessages(messageList)
                        handleMessageWrapper(messageWrapper)

                        val time = System.currentTimeMillis()
                        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).postDelay(
                            MessageEvent(
                                2,
                                null,
                                null,
                                time,
                                isUnreadMessage,
                                timestampStart = messageEntityList.firstOrNull()?.sendTime
                                    ?: 0L,
                                timestampEnd = messageEntityList.lastOrNull()?.sendTime
                                    ?: 0L,
                            ), 120
                        )
                    } else {
                        if (isDecomposerUnreadMessage) {
                            isDecomposerUnreadMessage = false
                            //告诉大厅拉未读已经结束
                            LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(
                                MessageEvent(
                                    3,
                                    null,
                                    null,
                                    System.currentTimeMillis(),
                                    false
                                )
                            )
                        }

                        delay(100)
                    }

                    handleMessageWrapperAfter()
                }
            }
        }
    }

    //关闭处理消息处理协程
    fun stopMessageJob() {
        messageJob?.cancel()
        messageJob = null
        messageUserEntityList.clear()
        messageEntityList.clear()
    }

    //将消息包放入队列
    fun onDirectMessage(message: VenusDirectMessageWrapper?) {
        if (message == null) return
        messageSyncList.add(message)
    }

    protected abstract fun <T: MessageUserEntity>toMessageUserEntity(message: VenusDirectMessage): T
    protected abstract fun <T: MessageEntity>toMessageEntity(message: VenusDirectMessage):T
    protected abstract fun <T: MessageUserEntity>getMessageUserEntity(userId: String):T?
    //处理消息一些自定义操作
    protected abstract suspend fun handleEveryMessage(message: VenusDirectMessage)
    //本地没有改群需要去缓存
    protected abstract fun fetchConversation(conId: String)
    //处理消息包的一些自定义操作
    protected abstract suspend fun handleMessageWrapper(messageWrapper: VenusDirectMessageWrapper)
    //一个消息包处理完之后的操作，如更新会话最新消息的时间戳
    protected abstract suspend fun handleMessageWrapperAfter()
}