package io.github.jpiasecki.shoppinglist.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Config
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.repositories.ShoppingListsRepository
import io.github.jpiasecki.shoppinglist.repositories.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel @ViewModelInject constructor(
    private val usersRepository: UsersRepository,
    private val shoppingListsRepository: ShoppingListsRepository,
    private val config: Config
) : ViewModel() {

    fun getShoppingList(id: String) = shoppingListsRepository.getList(id)

    fun getAllShoppingLists() = shoppingListsRepository.getAllLists()

    fun getAllUsers() = usersRepository.getAllUsers()

    fun setupUser() = usersRepository.setupUser()

    fun trySettingOwner() = shoppingListsRepository.trySettingOwner()

    fun deleteList(list: ShoppingList) {
        if (list.keepInSync) {
            GlobalScope.launch(Dispatchers.IO) {
                if (list.owner == FirebaseAuth.getInstance().currentUser?.uid)
                    shoppingListsRepository.deleteOrReleaseListBlocking(list.id)
                else
                    shoppingListsRepository.removeUserFromList(list.id, FirebaseAuth.getInstance().currentUser?.uid ?: Values.USER_ID_NOT_FOUND)

                usersRepository.removeListFromUser(list.id)
            }
        }

        shoppingListsRepository.deleteLocalList(list.id)
    }

    fun uploadList(list: ShoppingList) {
        if (!list.keepInSync) {
            shoppingListsRepository.uploadList(list.id)
        }
    }

    fun updateItems(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val syncResult = shoppingListsRepository.syncListBlocking(listId)

            if (syncResult) {
                val list = shoppingListsRepository.getListPlain(listId)

                if (list != null) {
                    for (userId in list.getAllUsersNoOwner()) {
                        usersRepository.updateUser(userId)
                    }
                }

                result.postValue(true)
            } else {
                result.postValue(false)
            }

            shoppingListsRepository.deleteUnusedItemsBlocking(listId)
        }

        return result
    }

    fun syncAllLists(): LiveData<Boolean?> {
        config.updateListsManualUpdateTimestamp()
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val syncResult = shoppingListsRepository.syncAllListsBlocking()
            val updated = ArrayList<String>()

            for (list in shoppingListsRepository.getAllListsPlain()) {
                list.owner?.let {
                    if (it !in updated) {
                        usersRepository.updateUser(it)
                        updated.add(it)
                    }
                }

                for (user in list.getAllUsersNoOwner()) {
                    if (user !in updated) {
                        usersRepository.updateUser(user)
                        updated.add(user)
                    }
                }

                if (list.keepInSync) {
                    shoppingListsRepository.deleteUnusedItemsBlocking(list.id)
                }
            }

            result.postValue(syncResult)
        }

        return result
    }

    fun canSyncListsManually(): Boolean {
        return Calendar.getInstance().timeInMillis - config.getListsManualUpdateTimestamp() >= Values.LISTS_MANUAL_UPDATE_PERIOD
    }

    fun setItemCompleted(listId: String, itemId: String, completed: Boolean): LiveData<Boolean?> {
        return if (completed)
            shoppingListsRepository.setItemCompleted(listId, itemId)
        else
            shoppingListsRepository.setItemNotCompleted(listId, itemId)
    }

    fun downloadList(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsRepository.downloadListBlocking(listId)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: Values.USER_ID_NOT_FOUND

            if (list == null) {
                result.postValue(false)
            } else if (!usersRepository.addListToUser(listId)) {
                result.postValue(false)
            } else if (list.owner != uid && !shoppingListsRepository.addUserToListBlocking(listId, uid)) {
                result.postValue(false)
            } else {
                result.postValue(true)
            }
        }

        return result
    }

    fun downloadRemoteLists() {
        GlobalScope.launch(Dispatchers.IO) {
            val remoteLists = usersRepository.getRemoteLists()

            for (listId in remoteLists) {
                if (!shoppingListsRepository.downloadOrSyncListBlocking(listId)) {
                    usersRepository.removeListFromUser(listId)
                } else {
                    usersRepository.addListToUser(listId)
                }
            }


            val updated = ArrayList<String>()

            for (list in shoppingListsRepository.getAllListsPlain()) {
                list.owner?.let {
                    if (it !in updated) {
                        usersRepository.updateUser(it)
                        updated.add(it)
                    }
                }

                for (user in list.getAllUsersNoOwner()) {
                    if (user !in updated) {
                        usersRepository.updateUser(user)
                        updated.add(user)
                    }
                }
            }
        }
    }
}