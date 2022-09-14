package com.venus.dm.db.dao

import androidx.room.*
import com.jeremyliao.liveeventbus.LiveEventBus
import com.venus.dm.app.ChatConstants
import com.venus.dm.db.entity.*
import com.venus.dm.model.event.MessageEvent
import kotlinx.coroutines.flow.Flow

/**
 * 消息数据库操作，使用时项目初始化的自定义RoomDatabase类需要继承VenusAppDatabase
 */
@Dao
interface DirectMessageDao {
    @Query("SELECT * FROM messages WHERE uuid = :uuid AND user_id = :userId")
    fun getMessagedByUuid(uuid: String, userId: String):MessageEntity?

    @Query("SELECT * FROM messages WHERE send_time < :timeToken AND conversation_id = :conId AND user_id = :currentUserId AND state == 1 ORDER BY send_time DESC LIMIT 100")
    fun getMessagesByConIdDesc(
        conId: String,
        currentUserId: String,
        timeToken: Long,
    ): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE send_time >= :startTimeToken AND send_time <= :endTimeToken AND conversation_id = :conId AND user_id = :currentUserId AND state == 1 ORDER BY send_time")
    fun getMessagesByConIdNewInsert(
        conId: String,
        currentUserId: String,
        startTimeToken: Long,
        endTimeToken: Long,
    ): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE send_time < :timeToken AND conversation_id = :conId AND user_id = :currentUserId AND state == 1 ORDER BY send_time LIMIT 100")
    fun getMessagesByConIdMore(
        conId: String,
        currentUserId: String,
        timeToken: Long,
    ): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversation_id = :conId AND user_id = :currentUserId AND state == 1 AND type IN (3, 4) ORDER BY send_time LIMIT 100")
    fun getMediaMessagesByConId(
        conId: String,
        currentUserId: String,
    ): List<MessageEntity>

    @Query("SELECT * FROM conversations WHERE user_id = :currentUserId ORDER BY last_msg_timetoken DESC LIMIT 20")
    fun getLatestTenConversations(currentUserId: String): Flow<List<ConversationsEntity>>

    @Query("SELECT * FROM conversations WHERE user_id = :currentUserId AND recipient_username LIKE :searchContent||'%'  ORDER BY last_msg_timetoken DESC")
    fun getLatestTenConversationsAll(
        currentUserId: String,
        searchContent: String,
    ): List<ConversationsEntity>

    @Query("SELECT * FROM messages WHERE user_id = :currentUserId AND conversation_id = :conId AND type = :type")
    fun  getTypeConversationsMessages(
        conId: String,
        currentUserId: String,
        type: Int
    ): List<MessageEntity>

    //若要在消息列表更新，请使用下方函数
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(t: MessageEntity)

    //若要在消息列表更新，请使用下方函数
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(t: List<MessageEntity>)

    @Update
    fun updateMessage(t: MessageEntity)

    @Delete
    fun deleteMessage(t: MessageEntity)

    @Query("DELETE FROM messages WHERE send_time = :timeToken")
    fun deleteMessage(timeToken: Long)

    @Query("DELETE FROM messages WHERE msg_disappear_tt < :currentTimestamp AND msg_disappear_tt != 0")
    fun disappearMessage(currentTimestamp: Long)

