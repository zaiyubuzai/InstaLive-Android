package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ConversationResponseData(
    val id: String,
    val type: Int,
    val mute: Int,
    val recipients: List<RecipientsData>? = listOf(),
    @SerializedName("state") val chatState: Int?,
    @SerializedName("permanent_tips") val permanentTips: PermanentTipsInfo?,
    val online: Int,
    val pin: Int
):Serializable


//{
//  "result": "ok",
//  "data": {
//    "conversation_list": [
//      {
//        "id": "deXwDg2wmn631J",
//        "type": 1,
//        "state": 1,
//        "mute": 0,
//        "recipients": [
//          {
//            "user_id": "pZvgxo2ozO",
//            "user_name": "wuyapeng",
//            "nickname": "wuyapeng",
//            "portrait": "",
//            "bio": "",
//            "portrait_icon": "",
//            "chat_state": 1,
//            "relationship": -1
//          },
//          {
//            "user_id": "9Om8Eq8EZz",
//            "user_name": "XieTee",
//            "nickname": "XieTee",
//            "portrait": "",
//            "bio": "",
//            "portrait_icon": "",
//            "chat_state": 1,
//            "relationship": 0
//          }
//        ],
//        "permanent_tips": {
//          "show": 0,
//          "image": "",
//          "title": "",
//          "desc": "",
//          "deeplink": "",
//          "live_id": "",
//          "live_owner_id": ""
//        },
//        "pin": 0
//      }
//    ]
//  }
//}
data class RecipientsData(
    @SerializedName("user_id")val id: String,
    @SerializedName("user_name") val username: String,
    @SerializedName("portrait_icon") val portraitIc: String,
    val nickname: String,
    val portrait: String,
    val bio: String,
    @SerializedName("chat_state") val chatState: Int,
    val relationship: Int,
):Serializable

data class PermanentTipsInfo(
    @JsonAdapter(BooleanTypeAdapter::class) val show: Boolean,
    val image: String,
    val title: String,
    val desc: String,
    val deeplink: String,
    @SerializedName("live_id") val liveId: String?,
    @SerializedName("live_owner_id") val liveOwnerId: String?,
) : Serializable
