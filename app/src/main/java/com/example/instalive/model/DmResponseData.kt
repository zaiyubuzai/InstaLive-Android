package com.example.instalive.model

import com.google.gson.annotations.SerializedName


data class DmResponseData(
    val timetoken: Long,
    @SerializedName("msg_disappear_tt") val msgDisappearTT: Long,
    @SerializedName("conversation_disappear_tt") val conversationDisappearTT: Long,
    val level: Int,
    @SerializedName("portrait_icon") val portraitIc: String
)

data class DmRecallResponseData(
    val timetoken: Long,
    @SerializedName("msg_disappear_tt") val msgDisappearTT: Long,
    @SerializedName("conversation_disappear_tt") val conversationDisappearTT: Long,
)