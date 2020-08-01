package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListUsersViewModel @ViewModelInject constructor(
    private val shoppingListsRepository: ShoppingListsRepository,
    private val usersRepository: UsersRepository,
    private val config: Config
) : ViewModel() {

    fun getAllUsers() = usersRepository.getAllUsers()

    fun getShoppingList(listId: String) = shoppingListsRepository.getList(listId)

    fun changeOwner(listId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            shoppingListsRepository.removeUserFromListBlocking(listId, userId)

            shoppingListsRepository.changeListOwner(listId, userId)

            FirebaseAuth.getInstance().currentUser?.let {
                shoppingListsRepository.addUserToList(listId, it.uid)
            }
        }
    }

    fun banUser(listId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            shoppingListsRepository.banUserFromListBlocking(listId, userId)
            shoppingListsRepository.removeUserFromListBlocking(listId, userId)
        }
    }

    fun unBanUser(listId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            shoppingListsRepository.unBanUserFromListBlocking(listId, userId)
            shoppingListsRepository.addUserToList(listId, userId)
        }
    }
}