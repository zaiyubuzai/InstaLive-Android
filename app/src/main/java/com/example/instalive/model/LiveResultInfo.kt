package com.example.instalive.model

import com.google.gson.annotations.SerializedName

//{
//    "result":"ok",
//    "data":{
//        "live_duration":"00:03:14",
//        "viewer_count":1,
//        "like_count":0,
//        "diamonds":0,
//        "loading_data":true,
//        "you_got":0
//    }
//}
data class LiveResultInfo(
    @SerializedName("live_duration") val liveDuration: String,
    @SerializedName("viewer_count") val viewerCount: Int,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("unlock_live_diamonds") val unlockLiveDiamonds: Int?,
    @SerializedName("loading_data")val loadingData: Boolean,
    @SerializedName("you_got")val youGot: Int,
    val diamonds: Int,
)


//"live_duration": 1200, # 直播时长（s）
//"viewers": 100 # 观看人次