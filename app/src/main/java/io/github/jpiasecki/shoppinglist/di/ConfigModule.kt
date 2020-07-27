package io.github.jpiasecki.shoppinglist.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jpiasecki.shoppinglist.database.Config
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideConfig(
        @ApplicationContext app: Context
    ) = Config(app)
}