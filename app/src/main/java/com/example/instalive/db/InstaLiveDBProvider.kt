package com.example.instalive.db

import androidx.room.Database
import androidx.room.Room
import com.example.instalive.InstaLiveApp
import com.venus.dm.db.ChatDatabase
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageUserEntity

object InstaLiveDBProvider {
    val db =
        Room.databaseBuilder(
            InstaLiveApp.appInstance,
            InstaLiveAppDatabase::class.java,
            "insta-live-app-db.db"
        ).build()
}

@Database(
    entities = arrayOf(
        MessageEntity::class,
        ConversationsEntity::class,
        MessageUserEntity::class,
    ), version = 1, exportSchema = false
)
abstract class InstaLiveAppDatabase : ChatDatabase() {
}