package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import javax.inject.Inject

class ListUsersViewModel @ViewModelInject constructor(
    private val shoppingListsRepository: ShoppingListsRepository,
    private val usersRepository: UsersRepository,
    private val config: Config
) : ViewModel() {

    fun getAllUsers() = usersRepository.getAllUsers()

    fun getShoppingList(listId: String) = shoppingListsRepository.getList(listId)
}