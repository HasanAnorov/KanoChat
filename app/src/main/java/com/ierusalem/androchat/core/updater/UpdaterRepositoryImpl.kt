package com.ierusalem.androchat.core.updater

import com.ierusalem.androchat.features_local.tcp.data.db.dao.MessagesDao
import retrofit2.Response
import javax.inject.Inject

class UpdaterRepositoryImpl @Inject constructor(
    private val messagesDao: MessagesDao,
    private val updaterService: UpdaterService
) : UpdaterRepository {

    override suspend fun getUnUpdatedMessagesCount(): Int {
        return messagesDao.getUnUpdatedMessagesCount()
    }

    override suspend fun postTextMessage(
        accessToken: String,
        textMessageBody: TextMessageBody
    ): Response<Unit> {
        return updaterService.postTextMessage(
            accessToken = accessToken,
            body = textMessageBody
        )
    }

}