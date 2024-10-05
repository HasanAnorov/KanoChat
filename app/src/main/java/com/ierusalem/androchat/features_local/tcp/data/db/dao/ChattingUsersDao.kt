package com.ierusalem.androchat.features_local.tcp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChattingUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChattingUser(chattingUserEntity: ChattingUserEntity):Long

    @Query("UPDATE chatting_users SET partnerUsername = :newName WHERE partnerSessionID = :userId")
    suspend fun updateUserUniqueName(userId: String, newName: String): Int

    @Query("UPDATE chatting_users SET isOnline = :isOnline WHERE partnerSessionID = :userId")
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Int

    @Query("UPDATE chatting_users SET isOnline = :isOnline")
    suspend fun updateAllUsersOnlineStatus(isOnline: Boolean): Int

    @Query("DELETE FROM chatting_users WHERE partnerSessionID = :userUniqueId")
    suspend fun deleteChattingUser(userUniqueId: String)

    @Query("DELETE FROM chatting_users")
    suspend fun deleteAllChattingUsers()

    @Query("SELECT EXISTS(SELECT * FROM chatting_users WHERE partnerSessionID = :partnerSessionID AND authorSessionId = :authorSessionId)")
    suspend fun isChattingUserExists(partnerSessionID: String, authorSessionId: String): Boolean

    @Query("SELECT * FROM chatting_users WHERE partnerSessionID = :userUniqueId LIMIT 1")
    fun getChattingUserByIdFlow(userUniqueId: String): Flow<ChattingUserEntity?>

}

