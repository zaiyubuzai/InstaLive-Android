package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class NotificationSettingsData(
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("pause_type") val pauseType: Boolean,
    @SerializedName("pause_at") val pauseAT: Long?,//暂停结束的时间戳
)