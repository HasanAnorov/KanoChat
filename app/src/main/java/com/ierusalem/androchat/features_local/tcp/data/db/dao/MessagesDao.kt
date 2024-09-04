package com.ierusalem.androchat.features_local.tcp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.UserWithLastMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Query("SELECT EXISTS(SELECT * FROM messages WHERE peerUniqueId = :userUniqueId)")
    fun isUserExist(userUniqueId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query(
        """
        SELECT chatting_users.userUniqueId, 
               chatting_users.userUniqueName, 
               chatting_users.avatarBackgroundColor, -- Add the missing fields
               messages.*
        FROM chatting_users 
        LEFT JOIN messages ON chatting_users.userUniqueId = messages.peerUniqueId
        WHERE messages.id IN (
            SELECT MAX(id) FROM messages GROUP BY peerUniqueId
        ) OR messages.id IS NULL
    """
    )
    fun getAllUsersWithLastMessage(): Flow<List<UserWithLastMessage>>

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