package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveRemoveEvent(
    @SerializedName("target_user_ids") val targetUserIds: List<String>? = listOf(),
    @SerializedName("ignore_user_ids") val ignoreUserIds: List<String>? = listOf(),
    @SerializedName("remove_type") val removeType: Int,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent