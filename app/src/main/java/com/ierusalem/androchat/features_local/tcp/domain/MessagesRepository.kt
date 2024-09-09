package com.ierusalem.androchat.features_local.tcp.domain

import androidx.paging.PagingSource
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.UserWithLastMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {
    suspend fun updateVoiceFileMessage(
        messageId: Long,
        newFileState: FileMessageState?,
        newDuration: Long?
    )
    fun getChattingUserByIdFlow(userUniqueId: String): Flow<ChattingUserEntity?>
    suspend fun updateFileMessage(messageId: Long, newFileState: FileMessageState?)
    fun getPagedUserMessagesById(userId: String): PagingSource<Int, ChatMessageEntity>
    suspend fun insertChattingUser(chattingUserEntity: ChattingUserEntity): Long
    suspend fun updateAllUsersOnlineStatus(isOnline: Boolean):Int
    suspend fun updateChattingUserUniqueName(userUniqueId: String, userUniqueName: String):Int
    suspend fun updateIsUserOnline(userUniqueId: String, isOnline: Boolean):Int
    suspend fun insertMessage(message: ChatMessageEntity): Long
    suspend fun isUserExist(userUniqueId: String): Boolean
    fun getAllUsersWithLastMessages(): Flow<List<UserWithLastMessage>>

}