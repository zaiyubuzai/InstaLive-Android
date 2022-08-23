package com.venus.dm.model

import com.google.gson.annotations.SerializedName
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageEntity.Companion.SEND_STATUS_SUCCESS
import com.venus.dm.db.entity.MessageUserEntity

class VenusDirectMessageWrapper {
    lateinit var type: String
    @SerializedName("receive_mode")
    lateinit var receiveMode: String
    lateinit var messages: List<VenusDirectMessage>
    @SerializedName("report_uuid")
    lateinit var reportUUID: String

    //是否拉未读拉到的消息
    var isUnreadMessage: Boolean = false

    //是否为会话socket的消息
    var isLiveCommentMessage: Boolean = false

}

class VenusDirectMessage {

    var uuid: String = ""
    var timetoken: Long = 0L
    @SerializedName("sender_id")
    var senderId: String = ""
    @SerializedName("conversation_id")
    var conId: String = ""
    var type: Int = 0
    lateinit var payload: MessageEntity.Payload
    @SerializedName("sender_portrait")
    var senderPortrait: String = ""
    @SerializedName("sender_name")
    var senderName: String = ""
    @SerializedName("sender_user_name")
    var senderUsername: String = ""
    @SerializedName("live_comment")
    var liveComment: Int = 0
    @SerializedName("msg_disappear_tt")
    var msgDisappearTT: Long = 0L
    @SerializedName("conversation_disappear_tt")
    var conversationDisappearTT: Long = 0L

    //    @SerializedName("is_supported") lateinit var isSupported: Int? = -1,  // -1表示没有tiers上线 0表示未支持  1表示已经支持
    @SerializedName("user_role")
    var userRole: Int? = null
    @SerializedName("portrait_icon")
    var senderPortraitIc: String? = null
    var read: Int = 0
    @SerializedName("report_uuid")
    lateinit var reportUUID: String

    fun toMessageEntity(ownerUserId: String): MessageEntity {
        return MessageEntity(
            id = 0,
            userId = ownerUserId,
            conId = conId,
            uuid = uuid,
            senderId = senderId,
            senderPortrait = senderPortrait,
            portraitIc = "",
            senderName = senderName,
            senderUsername = senderUsername,
            userRole = userRole,
            type = type,
            payload = payload.toString(),
            content = payload.content,
            sendTime = timetoken,
            sendStatus = SEND_STATUS_SUCCESS,
            extraInfo = "",
            messageFailed = 0,
            msgDisappearTT = msgDisappearTT,
            read = read,
            renderType = getRenderType(),
            isMentionMe = 0,
        )
    }

    fun toMessageUserEntity(): MessageUserEntity {
        return if (!senderId.isNullOrEmpty()) {
            MessageUserEntity(
                userId = senderId,
                name = senderName,
                portrait = senderPortrait,
                username = senderUsername,
                portraitIc = senderPortraitIc
            )
        } else {
            if (payload.userInfo != null) {
                MessageUserEntity(
                    userId = payload.userInfo!!.userId,
                    name = payload.userInfo!!.nickname,
                    portrait = payload.userInfo!!.portrait,
                    username = payload.userInfo!!.username,
                    portraitIc = payload.userInfo!!.portraitIc
                )
            } else {
                MessageUserEntity(
                    userId = senderId,
                    name = senderName,
                    portrait = senderPortrait,
                    username = senderUsername,
                    portraitIc = senderPortraitIc
                )
            }
        }
    }

    private fun getShowType(): Int {
        return payload.showType
    }

    private fun getRenderType(): Int {
        return payload.renderType ?: 1
    }

}