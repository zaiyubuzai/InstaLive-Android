package com.example.instalive.model

import com.google.gson.annotations.SerializedName
import com.venus.dm.db.entity.MessageEntity


data class LiveCommentEvent(
    val content: String,
    val uuid: String,
    @SerializedName("user_info") val userInfo: LiveUserInfo,
    val type: Int = 1,
) {
    @Transient
    var sendStatus: Int = MessageEntity.SEND_STATUS_SUCCESS
}
