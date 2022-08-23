package com.venus.dm.model.event

data class MessageEvent(
    val type: Int,//1:自己发送的消息;2：socket推送的消息；3:拉未读结束；4:更新消息数据内容；5：删除消息，并在该位置新增消息；6：从数据库取出来的消息；7：删除消息
    val uuid: String?,
    val targetUUID: String?,//type=5:target message
    val timestamp: Long,
    val isLoading: Boolean = false,//正在loading中不刷新列表,主要时拉未读消息时，暂时拉取的消息在没有拉取完时不显示
    val deleteMessageTimetoken: Long = 0L,
    val timestampStart: Long = 0L,
    val timestampEnd: Long = 0L,
)
