package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LiveViewerData(
    var role: Int,
    @SerializedName("user_info") val userInfo: LiveUserInfo,
    var diamonds: Long = 0,
)

data class LiveViewerExtData(
    @SerializedName("online_str") val onlineStr: String,
    val unlocked: String
)
