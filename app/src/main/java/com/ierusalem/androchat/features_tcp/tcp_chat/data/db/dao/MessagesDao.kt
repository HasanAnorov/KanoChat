package com.ierusalem.androchat.features_tcp.tcp_chat.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.ierusalem.androchat.features_tcp.tcp_chat.data.db.entity.UserMessages

@Dao
interface MessagesDao {

    @Upsert
    suspend fun createNewChatHistory(userMessages: UserMessages)

    @Delete
    suspend fun deleteUserHistory(userMessages: UserMessages)

    @Update
    suspend fun updateUserHistory(userMessages: UserMessages)

    @Query("SELECT * FROM user_messages WHERE userUniqueId = :userUniqueId ")
    suspend fun getUserHistory(userUniqueId: String): UserMessages

    @Query("SELECT EXISTS(SELECT * FROM user_messages WHERE userUniqueId = :userUniqueId)")
    fun isUserExist(userUniqueId: String): Boolean

}