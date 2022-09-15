package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConversationListData(
    @SerializedName("conversation_list") val conversationList: List<ConversationInfo> = listOf(),
) : Serializable

data class ConversationInfo(
    val id: String,
    val type: Int,
    val mute: Int,
    val recipients: List<RecipientsData>,
    @SerializedName("last_message_timestamp") val lastMessageTimestamp: Long?,
    val state: Int,
    val pin: Int
)
