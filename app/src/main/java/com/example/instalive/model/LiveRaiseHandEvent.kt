package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveRaiseHandEvent(
    val event: Int,
    @SerializedName("raise_hand_count") val raiseHandCount: Int,
    @SerializedName("user_info") val userInfo: LiveUserInfo,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent