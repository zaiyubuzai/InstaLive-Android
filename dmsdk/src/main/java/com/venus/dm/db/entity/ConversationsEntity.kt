package com.venus.dm.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "conversations")
data class ConversationsEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "user_id")
    var userId: String = "",

    @ColumnInfo(name = "conversation_id")
    var conversationId: String = "",

    @ColumnInfo(name = "type")
    var type: Int = 1,

    @ColumnInfo(name = "recipient_name")
    var recipientName: String = "",

    @ColumnInfo(name = "recipient_portrait")
    var recipientPortrait: String = "",

    @ColumnInfo(name = "last_msg_content")
    var lastMsgContent: String? = null,

    @ColumnInfo(name = "last_sender_name")
    var lastSenderName: String? = null,

    @ColumnInfo(name = "last_msg_user_id")
    var lastMsgUserId: String? = null,

    @ColumnInfo(name = "last_msg_type")
    var lastMsgType: Int? = null,

    @ColumnInfo(name = "last_msg_timetoken")
    var lastMsgTimetoken: Long = 0,

    @ColumnInfo(name = "last_read")
    var lastRead: Long = 0L,

    @ColumnInfo(name = "last_leave_timetoken")
    var lastLeaveTimetoken: Long = 0L,

    @ColumnInfo(name = "last_msg_status")
    var lastMsgStatus: Int = 1,

    @ColumnInfo(name = "chat_state")
    var chatState: Int = 1,
    @ColumnInfo(name = "mute")
    var mute: Int? = null,

    @ColumnInfo(name = "being_at")
    var beingAt: Int? = null,

    @ColumnInfo(name = "is_pin")
    var isPin: Int = 0,

    @ColumnInfo(name = "recipient_id")
    var recipientId: String = "",

    @ColumnInfo(name = "recipient_username")
    var recipientUsername: String = "",

    @ColumnInfo(name = "relationship")
    var relationship: Int = 1,

    @ColumnInfo(name = "owner_id")
    var ownerId: String = "",

    @ColumnInfo(name = "member_count")
    var memberCount: Int = 1,

    @ColumnInfo(name = "recipients")
    var recipients: String = "",

    @ColumnInfo(name = "unread_count")
    var unreadCount: Int = 9,

    @ColumnInfo(name = "living")
    var living: Int = 0,

    @ColumnInfo(name = "user_role")
    var role: Int = 9,
) : Serializable