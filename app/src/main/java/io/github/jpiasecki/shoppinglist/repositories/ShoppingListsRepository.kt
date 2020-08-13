package io.github.jpiasecki.shoppinglist.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import io.github.jpiasecki.shoppinglist.consts.Values
import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ShoppingList
import io.github.jpiasecki.shoppinglist.database.ShoppingListsDao
import io.github.jpiasecki.shoppinglist.remote.ShoppingListsRemoteSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ShoppingListsRepository @Inject constructor(
    private val shoppingListsRemoteSource: ShoppingListsRemoteSource,
    private val shoppingListsDao: ShoppingListsDao
) {
    private val allShoppingLists = shoppingListsDao.getAll()

    fun getAllLists() = allShoppingLists

    fun getAllListsPlain() = shoppingListsDao.getAllPlain()

    fun getList(listId: String) = shoppingListsDao.getById(listId)

    fun getAllIds() = shoppingListsDao.getAllIds()

    fun createList(list: ShoppingList): LiveData<String?> {
        val result = MutableLiveData<String?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            shoppingListsDao.insert(list)

            result.postValue(list.id)
        }

        return result
    }

    fun uploadList(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null && !list.keepInSync) {
                if (list.owner == null) {
                    list.owner = FirebaseAuth.getInstance().currentUser?.uid
                    shoppingListsDao.changeOwner(listId, list.owner)
                }

                if (shoppingListsRemoteSource.setList(list)) {
                    shoppingListsDao.setKeepSynced(listId)
                    result.postValue(true)
                } else {
                    result.postValue(false)
                }
            }
        }

        return result
    }

    fun deleteLocalList(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            shoppingListsDao.delete(listId)

            result.postValue(true)
        }

        return result
    }

    fun deleteRemoteList(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(shoppingListsRemoteSource.deleteList(listId))
        }

        return result
    }

    fun downloadList(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(downloadListBlocking(listId) != null)
        }

        return result
    }

    fun changeListOwner(listId: String, ownerId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            if (shoppingListsRemoteSource.changeListOwner(listId, ownerId)) {
                shoppingListsDao.updateTimestamp(listId)
                shoppingListsDao.changeOwner(listId, ownerId)
                result.postValue(true)
            } else {
                result.postValue(false)
            }
        }

        return result
    }

    fun changeListName(listId: String, name: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                if (list.keepInSync) {
                    if (shoppingListsRemoteSource.changeListName(listId, name)) {
                        shoppingListsDao.updateTimestamp(listId)
                        shoppingListsDao.rename(listId, name)
                        result.postValue(true)
                    } else {
                        result.postValue(false)
                    }
                } else {
                    shoppingListsDao.updateTimestamp(listId)
                    shoppingListsDao.rename(listId, name)
                    result.postValue(true)
                }
            }
        }

        return result
    }

    fun changeListNote(listId: String, note: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                if (list.keepInSync) {
                    if (shoppingListsRemoteSource.changeListNote(listId, note)) {
                        shoppingListsDao.updateTimestamp(listId)
                        shoppingListsDao.changeNote(listId, note)
                        result.postValue(true)
                    } else {
                        result.postValue(false)
                    }
                } else {
                    shoppingListsDao.updateTimestamp(listId)
                    shoppingListsDao.changeNote(listId, note)
                    result.postValue(true)
                }
            }
        }

        return result
    }

    fun changeListIcon(listId: String, icon: Int): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                if (list.keepInSync) {
                    if (shoppingListsRemoteSource.changeListIcon(listId, icon)) {
                        shoppingListsDao.updateTimestamp(listId)
                        shoppingListsDao.changeIcon(listId, icon)
                        result.postValue(true)
                    } else {
                        result.postValue(false)
                    }
                } else {
                    shoppingListsDao.updateTimestamp(listId)
                    shoppingListsDao.changeIcon(listId, icon)
                    result.postValue(true)
                }
            }
        }

        return result
    }

    suspend fun addUserToListBlocking(listId: String, userId: String): Boolean {
        if (shoppingListsRemoteSource.addUserToList(listId, userId)) {
            shoppingListsDao.getByIdPlain(listId)?.apply {
                users.add(userId)
                timestamp = Calendar.getInstance().timeInMillis

                shoppingListsDao.insert(this)
            }

            return true
        }

        return false
    }

    fun addUserToList(listId: String, userId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(addUserToListBlocking(listId, userId))
        }

        return result
    }

    suspend fun removeUserFromListBlocking(listId: String, userId: String): Boolean {
        if (shoppingListsRemoteSource.removeUserFromList(listId, userId)) {
            shoppingListsDao.getByIdPlain(listId)?.apply {
                users.remove(userId)
                timestamp = Calendar.getInstance().timeInMillis

                shoppingListsDao.insert(this)
            }

            return true
        }

        return false
    }

    fun removeUserFromList(listId: String, userId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(removeUserFromListBlocking(listId, userId))
        }

        return result
    }

    suspend fun banUserFromListBlocking(listId: String, userId: String): Boolean {
        if (shoppingListsRemoteSource.banUserFromList(listId, userId)) {
            shoppingListsDao.getByIdPlain(listId)?.apply {
                banned.add(userId)
                timestamp = Calendar.getInstance().timeInMillis

                shoppingListsDao.insert(this)
            }

            return true
        }

        return false
    }

    fun banUserFromList(listId: String, userId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(banUserFromListBlocking(listId, userId))
        }

        return result
    }

    suspend fun unBanUserFromListBlocking(listId: String, userId: String): Boolean {
        if (shoppingListsRemoteSource.unBanUserFromList(listId, userId)) {
            shoppingListsDao.getByIdPlain(listId)?.apply {
                banned.remove(userId)
                timestamp = Calendar.getInstance().timeInMillis

                shoppingListsDao.insert(this)
            }

            return true
        }

        return false
    }

    fun unBanUserFromList(listId: String, userId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(unBanUserFromListBlocking(listId, userId))
        }

        return result
    }

    fun getListUsers(listId: String): LiveData<List<String>?> {
        val result = MutableLiveData<List<String>?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(shoppingListsRemoteSource.getUsers(listId))
        }

        return result
    }

    fun updateListUsers(listId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                list.users = ArrayList(shoppingListsRemoteSource.getUsers(listId))

                shoppingListsDao.insert(list)
            }
        }
    }

    fun addItemToList(listId: String, item: Item): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                val index = list.items.indexOfFirst { it.id == item.id }
                val updatedItem = item.copy(timestamp = Calendar.getInstance().timeInMillis)

                if (list.keepInSync) {
                    if (shoppingListsRemoteSource.addItemToList(listId, updatedItem, false)) {
                        if (index == -1)
                            list.items.add(updatedItem)
                        else
                            list.items[index] = updatedItem

                        list.timestamp = Calendar.getInstance().timeInMillis

                        shoppingListsDao.insert(list)
                        result.postValue(true)
                    } else {
                        result.postValue(false)
                    }
                } else {
                    if (index == -1)
                        list.items.add(updatedItem)
                    else
                        list.items[index] = updatedItem

                    list.timestamp = Calendar.getInstance().timeInMillis

                    shoppingListsDao.insert(list)
                    result.postValue(true)
                }
            }
        }

        return result
    }

    fun removeItemFromList(listId: String, item: Item): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                val index = list.items.indexOfFirst { it.id == item.id }

                if (index != -1) {
                    val updatedItem = item.copy(deleted = true, timestamp = Calendar.getInstance().timeInMillis)

                    if (list.keepInSync) {
                        if (shoppingListsRemoteSource.removeItemFromList(listId, item)) {
                            list.items[index] = updatedItem
                            list.timestamp = Calendar.getInstance().timeInMillis

                            shoppingListsDao.insert(list)
                            result.postValue(true)
                        } else {
                            result.postValue(false)
                        }
                    } else {
                        list.items[index] = updatedItem
                        list.timestamp = Calendar.getInstance().timeInMillis

                        shoppingListsDao.insert(list)
                        result.postValue(true)
                    }
                } else {
                    result.postValue(false)
                }
            }
        }

        return result
    }

    fun deleteUnusedItems(listId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            deleteUnusedItemsBlocking(listId)
        }
    }

    fun setItemCompleted(listId: String, itemId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                val index = list.items.indexOfFirst { it.id == itemId }

                if (index != -1) {
                    if (list.keepInSync) {
                        val item = list.items[index].copy(completed = true, completedBy = FirebaseAuth.getInstance().currentUser?.uid, timestamp = Calendar.getInstance().timeInMillis)
                        list.items[index] = item
                        list.timestamp = Calendar.getInstance().timeInMillis

                        shoppingListsDao.insert(list)

                        if (shoppingListsRemoteSource.addItemToList(listId, item)) {
                            result.postValue(true)
                        } else {
                            list.items[index] = list.items[index].copy(completed = false, completedBy = null, timestamp = Calendar.getInstance().timeInMillis)
                            list.timestamp = Calendar.getInstance().timeInMillis

                            shoppingListsDao.insert(list)

                            result.postValue(false)
                        }
                    } else {
                        list.items[index] = list.items[index].copy(completed = true, completedBy = FirebaseAuth.getInstance().currentUser?.uid, timestamp = Calendar.getInstance().timeInMillis)
                        list.timestamp = Calendar.getInstance().timeInMillis

                        shoppingListsDao.insert(list)
                        result.postValue(true)
                    }
                } else {
                    result.postValue(false)
                }
            }
        }

        return result
    }

    fun setItemNotCompleted(listId: String, itemId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val list = shoppingListsDao.getByIdPlain(listId)

            if (list != null) {
                val index = list.items.indexOfFirst { it.id == itemId }

                if (index != -1) {
                    if (list.keepInSync) {
                        val previousCompletedBy = list.items[index].completedBy
                        val item = list.items[index].copy(completed = false, completedBy = null, timestamp = Calendar.getInstance().timeInMillis)
                        list.items[index] = item
                        list.timestamp = Calendar.getInstance().timeInMillis

                        shoppingListsDao.insert(list)

                        if (shoppingListsRemoteSource.addItemToList(listId, item)) {
                            result.postValue(true)
                        } else {
                            list.items[index] = list.items[index].copy(completed = true, completedBy = previousCompletedBy, timestamp = Calendar.getInstance().timeInMillis)
                            list.timestamp = Calendar.getInstance().timeInMillis

                            shoppingListsDao.insert(list)
                            result.postValue(false)
                        }
                    } else {
                        list.items[index] = list.items[index].copy(completed = false, completedBy = null, timestamp = Calendar.getInstance().timeInMillis)
                        list.timestamp = Calendar.getInstance().timeInMillis

                        shoppingListsDao.insert(list)
                        result.postValue(true)
                    }
                } else {
                    result.postValue(false)
                }
            } else {
                result.postValue(false)
            }
        }

        return result
    }

    fun syncAllListsMetadata(): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val allLists = shoppingListsDao.getAllIds()

            for (id in allLists) {
                if (shoppingListsDao.isSynced(id)) {
                    val list = shoppingListsRemoteSource.getListMetadata(id)

                    if (list != null && shoppingListsDao.getTimestamp(id) < list.timestamp) {
                        val localList = shoppingListsDao.getByIdPlain(id)

                        if (localList != null) {
                            list.items = localList.items
                            list.users = localList.users

                            shoppingListsDao.insert(list)
                        }
                    }
                }
            }

            result.postValue(true)
        }

        return result
    }

    fun syncAllLists(): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            result.postValue(syncAllListsBlocking())
        }

        return result
    }

    fun syncList(listId: String): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            syncListBlocking(listId)

            result.postValue(true)
        }

        return result
    }

    fun trySettingOwner(): LiveData<Boolean?> {
        val result = MutableLiveData<Boolean?>(null)

        GlobalScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid != null) {
                for (list in shoppingListsDao.getAllPlain()) {
                    if (list.owner == null)
                        list.owner = uid

                    for (index in 0 until list.items.size) {
                        var item = list.items[index]

                        if (item.addedBy == null)
                            item = item.copy(addedBy = uid)

                        if (item.completed && item.completedBy == null)
                            item = item.copy(completedBy = uid)

                        list.items[index] = item
                    }

                    shoppingListsDao.update(list)
                }
            }

            result.postValue(true)
        }

        return result
    }

    fun getListPlain(listId: String) = shoppingListsDao.getByIdPlain(listId)

    fun startListeningForItemsChanges(listId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            if (shoppingListsDao.isSynced(listId)) {
                shoppingListsRemoteSource.startListeningForItemsChanges(listId) { items ->
                    GlobalScope.launch(Dispatchers.IO) {
                        shoppingListsDao.getByIdPlain(listId)?.let { list ->
                            for (item in items) {
                                val index = list.items.indexOfFirst { it.id == item.id }

                                if (index == -1)
                                    list.items.add(item)
                                else
                                    list.items[index] = item

                                list.timestamp = Calendar.getInstance().timeInMillis
                            }

                            shoppingListsDao.insert(list)
                        }
                    }
                }
            }
        }
    }

    fun startListeningForMetadataChanges(listId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            if (shoppingListsDao.isSynced(listId)) {
                shoppingListsRemoteSource.startListeningForMetadataChanges(listId) { updatedList ->
                    GlobalScope.launch(Dispatchers.IO) {
                        shoppingListsDao.getByIdPlain(listId)?.let { list ->
                            updatedList.items = list.items
                            updatedList.users = list.users

                            shoppingListsDao.insert(updatedList)
                        }
                    }
                }
            }
        }
    }

    fun stopListeningForItemsChanges() = shoppingListsRemoteSource.stopListeningForItemsChanges()

    fun stopListeningForMetadataChanges() = shoppingListsRemoteSource.stopListeningForMetadataChanges()

    suspend fun deleteUnusedItemsBlocking(listId: String) {
        shoppingListsDao.getByIdPlain(listId)?.let {
            val iterator = it.items.listIterator()

            while (iterator.hasNext()) {
                val item = iterator.next()

                if (item.deleted && Calendar.getInstance().timeInMillis - item.timestamp >= Values.STALE_ITEM_DELETION_DELAY) {
                    shoppingListsRemoteSource.deleteItemFromList(listId, item.id)
                    iterator.remove()
                }
            }

            shoppingListsDao.insert(it)
        }
    }

    suspend fun syncListBlocking(listId: String): Boolean {
        if (shoppingListsDao.isSynced(listId)) {
            val list = shoppingListsRemoteSource.getListMetadata(listId)

            if (list != null) {
                if (shoppingListsDao.getTimestamp(listId) >= list.timestamp)
                    return true

                val localList = shoppingListsDao.getByIdPlain(listId)

                if (localList != null) {
                    list.items = localList.items
                    list.users = localList.users

                    for (item in shoppingListsRemoteSource.getUpdatedItems(listId, localList.timestamp)) {
                        val index = list.items.indexOfFirst { it.id == item.id }

                        if (index == -1) {
                            list.items.add(item)
                        } else {
                            list.items[index] = item
                        }
                    }

                    shoppingListsDao.insert(list)

                    return true
                }
            } else {
                shoppingListsDao.setKeepSynced(listId, false)
                shoppingListsDao.changeOwner(listId, FirebaseAuth.getInstance().currentUser?.uid)
                shoppingListsDao.setNewId(listId)
            }
        }

        return false
    }

    suspend fun syncAllListsBlocking(): Boolean {
        val allLists = shoppingListsDao.getAllIds()

        val jobs = ArrayList<Job>()

        for (id in allLists) {
            jobs.add(
                GlobalScope.launch(Dispatchers.IO) {
                    syncListBlocking(id)
                }
            )
        }

        for (job in jobs)
            job.join()

        return true
    }

    suspend fun getRemoteTimestampBlocking(listId: String) = shoppingListsRemoteSource.getTimestamp(listId)

    suspend fun getRemoteUsersBlocking(listId: String) = shoppingListsRemoteSource.getUsers(listId)

    suspend fun downloadListBlocking(listId: String): ShoppingList? {
        try {
            UUID.fromString(listId)
        } catch (e: IllegalArgumentException) {
            return null
        }

        val localList = shoppingListsDao.getByIdPlain(listId)

        if (localList != null) {
            syncListBlocking(listId)

            return localList
        } else {
            val list = shoppingListsRemoteSource.getList(listId)

            if (list != null) {
                shoppingListsDao.insert(list)
                return list
            }
        }

        return null
    }

    suspend fun downloadListBlockingNoSafeChecks(listId: String) = shoppingListsRemoteSource.getList(listId)

    suspend fun downloadOrSyncListBlocking(listId: String): Boolean {
        val localList = shoppingListsDao.getByIdPlain(listId)
        if (localList != null) {
            return syncListBlocking(listId)
        } else {
            val list = downloadListBlockingNoSafeChecks(listId)

            if (list != null) {
                shoppingListsDao.insert(list)

                return true
            }
        }

        return false
    }

    suspend fun deleteRemoteListBlocking(listId: String) = shoppingListsRemoteSource.deleteList(listId)

    suspend fun changeListMetadataBlocking(list: ShoppingList) {
        if (list.keepInSync) {
            if (shoppingListsRemoteSource.changeListMetadata(list)) {
                shoppingListsDao.update(list)
            }
        } else {
            shoppingListsDao.update(list)
        }
    }

    suspend fun deleteOrReleaseListBlocking(listId: String) = shoppingListsRemoteSource.deleteOrReleaseList(listId)
}