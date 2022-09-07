package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LiveViewerData(
    var role: Int,
    @SerializedName("user_info") val userInfo: LiveUserInfo,
)
