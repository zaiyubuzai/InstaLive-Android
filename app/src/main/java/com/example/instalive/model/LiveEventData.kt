package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EventData(
    @SerializedName("event_id") val eventId: String,
    val name: String,
    val description: String,
    @SerializedName("event_ts") val eventTS: Long,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_interest") var isInterest: Boolean,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("owner_is_interest") var isOwnerInterest: Boolean,
    @SerializedName("switch_start_ts") val switchStartTS: Long, //turn on start_live timestamp
): Serializable

data class InterestedMemberData(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val username: String,
    val nickname: String,
    val portrait: String,
    val bio: String,
)

data class EventDetailData(
    @SerializedName("event_id") val eventId: String,
    val name: String,
    val description: String,
    @SerializedName("event_ts") val eventTS: Long,
    @SerializedName("interested_list") val interestedList: List<InterestedMemberData>? = listOf(),
    @SerializedName("interested_member_count") val interestedMemberCount: Int, //turn on start_live timestamp
): Serializable