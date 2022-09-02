package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveWithCallEvent(
    val id: String,
    val event: Int,
    @SerializedName("end_time") val endTime: Long,
    @SerializedName("user_info") val userInfo: LiveUserInfo,
    @SerializedName("live_user_infos") val liveUserInfos: List<LiveUserInfo>,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent