package com.venus.dm.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.venus.dm.model.GroupMember
import org.json.JSONObject

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["send_time"], unique = true),
        Index(value = ["uuid"], unique = true),
        Index(value = ["conversation_id"]),
        Index(value = ["sender_id"]),
        Index(value = ["user_id"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "user_id") var userId: String = "",
    @ColumnInfo(name = "conversation_id") var conId: String = "",
    var uuid: String = "",
    @ColumnInfo(name = "sender_id") var senderId: String = "",
    @ColumnInfo(name = "sender_portrait") var senderPortrait: String = "",
    @ColumnInfo(name = "portrait_icon") var portraitIc: String = "",
    @ColumnInfo(name = "sender_name") var senderName: String = "",
    @ColumnInfo(name = "sender_user_name") var senderUsername: String = "",
    @ColumnInfo(name = "user_role") var userRole: Int? = 0,
    var type: Int = 1,
    var payload: String = "",
    var content: String? = null,
    @ColumnInfo(name = "send_time") var sendTime: Long = 0L,
    @ColumnInfo(name = "send_status") var sendStatus: Int = 1,
    @ColumnInfo(name = "extra_info") var extraInfo: String = "",
    @ColumnInfo(name = "message_failed") var messageFailed: Int? = null,
    @ColumnInfo(name = "msg_disappear_tt") var msgDisappearTT: Long = 0L,
    var read: Int = 0,
    var state: Int = 1,
    @ColumnInfo(name = "render_type") var renderType: Int = 1,
    @ColumnInfo(name = "is_mention_me") var isMentionMe: Int = 0,
    @ColumnInfo(name = "local_res_path") var localResPath: String? = null,
    @ColumnInfo(name = "local_thumbnail") var localThumbnail: String? = null,
    @ColumnInfo(name = "live_id") var liveId: String? = null,
    @ColumnInfo(name = "show_type") var showType: Int = 0,// 0 都展示（默认）  1 会话中 2 直播中  3 都展示
) {

    @Transient
    var samePersonSaidAgain: Boolean = false

    fun toJson(): JSONObject {
        val jo = JSONObject()
        val payload = JSONObject(payload)
        jo.put("uuid", uuid)
        jo.put("conversation_id", conId)
        jo.put("sender_id", senderId)
        jo.put("type", type)
        jo.put("payload", payload)
        return jo
    }

    companion object {
        const val SEND_STATUS_SENDING = 0
        const val SEND_STATUS_SUCCESS = 1
        const val SEND_STATUS_FAILED = 2
    }

    data class Payload(
        val content: String? = null,
        var url: String? = null,
        var cover: String? = null,
        val height: Int? = null,
        val width: Int? = null,
        var size: Long? = null,
        val length: Int? = null,
        @SerializedName("user_name")
        val userName: String? = null,
        @SerializedName("full_name")
        val fullName: String? = null,
        val id: String? = null,
        val portrait: String? = null,
        val desc: String? = null,
        val title: String? = null,
        val status: Int? = null,
        @SerializedName("message_uuid")
        val uuid: String? = null,
        @SerializedName("target_uuid")
        val targetUUID: String? = null,
        @SerializedName("content_me")
        val contentMe: String? = null,
        @SerializedName("sender_portrait")
        val senderPortrait: String? = null,
        @SerializedName("sender_name")
        val senderName: String? = null,
        @SerializedName("thumbnail_url")
        val thumbnail: String? = null,
        val amount: Int? = null,
        val deeplink: String? = null,
        val state: Int? = null,
        @SerializedName("share_user_id")
        val shareUserId: String? = null,
        @SerializedName("user_id")
        val userId: String? = null,
        @SerializedName("target_msg")
        var targetMessage: TargetMessage? = null,
        @SerializedName("live_id")
        val liveId: String? = null,
        @SerializedName("show_type")
        val showType: Int = 0,
        @SerializedName("render_type")
        val renderType: Int? = 1,
        val level: Int = -1,
        @SerializedName("gift_img")
        val giftImg: String? = null,
        @SerializedName("gift_name")
        val giftName: String? = null,
        @SerializedName("gift_id")
        val giftId: String? = null,
        @SerializedName("user_info")
        val userInfo: GroupMember? = null,
        val highlight: Int = 0,
        @SerializedName("target_timetoken")
        var targetTimetoken: Long? = null,
    ) {

        companion object {
            fun fromJson(json: String): Payload? {
                try {
                    return Gson().fromJson(json, Payload::class.java)
                } catch (e: Exception) {

                }
                return null
            }
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    class TargetMessage(
        val uuid: String,
        val payload: Payload,
        @SerializedName("sender_name") val senderName: String,
        val type: Int,
        var state: Int = 1,//1:normal, 2:delete
    ) {
        companion object {
            fun fromJson(json: String): TargetMessage {
                return Gson().fromJson(json, TargetMessage::class.java)
            }
        }

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }
}
