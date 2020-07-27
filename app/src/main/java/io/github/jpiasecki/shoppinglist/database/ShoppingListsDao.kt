package io.github.jpiasecki.shoppinglist.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShoppingListsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: ShoppingList)

    @Delete
    fun delete(list: ShoppingList)

    @Update
    fun update(list: ShoppingList)

    @Query("SELECT * FROM shopping_lists ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE id=(:id)")
    fun getById(id: String): LiveData<ShoppingList>
}