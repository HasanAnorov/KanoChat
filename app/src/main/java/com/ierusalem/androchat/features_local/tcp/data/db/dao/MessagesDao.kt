package com.ierusalem.androchat.features_local.tcp.data.db.dao

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE messages SET fileState = 'failure' WHERE isFileAvailable = 0")
    suspend fun updateFileStateToFailure(): Int

    @Query(
    """
    SELECT chatting_users.authorSessionId,
           chatting_users.partnerSessionID, 
           chatting_users.partnerUsername, 
           chatting_users.avatarBackgroundColor, 
           chatting_users.isOnline,
           messages.*
    FROM chatting_users 
    LEFT JOIN messages ON chatting_users.partnerSessionID = messages.partnerSessionId 
                       AND chatting_users.authorSessionId = messages.authorSessionId
    WHERE chatting_users.authorSessionId = :authorSessionId 
    AND (messages.id IN (
        SELECT MAX(id) FROM messages WHERE authorSessionId = :authorSessionId GROUP BY partnerSessionID
    ) OR messages.id IS NULL)
    """
    )
    fun getAllUsersWithLastMessage(authorSessionId: String): Flow<List<UserWithLastMessage>>

    @Query("UPDATE messages SET isFileAvailable = :isFileAvailable, fileState = :newFileState WHERE id = :messageId")
    suspend fun updateFileMessage(
        messageId: Long,
        isFileAvailable: Boolean,
        newFileState: FileMessageState?
    )

    @Query("UPDATE messages SET isFileAvailable = :isFileAvailable, fileState = :newFileState, voiceMessageAudioFileDuration = :newDuration WHERE id = :messageId")
    suspend fun updateVoiceFileMessage(
        messageId: Long,
        isFileAvailable: Boolean,
        newFileState: FileMessageState?,
        newDuration: Long?
    )

    @Query("SELECT * FROM messages WHERE partnerSessionId = :peerSessionId AND authorSessionId = :authorSessionId ORDER BY id DESC LIMIT :limitPerPage OFFSET :offset ")
    fun getPagedUserMessagesById(peerSessionId: String, authorSessionId: String, offset: Int, limitPerPage:Int): Flow<List<ChatMessageEntity>>
}