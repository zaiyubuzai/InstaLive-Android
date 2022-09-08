package com.example.instalive.model

import com.venus.dm.db.entity.MessageEntity

data class LiveMsgEvent(
    val uuid:String,
    val liveId: String,
    val type: Int,
    val messageEntity: MessageEntity? = null
)