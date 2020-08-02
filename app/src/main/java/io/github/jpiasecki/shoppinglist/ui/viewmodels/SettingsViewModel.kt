package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository

class SettingsViewModel @ViewModelInject constructor(
    private val usersRepository: UsersRepository,
    private val config: Config
) : ViewModel() {

    fun isDarkThemeEnabled() = config.isDarkThemeEnabled()

    fun setDarkThemeEnabled(value: Boolean) = config.setDarkThemeEnabled(value)
}