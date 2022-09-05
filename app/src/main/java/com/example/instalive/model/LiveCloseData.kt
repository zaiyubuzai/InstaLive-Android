package com.example.instalive.model

import com.google.gson.annotations.SerializedName

//        "diamonds": 9999,
//        "host_got_diamonds": 9999,
//        "like_count": 10,
//        "live_duration": 191,
//        "viewer_count": 1
data class LiveCloseData(
    val diamonds: Long,
    @SerializedName("host_got_diamonds")val gotDiamonds: Long,
    @SerializedName("like_count")val likeCount: Int,
    @SerializedName("live_duration")val liveDuration: Long,
    @SerializedName("viewer_count")val viewerCount: Int,
)