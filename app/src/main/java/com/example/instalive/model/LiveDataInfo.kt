package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable
//{
//  "code": 200,
//  "result": "ok",
//  "data": {
//    "diamonds": 100000,
//    "id": "le5Aaw8VuvyKYpkB",
//    "live_diamonds_public": 1,
//    "live_gift": 1,
//    "live_type": 1,
//    "live_user_infos": [
//      {
//        "uid": 11,
//        "user_info": {
//          "nickname": "wuyapeng",
//          "user_id": "pZvgxo2ozO",
//          "user_name": "wuyapeng"
//        }
//      }
//    ],
//    "messages": [
//      "Welcome to your Live! We're telling group members that you've started a Live."
//    ],
//    "online_str": "0",
//    "owner": {
//      "nickname": "wuyapeng",
//      "user_id": "pZvgxo2ozO",
//      "user_name": "wuyapeng"
//    },
//    "raise_hand_count": 8,
//    "resolution": {
//      "live_high": 720,
//      "live_width": 1280,
//      "live_with_high": 360,
//      "live_with_width": 640
//    },
//    "resolution_level": 4,
//    "role": 1,
//    "state": 1,
//    "title": " ",
//    "token": "007eJxSYJC+7ag1l79h66R3bj73WGZFxB5lLDo/R9z/qR7XW8VSyTQFBhMjA2MjS0uj5CTLFBNzUxMLM9PURJPkVHMT01TDlJQk3yWiyQJ8DAweaw8wMjIwMrAwMDKA+ExgkhlMsoBJAYacVFPHxHKLsNKySu/IgmwnJgZDQ0AAAAD//wVzIJM=",
//    "uid": 11
//  }
//}
data class LiveDataInfo(
    val diamonds: Long,
    val id: String,
    @SerializedName("live_diamonds_public") val liveDiamondsPublic: Int,
    @SerializedName("live_gift") val liveGift: Int,
    @SerializedName("live_type") val liveType: Int,
    @SerializedName("live_user_infos") val liveWithUserInfos: List<LiveUserWithUidData>,
    @SerializedName("online_list") val onlineList: List<Online>?,
    val messages: List<String>,
    @SerializedName("online_str") val onlineStr: String,
    val owner: Owner,
    @SerializedName("raise_hand_count") val raiseHandCount: Int,
    @SerializedName("resolution") val resolution: Resolution,
    @SerializedName("resolution_level") val resolutionLevel: Int,
    val role: Int,
    val state: Int,
    val title: String,
    val token: String,
    val uid: Int,
)

data class Resolution(
    @SerializedName("live_width") val liveWidth: Int,
    @SerializedName("live_high") val liveHigh: Int,
    @SerializedName("live_with_high") val liveWithHigh: Int,
    @SerializedName("live_with_width") val liveWithWidth: Int,
):Serializable


data class Owner(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    val nickname: String,
    val portrait: String,
    val bio: String?,
    @SerializedName("portrait_icon") val portraitIcon: String?,
):Serializable

data class Online(
    val id: String,
    val nickname: String,
    @SerializedName("user_name") val userName: String,
    val portrait: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_verified") val isVerified: Boolean,
    val relationship: Int
)

data class TokenInfo(val uid: String, val token: String):Serializable