    @Query("DELETE FROM messages WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun deleteMessageBasedOnConversationId(conId: String, currentUserId: String)

    @Query("DELETE FROM conversations WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun deleteConversation(conId: String, currentUserId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(t: ConversationsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertConversationNoneReplace(t:ConversationsEntity)

    @Query("SELECT a.*, COUNT(b.id) AS unread_count FROM conversations a LEFT JOIN messages b ON a.conversation_id = b.conversation_id AND b.send_time > a.last_read AND b.read = 0 AND b.user_id = :currentUserId AND b.sender_id <> :currentUserId AND b.state = 1 WHERE a.user_id = :currentUserId GROUP BY a.conversation_id ORDER BY a.is_pin DESC, a.last_msg_timetoken DESC")
    fun getConversationsFlow(currentUserId: String): Flow<List<ConversationsEntity>>

    @Query("SELECT * FROM conversations WHERE user_id = :currentUserId")
    fun getAllConversations(currentUserId: String): List<ConversationsEntity>

    @Query("SELECT * FROM conversations WHERE recipient_id = :recipient_id AND user_id = :currentUserId")
    fun getConversationByRecipientId(
        recipient_id: String,
        currentUserId: String
    ): List<ConversationsEntity>

    @Query("SELECT * FROM conversations WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun getConversationByConId(conId: String, currentUserId: String): ConversationsEntity?

    @Update
    fun updateConversation(t: ConversationsEntity)

    @Query("UPDATE conversations SET being_at = 0 WHERE conversation_id = :conId AND user_id = :userId")
    fun clearBeingAt(conId: String, userId: String)

    @Query("UPDATE messages SET send_status = :sendStates WHERE uuid = :uuid")
    fun updateMessageSendStatus(uuid: String, sendStates: Int)

    @Query("UPDATE conversations SET last_leave_timetoken = last_msg_timetoken WHERE conversation_id = :conId AND user_id == :userId")
    fun updateConversationLeaveTimeToken(conId: String, userId: String)

    @Query("UPDATE conversations SET being_at = :beingAt WHERE conversation_id = :conId AND user_id = :userId")
    fun removeConversationATMeState(conId: String, userId: String, beingAt: Int)

    @Query("UPDATE conversations SET last_msg_timetoken = :timeToken WHERE conversation_id = :conId AND user_id = :userId AND last_msg_timetoken < :timeToken")
    fun updateConversationLastTimeToken(conId: String, userId: String, timeToken: Long)

    @Query("SELECT max(send_time) FROM messages WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun getLatestRecievedTimetoken(conId: String, currentUserId: String): Long?

    @Query("SELECT max(send_time) FROM messages WHERE user_id = :currentUserId")
    fun getLatestRecievedTimetoken(currentUserId: String): Long?

    @Query("UPDATE conversations SET last_read = :timeToken WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun updateConversationLastRead(conId: String, timeToken: Long, currentUserId: String)

    @Query("SELECT max(send_time) FROM messages WHERE user_id = :currentUserId")
    fun getLatestReceivedTimeToken(currentUserId: String): Long?

    @Query("SELECT max(send_time) FROM messages WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun getLatestReceivedTimeToken(conId: String, currentUserId: String): Long?

    @Query("SELECT last_read FROM conversations WHERE conversation_id = :conId AND user_id = :currentUserId")
    fun getConversationLastRead(conId: String, currentUserId: String): Long

    @Query("SELECT * FROM messages WHERE conversation_id = :conId AND sender_id <> :currentUserId AND user_id = :currentUserId ORDER BY send_time DESC LIMIT 1")
    fun getLatestConversationMessageNotMe(
        conId: String,
        currentUserId: String
    ): MessageEntity?

    @Query("SELECT COUNT(1) FROM conversations a, messages b WHERE a.conversation_id = b.conversation_id AND b.send_time > a.last_read AND a.user_id = :currentUserId AND b.user_id = :currentUserId AND b.state == 1 AND b.read == 0 AND b.sender_id <> :currentUserId")
    fun getTotalUnreadCount(currentUserId: String): Flow<Int>

    @Query("SELECT COUNT(1) FROM messages WHERE user_id = :currentUserId AND conversation_id = :conId AND state == 1 AND read == 0 AND send_time > :timeToken AND sender_id <> :currentUserId")
    fun getConversationUnreadCount(currentUserId: String, conId: String, timeToken: Long): Int

    @Query("SELECT * FROM messages WHERE user_id = :currentUserId AND conversation_id = :conId AND state == 1 AND read == 0 AND send_time > :timeToken AND sender_id <> :currentUserId ORDER BY send_time LIMIT 1")
    fun getConversationUnreadFirstMessage(currentUserId: String, conId: String, timeToken: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE uuid = :uuid AND user_id = :userId AND conversation_id = :conId")
    fun getMessagedByUuid(uuid: String, userId: String, conId: String): MessageEntity?

    @Query("SELECT * FROM messages_users WHERE user_id = :userId")
    fun getMessageUserById(userId: String): MessageUserEntity?

    @Query("SELECT * FROM messages_users WHERE user_name = :username")
    fun getMessageUserByUsername(username: String): MessageUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageUser(t: MessageUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageUsers(messageUserEntity: List<MessageUserEntity>)

    @Update
    fun updateMessageUser(t: MessageUserEntity)

    @Transaction
    suspend fun updateMessageAndUpdateConversation(
        messageEntity: MessageEntity,
        isFinalStep: Boolean = false,
    ) {
        updateMessage(messageEntity)
        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(MessageEvent(4, messageEntity, null, System.currentTimeMillis()))
        if (isFinalStep) {
            updateMessageSendStatus(messageEntity.uuid, messageEntity.sendStatus)
        }
    }

    @Transaction
    fun <T: MessageEntity> insertOwnerMessageAndShow(
        messageEntity: T
    ): MessageEvent {
        insertMessage(messageEntity)
        LiveEventBus.get(ChatConstants.EVENT_BUS_KEY_MESSAGE_EVENT).post(MessageEvent(1, messageEntity, null, System.currentTimeMillis()))
        return MessageEvent(
            1,
            messageEntity,
            null,
            System.currentTimeMillis()
        )
    }

}