package io.github.jpiasecki.shoppinglist.repositories

import io.github.jpiasecki.shoppinglist.database.Item
import io.github.jpiasecki.shoppinglist.database.ItemsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemsRepository @Inject constructor(
    private val itemsDao: ItemsDao
) {

    fun delete(item: Item) {
        GlobalScope.launch(Dispatchers.IO) {
            itemsDao.delete(item)
        }
    }

    fun insert(item: Item) {
        GlobalScope.launch(Dispatchers.IO) {
            itemsDao.insert(item)
        }
    }

    suspend fun getForAutoComplete() = itemsDao.getForAutoComplete().distinctBy { it.name }
}