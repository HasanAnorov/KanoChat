package com.ierusalem.androchat.ui.navigation

import kotlinx.coroutines.flow.Flow

interface NavigationEventDelegate<T> {
    val screenNavigation: Flow<T>
    suspend fun sendEvent(navigation: T)
}