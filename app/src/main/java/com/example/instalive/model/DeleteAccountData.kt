package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class DeleteAccountData(
    val title: String?,
    val message: String?,
    @SerializedName("next_action") val nextAction: Int?,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_deleted") val isDeleted: Boolean,
)