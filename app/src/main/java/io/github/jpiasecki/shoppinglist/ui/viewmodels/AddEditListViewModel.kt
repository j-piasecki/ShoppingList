package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddEditListViewModel @ViewModelInject constructor(
    private val usersRepository: UsersRepository,
    private val shoppingListsRepository: ShoppingListsRepository
) : ViewModel() {

    fun getList(listId: String) = shoppingListsRepository.getList(listId)

    fun updateList(list: ShoppingList) {
        GlobalScope.launch(Dispatchers.IO) {
            shoppingListsRepository.changeListMetadataBlocking(list)
        }
    }

    fun createList(list: ShoppingList) = shoppingListsRepository.createList(list)
}