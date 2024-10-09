package com.ierusalem.androchat.core.updater

import retrofit2.Response

class UpdaterRepositoryImpl(
    private val updaterService: UpdaterService
) : UpdaterRepository {

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