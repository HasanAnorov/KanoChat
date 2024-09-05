package com.ierusalem.androchat.features_local.tcp.domain

import androidx.paging.PagingSource
import com.ierusalem.androchat.features_local.tcp.data.db.dao.ChattingUsersDao
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import com.ierusalem.androchat.features_local.tcp.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.UserWithLastMessage
import com.ierusalem.androchat.features_local.tcp.domain.state.FileMessageState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessagesRepositoryImpl @Inject constructor(
    private val messagesDao: MessagesDao,
    private val chattingUsersDao: ChattingUsersDao
) : MessagesRepository {

    override suspend fun updateVoiceFileMessage(
        messageId: Long,
        newFileState: FileMessageState,
        newDuration: Long?
    ) {
        return messagesDao.updateVoiceFileMessage(
            messageId = messageId,
            newFileState = newFileState,
            newDuration = newDuration
        )
    }

    override suspend fun updateFileMessage(messageId: Long, newFileState: FileMessageState) {
        messagesDao.updateFileMessage(
            messageId = messageId,
            newFileState = newFileState
        )
    }

    override fun getPagedUserMessagesById(userId: String): PagingSource<Int, ChatMessageEntity> {
        return messagesDao.getPagedUserMessagesById(userId)
    }

    override suspend fun insertChattingUser(chattingUserEntity: ChattingUserEntity): Long {
        return chattingUsersDao.insertChattingUser(chattingUserEntity)
    }

    override suspend fun updateChattingUserUniqueName(
        userUniqueId: String,
        userUniqueName: String
    ): Int {
        return chattingUsersDao.updateUserUniqueName(
            userId = userUniqueId,
            newName = userUniqueName
        )
    }

    override suspend fun updateAllUsersOnlineStatus(isOnline: Boolean): Int {
        return chattingUsersDao.updateAllUsersOnlineStatus(isOnline)
    }

    override suspend fun updateIsUserOnline(userUniqueId: String, isOnline: Boolean):Int {
        return chattingUsersDao.updateUserOnlineStatus(
            userId = userUniqueId,
            isOnline = isOnline
        )
    }

    override suspend fun isUserExist(userUniqueId: String): Boolean {
        return chattingUsersDao.isChattingUserExists(userUniqueId = userUniqueId)
    }

    override suspend fun insertMessage(message: ChatMessageEntity): Long {
        return messagesDao.insertMessage(message)
    }

    override fun getAllUsersWithLastMessages(): Flow<List<UserWithLastMessage>> {
        return messagesDao.getAllUsersWithLastMessage()
    }

}