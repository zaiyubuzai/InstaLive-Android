package com.example.instalive.utils

import com.example.instalive.app.SessionPreferences
import com.example.instalive.db.InstaLiveDBProvider
import com.venus.dm.db.dao.DirectMessageDao
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageUserEntity
import com.venus.dm.message.ChatMessagesDecomposer
import com.venus.dm.model.VenusDirectMessage
import com.venus.dm.model.VenusDirectMessageWrapper

abstract class BaseInstaSocket: ChatMessagesDecomposer() {
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

    override fun handleEveryMessage(message: VenusDirectMessage) {

    }

    override fun fetchConversation(conId: String) {

    }

    override fun handleMessageWrapper(messageWrapper: VenusDirectMessageWrapper) {

    }

    override fun handleMessageWrapperAfter() {

    }

    abstract fun initSocket(initDM: Boolean)
    abstract fun releaseSocket()
}