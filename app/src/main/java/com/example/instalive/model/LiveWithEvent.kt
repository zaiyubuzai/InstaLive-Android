package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveWithInviteEvent(
    val id: String,
    @SerializedName("target_user_id")val targetUserId: String,
    @SerializedName("target_user_info") val targetUserInfo:LiveUserInfo,
    @SerializedName("timeout_ts") val timeoutTS:Long,
): LiveEvent

data class LiveWithCancelEvent(
    val id: String,
): LiveEvent
data class LiveWithRejectEvent(
    val id: String,
): LiveEvent
data class LiveWithAgreeEvent(
    val id: String,
    @SerializedName("live_user_infos")val liveUserInfos: List<LiveWithAgreeUserData>
): LiveEvent
data class LiveWithHangupEvent(
    val id: String,
    @SerializedName("target_user_id") val targetUserId: String,
    @SerializedName("live_user_infos")val liveUserInfos: List<LiveWithAgreeUserData>
): LiveEvent

data class LiveWithAgreeUserData(
    val uid: Int,
    @SerializedName("user_info")val userInfo: LiveUserInfo
)
