package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveSystemEvent(
    @SerializedName("target_user_ids") val targetUserIds: List<String>? = listOf(),
    val content: String,
    val uuid: String,
    var isRequest: Int = 0,
    @SerializedName("report_uuid") val reportUUID: String = "",
) : LiveEvent