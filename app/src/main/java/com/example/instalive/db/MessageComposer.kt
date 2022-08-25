package com.example.instalive.db

import com.example.instalive.InstaLiveApp
import com.example.instalive.app.SessionPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageEntity.Companion.SEND_STATUS_SENDING
import timber.log.Timber
import java.util.*

object MessageComposer {

    fun composeMessage(
        content: String,
        conId: String,
        showType: Int,
        liveId: String? = null,
        isFromTip: Boolean = false,
        level: Int = -1,
    ): MessageEntity {
        Timber.d("content : $content")
        val payloadJson = JsonObject()
        payloadJson.addProperty("content", content)
        payloadJson.addProperty("live_id", liveId)
        payloadJson.addProperty("show_type", showType)
        payloadJson.addProperty("level", level)
        return MessageEntity(
            userId = SessionPreferences.id,
            conId = conId,
            uuid = UUID.randomUUID().toString(),
            senderId = SessionPreferences.id,
            type = if (isFromTip) 21 else 1,
            payload = Gson().toJson(payloadJson),
            content = content,
            sendTime = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) * 10000L,
            sendStatus = SEND_STATUS_SENDING,
            messageFailed = 0,
            extraInfo = "",
            msgDisappearTT = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 + 24 * 60 * 60,
            read = 1,
            userRole = 9,
        )
    }

    fun composePromptMessage(
        content: String,
        conId: String,
        showType: Int,
        liveId: String,
        uuid: String? = null,
        timeToken: Long = 0,
    ): MessageEntity {
        val payloadJson = JsonObject()
        payloadJson.addProperty("content", content)
        payloadJson.addProperty("live_id", liveId)
        payloadJson.addProperty("show_type", showType)
        return MessageEntity(
            userId = SessionPreferences.id,
            conId = conId,
            uuid = uuid ?: UUID.randomUUID().toString(),
            senderId = SessionPreferences.id,
            type = 8,
            payload = Gson().toJson(payloadJson),
            content = content,
            sendTime = if (timeToken == 0L) {
                (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) * 10000L
            } else timeToken,
            sendStatus = SEND_STATUS_SENDING,
            messageFailed = 0,
            extraInfo = "",
            msgDisappearTT = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 + 24 * 60 * 60,
            read = 1,
            userRole = 9,
        )
    }
}