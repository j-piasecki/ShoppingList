package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import kotlin.random.Random

class MainViewModel @ViewModelInject constructor(
    private val usersRepository: UsersRepository,
    private val shoppingListsRepository: ShoppingListsRepository
) : ViewModel() {

    fun getShoppingList(id: String) = shoppingListsRepository.getList(id)

    fun getAllShoppingLists() = shoppingListsRepository.getAllLists()

    fun getAllUsers() = usersRepository.getAllUsers()

    fun setupUser() = usersRepository.setupUser()

    fun createList() = shoppingListsRepository.createList("list name ${Random.nextInt(100)}", "list note number ${Random.nextInt(1000)}", "pln")

    fun deleteList(list: ShoppingList) {
        if (list.keepInSync)
            shoppingListsRepository.deleteRemoteList(list.id)

        shoppingListsRepository.deleteLocalList(list.id)
    }

    fun uploadList(list: ShoppingList) {
        if (!list.keepInSync) {
            shoppingListsRepository.uploadList(list.id)
        }
    }

    fun updateItems(listId: String) = shoppingListsRepository.syncList(listId)

    fun downloadList(listId: String): LiveData<Boolean?> {
        val result = shoppingListsRepository.downloadList(listId)

        GlobalScope.launch(Dispatchers.IO) {
            usersRepository.addListToUser(listId)
        }

        return result
    }
}