package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent
import java.io.Serializable

data class LikeEvent(
    @SerializedName("target_user_ids") val targetUserIds: List<String>? = listOf(),
    @SerializedName("like_num") val likeNum: Int,
    val uuid: String,
    @SerializedName("user_info") val userInfo: LiveUserInfo?,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent

data class LiveActivityEvent(
    @SerializedName("target_user_ids") val targetUserIds: List<String>? = listOf(),
    val event: Int,
    val content: String,
    @SerializedName("members_num") val membersNum: String?,
    @SerializedName("online_str") val onlineNumStr: String,
    @SerializedName("entry_beep") val entryBeep: Int?, // 0 表示没有声音  1，2，3，4 代表不同的声音
    @SerializedName("user_info") val userInfo: LiveUserInfo?,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("private_enabled") val privateEnabled: Boolean,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent

data class LiveUserInfo(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    val nickname: String,
    val portrait: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_verified") val verified: Boolean? = false,
    val bio: String? = null,
    @SerializedName("portrait_icon") val portraitIc: String?,
    var uid:Int?,
    var mute:Int?,
) : Serializable