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
        isFileAvailable:Boolean,
        newDuration: Long?
    )
    fun getChattingUserByIdFlow(userUniqueId: String): Flow<ChattingUserEntity?>
    suspend fun updateFileMessage(messageId: Long, newFileState: FileMessageState?, isFileAvailable:Boolean)
    fun getPagedUserMessagesById(partnerSessionId: String, authorSessionId: String): PagingSource<Int, ChatMessageEntity>
    suspend fun insertChattingUser(chattingUserEntity: ChattingUserEntity): Long
    suspend fun updateAllUsersOnlineStatus(isOnline: Boolean):Int
    suspend fun updateIsUserOnline(userUniqueId: String, isOnline: Boolean):Int
    suspend fun insertMessage(message: ChatMessageEntity): Long
    suspend fun isUserExist(partnerSessionId: String, authorSessionId:String): Boolean
    fun getAllUsersWithLastMessages(authorSessionId: String): Flow<List<UserWithLastMessage>>
    suspend fun updateFileStateToFailure():Int
}