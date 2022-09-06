package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

//"event": 1, # 1 静音 2 取消静音 3 摄像头关闭 4 摄像头开启
//"target_user_info": {
//    "id": "XkPMQjWP9b",
//    "user_name": "",
//    "nickname": "",
//    "portrait": "",
//    "is_verified" : 1,
//},
data class PublisherStateEvent(
    val event: Int,
    @SerializedName("target_user_info") val targetUserInfo: TargetUserInfo,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent

