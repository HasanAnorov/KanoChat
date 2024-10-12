package com.ierusalem.androchat.core.updater

import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.features_local.tcp.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChatMessageEntity
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import kotlinx.coroutines.flow.first
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class UpdaterRepositoryImpl @Inject constructor(
    private val messagesDao: MessagesDao,
    private val updaterService: UpdaterService,
    private val dataStorePreferenceRepository: DataStorePreferenceRepository,
) : UpdaterRepository {

    override suspend fun getUnSentMessages(): List<ChatMessageEntity> {
        return messagesDao.getUnSentMessages()
    }

    override suspend fun getUnUpdatedMessagesCount(): Int {
        return messagesDao.getUnUpdatedMessagesCount()
    }

    override suspend fun postTextMessage(textMessageBody: TextMessageBody): Response<Unit> {
        return updaterService.postTextMessage(textMessageBody = textMessageBody)
    }

    override suspend fun postContactMessage(contactMessageBody: ContactMessageBody): Response<Unit> {
        return updaterService.postContactMessage(contactMessageBody = contactMessageBody)
    }

    override suspend fun postFileMessage(body: RequestBody): Response<Unit> {
        return updaterService.postFileMessage(body = body)
    }

    override suspend fun markMessageAsUpdated(messageId: Long) {
        messagesDao.markMessageAsUpdated(messageId)
    }

    override suspend fun postUsers(users: Users): Response<Unit> {
        return updaterService.postUsers(userBody = users)
    }

    override suspend fun getUnUpdatedChattingUsersCount(): Int {
        return messagesDao.getUnUpdatedChattingUsersCount()
    }

    override suspend fun getUnSentChattingUsers(): List<ChattingUserEntity> {
        return messagesDao.getUnsentUsers()
    }

    override suspend fun markUserAsUpdated(partnerSessionId: String) {
        messagesDao.markUserAsUpdated(partnerSessionId)
    }

    override suspend fun getDeviceInfoStatus(): Boolean {
        return dataStorePreferenceRepository.getDeviceInfoStatus.first()
    }

    override suspend fun postDeviceInfo(deviceInfo: DeviceInfo): Response<Unit> {
        return updaterService.postDeviceInfo(deviceInfo = deviceInfo)
    }

    override suspend fun markDeviceInfoAsSent() {
        dataStorePreferenceRepository.setDeviceInfoStatus(true)
    }

}