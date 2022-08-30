package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveGiftEvent(
    val uuid: String,
    @SerializedName("gift_id") val giftId: String,
    @SerializedName("show_seconds") val showSeconds: Int,
    val diamonds: Long,
    val weight: Int,
    @SerializedName("user_info") val userInfo: LiveUserInfo,
    @SerializedName("gift_info") var giftInfo: GiftInfo?,
    @SerializedName("report_uuid") val reportUUID: String = "",
) : LiveEvent {
    var isOwnerGift: Boolean = false
}

data class GiftInfo(
    @SerializedName("system_message") val systemMessage: SystemMessage,
    @SerializedName("special_effect") val specialEffect: SpecialEffect,
    val card: Card,
)

data class SystemMessage(
    @JsonAdapter(BooleanTypeAdapter::class) val show: Boolean,
    val content: String,
)

data class SpecialEffect(
    @JsonAdapter(BooleanTypeAdapter::class) val show: Boolean,
    val img: String,
)

data class Card(
    @JsonAdapter(BooleanTypeAdapter::class) val show: Boolean,
    @JsonAdapter(BooleanTypeAdapter::class) val highlight: Boolean,
    val img: String,
    @SerializedName("duration_viewer") val durationViewer: Int,
    @SerializedName("duration_host") val durationHost: Int,
    @SerializedName("duration_me") val durationMe: Int,
    val content: String,
)