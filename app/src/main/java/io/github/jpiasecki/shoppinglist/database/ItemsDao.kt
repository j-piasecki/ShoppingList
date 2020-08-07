package io.github.jpiasecki.shoppinglist.database

import androidx.room.*

@Dao
interface ItemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Item)

    @Delete
    fun delete(item: Item)

    @Query("SELECT * FROM items ORDER BY timestamp DESC LIMIT 300")
    suspend fun getForAutoComplete(): List<Item>
}