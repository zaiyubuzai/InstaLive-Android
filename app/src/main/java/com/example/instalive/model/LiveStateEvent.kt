package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.jeremyliao.liveeventbus.core.LiveEvent

data class LiveStateEvent(
    @SerializedName("live_state") val liveState: Int,
) : LiveEvent