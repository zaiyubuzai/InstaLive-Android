package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveWithInviteEvent(
    @SerializedName("target_user_id")val targetUserId: String,
    @SerializedName("target_user_info") val targetUserInfo:LiveUserInfo,
    @SerializedName("timeout_ts") val timeoutTS:Long,
): LiveEvent

data class LiveWithCancelEvent(
    @SerializedName("target_user_id") val targetUserId: String,
): LiveEvent
data class LiveWithRejectEvent(
    @SerializedName("target_user_id") val targetUserId: String,
): LiveEvent
data class LiveWithAgreeEvent(
    @SerializedName("target_user_id") val targetUserId: String,
    @SerializedName("live_user_infos") val liveUserWithUidInfos: List<LiveUserWithUidData>
): LiveEvent
data class LiveWithHangupEvent(
    @SerializedName("target_user_id") val targetUserId: String,
    @SerializedName("live_user_infos")val liveUserWithUidInfos: List<LiveUserWithUidData>
): LiveEvent

data class LiveUserWithUidData(
    val uid: Int,
    @SerializedName("user_info")val userInfo: LiveUserInfo,
    val mute: Int? = 0,
)
