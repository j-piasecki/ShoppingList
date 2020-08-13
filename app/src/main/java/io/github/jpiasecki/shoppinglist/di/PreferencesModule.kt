package io.github.jpiasecki.shoppinglist.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.ListUsersTimers
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideConfig(
        @ApplicationContext app: Context
    ) = Config(app)

    @Provides
    @Singleton
    fun provideListUsersTimer(
        @ApplicationContext app: Context
    ) = ListUsersTimers(app)
}