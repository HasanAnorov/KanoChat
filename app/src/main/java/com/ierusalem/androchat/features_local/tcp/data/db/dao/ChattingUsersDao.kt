package com.ierusalem.androchat.features_local.tcp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ierusalem.androchat.features_local.tcp.data.db.entity.ChattingUserEntity

@Dao
interface ChattingUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChattingUser(chattingUserEntity: ChattingUserEntity):Long

    @Query("UPDATE chatting_users SET userUniqueName = :newName WHERE userUniqueId = :userId")
    suspend fun updateUserUniqueName(userId: String, newName: String): Int

    @Query("UPDATE chatting_users SET isOnline = :isOnline WHERE userUniqueId = :userId")
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Int

    @Query("UPDATE chatting_users SET isOnline = :isOnline")
    suspend fun updateAllUsersOnlineStatus(isOnline: Boolean)

    @Query("DELETE FROM chatting_users WHERE userUniqueId = :userUniqueId")
    suspend fun deleteChattingUser(userUniqueId: String)

    @Query("DELETE FROM chatting_users")
    suspend fun deleteAllChattingUsers()

    @Query("SELECT EXISTS(SELECT * FROM chatting_users WHERE userUniqueId = :userUniqueId)")
    suspend fun isChattingUserExists(userUniqueId: String): Boolean

}

