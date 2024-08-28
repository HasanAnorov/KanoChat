package com.ierusalem.androchat.core.app

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.wifi.WifiManager
import androidx.room.Room
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.connectivity.NetworkConnectivityObserver
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.utils.FieldValidator
import com.ierusalem.androchat.core.voice_message.playback.AndroidAudioPlayer
import com.ierusalem.androchat.core.voice_message.recorder.AndroidAudioRecorder
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuard
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_local.tcp.data.db.dao.ChattingUsersDao
import com.ierusalem.androchat.features_local.tcp_chat.data.db.MessagesDatabase
import com.ierusalem.androchat.features_local.tcp_chat.data.db.dao.MessagesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMessageDatabase(application: Application): MessagesDatabase {
        return Room.databaseBuilder(
            application,
            MessagesDatabase::class.java,
            Constants.MESSAGES_DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(application: Application): AndroidAudioPlayer {
        return AndroidAudioPlayer(application)
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(application: Application): AndroidAudioRecorder {
        return AndroidAudioRecorder(application)
    }

    @Provides
    @Singleton
    fun provideMessagesDao(messagesDatabase: MessagesDatabase) : MessagesDao {
        return messagesDatabase.messagesDao
    }

    @Provides
    @Singleton
    fun provideChattingUsersDao(messagesDatabase: MessagesDatabase) : ChattingUsersDao {
        return messagesDatabase.chattingUsersDao
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(application: Application): ConnectivityObserver {
        return NetworkConnectivityObserver(context = application)
    }

    @Provides
    @Singleton
    fun providePermissionGuard(application: Application): PermissionGuard {
        return PermissionGuardImpl(application)
    }

    @Provides
    @Singleton
    fun provideContentResolver(application: Application):ContentResolver{
        return application.contentResolver
    }

    @Provides
    @Singleton
    fun provideDataStore(application: Application): DataStorePreferenceRepository {
        return DataStorePreferenceRepository(application)
    }

    @Provides
    @Singleton
    fun provideWifiManager(application: Application): WifiManager {
        return application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @Provides
    @Singleton
    fun provideFieldValidator(): FieldValidator = FieldValidator()

}