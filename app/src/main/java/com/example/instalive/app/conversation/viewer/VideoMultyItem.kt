package com.example.instalive.app.conversation.viewer

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.Gson

data class VideoMultyItem(
    val uuid: String,
    val url: String,
    val type: Int,
    val localRes:String,
    val coverUrl: String,
) : MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(json: String): VideoMultyItem {
            return Gson().fromJson(json, VideoMultyItem::class.java)
        }
    }
}