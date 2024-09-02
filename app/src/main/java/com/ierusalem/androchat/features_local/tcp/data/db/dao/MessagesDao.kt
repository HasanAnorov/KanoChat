package com.ierusalem.androchat.features_local.tcp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Query("SELECT EXISTS(SELECT * FROM messages WHERE peerUniqueId = :userUniqueId)")
    fun isUserExist(userUniqueId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM messages WHERE peerUniqueId = :userId ORDER BY id DESC LIMIT 1")
    fun getLastUserMessage(userId: String): Flow<ChatMessageEntity?>

    @Query(
        """
    SELECT * FROM messages
    WHERE id IN (
        SELECT MAX(id) FROM messages
        GROUP BY peerUniqueId
    )
    ORDER BY id DESC
"""
    )
    fun getAllUsersLastMessages(): Flow<List<ChatMessageEntity?>>

    @Query("UPDATE messages SET fileState = :newFileState WHERE id = :messageId")
    suspend fun updateFileMessage(messageId: Long, newFileState: FileMessageState)

    @Query("UPDATE messages SET fileState = :newFileState, voiceMessageAudioFileDuration = :newDuration WHERE id = :messageId")
    suspend fun updateVoiceFileMessage(
        messageId: Long,
        newFileState: FileMessageState,
        newDuration: Long?
    )

    @Query("SELECT * FROM messages where id = :messageId")
    fun getMessageById(messageId: Long): Flow<ChatMessageEntity>

    @Query("SELECT * FROM messages WHERE peerUniqueId = :userId")
    fun getUserMessagesById(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM messages WHERE peerUniqueId = :userId")
    fun getPagedUserMessagesById(userId: String): PagingSource<Int, ChatMessageEntity>

}