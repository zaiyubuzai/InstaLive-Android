package com.venus.dm.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

/**
 *  "user_id": "ddsadsa",
"user_name": "haha2020",
"nickname": "haha2020",
"portrait": "http://res.5milesapp.com/image/upload/v1440581973/5miles/default-head.jpg",
"user_role": '1' # 1群主 2管理员 9普通成员
'portrait_icon': "",
'online': True,
 */
data class GroupMember(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val username: String,
    val nickname: String,
    val portrait: String,
    @SerializedName("user_role") var userRole: Int, // 1群主 2管理员 9普通成员
    @SerializedName("portrait_icon") val portraitIc: String?,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("online") var online: Boolean? = false,  // -1表示没有tiers上线 0表示未支持  1表示已经支持
    var level: Int = -1,
    @JsonAdapter(BooleanTypeAdapter::class) @SerializedName("is_performer") val isPerformer: Boolean,
)