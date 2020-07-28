package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository

class AddEditItemViewModel @ViewModelInject constructor(
    private val usersRepository: UsersRepository,
    private val shoppingListsRepository: ShoppingListsRepository
) : ViewModel() {

    fun getShoppingList(id: String) = shoppingListsRepository.getList(id)

    fun addItemToList(listId: String, item: Item) = shoppingListsRepository.addItemToList(listId, item)
}