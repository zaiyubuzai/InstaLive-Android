package com.example.instalive.model

import com.example.baselibrary.api.BaseRepositoryError
import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import java.io.Serializable

data class LiveStateInfo(
    val diamonds: Int,
    val id: String,
    @SerializedName("live_diamonds_public") val liveDiamondsPublic: Int,
    @SerializedName("live_gift") val liveGift: Int,
    @SerializedName("live_type") val liveType: Int,
    @SerializedName("live_user_infos") val liveWithUserInfos: List<LiveUserInfo>,
    @SerializedName("online_list") val onlineList: List<Online>?,
    val messages: List<String>,
    @SerializedName("online_str") val onlineStr: String,
    val owner: Owner,
    @SerializedName("raise_hand_count") val raiseHandCount: Int,
    @SerializedName("resolution") val resolution: Resolution,
    @SerializedName("resolution_level") val resolutionLevel: Int,
    val role: Int,
    val state: Int,
    val title: String,
    val token: String,
    val uid: Int,
):Serializable

data class JoinLiveError(
    val error: BaseRepositoryError.BaseErrorBody,
    @SerializedName("ext_data") val extData: JoinLiveExtData?
) {
    data class JoinLiveExtData(
        @SerializedName("conversation_id") val conversationId: String,
        @SerializedName("gift_info") val giftInfo: GiftData,
        @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("shadow_display") val shadowDisplay: Boolean = false,
        val owner: Owner,
        @SerializedName("token_info") val tokenInfo: TokenInfo
    )
}

//"live_duration": 1200, # 直播时长（s）
//"viewers": 100 # 观看人次