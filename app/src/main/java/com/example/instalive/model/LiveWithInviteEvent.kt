package com.example.instalive.model

import com.google.gson.annotations.SerializedName

data class LiveWithInviteEvent(
    val id: String,
    @SerializedName("timeout_timestamp") val timeout: Long
)