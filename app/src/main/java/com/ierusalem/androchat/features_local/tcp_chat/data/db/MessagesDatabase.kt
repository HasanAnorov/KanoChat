package com.ierusalem.androchat.features_local.tcp_chat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ierusalem.androchat.features_local.tcp.data.db.converters.ChattingUserConverter
import com.ierusalem.androchat.features_local.tcp.data.db.dao.ChattingUsersDao
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp_chat.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_local.tcp_chat.data.db.entity.ChatMessageEntity

@Database(
    entities = [ChatMessageEntity::class, ChattingUserEntity::class],
    version = 4,
)
@TypeConverters(ChatMessageTypeConverter::class, ChattingUserConverter::class)
abstract class MessagesDatabase: RoomDatabase() {
    abstract val messagesDao: MessagesDao
    abstract val chattingUsersDao: ChattingUsersDao
}