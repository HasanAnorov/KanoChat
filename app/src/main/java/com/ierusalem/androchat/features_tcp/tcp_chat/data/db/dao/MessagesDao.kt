package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Query("SELECT EXISTS(SELECT * FROM messages WHERE userId = :userUniqueId)")
    fun isUserExist(userUniqueId: String): Boolean

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM messages WHERE userId = :userId")
    fun getUserMessagesById(userId: String): Flow<List<ChatMessageEntity>>

}