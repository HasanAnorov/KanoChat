package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.FileMessageState
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Query("SELECT EXISTS(SELECT * FROM messages WHERE userId = :userUniqueId)")
    fun isUserExist(userUniqueId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE messages SET fileState = :newFileState WHERE id = :messageId")
    suspend fun updateFileMessage(messageId: Long, newFileState: FileMessageState)

    @Query("SELECT * FROM messages where id = :messageId")
    fun getMessageById(messageId: Long): Flow<ChatMessageEntity>

    @Query("SELECT * FROM messages WHERE userId = :userId")
    fun getUserMessagesById(userId: String): Flow<List<ChatMessageEntity>>

}