package com.example.instalive.model

import com.example.baselibrary.network.BooleanTypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.venus.dm.model.UserData
import com.venus.dm.model.UserRelation

//{
//    "meta": {
//    "offset": 0,
//    "limit": 50,
//    "next_offset": 0,
//    "has_next": false
//},
//    "data": [
//    {
//        "user_id": "LqNyW44jzV",
//        "user_name": "gsvssbs",
//        "nickname": "gsvssbs",
//        "portrait": "https://venus-media-test.s3.amazonaws.com/portraits/user/1221f474ee9111ebac69aeb109266e5a.png",
//        "bio": "",
//        "portrait_icon": null,
//        "role": 9
//    }
//    ]
//}
//{
//      "user_id": "Q16yG5o5wz",
//      "user_name": "wuyapeng1",
//      "nickname": "wyp111111",
//      "portrait": "https://test-res-cf-6a465plxmjyfa7614mqgoxfg.joinfambase.com/cdn-cgi/image/width=200,quality=60/portrait/user/c6b92c1e4c3c11eca276fa9e9090310d.jpeg",
//      "bio": "",
//      "portrait_icon": null,
//      "relationship": 0,
//      "role": 9,
//      "level": 0,
//      "unlocked": 0,
//      "diamonds": 0
//    }
data class LiveViewerData(
    @SerializedName("user_id") val userId: String,
    val nickname: String,
    @SerializedName("user_name") val username: String,
    val portrait: String,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_verified") val isVerified: Boolean?,
    @UserRelation var relationship: Int?,
    @SerializedName("rf_tag") val rfTag: String? = null,
    val diamonds: Long?,
    val level: Int = -1,
    @JsonAdapter(BooleanTypeAdapter::class) val unlocked: Boolean = false,
    var role: Int,
    @SerializedName("portrait_icon") val portraitIc: String?,
) {
    fun setFollowed() {
        if (relationship == UserData.USER_RELATION_DEFAULT) {
            relationship = UserData.USER_RELATION_FOLLOWING
        } else if (relationship == UserData.USER_RELATION_BEING_FOLLOWED) {
            relationship = UserData.USER_RELATION_MUTUAL_FOLLOWED
        }
    }

    fun setUnfollowd() {
        if (relationship == UserData.USER_RELATION_MUTUAL_FOLLOWED) {
            relationship = UserData.USER_RELATION_BEING_FOLLOWED
        } else if (relationship == UserData.USER_RELATION_FOLLOWING) {
            relationship = UserData.USER_RELATION_DEFAULT
        }
    }

    fun canFollow(): Boolean {
        return relationship == UserData.USER_RELATION_DEFAULT || relationship == UserData.USER_RELATION_BEING_FOLLOWED
    }
}

data class LiveViewerExtData(
    @SerializedName("unlocked_user_count") val unlockedUserCount: Int
)