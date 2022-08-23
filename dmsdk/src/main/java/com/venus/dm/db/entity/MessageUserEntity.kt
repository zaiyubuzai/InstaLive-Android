package com.venus.dm.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages_users",
    indices = [
        Index(value = ["user_id"], unique = true)
    ]
)
open class MessageUserEntity  {
    constructor(
        id: Int = 0,
        userId: String,
        name: String,
        portrait: String,
        username: String,
        bio: String? = null,
        relationship: Int = 1,
        portraitIc: String? = null,
    ){
        this.id = id
        this.userId = userId
        this.name = name
        this.portrait = portrait
        this.username = username
        this.bio = bio
        this.relationship = relationship
        this.portraitIc = portraitIc
    }

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @ColumnInfo(name = "user_id")
    var userId: String = ""
    @ColumnInfo(name = "name")
    var name: String = ""
    @ColumnInfo(name = "portrait")
    var portrait: String = ""
    @ColumnInfo(name = "user_name")
    var username: String = ""
    @ColumnInfo(name = "bio")
    var bio: String? = null
    @ColumnInfo(name = "relationship")
    var relationship: Int = 1
    @ColumnInfo(name = "portrait_icon")
    var portraitIc: String? = null
}