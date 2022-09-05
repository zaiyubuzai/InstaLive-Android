package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LiveDiamondsPublicEvent(
    @SerializedName("live_diamonds_public") val liveDiamondsPublic: Int,
    @SerializedName("diamonds") val diamonds: Long,
)
