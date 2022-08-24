package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConversationResponseData(
    val id: String,
    val type: Int,
    val mute: Int,
    val recipients: List<RecipientsData>?,
    @SerializedName("state") val chatState: Int?,
    @SerializedName("permanent_tips") val permanentTips: PermanentTipsInfo?,
    val online: Int,
    val pin: Int
):Serializable

data class RecipientsData(
    val id: String,
    @SerializedName("user_name") val username: String,
    val nickname: String,
    val portrait: String,
    @SerializedName("chat_state") val chatState: Int,
    val relationship: Int,
):Serializable

data class PermanentTipsInfo(
    @JsonAdapter(BooleanTypeAdapter::class) val show: Boolean,
    val image: String,
    val title: String,
    val desc: String,
    val deeplink: String,
    @SerializedName("live_id") val liveId: String?,
    @SerializedName("live_owner_id") val liveOwnerId: String?,
) : Serializable
