package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val config: Config
) : ViewModel() {

}