package com.ierusalem.androchat.features_tcp.tcp_chat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.UserMessages

@Database(
    entities = [UserMessages::class],
    version = 1
)
@TypeConverters(ChatMessageTypeConverter::class)
abstract class MessagesDatabase: RoomDatabase() {
    abstract val dao: MessagesDao
}