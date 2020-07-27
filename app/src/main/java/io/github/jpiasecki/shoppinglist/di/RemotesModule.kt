package io.github.jpiasecki.shoppinglist.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jpiasecki.shoppinglist.remote.ShoppingListsRemoteSource
import io.github.jpiasecki.shoppinglist.remote.UsersRemoteSource
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object RemotesModule {

    @Provides
    @Singleton
    fun provideUsersRemoteSource(
        @ApplicationContext app: Context
    ) = UsersRemoteSource(app)

    @Provides
    @Singleton
    fun provideShoppingListsRemoteSource(
        @ApplicationContext app: Context
    ) = ShoppingListsRemoteSource(app)
}