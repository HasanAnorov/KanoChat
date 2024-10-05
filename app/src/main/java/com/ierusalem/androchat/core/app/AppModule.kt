package com.ierusalem.androchat.core.app

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import androidx.room.Room
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.connectivity.NetworkConnectivityObserver
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.directory_router.FilesDirectoryImpl
import com.ierusalem.androchat.core.directory_router.FilesDirectoryService
import com.ierusalem.androchat.core.utils.Constants
import com.ierusalem.androchat.core.utils.FieldValidator
import com.ierusalem.androchat.core.voice_message.playback.AndroidAudioPlayer
import com.ierusalem.androchat.core.voice_message.recorder.AndroidAudioRecorder
import com.ierusalem.androchat.features_local.tcp.data.MessagesDatabase
import com.ierusalem.androchat.features_local.tcp.data.db.dao.ChattingUsersDao
import com.ierusalem.androchat.features_local.tcp.data.db.dao.MessagesDao
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuard
import com.ierusalem.androchat.features_local.tcp.data.server.permission.PermissionGuardImpl
import com.ierusalem.androchat.features_local.tcp.domain.MessagesRepository
import com.ierusalem.androchat.features_local.tcp.domain.MessagesRepositoryImpl
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
    fun provideMessagesRepository(
        messagesDao: MessagesDao,
        chattingUsersDao: ChattingUsersDao
        ): MessagesRepository {
        return MessagesRepositoryImpl(messagesDao, chattingUsersDao)
    }

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

    @Provides
    @Singleton
    fun provideWifiP2PManager(application: Application): WifiP2pManager{
        return application.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    @Provides
    @Singleton
    fun provideChannel(application: Application, wifiP2pManager: WifiP2pManager): WifiP2pManager.Channel{
        return wifiP2pManager.initialize(application, application.mainLooper){
            println("WifiP2PManager Channel died! Do nothing :D")
        }
    }

    @Provides
    @Singleton
    fun provideFileDirectoryService(application: Application): FilesDirectoryService {
        return FilesDirectoryImpl(application)
    }

}