package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GiftListData(
    val version: Int,
    val gifts: List<GiftData>,
) : Serializable

data class GiftData(
    val id: String,
    val coins: Long,
    @SerializedName("display_time") val displayTime: Int,
    val name: String,
    val image: String,
    @SerializedName("gift_info") val giftInfo: GiftInfo?,
) : Serializable