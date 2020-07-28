package io.github.jpiasecki.shoppinglist.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface ShoppingListsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: ShoppingList)

    @Delete
    fun delete(list: ShoppingList)

    @Query("DELETE FROM shopping_lists WHERE id = (:listId)")
    fun delete(listId: String)

    @Update
    fun update(list: ShoppingList)

    @Query("SELECT * FROM shopping_lists ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists ORDER BY timestamp DESC")
    fun getAllPlain(): List<ShoppingList>

    @Query("SELECT * FROM shopping_lists WHERE id=(:id)")
    fun getById(id: String): LiveData<ShoppingList>

    @Query("SELECT * FROM shopping_lists WHERE id=(:id)")
    fun getByIdPlain(id: String): ShoppingList?

    @Query("UPDATE shopping_lists SET keepInSync = 1 WHERE id = (:id)")
    fun setKeepSynced(id: String)

    @Query("UPDATE shopping_lists SET name = (:name) WHERE id = (:id)")
    fun rename(id: String, name: String)

    @Query("UPDATE shopping_lists SET note = (:note) WHERE id = (:id)")
    fun changeNote(id: String, note: String)

    @Query("UPDATE shopping_lists SET owner = (:owner) WHERE id = (:id)")
    fun changeOwner(id: String, owner: String?)

    @Query("SELECT id from shopping_lists")
    fun getAllIds(): List<String>

    @Query("SELECT keepInSync FROM shopping_lists WHERE id = (:id)")
    fun isSynced(id: String): Boolean

    @Query("SELECT timestamp from shopping_lists WHERE id = (:id)")
    fun getTimestamp(id: String): Long

    @Query("UPDATE shopping_lists SET timestamp = (:timestamp) WHERE id = (:id)")
    fun updateTimestamp(id: String, timestamp: Long = Calendar.getInstance().timeInMillis)
}