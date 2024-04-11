package com.ierusalem.androchat.features.home.di

import com.ierusalem.androchat.features.home.data.HomeRepositoryImpl
import com.ierusalem.androchat.features.home.domain.HomeRepository
import com.ierusalem.androchat.utils.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideRepository(preferenceHelper: PreferenceHelper): HomeRepository{
        return HomeRepositoryImpl(preferenceHelper)
    }

}