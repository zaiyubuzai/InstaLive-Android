package com.example.instalive.model

import com.google.gson.annotations.SerializedName

//{
//    "got_diamonds": 0,
//    "more": [
//        {
//            "text": "Group Owner got",
//            "diamonds": 0
//        },
//        {
//            "text": "Co-host got",
//            "diamonds": 0
//        },
//    ]
//}
data class LiveEndDetailData(
    @SerializedName("duration") val duration: String?,
    @SerializedName("got_diamonds") val gotDiamonds: Long,
    val more: List<LiveEndMoreData>? = listOf()
)

data class LiveEndMoreData(
    val text: String,
    val diamonds: Long,
)