package com.ierusalem.androchat.features_local.tcp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ierusalem.androchat.features_local.tcp.data.db.converters.ChatMessageTypeConverter
import com.ierusalem.androchat.features_local.tcp.data.db.converters.ChattingUserConverter
import com.ierusalem.androchat.features_local.tcp.data.db.dao.ChattingUsersDao
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity

@Database(
    entities = [ChatMessageEntity::class, ChattingUserEntity::class],
    version =9,
)
@TypeConverters(ChatMessageTypeConverter::class, ChattingUserConverter::class)
abstract class MessagesDatabase: RoomDatabase() {
    abstract val messagesDao: MessagesDao
    abstract val chattingUsersDao: ChattingUsersDao
}