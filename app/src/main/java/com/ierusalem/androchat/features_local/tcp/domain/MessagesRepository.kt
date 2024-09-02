package com.ierusalem.androchat.features_local.tcp.domain

import androidx.paging.PagingSource
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {

    suspend fun updateVoiceFileMessage(
        messageId: Long,
        newFileState: FileMessageState,
        newDuration: Long?
    )

    suspend fun updateFileMessage(messageId: Long, newFileState: FileMessageState)

    fun getPagedUserMessagesById(userId: String): PagingSource<Int, ChatMessageEntity>

    suspend fun insertChattingUser(chattingUserEntity: ChattingUserEntity):Long

    suspend fun insertMessage(message: ChatMessageEntity): Long

    fun getChattingUsers(): Flow<List<ChattingUserEntity>>

    fun getAllUsersLastMessages(): Flow<List<ChatMessageEntity?>>

}