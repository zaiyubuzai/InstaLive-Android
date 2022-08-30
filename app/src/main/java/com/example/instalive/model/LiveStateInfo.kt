package com.example.instalive.model

import com.example.baselibrary.api.BaseRepositoryError
import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import java.io.Serializable

data class LiveStateInfo(
    val id: String,
    val owner: Owner,
    @SerializedName("token_info") val tokenInfo: TokenInfo,
    @SerializedName("online_num") val onlineNum: Long,
    @SerializedName("online_str") val onlineNumStr: String,
    val title: String,
    val state: Int,
    @SerializedName("live_user_infos") val liveWithUserInfos: List<LiveUserInfo>,
    val uid: Int,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("live_gift") val liveGiftShowEnable: Boolean,
    @SerializedName("request_on_join") val requestOnJoin: String?,
    @SerializedName("resolution_level") val resolutionLevel: Int,
    @SerializedName("resolution") val resolution: Resolution,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("live_diamonds_public") val liveDiamondsPublic: Boolean,
    val role: Int,
    val messages: List<String>?,
    val diamonds: Long,
    @SerializedName("raise_hand_count") val raiseHandCount: Int,
    @SerializedName("need_live_with") val needLiveWith: Int,
    @SerializedName("conversation_id") val conversationId: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_paid_live") val isPaidLive: Boolean,
    @SerializedName("has_raised_hands") val hasRaisedHands: Int,
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