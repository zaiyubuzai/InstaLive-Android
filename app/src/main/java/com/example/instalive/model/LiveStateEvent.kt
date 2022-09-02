package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveStateEvent(
    @SerializedName("live_state") val liveState: Int,
    @SerializedName("live_id") val liveId: String,
    @SerializedName("report_uuid") val reportUUID: String,
) : LiveEvent