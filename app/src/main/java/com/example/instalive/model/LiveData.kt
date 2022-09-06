package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LiveData(
    val id: String,
    @SerializedName("owner")val liveOwner: LiveOwnerData
)
data class LiveOwnerData(
    @SerializedName("user_id")val userId: String,
    @SerializedName("user_name")val username: String,
    @SerializedName("nickname")val nickname: String,
)