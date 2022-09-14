package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

//{
//    "code": 200,
//    "result": "ok",
//    "data": {
//    "co_host_got_diamonds": 0,
//    "diamonds": 0,
//    "host_got_diamonds": 0,
//    "like_count": 0,
//    "live_duration": 195,
//    "loading_data": 0,
//    "ticket_diamonds": 0,
//    "viewer_count": 0
//}
//}
data class LiveCloseData(
    val diamonds: Long,
    @SerializedName("host_got_diamonds") val gotDiamonds: Long,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("live_duration") val liveDuration: Long,
    @SerializedName("viewer_count") val viewerCount: Int,
    @SerializedName("ticket_diamonds") val ticketDiamonds: Int,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("loading_data") val loadingData: Boolean,
)