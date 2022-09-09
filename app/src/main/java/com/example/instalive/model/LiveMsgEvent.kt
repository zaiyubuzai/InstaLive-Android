package com.example.instalive.model


//live event bus 事件
data class LiveMsgEvent(
    val uuid:String,
    val liveId: String,
    val type: Int,//1:send;2:received
    val sendType: Int,//send statue
    val comment: LiveCommentEvent? = null
)
