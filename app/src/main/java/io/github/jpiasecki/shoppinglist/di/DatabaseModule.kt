package io.github.jpiasecki.shoppinglist.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jpiasecki.shoppinglist.database.Database
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "database"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(app, Database::class.java, DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideUsersDao(db: Database) = db.usersDao()

    @Provides
    @Singleton
    fun provideShoppingListsDao(db: Database) = db.shoppingListsDao()

    @Provides
    @Singleton
    fun provideItemsDao(db: Database) = db.itemsDao()
}