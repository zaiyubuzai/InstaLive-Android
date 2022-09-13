package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LevelData(
    val version: Int,
    @SerializedName("level_icons")val levelIcons: List<LevelIconData>
)
data class LevelIconData(
    val icon: String,
    val level: Int,
)

data class MyLevelData(
    val level: Int,
    @SerializedName("prev_level") val prevLevel: Int?,
    @SerializedName("valid_amount") val validAmount: Long,
    @SerializedName("next_level_amount") val nextLevelAmount: Long,
    @SerializedName("level_amount") val levelAmount: Long,
)

data class LiveSendGiftResponse(
    val balance: Long,
    val level: Int,
    @SerializedName("valid_amount") val validAmount: Long,
    @SerializedName("next_level_amount") val nextLevelAmount: Long,
    @SerializedName("level_amount") val levelAmount: Long = 1,
    @SerializedName("gift_info") val giftInfo: GiftInfo?,
) {
    fun toMyLevelData(prevLevel: Int): MyLevelData {
        return MyLevelData(
            level,
            prevLevel,
            validAmount,
            nextLevelAmount,
            levelAmount
        )
    }
}