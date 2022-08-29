package com.example.instalive.db

import android.graphics.BitmapFactory
import com.example.baselibrary.utils.ZipUtils
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
            senderName = SessionPreferences.nickName?:"",
            senderUsername = SessionPreferences.userName?:"",
            senderPortrait = SessionPreferences.portrait?:"",
            portraitIc = SessionPreferences.portraitIc?:"",
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
            state = 1,
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
            senderName = SessionPreferences.nickName?:"",
            senderUsername = SessionPreferences.userName?:"",
            senderPortrait = SessionPreferences.portrait?:"",
            portraitIc = SessionPreferences.portraitIc?:"",
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

    fun composeImageMessage(
        localPath: String,
        conId: String,
        showType: Int,
        liveId: String? = null,
        level: Int = -1
    ): MessageEntity {
        var width = 0
        var height = 0
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(localPath, options)

            if (ZipUtils.pictureDegree(localPath) > 0) {
                width = options.outHeight
                height = options.outWidth
            } else {
                width = options.outWidth
                height = options.outHeight
            }

        } catch (e: Exception) {
        }
        return MessageEntity(
            userId = SessionPreferences.id,
            conId = conId,
            uuid = UUID.randomUUID().toString(),
            senderId = SessionPreferences.id,
            senderName = SessionPreferences.nickName?:"",
            senderUsername = SessionPreferences.userName?:"",
            senderPortrait = SessionPreferences.portrait?:"",
            portraitIc = SessionPreferences.portraitIc?:"",
            type = 3,
            payload = MessageEntity.Payload(
                width = width,
                height = height,
                showType = showType,
                liveId = liveId,
                level = level
            ).toString(),
            content = "",
            sendTime = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) * 10000L,
            sendStatus = SEND_STATUS_SENDING,
            messageFailed = 0,
            extraInfo = "",
            localResPath = localPath,
            localThumbnail = null,
            msgDisappearTT = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 + 24 * 60 * 60,
            read = 1,
            userRole = 0,
            liveId = liveId,
            showType = showType
        )
    }

    fun composeVideoMessage(
        localPath: String,
        localThumbnail: String,
        conId: String,
        width: String,
        height: String,
        length: String,
        size: String,
        showType: Int,
        liveId: String? = null,
        level: Int = -1,
    ): MessageEntity = MessageEntity(
        userId = SessionPreferences.id,
        conId = conId,
        uuid = UUID.randomUUID().toString(),
        senderId = SessionPreferences.id,
        senderName = SessionPreferences.nickName?:"",
        senderUsername = SessionPreferences.userName?:"",
        senderPortrait = SessionPreferences.portrait?:"",
        portraitIc = SessionPreferences.portraitIc?:"",
        type = 4,
        payload = MessageEntity.Payload(
            width = width.toInt(),
            height = height.toInt(),
            length = length.toInt(),
            size = size.toLong(),
            showType = showType,
            liveId = liveId,
            level = level
        )
            .toString(),//"{width:\"$width\", height:\"$height\", length:\"$length\", size:\"$size\", show_type:\"$showType\"}",
        content = "",
        sendTime = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) * 10000L,
        sendStatus = SEND_STATUS_SENDING,
        messageFailed = 0,
        extraInfo = "",
        localResPath = localPath,
        localThumbnail = localThumbnail,
        msgDisappearTT = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 + 24 * 60 * 60,
        read = 1,
        userRole = 0,
        liveId = liveId,
        showType = showType
    )

    fun composeTargetMessage(
        content: String,
        conId: String,
        showType: Int,
        targetMessage: MessageEntity.TargetMessage,
        liveId: String? = null,
        isFromTip: Boolean = false,
        level: Int = -1,
    ): MessageEntity {
        val tar = Gson().toJsonTree(targetMessage)
        val payloadJson = JsonObject()
        payloadJson.addProperty("content", content)
        payloadJson.add("target_msg", tar)
        payloadJson.addProperty("show_type", showType)
        payloadJson.addProperty("level", level)
        return MessageEntity(
            userId = SessionPreferences.id,
            conId = conId,
            uuid = UUID.randomUUID().toString(),
            senderId = SessionPreferences.id,
            senderName = SessionPreferences.nickName?:"",
            senderUsername = SessionPreferences.userName?:"",
            senderPortrait = SessionPreferences.portrait?:"",
            portraitIc = SessionPreferences.portraitIc?:"",
            type = 32,
            payload = Gson().toJson(payloadJson),
            content = content,
            sendTime = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) * 10000L,
            sendStatus = SEND_STATUS_SENDING,
            messageFailed = 0,
            extraInfo = "",
            localResPath = null,
            localThumbnail = null,
            msgDisappearTT = (System.currentTimeMillis() - InstaLiveApp.appInstance.timeDiscrepancy) / 1000 + 24 * 60 * 60,
            read = 1,
            userRole = 0,
            liveId = liveId,
            showType = showType
        )
    }

}