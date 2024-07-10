package com.ierusalem.androchat.core.app

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.wifi.WifiManager
import com.ierusalem.androchat.core.connectivity.ConnectivityObserver
import com.ierusalem.androchat.core.connectivity.NetworkConnectivityObserver
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.utils.FieldValidator
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuard
import com.ierusalem.androchat.features_tcp.server.permission.PermissionGuardImpl
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