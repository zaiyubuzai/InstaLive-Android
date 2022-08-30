package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LiveDataInfo(
    val id: String,
    val uid: Int,
    val owner: Owner,
    @SerializedName("online_num") val onlineNum: Int,
    @SerializedName("online_str") val onlineStr: String,
    val title: String,
    val role: Int,
    val diamonds: Int,
    @SerializedName("live_with") val liveWith: Int,
    val beauty: Int,
    @SerializedName("state") val state: Int,
    @SerializedName("online_list") val onlineList: List<Online>?,
    val messages: List<String>,
    @SerializedName("token_info") val tokenInfo: TokenInfo,
    @SerializedName("live_user_infos") val liveWithUserInfos: List<LiveUserInfo>,
    @SerializedName("resolution_level") val resolutionLevel: Int,
    @SerializedName("resolution") val resolution: Resolution,
    @SerializedName("join_music_link") val joinMusicLink: String,
    @SerializedName("raise_hand_count") val raiseHandCount: Int,
    @SerializedName("request_on_join") val requestOnJoin: String
)

data class Resolution(
    @SerializedName("live_width") val liveWidth: Int,
    @SerializedName("live_high") val liveHigh: Int,
    @SerializedName("live_with_high") val liveWithHigh: Int,
    @SerializedName("live_with_width") val liveWithWidth: Int,
):Serializable


data class Owner(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    val nickname: String,
    val portrait: String,
    val bio: String?,
    @SerializedName("portrait_icon") val portraitIcon: String?,
):Serializable

data class Online(
    val id: String,
    val nickname: String,
    @SerializedName("user_name") val userName: String,
    val portrait: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_verified") val isVerified: Boolean,
    val relationship: Int
)

data class TokenInfo(val uid: String, val token: String):Serializable
