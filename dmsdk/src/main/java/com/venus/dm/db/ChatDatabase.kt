package com.venus.dm.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.venus.dm.db.dao.DirectMessageDao
import com.venus.dm.db.entity.ConversationsEntity
import com.venus.dm.db.entity.MessageEntity
import com.venus.dm.db.entity.MessageUserEntity

//@Database(
//    entities = arrayOf(
//        MessageEntity::class,
//        ConversationsEntity::class,
//        MessageUserEntity::class,
//    ), version = 1, exportSchema = false
//)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun directMessagingDao(): DirectMessageDao
}