package io.github.jpiasecki.shoppinglist.database

import androidx.room.*

@Dao
interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE id=(:id)")
    fun getById(id: String): User
}