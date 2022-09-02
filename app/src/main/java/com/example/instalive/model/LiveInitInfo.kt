package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class LiveInitInfo(
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("paid_live_enable") val paidLiveEnable: Boolean,
    @SerializedName("paid_live_tips_title") val paidLiveTipsTitle: String,
    @SerializedName("paid_live_tips_desc") val paidLiveTipsDesc: String,
    @SerializedName("default_ticket_gift_id") val defaultTicketGiftId: String,
    @SerializedName("default_ticket_gift_info") val defaultTicketGiftInfo: GiftData?,
    @SerializedName("midway_switch_paid_live_title") val midwayPaidLiveTitle: String,
    @SerializedName("midway_switch_paid_live_desc") val midwayPaidLiveDesc: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("paid_live_function_enable") val paidLiveFunctionEnable: Boolean,
    @SerializedName("performer_desc") val performerDesc: String,
    @SerializedName("divide_popup_title") val dividePopupTitle: String,
    @SerializedName("divide_popup_desc") val dividePopupDesc: String,
)